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
package de.jost_net.JVerein.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class SplitbuchungsContainer
{
  private static ArrayList<Buchung> splitbuchungen = null;

  private static int dependencyid = 0;
  
  private static Buchung[] buchungen = null;
  
  private static int anzahl = 0;
  
  private static String text = null; 

  public static void init(Buchung[] bl)
      throws RemoteException, ApplicationException
  {
    anzahl = bl.length;
    if (anzahl == 1)
    {
      buchungen = null;
      text = "Es wird eine Splitbuchung erzeugt.";
    }
    else
    {
      buchungen = bl;
      text = String.format("Es werden %s Splitbuchungen erzeugt.", anzahl);
    }
    initiate(bl[0]);
  }
  
  public static void init(Buchung b)
      throws RemoteException, ApplicationException
  {
    buchungen = null;
    anzahl = 1;
    text = "Es wird eine Splitbuchung erzeugt.";
    initiate(b);
  }
  
  public static void initiate(Buchung b)
      throws RemoteException, ApplicationException
  {
    splitbuchungen = new ArrayList<>();
    dependencyid = 0;

    // Wenn eine gesplittete Buchung aufgerufen wird, wird die Hauptbuchung
    // gelesen
    if (b.getSplitId() != null)
    {
      b = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
          b.getSplitId() + "");
    }
    else
    {
      b.setSplitId(Long.valueOf(b.getID()));
      SplitbuchungsContainer.add(b);
    }
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.addFilter("splitid = ?", b.getID());

    if (!it.hasNext())
    {
      // Wenn keine Buchung gefunden wurde, gibt es auch keine Gegenbuchung.
      // Dann wird die jetzt erstellt.
      Buchung b2 = getGegenbuchung(b);
      SplitbuchungsContainer.add(b2);
    }
    while (it.hasNext())
    {
      Buchung buchung = (Buchung) it.next();
      SplitbuchungsContainer.add(buchung);
      dependencyid = Math.max(dependencyid, buchung.getDependencyId());
    }
  }

  public static ArrayList<Buchung> get()
  {
    return splitbuchungen;
  }

  public static Buchung getMaster() throws RemoteException
  {
    for (Buchung b : splitbuchungen)
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        return b;
      }
    }
    throw new RemoteException("Hauptbuchung fehlt");
  }

  public static void add(Buchung b) throws RemoteException, ApplicationException
  {
    b.setSpeicherung(false);
    if (!splitbuchungen.contains(b))
    {
      splitbuchungen.add(b);
    }
  }

  public static BigDecimal getSumme(Integer typ) throws RemoteException
  {
    BigDecimal summe = BigDecimal.valueOf(0).setScale(2);
    for (Buchung b : splitbuchungen)
    {
      if (b.getSplitTyp().equals(typ) && !b.isToDelete())
      {
        summe = summe.add(BigDecimal.valueOf(b.getBetrag()).setScale(2,
            RoundingMode.HALF_UP));
      }
    }
    return summe;
  }

  public static String getDifference() throws RemoteException
  {
    return Einstellungen.DECIMALFORMAT.format(getSumme(SplitbuchungTyp.HAUPT)
        .subtract(getSumme(SplitbuchungTyp.SPLIT)));
  }

  public static void aufloesen() throws RemoteException, ApplicationException
  {
    for (Buchung b : splitbuchungen)
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        b.setSplitId(null);
        b.setSplitTyp(null);
        b.store();
      }
      else
      {
        if (b.getSpendenbescheinigung() != null)
          b.getSpendenbescheinigung().delete();
        b.delete();
      }
    }
    splitbuchungen.clear();
  }

  public static void store() throws RemoteException, ApplicationException
  {
    if (splitbuchungen.size() == 0) // Splitbuchungen wurden aufgelöst
    {
      return;
    }
    BigDecimal gegen = getSumme(SplitbuchungTyp.GEGEN)
        .multiply(BigDecimal.valueOf(-1));
    if (!getSumme(SplitbuchungTyp.HAUPT).equals(gegen))
    {
      throw new RemoteException(
          "Die Minusbuchung muss den gleichen Betrag mit umgekehrtem Vorzeichen wie die Hauptbuchung haben.");
    }
    BigDecimal differenz = getSumme(SplitbuchungTyp.HAUPT)
        .subtract(getSumme(SplitbuchungTyp.SPLIT));
    if (!differenz.equals(BigDecimal.valueOf(0).setScale(2)))
    {
      throw new RemoteException(
          "Differenz zwischen Hauptbuchung und Splitbuchungen: " + differenz);
    }

    Buchungsart ba_haupt = null;
    Buchungsart ba_gegen = null;
    for (Buchung b : get())
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        ba_haupt = b.getBuchungsart();
        if (ba_haupt == null)
        {
          throw new RemoteException("Buchungsart bei der Hauptbuchung fehlt");
        }
      }
      if (b.getSplitTyp() == SplitbuchungTyp.GEGEN)
      {
        ba_gegen = b.getBuchungsart();
        if (ba_gegen == null)
        {
          throw new RemoteException("Buchungsart bei der Gegenbuchung fehlt");
        }
      }
    }
    if (ba_haupt.getNummer() != ba_gegen.getNummer())
    {
      throw new RemoteException(
          "Buchungsarten bei Haupt- und Gegenbuchung müssen identisch sein");
    }
    try
    {
      DBTransaction.starten();
      handleStore();
      DBTransaction.commit();
    }
    catch (Exception ex)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(ex.getMessage());
      throw ex;
    }
  }
  
  public static void handleStore() throws RemoteException, ApplicationException
  {
    for (Buchung b : get())
    {
      if (b.isToDelete())
      {
        if (b.getSpendenbescheinigung() != null)
          b.getSpendenbescheinigung().delete();
        b.delete();
      }
      else
      {
        b.store();
      }
    }
    if (buchungen != null)
    {
      Buchung master = null;
      Buchung gegen = null;
      Buchung split = null;
      for (int i = 1; i < anzahl; i++)
      {
        master = buchungen[i];
        master.setSplitTyp(SplitbuchungTyp.HAUPT);
        master.setSplitId(Long.valueOf(master.getID()));
        master.store();
        gegen = getGegenbuchung(master);
        gegen.store();
        for (Buchung b : get())
        {
          if (!b.isToDelete() && (b.getSplitTyp() == SplitbuchungTyp.SPLIT))
          {
            split = getSplitbuchung(master, b);
            split.store();
          }
        }
      }
      buchungen = null;
      splitbuchungen.clear();
    }
  }

  public static int getNewDependencyId() {
    return ++dependencyid;
  }
  
  public static String getText() {
    return text;
  }
  
  private static Buchung getGegenbuchung(Buchung b) throws RemoteException
  {
    Buchung buch = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    buch.setArt(b.getArt());
    buch.setAuszugsnummer(b.getAuszugsnummer());
    buch.setBetrag(b.getBetrag() * -1);
    buch.setBlattnummer(b.getBlattnummer());
    buch.setBuchungsartId(b.getBuchungsartId());
    buch.setBuchungsklasseId(b.getBuchungsklasseId());
    buch.setDatum(b.getDatum());
    buch.setKommentar(b.getKommentar());
    buch.setKonto(b.getKonto());
    buch.setMitgliedskonto(b.getMitgliedskonto());
    buch.setName(b.getName());
    buch.setProjekt(b.getProjekt());
    buch.setSplitId(Long.valueOf(b.getID()));
    buch.setUmsatzid(b.getUmsatzid());
    buch.setZweck(b.getZweck());
    buch.setSplitTyp(SplitbuchungTyp.GEGEN);
    return buch;
  }
  
  private static Buchung getSplitbuchung(Buchung master, Buchung origin) throws RemoteException
  {
    Buchung buch = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
        null);
    buch.setAuszugsnummer(master.getAuszugsnummer());
    buch.setBetrag(origin.getBetrag());
    buch.setBlattnummer(master.getBlattnummer());
    buch.setBuchungsartId(origin.getBuchungsartId());
    buch.setBuchungsklasseId(origin.getBuchungsklasseId());
    buch.setDatum(master.getDatum());
    buch.setKommentar(origin.getKommentar());
    buch.setKonto(master.getKonto());
    buch.setMitgliedskonto(master.getMitgliedskonto());
    buch.setName(master.getName());
    buch.setProjekt(master.getProjekt());
    buch.setSplitId(Long.valueOf(master.getID()));
    buch.setUmsatzid(master.getUmsatzid());
    buch.setZweck(origin.getZweck());
    buch.setDependencyId(origin.getDependencyId());
    buch.setSplitTyp(SplitbuchungTyp.SPLIT);
    return buch;
  }
  
  public static int getAnzahl() 
  {
    return anzahl;
  }
}