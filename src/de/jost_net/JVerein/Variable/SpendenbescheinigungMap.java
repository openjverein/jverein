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

  public SpendenbescheinigungMap()
  {
    super();
  }

  public Map<String, Object> getMap(Spendenbescheinigung spb, Map<String, Object> inMap)
      throws RemoteException
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
      spb.setErsatzAufwendungen(false);
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
    String empfaenger = spb.getZeile1() + newLineStr + spb.getZeile2() + newLineStr
        + spb.getZeile3() + newLineStr + spb.getZeile4() + newLineStr + spb.getZeile5()
        + newLineStr + spb.getZeile6() + newLineStr + spb.getZeile7() + newLineStr;
    map.put(SpendenbescheinigungVar.EMPFAENGER.getName(), empfaenger);
    String anrede = (spb.getZeile1().length() > 0) ? spb.getZeile1() + " " + spb.getZeile2() : spb.getZeile2();
    map.put(SpendenbescheinigungVar.ANREDE.getName(), anrede);
    map.put(SpendenbescheinigungVar.ZEILE1.getName(), spb.getZeile1());
    map.put(SpendenbescheinigungVar.ZEILE2.getName(), spb.getZeile2());
    map.put(SpendenbescheinigungVar.ZEILE3.getName(), spb.getZeile3());
    map.put(SpendenbescheinigungVar.ZEILE4.getName(), spb.getZeile4());
    map.put(SpendenbescheinigungVar.ZEILE5.getName(), spb.getZeile5());
    map.put(SpendenbescheinigungVar.ZEILE6.getName(), spb.getZeile6());
    map.put(SpendenbescheinigungVar.ZEILE7.getName(), spb.getZeile7());
    Double dWert = spb.getBetrag();
    // Hier keinen String, sondern ein Double-Objekt in die Map stellen,
    // damit eine rechtsbündige Ausrichtung des Betrages in der Formular-
    // aufbereitung.getString() erfolgt.
    // Dies ist der Zustand vor Version 2.0
    // map.put(SpendenbescheinigungVar.BETRAG.getName(),
    // Einstellungen.DECIMALFORMAT.format(spb.getBetrag()));
    map.put(SpendenbescheinigungVar.BETRAG.getName(), dWert);
    try
    {
      String betraginworten = GermanNumber.toString(dWert.longValue());
      map.put(SpendenbescheinigungVar.BETRAGINWORTEN.getName(),
          "-" + betraginworten + "-");
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      throw new RemoteException(
          "Fehler bei der Aufbereitung des Betrages in Worten");
    }
    // Calendar für Alt/Neu
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(spb.getBescheinigungsdatum());

    String bescheinigungsdatum = new JVDateFormatTTMMJJJJ()
        .format(spb.getBescheinigungsdatum());
    map.put(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName(),
        bescheinigungsdatum);
    switch (spb.getSpendenart())
    {
      case Spendenart.GELDSPENDE:
        String art = "Geldzuwendungen";
        if ((Boolean) Einstellungen.getEinstellung(Property.MITGLIEDSBETRAEGE))
        {
          art += "/Mitgliedsbeitrag";
        }
        map.put(SpendenbescheinigungVar.SPENDEART.getName(), art);
        break;
      case Spendenart.SACHSPENDE:
        map.put(SpendenbescheinigungVar.SPENDEART.getName(), "Sachzuwendungen");
        break;
    }
    String spendedatum = new JVDateFormatTTMMJJJJ().format(spb.getSpendedatum());
    boolean printBuchungsart = (Boolean) Einstellungen.getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART);
    map.put(SpendenbescheinigungVar.BEZEICHNUNGSACHZUWENDUNG.getName(),
        spb.getBezeichnungSachzuwendung());
    map.put(SpendenbescheinigungVar.UNTERLAGENWERTERMITTUNG.getName(),
        spb.getUnterlagenWertermittlung()
            ? "Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor."
            : "");
    // Unterscheidung bis 2012 / ab 2013
    if (gc.get(GregorianCalendar.YEAR) <= 2012)
    {
      map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
          HerkunftSpende.get(spb.getHerkunftSpende()));
      map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN.getName(),
          (spb.getErsatzAufwendungen() ? "X" : ""));
    }
    else
    {
      // ab 2013
      switch (spb.getHerkunftSpende())
      {
        case HerkunftSpende.BETRIEBSVERMOEGEN:
          map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
              "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsvermögen und ist"
                  + newLineStr
                  + "mit dem Entnahmewert (ggf. mit dem niedrigeren gemeinen Wert) bewertet.");
          break;
        case HerkunftSpende.PRIVATVERMOEGEN:
          map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
              "Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatvermögen.");
          break;
        case HerkunftSpende.KEINEANGABEN:
          map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
              "Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.");
          break;
      }
      boolean ersatz = spb.getErsatzAufwendungen();
      if (spb.getAutocreate())
      {
        // Geldspende und keine Sammelbestätigung
        if (spb.getBuchungen() != null && spb.getBuchungen().size() == 1)
        {
          ersatz = spb.getBuchungen().get(0).getVerzicht().booleanValue();
        }
      }
      map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN.getName(),
          (ersatz ? "Ja" : "Nein"));
      map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN_JA.getName(),
          (ersatz ? "X" : " "));
      map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN_NEIN.getName(),
          (ersatz ? " " : "X"));
    }

    // bei Sammelbestätigungen ein Zeitraum und "siehe Anlage"
    if (spb.getBuchungen() != null && spb.getBuchungen().size() > 1)
    {
      String zeitraumende = new JVDateFormatTTMMJJJJ().format(spb.getZeitraumBis());
      map.put(SpendenbescheinigungVar.SPENDEDATUM.getName(), "s. Anlage");
      map.put(SpendenbescheinigungVar.SPENDENZEITRAUM.getName(),
          String.format("%s bis %s", spendedatum, zeitraumende));
      StringBuilder bl = new StringBuilder();
      StringBuilder bl_daten = new StringBuilder();
      StringBuilder bl_art = new StringBuilder();
      StringBuilder bl_verzicht = new StringBuilder();
      StringBuilder bl_betrag = new StringBuilder();
      if (gc.get(GregorianCalendar.YEAR) <= 2012)
      {
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
      }
      else
      {
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
          bl.append(StringTool.rpad(
              new JVDateFormatTTMMJJJJ().format(b.getDatum()), colDatumLen));
          bl_daten.append(new JVDateFormatTTMMJJJJ().format(b.getDatum()));
          bl_daten.append(newLineStr);
          bl.append("  ");
          if (printBuchungsart)
          {
            bl.append(StringTool.rpad(b.getBuchungsart().getBezeichnung(),
                colArtLen));
            bl_art.append(b.getBuchungsart().getBezeichnung());
            bl_art.append(newLineStr);
          }
          else
          {
            bl.append(StringTool.rpad(b.getZweck(), colArtLen));
          }
          bl.append("  ");
          if (b.getVerzicht().booleanValue())
          {
            bl.append(StringTool.rpad(
                StringTool.lpad("ja", colVerzichtLen / 2 - 2), colVerzichtLen));
            bl_verzicht.append("ja");
          }
          else
          {
            bl.append(
                StringTool.rpad(StringTool.lpad("nein", colVerzichtLen / 2 - 2),
                    colVerzichtLen));
            bl_verzicht.append("nein");
          }
          bl_verzicht.append(newLineStr);
          bl.append("  ");
          String str = Einstellungen.DECIMALFORMAT.format(b.getBetrag());
          bl.append(StringTool.lpad(str, colBetragLen));
          bl_betrag.append(StringTool.lpad(str, colBetragLen));
          bl_betrag.append(newLineStr);
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
        // bl.append(StringTool.rpad("-",
        // colDatumLen+2+colArtLen+2+colVerzichtLen, "-"));
        // bl.append(" ");
        // bl.append(StringTool.rpad("-", colBetragLen, "-"));
        // bl.append(newLineStr);

        bl.append(StringTool.rpad("Gesamtsumme:",
            colDatumLen + 2 + colArtLen + 2 + colVerzichtLen));
        bl.append("  ");
        String str = Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
        bl.append(StringTool.lpad(str, colBetragLen));
        bl.append(newLineStr);
      }
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE.getName(), bl.toString());
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_DATEN.getName(), bl_daten.toString());
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_ART.getName(), bl_art.toString());
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_VERZICHT.getName(), bl_verzicht.toString());
      map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_BETRAG.getName(), bl_betrag.toString());
    }
    else
    {
      map.put(SpendenbescheinigungVar.SPENDEDATUM.getName(), spendedatum);
    }

    map.put(SpendenbescheinigungVar.FINANZAMT.getName(),
        (String) Einstellungen.getEinstellung(Property.FINANZAMT));
    map.put(SpendenbescheinigungVar.STEUER_NR.getName(),
        (String) Einstellungen.getEinstellung(Property.STEUERNUMMER));
    String bescheiddatum = new JVDateFormatTTMMJJJJ()
        .format((Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM));
    map.put(SpendenbescheinigungVar.DATUM_BESCHEID.getName(), bescheiddatum);
    Calendar cal = Calendar.getInstance();
    cal.setTime((Date) Einstellungen.getEinstellung(Property.VERANLAGUNGVON));
    String start = "" + cal.get(Calendar.YEAR);
    cal.setTime((Date) Einstellungen.getEinstellung(Property.VERANLAGUNGBIS));
    map.put(SpendenbescheinigungVar.VERANLAGUNGSZEITRAUM.getName(), String
        .format("%s bis %s", start, "" + cal.get(Calendar.YEAR)));
    map.put(SpendenbescheinigungVar.ZWECK.getName(),
        (String) Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK));

    String unterschrift = (String) Einstellungen
        .getEinstellung(Property.UNTERSCHRIFT);
    if (spb.isEchteGeldspende()
        && (Boolean) Einstellungen.getEinstellung(Property.UNTERSCHRIFTDRUCKEN)
        && unterschrift != null && !unterschrift.isBlank())
    {
      Image i;
      try
      {
        i = Image.getInstance(Base64.decode(unterschrift));
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
        map.put(SpendenbescheinigungVar.UNTERSCHRIFT.getName(), i);
      }
      catch (BadElementException | IOException e)
      {
        // Dann drucken wir halt keine Unterschrift
      }
    }
    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
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

    map.put(SpendenbescheinigungVar.ANREDE.getName(), "Herr Willi Wichtig");
    map.put(SpendenbescheinigungVar.EMPFAENGER.getName(),
        "Herr\nWilli Wichtig\nBahnhofstr. 22\n12345 Testenhausen");
    map.put(SpendenbescheinigungVar.BETRAG.getName(), Double.valueOf("300.00"));
    map.put(SpendenbescheinigungVar.BETRAGINWORTEN.getName(), "dreihundert");
    map.put(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName(), "10.01.2025");
    map.put(SpendenbescheinigungVar.SPENDEART.getName(), "Geldspende");
    map.put(SpendenbescheinigungVar.SPENDEDATUM.getName(), "01.01.2025");
    map.put(SpendenbescheinigungVar.SPENDENZEITRAUM.getName(),
        "01.01.2025 bis 01.03.2025");
    map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN.getName(), "X");
    map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN_JA.getName(), "X");
    map.put(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN_NEIN.getName(),
        (char) 113);
    map.put(SpendenbescheinigungVar.BUCHUNGSLISTE.getName(), "Liste");
    map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_DATEN.getName(), "Daten");
    map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_ART.getName(), "Spende");
    map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_VERZICHT.getName(), "nein");
    map.put(SpendenbescheinigungVar.BUCHUNGSLISTE_BETRAG.getName(), "300.00");
    map.put(SpendenbescheinigungVar.BEZEICHNUNGSACHZUWENDUNG.getName(),
        "Waschmaschine");
    map.put(SpendenbescheinigungVar.HERKUNFTSACHZUWENDUNG.getName(),
        "Privatvermögen");
    map.put(SpendenbescheinigungVar.UNTERLAGENWERTERMITTUNG.getName(), "X");
    map.put(SpendenbescheinigungVar.FINANZAMT.getName(), "Testhausen");
    map.put(SpendenbescheinigungVar.STEUER_NR.getName(), "14/814/70099");
    map.put(SpendenbescheinigungVar.DATUM_BESCHEID.getName(), "01.06.2025");
    map.put(SpendenbescheinigungVar.VERANLAGUNGSZEITRAUM.getName(),
        "2022 bis 2024");
    map.put(SpendenbescheinigungVar.ZWECK.getName(), "Spende");
    map.put(SpendenbescheinigungVar.UNTERSCHRIFT.getName(), "Unterschrift");
    map.put(SpendenbescheinigungVar.ZEILE1.getName(), "Herr");
    map.put(SpendenbescheinigungVar.ZEILE2.getName(), "Willi Wichtig");
    map.put(SpendenbescheinigungVar.ZEILE3.getName(), "Bahnhofstr. 22");
    map.put(SpendenbescheinigungVar.ZEILE4.getName(), "12345 Testenhausen");
    map.put(SpendenbescheinigungVar.ZEILE5.getName(), "");
    map.put(SpendenbescheinigungVar.ZEILE6.getName(), "");
    map.put(SpendenbescheinigungVar.ZEILE7.getName(), "");

    return map;
  }
}
