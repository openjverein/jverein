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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.SaldoPartExportDialog;
import de.jost_net.JVerein.io.JahresvergleichDaten;
import de.jost_net.JVerein.io.JahresvergleichDaten.Abschnitt;
import de.jost_net.JVerein.io.JahresvergleichDaten.Kennzahl;
import de.jost_net.JVerein.io.JahresvergleichDaten.Werte;
import de.jost_net.JVerein.io.JahresvergleichPDF;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/** Erstellt den Vergleich des aktuellen Geschäftsjahrs mit seinem Vorgänger. */
public class JahresvergleichControl
{
  private final AbstractView view;

  private Geschaeftsjahr aktuellesJahr;

  private Geschaeftsjahr vorjahr;

  private SelectInput aktuellesJahrInput;
  private SelectInput vorjahrInput;

  private List<Integer> geschaeftsjahre;

  private CheckboxInput gesamtUebersichtInput;
  private CheckboxInput buchungsartenvergleichInput;
  private CheckboxInput projektvergleichInput;

  public JahresvergleichControl(AbstractView view)
  {
    this.view = view;
  }

  public String getZeitraumAktuell() throws Exception
  {
    return new JVDateFormatTTMMJJJJ().format(getAusgewaehltesJahr(true)
        .getBeginnGeschaeftsjahr()) + " - "
        + new JVDateFormatTTMMJJJJ().format(getAusgewaehltesJahr(true)
            .getEndeGeschaeftsjahr());
  }

  public String getZeitraumVorjahr() throws Exception
  {
    return new JVDateFormatTTMMJJJJ().format(getAusgewaehltesJahr(false)
        .getBeginnGeschaeftsjahr()) + " - "
        + new JVDateFormatTTMMJJJJ().format(getAusgewaehltesJahr(false)
            .getEndeGeschaeftsjahr());
  }

  public Button getPDFButton()
  {
    return new Button("PDF erstellen", context -> exportPDF(), null, false,
        "file-pdf.png");
  }

  private Geschaeftsjahr getAktuellesJahr() throws Exception
  {
    if (aktuellesJahr == null)
    {
      aktuellesJahr = new Geschaeftsjahr(new Date());
    }
    return aktuellesJahr;
  }

  private Geschaeftsjahr getVorjahr() throws Exception
  {
    if (vorjahr == null)
    {
      vorjahr = new Geschaeftsjahr(
          getAktuellesJahr().getBeginnGeschaeftsjahrjahr() - 1);
    }
    return vorjahr;
  }

  private Geschaeftsjahr getAusgewaehltesJahr(boolean aktuell) throws Exception
  {
    SelectInput input = aktuell ? aktuellesJahrInput : vorjahrInput;
    Object value = input == null ? null : input.getValue();
    if (value instanceof Number)
    {
      return new Geschaeftsjahr(((Number) value).intValue());
    }
    return aktuell ? getAktuellesJahr() : getVorjahr();
  }

  private List<Integer> getGeschaeftsjahre() throws Exception
  {
    if (geschaeftsjahre != null)
    {
      return geschaeftsjahre;
    }
    int aktuell = getAktuellesJahr().getBeginnGeschaeftsjahrjahr();
    int fruehestes = aktuell;
    int spaetestes = aktuell;
    DBIterator<Buchung> iterator = Einstellungen.getDBService()
        .createList(Buchung.class);
    iterator.addFilter("datum is not null");
    iterator.setOrder("order by datum");
    while (iterator.hasNext())
    {
      Buchung buchung = iterator.next();
      int jahr = new Geschaeftsjahr(buchung.getDatum())
          .getBeginnGeschaeftsjahrjahr();
      fruehestes = Math.min(fruehestes, jahr);
      spaetestes = Math.max(spaetestes, jahr);
    }
    geschaeftsjahre = new ArrayList<>();
    for (int jahr = spaetestes; jahr >= fruehestes; jahr--)
    {
      geschaeftsjahre.add(jahr);
    }
    if (!geschaeftsjahre.contains(aktuell - 1))
    {
      geschaeftsjahre.add(aktuell - 1);
      Collections.sort(geschaeftsjahre, Collections.reverseOrder());
    }
    return geschaeftsjahre;
  }

