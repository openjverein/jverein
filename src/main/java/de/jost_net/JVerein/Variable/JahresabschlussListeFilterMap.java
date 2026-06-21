/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.gui.control.JahresabschlussControl;
import de.willuhn.logging.Logger;

public class JahresabschlussListeFilterMap extends AbstractMap
{

  public Map<String, Object> getMap(JahresabschlussControl control,
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

    for (JahresabschlussListeFilterVar var : JahresabschlussListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = fromDate((Date) control.getDatumvon().getDate());
          break;
        case DATUM_BIS_F:
          value = fromDate((Date) control.getDatumbis().getDate());
          break;
        case DATUM_F:
          try
          {
            value = fromDate((Date) control.getDatum().getValue());
          }
          catch (ParseException e)
          {
            Logger.error("Kann Jahresabschluss Datum nicht lesen", e);
            value = "";
          }
          break;
        case NAME:
          try
          {
            value = control.getName().getValue().toString();
          }
          catch (ParseException e)
          {
            Logger.error("Kann Jahresabschluss Name nicht lesen", e);
            value = "";
          }
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
    for (JahresabschlussListeFilterVar var : JahresabschlussListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = "20240101";
          break;
        case DATUM_BIS_F:
          value = "20241231";
          break;
        case DATUM_F:
          value = "20250303";
          break;
        case NAME:
          value = "Meier";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
