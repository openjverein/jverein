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
import java.util.Comparator;
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
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragAbrechnungslauf;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatDATETIME;
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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AbrechnungSEPA
{
  private final Calendar sepagueltigkeit;

  private int counter = 0;
  
  private BackgroundTask interrupt;

  private HashMap<String, ArrayList<JVereinZahler>> zahlermap = new HashMap<>();

  public AbrechnungSEPA(AbrechnungSEPAParam param, ProgressMonitor monitor,
      BackgroundTask backgroundTask) throws Exception
  {
    this.interrupt = backgroundTask;
    if (Einstellungen.getEinstellung().getName() == null
        || Einstellungen.getEinstellung().getName().length() == 0
        || Einstellungen.getEinstellung().getIban() == null
        || Einstellungen.getEinstellung().getIban().length() == 0)
    {
      throw new ApplicationException(
          "Name des Vereins oder Bankverbindung fehlt. Bitte unter Administration|Einstellungen erfassen.");
    }

    if (Einstellungen.getEinstellung().getGlaeubigerID() == null
        || Einstellungen.getEinstellung().getGlaeubigerID().length() == 0)
    {
      throw new ApplicationException(
          "Gläubiger-ID fehlt. Gfls. unter https://extranet.bundesbank.de/scp/ oder http://www.oenb.at/idakilz/cid?lang=de beantragen und unter Administration|Einstellungen|Allgemein eintragen.\n"
              + "Zu Testzwecken kann DE98ZZZ09999999999 eingesetzt werden.");
    }

    Abrechnungslauf abrl = getAbrechnungslauf(param);

    sepagueltigkeit = Calendar.getInstance();
    sepagueltigkeit.add(Calendar.MONTH, -36);
    Basislastschrift lastschrift = new Basislastschrift();
    // Vorbereitung: Allgemeine Informationen einstellen
    lastschrift.setBIC(Einstellungen.getEinstellung().getBic());
    lastschrift
        .setGlaeubigerID(Einstellungen.getEinstellung().getGlaeubigerID());
    lastschrift.setIBAN(Einstellungen.getEinstellung().getIban());
    lastschrift.setKomprimiert(param.kompakteabbuchung.booleanValue());
    lastschrift
        .setName(Zeichen.convert(Einstellungen.getEinstellung().getName()));
    lastschrift.setMessageID(abrl.getID() + "-RCUR");

    Konto konto = getKonto();
    ArrayList<JVereinZahler> zahlerarray = new ArrayList<>();

    // Mitglieder Abrechnen und zahlerMap füllen
    abrechnenMitglieder(param, abrl, konto, monitor);

    if (param.zusatzbetraege)
    {
      // Zusatzbetraege Abrechnen und zahlerMap füllen
      abbuchenZusatzbetraege(param, abrl, konto, monitor);
    }

    if (param.kursteilnehmer)
    {
      // Kursteilnehmer direkt in zahlerarray da es für jeden nur eine
      // Lastschrift geben kann
      zahlerarray = abbuchenKursteilnehmer(param, abrl, konto, monitor);
    }

    monitor.log(counter + " abgerechnete Fälle");

    Iterator<Entry<String, ArrayList<JVereinZahler>>> iterator = zahlermap
        .entrySet().iterator();

    while (iterator.hasNext())
    {
      if (interrupt.isInterrupted())
      {
        throw new ApplicationException("Abrechnung abgebrochen");
      }
      HashMap<Zahlungsweg, ArrayList<SollbuchungPosition>> spMap = new HashMap<>();
      HashMap<Zahlungsweg, JVereinZahler> gesamtZahlerMap = new HashMap<>();
      ArrayList<JVereinZahler> zahlerList = iterator.next().getValue();
      //Nach Betrag sortieren damit auch erstettungen funktionieren
      zahlerList.sort(new Comparator<JVereinZahler>()
      {
        @Override
        public int compare(JVereinZahler z1, JVereinZahler z2)
        {
          try
          {
            return z2.getBetrag().compareTo(z1.getBetrag());
          }
          catch (SEPAException e)
          {
            return 0;
          }
        }
      });
      for (JVereinZahler zahler : zahlerList)
      {
        // Sollbuchungsositionen in Map füllen
        if (param.sollbuchungenzusammenfassen)
        {
          ArrayList<SollbuchungPosition> spArray = spMap
              .get(zahler.getZahlungsweg());
          if (spArray == null)
          {
            spArray = new ArrayList<>();
            spArray.add(getSollbuchungPosition(zahler));
            spMap.put(zahler.getZahlungsweg(), spArray);
          }
          else
          {
            spArray.add(getSollbuchungPosition(zahler));
            spMap.replace(zahler.getZahlungsweg(), spArray);
          }
        }
        else
        {
          // Für jede Buchung eine Sollbuchung mit einer Sollbuchungsposition.
          ArrayList<SollbuchungPosition> sbArray = new ArrayList<>();
          sbArray.add(getSollbuchungPosition(zahler));

          writeSollbuchung(zahler, sbArray, param.faelligkeit, abrl, konto,
              zahler.getZahlungsweg().getKey() == Zahlungsweg.BASISLASTSCHRIFT,
              param);
        }

        // Bei kompakter Abbuchung Zahler zusammenfassen.
        if (param.kompakteabbuchung || param.sollbuchungenzusammenfassen)
        {
          JVereinZahler gesamtZahler = gesamtZahlerMap
              .get(zahler.getZahlungsweg());
          if (gesamtZahler == null)
          {
            gesamtZahler = zahler;
            gesamtZahlerMap.put(zahler.getZahlungsweg(), gesamtZahler);
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
            gesamtZahlerMap.replace(zahler.getZahlungsweg(), gesamtZahler);
          }
        }
        // Bei nicht kompakter Abbuchung Lastschriten direkt füllen.
        else if (zahler.getZahlungsweg()
            .getKey() == Zahlungsweg.BASISLASTSCHRIFT)
        {
          zahlerarray.add(zahler);
        }
      }

      // Bei kompakter Abbuchung erst hier die zusammengefassten Lastschriften
      // hinzufügen.
      JVereinZahler lsGesamtZahler = gesamtZahlerMap
          .get(new Zahlungsweg(Zahlungsweg.BASISLASTSCHRIFT));
      if ((param.kompakteabbuchung || param.sollbuchungenzusammenfassen)
          && lsGesamtZahler != null)
      {
        zahlerarray.add(lsGesamtZahler);
      }

      if (param.sollbuchungenzusammenfassen)
      {
        // Für jeden Zahlungsweg eine Sollbuchung mit X Sollbuchungspositionen.
        Iterator<Entry<Zahlungsweg, ArrayList<SollbuchungPosition>>> spIterator = spMap
            .entrySet().iterator();
        // Wird für die verschiedenen Zahlungswege des Mitglieds durchlaufen.
        while (spIterator.hasNext())
        {
          Entry<Zahlungsweg, ArrayList<SollbuchungPosition>> entry = spIterator
              .next();
          ArrayList<SollbuchungPosition> spArray = entry.getValue();

          JVereinZahler zahler = gesamtZahlerMap
              .get((Zahlungsweg) entry.getKey());
          writeSollbuchung(zahler, spArray, param.faelligkeit, abrl, konto,
              ((Zahlungsweg) entry.getKey())
                  .getKey() == Zahlungsweg.BASISLASTSCHRIFT,
              param);
        }
      }
    }

    BigDecimal summelastschriften = BigDecimal.valueOf(0);
    for (JVereinZahler zahler : zahlerarray)
    {
      summelastschriften = summelastschriften.add(zahler.getBetrag());

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
          ls.setStaat(k.getStaat());
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
          if (m.getKtoiName() == null || m.getKtoiName().length() == 0)
          {
            ls.setPersonenart(m.getPersonenart());
            ls.setAnrede(m.getAnrede());
            ls.setTitel(m.getTitel());
            ls.setName(m.getName());
            ls.setVorname(m.getVorname());
            ls.setStrasse(m.getStrasse());
            ls.setAdressierungszusatz(m.getAdressierungszusatz());
            ls.setPlz(m.getPlz());
            ls.setOrt(m.getOrt());
            ls.setStaat(m.getStaat());
            ls.setEmail(m.getEmail());
            ls.setGeschlecht(m.getGeschlecht());
          }
          else
          {
            ls.setPersonenart(m.getKtoiPersonenart());
            ls.setAnrede(m.getKtoiAnrede());
            ls.setTitel(m.getKtoiTitel());
            ls.setName(m.getKtoiName());
            ls.setVorname(m.getKtoiVorname());
            ls.setStrasse(m.getKtoiStrasse());
            ls.setAdressierungszusatz(m.getKtoiAdressierungszusatz());
            ls.setPlz(m.getKtoiPlz());
            ls.setOrt(m.getKtoiOrt());
            ls.setStaat(m.getKtoiStaat());
            ls.setEmail(m.getKtoiEmail());
            ls.setGeschlecht(m.getKtoiGeschlecht());
          }
          String zweck = getVerwendungszweckName(m,
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
      ls.store();
    }

    // Gegenbuchung für die Sollbuchungen schreiben
    if (!summelastschriften.equals(BigDecimal.valueOf(0)))
    {
      writeSollbuchung(null, null, param.faelligkeit, abrl, konto, true, param);
    }

    // Wenn keine Lastschriften vorhanden sind, wird kein File erzeugt.
    if ((param.abbuchungsausgabe == Abrechnungsausgabe.SEPA_DATEI)
        && !zahlerarray.isEmpty())
    {
      writeSepaFile(param, lastschrift, zahlerarray);
      monitor.log(String.format("SEPA-Datei %s geschrieben.",
          param.sepafileRCUR.getAbsolutePath()));
      param.setText(String.format(", SEPA-Datei %s geschrieben.",
          param.sepafileRCUR.getAbsolutePath()));
    }

    if (param.abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
    {
      // Wenn keine Buchungen vorhanden sind, wird nichts an Hibiscus übergeben.
      if (zahlerarray.size() != 0)
      {
        buchenHibiscus(param, zahlerarray);
        monitor.log("Hibiscus-Lastschrift erzeugt.");
        param.setText(String.format(", Hibiscus-Lastschrift erzeugt."));
      }
    }

    if (param.pdffileRCUR != null)
    {
      // Nur für die PDF erstellung müssen die Zahler in der Lastschrift
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
    monitor.log("Abrechnung durchgeführt");
  }

  private void abrechnenMitglieder(AbrechnungSEPAParam param,
      Abrechnungslauf abrl, Konto konto, ProgressMonitor monitor)
      throws Exception
  {
    if (param.abbuchungsmodus != Abrechnungsmodi.KEINBEITRAG)
    {
      // Alle Mitglieder lesen
      DBIterator<Mitglied> list = Einstellungen.getDBService()
          .createList(Mitglied.class);
      MitgliedUtils.setMitglied(list);

      // Das Mitglied muss bereits eingetreten sein
      list.addFilter("(eintritt <= ? or eintritt is null) ",
          new Object[] { new java.sql.Date(param.stichtag.getTime()) });
      // Das Mitglied darf noch nicht ausgetreten sein
      list.addFilter("(austritt is null or austritt > ?)",
          new Object[] { new java.sql.Date(param.stichtag.getTime()) });
      // Bei Abbuchungen im Laufe des Jahres werden nur die Mitglieder
      // berücksichtigt, die bis zu einem bestimmten Zeitpunkt ausgetreten sind.
      if (param.bisdatum != null)
      {
        list.addFilter("(austritt <= ?)",
            new Object[] { new java.sql.Date(param.bisdatum.getTime()) });
      }
      // Bei Abbuchungen im Laufe des Jahres werden nur die Mitglieder
      // berücksichtigt, die ab einem bestimmten Zeitpunkt eingetreten sind.
      if (param.vondatum != null)
      {
        list.addFilter("eintritt >= ?",
            new Object[] { new java.sql.Date(param.vondatum.getTime()) });
      }
      if (Einstellungen.getEinstellung()
          .getBeitragsmodel() == Beitragsmodel.MONATLICH12631)
      {
        if (param.abbuchungsmodus == Abrechnungsmodi.HAVIMO)
        {
          list.addFilter(
              "(zahlungsrhytmus = ? or zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.HALBJAEHRLICH),
                  Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
                  Integer.valueOf(Zahlungsrhythmus.MONATLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.JAVIMO)
        {
          list.addFilter(
              "(zahlungsrhytmus = ? or zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.JAEHRLICH),
                  Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
                  Integer.valueOf(Zahlungsrhythmus.MONATLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.VIMO)
        {
          list.addFilter("(zahlungsrhytmus = ? or zahlungsrhytmus = ?)",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH),
                  Integer.valueOf(Zahlungsrhythmus.MONATLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.MO)
        {
          list.addFilter("zahlungsrhytmus = ?",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.MONATLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.VI)
        {
          list.addFilter("zahlungsrhytmus = ?", new Object[] {
              Integer.valueOf(Zahlungsrhythmus.VIERTELJAEHRLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.HA)
        {
          list.addFilter("zahlungsrhytmus = ?",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.HALBJAEHRLICH) });
        }
        if (param.abbuchungsmodus == Abrechnungsmodi.JA)
        {
          list.addFilter("zahlungsrhytmus = ?",
              new Object[] { Integer.valueOf(Zahlungsrhythmus.JAEHRLICH) });
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

        JVereinZahler zahler = abrechnungMitgliederSub(param, monitor, abrl,
            konto, m, m.getBeitragsgruppe(), true);

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
              monitor, abrl, konto, m, sb.getBeitragsgruppe(), false);
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
      ProgressMonitor monitor, Abrechnungslauf abrl, Konto konto, Mitglied m,
      Beitragsgruppe bg, boolean primaer)
      throws RemoteException, ApplicationException
  {
    Double betr = 0d;
    JVereinZahler zahler = null;
    Mitglied mZahler = m;
    if (m.getZahlungsweg() != null
        && m.getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
    {
      if (m.getZahlerID() == null)
      {
        throw new ApplicationException("Kein Vollzahler vorhanden: "
            + Adressaufbereitung.getNameVorname(m));
      }
      mZahler = Einstellungen.getDBService().createObject(Mitglied.class,
          m.getZahlerID().toString());
    }
    if (Einstellungen.getEinstellung()
        .getBeitragsmodel() == Beitragsmodel.FLEXIBEL)
    {
      if (mZahler.getZahlungstermin() != null
          && !mZahler.getZahlungstermin().isAbzurechnen(param.abrechnungsmonat))
      {
        return null;
      }
    }

    try
    {
      betr = BeitragsUtil.getBeitrag(
          Einstellungen.getEinstellung().getBeitragsmodel(),
          mZahler.getZahlungstermin(), mZahler.getZahlungsrhythmus().getKey(),
          bg, param.stichtag, m);
    }
    catch (NullPointerException e)
    {
      throw new ApplicationException(
          "Zahlungsinformationen bei " + Adressaufbereitung.getNameVorname(m));
    }
    if (primaer)
    {
      if (Einstellungen.getEinstellung().getIndividuelleBeitraege()
          && m.getIndividuellerBeitrag() != null)
      {
        betr = m.getIndividuellerBeitrag();
      }
    }
    if ((betr == 0d) || !checkSEPA(mZahler, monitor))
    {
      return null;
    }
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
        zahler.setBuchungsartId(bg.getBuchungsart().getID());
      }
      if (bg.getBuchungsklasseId() != null)
      {
        zahler.setBuchungsklasseId(bg.getBuchungsklasseId().toString());
      }
      zahler.setDatum(param.faelligkeit);
      zahler.setMitglied(m);
      if (m.getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
      {
        zahler.setVerwendungszweck(
            (primaer ? param.verwendungszweck : bg.getBezeichnung()) + " "
                + m.getVorname());
      }
      else if (primaer)
      {
        String vzweck = abrl.getZahlungsgrund();
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
      zahler.setName(mZahler.getKontoinhaber(1));
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
      Abrechnungslauf abrl, Konto konto, ProgressMonitor monitor)
      throws Exception
  {
    int count = 0;
    DBIterator<Zusatzbetrag> list = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
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
        if (!m.isAngemeldet(param.stichtag)
            && !Einstellungen.getEinstellung().getZusatzbetragAusgetretene())
        {
          continue;
        }
        Mitglied mZahler = m;
        if (m.getZahlungsweg() != null
            && m.getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
        {
          mZahler = Einstellungen.getDBService().createObject(Mitglied.class,
              m.getZahlerID().toString());
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

        if (!checkSEPA(mZahler, monitor))
        {
          continue;
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
          zahler.setName(mZahler.getKontoinhaber(1));
          zahler.setVerwendungszweck(vzweck);
          zahler.setZahlungsweg(new Zahlungsweg(zahlungsweg));
          if (z.getBuchungsart() != null)
          {
            zahler.setBuchungsartId(z.getBuchungsart().getID());
          }
          if (z.getBuchungsklasseId() != null)
          {
            zahler.setBuchungsklasseId(z.getBuchungsklasseId().toString());
          }
          zahler.setDatum(z.getFaelligkeit());
          zahler.setMitglied(m);

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
            z.setAusfuehrung(Datum.getHeute());
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
        zahlerarray.add(zahler);
        kt.setAbbudatum(param.faelligkeit);
        kt.store();

        ArrayList<SollbuchungPosition> spArray = new ArrayList<>();
        spArray.add(getSollbuchungPosition(zahler));
        writeSollbuchung(zahler, spArray, param.faelligkeit, abrl, konto, true,
            param);

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
    GUI.getDisplay().asyncExec(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          new Program().handleAction(new File(pdf_fn));
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(
              ae.getLocalizedMessage(), StatusBarMessage.TYPE_ERROR));
        }
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
        sl.setCreditorId(Einstellungen.getEinstellung().getGlaeubigerID());
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
    if (adr instanceof Mitglied
        && Einstellungen.getEinstellung().getExterneMitgliedsnummer())
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
    abrl.store();
    return abrl;
  }

  private SollbuchungPosition getSollbuchungPosition(JVereinZahler zahler)
      throws RemoteException, SEPAException
  {

    SollbuchungPosition sp = Einstellungen.getDBService()
        .createObject(SollbuchungPosition.class, null);
    sp.setBetrag(zahler.getBetrag().doubleValue());
    sp.setSteuersatz(0d);
    sp.setBuchungsartId(zahler.getBuchungsartId());
    sp.setBuchungsklasseId(zahler.getBuchungsklasseId());
    sp.setDatum(zahler.getDatum());
    sp.setZweck(zahler.getVerwendungszweckOrig());
    return sp;
  }

  private void writeSollbuchung(JVereinZahler zahler,
      ArrayList<SollbuchungPosition> spArray, Date datum, Abrechnungslauf abrl,
      Konto konto, boolean haben, AbrechnungSEPAParam param)
      throws ApplicationException, RemoteException, SEPAException
  {
    Mitgliedskonto mk = null;
    double summe = 0d;
    String zweck = null;
    Rechnung re = null;
    if (spArray != null)
    {
      // Rechnungen nur für (Nicht-)Mitglieder unterstützt
      // (nicht für Kursteilnehmer)
      if (param.rechnung && zahler.getMitglied() != null)
      {
        Formular form = param.rechnungsformular;
        if (form == null)
        {
          throw new ApplicationException("Kein Rechnungs-Formular ausgewählt");
        }
        Mitglied mitglied = zahler.getMitglied();
        re = (Rechnung) Einstellungen.getDBService()
            .createObject(Rechnung.class, null);

        re.setMitglied(Integer.parseInt(mitglied.getID()));
        re.setFormular(form);
        if (mitglied.getKtoiName() == null
            || mitglied.getKtoiName().length() == 0)
        {
          re.setPersonenart(mitglied.getPersonenart());
          re.setAnrede(mitglied.getAnrede());
          re.setTitel(mitglied.getTitel());
          re.setName(mitglied.getName());
          re.setVorname(mitglied.getVorname());
          re.setStrasse(mitglied.getStrasse());
          re.setAdressierungszusatz(mitglied.getAdressierungszusatz());
          re.setPlz(mitglied.getPlz());
          re.setOrt(mitglied.getOrt());
          re.setStaat(mitglied.getStaat());
          // re.setEmail(mitglied.getEmail());
          re.setGeschlecht(mitglied.getGeschlecht());
        }
        else
        {
          re.setPersonenart(mitglied.getKtoiPersonenart());
          re.setAnrede(mitglied.getKtoiAnrede());
          re.setTitel(mitglied.getKtoiTitel());
          re.setName(mitglied.getKtoiName());
          re.setVorname(mitglied.getKtoiVorname());
          re.setStrasse(mitglied.getKtoiStrasse());
          re.setAdressierungszusatz(mitglied.getKtoiAdressierungszusatz());
          re.setPlz(mitglied.getKtoiPlz());
          re.setOrt(mitglied.getKtoiOrt());
          re.setStaat(mitglied.getKtoiStaat());
          // re.setEmail(mitglied.getKtoiEmail());
          re.setGeschlecht(mitglied.getKtoiGeschlecht());
        }
        re.setDatum(new Date());
        if (!mitglied.getMandatDatum().equals(Einstellungen.NODATE))
        {
          re.setMandatDatum(mitglied.getMandatDatum());
        }
        re.setMandatID(mitglied.getMandatID());
        re.setBIC(mitglied.getBic());
        re.setIBAN(mitglied.getIban());
        re.setZahlungsweg(zahler.getZahlungsweg().getKey());

        double reSumme = 0;
        for (SollbuchungPosition sp : spArray)
        {
          reSumme += sp.getBetrag().doubleValue();
        }
        re.setBetrag(reSumme);
        re.store();

        zweck = param.rechnungstext;
        boolean ohneLesefelder = !zweck.contains(Einstellungen.LESEFELD_PRE);
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap(mitglied, map, ohneLesefelder);
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
        zahler.setVerwendungszweck(zweck);
      }
      else
      {
        zweck = zahler.getVerwendungszweckOrig();
      }

      mk = (Mitgliedskonto) Einstellungen.getDBService()
          .createObject(Mitgliedskonto.class, null);
      mk.setAbrechnungslauf(abrl);
      mk.setZahlungsweg(zahler.getZahlungsweg().getKey());

      mk.setDatum(datum);
      if (zahler.getMitglied() != null)
      {
        mk.setMitglied(zahler.getMitglied());
        zweck = getVerwendungszweckName(zahler.getMitglied(), zweck);
      }
      mk.setZweck1(zweck);
      mk.setBetrag(0d);
      mk.store();

      for (SollbuchungPosition sp : spArray)
      {
        summe += sp.getBetrag().doubleValue();
        sp.setSollbuchung(mk.getID());
        sp.store();
      }
      mk.setBetrag(summe);
      mk.setRechnung(re);
      mk.store();
    }

    if (haben)
    {
      Buchung buchung = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
      buchung.setAbrechnungslauf(abrl);
      buchung.setBetrag(summe);
      buchung.setDatum(datum);
      buchung.setKonto(konto);
      IAdresse adr = null;
      if (zahler != null && zahler.getPersonTyp() == JVereinZahlerTyp.MITGLIED)
      {
        adr = zahler.getMitglied();
      }
      else if (zahler != null
          && zahler.getPersonTyp() == JVereinZahlerTyp.KURSTEILNEHMER)
      {
        adr = (IAdresse) Einstellungen.getDBService()
            .createObject(Kursteilnehmer.class, zahler.getPersonId());
      }
      buchung.setName(
          adr != null ? Adressaufbereitung.getNameVorname(adr) : "JVerein");
      buchung.setZweck(zahler == null ? "Gegenbuchung" : zweck);
      if (mk != null)
      {
        buchung.setMitgliedskonto(mk);
      }
      buchung.store();

      if (spArray == null)
      {
        return;
      }

      if (spArray.get(0).getBuchungsartId() != null)
      {
        buchung.setBuchungsartId(
            Long.parseLong(spArray.get(0).getBuchungsartId()));
      }
      if (spArray.get(0).getBuchungsklasseId() != null)
      {
        buchung.setBuchungsklasseId(
            Long.parseLong(spArray.get(0).getBuchungsklasseId()));
      }

      // Buchungen automatisch splitten
      HashMap<String, Double> splitMap = new HashMap<>();
      for (SollbuchungPosition sp : spArray)
      {
        // Wenn eine Buchungsart fehlt können wir nicht automatisch splitten
        if (sp.getBuchungsartId() == null)
        {
          splitMap = new HashMap<>();
          break;
        }
        String key = sp.getBuchungsartId() + "-"
            + (sp.getBuchungsklasseId() != null ? sp.getBuchungsklasseId()
                : "");
        Double betrag = splitMap.get(key);
        if (sp.getBetrag().doubleValue() == 0)
        {
          continue;
        }

        if (betrag == null)
        {
          splitMap.put(key, sp.getBetrag().doubleValue());
        }
        else
        {
          splitMap.replace(key, betrag + sp.getBetrag().doubleValue());
        }
      }

      if (splitMap.size() > 1)
      {
        buchung.setSplitTyp(SplitbuchungTyp.HAUPT);
        buchung.store();

        Iterator<Entry<String, Double>> iterator = splitMap.entrySet()
            .iterator();
        SplitbuchungsContainer.init(buchung);
        while (iterator.hasNext())
        {
          Entry<String, Double> entry = iterator.next();

          Buchung splitBuchung = (Buchung) Einstellungen.getDBService()
              .createObject(Buchung.class, null);
          splitBuchung.setAbrechnungslauf(abrl);
          splitBuchung.setBetrag(entry.getValue());
          splitBuchung.setDatum(datum);
          splitBuchung.setKonto(konto);
          splitBuchung.setName(buchung.getName());
          splitBuchung.setZweck(buchung.getZweck());
          splitBuchung.setMitgliedskonto(mk);
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
          splitBuchung.setSplitId(Long.parseLong(buchung.getID()));

          SplitbuchungsContainer.add(splitBuchung);
        }
        SplitbuchungsContainer.store();
      }
    }
  }

  /**
   * Ist das Abbuchungskonto in der Buchführung eingerichtet?
   *
   * @throws SEPAException
   */
  private Konto getKonto()
      throws ApplicationException, RemoteException, SEPAException
  {
    // Variante 1: IBAN
    DBIterator<Konto> it = Einstellungen.getDBService().createList(Konto.class);
    it.addFilter("nummer = ?", Einstellungen.getEinstellung().getIban());
    if (it.size() == 1)
    {
      return it.next();
    }
    // Variante 2: Kontonummer aus IBAN
    it = Einstellungen.getDBService().createList(Konto.class);
    IBAN iban = new IBAN(Einstellungen.getEinstellung().getIban());
    it.addFilter("nummer = ?", iban.getKonto());
    if (it.size() == 1)
    {
      return it.next();
    }
    throw new ApplicationException(String.format(
        "Weder Konto %s noch Konto %s ist in der Buchführung eingerichtet. Menu: Buchführung | Konten",
        Einstellungen.getEinstellung().getIban(), iban.getKonto()));
  }

  private boolean checkSEPA(Mitglied m, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    if (m.getZahlungsweg() == null
        || m.getZahlungsweg() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      return true;
    }
    Date letzte_lastschrift = m.getLetzteLastschrift();
    if (letzte_lastschrift != null
        && letzte_lastschrift.before(sepagueltigkeit.getTime()))
    {
      monitor.log(Adressaufbereitung.getNameVorname(m)
          + ": Letzte Lastschrift ist älter als 36 Monate.");
      throw new ApplicationException(Adressaufbereitung.getNameVorname(m)
          + ": Letzte Lastschrift ist älter als 36 Monate.");
    }
    if (m.getMandatDatum() == Einstellungen.NODATE)
    {
      monitor.log(Adressaufbereitung.getNameVorname(m)
          + ": Kein Mandat-Datum vorhanden.");
      throw new ApplicationException(Adressaufbereitung.getNameVorname(m)
          + ": Kein Mandat-Datum vorhanden.");
    }
    return true;
  }

}
