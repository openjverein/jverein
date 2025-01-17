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
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
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
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AbrechnungSEPA
{
  private int counter = 0;

  public AbrechnungSEPA(AbrechnungSEPAParam param, ProgressMonitor monitor)
      throws Exception
  {
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
          "Gl�ubiger-ID fehlt. Gfls. unter https://extranet.bundesbank.de/scp/ oder http://www.oenb.at/idakilz/cid?lang=de beantragen und unter Administration|Einstellungen|Allgemein eintragen.\n"
              + "Zu Testzwecken kann DE98ZZZ09999999999 eingesetzt werden.");
    }

    Abrechnungslauf abrl = getAbrechnungslauf(param);

    Basislastschrift lastschrift = new Basislastschrift();
    // Vorbereitung: Allgemeine Informationen einstellen
    lastschrift.setBIC(Einstellungen.getEinstellung().getBic());
    lastschrift
        .setGlaeubigerID(Einstellungen.getEinstellung().getGlaeubigerID());
    lastschrift.setIBAN(Einstellungen.getEinstellung().getIban());
    lastschrift.setKomprimiert(param.kompakteabbuchung.booleanValue());
    lastschrift
        .setName(Zeichen.convert(Einstellungen.getEinstellung().getName()));

    Konto konto = getKonto();

    abrechnenMitglieder(param, lastschrift, monitor, abrl, konto);

    if (param.zusatzbetraege)
    {
      abbuchenZusatzbetraege(param, lastschrift, abrl, konto, monitor);
    }
    if (param.kursteilnehmer)
    {
      abbuchenKursteilnehmer(param, lastschrift, abrl, konto, monitor);
    }

    monitor.log(counter + " abgerechnete F�lle");

    lastschrift.setMessageID(abrl.getID() + "-RCUR");
    if (param.kompakteabbuchung || param.sepaprint)
    {
      // F�r kompakte Abbuchung wird erst in write die Zahlerliste gef�llt. Das
      // f�r die
      // PDF-Erzeugung ben�tigte Datum wird auch erst in write gesetzt
      File temp_file = Files.createTempFile("jv", ".xml").toFile();
      lastschrift.write(temp_file);
      temp_file.delete();
    }

    ArrayList<Zahler> z = lastschrift.getZahler();
    // Wenn keine Buchungen vorhanden sind, wird kein File erzeugt.
    if ((param.abbuchungsausgabe == Abrechnungsausgabe.SEPA_DATEI)
        && !z.isEmpty())
    {
      writeSepaFile(param, lastschrift, z);
      monitor.log(String.format("SEPA-Datei %s geschrieben.",
          param.sepafileRCUR.getAbsolutePath()));
      param.setText(String.format(", SEPA-Datei %s geschrieben.",
          param.sepafileRCUR.getAbsolutePath()));
    }

    BigDecimal summemitgliedskonto = BigDecimal.valueOf(0);
    for (Zahler za : z)
    {
      Lastschrift ls = (Lastschrift) Einstellungen.getDBService()
          .createObject(Lastschrift.class, null);
      ls.setAbrechnungslauf(Integer.parseInt(abrl.getID()));

      assert (za instanceof JVereinZahler) : "Illegaler Zahlertyp in Sepa-Abrechnung detektiert.";

      JVereinZahler vza = (JVereinZahler) za;

      switch (vza.getPersonTyp())
      {
        case KURSTEILNEHMER:
          ls.setKursteilnehmer(Integer.parseInt(vza.getPersonId()));
          Kursteilnehmer k = (Kursteilnehmer) Einstellungen.getDBService()
              .createObject(Kursteilnehmer.class, vza.getPersonId());
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
          break;
        case MITGLIED:
          ls.setMitglied(Integer.parseInt(vza.getPersonId()));
          Mitglied m = (Mitglied) Einstellungen.getDBService()
              .createObject(Mitglied.class, vza.getPersonId());
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
            ls.setStaat(m.getStaatCode());
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
            ls.setStaat(m.getKtoiStaatCode());
            ls.setEmail(m.getKtoiEmail());
            ls.setGeschlecht(m.getKtoiGeschlecht());
          }
          break;
        default:
          assert false : "Personentyp ist nicht implementiert";
      }
      ls.setBetrag(za.getBetrag().doubleValue());
      summemitgliedskonto = summemitgliedskonto.add(za.getBetrag());
      ls.setBIC(za.getBic());
      ls.setIBAN(za.getIban());
      ls.setMandatDatum(za.getMandatdatum());
      ls.setMandatSequence(za.getMandatsequence().getTxt());
      ls.setMandatID(za.getMandatid());
      ls.setVerwendungszweck(za.getVerwendungszweck());
      ls.store();
    }

    // Gegenbuchung f�r das Mitgliedskonto schreiben
    if (!summemitgliedskonto.equals(BigDecimal.valueOf(0)))
    {
      writeMitgliedskonto(null, param.faelligkeit, "Gegenbuchung",
          summemitgliedskonto.doubleValue() * -1, abrl, true, getKonto(), null,
          null, null);
    }
    if (param.abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
    {
      // Wenn keine Buchungen vorhanden sind, wird nichts an Hibiscus �bergeben.
      if (z.size() != 0)
      {
        buchenHibiscus(param, z);
        monitor.log("Hibiscus-Lastschrift erzeugt.");
        param.setText(String.format(", Hibiscus-Lastschrift erzeugt."));
      }
    }
    if (param.pdffileRCUR != null)
    {
      ausdruckenSEPA(lastschrift, param.pdffileRCUR);
    }
  }

  private void abrechnenMitglieder(AbrechnungSEPAParam param,
      Basislastschrift lastschrift, ProgressMonitor monitor,
      Abrechnungslauf abrl, Konto konto) throws Exception
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
      // ber�cksichtigt, die bis zu einem bestimmten Zeitpunkt ausgetreten sind.
      if (param.bisdatum != null)
      {
        list.addFilter("(austritt <= ?)",
            new Object[] { new java.sql.Date(param.bisdatum.getTime()) });
      }
      // Bei Abbuchungen im Laufe des Jahres werden nur die Mitglieder
      // ber�cksichtigt, die ab einem bestimmten Zeitpunkt eingetreten sind.
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

      // S�tze im Resultset
      int count = 0;
      while (list.hasNext())
      {
        Mitglied m = list.next();

        JVereinZahler z = abrechnungMitgliederSub(param, monitor, abrl, konto,
            m, m.getBeitragsgruppe(), true);

        DBIterator<SekundaereBeitragsgruppe> sekundaer = Einstellungen
            .getDBService().createList(SekundaereBeitragsgruppe.class);
        sekundaer.addFilter("mitglied=?", m.getID());
        while (sekundaer.hasNext())
        {
          SekundaereBeitragsgruppe sb = sekundaer.next();
          JVereinZahler z2 = abrechnungMitgliederSub(param, monitor, abrl,
              konto, m, sb.getBeitragsgruppe(), false);
          if (z2 != null)
          {
            if (z != null)
            {
              z.add(z2);
            }
            else
            {
              z = z2;
            }
          }
        }
        if (z != null)
        {
          lastschrift.add(z);
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
            + m.getName() + ", " + m.getVorname());
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
        return zahler;
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
          "Zahlungsinformationen bei " + m.getName() + ", " + m.getVorname());
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
      return zahler;
    }
    counter++;

    String vzweck = abrl.getZahlungsgrund();
    Map<String, Object> map = new MitgliedMap().getMap(m, null);
    try
    {
      vzweck = VelocityTool.eval(map, vzweck);
    }
    catch (IOException e)
    {
      Logger.error("Fehler bei der Aufbereitung der Variablen", e);
    }

    writeMitgliedskonto(m, param.faelligkeit,
        primaer ? vzweck : bg.getBezeichnung(), betr, abrl,
        mZahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT, konto,
        bg.getBuchungsart(), bg.getBuchungsklasseId(),
        mZahler.getZahlungsweg());
    if (mZahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
    {
      try
      {
        zahler = new JVereinZahler();
        zahler.setPersonId(mZahler.getID());
        zahler.setPersonTyp(JVereinZahlerTyp.MITGLIED);
        zahler.setBetrag(
            BigDecimal.valueOf(betr).setScale(2, RoundingMode.HALF_UP));
        IBAN i = new IBAN(mZahler.getIban()); // Pr�fung der IBAN
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
        new BIC(zahler.getBic()); // Pr�fung des BIC
        zahler.setMandatid(mZahler.getMandatID());
        zahler.setMandatdatum(mZahler.getMandatDatum());
        zahler.setMandatsequence(MandatSequence.RCUR);
        zahler.setFaelligkeit(param.faelligkeit);
        if (primaer && m.getZahlungsweg() != Zahlungsweg.VOLLZAHLER)
        {
          String verwendungszweck = getVerwendungszweck2(mZahler) + " "
              + vzweck;
          if (verwendungszweck.length() >= 140)
          {
            verwendungszweck = verwendungszweck.substring(0, 136) + "...";
          }
          zahler.setVerwendungszweck(verwendungszweck);
        }
        else
        {
          zahler.setVerwendungszweck(bg.getBezeichnung());
        }
        if (m.getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
        {
          zahler.setVerwendungszweck(
              zahler.getVerwendungszweck() + " " + m.getVorname());
        }
        zahler.setName(mZahler.getKontoinhaber(1));
      }
      catch (Exception e)
      {
        throw new ApplicationException(
            Adressaufbereitung.getNameVorname(m) + ": " + e.getMessage());
      }
    }
    return zahler;
  }

  private void abbuchenZusatzbetraege(AbrechnungSEPAParam param,
      Basislastschrift lastschrift, Abrechnungslauf abrl, Konto konto,
      ProgressMonitor monitor)
      throws NumberFormatException, IOException, ApplicationException
  {
    int count = 0;
    DBIterator<Zusatzbetrag> list = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    while (list.hasNext())
    {
      Zusatzbetrag z = list.next();
      if (z.isAktiv(param.stichtag))
      {
        Mitglied m = z.getMitglied();
        if (m.isAngemeldet(param.stichtag)
            || Einstellungen.getEinstellung().getZusatzbetragAusgetretene())
        {
          //
        }
        else
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

        if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT
            && !checkSEPA(mZahler, monitor))
        {
          continue;
        }
        counter++;
        String vzweck = z.getBuchungstext();
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap(m, map);
        map = new AbrechnungsParameterMap().getMap(param, map);
        try
        {
          vzweck = VelocityTool.eval(map, vzweck);
        }
        catch (IOException e)
        {
          Logger.error("Fehler bei der Aufbereitung der Variablen", e);
        }

        if (zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT)
        {
          try
          {
            JVereinZahler zahler = new JVereinZahler();
            zahler.setPersonId(mZahler.getID());
            zahler.setPersonTyp(JVereinZahlerTyp.MITGLIED);
            zahler.setBetrag(BigDecimal.valueOf(z.getBetrag()).setScale(2,
                RoundingMode.HALF_UP));
            new BIC(mZahler.getBic());
            new IBAN(mZahler.getIban());
            zahler.setBic(mZahler.getBic());
            zahler.setIban(mZahler.getIban());
            zahler.setMandatid(mZahler.getMandatID());
            zahler.setMandatdatum(mZahler.getMandatDatum());
            zahler.setMandatsequence(MandatSequence.RCUR);
            zahler.setFaelligkeit(param.faelligkeit);
            zahler.setName(mZahler.getKontoinhaber(1));
            zahler.setVerwendungszweck(vzweck);
            lastschrift.add(zahler);
          }
          catch (Exception e)
          {
            throw new ApplicationException(
                Adressaufbereitung.getNameVorname(m) + ": " + e.getMessage());
          }
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
        writeMitgliedskonto(m, param.faelligkeit, vzweck, z.getBetrag(), abrl,
            zahlungsweg == Zahlungsweg.BASISLASTSCHRIFT, konto,
            z.getBuchungsart(), z.getBuchungsklasseId(), zahlungsweg);
        monitor
            .setStatusText(String.format("Zusatzbetrag von %s, %s abgerechnet",
                m.getName(), m.getVorname()));
      }
      monitor.setPercentComplete(
          (int) ((double) count++ / (double) list.size() * 100d));
    }
  }

  private void abbuchenKursteilnehmer(AbrechnungSEPAParam param,
      Basislastschrift lastschrift, Abrechnungslauf abrl, Konto konto,
      ProgressMonitor monitor) throws ApplicationException, IOException
  {
    int count = 0;
    DBIterator<Kursteilnehmer> list = Einstellungen.getDBService()
        .createList(Kursteilnehmer.class);
    list.addFilter("abbudatum is null");
    while (list.hasNext())
    {
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
        zahler.setVerwendungszweck(kt.getVZweck1());
        lastschrift.add(zahler);
        kt.setAbbudatum(param.faelligkeit);
        kt.store();
        writeMitgliedskonto(kt, param.faelligkeit, kt.getVZweck1(),
            zahler.getBetrag().doubleValue(), abrl, true, konto, null, null,
            null);
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
      Basislastschrift lastschrift, ArrayList<Zahler> alle_zahler)
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
    for (Zahler zahler : alle_zahler)
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

  private void buchenHibiscus(AbrechnungSEPAParam param, ArrayList<Zahler> z)
      throws ApplicationException
  {
    try
    {
      SepaLastschrift[] lastschriften = new SepaLastschrift[z.size()];
      int sli = 0;
      Date d = new Date();
      for (Zahler za : z)
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

  private void writeMitgliedskonto(Object mitglied, Date datum, String zweck1,
      double betrag, Abrechnungslauf abrl, boolean haben, Konto konto,
      Buchungsart buchungsart, Long buchungsklasseId, Integer zahlungsweg)
      throws ApplicationException, RemoteException
  {
    Mitgliedskonto mk = null;
    if (mitglied != null
        && mitglied instanceof Mitglied) /*
                                          * Mitglied darf dann null sein, wenn
                                          * die Gegenbuchung geschrieben wird
                                          */
    {
      Mitglied mg = (Mitglied) mitglied;
      mk = (Mitgliedskonto) Einstellungen.getDBService()
          .createObject(Mitgliedskonto.class, null);
      mk.setAbrechnungslauf(abrl);
      if (zahlungsweg != null)
      {
        mk.setZahlungsweg(zahlungsweg);
      }
      else
      {
        mk.setZahlungsweg(mg.getZahlungsweg());
      }
      mk.setBetrag(betrag);
      mk.setDatum(datum);
      mk.setMitglied(mg);
      mk.setZweck1(zweck1);
      double steuersatz = 0d;
      if (buchungsart != null)
      {
        mk.setBuchungsart(buchungsart);
        steuersatz = buchungsart.getSteuersatz();
      }
      mk.setBuchungsklasseId(buchungsklasseId);
      // Set tax rate
      mk.setSteuersatz(steuersatz);
      // Set bill amount without taxes
      double nettobetrag = (steuersatz != 0d)
          ? (betrag / (1d + (steuersatz / 100d)))
          : betrag;
      mk.setNettobetrag(nettobetrag);
      // Set tax amount
      mk.setSteuerbetrag(betrag - nettobetrag);
      mk.store();
    }
    if (haben)
    {
      Buchung buchung = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
      buchung.setAbrechnungslauf(abrl);
      buchung.setBetrag(betrag);
      buchung.setDatum(datum);
      buchung.setKonto(konto);
      buchung.setName(mitglied != null
          ? Adressaufbereitung.getNameVorname((IAdresse) mitglied)
          : "JVerein");
      buchung.setZweck(zweck1);
      if (mk != null)
      {
        buchung.setMitgliedskonto(mk);
      }
      if (buchungsart != null)
      {
        buchung.setBuchungsartId(Long.valueOf(buchungsart.getID()));
      }
      buchung.setBuchungsklasseId(buchungsklasseId);
      buchung.store();
    }
  }

  private Konto getKonto() throws RemoteException, ApplicationException
  {
    if (Einstellungen.getEinstellung().getVerrechnungskontoId() == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gesetzt. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    Konto k = Einstellungen.getDBService().createObject(Konto.class,
        Einstellungen.getEinstellung().getVerrechnungskontoId().toString());
    if (k == null)
    {
      throw new ApplicationException(
          "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.");
    }
    return k;
  }

  private String getVerwendungszweck2(Mitglied m) throws RemoteException
  {
    String mitgliedname = (Einstellungen.getEinstellung()
        .getExterneMitgliedsnummer() ? m.getExterneMitgliedsnummer()
            : m.getID())
        + "/" + Adressaufbereitung.getNameVorname(m);
    return mitgliedname;
  }

  private boolean checkSEPA(Mitglied m, ProgressMonitor monitor)
      throws RemoteException
  {
    if (m.getZahlungsweg() == null
        || m.getZahlungsweg() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      return true;
    }
    // Ohne Mandat keine Lastschrift
    if (m.getMandatDatum() == Einstellungen.NODATE)
    {
      monitor.log(Adressaufbereitung.getNameVorname(m)
          + ": Kein Mandat-Datum vorhanden.");
      return false;
    }
    // Bei Mandaten �lter als 3 Jahre muss es eine Lastschrift
    // innerhalb der letzten 3 Jahre geben
    Calendar sepagueltigkeit = Calendar.getInstance();
    sepagueltigkeit.add(Calendar.MONTH, -36);
    if (m.getMandatDatum().before(sepagueltigkeit.getTime()))
    {
      Date letzte_lastschrift = m.getLetzteLastschrift();
      if (letzte_lastschrift == null
          || letzte_lastschrift.before(sepagueltigkeit.getTime()))
      {
        monitor.log(Adressaufbereitung.getNameVorname(m)
            + ": Das Mandat-Datum ist �lter als 36 Monate und es erfolgte keine Lastschrift in den letzten 36 Monaten.");
        return false;
      }
    }
    return true;
  }

}
