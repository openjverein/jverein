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
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.control.VorZurueckControl;
import de.jost_net.JVerein.gui.menu.WiedervorlageMenu;
import de.jost_net.JVerein.gui.view.WiedervorlageDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class WiedervorlageList extends JVereinTablePart
{

  private AutoUpdateTablePart wiedervorlageList;

  private FilterControl control;

  public WiedervorlageList(Action action, FilterControl control)
  {
    super(action);
    this.control = control;
  }

  public AutoUpdateTablePart getWiedervorlageList()
      throws RemoteException, ApplicationException
  {
    if (wiedervorlageList != null)
    {
      return wiedervorlageList;
    }
    DBIterator<Wiedervorlage> wiedervorlagen = getIterator();

    wiedervorlageList = new AutoUpdateTablePart(wiedervorlagen, null);
    wiedervorlageList.addColumn("Nr", "id-int");
    wiedervorlageList.addColumn("Name", "mitglied");
    wiedervorlageList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.addColumn("Vermerk", "vermerk");
    wiedervorlageList.addColumn("Erledigung", "erledigung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.setContextMenu(new WiedervorlageMenu(wiedervorlageList));
    wiedervorlageList.setMulti(true);
    wiedervorlageList.setAction(
        new EditAction(WiedervorlageDetailView.class, wiedervorlageList));
    VorZurueckControl.setObjektListe(null, null);

    return wiedervorlageList;
  }

  private DBIterator<Wiedervorlage> getIterator()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Wiedervorlage> wiedervorlagen = service
        .createList(Wiedervorlage.class);

    wiedervorlagen.join("mitglied");
    wiedervorlagen.addFilter("mitglied.id = wiedervorlage.mitglied");

    for (Entry<Filter, Object> entry : control.getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NAME:
          String suchName = "%" + value.toString().toLowerCase() + "%";
          wiedervorlagen.addFilter(
              "(lower(name) like ? " + "or lower(vorname) like ?)", suchName,
              suchName);
          break;
        case DATUM_VON:
          wiedervorlagen.addFilter("datum >= ?", value);
          break;
        case DATUM_BIS:
          wiedervorlagen.addFilter("datum <= ?", value);
          break;
        case VERMERK:
          wiedervorlagen.addFilter("(lower(vermerk) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case OHNE_ERLEDIGUNG:
          if ((Boolean) value)
          {
            wiedervorlagen.addFilter("erledigung IS NULL");
          }
          break;
        case DATUM_ERLEDIGUNG_VON:
          wiedervorlagen.addFilter("erledigung >= ?", value);
          break;
        case DATUM_ERLEDIGUNG_BIS:
          wiedervorlagen.addFilter("erledigung <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    return wiedervorlagen;
  }

  public void refresh() throws ApplicationException
  {
    try
    {
      if (wiedervorlageList == null)
      {
        return;
      }
      DBIterator<Wiedervorlage> wiedervorlagen = getIterator();
      wiedervorlageList.removeAll();
      while (wiedervorlagen.hasNext())
      {
        wiedervorlageList.addItem(wiedervorlagen.next());
      }
      wiedervorlageList.sort();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e);
    }
  }
}
