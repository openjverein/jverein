/**********************************************************************
 * basiert auf dem KontoAuswahlDialog aus Hibiscus
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

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der die automatisch ermittelten Zuordnungen zwischen Buchung und
 * Sollbuchung anzeigt
 */
public class BuchungenSollbuchungZuordnungVorschauDialog
    extends AbstractDialog<Object>
{
  private HashMap<String, HashMap<Integer, Integer>> zuordnungMap;

  private JVereinTablePart buchungList;

  public BuchungenSollbuchungZuordnungVorschauDialog(
      HashMap<String, HashMap<Integer, Integer>> zuordnungMap)
  {
    super(AbstractDialog.POSITION_CENTER);
    super.setSize(1400, 400);
    this.setTitle("Buchungszuordnung bestätigen");
    this.zuordnungMap = zuordnungMap;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    List<PseudoDBObject> list = new ArrayList<>();
    for (Entry<String, HashMap<Integer, Integer>> entry : zuordnungMap
        .entrySet())
    {
      String art = entry.getKey();
      for (Entry<Integer, Integer> map : entry.getValue().entrySet())
      {
        if (map.getValue() == null)
        {
          continue;
        }
        Buchung b = Einstellungen.getDBService().createObject(Buchung.class,
            map.getKey().toString());
        Sollbuchung s = Einstellungen.getDBService()
            .createObject(Sollbuchung.class, map.getValue().toString());

        Mitglied m = s.getMitglied();
        PseudoDBObject o = new PseudoDBObject();

        o.setAttribute("mitglied_vorname", m.getVorname());
        o.setAttribute("mitglied_name", m.getName());
        o.setAttribute("mitglied_iban", m.getIban());
        o.setAttribute("mitglied_id", m.getID());
        o.setAttribute("mitglied_externe_id", m.getExterneMitgliedsnummer());

        o.setAttribute("sollbuchung_datum", s.getDatum());
        o.setAttribute("sollbuchung_zweck", s.getZweck1());
        o.setAttribute("sollbuchung_abrechnungslauf", s.getAbrechnungslauf());

        o.setAttribute("buchung_id", b.getID());
        o.setAttribute("buchung_name", b.getName());
        o.setAttribute("buchung_zweck", b.getZweck());
        o.setAttribute("buchung_datum", b.getDatum());
        o.setAttribute("buchung_betrag", b.getBetrag());

        o.setAttribute("art", art);

        // Objecte für die Zuordnung speichern
        o.setAttribute("buchung", b);
        o.setAttribute("sollbuchung", s);
        list.add(o);
      }
    }

    buchungList = new JVereinTablePart(list, null);

    buchungList.addColumn("Zuordnen", "");

    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
    {
      buchungList.addColumn("Ext. Mitgliedsnummer", "mitglied_externe_id");
    }
    else
    {
      buchungList.addColumn("Mitgliedsnummer", "mitglied_id");
    }

    buchungList.addColumn("Vorname", "mitglied_vorname");
    buchungList.addColumn("Name", "mitglied_name");
    buchungList.addColumn("IBAN", "mitglied_iban", new IBANFormatter());

    buchungList.addColumn("Buchungsnummer", "buchung_id");
    buchungList.addColumn("Name Buchung", "buchung_name");
    buchungList.addColumn("Betrag", "buchung_betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    buchungList.addColumn("Verwendungszweck", "buchung_zweck",
        value -> value == null ? null
            : value.toString().replaceAll("\r\n", " ").replaceAll("\r", " ")
                .replaceAll("\n", " "));
    buchungList.addColumn("Buchungsdatum", "buchung_datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    buchungList.addColumn("Abrechnungslaufnummer",
        "sollbuchung_abrechnungslauf");
    buchungList.addColumn("Sollbuchung Zweck", "sollbuchung_zweck",
        value -> value == null ? null
            : value.toString().replaceAll("\r\n", " ").replaceAll("\r", " ")
                .replaceAll("\n", " "));
    buchungList.addColumn("Sollbuchung Datum", "sollbuchung_datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    buchungList.addColumn("Zuordnungsart", "art");

    buchungList.setRememberColWidths(true);
    buchungList.setCheckable(true);
    buchungList.setChecked(list.toArray(), true);
    buchungList.paint(parent);

    ButtonArea b = new ButtonArea();

    b.addButton("Zuordnen", context -> store(), null, false, "ok.png");

    b.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");

    b.paint(parent);
  }

  private void store() throws ApplicationException
  {
    try
    {
      DBTransaction.starten();
      for (Object item : buchungList.getItems())
      {
        PseudoDBObject o = (PseudoDBObject) item;
        Buchung b = (Buchung) o.getAttribute("buchung");
        Sollbuchung s = (Sollbuchung) o.getAttribute("sollbuchung");

        SplitbuchungsContainer.autoSplit(b, s, true);
      }

      GUI.getCurrentView().reload();
      GUI.getStatusBar().setSuccessText(buchungList.getItems().size()
          + " Buchungen wurden erfolgreich zugeordnet.");

      DBTransaction.commit();
      close();
    }
    catch (RemoteException e)
    {
      DBTransaction.rollback();
      Logger.error("error while assignment", e);
      throw new ApplicationException(
          "Fehler bei der Durchführung der Zuordnung", e);
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  @Override
  protected void onEscape()
  {
    // Keine OperationCancelledException werfen
    close();
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
}
