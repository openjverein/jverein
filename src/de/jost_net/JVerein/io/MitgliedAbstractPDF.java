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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.view.IAuswertung;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

public abstract class MitgliedAbstractPDF implements IAuswertung
{

  protected MitgliedControl control;

  protected Mitgliedstyp mitgliedstyp;

  protected String subtitle = "";

  protected TreeMap<String, String> params;

  protected String zusatzfeld = null;

  protected String zusatzfelder = null;

  public MitgliedAbstractPDF(MitgliedControl control)
  {
    this.control = control;
    this.zusatzfeld = control.getAdditionalparamprefix1();
    this.zusatzfelder = control.getAdditionalparamprefix2();
  }

  @Override
  public String getDateiname()
  {
    return null;
  }

  @Override
  public String getDateiendung()
  {
    return null;
  }

  @Override
  public void beforeGo() throws RemoteException
  {
    if (control.isSuchMitgliedstypActive())
    {
      mitgliedstyp = (Mitgliedstyp) control
          .getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED).getValue();
    }
    else
    {
      DBIterator<Mitgliedstyp> mtIt = Einstellungen.getDBService()
          .createList(Mitgliedstyp.class);
      mtIt.addFilter(Mitgliedstyp.JVEREINID + " = " + Mitgliedstyp.MITGLIED);
      mitgliedstyp = (Mitgliedstyp) mtIt.next();
    }
    initParams();
    String ueberschrift = (String) control.getAuswertungUeberschrift()
        .getValue();
    if (ueberschrift.length() > 0)
    {
      subtitle = ueberschrift;
    }
  }

  @Override
  public void go(ArrayList<Mitglied> list, File file)
      throws ApplicationException
  {
    // Wird in abgeleiteter Klasse implementiert

  }

  @Override
  public boolean openFile()
  {
    return false;
  }

  public void initParams() throws RemoteException
  {

    params = new TreeMap<>();

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
    if (control.isSuchExterneMitgliedsnummerActive()
        && control.getSuchExterneMitgliedsnummer() != null)
    {
      String val = control.getSuchExterneMitgliedsnummer().getValue()
          .toString();
      if (val.length() > 0)
      {
        params.put("Externe Mitgliedsnummer ", val);
      }
    }
    if (control.isGeburtsdatumvonAktiv()
        && control.getGeburtsdatumvon().getValue() != null)
    {
      Date d = (Date) control.getGeburtsdatumvon().getValue();
      params.put("Geburtsdatum von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isGeburtsdatumbisAktiv()
        && control.getGeburtsdatumbis().getValue() != null)
    {
      Date d = (Date) control.getGeburtsdatumbis().getValue();
      params.put("Geburtsdatum bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isEintrittvonAktiv()
        && control.getEintrittvon().getValue() != null)
    {
      Date d = (Date) control.getEintrittvon().getValue();
      params.put("Eintritt von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isEintrittbisAktiv()
        && control.getEintrittbis().getValue() != null)
    {
      Date d = (Date) control.getEintrittbis().getValue();
      params.put("Eintritt bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isAustrittvonAktiv()
        && control.getAustrittvon().getValue() != null)
    {
      Date d = (Date) control.getAustrittvon().getValue();
      params.put("Austritt von ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isAustrittbisAktiv()
        && control.getAustrittbis().getValue() != null)
    {
      Date d = (Date) control.getAustrittbis().getValue();
      params.put("Austritt bis ", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isSterbedatumvonAktiv()
        && control.getSterbedatumvon().getValue() != null)
    {
      Date d = (Date) control.getSterbedatumvon().getValue();
      params.put("Sterbetag von", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isSterbedatumbisAktiv()
        && control.getSterbedatumbis().getValue() != null)
    {
      Date d = (Date) control.getSterbedatumbis().getValue();
      params.put("Sterbedatum bis", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isBeitragsgruppeAuswAktiv()
        && control.getBeitragsgruppeAusw().getValue() != null)
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
    if (control.isSuchGeschlechtAktiv()
        && control.getSuchGeschlecht().getValue() != null)
    {
      params.put("Geschlecht", control.getSuchGeschlecht().getText());
    }
    if (control.isStichtagAktiv()
        && control.getStichtag(false).getValue() != null)
    {
      Date d = (Date) control.getStichtag(false).getValue();
      params.put("Stichtag", new JVDateFormatTTMMJJJJ().format(d));
    }
    if (control.isZusatzfelderAuswahlAktiv())
    {
      int counter = control.getSettings().getInt(zusatzfelder + "counter", 0);
      for (int i = 1; i <= counter; i++)
      {
        String value = control.getSettings()
            .getString(zusatzfeld + i + ".value", "");
        if (!value.equals("") && !value.equals("false"))
        {
          params.put(
              control.getSettings().getString(zusatzfeld + i + ".name", ""),
              value);
        }
      }
    }
  }
}
