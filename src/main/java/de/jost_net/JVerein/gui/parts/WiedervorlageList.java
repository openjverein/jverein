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
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.WiedervorlageAction;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.menu.WiedervorlageMenu;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;

public class WiedervorlageList extends TablePart implements Part
{

  private TablePart wiedervorlageList;
  
  private FilterControl control;

  public WiedervorlageList(Action action, FilterControl control)
  {
    super(action);
    this.control = control;
  }

  public Part getWiedervorlageList() throws RemoteException
  {
    
    DBIterator<Wiedervorlage> wiedervorlagen = getIterator();
    if (wiedervorlageList == null)
    {
      wiedervorlageList = new TablePart(wiedervorlagen,
          new WiedervorlageAction(null));
      wiedervorlageList.addColumn("Name", "mitglied");
      wiedervorlageList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      wiedervorlageList.addColumn("Vermerk", "vermerk");
      wiedervorlageList.addColumn("Erledigung", "erledigung",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      wiedervorlageList
          .setContextMenu(new WiedervorlageMenu(wiedervorlageList));
      wiedervorlageList.setRememberColWidths(true);
      wiedervorlageList.setRememberOrder(true);
      wiedervorlageList.addFeature(new FeatureSummary());
    }
    else
    {
      wiedervorlageList.removeAll();
      while (wiedervorlagen.hasNext())
      {
        wiedervorlageList.addItem(wiedervorlagen.next());
      }
      wiedervorlageList.sort();
    }
    return wiedervorlageList;
  }
  
  private DBIterator<Wiedervorlage> getIterator() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Wiedervorlage> wiedervorlagen = service
        .createList(Wiedervorlage.class);
    
    wiedervorlagen.join("mitglied");
    wiedervorlagen.addFilter("mitglied.id = wiedervorlage.mitglied");
    
    if (control.isSuchnameAktiv() && control.getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) control.getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        String suchName = "%" + tmpSuchname.toLowerCase() + "%";
        wiedervorlagen.addFilter("(lower(name) like ? "
            + "or lower(vorname) like ?)" , 
            new Object[] { suchName, suchName });
      }
    }
    if (control.isDatumvonAktiv() && control.getDatumvon().getValue() != null)
    {
      wiedervorlagen.addFilter("datum >= ?",
          new Object[] { (Date) control.getDatumvon().getValue() });
    }
    if (control.isDatumbisAktiv() && control.getDatumbis().getValue() != null)
    {
      wiedervorlagen.addFilter("datum <= ?",
          new Object[] { (Date) control.getDatumbis().getValue() });
    }
    if (control.isSuchtextAktiv() && control.getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) control.getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        wiedervorlagen.addFilter("(lower(vermerk) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%"});
      }
    }
    wiedervorlagen.setOrder("ORDER BY datum DESC");
    
    return wiedervorlagen;
  }

  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }

  public void refresh()
  {
    try
    {
      getWiedervorlageList();
    }
    catch (RemoteException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
