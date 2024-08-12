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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstyp;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.view.IAuswertung;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


public class MitgliedAdresslistePDF implements IAuswertung
{

  private MitgliedControl control;

  private Adresstyp adresstyp;

  private String subtitle = "";

  private TreeMap<String, String> params;
  
  String zusatzfeld = null;
  
  String zusatzfelder = null;

  public MitgliedAdresslistePDF(MitgliedControl control)
  {
    this.control = control;
  }

  @Override
  public void beforeGo() throws RemoteException
  {
    params = new TreeMap<>();
    
    zusatzfeld = control.getAdditionalparamprefix1();
    zusatzfelder = control.getAdditionalparamprefix2();
    
    if (control.isSuchAdresstypActive())
    {
      adresstyp = (Adresstyp) control.getSuchAdresstyp(Mitgliedstyp.NICHTMITGLIED).getValue();
    }
    else
    {
      DBIterator<Adresstyp> it = Einstellungen.getDBService()
          .createList(Adresstyp.class);
      it.addFilter("jvereinid=1");
      adresstyp = (Adresstyp) it.next();
    }

    if (control.isMitgliedStatusAktiv())
    {
      params.put("Status", (String) control.getMitgliedStatus().getValue());
    }
    if (control.isEigenschaftenAuswahlAktiv())
    {
      String eig = control.getEigenschaftenAuswahl().getText();
      if (eig.length() > 0)
      {
        params.put("Eigenschaften", eig);
      }
    }
    if (control.isSuchExterneMitgliedsnummerActive() && control.getSuchExterneMitgliedsnummer() != null)
    {
      String val = control.getSuchExterneMitgliedsnummer().getValue().toString();
      if (val.length() > 0) {
        params.put("Externe Mitgliedsnummer ", val);
      }
    }
    if (control.isGeburtsdatumvonAktiv() && control.getGeburtsdatumvon().getValue() != null)
    {
      Date d = (Date) control.getGeburtsdatumvon().getValue();
      params.put("Geburtsdatum von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isGeburtsdatumbisAktiv() && control.getGeburtsdatumbis().getValue() != null)
    {
      Date d = (Date) control.getGeburtsdatumbis().getValue();
      params.put("Geburtsdatum bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isEintrittvonAktiv() && control.getEintrittvon().getValue() != null)
    {
      Date d = (Date) control.getEintrittvon().getValue();
      params.put("Eintritt von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isEintrittbisAktiv() && control.getEintrittbis().getValue() != null)
    {
      Date d = (Date) control.getEintrittbis().getValue();
      params.put("Eintritt bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isAustrittvonAktiv() && control.getAustrittvon().getValue() != null)
    {
      Date d = (Date) control.getAustrittvon().getValue();
      params.put("Austritt von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isAustrittbisAktiv() && control.getAustrittbis().getValue() != null)
    {
      Date d = (Date) control.getAustrittbis().getValue();
      params.put("Austritt bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isSterbedatumvonAktiv() && control.getSterbedatumvon().getValue() != null)
    {
      Date d = (Date) control.getSterbedatumvon().getValue();
      params.put("Sterbetag von", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isSterbedatumbisAktiv() && control.getSterbedatumbis().getValue() != null)
    {
      Date d = (Date) control.getSterbedatumbis().getValue();
      params.put("Sterbedatum bis", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isBeitragsgruppeAuswAktiv() && control.getBeitragsgruppeAusw().getValue() != null)
    {
      Beitragsgruppe bg = (Beitragsgruppe) control.getBeitragsgruppeAusw()
          .getValue();
      params.put("Beitragsgruppe", bg.getBezeichnung());
    }
    if (control.isMailauswahlAktiv())
    {
      int ma = (Integer) control.getMailauswahl().getValue();
      if (ma != MailAuswertungInput.ALLE)
      {
        params.put("Mail", control.getMailauswahl().getText());
      }
    }
    if (control.isSuchGeschlechtAktiv() && control.getSuchGeschlecht().getText() != null
        && !control.getSuchGeschlecht().getText().equals("Bitte auswählen"))
    {
      params.put("Geschlecht", control.getSuchGeschlecht().getText());
    }
    if (control.isStichtagAktiv() && control.getStichtag(false).getValue() != null)
    {
      Date d = (Date) control.getStichtag(false).getValue();
      params.put("Stichtag", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isZusatzfelderAuswahlAktiv())
    {
      int counter = control.getSettings().getInt(zusatzfelder + "counter", 0);
      for (int i = 1; i <= counter; i++)
      {
        String value = control.getSettings().getString(zusatzfeld + i + ".value", "");
        if (!value.equals("") && !value.equals("false"))
        {
          params.put(
            control.getSettings().getString(zusatzfeld + i + ".name", ""), value);
        }
      }
    }
    String ueberschrift = (String) control.getAuswertungUeberschrift()
        .getValue();
    if (ueberschrift.length() > 0)
    {
      subtitle = ueberschrift;
    }
  }

  @Override
  public void go(ArrayList<Mitglied> list, final File file)
      throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);

      Reporter report = new Reporter(fos, adresstyp.getBezeichnungPlural(),
          subtitle, list.size(), 20, 20, 20, 25);

      report.addHeaderColumn("Name", Element.ALIGN_CENTER, 60,
          BaseColor.LIGHT_GRAY);
      report.addHeaderColumn("Adresse", Element.ALIGN_CENTER,
          100, BaseColor.LIGHT_GRAY);
      report.addHeaderColumn("Telefon", Element.ALIGN_CENTER,
          50, BaseColor.LIGHT_GRAY);
      report.addHeaderColumn("Email", Element.ALIGN_CENTER,
          80, BaseColor.LIGHT_GRAY);
      if(Einstellungen.getEinstellung().getGeburtsdatumPflicht())
        report.addHeaderColumn("Geburtsdatum", Element.ALIGN_CENTER, 30,
            BaseColor.LIGHT_GRAY);
      report.createHeader(100, Element.ALIGN_CENTER);

      for (int i = 0; i < list.size(); i++)
      {
        Mitglied m = list.get(i);
        report.addColumn(Adressaufbereitung.getNameVorname(m),
            Element.ALIGN_LEFT);
        report.addColumn(Adressaufbereitung.getAnschrift(m),
            Element.ALIGN_LEFT);
        String telefon = "";
        if (m.getTelefonprivat() != null && m.getTelefonprivat().length() > 0)
        {
          telefon = m.getTelefonprivat();
        }
        if (m.getTelefondienstlich() != null
            && m.getTelefondienstlich().length() > 0)
        {
          telefon += "\n" + "dienstl: "
              + m.getTelefondienstlich();
        }
        if (m.getHandy() != null && m.getHandy().length() > 0)
        {
          telefon += "\n" + "Handy: " + m.getHandy();
        }
        report.addColumn(telefon, Element.ALIGN_LEFT);
        //Bei verwendung von mehreren Mailadresse im Fomar NAME:Mail1@xx.de,mail2@xx.de; trennen wir die Mailadressen
        String mail = m.getEmail();
        if(mail.indexOf(":") > 0)
        {
          mail = mail.substring(mail.indexOf(":")+1).replace(",", "\n").replace(";","").trim();
        }
        report.addColumn(mail, Element.ALIGN_LEFT);
        if(Einstellungen.getEinstellung().getGeburtsdatumPflicht())
          report.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT);
      }
      report.closeTable();

      report.add(new Paragraph(String.format("Anzahl %s: %d",
          adresstyp.getBezeichnungPlural(), list.size()), Reporter.getFreeSans(8)));

      report.close();
      GUI.getStatusBar().setSuccessText(
          String.format("Auswertung fertig. %d Sätze.", list.size()));
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  @Override
  public String getDateiname()
  {
    return "adressliste";
  }

  @Override
  public String getDateiendung()
  {
    return "PDF";
  }

  @Override
  public String toString()
  {
    return "Adressliste PDF";
  }

  @Override
  public boolean openFile()
  {
    return true;
  }
}
