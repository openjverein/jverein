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

import de.jost_net.JVerein.gui.control.SollbuchungControl;

public class SollbuchungListeFilterMap extends AbstractMap
{

  public SollbuchungListeFilterMap()
  {

  }

  public Map<String, Object> getMap(SollbuchungControl control,
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

    map.put(SollbuchungListeFilterVar.ZAHLER.getName(),
        control.getSuchname().getValue().toString());
    map.put(SollbuchungListeFilterVar.MITGLIED.getName(),
        control.getSuchtext().getValue().toString());
    map.put(SollbuchungListeFilterVar.ZAHLER_MAIL.getName(),
        control.getMailauswahl().getText());
    map.put(SollbuchungListeFilterVar.DIFFERENZ.getName(),
        control.getDifferenz().getValue().toString());
    Double limit = (Double) control.getDoubleAusw().getValue();
    if (limit != null)
    {
      map.put(SollbuchungListeFilterVar.DIFFERENZ_LIMIT.getName(), limit);
    }
    else
    {
      map.put(SollbuchungListeFilterVar.DIFFERENZ_LIMIT.getName(), "");
    }
    String o = (Boolean) control.getOhneAbbucher().getValue() ? "Ja" : "Nein";
    map.put(SollbuchungListeFilterVar.OHNE_ABBUCHER.getName(), o);
    map.put(SollbuchungListeFilterVar.DATUM_VON_U.getName(),
        control.getDatumvon().getValue());
    map.put(SollbuchungListeFilterVar.DATUM_VON_F.getName(),
        fromDate((Date) control.getDatumvon().getValue()));
    map.put(SollbuchungListeFilterVar.DATUM_BIS_U.getName(),
        control.getDatumbis().getValue());
    map.put(SollbuchungListeFilterVar.DATUM_BIS_F.getName(),
        fromDate((Date) control.getDatumbis().getValue()));

    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
      throws RemoteException
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

    map.put(SollbuchungListeFilterVar.ZAHLER.getName(), "Willi");
    map.put(SollbuchungListeFilterVar.MITGLIED.getName(), "Otto");
    map.put(SollbuchungListeFilterVar.ZAHLER_MAIL.getName(), "Alle");
    map.put(SollbuchungListeFilterVar.DIFFERENZ.getName(), "Egal");
    map.put(SollbuchungListeFilterVar.DIFFERENZ_LIMIT.getName(), "100");
    map.put(SollbuchungListeFilterVar.OHNE_ABBUCHER.getName(), "Nein");
    map.put(SollbuchungListeFilterVar.DATUM_VON_U.getName(),
        toDate("01.01.2024"));
    map.put(SollbuchungListeFilterVar.DATUM_VON_F.getName(), "20240101");
    map.put(SollbuchungListeFilterVar.DATUM_BIS_U.getName(),
        toDate("31.12.2024"));
    map.put(SollbuchungListeFilterVar.DATUM_BIS_F.getName(), "20241231");

    return map;
  }
}
