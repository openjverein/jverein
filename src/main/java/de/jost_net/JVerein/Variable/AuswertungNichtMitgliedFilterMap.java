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
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.Filter;

public class AuswertungNichtMitgliedFilterMap extends AbstractMap
{

  public Map<String, Object> getMap(MitgliedControl control,
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
    Map<Filter, String> filter = control.getFilterText(true);
    for (AuswertungNichtMitgliedFilterVar var : AuswertungNichtMitgliedFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case MITGLIEDSTYP:
          value = filter.get(Filter.MITGLIEDSTYP);
          if (value == null)
          {
            value = "Alle";
          }
          break;
        case EIGENSCHAFTEN:
          value = filter.get(Filter.EIGENSCHAFTEN);
          break;
        case ZUSATZFELDER:
          value = filter.get(Filter.ZUSATZFELD);
          break;
        case MAIL:
          value = filter.get(Filter.MAIL);
          break;
        case GESCHLECHT:
          value = filter.get(Filter.GESCHLECHT);
          break;
        case DATUM_GEBURT_VON_F:
          value = filter.get(Filter.GEBURTSDATUM_VON);
          break;
        case DATUM_GEBURT_BIS_F:
          value = filter.get(Filter.GEBURTSDATUM_BIS);
          break;
        case SORTIERUNG:
          value = control.getSortierung().getText();
          break;
        case UEBERSCHRIFT:
          value = control.getAuswertungUeberschrift().getValue().toString();
          break;
        case AUSGABE:
          String ausgabe = control.getAusgabe().getText();
          if (ausgabe.startsWith("Vorlage CSV:"))
          {
            ausgabe = ausgabe.substring(13);
          }
          value = ausgabe;
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
    for (AuswertungNichtMitgliedFilterVar var : AuswertungNichtMitgliedFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case MITGLIEDSTYP:
          value = "Spender/in";
          break;
        case EIGENSCHAFTEN:
          value = "+Eigenschaft";
          break;
        case ZUSATZFELDER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.USEZUSATZFELDER))
            {
              value = "Kein Feld ausgewählt";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case MAIL:
          value = "Alle";
          break;
        case GESCHLECHT:
          value = "Alle";
          break;
        case DATUM_GEBURT_VON_F:
          value = "20000101";
          break;
        case DATUM_GEBURT_BIS_F:
          value = "20241231";
          break;
        case SORTIERUNG:
          value = "Name, Vorname";
          break;
        case UEBERSCHRIFT:
          value = "Überschrift";
          break;
        case AUSGABE:
          value = "Mitgliederliste PDF";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
