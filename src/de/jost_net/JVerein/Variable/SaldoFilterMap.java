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

  public SaldoFilterMap()
  {

  }

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

    map.put(SaldoFilterVar.DATUM_VON_F.getName(),
        control.getDatumvon().getValue());
    map.put(SaldoFilterVar.DATUM_VON_U.getName(),
        toDate((String) control.getDatumvon().getValue()));
    map.put(SaldoFilterVar.DATUM_BIS_F.getName(),
        control.getDatumbis().getValue());
    map.put(SaldoFilterVar.DATUM_BIS_U.getName(),
        toDate((String) control.getDatumbis().getValue()));
    map.put(SaldoFilterVar.JAHR.getName(),
        control.getGeschaeftsjahr().getValue());

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

    map.put(SaldoFilterVar.DATUM_VON_U.getName(), toDate("01.01.2024"));
    map.put(SaldoFilterVar.DATUM_VON_F.getName(), "20240101");
    map.put(SaldoFilterVar.DATUM_BIS_U.getName(), toDate("31.12.2024"));
    map.put(SaldoFilterVar.DATUM_BIS_F.getName(), "20241231");
    map.put(SaldoFilterVar.JAHR.getName(), "2024");

    return map;
  }
}
