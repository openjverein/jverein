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

import de.jost_net.JVerein.gui.control.AbrechnungslaufBuchungenControl;

public class AbrechnungSollbuchungenParameterMap extends AbstractMap
{

  public AbrechnungSollbuchungenParameterMap()
  {

  }

  public Map<String, Object> getMap(AbrechnungslaufBuchungenControl control,
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

    map.put(AbrechnungSollbuchungenParameterVar.DATUM_F.getName(),
        fromDate((Date) control.getDatum(false).getValue()));
    map.put(AbrechnungSollbuchungenParameterVar.ZAHLUNGSGRUND.getName(),
        control.getZahlungsgrund().getValue().toString());
    map.put(AbrechnungSollbuchungenParameterVar.LAUF.getName(),
        control.getLauf().getValue().toString());
    map.put(AbrechnungSollbuchungenParameterVar.BEMERKUNG.getName(),
        control.getBemerkung().getValue().toString());

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

    map.put(AbrechnungSollbuchungenParameterVar.DATUM_F.getName(), "20240101");
    map.put(AbrechnungSollbuchungenParameterVar.ZAHLUNGSGRUND.getName(),
        "Zahlungsgrund");
    map.put(AbrechnungSollbuchungenParameterVar.LAUF.getName(), "33");
    map.put(AbrechnungSollbuchungenParameterVar.BEMERKUNG.getName(),
        "Bemerkung");

    return map;
  }
}
