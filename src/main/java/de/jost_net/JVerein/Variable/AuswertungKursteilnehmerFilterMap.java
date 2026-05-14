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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.gui.control.FilterControl;

public class AuswertungKursteilnehmerFilterMap extends AbstractMap
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

    for (AuswertungKursteilnehmerFilterVar var : AuswertungKursteilnehmerFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_ABBUCHUNG_VON_F:
          value = fromDate((Date) control.getAbbuchungsdatumvon().getValue());
          break;
        case DATUM_ABBUCHUNG_BIS_F:
          value = fromDate((Date) control.getAbbuchungsdatumbis().getValue());
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
    for (AuswertungKursteilnehmerFilterVar var : AuswertungKursteilnehmerFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_ABBUCHUNG_VON_F:
          value = "20240101";
          break;
        case DATUM_ABBUCHUNG_BIS_F:
          value = "20241231";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
