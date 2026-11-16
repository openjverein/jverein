package de.jost_net.JVerein.util;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.server.SplitBuchung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SplitbuchungUtil
{
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
    HashMap<String, BigDecimal> splitMap = new HashMap<>();
    HashMap<String, String> splitZweckMap = new HashMap<>();
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    List<SollbuchungPosition> spArray = sollb.getSollbuchungPositionList();
    if (spArray.size() == 0)
    {
      throw new ApplicationException("Sollbuchung enthält keine Positionen");
    }
    SplitBuchung splitBuchungSplit;

    try
    {
      positionenAbgleichen(sollb, splitMap, splitZweckMap, true);

      // Bei nur einem oder keinem Eintrag (kann bei Überzahlung passieren)
      // und gleichem Betrag ist kein Splitten nötig, wir können also die
      // Daten direkt speichern
      if (splitMap.size() <= 1
          && sollb.getBetrag().subtract(sollb.getIstSumme())
              .subtract(buchung.getBetrag()).signum() == 0)
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
        buchung.setSollbuchungID(null);
        splitBuchungSplit = new SplitBuchung(buchung);
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
        splitBuchungSplit = new SplitBuchung(buchung);
        for (Buchung b : splitBuchungSplit.getBuchungen())
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
      BigDecimal zugeordnet = BigDecimal.ZERO;
      // Wir nehmen die kleinsten Beträge zuerst, so werden ggf. Guthaben als
      // erstes ausgeglichen. Bei negativem Betrag der Sollbuchung in
      // umgekehrter Reihenfolge
      Iterator<Entry<String, BigDecimal>> iterator;
      if (buchung.getBetrag().signum() > 0)
      {
        iterator = splitMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue()).iterator();
      }
      else
      {
        iterator = splitMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .iterator();
      }

      while (iterator.hasNext())
      {
        // Wenn der Restbetrag kleiner als der Fehlbetrag ist, nur den Rest
        // zuordnen.
        Entry<String, BigDecimal> entry = iterator.next();
        BigDecimal betragZuordnen = entry.getValue();
        if ((buchung.getBetrag().signum() > 0 && betragZuordnen
            .compareTo((buchung.getBetrag().subtract(zugeordnet))) > 0)
            || (buchung.getBetrag().signum() < 0 && betragZuordnen
                .compareTo(buchung.getBetrag().subtract(zugeordnet)) < 0))
        {
          betragZuordnen = buchung.getBetrag().subtract(zugeordnet);
        }
        if (betragZuordnen.signum() == 0)
        {
          continue;
        }
        zugeordnet = zugeordnet.add(betragZuordnen);

        Buchung splitBuchung = splitBuchungSplit.createSplitBuchung();
        splitBuchungSplit.addBuchung(splitBuchung);

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
      }
      if (zugeordnet.signum() != 0)
      {
        if (buchung.getBetrag().compareTo(zugeordnet) != 0)
        {
          restBuchung = splitBuchungSplit.createSplitBuchung();
          splitBuchungSplit.addBuchung(restBuchung);
          restBuchung.setBetrag(buchung.getBetrag().subtract(zugeordnet));
        }
        // Wenn es nur eine Splitposition gibt, die Splitbuchung auflösen.
        // (Haupt- und Gegen-Buchung gibt es immer, daher müssen es 3 sein)
        if (splitBuchungSplit.getBuchungen().size() == 3)
        {
          Buchung split = null;
          for (Buchung b : splitBuchungSplit.getBuchungen())
          {
            if (b.getSplitTyp() == SplitbuchungTyp.SPLIT)
            {
              split = b;
              break;
            }
          }
          Buchung haupt = splitBuchungSplit.getMaster();
          haupt.setSollbuchung(sollb);
          haupt.setBuchungsartId(split.getBuchungsartId());
          haupt.setBuchungsklasseId(split.getBuchungsklasseId());
          haupt.setSteuer(split.getSteuer());

          splitBuchungSplit.aufloesen();
        }
        else
        {
          splitBuchungSplit.store();
        }
      }
      else
      {
        splitBuchungSplit.aufloesen();
        restBuchung = buchung;
      }
    }
    catch (ApplicationException e)
    {
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
    return restBuchung;
  }

  /**
   * Gleicht die Sollbuchungspositionen der Sollbuchung mit den bereits
   * zugewiesenen Buchungen ab. Die bereits zugewiesenen Beträge werden vom Soll
   * abgezogen.
   * 
   * @param sollb
   *          Sollbuchung deren Sollbuchungspositionen abgeglichen werden
   *          sollen.
   * @param splitMap
   *          Map welche die Beträge der Sollbuchungspositionen enthält. Sie
   *          wird von der Methode gefüllt.
   * @param splitZweckMap
   *          Map welche die Zwecke der Sollbuchungspositionen enthält. Sie wird
   *          von der Methode gefüllt.
   * @param ignore
   *          Ignoriere Überzahlung und Buchungen ohne Match bei den Posten.
   */
  public static void positionenAbgleichen(Sollbuchung sollb,
      HashMap<String, BigDecimal> splitMap,
      HashMap<String, String> splitZweckMap, boolean ignore)
      throws RemoteException, ApplicationException
  {
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    // Positionen mit gleicher Buchungsart, buchungsklasse und Steuer zusammen
    // fassen
    positionenZusammenfassen(sollb, splitMap, splitZweckMap);

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
        key += (istBuchung.getSteuer() != null ? istBuchung.getSteuer().getID()
            : "");
      }
      BigDecimal sollBetrag = splitMap.get(key);
      if (ignore)
      {
        if (sollBetrag == null)
        {
          // Diese Buchungsart/Steuer kombination existiert in der Sollbuchung
          // nicht, das ignorieren wir.
        }
        else if ((sollBetrag.signum() > 0
            && sollBetrag.compareTo(istBuchung.getBetrag()) < 0)
            || (sollBetrag.signum() < 0
                && sollBetrag.compareTo(istBuchung.getBetrag()) > 0))
        {
          // Der Sollbuchung ist eine Istbuchung zugeordnet, die größer als das
          // Soll dieser Position ist, wir entfernen sie. Es bleibt eine
          // Überzahlung erhalten.
          splitMap.remove(key);
        }
        else
        {
          // Restbetrag in die Map schreiben
          splitMap.put(key, sollBetrag.subtract(istBuchung.getBetrag()));
        }
      }
      else
      {
        if (sollBetrag == null)
        {
          sollBetrag = BigDecimal.ZERO;
          splitZweckMap.put(key, istBuchung.getZweck());
        }
        splitMap.put(key, sollBetrag.subtract(istBuchung.getBetrag()));
      }
    }
  }

  /**
   * Die Methode sucht Sollbuchungspositionen und fasst solche mit gleicher
   * Buchungsart, Buchungsklasse und Steuer zu einer Position zusammen.
   * Positionen ohne Betrag werden aussortiert!
   * 
   * @param sollb
   *          Sollbuchung deren Sollbuchungspositionen zusammengefasst werden
   *          sollen.
   * @param splitMap
   *          Map welche die Sollbuchungspositionen enthält. Sie wird von der
   *          Methode gefüllt.
   * @param splitZweckMap
   *          Map welche die Zwecke der Sollbuchungspositionen enthält. Sie wird
   *          von der Methode gefüllt.
   */
  public static void positionenZusammenfassen(Sollbuchung sollb,
      HashMap<String, BigDecimal> splitMap,
      HashMap<String, String> splitZweckMap)
      throws RemoteException, ApplicationException
  {
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    List<SollbuchungPosition> spArray = sollb.getSollbuchungPositionList();

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

      BigDecimal betrag = splitMap.getOrDefault(key, BigDecimal.ZERO);
      if (sp.getBetrag().signum() == 0)
      {
        continue;
      }

      splitMap.put(key, betrag.add(sp.getBetrag()));
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
  }
}
