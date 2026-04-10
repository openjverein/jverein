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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.SaldoDetailAction;
import de.jost_net.JVerein.gui.formatter.SaldoFormatter;
import de.jost_net.JVerein.gui.menu.SaldoMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.SaldoListTablePart;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.BuchungsklassesaldoCSV;
import de.jost_net.JVerein.io.BuchungsklassesaldoPDF;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.BuchungsartAnzeige;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.util.ApplicationException;

public class BuchungsklasseSaldoControl extends AbstractSaldoControl
{
  /**
   * Die Art der Buchung: Einnahme (0), Ausgabe (1), Umbuchung (2)
   */
  public static final String ARTBUCHUNGSART = "artBuchungsArt";

  /**
   * Die Summe, bei optiernenden Vereinen die Nettosumme + Steuern auf der
   * Steuerbuchungsart
   */
  protected static final String SUMME = "summe";

  /**
   * true wenn Steuern verwendet werden sollen. Default wie in Einstellungen
   * "Optiert" gesetzt.
   */
  protected boolean mitSteuer;

  /**
   * Soll "Buchungen ohne Buchungsart" mit ausgegeben werden? Default true
   */
  protected boolean mitOhneBuchungsart = true;

  /**
   * Die Bezeichnung der Gruppen-Spalte: "Buchungsklasse", "Projekt". Default
   * "Buchungsklasse"
   */
  protected String gruppenBezeichnung = "Buchnugsklasse";

  /**
   * Spalte die für die Gruppe verwendet wird. Default Buchungsklasse
   */
  protected String spalteGruppe = BUCHUNGSKLASSE_TEXT;

  /**
   * Soll eine Buchungsklasse-Spalte angezeigt werden? Default false.
   */
  protected boolean mitBuchungsklasseSpalte = false;

  private SaldoListTablePart saldoList;

  /**
   * Sollen Umbuchungen mit augegeben werden? Default true.
   */
  protected boolean mitUmbuchung = true;

