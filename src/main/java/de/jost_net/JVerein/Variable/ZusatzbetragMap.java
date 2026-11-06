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
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.Datum;

public class ZusatzbetragMap extends AbstractMap
{
  public Map<String, Object> getMap(Zusatzbetrag zusatzbetrag,
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
    for (ZusatzbetragVar var : ZusatzbetragVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ERSTE_FAELLIGKEIT:
          value = Datum.formatDate(zusatzbetrag.getStartdatum());
          break;
        case LETZTE_FAELLIGKEIT:
          value = Datum.formatDate(Datum.addInterval(
              zusatzbetrag.getFaelligkeit(), -zusatzbetrag.getIntervall()));
          break;
        case NAECHSTE_FAELLIGKEIT:
          value = Datum.formatDate(zusatzbetrag.getFaelligkeit());
          break;
        case NICHT_MEHR_AUSFUEHREN:
          value = Datum.formatDate(zusatzbetrag.getEndedatum());
          break;
        case INTERVALL:
          value = new IntervallZusatzzahlung(zusatzbetrag.getIntervall())
              .getText();
          break;
        case BETRAG:
          value = Einstellungen.BIGDECIMALFORMAT
              .format(zusatzbetrag.getBetrag());
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

    for (ZusatzbetragVar var : ZusatzbetragVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ERSTE_FAELLIGKEIT:
          value = Datum.formatDate(toDate("01.01.2026"));
          break;
        case LETZTE_FAELLIGKEIT:
          value = Datum.formatDate(toDate("01.05.2026"));
          break;
        case NAECHSTE_FAELLIGKEIT:
          value = Datum.formatDate(toDate("01.06.2026"));
          break;
        case NICHT_MEHR_AUSFUEHREN:
          value = Datum.formatDate(toDate("01.09.2026"));
          break;
        case INTERVALL:
          value = "Monatlich";
          break;
        case BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(100.00);
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
