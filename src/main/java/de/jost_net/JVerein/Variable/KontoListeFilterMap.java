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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.gui.control.FilterControl;

public class KontoListeFilterMap extends AbstractMap
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

    for (KontoListeFilterVar var : KontoListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case NUMMER:
          value = control.getSuchtext().getValue().toString();
          break;
        case BEZEICHNUNG:
          value = control.getSuchname().getValue().toString();
          break;
        case STATUS:
          value = control.getSuchStatus("Nur aktive Konten").getText();
          break;
        case KONTOART:
          value = control.getSuchKontoart().getText();
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
  {
    Map<String, Object> map = null;
    if (inMap == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inMap;
    }
    for (KontoListeFilterVar var : KontoListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case NUMMER:
          value = "1234";
          break;
        case BEZEICHNUNG:
          value = "Giro";
          break;
        case KONTOART:
          value = "Alle";
          break;
        case STATUS:
          value = "Nur aktive Konten";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
