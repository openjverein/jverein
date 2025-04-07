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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
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
      HashMap<Buchung, Boolean> zweckeVonHauptbuchung = new HashMap<>();
      HashMap<Buchung, Boolean> kommentareVonHauptbuchung = new HashMap<>();
      Boolean zweckVonHauptbuchung = false;
      Boolean kommentarVonHauptbuchung = false;
      for (Buchung b : get())
      {
        zweckVonHauptbuchung = false;
        kommentarVonHauptbuchung = false;
        if (!b.isToDelete() && (b.getSplitTyp() == SplitbuchungTyp.SPLIT))
        {
          if (buchungen[0].getZweck() != null
              && buchungen[0].getZweck().equals(b.getZweck()))
          {
            zweckVonHauptbuchung = true;
          }
          zweckeVonHauptbuchung.put(b, zweckVonHauptbuchung);
          if (buchungen[0].getKommentar() != null
              && buchungen[0].getKommentar().equals(b.getKommentar()))
          {
            kommentarVonHauptbuchung = true;
          }
          kommentareVonHauptbuchung.put(b, kommentarVonHauptbuchung);
        }
      }
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
            split = getSplitbuchung(master, b, zweckeVonHauptbuchung,
                kommentareVonHauptbuchung);
            split.store();
          }
        }
      }
      buchungen = null;
      splitbuchungen.clear();
    }
  }

  public static int getNewDependencyId()
  {
    return ++dependencyid;
  }

  public static String getText()
  {
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
    buch.setSollbuchungID(b.getSollbuchungID());
    buch.setName(b.getName());
    buch.setProjekt(b.getProjekt());
    buch.setSplitId(Long.valueOf(b.getID()));
    buch.setUmsatzid(b.getUmsatzid());
    buch.setZweck(b.getZweck());
    buch.setIban(b.getIban());
    buch.setSplitTyp(SplitbuchungTyp.GEGEN);
    return buch;
  }

  private static Buchung getSplitbuchung(Buchung master, Buchung origin,
      HashMap<Buchung, Boolean> zweckeVonHauptbuchung,
      HashMap<Buchung, Boolean> kommentareVonHauptbuchung)
      throws RemoteException
  {
    Buchung buch = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    buch.setAuszugsnummer(master.getAuszugsnummer());
    buch.setBetrag(origin.getBetrag());
    buch.setBlattnummer(master.getBlattnummer());
    buch.setBuchungsartId(origin.getBuchungsartId());
    buch.setBuchungsklasseId(origin.getBuchungsklasseId());
    buch.setDatum(master.getDatum());
    if (kommentareVonHauptbuchung.get(origin) == true)
    {
      buch.setKommentar(master.getKommentar());
    }
    else
    {
      buch.setKommentar(origin.getKommentar());
    }
    buch.setKonto(master.getKonto());
    buch.setSollbuchungID(master.getSollbuchungID());
    buch.setName(master.getName());
    buch.setProjekt(master.getProjekt());
    buch.setSplitId(Long.valueOf(master.getID()));
    buch.setUmsatzid(master.getUmsatzid());
    if (zweckeVonHauptbuchung.get(origin) == true)
    {
      buch.setZweck(master.getZweck());
    }
    else
    {
      buch.setZweck(origin.getZweck());
    }
    buch.setIban(master.getIban());
    buch.setDependencyId(origin.getDependencyId());
    buch.setSplitTyp(SplitbuchungTyp.SPLIT);
    return buch;
  }

  /**
   * Splittet eine Buchung anhand der in der Sollbuchung enthaltenen
   * Sollbuchungspositionen.
   * 
   * @param Buchung
   *          die zu splittende Buchung
   * @param Sollbuchung
   *          die Sollbuchung, die der Buchung zugewiesen werden soll
   * @param immerSpliten
   *          auch bei nur einer Sollbuchungsposition splitten
   * @return Wenn die Beträge von Sollbuchung und Buchung verschieden sind, wird
   *         die erzeugte Restbuchung zurückgegeben, sonst <code>null</code>.
   */
  public static Buchung autoSplit(Buchung buchung, Sollbuchung sollb,
      boolean immerSplitten)
      throws NumberFormatException, RemoteException, ApplicationException
  {
    boolean splitten = false;
    Buchung restBuchung = null;
    if (sollb == null)
    {
      buchung.setSollbuchung(null);
      buchung.store();
      return null;
    }

    HashMap<String, Double> splitMap = new HashMap<>();
    HashMap<String, String> splitZweckMap = new HashMap<>();
    ArrayList<SollbuchungPosition> spArray = sollb.getSollbuchungPositionList();
    try
    {
      for (SollbuchungPosition sp : spArray)
      {
        // Wenn eine Buchungsart fehlt können wir nicht automatisch splitten
        if (sp.getBuchungsartId() == null)
        {
          throw new ApplicationException(
              "Es haben nicht alle Sollbuchungspositionen eine Buchungsart.");
        }
        String key = sp.getBuchungsartId() + "-"
            + (sp.getBuchungsklasseId() != null ? sp.getBuchungsklasseId()
                : "");
        Double betrag = splitMap.getOrDefault(key, 0d);
        if (sp.getBetrag().doubleValue() == 0)
        {
          continue;
        }

        splitMap.put(key, betrag + sp.getBetrag().doubleValue());
        String zweck = splitZweckMap.get(key);
        if (zweck == null)
        {
          zweck = sp.getZweck() + " " + sp.getBetrag();
        }
        else
        {
          zweck = zweck + ", " + sp.getZweck() + " " + sp.getBetrag();
        }
        splitZweckMap.put(key, zweck);
      }

      if ((splitMap.size() > 1 && sollb.getBetrag().equals(buchung.getBetrag()))
          || immerSplitten)
      {
        splitten = true;
      }
      else if (splitMap.size() > 1)
      {
        YesNoDialog dialog = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        dialog.setTitle("Buchung splitten");
        dialog.setText(
            "Der Betrag der Sollbuchung entspricht nicht dem der Buchung.\n"
                + "Soll die Buchung trotzdem anhand der Sollbuchungspositionen\n"
                + "gesplittet und eine Restbuchung erzeugt werden?");
        if (!((Boolean) dialog.open()).booleanValue())
        {
          throw new OperationCanceledException();
        }
        splitten = true;
      }
      if (splitten)
      { 
        boolean ersetzen = false;
        if (buchung.getBuchungsartId() == null
            && spArray.get(0).getBuchungsartId() != null)
        {
          buchung.setBuchungsartId(spArray.get(0).getBuchungsartId());
        }
        if (buchung.getBuchungsklasseId() == null
            && spArray.get(0).getBuchungsklasseId() != null)
        {
          buchung.setBuchungsklasseId(spArray.get(0).getBuchungsklasseId());
        }

        if (buchung.getSplitTyp() == null)
        {
          buchung.setSplitTyp(SplitbuchungTyp.HAUPT);
          buchung.setSollbuchungID(null);
        }
        // Haupt- und Gegen-Buchungen können nicht gesplittet werden.
        else if (buchung.getSplitTyp() == SplitbuchungTyp.GEGEN
            || buchung.getSplitTyp() == SplitbuchungTyp.HAUPT)
        {
          throw new ApplicationException(
              "Splitten von Haupt und Gegenbuchung nicht möglich.");
        }
        else
        {
          // Spitbuchungen müssen durch die neuen Buchungen ersetzt werden
          ersetzen = true;
        }

        SplitbuchungsContainer.init(buchung);

        if (ersetzen)
        {
          for (Buchung b : splitbuchungen)
          {
            if (b.getID().equals(buchung.getID()))
            {
              if (b.getSpendenbescheinigung() != null)
              {
                Logger.error(
                    "Splitbuchung ist einer Spendenbescheinigung zugeordnet, neu splitten nicht möglich.");
                throw new ApplicationException(
                    "Splitbuchung ist einer Spendenbescheinigung zugeordnet, neu splitten nicht möglich.");
              }
              b.setDelete(true);
              break;
            }
          }
        }
      
        boolean splitPositionZweck = Einstellungen.getEinstellung()
            .getSplitPositionZweck();
        Iterator<Entry<String, Double>> iterator = splitMap.entrySet()
            .iterator();
        while (iterator.hasNext())
        {
          Entry<String, Double> entry = iterator.next();

          Buchung splitBuchung = (Buchung) Einstellungen.getDBService()
              .createObject(Buchung.class, null);
          splitBuchung.setBetrag(entry.getValue());
          splitBuchung.setDatum(buchung.getDatum());
          splitBuchung.setKonto(buchung.getKonto());
          splitBuchung.setName(buchung.getName());
          if (splitPositionZweck)
          {
            splitBuchung.setZweck(splitZweckMap.get(entry.getKey()));
          }
          else
          {
            splitBuchung.setZweck(buchung.getZweck());
          }
          splitBuchung.setSollbuchung(sollb);
          String buchungsart = entry.getKey().substring(0,
              entry.getKey().indexOf("-"));
          splitBuchung.setBuchungsartId(Long.parseLong(buchungsart));
          String buchungsklasse = entry.getKey()
              .substring(entry.getKey().indexOf("-") + 1);
          if (buchungsklasse.length() > 0)
          {
            splitBuchung.setBuchungsklasseId(Long.parseLong(buchungsklasse));
          }
          splitBuchung.setSplitTyp(SplitbuchungTyp.SPLIT);
          splitBuchung.setSplitId(Long.parseLong(getMaster().getID()));

          SplitbuchungsContainer.add(splitBuchung);
        }
        if (!sollb.getBetrag().equals(buchung.getBetrag()))
        {
          restBuchung = (Buchung) Einstellungen.getDBService()
              .createObject(Buchung.class, null);
          restBuchung.setBetrag(buchung.getBetrag() - sollb.getBetrag());
          restBuchung.setDatum(buchung.getDatum());
          restBuchung.setKonto(buchung.getKonto());
          restBuchung.setName(buchung.getName());
          restBuchung.setZweck(buchung.getZweck());
          restBuchung.setSplitTyp(SplitbuchungTyp.SPLIT);
          restBuchung.setSplitId(Long.parseLong(getMaster().getID()));
          restBuchung.setBuchungsartId(buchung.getBuchungsartId());
          restBuchung.setBuchungsklasseId(buchung.getBuchungsklasseId());

          SplitbuchungsContainer.add(restBuchung);
        }
        SplitbuchungsContainer.store();
      }
    }
    catch (OperationCanceledException oce)
    {
      splitten = false;
    }
    catch (Exception e)
    {
      if (immerSplitten)
      {
        throw new ApplicationException(e.getLocalizedMessage());
      }
      splitten = false;
      if (splitbuchungen != null)
      {
        splitbuchungen.clear();
      }
      GUI.getStatusBar().setErrorText(
          "Fehler beim Autosplit, ordne Buchung Sollbuchung ohne splitten zu.");
    }
    if (!splitten)
    {
      // Wenn kein automatisches Spliten möglich ist nur Buchungsart,
      // Buchungsklasse und Sollbuchung zuweisen
      if (spArray.get(0).getBuchungsartId() != null)
      {
        buchung.setBuchungsartId(spArray.get(0).getBuchungsartId());
      }
      if (spArray.get(0).getBuchungsklasseId() != null)
      {
        buchung.setBuchungsklasseId(spArray.get(0).getBuchungsklasseId());
      }
      buchung.setSollbuchung(sollb);
      buchung.store();
    }
    return restBuchung;
  }

  public static int getAnzahl()
  {
    return anzahl;
  }
}