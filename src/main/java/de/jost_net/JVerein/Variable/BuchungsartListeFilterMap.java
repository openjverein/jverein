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

import de.jost_net.JVerein.gui.control.BuchungsartControl;

public class BuchungsartListeFilterMap extends AbstractMap
{
  public Map<String, Object> getMap(BuchungsartControl control,
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

    for (BuchungsartListeFilterVar var : BuchungsartListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case NUMMER:
          value = control.getSuchname().getValue().toString();
          break;
        case BEZEICHNUNG:
          value = control.getSuchtext().getValue().toString();
          break;
        case BUCHUNGSKLASSE:
          value = control.getSuchBuchungsklasse().getText();
          break;
        case ART:
          value = control.getSuchBuchungsartArt().getText();
          break;
        case STATUS:
          value = control.getSuchStatus("Ohne Deaktiviert").getText();
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
    for (BuchungsartListeFilterVar var : BuchungsartListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case NUMMER:
          value = "1";
          break;
        case BEZEICHNUNG:
          value = "Beiträge";
          break;
        case BUCHUNGSKLASSE:
          value = "Alle";
          break;
        case ART:
          value = "Alle";
          break;
        case STATUS:
          value = "Alle";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
