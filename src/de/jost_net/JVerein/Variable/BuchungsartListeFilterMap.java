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

  public BuchungsartListeFilterMap()
  {

  }

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

    map.put(BuchungsartListeFilterVar.NUMMER.getName(),
        control.getSuchname().getValue().toString());
    map.put(BuchungsartListeFilterVar.BEZEICHNUNG.getName(),
        control.getSuchtext().getValue().toString());
    map.put(BuchungsartListeFilterVar.BUCHUNGSKLASSE.getName(),
        control.getSuchBuchungsklasse().getText());
    map.put(BuchungsartListeFilterVar.ART.getName(),
        control.getSuchBuchungsartArt().getText());
    map.put(BuchungsartListeFilterVar.STATUS.getName(),
        control.getSuchStatus("Ohne Deaktiviert").getText());

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

    map.put(BuchungsartListeFilterVar.NUMMER.getName(), "1");
    map.put(BuchungsartListeFilterVar.BEZEICHNUNG.getName(), "Beiträge");
    map.put(BuchungsartListeFilterVar.BUCHUNGSKLASSE.getName(), "Alle");
    map.put(BuchungsartListeFilterVar.ART.getName(), "Alle");
    map.put(BuchungsartListeFilterVar.STATUS.getName(), "Alle");

    return map;
  }
}