  public void paintZeitraeume(org.eclipse.swt.widgets.Composite parent)
      throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent, true, 2);
    List<Integer> jahre = getGeschaeftsjahre();
    aktuellesJahrInput = new SelectInput(jahre, getAktuellesJahr()
        .getBeginnGeschaeftsjahrjahr());
    vorjahrInput = new SelectInput(jahre, getVorjahr()
        .getBeginnGeschaeftsjahrjahr());
    TextInput aktuell = new TextInput(getZeitraumAktuell());
    aktuell.disable();
    TextInput vorher = new TextInput(getZeitraumVorjahr());
    vorher.disable();
    aktuellesJahrInput.addListener(e -> aktualisiereZeitraeume(aktuell, vorher));
    vorjahrInput.addListener(e -> aktualisiereZeitraeume(aktuell, vorher));
    container.addLabelPair("Geschäftsjahr", aktuellesJahrInput);
    container.addLabelPair("Vergleichsjahr", vorjahrInput);
    container.addLabelPair("Zeitraum", aktuell);
    container.addLabelPair("Vergleichszeitraum", vorher);
    gesamtUebersichtInput = new CheckboxInput(true);
    buchungsartenvergleichInput = new CheckboxInput(true);
    projektvergleichInput = new CheckboxInput(true);
    container.addLabelPair("1. Gesamtübersicht", gesamtUebersichtInput);
    container.addLabelPair("2. Buchungsartenvergleich",
        buchungsartenvergleichInput);
    container.addLabelPair("3. Projektvergleich", projektvergleichInput);
  }

  private void aktualisiereZeitraeume(TextInput aktuell, TextInput vorher)
  {
    try
    {
      aktuell.setValue(getZeitraumAktuell());
      vorher.setValue(getZeitraumVorjahr());
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Aktualisieren der Vergleichszeiträume.", e);
    }
  }

  private void exportPDF() throws ApplicationException
  {
    try
    {
      EnumSet<Abschnitt> abschnitte = getAusgewaehlteAbschnitte();
      if (abschnitte.isEmpty())
      {
        throw new ApplicationException(
            "Bitte wählen Sie mindestens einen Bereich für den Bericht aus.");
      }
      JahresvergleichDaten daten = erstelleDaten(abschnitte);
      String aktuellerZeitraum = getZeitraumAktuell();
      String vorherigerZeitraum = getZeitraumVorjahr();
      SaldoPartExportDialog dialog = new SaldoPartExportDialog(
          new JahresvergleichPDF(daten, abschnitte),
          new ArrayList<>(), "Jahresvergleich.Export.", ExportArt.PDF,
          "Jahresvergleich", "Geschäftsjahr " + aktuellerZeitraum
              + " gegenüber " + vorherigerZeitraum,
          "Jahresvergleich");
      if (!dialog.open())
      {
        throw new OperationCanceledException();
      }
    }
    catch (OperationCanceledException | ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Erstellen des Jahresvergleichs.", e);
      throw new ApplicationException("Fehler beim Erstellen des Jahresvergleichs.", e);
    }
  }

  private EnumSet<Abschnitt> getAusgewaehlteAbschnitte()
  {
    EnumSet<Abschnitt> abschnitte = EnumSet.noneOf(Abschnitt.class);
    if (gesamtUebersichtInput != null
        && Boolean.TRUE.equals(gesamtUebersichtInput.getValue()))
    {
      abschnitte.add(Abschnitt.GESAMT);
    }
    if (buchungsartenvergleichInput != null
        && Boolean.TRUE.equals(buchungsartenvergleichInput.getValue()))
    {
      abschnitte.add(Abschnitt.BUCHUNGSART);
    }
    if (projektvergleichInput != null
        && Boolean.TRUE.equals(projektvergleichInput.getValue()))
    {
      abschnitte.add(Abschnitt.PROJEKT);
    }
    return abschnitte;
  }

  private JahresvergleichDaten erstelleDaten(EnumSet<Abschnitt> abschnitte)
      throws Exception
  {
    JahresvergleichDaten daten = new JahresvergleichDaten();
    Geschaeftsjahr aktuellesJahr = getAusgewaehltesJahr(true);
    Geschaeftsjahr vergleichsjahr = getAusgewaehltesJahr(false);
    List<PseudoDBObject> aktuellerSaldo = List.of();
    List<PseudoDBObject> vorherigerSaldo = List.of();
    if (abschnitte.contains(Abschnitt.GESAMT)
        || abschnitte.contains(Abschnitt.BUCHUNGSART))
    {
      SaldoPeriodenControl aktuell = new SaldoPeriodenControl(view,
          aktuellesJahr);
      SaldoPeriodenControl vorher = new SaldoPeriodenControl(view,
          vergleichsjahr);
      aktuellerSaldo = aktuell.getList();
      vorherigerSaldo = vorher.getList();
    }

    if (abschnitte.contains(Abschnitt.GESAMT))
    {
      BigDecimal[] summenAktuell = gesamtwerte(aktuellerSaldo);
      BigDecimal[] summenVorjahr = gesamtwerte(vorherigerSaldo);
      daten.add(Abschnitt.GESAMT, "Gesamt", Kennzahl.EINNAHMEN,
          summenAktuell[0], summenVorjahr[0]);
      daten.add(Abschnitt.GESAMT, "Gesamt", Kennzahl.AUSGABEN,
          summenAktuell[1], summenVorjahr[1]);
      daten.add(Abschnitt.GESAMT, "Gesamt", Kennzahl.SALDO,
          summenAktuell[2], summenVorjahr[2]);
    }

    if (abschnitte.contains(Abschnitt.BUCHUNGSART))
    {
      boolean detaillierteBuchungsarten = Boolean.TRUE.equals(
          Einstellungen.getEinstellung(
              Einstellungen.Property.BUCHUNGSKLASSEINBUCHUNG));
      addBuchungsklassenVergleich(daten, aktuellerSaldo, vorherigerSaldo,
          detaillierteBuchungsarten);
    }

    if (abschnitte.contains(Abschnitt.PROJEKT))
    {
      ProjektPeriodenControl projektAktuell = new ProjektPeriodenControl(view,
          aktuellesJahr);
      ProjektPeriodenControl projektVorjahr = new ProjektPeriodenControl(view,
          vergleichsjahr);
      addVergleich(daten, Abschnitt.PROJEKT,
          gruppiere(projektAktuell.getList(), true),
          gruppiere(projektVorjahr.getList(), true));
    }
    return daten;
  }

  private void addVergleich(JahresvergleichDaten daten, Abschnitt abschnitt,
      Aggregat aktuell, Aggregat vorjahr)
  {
    addVergleich(daten, abschnitt, aktuell, vorjahr, true);
  }

  private void addVergleich(JahresvergleichDaten daten, Abschnitt abschnitt,
      Aggregat aktuell, Aggregat vorjahr, boolean detaillierteKennzahlen)
  {
    Map<String, String> namen = new LinkedHashMap<>(aktuell.namen);
    vorjahr.namen.forEach(namen::putIfAbsent);
    daten.addBereich(abschnitt, namen, aktuell.werte, vorjahr.werte,
        detaillierteKennzahlen);
  }

  private void addBuchungsklassenVergleich(JahresvergleichDaten daten,
      List<PseudoDBObject> aktuell, List<PseudoDBObject> vorjahr,
      boolean detaillierteKennzahlen) throws RemoteException
  {
    BuchungsklassenDaten aktuelleDaten = gruppiereBuchungsklassen(aktuell);
    BuchungsklassenDaten vorherigeDaten = gruppiereBuchungsklassen(vorjahr);
    Map<String, String> gruppenNamen = new LinkedHashMap<>(
        aktuelleDaten.gruppenNamen);
    vorherigeDaten.gruppenNamen.forEach(gruppenNamen::putIfAbsent);
    Map<String, Map<String, String>> namen = new LinkedHashMap<>(
        aktuelleDaten.buchungsartNamen);
    vorherigeDaten.buchungsartNamen.forEach((key, value) ->
    {
      Map<String, String> gruppenNamenJeKlasse = namen.computeIfAbsent(key,
          ignored -> new LinkedHashMap<>());
      value.forEach(gruppenNamenJeKlasse::putIfAbsent);
    });
    daten.addBuchungsklassenBereich(Abschnitt.BUCHUNGSART, gruppenNamen,
        namen, aktuelleDaten.werte, vorherigeDaten.werte,
        detaillierteKennzahlen);
  }

  private BuchungsklassenDaten gruppiereBuchungsklassen(
      List<PseudoDBObject> zeilen) throws RemoteException
  {
    BuchungsklassenDaten daten = new BuchungsklassenDaten();
    for (PseudoDBObject zeile : zeilen)
    {
      if (!Integer.valueOf(AbstractSaldoControl.ART_DETAIL)
          .equals(zeile.getInteger(AbstractSaldoControl.ART)))
      {
        continue;
      }
      Integer anzahl = zeile.getInteger(AbstractSaldoControl.ANZAHL);
      if (anzahl != null && anzahl == 0)
      {
        continue;
      }
      String klassenKey = key(
          zeile.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_ID));
      String klassenName = text(
          zeile.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_TEXT));
      String artenKey = key(
          zeile.getAttribute(AbstractSaldoControl.BUCHUNGSART_ID));
      String artenName = text(
          zeile.getAttribute(AbstractSaldoControl.BUCHUNGSART_TEXT));
      daten.gruppenNamen.putIfAbsent(klassenKey, klassenName);
      daten.buchungsartNamen.computeIfAbsent(klassenKey,
          ignored -> new LinkedHashMap<>()).putIfAbsent(artenKey, artenName);
      Werte werte = daten.werte.computeIfAbsent(klassenKey,
          ignored -> new LinkedHashMap<>()).computeIfAbsent(artenKey,
              ignored -> new Werte());
      werte.add(Kennzahl.EINNAHMEN,
          zeile.getBigDecimal(AbstractSaldoControl.EINNAHMEN));
      werte.add(Kennzahl.AUSGABEN,
          zeile.getBigDecimal(AbstractSaldoControl.AUSGABEN));
      werte.add(Kennzahl.UMBUCHUNGEN,
          zeile.getBigDecimal(AbstractSaldoControl.UMBUCHUNGEN));
    }
    return daten;
  }

  private String key(Object value)
  {
    return value == null ? "__nicht_zugeordnet__" : value.toString();
  }

  private String text(Object value)
  {
    return value == null || value.toString().isBlank()
        ? "Nicht zugeordnet" : value.toString();
  }

  private Aggregat gruppiere(List<PseudoDBObject> zeilen, boolean projekt)
      throws RemoteException
  {
    Aggregat aggregat = new Aggregat();
    String idFeld = projekt ? AbstractSaldoControl.PROJEKT_ID
        : AbstractSaldoControl.BUCHUNGSART_ID;
    String nameFeld = projekt ? AbstractSaldoControl.PROJEKT
        : AbstractSaldoControl.BUCHUNGSART_TEXT;
    for (PseudoDBObject zeile : zeilen)
    {
      if (!Integer.valueOf(AbstractSaldoControl.ART_DETAIL)
          .equals(zeile.getInteger(AbstractSaldoControl.ART)))
      {
        continue;
      }
      Integer anzahl = zeile.getInteger(AbstractSaldoControl.ANZAHL);
      if (anzahl != null && anzahl == 0)
      {
        continue;
      }
      Object id = zeile.getAttribute(idFeld);
      Object name = zeile.getAttribute(nameFeld);
      String key = id == null ? "__nicht_zugeordnet__" : id.toString();
      String text = name == null || name.toString().isBlank()
          ? "Nicht zugeordnet" : name.toString();
      aggregat.namen.putIfAbsent(key, text);
      Werte werte = aggregat.werte.computeIfAbsent(key, ignored -> new Werte());
      werte.add(Kennzahl.EINNAHMEN,
          zeile.getBigDecimal(AbstractSaldoControl.EINNAHMEN));
      werte.add(Kennzahl.AUSGABEN,
          zeile.getBigDecimal(AbstractSaldoControl.AUSGABEN));
      werte.add(Kennzahl.UMBUCHUNGEN,
          zeile.getBigDecimal(AbstractSaldoControl.UMBUCHUNGEN));
    }
    return aggregat;
  }

  private BigDecimal[] gesamtwerte(List<PseudoDBObject> zeilen)
      throws RemoteException
  {
    BigDecimal einnahmen = BigDecimal.ZERO;
    BigDecimal ausgaben = BigDecimal.ZERO;
    BigDecimal saldo = BigDecimal.ZERO;
    for (PseudoDBObject zeile : zeilen)
    {
      Integer art = zeile.getInteger(AbstractSaldoControl.ART);
      if (Integer.valueOf(AbstractSaldoControl.ART_GESAMTSALDOFOOTER)
          .equals(art))
      {
        einnahmen = nullToZero(
            zeile.getBigDecimal(AbstractSaldoControl.EINNAHMEN));
        ausgaben = nullToZero(
            zeile.getBigDecimal(AbstractSaldoControl.AUSGABEN));
      }
      else if (Integer.valueOf(AbstractSaldoControl.ART_GESAMTGEWINNVERLUST)
          .equals(art))
      {
        saldo = nullToZero(
            zeile.getBigDecimal(AbstractSaldoControl.EINNAHMEN));
      }
    }
    return new BigDecimal[] { einnahmen, ausgaben, saldo };
  }

  private BigDecimal nullToZero(BigDecimal wert)
  {
    return wert == null ? BigDecimal.ZERO : wert;
  }

  private static class Aggregat
  {
    private final Map<String, String> namen = new LinkedHashMap<>();
    private final Map<String, Werte> werte = new LinkedHashMap<>();
  }

  private static class BuchungsklassenDaten
  {
    private final Map<String, String> gruppenNamen = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> buchungsartNamen =
        new LinkedHashMap<>();
    private final Map<String, Map<String, Werte>> werte = new LinkedHashMap<>();
  }

  private static class SaldoPeriodenControl extends BuchungsklasseSaldoControl
  {
    private final DateLabel von;
    private final DateLabel bis;

    SaldoPeriodenControl(AbstractView view, Geschaeftsjahr jahr)
        throws RemoteException
    {
      super(view);
      von = new DateLabel(jahr.getBeginnGeschaeftsjahr());
      bis = new DateLabel(jahr.getEndeGeschaeftsjahr());
    }

    @Override
    public DateLabel getDatumvon()
    {
      return von;
    }

    @Override
    public DateLabel getDatumbis()
    {
      return bis;
    }
  }

  private static class ProjektPeriodenControl extends ProjektSaldoControl
  {
    private final DateLabel von;
    private final DateLabel bis;
    private SelectInput keineProjektAuswahl;

    ProjektPeriodenControl(AbstractView view, Geschaeftsjahr jahr)
        throws RemoteException
    {
      super(view);
      von = new DateLabel(jahr.getBeginnGeschaeftsjahr());
      bis = new DateLabel(jahr.getEndeGeschaeftsjahr());
    }

    @Override
    public DateLabel getDatumvon()
    {
      return von;
    }

    @Override
    public DateLabel getDatumbis()
    {
      return bis;
    }

    @Override
    public SelectInput getProjekt()
    {
      if (keineProjektAuswahl == null)
      {
        keineProjektAuswahl = new SelectInput(List.of(), null);
      }
      return keineProjektAuswahl;
    }
  }
}
