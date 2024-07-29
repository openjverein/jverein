/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.util.ApplicationException;

/**
 * Action zur Generierung von Spendenbescheinigungen aus der Datenbank.<br>
 * Diese Klasse kapselt die Generierung des Standard-Formulars und wird auch bei
 * der Generierung eines Dokuments aus der Detailansicht der
 * Spendenbescheinigung heraus verwendet.
 */
public class SpendenbescheinigungPrintAction implements Action
{

  private boolean standardPdf = true;
  
  private boolean adressblatt = false;

  private String fileName = null;

  private de.willuhn.jameica.system.Settings settings;

  /**
   * Konstruktor ohne Parameter. Es wird angenommen, dass das Standard-Dokument
   * aufbereitet werden soll.
   */
  public SpendenbescheinigungPrintAction()
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Konstruktor. �ber den Parameter kann festgelegt werden, ob das Standard-
   * oder das individuelle Dokument aufbereitet werden soll.
   * 
   * @param standard
   *          true=Standard-Dokument, false=individuelles Dokument
   * @param adressblatt
   *          true=f�r Adressblatt drucken, false=f�r kein Adressblatt drucken
   */
  public SpendenbescheinigungPrintAction(boolean standard, boolean adressblatt)
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    standardPdf = standard;
    this.adressblatt = adressblatt;
  }

  /**
   * Konstruktor. �ber den Parameter kann festgelegt werden, ob das Standard-
   * oder das individuelle Dokument aufbereitet werden soll.
   * 
   * @param standard
   *          true=Standard-Dokument, false=individuelles Dokument
   * @param adressblatt
   *          true=Standard-Dokument, false=individuelles Dokument
   * @param fileName
   *          Dateiname als Vorgabe inklusive Pfad
   */
  public SpendenbescheinigungPrintAction(boolean standard, boolean adressblatt, String fileName)
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    standardPdf = standard;
    this.fileName = fileName;
    this.adressblatt = adressblatt;
  }

  /**
   * Aufbereitung der Spendenbescheinigungen
   * Hinweis: Das bzw. die generierten Formulare werden nicht im Acrobat Reader
   * angezeigt.
   * 
   * @param context
   *          Die Spendenbescheinigung(en)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Spendenbescheinigung[] spbArr = null;
    // Pr�fung des Contexs, vorhanden, eine oder mehrere
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Spendenbescheinigung ausgew�hlt");
    }
    else if (context instanceof Spendenbescheinigung)
    {
      spbArr = new Spendenbescheinigung[] { (Spendenbescheinigung) context };
    }
    else if (context instanceof Spendenbescheinigung[])
    {
      spbArr = (Spendenbescheinigung[]) context;
    }
    else
    {
      return;
    }
    // Aufbereitung
    try
    {
      String path = Einstellungen.getEinstellung()
          .getSpendenbescheinigungverzeichnis();
      if (path == null || path.length() == 0)
      {
        path = settings.getString("lastdir", System.getProperty("user.home"));
      }

      settings.setAttribute("lastdir", path);
      path = path.endsWith(File.separator) ? path : path + File.separator;
      if (!standardPdf)
      {
        // Check ob auch f�r alle Spendenbescheinigungen ein Forumular
        // ausgewaehlt ist
        for (Spendenbescheinigung spb : spbArr)
        {
          Formular spendeformular = spb.getFormular();
          if (spendeformular == null)
          {
            String text = "Nicht alle Spendenbescheinigungen haben ein g�ltiges Formular!";
            throw new ApplicationException(text);
          }
        }
      }
      // Start der Aufbereitung der Dokumente
      for (Spendenbescheinigung spb : spbArr)
      {
        String fileName = null;
        if (spbArr.length > 1 || this.fileName == null)
        {
          // Dokumentennamen aus konfiguriertem Verzeichnis und dem
          // DateinamenmusterSpende
          // zusammensetzen, wenn mehr als eine Spendenbescheinigung
          // aufzubereiten
          // oder keine Vorgabe f�r einen Dateinamen gemacht wurde.
          if (spb.getMitglied() != null)
          {
            fileName = new Dateiname(spb.getMitglied(),
                spb.getBescheinigungsdatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get();
          }
          else
          {
            fileName = new Dateiname(spb.getZeile1(), spb.getZeile2(),
                spb.getBescheinigungsdatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get();
          }
          fileName = path + fileName;
        }
        else
        {
          fileName = this.fileName;
        }
        final File file = new File(fileName);
        // Aufbereitung des Dokumentes
        if (standardPdf)
        {
          GregorianCalendar gc = new GregorianCalendar();
          gc.setTime(spb.getBescheinigungsdatum());
          if (gc.get(GregorianCalendar.YEAR) <= 2012)
          {
            generiereSpendenbescheinigungStandard(spb, fileName);
          }
          else if (gc.get(GregorianCalendar.YEAR) == 2013)
          {
            generiereSpendenbescheinigungStandardAb2013(spb, fileName);
          }
          else
          {
            generiereSpendenbescheinigungStandardAb2014(spb, fileName, adressblatt);
          }
        }
        else
        {
          Formular fo = (Formular) Einstellungen.getDBService()
              .createObject(Formular.class, spb.getFormular().getID());
          Map<String, Object> map = spb.getMap(null);
          map = new AllgemeineMap().getMap(map);
          FormularAufbereitung fa = new FormularAufbereitung(file);
          fa.writeForm(fo, map);
          // Brieffenster drucken bei Spendenbescheinigung
          if (adressblatt)
          {
            fa.printAdressfenster(getAussteller(), 
                (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()));
          }
          fa.closeFormular();
        }
      }
      String erfolg = (spbArr.length > 1) ? "Die Spendenbescheinigungen wurden erstellt und unter " + path + " gespeichert."
                                          : "Die Spendenbescheinigung wurde erstellt und unter " + path + " gespeichert.";
      GUI.getStatusBar().setSuccessText(erfolg);
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Aufbereiten der Spendenbescheinigung ("
          + e.getMessage() + ")";
      throw new ApplicationException(fehler);
    }
  }

  /**
   * Generierung des Standard-Dokumentes zu verwenden f�r Spendenbescheinigungen
   * bis 31.12.2012
   * 
   * @param spb
   *          Die Spendenbescheinigung aus der Datenbank
   * @param fileName
   *          Der Dateiname, wohin das Dokument geschrieben werden soll
   * @throws IOException
   * @throws DocumentException
   */
  private void generiereSpendenbescheinigungStandard(Spendenbescheinigung spb,
      String fileName) throws IOException, DocumentException
  {
    final File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);

    Map<String, Object> map = spb.getMap(null);
    map = new AllgemeineMap().getMap(map);

    boolean isSammelbestaetigung = spb.isSammelbestaetigung();

    Reporter rpt = new Reporter(fos, 80, 50, 50, 50);
    rpt.addHeaderColumn(
        "Aussteller (Bezeichnung und Anschrift der steuerbeg�nstigten Einrichtung)",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);

    rpt.createHeader();

    rpt.addColumn("\n" + getAussteller() + "\n ", Element.ALIGN_LEFT);
    rpt.closeTable();

    if (isSammelbestaetigung)
    {
      rpt.add("Sammelbest�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 13);
    }
    else
    {
      rpt.add("Best�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 13);
    }

    rpt.add(
        "im Sinne des � 10b des Einkommenssteuergesetzes an eine der in � 5 Abs. 1 Nr. 9 des K�rperschaftssteuergesetzes "
            + "bezeichneten K�rperschaften, Personenvereinigungen oder Verm�gensmassen\n",
        10);

    rpt.addHeaderColumn("Name und Anschrift des Zuwendenden",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),
        Element.ALIGN_LEFT);
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.GELDSPENDE:
        rpt.addHeaderColumn("Betrag der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn("Wert der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
    }
    rpt.addHeaderColumn("-in Buchstaben-", Element.ALIGN_CENTER, 250,
        BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Tag der Zuwendung", Element.ALIGN_CENTER, 50,
        BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        "*" + Einstellungen.DECIMALFORMAT
            .format(map.get(SpendenbescheinigungVar.BETRAG.getName())) + "*",
        Element.ALIGN_CENTER);
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()),
        Element.ALIGN_CENTER);
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.SPENDEDATUM.getName()),
        Element.ALIGN_CENTER);
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn(
            "Genaue Bezeichnung der Sachzuwendung mit Alter, Zustand, Kaufpreis usw.",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        rpt.createHeader();
        rpt.addColumn(spb.getBezeichnungSachzuwendung(), Element.ALIGN_LEFT);
        rpt.closeTable();
        switch (spb.getHerkunftSpende())
        {
          case HerkunftSpende.BETRIEBSVERMOEGEN:
            rpt.add(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsverm�gen und ist "
                    + "mit dem Entnahmewert (ggf. mit dem niedrigeren gemeinen Wert) bewertet.\n\n",
                9);
            break;
          case HerkunftSpende.PRIVATVERMOEGEN:
            rpt.add(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatverm�gen.\n\n",
                9);
            break;
          case HerkunftSpende.KEINEANGABEN:
            rpt.add(
                "Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.\n\n",
                9);
            break;
        }
        if (spb.getUnterlagenWertermittlung())
        {
          rpt.add(
              "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.\n\n",
              9);
        }
    }

    /*
     * Bei Sammelbest�tigungen ist der Verweis auf Verzicht in der Anlage
     * vermerkt
     */
    String verzicht = "nein";
    boolean andruckVerzicht = false;

    if (spb.getAutocreate())
    {
      if (!isSammelbestaetigung)
      {
        if (spb.getBuchungen().get(0).getVerzicht().booleanValue())
        {
          verzicht = "ja";
        }
        andruckVerzicht = true;
      }
    }
    else
    {
      if (spb.getErsatzAufwendungen())
      {
        verzicht = "ja";
      }
      andruckVerzicht = true;
    }

    if (!isSammelbestaetigung)
    {
      if (andruckVerzicht)
      {
        rpt.add("Es handelt sich um den Verzicht von Aufwendungen: " + verzicht
            + "\n\n", 9);
      }
      else
      {
        rpt.add(
            "Es handelt sich nicht um den Verzicht auf Erstattung von Aufwendungen: "
                + "\n\n",
            9);
      }
    }
    if (!Einstellungen.getEinstellung().getVorlaeufig())
    {
      // rdc: "F�rderung" entfernt, da in "Beguenstigterzweck" enthalten
      String txt = "Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " nach dem letzten uns zugegangenen Freistellungsbescheid bzw. nach der Anlage zum K�rperschaftssteuerbescheid des Finanzamtes "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer() + ", vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " nach � 5 Abs. 1 Nr. 9 des K�rperschaftsteuergesetzes von der K�rperschaftsteuer und nach � 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.";
      rpt.add(txt, 9);
    }
    else
    {
      // rdc: "F�rderung" entfernt, da in "Beguenstigterzweck" enthalten
      String txt = "Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " durch vorl�ufige Bescheinigung des Finanzamtes "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer() + ", vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " ab "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getVorlaeufigab())
          + " als steuerbeg�nstigten Zwecken dienend anerkannt.";
      rpt.add(txt, 9);
    }
    // rdc: "F�rderung" entfernt, da in "Beguenstigterzweck" enthalten
    rpt.add("\n\nEs wird best�tigt, dass die Zuwendung nur zur "
        + Einstellungen.getEinstellung().getBeguenstigterzweck()
        + " verwendet wird.\n", 9);
    if (!Einstellungen.getEinstellung().getMitgliedsbetraege()
        && spb.getSpendenart() == Spendenart.GELDSPENDE)
    {
      rpt.add(
          "Es wird best�tigt, dass es sich nicht um einen Mitgliedsbeitrag i.S.v � 10b Abs. 1 Satz 2 Einkommensteuergesetzes handelt.",
          9);
    }

    if (isSammelbestaetigung)
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add(
          "Es wird best�tigt, dass �ber die in der Gesamtsumme enthaltenen Zuwendungen keine weiteren Best�tigungen, weder formelle Zuwendungsbest�tigungen noch Beitragsquittungen oder �hnliches ausgestellt wurden und werden.",
          9);
    }

    rpt.add(
        "\n\n" + Einstellungen.getEinstellung().getOrt() + ", "
            + new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
        9);

    rpt.add(
        "\n\n\n\n.................................................................................\nUnterschrift des Zuwendungsempf�ngers",
        9);

    rpt.add("\n\nHinweis:", 9);
    rpt.add(
        "\nWer vors�tzlich oder grob fahrl�ssig eine unrichtige Zuwendungsbest�tigung erstellt oder wer veranlasst, dass "
            + "Zuwendungen nicht zu den in der Zuwendungsbest�tigung angegebenen steuerbeg�nstigten Zwecken verwendet "
            + "werden, haftet f�r die Steuer, die dem Fiskus durch einen etwaigen Abzug der Zuwendungen beim Zuwendenden "
            + "entgeht (� 10b Abs. 4 EStG, � 9 Abs. 3 KStG, � 9 Nr. 5 GewStG).\n\n"
            + "Diese Best�tigung wird nicht als Nachweis f�r die steuerliche Ber�cksichtigung der Zuwendung anerkannt, wenn das "
            + "Datum des Freistellungsbescheides l�nger als 5 Jahre bzw. das Datum der vorl�ufigen Bescheinigung l�nger als 3 Jahre "
            + "seit Ausstellung der Best�tigung zur�ckliegt (BMF vom 15.12.1994 - BStBl I S. 884).",
        8);

    /* Es sind mehrere Spenden f�r diese Spendenbescheinigung vorhanden */
    if (isSammelbestaetigung)
    {
      List<Buchung> buchungen = spb.getBuchungen();

      rpt.newPage();
      rpt.add(getAussteller(), 13);
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("Anlage zur Sammelbest�tigung vom " + (String) map
          .get(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName()), 11);
      rpt.add("f�r den Zeitraum vom "
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          11);

      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));

      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 100,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag in EUR", Element.ALIGN_RIGHT, 100,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Verwendung", Element.ALIGN_LEFT, 500,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();

      boolean printBuchungsart = Einstellungen.getEinstellung()
          .getSpendenbescheinigungPrintBuchungsart();

      for (Buchung buchung : buchungen)
      {
        rpt.addColumn(buchung.getDatum(), Element.ALIGN_RIGHT);
        rpt.addColumn(Double.valueOf(buchung.getBetrag()));
        String verwendung = "";
        if (printBuchungsart)
        {
          verwendung = buchung.getBuchungsart().getBezeichnung();
          // rpt.addColumn(buchung.getBuchungsart().getBezeichnung(),
          // Element.ALIGN_LEFT);
        }
        else
        {
          verwendung = buchung.getZweck();
          // rpt.addColumn(buchung.getZweck(), Element.ALIGN_LEFT);
        }
        if (buchung.getVerzicht().booleanValue())
        {
          verwendung = verwendung + " (b)";
          // rpt.addColumn("Verzicht auf Erstattung von Aufwendungen",
          // Element.ALIGN_LEFT);
        }
        else
        {
          verwendung = verwendung + " (a)";
          // rpt.addColumn("Kein Verzicht auf Erstattung von Aufwendungen",
          // Element.ALIGN_LEFT);
        }
        rpt.addColumn(verwendung, Element.ALIGN_LEFT);
      }

      /* Summenzeile */
      String sumString = Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
      rpt.addColumn("Summe", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn(sumString, Element.ALIGN_RIGHT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);

      rpt.closeTable();
      // Etwas Abstand
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      // Nun noch die Legende
      rpt.add("Legende:", 8);
      rpt.add(
          "(a): Es handelt sich nicht um den Verzicht auf Erstattung von Aufwendungen",
          8);
      rpt.add(
          "(b): Es handelt sich um den Verzicht auf Erstattung von Aufwendungen",
          8);
    }

    rpt.close();
    fos.close();
  }

  /**
   * Generierung des Standard-Dokumentes zu verwenden f�r Spendenbescheinigungen
   * ab 01.01.2013
   * 
   * @param spb
   *          Die Spendenbescheinigung aus der Datenbank
   * @param fileName
   *          Der Dateiname, wohin das Dokument geschrieben werden soll
   * @throws IOException
   * @throws DocumentException
   */
  private void generiereSpendenbescheinigungStandardAb2013(
      Spendenbescheinigung spb, String fileName)
      throws IOException, DocumentException
  {
    final File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);

    Map<String, Object> map = spb.getMap(null);
    map = new AllgemeineMap().getMap(map);

    boolean isSammelbestaetigung = spb.isSammelbestaetigung();

    Reporter rpt = new Reporter(fos, 80, 50, 50, 50);
    rpt.addHeaderColumn(
        "Aussteller (Bezeichnung und Anschrift der steuerbeg�nstigten Einrichtung)",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);

    rpt.createHeader();

    rpt.addColumn("\n" + getAussteller() + "\n ", Element.ALIGN_LEFT);
    rpt.closeTable();

    if (isSammelbestaetigung)
    {
      rpt.add("Sammelbest�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 13);
    }
    else
    {
      rpt.add("Best�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 13);
    }

    rpt.add(
        "im Sinne des � 10b des Einkommenssteuergesetzes an eine der in � 5 Abs. 1 Nr. 9 des K�rperschaftssteuergesetzes "
            + "bezeichneten K�rperschaften, Personenvereinigungen oder Verm�gensmassen\n",
        10);

    rpt.addHeaderColumn("Name und Anschrift des Zuwendenden",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),
        Element.ALIGN_LEFT);
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.GELDSPENDE:
        rpt.addHeaderColumn("Betrag der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn("Wert der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
    }
    rpt.addHeaderColumn("-in Buchstaben-", Element.ALIGN_CENTER, 250,
        BaseColor.LIGHT_GRAY);
    if (!isSammelbestaetigung)
    {
      rpt.addHeaderColumn("Tag der Zuwendung", Element.ALIGN_CENTER, 50,
          BaseColor.LIGHT_GRAY);
    }
    else
    {
      rpt.addHeaderColumn("Zeitraum der Sammelbest�tigung",
          Element.ALIGN_CENTER, 75, BaseColor.LIGHT_GRAY);
    }
    rpt.createHeader();
    rpt.addColumn(
        "*" + Einstellungen.DECIMALFORMAT
            .format(map.get(SpendenbescheinigungVar.BETRAG.getName())) + "*",
        Element.ALIGN_CENTER);
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()),
        Element.ALIGN_CENTER);
    if (!isSammelbestaetigung)
    {
      rpt.addColumn(
          (String) map.get(SpendenbescheinigungVar.SPENDEDATUM.getName()),
          Element.ALIGN_CENTER);
    }
    else
    {
      rpt.addColumn(
          (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          Element.ALIGN_CENTER);
    }
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn(
            "Genaue Bezeichnung der Sachzuwendung mit Alter, Zustand, Kaufpreis usw.",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        rpt.createHeader();
        rpt.addColumn(spb.getBezeichnungSachzuwendung(), Element.ALIGN_LEFT);
        rpt.closeTable();
        switch (spb.getHerkunftSpende())
        {
          case HerkunftSpende.BETRIEBSVERMOEGEN:
            rpt.add(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsverm�gen und ist "
                    + "mit dem Entnahmewert (ggf. mit dem niedrigeren gemeinen Wert) bewertet.\n\n",
                9);
            break;
          case HerkunftSpende.PRIVATVERMOEGEN:
            rpt.add(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatverm�gen.\n\n",
                9);
            break;
          case HerkunftSpende.KEINEANGABEN:
            rpt.add(
                "Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.\n\n",
                9);
            break;
        }
        if (spb.getUnterlagenWertermittlung())
        {
          rpt.add(
              "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.\n\n",
              9);
        }
    }

    /*
     * Bei Sammelbest�tigungen ist der Verweis auf Verzicht in der Anlage
     * vermerkt
     */
    String verzicht = "";
    char verzichtJa = (char) 113; // box leer
    char verzichtNein = (char) 53; // X

    if (spb.getAutocreate())
    {
      if (!isSammelbestaetigung && spb.getSpendenart() == Spendenart.GELDSPENDE)
      {
        if (spb.getBuchungen().get(0).getVerzicht().booleanValue())
        {
          verzichtJa = (char) 53; // X
          verzichtNein = (char) 113; // box leer
        }
      }
    }
    else
    {
      if (spb.getErsatzAufwendungen())
      {
        verzichtJa = (char) 53; // X
        verzichtNein = (char) 113; // box leer
      }
    }

    if (!isSammelbestaetigung)
    {
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(9));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk(
          "Es handelt sich um den Verzicht auf Erstattung von Aufwendungen: "));
      p.add(new Chunk(" Ja ", Reporter.getFreeSansBold(9)));
      p.add(new Chunk(verzichtJa,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk("   Nein ", Reporter.getFreeSansBold(9)));
      p.add(new Chunk(verzichtNein,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk("\n\n"));
      rpt.add(p);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
    }
    if (!Einstellungen.getEinstellung().getVorlaeufig())
    {
      String txt = "Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " nach dem letzten uns zugegangenen Freistellungsbescheid bzw. nach der Anlage zum K�rperschaftssteuerbescheid des Finanzamtes "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer() + ", vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " nach � 5 Abs. 1 Nr. 9 des K�rperschaftsteuergesetzes von der K�rperschaftsteuer und nach � 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.";
      rpt.add(txt, 8);
    }
    else
    {
      String txt = "Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " durch vorl�ufige Bescheinigung des Finanzamtes "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer() + ", vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " ab "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getVorlaeufigab())
          + " als beg�nstigten Zwecken dienend anerkannt.";
      rpt.add(txt, 8);
    }
    rpt.add("\nEs wird best�tigt, dass die Zuwendung nur zur "
        + Einstellungen.getEinstellung().getBeguenstigterzweck()
        + " verwendet wird.\n", 8);
    if (spb.getSpendenart() == Spendenart.GELDSPENDE)
    {
      char mitgliedBetraege = (char) 113; // box leer
      if (!Einstellungen.getEinstellung().getMitgliedsbetraege())
      {
        mitgliedBetraege = (char) 53; // X
      }
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(9));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk("\n"));
      p.add(new Chunk(
          "Nur f�r steuerbeg�nstigte Einrichtungen, bei denen die Mitgliedsbeitr�ge steuerlich nicht abziehbar sind:"));
      rpt.add(p);
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(9));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -18.5);
      p.setIndentationLeft((float) 18.5);
      p.add(new Chunk(mitgliedBetraege,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk(
          "   Es wird best�tigt, dass es sich nicht um einen Mitgliedsbeitrag handelt, dessen Abzug nach � 10b Abs. 1 des Einkommensteuergesetzes ausgeschlossen ist."));
      rpt.add(p);
    }

    if (isSammelbestaetigung)
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add(
          "Es wird best�tigt, dass �ber die in der Gesamtsumme enthaltenen Zuwendungen keine weiteren Best�tigungen, weder formelle Zuwendungsbest�tigungen noch Beitragsquittungen oder �hnliches ausgestellt wurden und werden.\n",
          8);
      rpt.add(
          "Ob es sich um den Verzicht auf Erstattung von Aufwendungen handelt, ist der Anlage zur Sammelbest�tigung zu entnehmen.",
          8);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n\n", 8);
      rpt.add("\n", 9);

    }

    rpt.add(
        "\n\n" + Einstellungen.getEinstellung().getOrt() + ", "
            + new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
        9);

    rpt.add(
        "\n\n\n\n.................................................................................\nUnterschrift des Zuwendungsempf�ngers",
        8);

    rpt.add("\nHinweis:", 9);
    rpt.add(
        "Wer vors�tzlich oder grob fahrl�ssig eine unrichtige Zuwendungsbest�tigung erstellt oder wer veranlasst, dass "
            + "Zuwendungen nicht zu den in der Zuwendungsbest�tigung angegebenen steuerbeg�nstigten Zwecken verwendet "
            + "werden, haftet f�r die entgangene Steuer (� 10b Abs. 4 EStG, � 9 Abs. 3 KStG, � 9 Nr. 5 GewStG).\n\n"
            + "Diese Best�tigung wird nicht als Nachweis f�r die steuerliche Ber�cksichtigung der Zuwendung anerkannt, wenn das "
            + "Datum des Freistellungsbescheides l�nger als 5 Jahre bzw. das Datum der vorl�ufigen Bescheinigung l�nger als 3 Jahre "
            + "seit Ausstellung der Best�tigung zur�ckliegt (BMF vom 15.12.1994 - BStBl I S. 884).",
        8);

    /* Es sind mehrere Spenden f�r diese Spendenbescheinigung vorhanden */
    if (isSammelbestaetigung)
    {
      List<Buchung> buchungen = spb.getBuchungen();

      rpt.newPage();
      rpt.add(getAussteller(), 13);
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("Anlage zur Sammelbest�tigung vom " + (String) map
          .get(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName()), 11);
      rpt.add("f�r den Zeitraum vom "
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          11);

      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));

      /* Kopfzeile */
      rpt.addHeaderColumn("Datum der\nZuwendung", Element.ALIGN_LEFT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Art der\nZuwendung", Element.ALIGN_LEFT, 400,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Verzicht auf die\nErstattung von Aufwendungen",
          Element.ALIGN_LEFT, 300, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();

      boolean printBuchungsart = Einstellungen.getEinstellung()
          .getSpendenbescheinigungPrintBuchungsart();

      /* Buchungszeilen */
      for (Buchung buchung : buchungen)
      {
        rpt.addColumn(buchung.getDatum(), Element.ALIGN_RIGHT);
        String verwendung = "";
        if (printBuchungsart)
        {
          verwendung = buchung.getBuchungsart().getBezeichnung();
        }
        else
        {
          verwendung = buchung.getZweck();
        }
        rpt.addColumn(verwendung, Element.ALIGN_LEFT);
        if (buchung.getVerzicht().booleanValue())
        {
          verzicht = "ja";
        }
        else
        {
          verzicht = "nein";
        }
        rpt.addColumn(verzicht, Element.ALIGN_CENTER);
        rpt.addColumn(Double.valueOf(buchung.getBetrag()));
      }

      /* Summenzeile */
      // String sumString =
      // Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
      rpt.addColumn("Gesamtsumme", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn(Double.valueOf(spb.getBetrag()));
      // rpt.addColumn(sumString, Element.ALIGN_RIGHT,
      // BaseColor.LIGHT_GRAY);

      rpt.closeTable();

      // // Etwas Abstand
      // rpt.add(new Paragraph(" "));
      // // Nun noch die Legende
      // rpt.add("Legende:", 8);
      // rpt.add(
      // "(a): Es handelt sich nicht um den Verzicht auf Erstattung von
      // Aufwendungen",
      // 8);
      // rpt.add(
      // "(b): Es handelt sich um den Verzicht auf Erstattung von Aufwendungen",
      // 8);
    }

    rpt.close();
    fos.close();
  }

  /**
   * Generierung des Standard-Dokumentes zu verwenden f�r Spendenbescheinigungen
   * ab 01.01.2014
   * 
   * @param spb
   *          Die Spendenbescheinigung aus der Datenbank
   * @param fileName
   *          Der Dateiname, wohin das Dokument geschrieben werden soll
   * @throws IOException
   * @throws DocumentException
   */
  private void generiereSpendenbescheinigungStandardAb2014(
      Spendenbescheinigung spb, String fileName, boolean adressblatt)
      throws IOException, DocumentException
  {
    final File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);

    Map<String, Object> map = spb.getMap(null);
    map = new AllgemeineMap().getMap(map);

    boolean isSammelbestaetigung = spb.isSammelbestaetigung();

    Reporter rpt = new Reporter(fos, 80, 50, 50, 50);
    rpt.addHeaderColumn(
        "Aussteller (Bezeichnung und Anschrift der steuerbeg�nstigten Einrichtung)",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);

    rpt.createHeader();

    rpt.addColumn("\n" + getAussteller() + "\n ", Element.ALIGN_LEFT);
    rpt.closeTable();

    if (isSammelbestaetigung)
    {
      rpt.add("Sammelbest�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 10);
    }
    else
    {
      rpt.add("Best�tigung �ber "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 10);
    }

    rpt.addLight(
        "im Sinne des � 10b des Einkommenssteuergesetzes an eine der in � 5 Abs. 1 Nr. 9 des K�rperschaftssteuergesetzes "
            + "bezeichneten K�rperschaften, Personenvereinigungen oder Verm�gensmassen\n",
        9);

    rpt.addHeaderColumn("Name und Anschrift des Zuwendenden",
        Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),
        Element.ALIGN_LEFT);
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.GELDSPENDE:
        rpt.addHeaderColumn("Betrag der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn("Wert der Zuwendung -in Ziffern-",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        break;
    }
    rpt.addHeaderColumn("-in Buchstaben-", Element.ALIGN_CENTER, 250,
        BaseColor.LIGHT_GRAY);
    if (!isSammelbestaetigung)
    {
      rpt.addHeaderColumn("Tag der Zuwendung", Element.ALIGN_CENTER, 50,
          BaseColor.LIGHT_GRAY);
    }
    else
    {
      rpt.addHeaderColumn("Zeitraum der Sammelbest�tigung",
          Element.ALIGN_CENTER, 75, BaseColor.LIGHT_GRAY);
    }
    rpt.createHeader();
    rpt.addColumn(
        "*" + Einstellungen.DECIMALFORMAT
            .format(map.get(SpendenbescheinigungVar.BETRAG.getName())) + "*",
        Element.ALIGN_CENTER);
    rpt.addColumn(
        (String) map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()),
        Element.ALIGN_CENTER);
    if (!isSammelbestaetigung)
    {
      rpt.addColumn(
          (String) map.get(SpendenbescheinigungVar.SPENDEDATUM.getName()),
          Element.ALIGN_CENTER);
    }
    else
    {
      rpt.addColumn(
          (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          Element.ALIGN_CENTER);
    }
    rpt.closeTable();

    switch (spb.getSpendenart())
    {
      case Spendenart.SACHSPENDE:
        rpt.addHeaderColumn(
            "Genaue Bezeichnung der Sachzuwendung mit Alter, Zustand, Kaufpreis usw.",
            Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
        rpt.createHeader();
        rpt.addColumn(spb.getBezeichnungSachzuwendung(), Element.ALIGN_LEFT);
        rpt.closeTable();
        switch (spb.getHerkunftSpende())
        {
          case HerkunftSpende.BETRIEBSVERMOEGEN:
            rpt.addLight(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsverm�gen. "
                    + "Die Zuwendung wurde mit dem Wert der Entnahme (ggf. mit dem niedrigeren gemeinen "
                    + "Wert) und nach der Umsatzsteuer, die auf die Entnahme entf�llt, bewertet.\n\n",
                9);
            break;
          case HerkunftSpende.PRIVATVERMOEGEN:
            rpt.addLight(
                "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatverm�gen.\n\n",
                9);
            break;
          case HerkunftSpende.KEINEANGABEN:
            rpt.addLight(
                "Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.\n\n",
                9);
            break;
        }
        if (spb.getUnterlagenWertermittlung())
        {
          rpt.addLight(
              "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.\n\n",
              9);
        }
    }

    /*
     * Bei Sammelbest�tigungen ist der Verweis auf Verzicht in der Anlage
     * vermerkt
     */
    String verzicht = "";
    char verzichtJa = (char) 113; // box leer
    char verzichtNein = (char) 53; // X

    if (spb.getAutocreate())
    {
      if (!isSammelbestaetigung && spb.getSpendenart() == Spendenart.GELDSPENDE)
      {
        if (spb.getBuchungen().get(0).getVerzicht().booleanValue())
        {
          verzichtJa = (char) 53; // X
          verzichtNein = (char) 113; // box leer
        }
      }
    }
    else
    {
      if (spb.getErsatzAufwendungen())
      {
        verzichtJa = (char) 53; // X
        verzichtNein = (char) 113; // box leer
      }
    }

    if (!isSammelbestaetigung)
    {
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk(
          "Es handelt sich um den Verzicht auf Erstattung von Aufwendungen: "));
      p.add(new Chunk(" Ja ", Reporter.getFreeSansBold(9)));
      p.add(new Chunk(verzichtJa,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk("   Nein ", Reporter.getFreeSansBold(9)));
      p.add(new Chunk(verzichtNein,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk("\n\n"));
      rpt.add(p);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
    }
    if (Einstellungen.getEinstellung().getVorlaeufig())
    {
      // Verein neu gegr�ndet, hat noch keinen Bescheid
      String txt = "Die Einhaltung der satzungsgem��en Voraussetzungen nach den �� 51, 59, 60 und 61 "
          + "AO wurde vom Finanzamt "
          + Einstellungen.getEinstellung().getFinanzamt() + ", StNr. "
          + Einstellungen.getEinstellung().getSteuernummer()
          + ", mit Bescheid vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " nach � 60a AO gesondert festgestellt. Wir f�rdern nach unserer Satzung "
          + Einstellungen.getEinstellung().getBeguenstigterzweck();
      rpt.addLight(txt, 8);
    }
    else
    {
      // Verein existiert und hat einen Bescheid bekommen
      String txt = "Wir sind wegen "
          + Einstellungen.getEinstellung().getBeguenstigterzweck()
          + " nach dem Freistellungsbescheid bzw. nach der Anlage zum K�rperschaftssteuerbescheid "
          + "des Finanzamtes " + Einstellungen.getEinstellung().getFinanzamt()
          + ", StNr. " + Einstellungen.getEinstellung().getSteuernummer()
          + ", vom "
          + new JVDateFormatTTMMJJJJ()
              .format(Einstellungen.getEinstellung().getBescheiddatum())
          + " f�r den letzten Veranlagungszeitraum "
          + new JVDateFormatJJJJ()
              .format(Einstellungen.getEinstellung().getVeranlagungVon())
          + " bis "
          + new JVDateFormatJJJJ()
              .format(Einstellungen.getEinstellung().getVeranlagungBis())
          + " nach � 5 Abs. 1 Nr. 9 des K�rperschaftsteuergesetzes von der K�rperschaftsteuer und nach "
          + "� 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.";
      rpt.addLight(txt, 8);
    }
    rpt.addLight("\nEs wird best�tigt, dass die Zuwendung nur zur "
        + Einstellungen.getEinstellung().getBeguenstigterzweck()
        + " verwendet wird.\n", 8);
    if (spb.getSpendenart() == Spendenart.GELDSPENDE)
    {
      char mitgliedBetraege = (char) 113; // box leer
      if (!Einstellungen.getEinstellung().getMitgliedsbetraege())
      {
        mitgliedBetraege = (char) 53; // X
      }
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk("\n"));
      p.add(new Chunk(
          "Nur f�r steuerbeg�nstigte Einrichtungen, bei denen die Mitgliedsbeitr�ge steuerlich nicht abziehbar sind:"));
      rpt.add(p);
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -18.5);
      p.setIndentationLeft((float) 18.5);
      p.add(new Chunk(mitgliedBetraege,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 10)));
      p.add(new Chunk(
          "   Es wird best�tigt, dass es sich nicht um einen Mitgliedsbeitrag handelt, dessen Abzug nach � 10b Abs. 1 des Einkommensteuergesetzes ausgeschlossen ist."));
      rpt.add(p);
    }

    if (isSammelbestaetigung)
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.addLight(
          "Es wird best�tigt, dass �ber die in der Gesamtsumme enthaltenen Zuwendungen keine weiteren Best�tigungen, weder formelle Zuwendungsbest�tigungen noch Beitragsquittungen oder �hnliches ausgestellt wurden und werden.\n",
          8);
      rpt.addLight(
          "Ob es sich um den Verzicht auf Erstattung von Aufwendungen handelt, ist der Anlage zur Sammelbest�tigung zu entnehmen.",
          8);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n\n", 8);
      rpt.add("\n", 9);

    }

    rpt.add(
        "\n\n" + Einstellungen.getEinstellung().getOrt() + ", "
            + new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
        9);

    if (Einstellungen.getEinstellung().getUnterschriftdrucken() &&
        Einstellungen.getEinstellung().getUnterschrift() != null)
    {
      rpt.add("\n", 8);
      rpt.add(Einstellungen.getEinstellung().getUnterschrift(), 400, 55, 0);
    }
    else
    {
      rpt.add("\n\n\n\n", 8);
    }
    
    rpt.add(
        ".................................................................................\nUnterschrift des Zuwendungsempf�ngers",
        8);

    rpt.add("\nHinweis:", 8);
    rpt.addLight(
        "Wer vors�tzlich oder grob fahrl�ssig eine unrichtige Zuwendungsbest�tigung erstellt "
            + "oder veranlasst, dass Zuwendungen nicht zu den in der Zuwendungsbest�tigung "
            + "angegebenen steuerbeg�nstigten Zwecken verwendet werden, haftet f�r die entgangene "
            + "Steuer (� 10b Absatz 4 EStG, � 9 Absatz 3 KStG, � 9 Nummer 5 GewStG).\n"
            + "\n"
            + "Diese Best�tigung wird nicht als Nachweis f�r die steuerliche Ber�cksichtigung der "
            + "Zuwendung anerkannt, wenn das Datum des Freistellungsbescheides l�nger als 5 Jahre "
            + "bzw. das Datum der Feststellung der Einhaltung der satzungsm��igen Voraussetzungen "
            + "nach � 60a Abs. 1 AO l�nger als 3 Jahre seit Ausstellung des Bescheides zur�ckliegt "
            + "(� 63 Abs. 5 AO).",
        7);

    /* Es sind mehrere Spenden f�r diese Spendenbescheinigung vorhanden */
    if (isSammelbestaetigung)
    {
      List<Buchung> buchungen = spb.getBuchungen();

      rpt.newPage();
      rpt.add(getAussteller(), 10);
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n", 12);
      rpt.add("Anlage zur Sammelbest�tigung vom " + (String) map
          .get(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName()), 8);
      rpt.add("f�r den Zeitraum vom "
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          8);

      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));

      /* Kopfzeile */
      rpt.addHeaderColumn("Datum der\nZuwendung", Element.ALIGN_LEFT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Art der\nZuwendung", Element.ALIGN_LEFT, 400,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Verzicht auf die\nErstattung von Aufwendungen",
          Element.ALIGN_LEFT, 300, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();

      boolean printBuchungsart = Einstellungen.getEinstellung()
          .getSpendenbescheinigungPrintBuchungsart();

      /* Buchungszeilen */
      for (Buchung buchung : buchungen)
      {
        rpt.addColumn(buchung.getDatum(), Element.ALIGN_RIGHT);
        String verwendung = "";
        if (printBuchungsart)
        {
          verwendung = buchung.getBuchungsart().getBezeichnung();
        }
        else
        {
          verwendung = buchung.getZweck();
        }
        rpt.addColumn(verwendung, Element.ALIGN_LEFT);
        if (buchung.getVerzicht().booleanValue())
        {
          verzicht = "ja";
        }
        else
        {
          verzicht = "nein";
        }
        rpt.addColumn(verzicht, Element.ALIGN_CENTER);
        rpt.addColumn(Double.valueOf(buchung.getBetrag()));
      }

      /* Summenzeile */
      // String sumString =
      // Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
      rpt.addColumn("Gesamtsumme", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn(Double.valueOf(spb.getBetrag()));
      // rpt.addColumn(sumString, Element.ALIGN_RIGHT,
      // BaseColor.LIGHT_GRAY);

      rpt.closeTable();      
    }
    
    if (adressblatt)
    {
      // Neue Seite mit Anschrift f�r Fenster in querem Brief
      rpt.newPage();
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n\n\n\n\n", 12);
      rpt.addUnderline(getAussteller(),8);
      rpt.addLight((String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),9);
    }

    rpt.close();
    fos.close();
  }

  private String getAussteller() throws RemoteException
  {
    return Einstellungen.getEinstellung().getName() + ", "
        + Einstellungen.getEinstellung().getStrasse() + ", "
        + Einstellungen.getEinstellung().getPlz() + " "
        + Einstellungen.getEinstellung().getOrt();
  }
}