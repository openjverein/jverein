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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Variable.FilterMap;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.keys.Filter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class AuswertungControl extends FilterControl
{

  private Map<String, Object> filtermap;

  private Map<Filter, Object> filter;

  public AuswertungControl(AbstractView view)
  {
    super(view);
  }

  public void generateFilter() throws RemoteException
  {
    filtermap = new FilterMap().getMap((FilterControl) this, null);
    filter = super.getFilter();
  }

  @Override
  public Map<Filter, Object> getFilter()
  {
    return this.filter;
  }

  public Map<String, Object> getMap(Map<String, Object> inma)
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }
    for (Entry<String, Object> entry : filtermap.entrySet())
    {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Hat keine Tabelle
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    return null;
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

}
