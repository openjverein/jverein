/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * spb program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  spb program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with spb program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.Variable;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;
import jonelo.NumericalChameleon.SpokenNumbers.GermanNumber;

public class SpendenbescheinigungMap extends AbstractMap
{

  public Map<String, Object> getMap(Spendenbescheinigung spb,
      Map<String, Object> inMap) throws RemoteException
  {
    Map<String, Object> map = null;
    final String newLineStr = "\n";
    if (inMap == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inMap;
    }
    if (spb.getID() == null)
    {
      spb.setBescheinigungsdatum(new Date());
      spb.setBetrag(1234.56);
      spb.setBezeichnungSachzuwendung("Buch");
      spb.setHerkunftSpende(1);
      spb.setSpendedatum(new Date());
      spb.setSpendenart(Spendenart.GELDSPENDE);
      spb.setUnterlagenWertermittlung(true);
      spb.setZeile1("Herr");
      spb.setZeile2("Dr. Willi Wichtig");
      spb.setZeile3("Hinterm Bahnhof 1");
      spb.setZeile4("12345 Testenhausen");
      spb.setZeile5(null);
      spb.setZeile6(null);
      spb.setZeile7(null);
    }
    String empfaenger = spb.getZeile1() + newLineStr + spb.getZeile2()
        + newLineStr + spb.getZeile3() + newLineStr + spb.getZeile4()
        + newLineStr + spb.getZeile5() + newLineStr + spb.getZeile6()
        + newLineStr + spb.getZeile7() + newLineStr;

    boolean ersatz = false;
    // Geldspende und keine Sammelbestätigung
    if (spb.getBuchungen() != null && spb.getBuchungen().size() == 1
        && spb.getSpendenart() == Spendenart.GELDSPENDE)
    {
      ersatz = spb.getBuchungen().get(0).getVerzicht().booleanValue();
    }

    // Calendar für Alt/Neu
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(spb.getBescheinigungsdatum());

    boolean spendenbescheinigungAb2013 = gc.get(GregorianCalendar.YEAR) > 2012;

    for (SpendenbescheinigungVar var : SpendenbescheinigungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case EMPFAENGER:
          value = empfaenger;
          break;
        case ANREDE:
          value = (spb.getZeile1().length() > 0)
              ? spb.getZeile1() + " " + spb.getZeile2()
              : spb.getZeile2();
          break;
        case ZEILE1:
          value = spb.getZeile1();
          break;
        case ZEILE2:
          value = spb.getZeile2();
          break;
        case ZEILE3:
          value = spb.getZeile3();
          break;
        case ZEILE4:
          value = spb.getZeile4();
          break;
        case ZEILE5:
          value = spb.getZeile5();
          break;
        case ZEILE6:
          value = spb.getZeile6();
          break;
        case ZEILE7:
          value = spb.getZeile7();
          break;
        case BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
          break;
        case BETRAGINWORTEN:
          try
          {
            value = "-" + GermanNumber.toString(spb.getBetrag().longValue())
                + "-";
          }
          catch (Exception e)
          {
            Logger.error("Fehler", e);
            throw new RemoteException(
                "Fehler bei der Aufbereitung des Betrages in Worten");
          }
          break;
        case BESCHEINIGUNGDATUM:
          value = new JVDateFormatTTMMJJJJ()
              .format(spb.getBescheinigungsdatum());
          break;
        case BESCHEINIGUNGDATUM_F:
          value = fromDate((Date) spb.getBescheinigungsdatum());
          break;
        case SPENDEART:
          switch (spb.getSpendenart())
          {
            case Spendenart.GELDSPENDE:
              value = "Geldzuwendungen";
              if ((Boolean) Einstellungen
                  .getEinstellung(Property.MITGLIEDSBETRAEGE))
              {
                value += "/Mitgliedsbeitrag";
              }
              break;
            case Spendenart.SACHSPENDE:
              value = "Sachzuwendungen";
              break;
          }
          break;
        case BEZEICHNUNGSACHZUWENDUNG:
          value = spb.getBezeichnungSachzuwendung();
          break;
        case UNTERLAGENWERTERMITTUNG:
          value = spb.getUnterlagenWertermittlung()
              ? "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor."
              : "";
          break;
        case HERKUNFTSACHZUWENDUNG:
          // ab 2013
          switch (spb.getHerkunftSpende())
          {
            case HerkunftSpende.BETRIEBSVERMOEGEN:
              value = "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsvermögen und ist"
                  + newLineStr
                  + "mit dem Entnahmewert (ggf. mit dem niedrigeren gemeinen Wert) bewertet.";
              break;
            case HerkunftSpende.PRIVATVERMOEGEN:
              value = "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatvermögen.";
              break;
            case HerkunftSpende.KEINEANGABEN:
              value = "Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.";
              break;
          }
          break;
        case ERSATZAUFWENDUNGEN:
          value = ersatz ? "Ja" : "Nein";
          break;
        case ERSATZAUFWENDUNGEN_JA:
          value = ersatz ? "X" : " ";
          break;
        case ERSATZAUFWENDUNGEN_NEIN:
          value = ersatz ? " " : "X";
          break;
        case SPENDEDATUM:
          // bei Sammelbestätigungen ein Zeitraum und "siehe Anlage"
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1)
          {
            value = "s. Anlage";
          }
          else
          {
            value = new JVDateFormatTTMMJJJJ().format(spb.getSpendedatum());
          }
          break;
        case SPENDENZEITRAUM:
          value = String.format("%s bis %s",
              new JVDateFormatTTMMJJJJ().format(spb.getSpendedatum()),
              new JVDateFormatTTMMJJJJ().format(spb.getZeitraumBis()));
          break;

        case BUCHUNGSLISTE:
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1)
          {
            if (spendenbescheinigungAb2013)
            {
              value = getBuchungslisteAb2013(spb);
            }
            else
            {
              value = getBuchungslisteBis2012(spb);
            }
          }
          break;
        case BUCHUNGSLISTE_DATEN:
          // bei Sammelbestätigungen Daten für Buchungslite
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1
              && spendenbescheinigungAb2013)
          {
            StringBuilder bl_daten = new StringBuilder();
            for (Buchung b : spb.getBuchungen())
            {
              bl_daten.append(new JVDateFormatTTMMJJJJ().format(b.getDatum()));
              bl_daten.append(newLineStr);
            }
            value = bl_daten.toString();
          }
          break;
        case BUCHUNGSLISTE_ART:
          // bei Sammelbestätigungen Art für Buchungsliste
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1
              && spendenbescheinigungAb2013
              && (Boolean) Einstellungen.getEinstellung(
                  Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART))
          {
            StringBuilder bl_art = new StringBuilder();
            for (Buchung b : spb.getBuchungen())
            {
              bl_art.append(b.getBuchungsart().getBezeichnung());
              bl_art.append(newLineStr);
            }
            value = bl_art.toString();
          }
          break;
        case BUCHUNGSLISTE_VERZICHT:
          // bei Sammelbestätigungen Verzicht für Buchungsliste
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1
              && spendenbescheinigungAb2013)
          {
            StringBuilder bl_verzicht = new StringBuilder();
            for (Buchung b : spb.getBuchungen())
            {
              bl_verzicht.append(b.getVerzicht() ? "ja" : "nein");
              bl_verzicht.append(newLineStr);
            }
            value = bl_verzicht.toString();
          }
          break;
        case BUCHUNGSLISTE_BETRAG:
          // bei Sammelbestätigungen Betrag für Buchungsliste
          if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1
              && spendenbescheinigungAb2013)
          {
            final int colBetragLen = 11;

            StringBuilder bl_betrag = new StringBuilder();
            for (Buchung b : spb.getBuchungen())
            {
              bl_betrag.append(StringTool.lpad(
                  Einstellungen.DECIMALFORMAT.format(b.getBetrag()),
                  colBetragLen));
              bl_betrag.append(newLineStr);
            }
            value = bl_betrag.toString();
          }
          break;
        case SPENDEDATUM_ERSTES:
          value = new JVDateFormatTTMMJJJJ().format(spb.getSpendedatum());
          break;
        case SPENDEDATUM_ERSTES_F:
          value = fromDate((Date) spb.getSpendedatum());
          break;
        case FINANZAMT:
          value = (String) Einstellungen.getEinstellung(Property.FINANZAMT);
          break;
        case STEUER_NR:
          value = (String) Einstellungen.getEinstellung(Property.STEUERNUMMER);
          break;
        case DATUM_BESCHEID:
          value = new JVDateFormatTTMMJJJJ().format(
              (Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM));
          break;
        case DATUM_BESCHEID_F:
          value = fromDate(
              (Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM));
          break;
        case VERANLAGUNGSZEITRAUM:
          Calendar cal = Calendar.getInstance();
          if (!(Boolean) Einstellungen.getEinstellung(Property.VORLAEUFIG))
          {
            cal.setTime(
                (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGVON));
            String start = "" + cal.get(Calendar.YEAR);
            cal.setTime(
                (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGBIS));
            value = String.format("%s bis %s", start,
                "" + cal.get(Calendar.YEAR));
          }
          break;
        case ZWECK:
          value = (String) Einstellungen
              .getEinstellung(Property.BEGUENSTIGTERZWECK);
          break;
        case UNTERSCHRIFT:
          String unterschrift = (String) Einstellungen
              .getEinstellung(Property.UNTERSCHRIFT);
          if (spb.isEchteGeldspende()
              && (Boolean) Einstellungen
                  .getEinstellung(Property.UNTERSCHRIFTDRUCKEN)
              && unterschrift != null && !unterschrift.isBlank())
          {
            try
            {
              Image i = Image.getInstance(Base64.decode(unterschrift));
              int width = 400;
              int height = 55;
              float w = i.getWidth() / width;
              float h = i.getHeight() / height;
              if (w > h)
              {
                h = i.getHeight() / w;
                w = width;
              }
              else
              {
                w = i.getWidth() / h;
                h = height;
              }
              i.scaleToFit(w, h);
              value = i;
            }
            catch (BadElementException | IOException e)
            {
              // Dann drucken wir halt keine Unterschrift
            }
          }
          break;
      }
      map.put(var.getName(), value);
    }
    return map;

  }

  private String getBuchungslisteBis2012(Spendenbescheinigung spb)
      throws RemoteException
  {
    final String newLineStr = "\n";
    StringBuilder bl = new StringBuilder();

    boolean printBuchungsart = (Boolean) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART);

    bl.append(StringTool.rpad("Datum", 10));
    bl.append("  ");
    bl.append(StringTool.rpad(StringTool.lpad("Betrag", 8), 11));
    bl.append("  ");
    bl.append("Verwendung");
    bl.append(newLineStr);

    bl.append("----------");
    bl.append("  ");
    bl.append("-----------");
    bl.append("  ");
    bl.append("-----------------------------------------");
    bl.append(newLineStr);
    for (Buchung b : spb.getBuchungen())
    {
      bl.append(new JVDateFormatTTMMJJJJ().format(b.getDatum()));
      bl.append("  ");
      String str = Einstellungen.DECIMALFORMAT.format(b.getBetrag());
      bl.append(StringTool.lpad(str, 11));
      bl.append("  ");
      if (printBuchungsart)
      {
        bl.append(b.getBuchungsart().getBezeichnung());
      }
      else
      {
        bl.append(b.getZweck());
      }
      bl.append(" ");
      bl.append((b.getVerzicht() ? "(b)" : "(a)"));
      bl.append(newLineStr);
    }
    bl.append(newLineStr);
    bl.append("----------");
    bl.append("  ");
    bl.append("-----------");
    bl.append("  ");
    bl.append("-----------------------------------------");
    bl.append(newLineStr);
    bl.append(StringTool.rpad("Summe:", 10));
    bl.append("  ");
    String str = Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
    bl.append(StringTool.lpad(str, 11));
    bl.append(newLineStr);
    bl.append(newLineStr);
    bl.append(newLineStr);
    bl.append("Legende:");
    bl.append(newLineStr);
    bl.append(
        "(a): Es handelt sich nicht um den Verzicht auf Erstattung von Aufwendungen");
    bl.append(newLineStr);
    bl.append(
        "(b): Es handelt sich um den Verzicht auf Erstattung von Aufwendungen");
    bl.append(newLineStr);

    return bl.toString();
  }

  private String getBuchungslisteAb2013(Spendenbescheinigung spb)
      throws RemoteException
  {
    final String newLineStr = "\n";
    StringBuilder bl = new StringBuilder();

    boolean printBuchungsart = (Boolean) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART);

    final int colDatumLen = 10;
    final int colArtLen = 27;
    final int colVerzichtLen = 17;
    final int colBetragLen = 11;
    bl.append(StringTool.rpad(" ", colDatumLen));
    bl.append("  ");
    bl.append(StringTool.rpad(" ", colArtLen));
    bl.append("  ");
    bl.append(StringTool.rpad("Verzicht auf", colVerzichtLen));
    bl.append("  ");
    bl.append(StringTool.rpad(" ", colBetragLen));
    bl.append(newLineStr);

    bl.append(StringTool.rpad("Datum der ", colDatumLen));
    bl.append("  ");
    bl.append(StringTool.rpad("Art der", colArtLen));
    bl.append("  ");
    bl.append(StringTool.rpad("die Erstattung", colVerzichtLen));
    bl.append("  ");
    bl.append(StringTool.rpad(" ", colBetragLen));
    bl.append(newLineStr);

    bl.append(StringTool.rpad("Zuwendung", colDatumLen));
    bl.append("  ");
    bl.append(StringTool.rpad("Zuwendung", colArtLen));
    bl.append("  ");
    bl.append(StringTool.rpad("von Aufwendungen", colVerzichtLen));
    bl.append("  ");
    bl.append(StringTool.rpad(StringTool.lpad("Betrag", 8), colBetragLen));
    bl.append(newLineStr);

    bl.append(StringTool.rpad("-", colDatumLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colArtLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colVerzichtLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colBetragLen, "-"));
    bl.append(newLineStr);

    for (Buchung b : spb.getBuchungen())
    {
      bl.append(StringTool.rpad(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
          colDatumLen));
      bl.append("  ");
      if (printBuchungsart)
      {
        bl.append(
            StringTool.rpad(b.getBuchungsart().getBezeichnung(), colArtLen));
      }
      else
      {
        bl.append(StringTool.rpad(b.getZweck(), colArtLen));
      }
      bl.append("  ");
      if (b.getVerzicht().booleanValue())
      {
        bl.append(StringTool.rpad(StringTool.lpad("ja", colVerzichtLen / 2 - 2),
            colVerzichtLen));
      }
      else
      {
        bl.append(StringTool.rpad(
            StringTool.lpad("nein", colVerzichtLen / 2 - 2), colVerzichtLen));
      }
      bl.append("  ");
      String str = Einstellungen.DECIMALFORMAT.format(b.getBetrag());
      bl.append(StringTool.lpad(str, colBetragLen));
      bl.append(newLineStr);
    }

    bl.append(StringTool.rpad("-", colDatumLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colArtLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colVerzichtLen, "-"));
    bl.append("  ");
    bl.append(StringTool.rpad("-", colBetragLen, "-"));
    bl.append(newLineStr);

    bl.append(StringTool.rpad("Gesamtsumme:",
        colDatumLen + 2 + colArtLen + 2 + colVerzichtLen));
    bl.append("  ");
    String str = Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
    bl.append(StringTool.lpad(str, colBetragLen));
    bl.append(newLineStr);

    return bl.toString();
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inMap == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inMap;
    }

    for (SpendenbescheinigungVar var : SpendenbescheinigungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ANREDE:
          value = "Herr Willi Wichtig";
          break;
        case EMPFAENGER:
          value = "Herr\nWilli Wichtig\nBahnhofstr. 22\n12345 Testenhausen";
          break;
        case BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(300);
          break;
        case BETRAGINWORTEN:
          value = "dreihundert";
          break;
        case BESCHEINIGUNGDATUM:
          value = "10.01.2025";
          break;
        case BESCHEINIGUNGDATUM_F:
          value = "20251001";
          break;
        case SPENDEART:
          value = "Geldspende";
          break;
        case SPENDEDATUM:
          value = "s. Anlage";
          break;
        case SPENDEDATUM_ERSTES:
          value = "01.01.2025";
          break;
        case SPENDEDATUM_ERSTES_F:
          value = "20250101";
          break;
        case SPENDENZEITRAUM:
          value = "01.01.2025 bis 01.03.2025";
          break;
        case ERSATZAUFWENDUNGEN:
          value = "Nein";
          break;
        case ERSATZAUFWENDUNGEN_JA:
          value = "X";
          break;
        case ERSATZAUFWENDUNGEN_NEIN:
          value = "X";
          break;
        case BUCHUNGSLISTE:
          value = "Liste";
          break;
        case BUCHUNGSLISTE_DATEN:
          value = "Daten";
          break;
        case BUCHUNGSLISTE_ART:
          value = "Spende";
          break;
        case BUCHUNGSLISTE_VERZICHT:
          value = "nein";
          break;
        case BUCHUNGSLISTE_BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(300.00);
          break;
        case BEZEICHNUNGSACHZUWENDUNG:
          value = "Waschmaschine";
          break;
        case HERKUNFTSACHZUWENDUNG:
          value = "Privatvermögen";
          break;
        case UNTERLAGENWERTERMITTUNG:
          value = "X";
          break;
        case FINANZAMT:
          value = "Testhausen";
          break;
        case STEUER_NR:
          value = "14/814/70099";
          break;
        case DATUM_BESCHEID:
          value = "01.06.2025";
          break;
        case DATUM_BESCHEID_F:
          value = "20250601";
          break;
        case VERANLAGUNGSZEITRAUM:
          if (!((Boolean) Einstellungen.getEinstellung(Property.VORLAEUFIG)))
          {
            value = "2022 bis 2024";
          }
        case ZWECK:
          value = "Spende";
          break;
        case ZEILE1:
          value = "Herr";
          break;
        case ZEILE2:
          value = "Willi Wichtig";
          break;
        case ZEILE3:
          value = "Bahnhofstr. 22";
          break;
        case ZEILE4:
          value = "12345 Testenhausen";
          break;
        case ZEILE5:
          value = "";
          break;
        case ZEILE6:
          value = "";
          break;
        case ZEILE7:
          value = "";
          break;
        case UNTERSCHRIFT:
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
