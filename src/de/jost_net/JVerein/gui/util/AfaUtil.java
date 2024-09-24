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

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class AfaUtil
{

  public static int doAbschreibung(Geschaeftsjahr aktuellesGJ)
      throws RemoteException, ParseException, ApplicationException
  {
    DBService service;
    int anzahlBuchungen = 0;

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
    kontenIt.addFilter("anschaffung IS NOT NULL");
    kontenIt.addFilter("nutzungsdauer IS NOT NULL");
    kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)",
        new Object[] { new java.sql.Date(calendar.getTimeInMillis())  });
    kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)",
        new Object[] { new java.sql.Date(calendar.getTimeInMillis())  });
    while (kontenIt.hasNext())
    {
      Konto konto = kontenIt.next();
      switch(konto.getAfaMode())
      {
        case AfaMode.ANGEPASST:
          anzahlBuchungen += doAbschreibungAngepasst(konto, aktuellesJahr, 
              ersterMonatAktuellesGJ, afaBuchungDatum);
          break;
      }
    }
    if (anzahlBuchungen > 0)
    {
      GUI.getStatusBar().setSuccessText("Abschreibungen erfolgreich erstellt");
    }
    else
    {
      GUI.getStatusBar().setSuccessText("Keine Abschreibung im aktuellen Geschäftjahr nötig");
    }
    return anzahlBuchungen;
  }

  private static int doAbschreibungAngepasst(Konto konto, int aktuellesJahr, 
      int ersterMonatAktuellesGJ, Date afaBuchungDatum) 
          throws RemoteException, ParseException, ApplicationException
  {
    if (konto.getAfaStart() == null || konto.getAfaDauer() == null || 
        konto.getAfaRestwert() == null)
      throw new ApplicationException("Daten für angepasste Abschreibung fehlen ";
    int anschaffungsJahr;
    int monatAnschaffung;
    int anzahlBuchungen = 0;
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
    buchung.store();
    anzahlBuchungen++;
    return anzahlBuchungen;
  }

}
