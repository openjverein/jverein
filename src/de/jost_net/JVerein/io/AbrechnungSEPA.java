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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.GV.generators.ISEPAGenerator;
import org.kapott.hbci.GV.generators.SEPAGeneratorFactory;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragAbrechnungslauf;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
import de.jost_net.JVerein.util.VorlageUtil;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.jost_net.OBanToo.SEPA.Basislastschrift.Basislastschrift;
import de.jost_net.OBanToo.SEPA.Basislastschrift.Basislastschrift2Pdf;
import de.jost_net.OBanToo.SEPA.Basislastschrift.MandatSequence;
import de.jost_net.OBanToo.SEPA.Basislastschrift.Zahler;
import de.jost_net.OBanToo.StringLatin.Zeichen;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.io.SepaLastschriftMerger;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AbrechnungSEPA
{
  private int counter = 0;

  private BackgroundTask interrupt;

  private HashMap<String, ArrayList<JVereinZahler>> zahlermap = new HashMap<>();

  public AbrechnungSEPA(AbrechnungSEPAParam param, ProgressMonitor monitor,
      BackgroundTask backgroundTask) throws Exception
  {
    interrupt = backgroundTask;

    Abrechnungslauf abrl = getAbrechnungslauf(param);

    Konto konto = getKonto();
    ArrayList<JVereinZahler> zahlerarray = new ArrayList<>();

    // Mitglieder abrechnen und zahlerMap füllen
    abrechnenMitglieder(param, monitor);

    if (param.zusatzbetraege)
    {
      // Zusatzbetraege abrechnen und zahlerMap füllen
      abbuchenZusatzbetraege(param, abrl, monitor);
    }

    if (param.kursteilnehmer)
    {
      // Kursteilnehmer direkt in zahlerarray da es für jeden nur eine
      // Lastschrift geben kann
      zahlerarray = abbuchenKursteilnehmer(param, abrl, konto, monitor);
    }

    Iterator<Entry<String, ArrayList<JVereinZahler>>> iterator = zahlermap
        .entrySet().iterator();

    monitor.setStatusText("Sollbuchungen erstellen");
    double count = 0;
    while (iterator.hasNext())
    {
      monitor.setPercentComplete(
          (int) (count++ / (double) zahlermap.size() * 100d));
      if (interrupt.isInterrupted())
      {
        throw new ApplicationException("Abrechnung abgebrochen");
      }
      ArrayList<JVereinZahler> zahlerList = iterator.next().getValue();

      // Nach Mitglied und Betrag sortieren damit auch erstattungen
      // funktionieren und bei Vollzahler erst die Positionen des Vollzahler
      // eingetragen werden
      zahlerList.sort((z1, z2) -> {
        try
        {
          if (z1.getMitglied().equals(z2.getMitglied()))
            return z2.getBetrag().compareTo(z1.getBetrag());
          else
            return z1.getPersonId().equals(z1.getMitglied().getID()) ? -1 : 1;
        }
        catch (SEPAException | RemoteException e)
        {
          return 0;
        }
      });

      ArrayList<String> verwendungszwecke = new ArrayList<>();
      if (!param.sollbuchungenzusammenfassen)
      {
        for (JVereinZahler zahler : zahlerList)
        {
          // Für jede Buchung eine Sollbuchung mit einer Sollbuchungsposition.
          ArrayList<SollbuchungPosition> spArray = new ArrayList<>();
          spArray.add(getSollbuchungPosition(zahler));

          verwendungszwecke
              .add(writeSollbuchung(Long.parseLong(zahler.getPersonId()),
                  zahler.getZahlungsweg().getKey(), zahler.getMitglied(),
                  spArray, param.faelligkeit, abrl, konto, param, null));

          // Ohne kompakte Abbuchung zahlerarray direkt füllen
          if (!param.kompakteabbuchung && zahler.getZahlungsweg()
              .getKey() == Zahlungsweg.BASISLASTSCHRIFT)
          {
            zahlerarray.add(zahler);
          }
        }
      }
      else
      {
        // Pro Zahlungsweg und Mitglied eine Sollbuchung
        HashMap<String, ArrayList<SollbuchungPosition>> spMap = new HashMap<>();
        HashMap<String, Mitglied> mitgliedMap = new HashMap<>();
        HashMap<String, String> zahlerIdMap = new HashMap<>();
        for (JVereinZahler zahler : zahlerList)
        {
          mitgliedMap.put(zahler.getMitglied().getID(), zahler.getMitglied());
          zahlerIdMap.put(zahler.getMitglied().getID(), zahler.getPersonId());

          String key = zahler.getZahlungsweg().getKey()
              + zahler.getMitglied().getID();
          ArrayList<SollbuchungPosition> spArray = spMap.getOrDefault(key,
              new ArrayList<>());

          spArray.add(getSollbuchungPosition(zahler));
          spMap.put(key, spArray);
        }

        for (Entry<String, ArrayList<SollbuchungPosition>> entry : spMap
            .entrySet())
        {
          // Zahlungsweg und Mitglied holen wir aus derm Key
          // (ZahlungswegID MitgliedID)
          String mapKey = entry.getKey().substring(1);
          verwendungszwecke
              .add(writeSollbuchung(Long.parseLong(zahlerIdMap.get(mapKey)),
                  Integer.parseInt(entry.getKey().substring(0, 1)),
                  mitgliedMap.get(mapKey), entry.getValue(), param.faelligkeit,
                  abrl, konto, param, null));
        }
      }

      // Bei kompakter Abbuchung Zahler zusammenfassen.
      if (param.kompakteabbuchung || param.sollbuchungenzusammenfassen)
      {
        JVereinZahler gesamtZahler = null;
        for (JVereinZahler zahler : zahlerList)
        {
          if (zahler.getZahlungsweg().getKey() == Zahlungsweg.BASISLASTSCHRIFT)
          {
            if (!zahler.getMitglied().getID().equals(zahler.getPersonId()))
            {
              zahler.setVerwendungszweck(zahler.getVerwendungszweck() + " "
                  + zahler.getMitglied().getVorname());
            }
            if (gesamtZahler == null)
            {
              gesamtZahler = zahler;
            }
            else
            {
              try
              {
                gesamtZahler.add(zahler);
              }
              catch (SEPAException se)
              {
                throw new ApplicationException(
                    "Ungültiger Betrag: " + zahler.getBetrag());
              }
            }
          }
        }
        if (gesamtZahler != null)
        {
          if (param.rechnung && verwendungszwecke.size() > 0)
          {
            gesamtZahler
                .setVerwendungszweck(String.join(", ", verwendungszwecke));
          }
          zahlerarray.add(gesamtZahler);
        }
      }
    }

    if (zahlerarray.size() > 0)
    {
      monitor.setStatusText("Lastschriften erstellen");

      if (Einstellungen.getEinstellung(Property.NAME) == null
          || ((String) Einstellungen.getEinstellung(Property.NAME))
              .length() == 0
          || Einstellungen.getEinstellung(Property.IBAN) == null
          || ((String) Einstellungen.getEinstellung(Property.IBAN))
              .length() == 0
          || Einstellungen.getEinstellung(Property.BIC) == null
          || ((String) Einstellungen.getEinstellung(Property.BIC))
              .length() == 0)
      {
        throw new ApplicationException(
            "Name des Vereins oder Bankverbindung fehlt. Bitte unter "
                + "Administration|Einstellungen|Allgemein erfassen.");
      }

      if (Einstellungen.getEinstellung(Property.GLAEUBIGERID) == null
          || ((String) Einstellungen.getEinstellung(Property.GLAEUBIGERID))
              .length() == 0)
      {
        throw new ApplicationException(
            "Gläubiger-ID fehlt. Gfls. unter https://extranet.bundesbank.de/scp/ oder"
                + " http://www.oenb.at/idakilz/cid?lang=de beantragen und unter"
                + " Administration|Einstellungen|Allgemein eintragen.\n"
                + "Zu Testzwecken kann DE98ZZZ09999999999 eingesetzt werden.");
      }

      if (param.faelligkeit.before(new Date()))
      {
        throw new ApplicationException(
            "Fälligkeit muss bei Lastschriften in der Zukunft liegen");
      }

      Basislastschrift lastschrift = new Basislastschrift();
      // Vorbereitung: Allgemeine Informationen einstellen
      lastschrift.setBIC((String) Einstellungen.getEinstellung(Property.BIC));
      lastschrift.setGlaeubigerID(
          (String) Einstellungen.getEinstellung(Property.GLAEUBIGERID));
      lastschrift.setIBAN((String) Einstellungen.getEinstellung(Property.IBAN));
      lastschrift.setKomprimiert(param.kompakteabbuchung);
      lastschrift.setName(Zeichen
          .convert((String) Einstellungen.getEinstellung(Property.NAME)));
      lastschrift.setMessageID(abrl.getID() + "-RCUR");

      count = 0;
      BigDecimal summelastschriften = BigDecimal.valueOf(0);
      for (JVereinZahler zahler : zahlerarray)
      {
        monitor.setPercentComplete(
            (int) (count++ / (double) zahlerarray.size() * 100d));
        summelastschriften = summelastschriften.add(zahler.getBetrag());
        Lastschrift ls = getLastschrift(zahler, abrl, param.kompakteabbuchung);
        ls.store();
      }

      // Gegenbuchung für die Sollbuchungen schreiben
      if (!summelastschriften.equals(BigDecimal.valueOf(0)))
      {
        writeSollbuchung(null, Zahlungsweg.BASISLASTSCHRIFT, null, null,
            param.faelligkeit, abrl, konto, param,
            -summelastschriften.doubleValue());
      }

      if (param.abbuchungsausgabe == Abrechnungsausgabe.SEPA_DATEI)
      {
        writeSepaFile(param, lastschrift, zahlerarray);
        monitor.log(String.format("SEPA-Datei %s geschrieben.",
            param.sepafileRCUR.getAbsolutePath()));
        param.setText(String.format(", SEPA-Datei %s geschrieben.",
            param.sepafileRCUR.getAbsolutePath()));
      }

      if (param.abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
      {
        buchenHibiscus(param, zahlerarray);
        monitor.log("Hibiscus-Lastschrift erzeugt.");
        param.setText(String.format(", Hibiscus-Lastschrift erzeugt."));
      }

      if (param.pdffileRCUR != null)
      {
        // Nur für die PDF-Erzeugung müssen die Zahler in der Lastschrift
        // enthalten sein
        for (JVereinZahler z : zahlerarray)
        {
          lastschrift.add(z);
        }
        // Das für die
        // PDF-Erzeugung benötigte Datum wird erst in write gesetzt
        File temp_file = Files.createTempFile("jv", ".xml").toFile();
        lastschrift.write(temp_file);
        temp_file.delete();

        ausdruckenSEPA(lastschrift, param.pdffileRCUR);
      }
    }
    monitor.setStatusText(counter + " abgerechnete Fälle");
  }

  private void abrechnenMitglieder(AbrechnungSEPAParam param,
      ProgressMonitor monitor) throws Exception
  {
    if (param.abbuchungsmodus != Abrechnungsmodi.KEINBEITRAG)
    {
      // Alle Mitglieder lesen
      DBIterator<Mitglied> list = Einstellungen.getDBService()
          .createList(Mitglied.class);
      MitgliedUtils.setMitglied(list);

      // Das Mitglied muss bereits eingetreten sein
      list.addFilter("(eintritt <= ? or eintritt is null) ",
          new java.sql.Date(param.stichtag.getTime()));
      // Das Mitglied darf noch nicht ausgetreten sein
      list.addFilter("(austritt is null or austritt > ?)",
          new java.sql.Date(param.stichtag.getTime()));
      // Bei Abbuchungen im Laufe des Jahres werden nur die Mitglieder
      // berücksichtigt, die bis zu einem bestimmten Zeitpunkt ausgetreten sind.
      if (param.bisdatum != null)
      {
        list.addFilter("(austritt <= ?)",
            new java.sql.Date(param.bisdatum.getTime()));
      }
      // Bei Abbuchungen im Laufe des Jahres werden nur die Mitglieder
      // berücksichtigt, die ab einem bestimmten Zeitpunkt eingetreten sind.
      if (param.vondatum != null)
      {
        list.addFilter("eintritt >= ?",
            new java.sql.Date(param.vondatum.getTime()));
      }
      if (param.voneingabedatum != null)
      {
        list.addFilter("eingabedatum >= ?",
            new java.sql.Date(param.voneingabedatum.getTime()));
      }
      if ((Integer) Einstellungen.getEinstellung(
          Property.BEITRAGSMODEL) == Beitragsmodel.MONATLICH12631.getKey())
      {
        if (param.abbuchungsmodus == Abrechnungsmodi.HAVIMO)
        {
          list.addFilter(
              "(zahlungsrhytmus = ? or zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              Integer.valueOf(Zahlungsrhythmus.HALBJAEHRLICH),
              Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
              Integer.valueOf(Zahlungsrhythmus.MONATLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.JAVIMO)
        {
          list.addFilter(
              "(zahlungsrhytmus = ? or zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              Integer.valueOf(Zahlungsrhythmus.JAEHRLICH),
              Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
              Integer.valueOf(Zahlungsrhythmus.MONATLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.VIMO)
        {
          list.addFilter("(zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
              Integer.valueOf(Zahlungsrhythmus.MONATLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.MO)
        {
          list.addFilter("zahlungsrhytmus = ?",
              Integer.valueOf(Zahlungsrhythmus.MONATLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.VI)
        {
          list.addFilter("zahlungsrhytmus = ?",
              Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.HA)
        {
          list.addFilter("zahlungsrhytmus = ?",
              Integer.valueOf(Zahlungsrhythmus.HALBJAEHRLICH));
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.JA)
        {
          list.addFilter("zahlungsrhytmus = ?",
              Integer.valueOf(Zahlungsrhythmus.JAEHRLICH));
        }
      }

      list.setOrder("ORDER BY zahlungsweg, name, vorname");

      // Sätze im Resultset
      int count = 0;
      while (list.hasNext())
      {
        if (interrupt.isInterrupted())
        {
          throw new ApplicationException("Abrechnung abgebrochen");
        }
        Mitglied m = list.next();

        JVereinZahler zahler = abrechnungMitgliederSub(param, monitor, m,
            m.getBeitragsgruppe(), true);

        if (zahler != null)
        {
          ArrayList<JVereinZahler> zlist = zahlermap
              .get(zahler.getPersonTyp() + zahler.getPersonId());
          if (zlist == null)
          {
            zlist = new ArrayList<>();
            zlist.add(zahler);
            zahlermap.put(zahler.getPersonTyp() + zahler.getPersonId(), zlist);
          }
          else
          {
            zlist.add(zahler);
            zahlermap.replace(zahler.getPersonTyp() + zahler.getPersonId(),
                zlist);
          }
        }

        DBIterator<SekundaereBeitragsgruppe> sekundaer = Einstellungen
            .getDBService().createList(SekundaereBeitragsgruppe.class);
        sekundaer.addFilter("mitglied=?", m.getID());
        while (sekundaer.hasNext())
        {
          SekundaereBeitragsgruppe sb = sekundaer.next();
          JVereinZahler zahlerSekundaer = abrechnungMitgliederSub(param,
              monitor, m, sb.getBeitragsgruppe(), false);
          if (zahlerSekundaer != null)
          {
            ArrayList<JVereinZahler> zlist = zahlermap.get(
                zahlerSekundaer.getPersonTyp() + zahlerSekundaer.getPersonId());
            if (zlist == null)
            {
              zlist = new ArrayList<>();
              zlist.add(zahlerSekundaer);
              zahlermap.put(zahlerSekundaer.getPersonTyp()
                  + zahlerSekundaer.getPersonId(), zlist);
            }
            else
            {
              zlist.add(zahlerSekundaer);
              zahlermap.replace(zahlerSekundaer.getPersonTyp()
                  + zahlerSekundaer.getPersonId(), zlist);
            }
          }
        }

        monitor.setPercentComplete(
            (int) ((double) count++ / (double) list.size() * 100d));
        monitor.setStatusText(
            String.format("%s, %s abgerechnet", m.getName(), m.getVorname()));
      }
    }
  }

  private JVereinZahler abrechnungMitgliederSub(AbrechnungSEPAParam param,
      ProgressMonitor monitor, Mitglied m, Beitragsgruppe bg, boolean primaer)
      throws RemoteException, ApplicationException
  {
    Double betr = 0d;
    JVereinZahler zahler = null;
    Mitglied mZahler = m.getZahler();
    if (((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
        && (mZahler.getZahlungstermin() != null && !mZahler.getZahlungstermin()
            .isAbzurechnen(param.abrechnungsmonat)))
    {
      return null;
    }

    try
    {
      betr = BeitragsUtil.getBeitrag(
          Beitragsmodel.getByKey(
              (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)),
          mZahler.getZahlungstermin(), mZahler.getZahlungsrhythmus(), bg,
          param.stichtag, m);
    }
    catch (NullPointerException e)
    {
      throw new ApplicationException(
          "Zahlungsinformationen bei " + Adressaufbereitung.getNameVorname(m));
    }
    if (primaer && ((Boolean) Einstellungen
        .getEinstellung(Property.INDIVIDUELLEBEITRAEGE)
        && m.getIndividuellerBeitrag() != null))
    {
      betr = m.getIndividuellerBeitrag();
    }
    if (betr == 0d)
    {
      return null;
    }
    checkSEPA(param, mZahler, monitor);

    counter++;

    try
    {
      zahler = new JVereinZahler();
      zahler.setPersonId(mZahler.getID());
      zahler.setPersonTyp(JVereinZahlerTyp.MITGLIED);
      zahler.setBetrag(
          BigDecimal.valueOf(betr).setScale(2, RoundingMode.HALF_UP));
      if (mZahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
      {
        IBAN i = new IBAN(mZahler.getIban()); // Prüfung der IBAN
        zahler.setIban(mZahler.getIban());
        // Wenn BIC nicht vorhanden versuchen sie automatisch zu ermitteln
        if (mZahler.getBic() == null || mZahler.getBic().length() == 0)
        {
          zahler.setBic(i.getBIC());
        }
        else
        {
          zahler.setBic(mZahler.getBic());
        }
        new BIC(zahler.getBic()); // Prüfung des BIC
        zahler.setMandatid(mZahler.getMandatID());
        zahler.setMandatdatum(mZahler.getMandatDatum());
        zahler.setMandatsequence(MandatSequence.RCUR);
      }
      zahler.setFaelligkeit(param.faelligkeit);
      zahler.setZahlungsweg(new Zahlungsweg(mZahler.getZahlungsweg()));
      if (bg.getBuchungsart() != null)
      {
        zahler.setBuchungsartId(bg.getBuchungsartId());
      }
      if (bg.getBuchungsklasseId() != null)
      {
        zahler.setBuchungsklasseId(bg.getBuchungsklasseId());
      }
      zahler.setDatum(param.faelligkeit);
      zahler.setMitglied(m);
      if (primaer)
      {
        String vzweck = getVerwendungszweck(param);
        boolean ohneLesefelder = !vzweck.contains(Einstellungen.LESEFELD_PRE);
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap(m, map, ohneLesefelder);
        map = new AbrechnungsParameterMap().getMap(param, map);
        try
        {
          vzweck = VelocityTool.eval(map, vzweck);
          if (vzweck.length() >= 140)
          {
            vzweck = vzweck.substring(0, 136) + "...";
          }
        }
        catch (IOException e)
        {
          Logger.error("Fehler bei der Aufbereitung der Variablen", e);
        }
        zahler.setVerwendungszweck(vzweck);
      }
      else
      {
        zahler.setVerwendungszweck(bg.getBezeichnung());
      }
      zahler
          .setName(mZahler.getKontoinhaber(Mitglied.namenformat.KONTOINHABER));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        zahler.setSteuer(bg.getSteuer());
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          Adressaufbereitung.getNameVorname(m) + ": " + e.getMessage());
    }

    return zahler;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void abbuchenZusatzbetraege(AbrechnungSEPAParam param,
      Abrechnungslauf abrl, ProgressMonitor monitor) throws Exception
  {
    int count = 0;
    DBIterator<Zusatzbetrag> list = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    // etwas vorfiltern um die Ergebnise zu reduzieren
    list.addFilter("(intervall != 0 or ausfuehrung is null)");
    list.addFilter("(endedatum is null or endedatum >= ?)", param.stichtag);
    while (list.hasNext())
    {
      if (interrupt.isInterrupted())
      {
        throw new ApplicationException("Abrechnung abgebrochen");
      }
      Zusatzbetrag z = list.next();
      if (z.isAktiv(param.stichtag))
      {
        Mitglied m = z.getMitglied();
        Mitglied mZahler;
        if (z.getMitgliedzahltSelbst())
        {
          mZahler = m;
        }
        else
        {
          mZahler = m.getZahler();
        }
        Integer zahlungsweg;
        if (z.getZahlungsweg() != null
            && z.getZahlungsweg().getKey() != Zahlungsweg.STANDARD)
        {
          zahlungsweg = z.getZahlungsweg().getKey();
        }
        else
        {
          zahlungsweg = mZahler.getZahlungsweg();
        }

        if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT)
        {
          checkSEPA(param, mZahler, monitor);
        }

        counter++;
        String vzweck = z.getBuchungstext();
        boolean ohneLesefelder = !vzweck.contains(Einstellungen.LESEFELD_PRE);
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap(m, map, ohneLesefelder);
        map = new AbrechnungsParameterMap().getMap(param, map);
        try
        {
          vzweck = VelocityTool.eval(map, vzweck);
        }
        catch (IOException e)
        {
          Logger.error("Fehler bei der Aufbereitung der Variablen", e);
        }

        try
        {
          JVereinZahler zahler = new JVereinZahler();
          zahler.setPersonId(mZahler.getID());
          zahler.setPersonTyp(JVereinZahlerTyp.MITGLIED);
          zahler.setBetrag(BigDecimal.valueOf(z.getBetrag()).setScale(2,
              RoundingMode.HALF_UP));
          if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT)
          {
            new BIC(mZahler.getBic());
            new IBAN(mZahler.getIban());
            zahler.setBic(mZahler.getBic());
            zahler.setIban(mZahler.getIban());
            zahler.setMandatid(mZahler.getMandatID());
            zahler.setMandatdatum(mZahler.getMandatDatum());
            zahler.setMandatsequence(MandatSequence.RCUR);
          }
          zahler.setFaelligkeit(param.faelligkeit);
          zahler.setName(
              mZahler.getKontoinhaber(Mitglied.namenformat.KONTOINHABER));
          zahler.setVerwendungszweck(vzweck);
          zahler.setZahlungsweg(new Zahlungsweg(zahlungsweg));
          if (z.getBuchungsart() != null)
          {
            zahler.setBuchungsartId(z.getBuchungsartId());
          }
          if (z.getBuchungsklasseId() != null)
          {
            zahler.setBuchungsklasseId(z.getBuchungsklasseId());
          }
          zahler.setDatum(z.getFaelligkeit());
          zahler.setMitglied(m);

          if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
          {
            zahler.setSteuer(z.getSteuer());
          }

          ArrayList<JVereinZahler> zlist = zahlermap
              .get(zahler.getPersonTyp() + zahler.getPersonId());
          if (zlist == null)
          {
            zlist = new ArrayList();
            zlist.add(zahler);
            zahlermap.put(zahler.getPersonTyp() + zahler.getPersonId(), zlist);
          }
          else
          {
            zlist.add(zahler);
            zahlermap.replace(zahler.getPersonTyp() + zahler.getPersonId(),
                zlist);
          }
        }
        catch (Exception e)
        {
          throw new ApplicationException(
              Adressaufbereitung.getNameVorname(m) + ": " + e.getMessage());
        }

        if (z.getIntervall().intValue() != IntervallZusatzzahlung.KEIN
            && (z.getEndedatum() == null
                || z.getFaelligkeit().getTime() <= z.getEndedatum().getTime()))
        {
          z.setFaelligkeit(
              Datum.addInterval(z.getFaelligkeit(), z.getIntervall()));
        }
        try
        {
          if (abrl != null)
          {
            ZusatzbetragAbrechnungslauf za = (ZusatzbetragAbrechnungslauf) Einstellungen
                .getDBService()
                .createObject(ZusatzbetragAbrechnungslauf.class, null);
            za.setAbrechnungslauf(abrl);
            za.setZusatzbetrag(z);
            za.setLetzteAusfuehrung(z.getAusfuehrung());
            za.store();
            z.setAusfuehrung(param.stichtag);
            z.store();
          }
        }
        catch (ApplicationException e)
        {
          String debString = z.getStartdatum() + ", " + z.getEndedatum() + ", "
              + z.getIntervallText() + ", " + z.getBuchungstext() + ", "
              + z.getBetrag();
          Logger.error(Adressaufbereitung.getNameVorname(z.getMitglied()) + " "
              + debString, e);
          monitor.log(z.getMitglied().getName() + " " + debString + " " + e);
          throw e;
        }
        monitor
            .setStatusText(String.format("Zusatzbetrag von %s, %s abgerechnet",
                m.getName(), m.getVorname()));
      }
      monitor.setPercentComplete(
          (int) ((double) count++ / (double) list.size() * 100d));
    }

  }

  private ArrayList<JVereinZahler> abbuchenKursteilnehmer(
      AbrechnungSEPAParam param, Abrechnungslauf abrl, Konto konto,
      ProgressMonitor monitor) throws Exception
  {
    ArrayList<JVereinZahler> zahlerarray = new ArrayList<>();
    int count = 0;
    DBIterator<Kursteilnehmer> list = Einstellungen.getDBService()
        .createList(Kursteilnehmer.class);
    list.addFilter("abbudatum is null");
    while (list.hasNext())
    {
      if (interrupt.isInterrupted())
      {
        throw new ApplicationException("Abrechnung abgebrochen");
      }

      counter++;
      Kursteilnehmer kt = list.next();
      try
      {
        JVereinZahler zahler = new JVereinZahler();
        zahler.setPersonId(kt.getID());
        zahler.setPersonTyp(JVereinZahlerTyp.KURSTEILNEHMER);
        zahler.setBetrag(BigDecimal.valueOf(kt.getBetrag()).setScale(2,
            RoundingMode.HALF_UP));
        new BIC(kt.getBic());
        new IBAN(kt.getIban());
        zahler.setBic(kt.getBic());
        zahler.setIban(kt.getIban());
        zahler.setMandatid(kt.getMandatID());
        zahler.setMandatdatum(kt.getMandatDatum());
        zahler.setMandatsequence(MandatSequence.RCUR);
        zahler.setFaelligkeit(param.faelligkeit);
        zahler.setName(kt.getName());
        zahler
            .setVerwendungszweck(getVerwendungszweckName(kt, kt.getVZweck1()));
        zahler.setZahlungsweg(new Zahlungsweg(Zahlungsweg.BASISLASTSCHRIFT));
        zahler.setDatum(param.faelligkeit);
        kt.setAbbudatum(param.faelligkeit);
        kt.store();

        ArrayList<SollbuchungPosition> spArray = new ArrayList<>();
        spArray.add(getSollbuchungPosition(zahler));
        String zweck = writeSollbuchung(null, Zahlungsweg.BASISLASTSCHRIFT, kt,
            spArray, param.faelligkeit, abrl, konto, param, null);
        zahler.setVerwendungszweck(zweck);
        zahlerarray.add(zahler);

        monitor.setStatusText(String.format("Kursteilnehmer %s, %s abgerechnet",
            kt.getName(), kt.getVorname()));
        monitor.setPercentComplete(
            (int) ((double) count++ / (double) list.size() * 100d));

      }
      catch (Exception e)
      {
        throw new ApplicationException(kt.getName() + ": " + e.getMessage());
      }
    }
    return zahlerarray;
  }

  private void ausdruckenSEPA(final Basislastschrift lastschrift,
      final String pdf_fn) throws IOException, DocumentException, SEPAException
  {
    new Basislastschrift2Pdf(lastschrift, pdf_fn);
    GUI.getDisplay().asyncExec(() -> {
      try
      {
        new Program().handleAction(new File(pdf_fn));
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(
            ae.getLocalizedMessage(), StatusBarMessage.TYPE_ERROR));
      }
    });
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void writeSepaFile(AbrechnungSEPAParam param,
      Basislastschrift lastschrift, ArrayList<JVereinZahler> zahlerarray)
      throws Exception
  {
    Properties ls_properties = new Properties();
    ls_properties.setProperty("src.bic", lastschrift.getBIC());
    ls_properties.setProperty("src.iban", lastschrift.getIBAN());
    ls_properties.setProperty("src.name", lastschrift.getName());
    long epochtime = Calendar.getInstance().getTimeInMillis();
    String epochtime_string = Long.toString(epochtime);
    DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);
    ls_properties.setProperty("sepaid", epochtime_string);
    ls_properties.setProperty("pmtinfid", epochtime_string);
    ls_properties.setProperty("sequencetype", "RCUR");
    ls_properties.setProperty("targetdate",
        param.faelligkeit != null ? ISO_DATE.format(param.faelligkeit)
            : SepaUtil.DATE_UNDEFINED);
    ls_properties.setProperty("type", "CORE");
    ls_properties.setProperty("batchbook", "");
    int counter = 0;
    String creditorid = lastschrift.getGlaeubigerID();
    for (Zahler zahler : zahlerarray)
    {
      ls_properties.setProperty(SepaUtil.insertIndex("dst.bic", counter),
          StringUtils.trimToEmpty(zahler.getBic()));
      ls_properties.setProperty(SepaUtil.insertIndex("dst.iban", counter),
          StringUtils.trimToEmpty(zahler.getIban()));
      ls_properties.setProperty(SepaUtil.insertIndex("dst.name", counter),
          StringUtils.trimToEmpty(zahler.getName()));
      ls_properties.setProperty(SepaUtil.insertIndex("btg.value", counter),
          zahler.getBetrag().toString());
      ls_properties.setProperty(SepaUtil.insertIndex("btg.curr", counter),
          HBCIProperties.CURRENCY_DEFAULT_DE);
      ls_properties.setProperty(SepaUtil.insertIndex("usage", counter),
          StringUtils.trimToEmpty(zahler.getVerwendungszweck()));
      ls_properties.setProperty(SepaUtil.insertIndex("endtoendid", counter),
          "NOTPROVIDED");
      ls_properties.setProperty(SepaUtil.insertIndex("creditorid", counter),
          creditorid);
      ls_properties.setProperty(SepaUtil.insertIndex("mandateid", counter),
          StringUtils.trimToEmpty(zahler.getMandatid()));
      ls_properties.setProperty(SepaUtil.insertIndex("manddateofsig", counter),
          ISO_DATE.format(zahler.getMandatdatum()));
      ls_properties.setProperty(SepaUtil.insertIndex("purposecode", counter),
          "OHTR");
      counter += 1;
    }
    final OutputStream os = Files.newOutputStream(param.sepafileRCUR.toPath());
    System.setProperty("sepa.pain.formatted", "true");
    ISEPAGenerator sepagenerator = SEPAGeneratorFactory.get("LastSEPA",
        param.sepaVersion);
    sepagenerator.generate(ls_properties, os, true);
    os.close();
  }

  private void buchenHibiscus(AbrechnungSEPAParam param,
      ArrayList<JVereinZahler> zahlerarray) throws ApplicationException
  {
    try
    {
      SepaLastschrift[] lastschriften = new SepaLastschrift[zahlerarray.size()];
      int sli = 0;
      Date d = new Date();
      for (Zahler za : zahlerarray)
      {
        SepaLastschrift sl = (SepaLastschrift) param.service
            .createObject(SepaLastschrift.class, null);
        sl.setBetrag(za.getBetrag().doubleValue());
        sl.setCreditorId(
            (String) Einstellungen.getEinstellung(Property.GLAEUBIGERID));
        sl.setGegenkontoName(za.getName());
        sl.setGegenkontoBLZ(za.getBic());
        sl.setGegenkontoNummer(za.getIban());
        sl.setKonto(param.konto);
        sl.setMandateId(za.getMandatid());
        sl.setSequenceType(
            SepaLastSequenceType.valueOf(za.getMandatsequence().getTxt()));
        sl.setSignatureDate(za.getMandatdatum());
        sl.setTargetDate(za.getFaelligkeit());
        sl.setTermin(d);
        sl.setType(SepaLastType.CORE);
        sl.setZweck(za.getVerwendungszweck());
        lastschriften[sli] = sl;
        sli++;
      }
      SepaLastschriftMerger merger = new SepaLastschriftMerger();
      List<SepaSammelLastschrift> sammler = merger
          .merge(Arrays.asList(lastschriften));
      for (SepaSammelLastschrift s : sammler)
      {
        // Hier noch die eigene Bezeichnung einfuegen
        String vzweck = getVerwendungszweck(param) + " "
            + s.getBezeichnung().substring(0, s.getBezeichnung().indexOf(" "))
            + " vom " + new JVDateFormatDATETIME().format(new Date());
        s.setBezeichnung(vzweck);
        s.store();
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
    catch (SEPAException e)
    {
      throw new ApplicationException(e);
    }
  }

  private String getVerwendungszweck(AbrechnungSEPAParam param)
      throws RemoteException
  {
    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = new AbrechnungsParameterMap().getMap(param, map);
    try
    {
      return VelocityTool.eval(map, param.verwendungszweck);
    }
    catch (IOException e)
    {
      Logger.error("Fehler bei der Aufbereitung der Variablen", e);
      return param.verwendungszweck;
    }
  }

  private String getVerwendungszweckName(ILastschrift adr,
      String verwendungszweck) throws RemoteException
  {
    String id = adr.getID();
    if (adr instanceof Mitglied && (Boolean) Einstellungen
        .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
    {
      id = ((Mitglied) adr).getExterneMitgliedsnummer();
    }
    String mitgliedname = id + "/" + Adressaufbereitung.getNameVorname(adr);

    verwendungszweck = mitgliedname + " " + verwendungszweck;
    if (verwendungszweck.length() >= 140)
    {
      verwendungszweck = verwendungszweck.substring(0, 136) + "...";
    }
    return verwendungszweck;
  }

  private Abrechnungslauf getAbrechnungslauf(AbrechnungSEPAParam param)
      throws RemoteException, ApplicationException
  {
    Abrechnungslauf abrl = (Abrechnungslauf) Einstellungen.getDBService()
        .createObject(Abrechnungslauf.class, null);
    abrl.setDatum(new Date());
    abrl.setAbbuchungsausgabe(param.abbuchungsausgabe.getKey());
    abrl.setFaelligkeit(param.faelligkeit);
    abrl.setDtausdruck(param.sepaprint);
    abrl.setEingabedatum(param.vondatum);
    abrl.setAustrittsdatum(param.bisdatum);
    abrl.setKursteilnehmer(param.kursteilnehmer);
    abrl.setModus(param.abbuchungsmodus);
    abrl.setStichtag(param.stichtag);
    abrl.setZahlungsgrund(getVerwendungszweck(param));
    abrl.setZusatzbetraege(param.zusatzbetraege);
    abrl.setAbgeschlossen(false);
    abrl.setBemerkung("");
    abrl.store();
    return abrl;
  }

  private SollbuchungPosition getSollbuchungPosition(JVereinZahler zahler)
      throws RemoteException, SEPAException
  {

    SollbuchungPosition sp = Einstellungen.getDBService()
        .createObject(SollbuchungPosition.class, null);
    sp.setBetrag(zahler.getBetrag().doubleValue());
    sp.setBuchungsartId(zahler.getBuchungsartId());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      sp.setSteuer(zahler.getSteuer());
    }
    sp.setBuchungsklasseId(zahler.getBuchungsklasseId());
    sp.setDatum(zahler.getDatum());
    sp.setZweck(zahler.getVerwendungszweckOrig());
    return sp;
  }

  private Lastschrift getLastschrift(JVereinZahler zahler, Abrechnungslauf abrl,
      boolean kompakt) throws RemoteException, SEPAException
  {
    Lastschrift ls = (Lastschrift) Einstellungen.getDBService()
        .createObject(Lastschrift.class, null);
    ls.setAbrechnungslauf(Integer.parseInt(abrl.getID()));

    switch (zahler.getPersonTyp())
    {
      case KURSTEILNEHMER:
        ls.setKursteilnehmer(Integer.parseInt(zahler.getPersonId()));
        Kursteilnehmer k = (Kursteilnehmer) Einstellungen.getDBService()
            .createObject(Kursteilnehmer.class, zahler.getPersonId());
        ls.setPersonenart(k.getPersonenart());
        ls.setAnrede(k.getAnrede());
        ls.setTitel(k.getTitel());
        ls.setName(k.getName());
        ls.setVorname(k.getVorname());
        ls.setStrasse(k.getStrasse());
        ls.setAdressierungszusatz(k.getAdressierungszusatz());
        ls.setPlz(k.getPlz());
        ls.setOrt(k.getOrt());
        ls.setStaat(k.getStaatCode());
        ls.setEmail(k.getEmail());
        if (k.getGeschlecht() != null)
        {
          ls.setGeschlecht(k.getGeschlecht());
        }
        ls.setVerwendungszweck(zahler.getVerwendungszweck());
        break;
      case MITGLIED:
        ls.setMitglied(Integer.parseInt(zahler.getPersonId()));
        Mitglied m = (Mitglied) Einstellungen.getDBService()
            .createObject(Mitglied.class, zahler.getPersonId());
        ls.setPersonenart(m.getPersonenart());
        ls.setAnrede(m.getAnrede());
        ls.setTitel(m.getTitel());
        ls.setName(m.getName());
        ls.setVorname(m.getVorname());
        ls.setStrasse(m.getStrasse());
        ls.setAdressierungszusatz(m.getAdressierungszusatz());
        ls.setPlz(m.getPlz());
        ls.setOrt(m.getOrt());
        ls.setStaat(m.getStaatCode());
        ls.setEmail(m.getEmail());
        ls.setGeschlecht(m.getGeschlecht());

        // Bei nicht kompakter Abbuchung Daten des Mitglieds und nicht die des
        // Zahlers verwenden.
        Mitglied mZweck = m;
        if (!kompakt)
        {
          mZweck = zahler.getMitglied();
        }
        String zweck = getVerwendungszweckName(mZweck,
            zahler.getVerwendungszweck());
        ls.setVerwendungszweck(zweck);
        zahler.setVerwendungszweck(zweck);
        break;
      default:
        assert false : "Personentyp ist nicht implementiert";
    }
    ls.setBetrag(zahler.getBetrag().doubleValue());
    ls.setBIC(zahler.getBic());
    ls.setIBAN(zahler.getIban());
    ls.setMandatDatum(zahler.getMandatdatum());
    ls.setMandatSequence(zahler.getMandatsequence().getTxt());
    ls.setMandatID(zahler.getMandatid());
    return ls;
  }

  /*
   * Schreibt die Sollbuchung inkl. Sollbuchungspositionen. Bei Lastschrift
   * werden Istbuchungen erstellt. Ggfs. wird auch die Rechnung erstellt.
   */
  private String writeSollbuchung(Long zahlerId, int zahlungsweg,
      IAdresse adress, ArrayList<SollbuchungPosition> spArray, Date datum,
      Abrechnungslauf abrl, Konto konto, AbrechnungSEPAParam param,
      Double summe) throws ApplicationException, RemoteException, SEPAException
  {
    Sollbuchung sollb = null;
    String zweck = null;
    Rechnung re = null;
    Map<String, Object> map = null;
    if (spArray != null && adress != null && adress instanceof Mitglied)
    {
      sollb = (Sollbuchung) Einstellungen.getDBService()
          .createObject(Sollbuchung.class, null);
      sollb.setAbrechnungslauf(abrl);
      sollb.setZahlungsweg(zahlungsweg);
      sollb.setZahlerId(zahlerId);
      sollb.setDatum(datum);
      sollb.setMitglied((Mitglied) adress);
      // Zweck wird später gefüllt, es muss aber schon was drin stehen damit
      // gespeichert werden kann
      sollb.setZweck1(" ");
      sollb.setBetrag(0d);
      sollb.store();

      summe = 0d;
      for (SollbuchungPosition sp : spArray)
      {
        summe += sp.getBetrag();
        sp.setSollbuchung(sollb.getID());
        sp.store();
      }
      sollb.setBetrag(summe);

      if (param.rechnung)
      {
        Formular form = param.rechnungsformular;
        if (form == null)
        {
          throw new ApplicationException("Kein Rechnungs-Formular ausgewählt");
        }

        re = (Rechnung) Einstellungen.getDBService()
            .createObject(Rechnung.class, null);

        re.setFormular(form);
        re.setDatum(param.rechnungsdatum);
        re.fill(sollb);
        re.store();
        sollb.setRechnung(re);

        if (param.rechnungstext.trim().length() > 0)
        {
          zweck = param.rechnungstext;
          boolean ohneLesefelder = !zweck.contains(Einstellungen.LESEFELD_PRE);
          map = new AllgemeineMap().getMap(null);
          map = new MitgliedMap().getMap((Mitglied) adress, map,
              ohneLesefelder);
          map = new RechnungMap().getMap(re, map);
          map = new AbrechnungsParameterMap().getMap(param, map);
          try
          {
            zweck = VelocityTool.eval(map, zweck);
            if (zweck.length() >= 140)
            {
              zweck = zweck.substring(0, 136) + "...";
            }
          }
          catch (IOException e)
          {
            Logger.error("Fehler bei der Aufbereitung der Variablen", e);
          }

          sollb.setZweck1(zweck);
        }
      }
      if (zweck == null)
      {
        if (spArray.size() == 1)
        {
          zweck = spArray.get(0).getZweck();
        }
        else
        {
          zweck = "";
          for (SollbuchungPosition sp : spArray)
          {
            zweck += ", " + sp.getZweck() + " " + sp.getBetrag();
          }
          zweck = zweck.substring(2);
        }
        sollb.setZweck1(zweck);
      }
      sollb.updateForced();
    }
    if (spArray != null && adress != null && adress instanceof Kursteilnehmer)
    {
      zweck = spArray.get(0).getZweck();
      summe = ((Kursteilnehmer) adress).getBetrag();
    }

    if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT && !((Boolean) Einstellungen
        .getEinstellung(Property.KEINEISTBUCHUNGBEILASTSCHRIFT)))
    {
      Buchung buchung = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
      buchung.setAbrechnungslauf(abrl);
      buchung.setBetrag(summe);
      buchung.setDatum(datum);
      buchung.setKonto(konto);
      buchung.setName(adress != null ? Adressaufbereitung.getNameVorname(adress)
          : "JVerein");
      buchung.setZweck(adress == null ? "Gegenbuchung" : zweck);
      buchung.setIban("");
      buchung.setVerzicht(false);
      buchung.setArt("Lastschrift");
      buchung.setKommentar("Abrechnungslauf " + abrl.getNr() + " vom "
          + Datum.formatDate(abrl.getDatum()));
      buchung.setBezeichnungSachzuwendung("");
      buchung.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
      buchung.setUnterlagenWertermittlung(false);
      buchung.setGeprueft(false);
      buchung.store();

      if (re != null
          && (Boolean) Einstellungen
              .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
          && JVereinPlugin.isArchiveServiceActive())
      {
        FileInputStream fis = null;
        try
        {
          // PDF erstellen
          String dateiname = VorlageUtil.getName(
              VorlageTyp.RECHNUNG_MITGLIED_DATEINAME, re, re.getMitglied());
          File file = File.createTempFile(dateiname, ".pdf");
          FormularAufbereitung aufbereitung = new FormularAufbereitung(file,
              false, true);
          aufbereitung.writeForm(re.getFormular(), map);
          aufbereitung.closeFormular();

          fis = new FileInputStream(file);
          if (fis.available() <= 0)
          {
            throw new ApplicationException("Datei ist leer");
          }
          AbstractDokument doc = Einstellungen.getDBService()
              .createObject(BuchungDokument.class, null);
          doc.setReferenz(Long.valueOf(buchung.getID()));

          // Dokument speichern
          String locverz = "buchungen" + doc.getReferenz();
          QueryMessage qm = new QueryMessage(locverz, fis);
          Application.getMessagingFactory()
              .getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);

          // Satz in die DB schreiben
          doc.setBemerkung(
              dateiname.length() > 50 ? dateiname.substring(0, 50) : dateiname);
          String uuid = qm.getData().toString();
          doc.setUUID(uuid);
          doc.setDatum(datum);
          doc.store();

          // Zusätzliche Eigenschaft speichern
          Map<String, String> filenameMap = new HashMap<>();
          filenameMap.put("filename", file.getName());
          qm = new QueryMessage(uuid, filenameMap);
          Application.getMessagingFactory()
              .getMessagingQueue("jameica.messaging.putmeta").sendMessage(qm);
          file.delete();
        }
        catch (IOException e)
        {
          Logger.error(
              "Fehler beim Speichern der Rechnung als Buchungsdokument", e);
        }
        finally
        {
          try
          {
            fis.close();
          }
          catch (IOException ignore)
          {
          }
        }
      }

      if (sollb != null)
      {
        // Buchungen automatisch splitten
        SplitbuchungsContainer.autoSplit(buchung, sollb, true);
      }
    }
    return zweck;
  }

  private Konto getKonto() throws RemoteException, ApplicationException
  {
    if (Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID) == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gesetzt. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    Konto k = Einstellungen.getDBService().createObject(Konto.class,
        Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID).toString());
    if (k == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    return k;
  }

  private void checkSEPA(AbrechnungSEPAParam param, Mitglied m,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    // Wenn nicht Basislastschrift, dann kein Check nötig
    if (m.getZahlungsweg() == null
        || m.getZahlungsweg() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      return;
    }
    // Check SEPA wenn nicht deaktiviert
    if (!param.sepacheckdisable)
    {
      try
      {
        m.checkSEPA();
      }
      catch (ApplicationException ae)
      {
        String errortext = ae.getLocalizedMessage();
        monitor.log(errortext);
        throw new ApplicationException(errortext);
      }
    }
  }

}
