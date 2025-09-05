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
package de.jost_net.JVerein.gui.boxes;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.control.VorZurueckControl;
import de.jost_net.JVerein.gui.menu.WiedervorlageMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.view.WiedervorlageDetailView;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;

public class WiedervorlagenCheck extends AbstractBox
{
  @Override
  public boolean getDefaultEnabled()
  {
    try
    {
      DBIterator<Wiedervorlage> dbIterator = Einstellungen.getDBService()
          .createList(Wiedervorlage.class);
      dbIterator.addFilter("datum <= ?", new Date());
      dbIterator.addFilter("erledigung IS NULL");
      return dbIterator.hasNext();
    }
    catch (RemoteException e)
    {
      Logger.error("WiedervorlagenCheck kann nicht initalisiert werden!", e);
    }
    return false;
  }

  @Override
  public String getName()
  {
    return "JVerein Wiedervorlagen";
  }

  @Override
  public boolean isActive()
  {
    return getDefaultEnabled();
  }

  @Override
  public int getDefaultIndex()
  {
    return 0;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup labelGroup = new LabelGroup(parent, "Fällige Wiedervorlagen:",
        true);
    labelGroup.addPart(getListe());
  }

  private TablePart getListe() throws RemoteException
  {
    DBIterator<Wiedervorlage> dbIterator = Einstellungen.getDBService()
        .createList(Wiedervorlage.class);
    dbIterator.addFilter("datum <= ?", new Date());
    dbIterator.addFilter("erledigung IS NULL");

    AutoUpdateTablePart wiedervorlageList = new AutoUpdateTablePart(dbIterator,
        null);
    wiedervorlageList.addColumn("Name", "mitglied");
    wiedervorlageList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.addColumn("Vermerk", "vermerk");
    wiedervorlageList.addColumn("Erledigung", "erledigung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.setContextMenu(new WiedervorlageMenu(wiedervorlageList));

    wiedervorlageList.setAction(
        new EditAction(WiedervorlageDetailView.class, wiedervorlageList));
    VorZurueckControl.setObjektListe(null, null);
    return wiedervorlageList;
  }
}
