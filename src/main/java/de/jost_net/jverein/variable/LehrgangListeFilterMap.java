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
package de.jost_net.jverein.variable;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.jverein.gui.control.FilterControl;

public class LehrgangListeFilterMap extends AbstractMap
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

    for (LehrgangListeFilterVar var : LehrgangListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case NAME:
          value = control.getSuchname().getValue().toString();
          break;
        case LEHRGANGSART:
          value = control.getSuchLehrgangsart().getText();
          break;
        case BEZEICHNUNG:
          value = control.getSuchbezeichnung().getValue().toString();
          break;
        case VERANSTALTER:
          value = control.getSuchtext().getValue().toString();
          break;
        case DATUM_VON_F:
          value = fromDate((Date) control.getDatumvon().getValue());
          break;
        case DATUM_BIS_F:
          value = fromDate((Date) control.getDatumbis().getValue());
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
    for (LehrgangListeFilterVar var : LehrgangListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case NAME:
          value = "Meier";
          break;
        case LEHRGANGSART:
          value = "Grundkurs";
          break;
        case BEZEICHNUNG:
          value = "A-2024";
          break;
        case VERANSTALTER:
          value = "Veranstalter";
          break;
        case DATUM_VON_F:
          value = "20240101";
          break;
        case DATUM_BIS_F:
          value = "20241231";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
