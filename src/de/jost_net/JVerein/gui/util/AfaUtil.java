/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.util;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AfaUtil
{

  public AfaUtil(final Geschaeftsjahr aktuellesGJ, 
      final Jahresabschluss abschluss)
  {
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          monitor.setStatusText("Genreriere Abschreibungen");
          
          int anzahlBuchungen = 0;
          DBService service;
          Calendar calendar = Calendar.getInstance();
          // Aktuelles Geschäftsjahr bestimmen
          int aktuellesJahr = aktuellesGJ.getBeginnGeschaeftsjahrjahr();
          calendar.setTime(aktuellesGJ.getBeginnGeschaeftsjahr());
          int ersterMonatAktuellesGJ = calendar.get(Calendar.MONTH);
          // AfA Buchungen zu Ende des aktuellen GJ
          Date afaBuchungDatum = aktuellesGJ.getEndeGeschaeftsjahr();

          service = Einstellungen.getDBService();
          DBIterator<Konto> kontenIt = service.createList(Konto.class);
          kontenIt.addFilter("anlagenkonto = TRUE");
          kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)",
              new Object[] { new java.sql.Date(calendar.getTimeInMillis())  });
          kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)",
              new Object[] { new java.sql.Date(calendar.getTimeInMillis())  });
          while (kontenIt.hasNext())
          {
            Konto konto = kontenIt.next();
            if (konto.getAfaMode() == null)
            {
              monitor.setStatusText("Konto " + konto.getNummer() + ": Afa Mode nicht gesetzt");
              continue;
            }
            switch(konto.getAfaMode())
            {
              case AfaMode.ANGEPASST:
                anzahlBuchungen += doAbschreibungAngepasst(konto, aktuellesJahr, 
                    ersterMonatAktuellesGJ, afaBuchungDatum, abschluss, monitor);
                break;
            }
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl generierter Buchungen: %d", anzahlBuchungen));
          GUI.getCurrentView().reload();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        catch (ParseException e)
        {
          e.printStackTrace();
        }
      } // Ende Run

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }

  private int doAbschreibungAngepasst(Konto konto, int aktuellesJahr, 
      int ersterMonatAktuellesGJ, Date afaBuchungDatum, Jahresabschluss abschluss,
      ProgressMonitor monitor) 
          throws RemoteException, ParseException, ApplicationException
  {
    if (checkKonto(konto, monitor))
      return 0;
    if (checkAfa(konto, monitor))
      return 0;
    int anschaffungsJahr;
    int monatAnschaffung;
    Calendar calendar = Calendar.getInstance();
    Geschaeftsjahr anschaffungGJ = new Geschaeftsjahr(konto.getAnschaffung());
    anschaffungsJahr = anschaffungGJ.getBeginnGeschaeftsjahrjahr();
    calendar.setTime(konto.getAnschaffung());
    monatAnschaffung = calendar.get(Calendar.MONTH);
    // Check ob ausserhalb des Abschreibungszeitraums
    if (aktuellesJahr < anschaffungsJahr || 
        aktuellesJahr > anschaffungsJahr + konto.getNutzungsdauer())
      return 0;
    // Check ob Anschaffung im ersten Monaz des GJ, dann keine Restabschreibung
    // Wenn Nutzungsdauer 0 dann direktabschreibung
    if ((aktuellesJahr == anschaffungsJahr + konto.getNutzungsdauer() && 
        ersterMonatAktuellesGJ == monatAnschaffung) &&
        konto.getNutzungsdauer() != 0)
      return 0;
    
    Buchung buchung = (Buchung) Einstellungen.getDBService().
        createObject(Buchung.class, null);
    buchung.setKonto(konto);
    buchung.setName(Einstellungen.getEinstellung().getName());
    buchung.setZweck(konto.getAfaart().getBezeichnung());
    buchung.setDatum(afaBuchungDatum);
    buchung.setBuchungsart(konto.getAfaartId());
    if (aktuellesJahr == anschaffungsJahr)
      buchung.setBetrag(-konto.getAfaStart());
    else if (aktuellesJahr < anschaffungsJahr + konto.getNutzungsdauer())
      buchung.setBetrag(-konto.getAfaDauer());
    else if (konto.getNutzungsdauer() == 1)
      buchung.setBetrag(-konto.getAfaDauer());
    else
      buchung.setBetrag(-konto.getAfaDauer() + konto.getAfaStart());
    if (abschluss != null)
      buchung.setAbschluss(abschluss);
    buchung.store();
    monitor.setStatusText("Konto " + konto.getNummer() + ": AfA Buchung erzeugt");
    return 1;
  }
  
  private boolean checkKonto(Konto konto, ProgressMonitor monitor) throws RemoteException 
  {
    boolean fehler = false;
    if (konto.getBetrag() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Anlagenwert nicht gesetzt");
      fehler = true;
    }
    if (konto.getAnschaffung() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Anschaffungsdatum nicht gesetzt");
      fehler = true;
    }
    if (konto.getNutzungsdauer() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Nutzungsdauer nicht gesetzt");
      fehler = true;
    }
    if (konto.getAfaRestwert() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Anlagen Restwert nicht gesetzt");
      fehler = true;
    }
    return fehler;
  }
  
  private boolean checkAfa(Konto konto, ProgressMonitor monitor) throws RemoteException 
  {
    boolean fehler = false;
    if (konto.getAfaStart() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Afa Erstes Jahr nicht gesetzt");
      fehler = true;
    }
    if (konto.getAfaDauer() == null)
    {
      monitor.setStatusText("Konto " + konto.getNummer() + ": Afa Folgejahr nicht gesetzt");
      fehler = true;
    }
    return fehler;
  }

}
