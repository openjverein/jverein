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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.SollbuchungControl;

public class SollbuchungListeFilterMap extends AbstractMap
{
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

    for (SollbuchungListeFilterVar var : SollbuchungListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ZAHLER:
          value = control.getSuchname().getValue().toString();
          break;
        case MITGLIED:
          value = control.getSuchtext().getValue().toString();
          break;
        case ZAHLER_MAIL:
          value = control.getMailauswahl().getText();
          break;
        case DIFFERENZ:
          value = control.getDifferenz().getText();
          break;
        case DIFFERENZ_LIMIT:
          Double limit = (Double) control.getDoubleAusw().getValue();
          if (limit != null)
          {
            value = Einstellungen.DECIMALFORMAT.format(limit);
          }
          else
          {
            value = "";
          }
          break;
        case OHNE_ABBUCHER:
          value = (Boolean) control.getOhneAbbucher().getValue() ? "Ja"
              : "Nein";
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
    for (SollbuchungListeFilterVar var : SollbuchungListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ZAHLER:
          value = "Willi";
          break;
        case MITGLIED:
          value = "Otto";
          break;
        case ZAHLER_MAIL:
          value = "Alle";
          break;
        case DIFFERENZ:
          value = "Egal";
          break;
        case DIFFERENZ_LIMIT:
          value = "100";
          break;
        case OHNE_ABBUCHER:
          value = "Nein";
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
