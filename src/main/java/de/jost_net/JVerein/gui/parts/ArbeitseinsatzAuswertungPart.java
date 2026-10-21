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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Auswertung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;

public class ArbeitseinsatzAuswertungPart extends JVereinTablePart
{
  public static final String MITGLIED = "mitglied";

  public static final String MITGLIED_ID = "id";

  public static final String SOLLSTUNDEN = "sollstunden";

  public static final String ISTSTUNDEN = "iststunden";

  public static final String DIFFERENZ = "differenz";

  public static final String STUNDENSATZ = "stundensatz";

  public static final String GESAMTBETRAG = "gesamtbetrag";

  private Auswertung schluessel = Auswertung.MINDERLEISTUNG;

  private int year;

  private JVereinTablePart arbeitseinsatzueberpruefungList;

  public ArbeitseinsatzAuswertungPart(Action action, Auswertung schluessel,
      Integer year)
  {
    super(action);
    this.schluessel = schluessel;
    this.year = year;
  }

  public JVereinTablePart getArbeitseinsatzUeberpruefungList()
      throws RemoteException
  {
    if (arbeitseinsatzueberpruefungList != null)
    {
      return arbeitseinsatzueberpruefungList;
    }
    else
    {
      arbeitseinsatzueberpruefungList = new JVereinTablePart(getList(), null);
      arbeitseinsatzueberpruefungList.addColumn("Name", MITGLIED);
      arbeitseinsatzueberpruefungList.addColumn("Sollstunden", SOLLSTUNDEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      arbeitseinsatzueberpruefungList.addColumn("Iststunden", ISTSTUNDEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      arbeitseinsatzueberpruefungList.addColumn("Differenz", DIFFERENZ,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      arbeitseinsatzueberpruefungList.addColumn("Stundensatz", STUNDENSATZ,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      arbeitseinsatzueberpruefungList.addColumn("Gesamtbetrag", GESAMTBETRAG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      return arbeitseinsatzueberpruefungList;
    }
  }

  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();
    while (it.hasNext())
    {
      PseudoDBObject o = it.next();
      Double soll = o.getAttribute(SOLLSTUNDEN) == null ? 0d
          : o.getDouble(SOLLSTUNDEN);
      Double ist = o.getAttribute(ISTSTUNDEN) == null ? 0d
          : o.getDouble(ISTSTUNDEN);
      Double satz = o.getDouble(STUNDENSATZ);
      Mitglied mitglied = (Mitglied) Einstellungen.getDBService()
          .createObject(Mitglied.class, o.getAttribute(MITGLIED_ID).toString());
      o.setAttribute(DIFFERENZ, ist - soll);
      o.setAttribute(GESAMTBETRAG, satz == null ? null : (ist - soll) * satz);
      o.setAttribute(MITGLIED, mitglied);
      zeilen.add(o);
    }
    return zeilen;
  }

  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "mitglied");
    it.addColumn("mitglied.id as " + MITGLIED_ID);
    it.addColumn("arbeitseinsatzstunden as " + SOLLSTUNDEN);
    it.addColumn("beitragsgruppe.arbeitseinsatzbetrag as " + STUNDENSATZ);
    it.addColumn("sum(stunden) as " + ISTSTUNDEN);
    it.join("beitragsgruppe", "mitglied.beitragsgruppe = beitragsgruppe.id");
    it.leftJoin("arbeitseinsatz",
        "mitglied.id = arbeitseinsatz.mitglied and year(arbeitseinsatz.datum) = ? ",
        year);
    it.addFilter(
        "(mitglied.eintritt is null or year(mitglied.eintritt) <= ?) and "
            + "(mitglied.austritt is null or year(mitglied.austritt) >= ?) ",
        year, year);
    if (schluessel != Auswertung.MEHRLEISTUNG && schluessel != Auswertung.ALLE)
    {
      it.addFilter("beitragsgruppe.arbeitseinsatzstunden is not null and "
          + "beitragsgruppe.arbeitseinsatzstunden > 0");
    }
    it.addGroupBy("mitglied.id, year(arbeitseinsatz.datum)");
    if (schluessel == Auswertung.MINDERLEISTUNG)
    {
      it.addHaving((ISTSTUNDEN + " < " + SOLLSTUNDEN + " or " + ISTSTUNDEN
          + " is null"));
    }
    if (schluessel == Auswertung.PASSENDELEISTUNG)
    {
      it.addHaving(ISTSTUNDEN + " = " + SOLLSTUNDEN);
    }
    if (schluessel == Auswertung.MEHRLEISTUNG)
    {
      it.addHaving(ISTSTUNDEN + " > " + SOLLSTUNDEN);
    }
    if (schluessel == Auswertung.ALLE)
    {
      it.addHaving(ISTSTUNDEN + " > 0 or " + SOLLSTUNDEN + " > 0");
    }
    it.setOrder("Order by mitglied.name, mitglied.vorname, mitglied.id ");
    return it;
  }

  public void refreshList(Auswertung schluessel, int year)
  {
    try
    {
      if (arbeitseinsatzueberpruefungList != null)
      {
        this.schluessel = schluessel;
        this.year = year;
        arbeitseinsatzueberpruefungList.removeAll();
        for (PseudoDBObject o : getList())
        {
          arbeitseinsatzueberpruefungList.addItem(o);
        }
        arbeitseinsatzueberpruefungList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Tabellenaufbau", e);
    }
  }

}
