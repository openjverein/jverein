/**********************************************************************
 * 
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.util.Datum;

public class LehrgangMap extends AbstractMap
{
  public Map<String, Object> getMap(Lehrgang lg, Map<String, Object> inma)
      throws RemoteException
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
    for (LehrgangVar var : LehrgangVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = Datum.formatDate(lg.getVon());
          break;
        case DATUM_BIS_F:
          value = fromDate((Date) lg.getBis());
          break;
        case LEHRGANGSART:
          value = lg.getLehrgangsart();
          break;
        case BETRAG:
          value = Einstellungen.BIGDECIMALFORMAT.format(lg.getBetrag());
          break;
        case BEZEICHNUNG:
          value = lg.getBezeichnung();
          break;
        case VERANSTALTER:
          value = lg.getVeranstalter();
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
    for (LehrgangVar var : LehrgangVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_VON_F:
          value = "20250101";
          break;
        case DATUM_BIS_F:
          value = "20251231";
          break;
        case LEHRGANGSART:
          value = "Grundkurs";
          break;
        case BETRAG:
          value = "100,00";
          break;
        case BEZEICHNUNG:
          value = "KS-2026-01";
          break;
        case VERANSTALTER:
          value = "Dummy AG";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
