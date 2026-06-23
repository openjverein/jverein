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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.keys.Filter;

public class FilterMap extends AbstractMap
{
  public Map<String, Object> getMap(FilterControl control,
      Map<String, Object> inma) throws RemoteException
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
    if (control != null)
    {
      for (Entry<Filter, String> entry : control.getFilterText(true).entrySet())
      {
        map.put(entry.getKey().getSetting(), entry.getValue());
      }
    }
    return map;
  }

  public Map<String, Object> getDummyMap(Set<Filter> set,
      Map<String, Object> inma)
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
    if (set != null)
    {
      for (Filter f : set)
      {
        map.put(f.getSetting(), f.getDefault());
      }
    }

    return map;
  }
}
