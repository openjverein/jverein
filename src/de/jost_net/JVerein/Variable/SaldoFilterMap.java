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

import de.jost_net.JVerein.gui.control.AbstractSaldoControl;

public class SaldoFilterMap extends AbstractMap
{

  public Map<String, Object> getMap(AbstractSaldoControl control,
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

    for (SaldoFilterVar var : SaldoFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = fromDate(toDate((String) control.getDatumvon().getValue()));
          break;
        case DATUM_BIS_F:
          value = fromDate(toDate((String) control.getDatumbis().getValue()));
          break;
        case JAHR:
          value = control.getGeschaeftsjahr().getValue().toString();
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
    for (SaldoFilterVar var : SaldoFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = "20240101";
          break;
        case DATUM_BIS_F:
          value = "20241231";
          break;
        case JAHR:
          value = "2024";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
