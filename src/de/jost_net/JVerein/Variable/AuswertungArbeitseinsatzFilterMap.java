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

import de.jost_net.JVerein.gui.control.ArbeitseinsatzControl;

public class AuswertungArbeitseinsatzFilterMap extends AbstractMap
{

  public AuswertungArbeitseinsatzFilterMap()
  {

  }

  public Map<String, Object> getMap(ArbeitseinsatzControl control,
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

    map.put(AuswertungArbeitseinsatzFilterVar.FILTER_AUSWERTUNG.getName(),
        control.getAuswertungSchluessel().getText());
    map.put(AuswertungArbeitseinsatzFilterVar.FILTER_JAHR.getName(),
        control.getSuchJahr().getText());

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

    map.put(AuswertungArbeitseinsatzFilterVar.FILTER_AUSWERTUNG.getName(),
        "ALLE");
    map.put(AuswertungArbeitseinsatzFilterVar.FILTER_JAHR.getName(), "2024");

    return map;
  }
}
