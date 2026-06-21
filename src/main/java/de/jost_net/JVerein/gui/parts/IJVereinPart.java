/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

public interface IJVereinPart
{

  @SuppressWarnings("rawtypes")
  public List getItems() throws RemoteException;

  public List<Column> getAllColums();

  public List<Column> getColums();

  public void export(String title, String subtitle, String filename,
      ExportArt art) throws ApplicationException;

  public void saveSpalten(List<Column> columns) throws RemoteException;

  public String getTableName();

  /**
   * Ermittelt die ID der Tablepart aus der View und dem Objekttyp
   * 
   * @param tablePartId
   * @param tableName
   * @return
   * @throws RemoteException
   */
  default String getTablePartID(String tablePartId, String tableName)
      throws RemoteException
  {
    if (tablePartId != null)
    {
      return tablePartId;
    }
    List<?> items = getItems();

    if (items.size() == 0)
    {
      tablePartId = "";
      return tablePartId;
    }
    StringBuilder sb = new StringBuilder();

    sb.append(GUI.getCurrentView().getClass().getSimpleName());
    sb.append(".");
    if (tableName != null)
    {
      sb.append(tableName);
    }
    else
    {
      sb.append(items.get(0).getClass().getSimpleName());
    }
    sb.append(".");

    tablePartId = sb.toString();
    return tablePartId;
  }

  default void saveSpalten(List<Column> columns, String tablePartId,
      String tableName, Settings settings) throws RemoteException
  {
    for (Column c : getAllColums())
    {
      settings.setAttribute(
          getTablePartID(tablePartId, tableName) + c.getName(),
          columns.contains(c));
    }
  }
}
