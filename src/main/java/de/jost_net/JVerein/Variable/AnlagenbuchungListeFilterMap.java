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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Konto;

public class AnlagenbuchungListeFilterMap extends AbstractMap
{

  public Map<String, Object> getMap(BuchungsControl control,
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

    Konto k = (Konto) control.getFilterValue(Filter.KONTO);

    for (AnlagenbuchungListeFilterVar var : AnlagenbuchungListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case KONTO_NR:
          if (k != null)
          {
            value = k.getNummer();
          }
          else
          {
            value = "";
          }
          break;
        case KONTO_BEZEICHNUNG:
          if (k != null)
          {
            value = k.getBezeichnung();
          }
          else
          {
            value = "";
          }
          break;
        case BUCHUNGSART:
          value = control.getFilterText(Filter.BUCHUNGSART);
          break;
        case PROJEKT:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = control.getFilterText(Filter.PROJEKT);
          }
          break;
        case SPLITBUCHUNG:
          value = control.getFilterText(Filter.SPLITBUCHUNG);
          break;
        case BETRAG:
          value = control.getFilterText(Filter.BETRAG);
          break;
        case DATUM_VON_F:
          value = control.getFilterText(Filter.DATUM_VON);
          break;
        case DATUM_BIS_F:
          value = control.getFilterText(Filter.DATUM_BIS);
          break;
        case ENTHALTENER_TEXT:
          value = control.getFilterText(Filter.ENTHALTENER_TEXT);
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
    for (AnlagenbuchungListeFilterVar var : AnlagenbuchungListeFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case KONTO_NR:
          value = "888999";
          break;
        case KONTO_BEZEICHNUNG:
          value = "Giro";
          break;
        case BUCHUNGSART:
          value = "Beitrag";
        case PROJEKT:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = "Projekt1";
          }
          break;
        case SPLITBUCHUNG:
          value = "Alle";
          break;
        case BETRAG:
          value = "100";
          break;
        case DATUM_VON_F:
          value = "20240101";
          break;
        case DATUM_BIS_F:
          value = "20241231";
          break;
        case ENTHALTENER_TEXT:
          value = "Text";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
