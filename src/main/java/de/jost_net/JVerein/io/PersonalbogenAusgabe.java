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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.control.PersonalbogenControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.DruckMailExportDialog;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedfoto;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class PersonalbogenAusgabe extends AbstractAusgabe
{
  private FileOutputStream fos;

  private Reporter rpt;

  private PersonalbogenControl control;

  private ExportLayoutParam params;

  private Font font;

  public PersonalbogenAusgabe(PersonalbogenControl control)
  {
    this.control = control;
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Mitglied m = (Mitglied) object;
    String filename = m.getID() + "#personalbogen# #";
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename + "#Personalbogen";
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Mitglied m = (Mitglied) object;
    Map<String, Object> map = new MitgliedMap().getMap(m, null);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      return VorlageUtil.getName(VorlageTyp.PERSONALBOGEN_MITGLIED_DATEINAME,
          object);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.PERSONALBOGEN_DATEINAME);
    }
  }

  @Override
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object)
      throws IOException, DocumentException, ApplicationException
  {
    if (params == null)
    {
      DruckMailExportDialog d = new DruckMailExportDialog("Personalbogen",
          ExportArt.PDF, "", "", null);
      try
      {
        if (!d.open())
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
        String text = "Fehler beim Erstellen des Reports.";
        Logger.error(text, e);
        throw new ApplicationException(text);
      }
      params = d.getParams();
      font = new Font(params.getFontHeader());
      font.setSize(12);
    }
    generierePersonalbogen(file, (Mitglied) object, control);
  }

  @Override
  protected void closeDocument(FormularAufbereitung formularaufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    rpt.close();
    fos.close();
    rpt = null;
    fos = null;
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    // Hier gibt es kein Formular
    return null;
  }

  private void generierePersonalbogen(File file, Mitglied m,
      PersonalbogenControl control) throws IOException, ApplicationException
  {
    try
    {
      if (fos == null)
      {
        fos = new FileOutputStream(file);
      }

      if (rpt == null)
      {
        rpt = new Reporter(fos, params);
      }
      else
      {
        rpt.resetPageCount();
        rpt.newPage();
      }

      String title = VorlageUtil.getName(VorlageTyp.PERSONALBOGEN_TITEL, m);
      String subtitle = VorlageUtil.getName(VorlageTyp.PERSONALBOGEN_SUBTITEL,
          m);
      Paragraph pTitle = new Paragraph(title, Reporter.getFreeSansBold(13));
      pTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(pTitle);
      Paragraph psubTitle = new Paragraph(subtitle,
          Reporter.getFreeSansBold(10));
      psubTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(psubTitle);

      generiereMitglied(rpt, m);

      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG)
          && control.getZusatzbetrag() != null
          && (boolean) control.getZusatzbetrag().getValue())
      {
        generiereZusatzbetrag(rpt, m);
      }
      if (control.getMitgliedskonto() != null
          && (boolean) control.getMitgliedskonto().getValue())
      {
        generiereMitgliedskonto(rpt, m);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.VERMERKE)
          && ((m.getVermerk1() != null && m.getVermerk1().length() > 0)
              || (m.getVermerk2() != null && m.getVermerk2().length() > 0))
          && control.getVermerk() != null
          && (boolean) control.getVermerk().getValue())
      {
        generiereVermerke(rpt, m);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.WIEDERVORLAGE)
          && control.getWiedervorlage() != null
          && (boolean) control.getWiedervorlage().getValue())
      {
        generiereWiedervorlagen(rpt, m);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE)
          && control.getLehrgang() != null
          && (boolean) control.getLehrgang().getValue())
      {
        generiereLehrgaenge(rpt, m);
      }
      if (control.getZusatzfelder() != null
          && (boolean) control.getZusatzfelder().getValue())
      {
        generiereZusatzfelder(rpt, m);
      }
      if (control.getEigenschaften() != null
          && (boolean) control.getEigenschaften().getValue())
      {
        generiereEigenschaften(rpt, m);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ)
          && control.getArbeitseinsatz() != null
          && (boolean) control.getArbeitseinsatz().getValue())
      {
        generiereArbeitseinsaetze(rpt, m);
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN)
          && control.getSpendenbescheinigung() != null
          && (boolean) control.getSpendenbescheinigung().getValue())
      {
        generiereSpendenbescheinigungen(rpt, m);
      }
    }
    catch (Exception re)
    {
      Logger.error("Fehler", re);
      GUI.getStatusBar().setErrorText(re.getMessage());
      throw new ApplicationException(re);
    }
  }

  private void generiereMitglied(Reporter rpt, Mitglied m)
      throws DocumentException, MalformedURLException, IOException,
      ApplicationException
  {
    rpt.addHeaderColumn("Feld", Element.ALIGN_LEFT, 50, params.getColorHeader(),
        params.getFontHeader());
    rpt.addHeaderColumn("Inhalt", Element.ALIGN_LEFT, 140,
        params.getColorHeader(), params.getFontHeader());
    rpt.createHeader();
    DBIterator<Mitgliedfoto> it = Einstellungen.getDBService()
        .createList(Mitgliedfoto.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    if (it.size() > 0)
    {
      Mitgliedfoto foto = it.next();
      if (foto.getFoto() != null)
      {
        rpt.addColumn("Foto", Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(foto.getFoto(), 100, 100, Element.ALIGN_RIGHT);
      }
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
    {
      rpt.addColumn("Ext. Mitgliedsnummer", Element.ALIGN_LEFT,
          params.getFontNormal());
      rpt.addColumn(m.getExterneMitgliedsnummer() != null
          ? m.getExterneMitgliedsnummer() + ""
          : "", Element.ALIGN_LEFT, params.getFontNormal());
    }
    else
    {
      rpt.addColumn("Mitgliedsnummer", Element.ALIGN_LEFT,
          params.getFontNormal());
      rpt.addColumn(m.getID(), Element.ALIGN_LEFT, params.getFontNormal());
    }
    rpt.addColumn("Name, Vorname", Element.ALIGN_LEFT, params.getFontNormal());
    rpt.addColumn(Adressaufbereitung.getNameVorname(m), Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn("Anschrift", Element.ALIGN_LEFT, params.getFontNormal());
    rpt.addColumn(Adressaufbereitung.getAnschrift(m), Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn("Geburtsdatum", Element.ALIGN_LEFT, params.getFontNormal());
    rpt.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT,
        params.getFontNormal());
    if (m.getSterbetag() != null)
    {
      rpt.addColumn("Sterbetag", Element.ALIGN_LEFT, params.getFontNormal());
      rpt.addColumn(m.getSterbetag(), Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    rpt.addColumn("Geschlecht", Element.ALIGN_LEFT, params.getFontNormal());
    rpt.addColumn(m.getGeschlecht(), Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn("Kommunikation", Element.ALIGN_LEFT, params.getFontNormal());
    String kommunikation = "";
    if (m.getTelefonprivat().length() != 0)
    {
      kommunikation += "privat: " + m.getTelefonprivat();
    }
    if (m.getTelefondienstlich().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "dienstlich: " + m.getTelefondienstlich();
    }
    if (m.getHandy().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "Handy: " + m.getHandy();
    }
    if (m.getEmail().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "Email: " + m.getEmail();
    }
    rpt.addColumn(kommunikation, Element.ALIGN_LEFT, params.getFontNormal());
    if (m.getMitgliedstyp().getID().equals(Mitgliedstyp.MITGLIED))
    {
      rpt.addColumn("Eintritt", Element.ALIGN_LEFT, params.getFontNormal());
      rpt.addColumn(m.getEintritt(), Element.ALIGN_LEFT,
          params.getFontNormal());
      printBeitragsgruppe(rpt, m, m.getBeitragsgruppe(), false);
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN))
      {
        DBIterator<SekundaereBeitragsgruppe> sb = Einstellungen.getDBService()
            .createList(SekundaereBeitragsgruppe.class);
        sb.addFilter("mitglied = ?", m.getID());
        while (sb.hasNext())
        {
          SekundaereBeitragsgruppe sebe = sb.next();
          printBeitragsgruppe(rpt, m, sebe.getBeitragsgruppe(), true);
        }
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.INDIVIDUELLEBEITRAEGE))
      {
        rpt.addColumn("Individueller Beitrag", Element.ALIGN_LEFT,
            params.getFontNormal());
        if (m.getIndividuellerBeitrag() != null)
        {
          rpt.addColumn(
              Einstellungen.DECIMALFORMAT.format(m.getIndividuellerBeitrag())
                  + " EUR",
              Element.ALIGN_LEFT, params.getFontNormal());
        }
        else
        {
          rpt.addColumn("", Element.ALIGN_LEFT);
        }
      }
      if (m.getBeitragsgruppe()
          .getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        DBIterator<Mitglied> itbg = Einstellungen.getDBService()
            .createList(Mitglied.class);
        itbg.addFilter("zahlerid = ?", m.getID());
        if (itbg.hasNext())
        {
          rpt.addColumn("Familienmitglieder im Familienverband",
              Element.ALIGN_LEFT, params.getFontNormal());
          String familienmitglieder = "";
          while (itbg.hasNext())
          {
            Mitglied mz = itbg.next();
            if (familienmitglieder.length() > 0)
            {
              familienmitglieder += "\n";
            }
            familienmitglieder += Adressaufbereitung.getNameVorname(mz);
          }
          rpt.addColumn(familienmitglieder, Element.ALIGN_LEFT,
              params.getFontNormal());
        }
      }
      else if (m.getBeitragsgruppe()
          .getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        Mitglied mfa = (Mitglied) Einstellungen.getDBService()
            .createObject(Mitglied.class, m.getVollZahlerID() + "");
        rpt.addColumn("Vollzahlendes Familienmitglied", Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(Adressaufbereitung.getNameVorname(mfa),
            Element.ALIGN_LEFT, params.getFontNormal());
      }
      rpt.addColumn("Austritts-/Kündigungsdatum", Element.ALIGN_LEFT,
          params.getFontNormal());
      String akdatum = "";
      if (m.getAustritt() != null)
      {
        akdatum += new JVDateFormatTTMMJJJJ().format(m.getAustritt());
      }
      if (m.getKuendigung() != null)
      {
        if (akdatum.length() != 0)
        {
          akdatum += " / ";
        }
        akdatum += new JVDateFormatTTMMJJJJ().format(m.getKuendigung());
      }
      rpt.addColumn(akdatum, Element.ALIGN_LEFT, params.getFontNormal());
    }
    rpt.addColumn("Zahlungsweg", Element.ALIGN_LEFT, params.getFontNormal());
    rpt.addColumn(Zahlungsweg.get(m.getZahlungsweg()), Element.ALIGN_LEFT,
        params.getFontNormal());
    if (m.getBic() != null && m.getBic().length() > 0
        && m.getIban().length() > 0)
    {
      rpt.addColumn("Bankverbindung", Element.ALIGN_LEFT,
          params.getFontNormal());
      rpt.addColumn(m.getBic() + "/" + m.getIban(), Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
    {
      rpt.addColumn("Mandat", Element.ALIGN_LEFT, params.getFontNormal());
      rpt.addColumn(
          m.getMandatID() + " vom "
              + new JVDateFormatTTMMJJJJ().format(m.getMandatDatum()),
          Element.ALIGN_LEFT, params.getFontNormal());
    }
    if (m.getKontoinhaber() != null && !m.getKontoinhaber().isBlank())
    {
      rpt.addColumn("Kontoinhaber", Element.ALIGN_LEFT, params.getFontNormal());
      rpt.addColumn(m.getKontoinhaber(), Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    if (m.getAbweichenderZahler() != null)
    {
      rpt.addColumn("Abweichender Zahler", Element.ALIGN_LEFT,
          params.getFontNormal());
      rpt.addColumn(
          Adressaufbereitung.getIdNameVorname(m.getAbweichenderZahler()),
          Element.ALIGN_LEFT, params.getFontNormal());
    }
    DBIterator<Mitglied> itZahler = Einstellungen.getDBService()
        .createList(Mitglied.class);
    itZahler.addFilter("altzahler = ?", m.getID());
    if (itZahler.hasNext())
    {
      rpt.addColumn("Zahlt für", Element.ALIGN_LEFT, params.getFontNormal());
      String zahltfuer = "";
      while (itZahler.hasNext())
      {
        Mitglied mz = itZahler.next();
        if (zahltfuer.length() > 0)
        {
          zahltfuer += "\n";
        }
        zahltfuer += Adressaufbereitung.getNameVorname(mz);
      }
      rpt.addColumn(zahltfuer, Element.ALIGN_LEFT, params.getFontNormal());
    }
    rpt.addColumn("Datum Erstspeicherung", Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn(m.getEingabedatum(), Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn("Datum letzte Änderung", Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.addColumn(m.getLetzteAenderung(), Element.ALIGN_LEFT,
        params.getFontNormal());
    rpt.closeTable();
  }

  private void printBeitragsgruppe(Reporter rpt, Mitglied m, Beitragsgruppe bg,
      boolean sek) throws RemoteException, ApplicationException
  {
    rpt.addColumn((sek ? "Sekundäre " : "") + "Beitragsgruppe",
        Element.ALIGN_LEFT, params.getFontNormal());
    String beitragsgruppe = bg.getBezeichnung() + " - "
        + Einstellungen.DECIMALFORMAT.format(BeitragsUtil.getBeitrag(
            Beitragsmodel.getByKey(
                (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)),
            m.getZahlungstermin(), m.getZahlungsrhythmus(), bg, new Date(), m))
        + " EUR";
    rpt.addColumn(beitragsgruppe, Element.ALIGN_LEFT, params.getFontNormal());
  }

  private void generiereZusatzbetrag(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Zusatzbetrag> it = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("ORDER BY faelligkeit DESC");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Zusatzbetrag", font));
      rpt.addHeaderColumn("Start", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("nächste Fäll.", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("letzte Ausf.", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Intervall", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Ende", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Buchungstext", Element.ALIGN_LEFT, 60,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Zusatzbetrag z = it.next();
        rpt.addColumn(z.getStartdatum(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getFaelligkeit(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getAusfuehrung(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getIntervallText(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getEndedatum(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getBuchungstext(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(z.getBetrag(), params.getFontNormal(),
            params.getNegativRot());
      }
    }
    rpt.closeTable();
  }

  private void generiereMitgliedskonto(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Sollbuchung> sollbIt = Einstellungen.getDBService()
        .createList(Sollbuchung.class);
    sollbIt.addFilter(Sollbuchung.MITGLIED + " = ?",
        new Object[] { m.getID() });
    sollbIt.setOrder("order by " + Sollbuchung.DATUM + " desc");
    if (sollbIt.size() > 0)
    {
      rpt.add(new Paragraph("Mitgliedskonto", font));
      rpt.addHeaderColumn("Text", Element.ALIGN_LEFT, 12,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Zweck", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Zahlungsweg", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Betrag", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (sollbIt.hasNext())
      {
        Sollbuchung sollb = sollbIt.next();
        rpt.addColumn("Soll", Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(sollb.getDatum(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(sollb.getZweck1(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(Zahlungsweg.get(sollb.getZahlungsweg()),
            Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(sollb.getBetrag(), params.getFontNormal(),
            params.getNegativRot());
        DBIterator<Buchung> it2 = Einstellungen.getDBService()
            .createList(Buchung.class);
        it2.addFilter(Buchung.SOLLBUCHUNG + " = ?",
            new Object[] { sollb.getID() });
        it2.setOrder("order by datum desc");
        while (it2.hasNext())
        {
          Buchung bu = it2.next();
          rpt.addColumn("Ist", Element.ALIGN_RIGHT, params.getFontNormal());
          rpt.addColumn(bu.getDatum(), Element.ALIGN_LEFT,
              params.getFontNormal());
          rpt.addColumn(bu.getZweck(), Element.ALIGN_LEFT,
              params.getFontNormal());
          rpt.addColumn("", Element.ALIGN_LEFT);
          rpt.addColumn(bu.getBetrag(), params.getFontNormal(),
              params.getNegativRot());
        }
      }
    }
    rpt.closeTable();

  }

  private void generiereVermerke(Reporter rpt, Mitglied m)
      throws DocumentException, RemoteException
  {
    rpt.add(new Paragraph("Vermerke", font));
    rpt.addHeaderColumn("Text", Element.ALIGN_LEFT, 100,
        params.getColorHeader(), params.getFontHeader());
    rpt.createHeader();
    if (m.getVermerk1() != null && m.getVermerk1().length() > 0)
    {
      rpt.addColumn(m.getVermerk1(), Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    if (m.getVermerk2() != null && m.getVermerk2().length() > 0)
    {
      rpt.addColumn(m.getVermerk2(), Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    rpt.closeTable();

  }

  private void generiereWiedervorlagen(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Wiedervorlage> it = Einstellungen.getDBService()
        .createList(Wiedervorlage.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("order by datum desc");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Wiedervorlage", font));
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Vermerk", Element.ALIGN_LEFT, 100,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Erledigung", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Wiedervorlage w = it.next();
        rpt.addColumn(w.getDatum(), Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(w.getVermerk(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(w.getErledigung(), Element.ALIGN_LEFT,
            params.getFontNormal());
      }
    }
    rpt.closeTable();

  }

  private void generiereLehrgaenge(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Lehrgang> it = Einstellungen.getDBService()
        .createList(Lehrgang.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("order by von");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Lehrgänge", font));
      rpt.addHeaderColumn("Lehrgangsart", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("am/vom", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("bis", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Veranstalter", Element.ALIGN_LEFT, 60,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Ergebnis", Element.ALIGN_LEFT, 60,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Lehrgang l = it.next();
        rpt.addColumn(l.getLehrgangsart().getBezeichnung(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(l.getVon(), Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(l.getBis(), Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(l.getVeranstalter(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(l.getErgebnis(), Element.ALIGN_LEFT,
            params.getFontNormal());
      }
    }
    rpt.closeTable();
  }

  private void generiereZusatzfelder(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Felddefinition> it = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    it.setOrder("order by label");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Zusatzfelder", font));
      rpt.addHeaderColumn("Feld", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Inhalt", Element.ALIGN_LEFT, 130,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Felddefinition fd = it.next();
        rpt.addColumn(fd.getLabel(), Element.ALIGN_LEFT,
            params.getFontNormal());
        DBIterator<Zusatzfelder> it2 = Einstellungen.getDBService()
            .createList(Zusatzfelder.class);
        it2.addFilter("mitglied = ? and felddefinition = ?",
            new Object[] { m.getID(), fd.getID() });
        if (it2.size() > 0)
        {
          Zusatzfelder zf = it2.next();
          rpt.addColumn(zf.getString(), Element.ALIGN_LEFT,
              params.getFontNormal());
        }
        else
        {
          rpt.addColumn("", Element.ALIGN_LEFT);
        }
      }
      rpt.closeTable();
    }
  }

  private void generiereEigenschaften(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        List<String> ids = new ArrayList<>();
        while (rs.next())
        {
          ids.add(rs.getString(1));
        }
        return ids;
      }
    };
    String sql = "select eigenschaften.id from eigenschaften, eigenschaft "
        + "where eigenschaften.eigenschaft = eigenschaft.id and mitglied = ? "
        + "order by eigenschaft.bezeichnung";
    @SuppressWarnings("unchecked")
    ArrayList<String> idliste = (ArrayList<String>) Einstellungen.getDBService()
        .execute(sql, new Object[] { m.getID() }, rs);
    if (idliste.size() > 0)
    {
      rpt.add(new Paragraph("Eigenschaften", font));
      rpt.addHeaderColumn("Eigenschaftengruppe", Element.ALIGN_LEFT, 100,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Eigenschaft", Element.ALIGN_LEFT, 100,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      for (String id : idliste)
      {
        DBIterator<Eigenschaften> it = Einstellungen.getDBService()
            .createList(Eigenschaften.class);
        it.addFilter("id = ?", new Object[] { id });
        while (it.hasNext())
        {
          Eigenschaften ei = it.next();
          rpt.addColumn(
              ei.getEigenschaft().getEigenschaftGruppe().getBezeichnung(),
              Element.ALIGN_LEFT, params.getFontNormal());
          rpt.addColumn(ei.getEigenschaft().getBezeichnung(),
              Element.ALIGN_LEFT, params.getFontNormal());
        }
      }
      rpt.closeTable();
    }
  }

  private void generiereArbeitseinsaetze(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Arbeitseinsatz> it = Einstellungen.getDBService()
        .createList(Arbeitseinsatz.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("ORDER BY datum");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Arbeitseinsätze", font));
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Stunden", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Bemerkung", Element.ALIGN_LEFT, 90,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Arbeitseinsatz ae = it.next();
        rpt.addColumn(ae.getDatum(), Element.ALIGN_LEFT,
            params.getFontNormal());
        rpt.addColumn(ae.getStunden(), params.getFontNormal(),
            params.getNegativRot());
        rpt.addColumn(ae.getBemerkung(), Element.ALIGN_LEFT,
            params.getFontNormal());
      }
    }
    rpt.closeTable();
  }

  private void generiereSpendenbescheinigungen(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Spendenbescheinigung> it = Einstellungen.getDBService()
        .createList(Spendenbescheinigung.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    // it.setOrder("ORDER BY datum");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Spendenbescheinigungen", font));
      rpt.addHeaderColumn("Spendenart", Element.ALIGN_LEFT, 30,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Bescheinigungsdatum", Element.ALIGN_LEFT, 60,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Spendedatum", Element.ALIGN_LEFT, 60,
          params.getColorHeader(), params.getFontHeader());
      rpt.addHeaderColumn("Betrag", Element.ALIGN_LEFT, 90,
          params.getColorHeader(), params.getFontHeader());
      rpt.createHeader();
      while (it.hasNext())
      {
        Spendenbescheinigung spb = it.next();
        rpt.addColumn(Spendenart.get(spb.getSpendenart()), Element.ALIGN_LEFT);
        rpt.addColumn(
            new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
            Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(new JVDateFormatTTMMJJJJ().format(spb.getSpendedatum()),
            Element.ALIGN_LEFT, params.getFontNormal());
        rpt.addColumn(Einstellungen.DECIMALFORMAT.format(spb.getBetrag()),
            Element.ALIGN_RIGHT, params.getFontNormal());
      }
    }
    rpt.closeTable();
  }
}
