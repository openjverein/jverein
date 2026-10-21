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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.SpendenbescheinigungQuery;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.io.SpendenbescheinigungAusgabe;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Datum;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungMailControl extends DruckMailControl
{

  public SpendenbescheinigungMailControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public String getInfoText(Object selection) throws RemoteException
  {
    Spendenbescheinigung[] spbArr = null;
    if (selection instanceof Spendenbescheinigung)
    {
      spbArr = new Spendenbescheinigung[] { (Spendenbescheinigung) selection };
    }
    else if (selection instanceof Spendenbescheinigung[])
    {
      spbArr = (Spendenbescheinigung[]) selection;
    }
    else
    {
      return "";
    }

    String text = "Es wurden " + spbArr.length
        + " Spendenbescheinigungen ausgewählt";
    String fehlen = "";
    String keinMitglied = "";

    for (Spendenbescheinigung spb : spbArr)
    {
      Mitglied m = spb.getMitglied();
      if (m != null && (m.getEmail() == null || m.getEmail().isEmpty()))
      {
        fehlen = fehlen + "\n - " + m.getName() + ", " + m.getVorname();
      }
      if (spb.getMitglied() == null)
      {
        keinMitglied = keinMitglied + "\n - " + spb.getZeile1() + ", "
            + spb.getZeile2() + ", " + spb.getZeile3();
      }
    }
    if (fehlen.length() > 0)
    {
      text += "\nFolgende Mitglieder haben keine Mailadresse:" + fehlen;
    }
    if (keinMitglied.length() > 0)
    {
      text += "\nFür folgende Spendenbescheinigungen existiert kein Mitglied und keine Mailadresse:"
          + keinMitglied;
    }

    return text;
  }

  public Button getStartButton(final Object currentObject)
  {
    return new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveFilterSettings();
          new SpendenbescheinigungAusgabe((String) mailtext.getValue(),
              (Adressblatt) adressblatt.getValue()).aufbereiten(
                  getDruckMailSpendenbescheinigungen(currentObject),
                  (Ausgabeart) getAusgabeart().getValue(), getBetreffString(),
                  getTxtString(), false,
                  (Boolean) Einstellungen
                      .getEinstellung(Property.UNTERSCHRIFTDRUCKEN),
                  (Boolean) versand.getValue());
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
  }

  private ArrayList<Spendenbescheinigung> getDruckMailSpendenbescheinigungen(
      Object object) throws RemoteException, ApplicationException
  {
    if (object instanceof Spendenbescheinigung)
    {
      object = new Spendenbescheinigung[] { (Spendenbescheinigung) object };
    }
    if (object instanceof Spendenbescheinigung[])
    {
      return new ArrayList<Spendenbescheinigung>(
          Arrays.asList((Spendenbescheinigung[]) object));
    }

    ArrayList<Spendenbescheinigung> spblist = new SpendenbescheinigungQuery(
        getFilter()).get();

    if (spblist == null || spblist.size() == 0)
    {
      throw new ApplicationException(
          "Für die gewählten Filterkriterien wurden keine Spendenbescheinigungen gefunden");
    }
    return spblist;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object object, String option)
      throws RemoteException, ApplicationException
  {
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = "";
    int ohneMail = 0;
    int ohneMitglied = 0;
    ArrayList<Spendenbescheinigung> spbs = getDruckMailSpendenbescheinigungen(
        object);
    Mitglied m;
    String dokument = "";
    for (Spendenbescheinigung spb : spbs)
    {
      m = spb.getMitglied();
      if (m != null)
      {
        String mail = m.getEmail();
        if ((mail == null || mail.isEmpty())
            && getAusgabeart().getValue() == Ausgabeart.MAIL)
        {
          ohneMail++;
        }
        dokument = "Spendenbescheinigung von "
            + Datum.formatDate(spb.getBescheinigungsdatum()) + " über "
            + Einstellungen.DECIMALFORMAT.format(spb.getBetrag()) + "€";
        liste.add(new DruckMailEmpfaengerEntry(dokument, mail, m.getName(),
            m.getVorname(), m.getMitgliedstyp()));
      }
      else
      {
        ohneMitglied++;
        dokument = "Spendenbescheinigung von "
            + Datum.formatDate(spb.getBescheinigungsdatum()) + " über "
            + Einstellungen.DECIMALFORMAT.format(spb.getBetrag())
            + "€ und Zeile 2: " + spb.getZeile2();
        liste.add(new DruckMailEmpfaengerEntry(dokument, null, null, null, ""));
      }
    }

    if (ohneMail > 0)
    {
      text = ohneMail + " Mitglied" + (ohneMail > 1 ? "er haben" : " hat")
          + " keine Mail Adresse";
      if (ohneMitglied == 0)
      {
        text += ".";
      }
      else
      {
        text += " und" + ohneMitglied + " Spendenbescheinigung"
            + (ohneMitglied > 1 ? "en haben" : " hat")
            + " kein Mitglied gesetzt.";
      }
    }
    else if (ohneMitglied > 0)
    {
      text = ohneMitglied + " Spendenbescheinigung"
          + (ohneMitglied > 1 ? "en haben" : " hat") + " kein Mitglied gesetzt";
      if (getAusgabeart().getValue() == Ausgabeart.MAIL)
      {
        text += ".";
      }
      else
      {
        text += ", " + (ohneMitglied > 1 ? "werden" : "wird")
            + " aber gedruckt.";
      }

    }
    return new DruckMailEmpfaenger(liste, text);
  }

  @Override
  public Map<Mitglied, Object> getDruckMailList()
      throws RemoteException, ApplicationException
  {
    Map<Mitglied, Object> map = new HashMap<>();
    ArrayList<Spendenbescheinigung> spbs = getDruckMailSpendenbescheinigungen(
        this.view.getCurrentObject());
    for (Spendenbescheinigung spb : spbs)
    {
      if (spb.getMitglied() != null)
      {
        map.put(spb.getMitglied(), spb);
      }
    }
    return map;
  }

  @Override
  protected String getTableTitle()
  {
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    return null;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    return null;
  }

}