  public BuchungsklasseSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    mitSteuer = (Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT);
  }

  @Override
  public JVereinTablePart getSaldoList() throws ApplicationException
  {
    try
    {
      if (saldoList != null)
      {
        return saldoList;
      }
      saldoList = new SaldoListTablePart(getList(), new SaldoDetailAction())
      {
        // Sortieren verhindern
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn(gruppenBezeichnung, GRUPPE, null, false);
      if (mitBuchungsklasseSpalte)
      {
        saldoList.addColumn("Buchungsklasse", BUCHUNGSKLASSE_TEXT);
      }
      saldoList.addColumn("Buchungsart", BUCHUNGSART_TEXT);
      saldoList.addColumn("Einnahmen", EINNAHMEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Ausgaben", AUSGABEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      if (mitUmbuchung)
      {
        saldoList.addColumn("Umbuchungen", UMBUCHUNGEN,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
      }
      saldoList.addColumn("Anzahl", ANZAHL);
      saldoList.setMulti(true);
      saldoList.setRememberColWidths(true);
      saldoList.setRememberOrder(true);
      saldoList.setRememberState(true);
      saldoList.setContextMenu(new SaldoMenu(this));
      saldoList.setFormatter(new SaldoFormatter());

      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    List<PseudoDBObject> list = PseudoIterator.asList(getIterator());

    if (mitSteuer)
    {
      List<PseudoDBObject> listNeu = new ArrayList<>();

      List<PseudoDBObject> steuerList = PseudoIterator
          .asList(getSteuerIterator());

      // Steuerbeträge hinzurechnen
      for (PseudoDBObject o : list)
      {
        Integer klasse = o.getInteger(BUCHUNGSKLASSE_ID);
        Integer art = o.getInteger(BUCHUNGSART_ID);
        Integer projekt = o.getInteger(PROJEKT_ID);

        for (PseudoDBObject steuerObject : steuerList)
        {
          Integer klasseSteuer = steuerObject.getInteger(BUCHUNGSKLASSE_ID);
          Integer artSteuer = steuerObject.getInteger(BUCHUNGSART_ID);
          Integer projektSteuer = steuerObject.getInteger(PROJEKT_ID);

          if (Objects.equals(klasse, klasseSteuer)
              && Objects.equals(art, artSteuer)
              && Objects.equals(projekt, projektSteuer))
          {
            o.setAttribute(SUMME,
                o.getDouble(SUMME) + steuerObject.getDouble(SUMME));

            if (o.getAttribute(EINNAHMEN) != null
                && steuerObject.getAttribute(EINNAHMEN) != null)
            {
              o.setAttribute(EINNAHMEN,
                  o.getDouble(EINNAHMEN) + steuerObject.getDouble(EINNAHMEN));
            }

            if (o.getAttribute(AUSGABEN) != null
                && steuerObject.getAttribute(AUSGABEN) != null)
            {
              o.setAttribute(AUSGABEN,
                  o.getDouble(AUSGABEN) + steuerObject.getDouble(AUSGABEN));
            }
            steuerObject.setAttribute("gefunden", true);
            break;
          }
        }
        listNeu.add(o);
      }
      // Alle Steuerobjecte hinzufügen, für die kein Eintrag gefunden wurde
      for (PseudoDBObject steuerObject : steuerList)
      {
        if (steuerObject.getAttribute("gefunden") == null)
        {
          listNeu.add(steuerObject);
        }
      }
      list = listNeu;
    }
    sortList(list);

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    String klasseAlt = null;

    // Summen der Buchungsklasse/Projekt
    Double einnahmenSumme = 0d;
    Double ausgabenSumme = 0d;
    Double umbuchungenSumme = 0d;

    // Summen aller Buchungsklassen/Projekte
    Double einnahmenGesamt = 0d;
    Double ausgabenGesamt = 0d;
    Double umbuchungenGesamt = 0d;

    for (PseudoDBObject o : list)
    {
      String buchungsart = (String) o.getAttribute(BUCHUNGSART);
      String buchungsartNummer = (String) o.getAttribute(BUCHUNGSART_NUMMER);

      String buchungsklasse = (String) o.getAttribute(BUCHUNGSKLASSE);
      String buchungsklasseNummer = (String) o
          .getAttribute(BUCHUNGSKLASSE_NUMMER);
      switch ((Integer) Einstellungen
          .getEinstellung(Property.BUCHUNGSARTANZEIGE))
      {
        case BuchungsartAnzeige.NUMMER_BEZEICHNUNG:
          o.setAttribute(BUCHUNGSART_TEXT,
              (buchungsartNummer != null ? buchungsartNummer : "") + " - "
                  + (buchungsart != null ? buchungsart : ""));
          o.setAttribute(BUCHUNGSKLASSE_TEXT,
              (buchungsklasseNummer != null ? buchungsklasseNummer : "") + " - "
                  + (buchungsklasse != null ? buchungsklasse : ""));
          break;
        case BuchungsartAnzeige.BEZEICHNUNG_NUMMER:
          o.setAttribute(BUCHUNGSART_TEXT,
              (buchungsart != null ? buchungsart : "") + " ("
                  + (buchungsartNummer != null ? buchungsartNummer : "") + ")");
          o.setAttribute(BUCHUNGSKLASSE_TEXT,
              (buchungsklasse != null ? buchungsklasse : "") + " ("
                  + (buchungsklasseNummer != null ? buchungsklasseNummer : "")
                  + ")");
          break;
        default:
          o.setAttribute(BUCHUNGSART_TEXT, buchungsart);
          o.setAttribute(BUCHUNGSKLASSE_TEXT, buchungsklasse);
          break;
      }

      String klasse = (String) o.getAttribute(spalteGruppe);
      if (klasse == null || klasse.equals(" - ") || klasse.equals(" ()"))
      {
        klasse = "Nicht zugeordnet";
      }
      // Die Art der Buchungsart: Einnahme, Ausgabe, Umbuchung
      Integer art = o.getInteger(ARTBUCHUNGSART);
      Double summe = o.getDouble(SUMME);

      // Wenn es "einnahmen" oder "ausgaben" spalten gibt, nehmen wir die Werte
      // direkt.
      Double einnahmen = o.getDouble(EINNAHMEN);
      Double ausgaben = o.getDouble(AUSGABEN);

      // Vor neuer Klasse Saldo der letzten anzeigen.
      if (!klasse.equals(klasseAlt) && klasseAlt != null)
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
        if (Math.abs(einnahmenSumme) >= 0.01d)
        {
          saldo.setAttribute(EINNAHMEN, einnahmenSumme);
        }
        if (Math.abs(ausgabenSumme) >= 0.01d)
        {
          saldo.setAttribute(AUSGABEN, ausgabenSumme);
        }
        if (Math.abs(umbuchungenSumme) >= 0.01d)
        {
          saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
        }
        zeilen.add(saldo);

        PseudoDBObject saldogv = new PseudoDBObject();
        saldogv.setAttribute(ART, ART_SALDOGEWINNVERLUST);
        saldogv.setAttribute(GRUPPE, "Gewinn/Verlust " + klasseAlt);
        saldogv.setAttribute(EINNAHMEN,
            einnahmenSumme + ausgabenSumme + umbuchungenSumme);
        zeilen.add(saldogv);

        einnahmenSumme = 0d;
        ausgabenSumme = 0d;
        umbuchungenSumme = 0d;
      }

      Double umbuchungen = 0d;
      switch (art)
      {
        case ArtBuchungsart.EINNAHME:
          if (einnahmen == null)
          {
            einnahmen = summe;
            o.setAttribute(EINNAHMEN, einnahmen);
          }
          einnahmenSumme += einnahmen;
          einnahmenGesamt += einnahmen;
          break;
        case ArtBuchungsart.AUSGABE:
          if (ausgaben == null)
          {
            ausgaben = summe;
            o.setAttribute(AUSGABEN, ausgaben);
          }
          ausgabenSumme += ausgaben;
          ausgabenGesamt += ausgaben;
          break;
        case ArtBuchungsart.UMBUCHUNG:
          if (einnahmen == null && ausgaben == null)
          {
            umbuchungen = summe;
            umbuchungenSumme += umbuchungen;
            umbuchungenGesamt += umbuchungen;
            o.setAttribute(UMBUCHUNGEN, umbuchungen);
          }
          else
          {
            einnahmenSumme += einnahmen;
            einnahmenGesamt += einnahmen;
            ausgabenSumme += ausgaben;
            ausgabenGesamt += ausgaben;
          }
          break;
      }

      // Bei neuer Klasse Kopfzeile anzeigen.
      if (!klasse.equals(klasseAlt))
      {
        PseudoDBObject head = new PseudoDBObject();
        head.setAttribute(ART, ART_HEADER);
        head.setAttribute(GRUPPE, klasse);
        head.setAttribute(BUCHUNGSKLASSE_ID, o.getAttribute(BUCHUNGSKLASSE_ID));
        // Für Projektsaldo
        head.setAttribute(PROJEKT_ID, o.getAttribute(PROJEKT_ID));
        zeilen.add(head);
        klasseAlt = klasse;
      }
      // Die Detailzeile wie sie aus dem iterator kommt azeigen.
      o.setAttribute(ART, ART_DETAIL);

      // Anzahl null blenden wir aus
      if (o.getInteger(ANZAHL) != null && o.getInteger(ANZAHL).equals(0))
      {
        o.setAttribute(ANZAHL, null);
      }
      zeilen.add(o);
    }

    // Am Ende noch Saldo der letzten Klasse.
    // (Nur wenn auch Buchungsklassen existieren)
    if (klasseAlt != null)
    {
      PseudoDBObject saldo = new PseudoDBObject();
      saldo.setAttribute(ART, ART_SALDOFOOTER);
      saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
      if (Math.abs(einnahmenSumme) >= 0.01d)
      {
        saldo.setAttribute(EINNAHMEN, einnahmenSumme);
      }
      if (Math.abs(ausgabenSumme) >= 0.01d)
      {
        saldo.setAttribute(AUSGABEN, ausgabenSumme);
      }
      if (Math.abs(umbuchungenSumme) >= 0.01d)
      {
        saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
      }
      zeilen.add(saldo);

      PseudoDBObject saldogv = new PseudoDBObject();
      saldogv.setAttribute(ART, ART_SALDOGEWINNVERLUST);
      saldogv.setAttribute(GRUPPE, "Gewinn/Verlust " + klasseAlt);
      saldogv.setAttribute(EINNAHMEN,
          einnahmenSumme + ausgabenSumme + umbuchungenSumme);
      zeilen.add(saldogv);
    }

    PseudoDBObject o = new PseudoDBObject();
    o.setAttribute(ART, ART_LEERZEILE);
    zeilen.add(o);

    PseudoDBObject saldo = new PseudoDBObject();
    saldo.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    saldo.setAttribute(GRUPPE, "Gesamt Saldo");
    saldo.setAttribute(EINNAHMEN, einnahmenGesamt);
    saldo.setAttribute(AUSGABEN, ausgabenGesamt);
    saldo.setAttribute(UMBUCHUNGEN, umbuchungenGesamt);
    zeilen.add(saldo);

    Double summeOhneBuchungsart = 0d;
    if (mitOhneBuchungsart)
    {
      // Ggf. die Anzahl und Summe nicht zugeordneter Buchungen anzeigen.
      // (Geht nicht mit im oberen Query, da MySQL und H2 kein FULL JOIN
      // unterstützen)
      ExtendedDBIterator<PseudoDBObject> ohneBaIt = new ExtendedDBIterator<>(
          "buchung");
      ohneBaIt.addColumn("count(*) AS anzahl");
      ohneBaIt.addColumn("sum(buchung.betrag) AS summe");
      ohneBaIt.addFilter("buchungsart IS NULL");
      ohneBaIt.addFilter("datum >= ?", getDatumvon().getDate());
      ohneBaIt.addFilter("datum <= ?", getDatumbis().getDate());

      PseudoDBObject oAnz = ohneBaIt.next();
      Integer anzahl = oAnz.getAttribute("anzahl") == null ? 0
          : oAnz.getInteger("anzahl");
      summeOhneBuchungsart = oAnz.getAttribute("summe") == null ? 0
          : oAnz.getDouble("summe");
      if (anzahl > 0)
      {
        PseudoDBObject ohneBuchungsart = new PseudoDBObject();
        ohneBuchungsart.setAttribute(ART,
            AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN);
        ohneBuchungsart.setAttribute(GRUPPE,
            "Saldo Buchungen ohne Buchungsart");
        ohneBuchungsart.setAttribute(EINNAHMEN, summeOhneBuchungsart);
        ohneBuchungsart.setAttribute(ANZAHL, anzahl);
        // Für Menü brauchen wir eine id
        ohneBuchungsart.setAttribute(BUCHUNGSART_ID, -1);
        zeilen.add(ohneBuchungsart);
      }
    }

    PseudoDBObject saldogv = new PseudoDBObject();
    saldogv.setAttribute(ART, ART_GESAMTGEWINNVERLUST);
    saldogv.setAttribute(GRUPPE, "Gesamt Gewinn/Verlust");
    saldogv.setAttribute(EINNAHMEN, einnahmenGesamt + ausgabenGesamt
        + umbuchungenGesamt + summeOhneBuchungsart);
    zeilen.add(saldogv);

    return zeilen;
  }

  /**
   * Sortiert die Liste je nach Einstellung nach Buchungsklasse/Buchungsart
   * Nummer oder Bezeichnung
   * 
   * @param list
   * @throws RemoteException
   */
  protected void sortList(List<PseudoDBObject> list) throws RemoteException
  {
    int order = (Integer) Einstellungen
        .getEinstellung(Property.BUCHUNGSARTSORT);
    list.sort((o1, o2) -> {
      try
      {
        String keyKlasse;
        String keyArt;
        switch (order)
        {
          case BuchungsartSort.NACH_NUMMER:
            keyKlasse = BUCHUNGSKLASSE_NUMMER;
            keyArt = BUCHUNGSART_NUMMER;
            break;
          case BuchungsartSort.NACH_BEZEICHNUNG:
          default:
            keyKlasse = BUCHUNGSKLASSE;
            keyArt = BUCHUNGSART;
            break;
        }

        if (o1.getAttribute(keyKlasse) == null)
        {
          return 1;
        }
        if (o2.getAttribute(keyKlasse) == null)
        {
          return -1;
        }

        int comp = ((String) o1.getAttribute(keyKlasse))
            .compareTo((String) o2.getAttribute(keyKlasse));
        if (comp != 0)
        {
          return comp;
        }
        if (o1.getAttribute(keyArt) == null)
        {
          return 1;
        }
        if (o2.getAttribute(keyArt) == null)
        {
          return -1;
        }
        comp = ((String) o1.getAttribute(keyArt))
            .compareTo((String) o2.getAttribute(keyArt));
        return comp;
      }
      catch (RemoteException e)
      {
        return 0;
      }
    });
  }

  /**
   * Holt den Iterator, auf dessen Basis die Salodliste erstellt wird.
   * 
   * @return der Iterator
   * @throws RemoteException
   */
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    final boolean unterdrueckung = (Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG);

    final boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    final boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "buchungsart");

    it.addColumn("buchungsart.art as " + ARTBUCHUNGSART);
    it.addColumn("buchungsart.id as " + BUCHUNGSART_ID);
    it.addColumn("buchungsart.nummer as " + BUCHUNGSART_NUMMER);
    it.addColumn("buchungsart.bezeichnung as " + BUCHUNGSART);
    it.addColumn("buchungsart.status");

    it.addColumn("buchungsklasse.id as " + BUCHUNGSKLASSE_ID);
    it.addColumn("buchungsklasse.nummer as " + BUCHUNGSKLASSE_NUMMER);
    it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);
    it.addColumn("COUNT(buchung.id) as " + ANZAHL);

    if (mitSteuer)
    {
      // Nettobetrag berechnen
      it.addColumn("COALESCE(SUM(CAST(buchung.betrag * 100 / (100 + "
          // Anlagenkonto immer Bruttobeträge.
          // Alte Steuerbuchungen mit dependencyid lassen wir bestehen ohne
          // Netto zu berehnen.
          + "CASE WHEN konto.kontoart = ? OR buchung.dependencyid > -1 THEN 0 ELSE COALESCE(steuer.satz,0) END"
          + ") AS DECIMAL(10,2))),0) AS " + SUMME, Kontoart.ANLAGE.getKey());
    }
    else
    {
      it.addColumn("COALESCE(SUM(buchung.betrag),0) AS " + SUMME);
    }

    it.leftJoin("buchung",
        "buchung.buchungsart = buchungsart.id AND datum >= ? AND datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.join("konto", "buchung.konto = konto.id");

    if (klasseInBuchung)
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchung.buchungsklasse");
    }
    else
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchungsart.buchungsklasse ");
    }

    it.addFilter("konto.kontoart is null OR konto.kontoart < ?",
        Kontoart.LIMIT.getKey());

    if (mitSteuer && steuerInBuchung)
    {
      it.leftJoin("steuer", "steuer.id = buchung.steuer");
    }
    else if (mitSteuer)
    {
      it.leftJoin("steuer", "steuer.id = buchungsart.steuer");
    }

    it.addGroupBy("buchungsklasse.bezeichnung");
    it.addGroupBy("buchungsklasse.nummer");
    it.addGroupBy("buchungsart.id");
    it.addGroupBy("buchungsart.bezeichnung");
    it.addGroupBy("buchungsart.art");
    it.addGroupBy("buchungsart.status");
    it.addGroupBy("buchungsart.nummer");

    // Ggf. Buchungsarten ausblenden
    if (unterdrueckung)
    {
      it.addHaving("anzahl > 0 OR abs(" + SUMME + ") >= 0.01");
    }
    else
    {
      it.addHaving(
          "anzahl > 0 OR abs(" + SUMME + ") >= 0.01 OR buchungsart.status != ?",
          StatusBuchungsart.INACTIVE);
    }

    return it;
  }

  protected ExtendedDBIterator<PseudoDBObject> getSteuerIterator()
      throws RemoteException
  {
    final boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    final boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>("buchung");

    it.addColumn("buchungsart.status");

    it.addColumn("buchungsart.nummer as " + BUCHUNGSART_NUMMER);
    it.addColumn("buchungsart.bezeichnung as " + BUCHUNGSART);
    it.addColumn("buchungsart.art as " + ARTBUCHUNGSART);
    it.addColumn("buchungsart.id as " + BUCHUNGSART_ID);
    it.addColumn("buchungsart.status");

    it.addColumn("buchungsklasse.id as " + BUCHUNGSKLASSE_ID);
    it.addColumn("buchungsklasse.nummer as " + BUCHUNGSKLASSE_NUMMER);
    it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);

    it.addColumn(
        "SUM(CAST(buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2))) AS "
            + SUMME);
    // Für das Menu müssen wir wissen, ob es eine Steuer ist
    it.addColumn(
        "SUM(CAST(buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2))) AS "
            + STEUERBETRAG);

    // Keine Steuer bei Anlagekonten
    it.join("konto",
        "buchung.konto = konto.id and konto.kontoart < ? and konto.kontoart != ?",
        Kontoart.LIMIT.getKey(), Kontoart.ANLAGE.getKey());
    // Wenn die Steuer in der Buchung steht, können wir sie direkt nehmen,
    // sonst müssen wir den Umweg über die Buchungsart nehmen.
    if (steuerInBuchung)
    {
      it.join("steuer", "steuer.id = buchung.steuer");
    }
    else
    {
      it.join("buchungsart babuchung", "buchung.buchungsart = babuchung.id");
      it.join("steuer", "steuer.id = babuchung.steuer");
    }
    it.join("buchungsart", "steuer.buchungsart = buchungsart.id");
    it.addFilter("datum between ? and ?", getDatumvon().getDate(),
        getDatumbis().getDate());

    // Keine Steuer bei alten Steuerbuchungen mit dependencyid
    it.addFilter("buchung.dependencyid is null or  buchung.dependencyid = -1");
    it.addGroupBy("steuer.buchungsart");
    it.addGroupBy("buchungsklasse.id");
    it.addGroupBy("buchungsklasse.nummer");

    if (klasseInBuchung)
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = steuer.buchungsklasse");
    }
    else
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchungsart.buchungsklasse ");
    }

    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSENSALDO_TITEL, this);
  }

  @Override
  protected String getAuswertungSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSENSALDO_SUBTITEL, this);
  }

  @Override
  protected String getDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSENSALDO_DATEINAME, this);
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new BuchungsklassesaldoCSV(mitUmbuchung);
      case AuswertungPDF:
        return new BuchungsklassesaldoPDF(mitUmbuchung);
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }

}
