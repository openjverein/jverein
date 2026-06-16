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
import de.willuhn.jameica.gui.parts.Column;
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
}
