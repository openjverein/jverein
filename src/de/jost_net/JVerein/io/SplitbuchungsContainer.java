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
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SplitbuchungsContainer
{
  private static ArrayList<Buchung> splitbuchungen = null;

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
      text = String.format(
          "Es werden %s Splitbuchungen erzeugt.\nGeänderte Zwecke oder Kommentare in Splitpositionen werden bei allen Buchungen übernommen.",
          anzahl);
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
      Buchung b2 = createSplitbuchung(b);
      b2.setSplitTyp(SplitbuchungTyp.GEGEN);
      SplitbuchungsContainer.add(b2);
    }
    while (it.hasNext())
    {
      Buchung buchung = (Buchung) it.next();
      SplitbuchungsContainer.add(buchung);
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
    Steuer steuer_haupt = null;
    Steuer steuer_gegen = null;

    for (Buchung b : get())
    {
      if (b.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        ba_haupt = b.getBuchungsart();
        if (ba_haupt == null)
        {
          throw new RemoteException("Buchungsart bei der Hauptbuchung fehlt");
        }
        steuer_haupt = b.getSteuer();
      }
      if (b.getSplitTyp() == SplitbuchungTyp.GEGEN)
      {
        ba_gegen = b.getBuchungsart();
        if (ba_gegen == null)
        {
          throw new RemoteException("Buchungsart bei der Gegenbuchung fehlt");
        }
        steuer_gegen = b.getSteuer();
      }
    }
    if (ba_haupt.getNummer() != ba_gegen.getNummer())
    {
      throw new RemoteException(
          "Buchungsarten bei Haupt- und Gegenbuchung müssen identisch sein");
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG)
        && ((steuer_haupt == null && steuer_gegen != null)
            || (steuer_haupt != null && !steuer_haupt.equals(steuer_gegen))))
    {
      throw new RemoteException(
          "Steuer bei Haupt- und Gegenbuchung müssen identisch sein");
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
        gegen = createSplitbuchung(master);
        gegen.setSplitTyp(SplitbuchungTyp.GEGEN);
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

  public static String getText()
  {
    return text;
  }

  /**
   * Erstellt eine Kopie der Buchung als Splitbuchung. Der splittyp muss noch
   * zugeordnet werden
   * 
   * @param b
   *          die zu kopierende Buchung
   * @return die neue erstellt (Split-) Buchung
   * @throws RemoteException
   */
  private static Buchung createSplitbuchung(Buchung b) throws RemoteException
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
    buch.setSteuer(b.getSteuer());

    // Folgende Spalten werden nicht übertragen (Stand 4.0.0):
    // betrag
    // sollbuchung
    // abrechnungslauf
    // splitid
    // splittyp
    // spendenbescheinigung
    // verzicht
    // abschluss
    // geprueft
    // unterlagenwertermittlung
    // herkunftspende
    // bezeichnungsachzuwendung

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
    if (kommentareVonHauptbuchung.get(origin))
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
    if (zweckeVonHauptbuchung.get(origin))
    {
      buch.setZweck(master.getZweck());
    }
    else
    {
      buch.setZweck(origin.getZweck());
    }
    buch.setSteuer(origin.getSteuer());
    buch.setIban(master.getIban());
    buch.setSplitTyp(SplitbuchungTyp.SPLIT);
    return buch;
  }

  /**
   * Splittet eine Buchung anhand der in der Sollbuchung enthaltenen
   * Sollbuchungspositionen.
   * 
   * @param buchung
   *          die zu splittende Buchung
   * @param sollb
   *          die Sollbuchung, die der Buchung zugewiesen werden soll
   * @param fallback
   *          wenn true wird bei Fehler ohne splitten der Sollbuchung
   *          zugeordnet.
   * @return Wenn die Beträge von Sollbuchung und Buchung verschieden sind, wird
   *         die erzeugte Restbuchung zurückgegeben, sonst <code>null</code>.
   */
  public static Buchung autoSplit(Buchung buchung, Sollbuchung sollb,
      boolean fallback) throws RemoteException, ApplicationException
  {
    Buchung restBuchung = null;
    if (sollb == null)
    {
      buchung.setSollbuchung(null);
      buchung.store();
      return null;
    }
    HashMap<String, Double> splitMap = new HashMap<>();
    HashMap<String, String> splitZweckMap = new HashMap<>();
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    ArrayList<SollbuchungPosition> spArray = sollb.getSollbuchungPositionList();
    if (spArray.size() == 0)
    {
      throw new ApplicationException("Sollbuchung enthält keine Positionen");
    }

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
        // Key in der Form BuchungsartId-BuchungsklasseId#SteuerId (Steuer nur
        // wenn steuerInBuchung gesetzt ist)
        String key = sp.getBuchungsartId() + "-"
            + (klasseInBuchung && sp.getBuchungsklasseId() != null
                ? sp.getBuchungsklasseId()
                : "")
            + "#";
        if (steuerInBuchung)
        {
          key += (sp.getSteuer() != null ? sp.getSteuer().getID() : "");
        }

        Double betrag = splitMap.getOrDefault(key, 0d);
        if (Math.abs(sp.getBetrag()) < 0.01d)
        {
          continue;
        }

        splitMap.put(key, betrag + sp.getBetrag().doubleValue());
        String zweck = splitZweckMap.get(key);
        if (zweck == null)
        {
          zweck = sp.getZweck();
        }
        else
        {
          zweck = zweck + ", " + sp.getZweck();
        }
        splitZweckMap.put(key, zweck);
      }
      // ggf. bereits zugeordnete Buchungen holen und diese vom Soll abziehen
      for (Buchung istBuchung : sollb.getBuchungList())
      {
        // Key in der Form BuchungsartId-BuchungsklasseId#SteuerId (Steuer nur
        // wenn steuerInBuchung gesetzt ist)
        String key = istBuchung.getBuchungsartId() + "-"
            + (klasseInBuchung && istBuchung.getBuchungsklasseId() != null
                ? istBuchung.getBuchungsklasseId()
                : "")
            + "#";
        if (steuerInBuchung)
        {
          key += (istBuchung.getSteuer() != null
              ? istBuchung.getSteuer().getID()
              : "");
        }
        Double sollBetrag = splitMap.get(key);
        if (sollBetrag == null)
        {
          // Diese Buchungsart/Steuer kombination existiert in der Sollbuchung
          // nicht, das ignorieren wir.
        }
        else if ((sollBetrag > 0 && sollBetrag < istBuchung.getBetrag())
            || (sollBetrag < 0 && sollBetrag > istBuchung.getBetrag()))
        {
          // Der Sollbuchung ist eine Istbuchung zugeordnet, die größer als das
          // Soll diser Position ist, wir entfernen sie. Es bleibt eine
          // Überzahlung erhalten.
          splitMap.remove(key);
        }
        else
        {
          // Restbetrag in die Map schreiben
          splitMap.put(key, sollBetrag - istBuchung.getBetrag());
        }
      }

      // Bei nur einem Eintrag ist kein Splitten nötig, wir können also die
      // Daten direkt speichern
      if (splitMap.size() == 1)
      {
        if (spArray.get(0).getBuchungsartId() != null)
        {
          buchung.setBuchungsartId(spArray.get(0).getBuchungsartId());
        }
        if (klasseInBuchung && spArray.get(0).getBuchungsklasseId() != null)
        {
          buchung.setBuchungsklasseId(spArray.get(0).getBuchungsklasseId());
        }
        // Wenn die Buchungsart gesetzt ist, auch die Steuer aus der Position
        // nehmen, sonst kann es zu Fehlern beim Speichern kommen (zB. Steuer
        // bei Spenden)
        if (steuerInBuchung && spArray.get(0).getBuchungsartId() != null)
        {
          buchung.setSteuer(spArray.get(0).getSteuer());
        }
        buchung.setSollbuchung(sollb);
        buchung.store();
        return null;
      }

      // Das Splittbuchungen immmer eine Buchungsart haben müssen, ordnen wir
      // diejenige der ersten Position zu, wenn keine Buchungsart in der
      // Buchung vorhanden ist.
      if (buchung.getBuchungsartId() == null
          && spArray.get(0).getBuchungsartId() != null)
      {
        buchung.setBuchungsartId(spArray.get(0).getBuchungsartId());
      }
      if (klasseInBuchung && buchung.getBuchungsklasseId() == null
          && spArray.get(0).getBuchungsklasseId() != null)
      {
        buchung.setBuchungsklasseId(spArray.get(0).getBuchungsklasseId());
      }

      if (buchung.getSplitTyp() == null)
      {
        buchung.setSplitTyp(SplitbuchungTyp.HAUPT);
        buchung.setSollbuchungID(null);
        SplitbuchungsContainer.init(buchung);
      }
      else if (buchung.getSplitTyp() == SplitbuchungTyp.GEGEN
          || buchung.getSplitTyp() == SplitbuchungTyp.HAUPT)
      {
        throw new ApplicationException(
            "Splitten von Haupt und Gegenbuchung nicht möglich.");
      }
      else
      {
        // Spitbuchungen müssen durch die neuen Buchungen ersetzt werden
        SplitbuchungsContainer.init(buchung);
        for (Buchung b : splitbuchungen)
        {
          if (b.getID() != null && b.getID().equals(buchung.getID()))
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

      boolean splitPositionZweck = (Boolean) Einstellungen
          .getEinstellung(Property.SPLITPOSITIONZWECK);
      double zugeordnet = 0d;
      // Wir nehmen die kleinsten Beträge zuerst, so werden ggf. Guthaben als
      // erstes ausgeglichen.
      Iterator<Entry<String, Double>> iterator = splitMap.entrySet().stream()
          .sorted(Map.Entry.comparingByValue()).iterator();
      while (iterator.hasNext())
      {
        Entry<String, Double> entry = iterator.next();

        // Wenn der Restbetrag kleiner als der Fehlbetrag ist, nur den Rest
        // zuordnen.
        double betragZuordnen = entry.getValue();
        if ((buchung.getBetrag() > 0
            && betragZuordnen > buchung.getBetrag() - zugeordnet)
            || (buchung.getBetrag() < 0
                && betragZuordnen < buchung.getBetrag() - zugeordnet))
        {
          betragZuordnen = buchung.getBetrag() - zugeordnet;
        }
        if (Math.abs(betragZuordnen) < 0.1d)
        {
          continue;
        }
        zugeordnet += betragZuordnen;

        Buchung splitBuchung = createSplitbuchung(buchung);
        splitBuchung.setBetrag(betragZuordnen);
        if (splitPositionZweck)
        {
          splitBuchung.setZweck(splitZweckMap.get(entry.getKey()));
        }
        splitBuchung.setSollbuchung(sollb);
        String buchungsart = entry.getKey().substring(0,
            entry.getKey().indexOf("-"));
        splitBuchung.setBuchungsartId(Long.parseLong(buchungsart));
        String tmpKey = entry.getKey()
            .substring(entry.getKey().indexOf("-") + 1);
        String buchungsklasse = tmpKey.substring(0, tmpKey.indexOf("#"));
        String steuer = tmpKey.substring(tmpKey.indexOf("#") + 1);

        if (buchungsklasse.length() > 0)
        {
          splitBuchung.setBuchungsklasseId(Long.parseLong(buchungsklasse));
        }
        if (steuer.length() > 0)
        {
          splitBuchung.setSteuerId(Long.parseLong(steuer));
        }
        splitBuchung.setSplitTyp(SplitbuchungTyp.SPLIT);
        splitBuchung.setSplitId(Long.parseLong(getMaster().getID()));

        SplitbuchungsContainer.add(splitBuchung);
      }
      if (Math.abs(buchung.getBetrag() - zugeordnet) >= 0.01d)
      {
        restBuchung = createSplitbuchung(buchung);
        restBuchung.setBetrag(buchung.getBetrag() - zugeordnet);
        restBuchung.setSplitTyp(SplitbuchungTyp.SPLIT);
        restBuchung.setSplitId(Long.parseLong(getMaster().getID()));

        SplitbuchungsContainer.add(restBuchung);
      }
      // Wenn es nur eine Splitposition gibt, die Splitbuchung auflösen.
      // (Haupt- und Gegen-Buchung gibt es immer, daher müssen es 3 sein)
      if (splitbuchungen.size() == 3)
      {
        Buchung split = null;
        for (Buchung b : splitbuchungen)
        {
          if (b.getSplitTyp() == SplitbuchungTyp.SPLIT)
          {
            split = b;
            break;
          }
        }
        Buchung haupt = getMaster();
        haupt.setSollbuchung(sollb);
        haupt.setBuchungsartId(split.getBuchungsartId());
        haupt.setBuchungsklasseId(split.getBuchungsklasseId());
        haupt.setSteuer(split.getSteuer());

        SplitbuchungsContainer.aufloesen();
      }
      else
      {
        SplitbuchungsContainer.handleStore();
      }
    }
    catch (ApplicationException e)
    {
      if (splitbuchungen != null)
      {
        splitbuchungen.clear();
      }
      if (!fallback)
      {
        throw e;
      }

      // Wenn kein automatisches Spliten möglich ist nur Buchungsart,
      // Buchungsklasse, Steuer und Sollbuchung zuweisen
      if (spArray.get(0).getBuchungsartId() != null)
      {
        buchung.setBuchungsartId(spArray.get(0).getBuchungsartId());
      }
      if (klasseInBuchung && spArray.get(0).getBuchungsklasseId() != null)
      {
        buchung.setBuchungsklasseId(spArray.get(0).getBuchungsklasseId());
      }
      // Wenn die Buchungsart gesetzt ist, auch die Steuer aus der Position
      // nehmen, sonst kann es zu Fehlern beim Speichern kommen (zB. Steuer bei
      // Spenden)
      if (steuerInBuchung && spArray.get(0).getBuchungsartId() != null)
      {
        buchung.setSteuer(spArray.get(0).getSteuer());
      }
      buchung.setSollbuchung(sollb);
      buchung.store();

      Logger.warn(
          "Kein Autosplit möglich, ordne Buchung Sollbuchung ohne splitten zu. "
              + e.getMessage());
    }
    catch (RemoteException e)
    {
      if (splitbuchungen != null)
      {
        splitbuchungen.clear();
      }
      throw e;
    }
    return restBuchung;
  }

  public static int getAnzahl()
  {
    return anzahl;
  }
}
