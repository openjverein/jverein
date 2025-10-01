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

public class SpendenbescheinigungListeFilterMap extends AbstractMap
{

  public SpendenbescheinigungListeFilterMap()
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

    map.put(
        SpendenbescheinigungListeFilterVar.DATUM_BESCHEINIGUNG_VON_F.getName(),
        fromDate((Date) control.getDatumvon().getValue()));
    map.put(
        SpendenbescheinigungListeFilterVar.DATUM_BESCHEINIGUNG_BIS_F.getName(),
        fromDate((Date) control.getDatumbis().getValue()));
    map.put(SpendenbescheinigungListeFilterVar.DATUM_SPENDE_VON_F.getName(),
        fromDate((Date) control.getEingabedatumvon().getValue()));
    map.put(SpendenbescheinigungListeFilterVar.DATUM_SPENDE_BIS_F.getName(),
        fromDate((Date) control.getEingabedatumbis().getValue()));
    map.put(SpendenbescheinigungListeFilterVar.ZEILE2.getName(),
        control.getSuchname().getValue().toString());
    map.put(SpendenbescheinigungListeFilterVar.MAIL.getName(),
        control.getMailauswahl().getText());
    map.put(SpendenbescheinigungListeFilterVar.SPENDENART.getName(),
        control.getSuchSpendenart().getText());

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

    map.put(
        SpendenbescheinigungListeFilterVar.DATUM_BESCHEINIGUNG_VON_F.getName(),
        "20240101");
    map.put(
        SpendenbescheinigungListeFilterVar.DATUM_BESCHEINIGUNG_BIS_F.getName(),
        "20241231");
    map.put(SpendenbescheinigungListeFilterVar.DATUM_SPENDE_VON_F.getName(),
        "20240101");
    map.put(SpendenbescheinigungListeFilterVar.DATUM_SPENDE_BIS_F.getName(),
        "20241231");
    map.put(SpendenbescheinigungListeFilterVar.ZEILE2.getName(), "Zeile2");
    map.put(SpendenbescheinigungListeFilterVar.MAIL.getName(), "Alle");
    map.put(SpendenbescheinigungListeFilterVar.SPENDENART.getName(), "Alle");

    return map;
  }
}
