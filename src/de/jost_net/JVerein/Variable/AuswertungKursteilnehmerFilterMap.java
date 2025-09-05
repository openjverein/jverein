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

  public AuswertungKursteilnehmerFilterMap()
  {

  }

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

    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_VON_U.getName(),
        control.getAbbuchungsdatumvon().getValue());
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_VON_F.getName(),
        fromDate((Date) control.getAbbuchungsdatumvon().getValue()));
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_BIS_U.getName(),
        control.getAbbuchungsdatumbis().getValue());
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_BIS_F.getName(),
        fromDate((Date) control.getAbbuchungsdatumbis().getValue()));

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

    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_VON_U.getName(),
        toDate("01.01.2024"));
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_VON_F.getName(),
        "20240101");
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_BIS_U.getName(),
        toDate("31.12.2024"));
    map.put(AuswertungKursteilnehmerFilterVar.DATUM_ABBUCHUNG_BIS_F.getName(),
        "20241231");

    return map;
  }
}
