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

public class AuswertungMitgliedFilterMap extends AbstractMap
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

    for (AuswertungMitgliedFilterVar var : AuswertungMitgliedFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case MITGLIEDSCHAFT:
          value = filter.get(Filter.MITGLIEDSCHAFT_STATUS);
          break;
        case EXT_MITGLIEDSNUMMER:
          value = filter.get(Filter.EXTERNEMITGLIEDSNUMMER);
          break;
        case EIGENSCHAFTEN:
          value = filter.get(Filter.EIGENSCHAFTEN);
          break;
        case BEITRAGSGRUPPE:
          value = filter.get(Filter.BEITRAGSGRUPPE);
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
        case STICHTAG_F:
          value = filter.get(Filter.STICHTAG);
          break;
        case DATUM_GEBURT_VON_F:
          value = filter.get(Filter.GEBURTSDATUM_VON);
          break;
        case DATUM_GEBURT_BIS_F:
          value = filter.get(Filter.GEBURTSDATUM_BIS);
          break;
        case DATUM_EINTRITT_VON_F:
          value = filter.get(Filter.EINTRITT_VON);
          break;
        case DATUM_EINTRITT_BIS_F:
          value = filter.get(Filter.EINTRITT_BIS);
          break;
        case DATUM_AUSTRITT_VON_F:
          value = filter.get(Filter.AUSTRITT_VON);
          break;
        case DATUM_AUSTRITT_BIS_F:
          value = filter.get(Filter.AUSTRITT_BIS);
          break;
        case DATUM_STERBE_VON_F:
          value = filter.get(Filter.STERBEDATUM_VON);
          break;
        case DATUM_STERBE_BIS_F:
          value = filter.get(Filter.STERBEDATUM_BIS);
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
    for (AuswertungMitgliedFilterVar var : AuswertungMitgliedFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case MITGLIEDSCHAFT:
          value = "Angemeldet";
          break;
        case EXT_MITGLIEDSNUMMER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
            {
              value = "Ext45";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case EIGENSCHAFTEN:
          value = "+Eigenschaft";
          break;
        case BEITRAGSGRUPPE:
          value = "Alle";
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
        case STICHTAG_F:
          value = "20240101";
          break;
        case DATUM_GEBURT_VON_F:
          value = "20000101";
          break;
        case DATUM_GEBURT_BIS_F:
          value = "20241231";
          break;
        case DATUM_EINTRITT_VON_F:
          value = "20240101";
          break;
        case DATUM_EINTRITT_BIS_F:
          value = "20241231";
          break;
        case DATUM_AUSTRITT_VON_F:
          value = "20240101";
          break;
        case DATUM_AUSTRITT_BIS_F:
          value = "20241231";
          break;
        case DATUM_STERBE_VON_F:
          try
          {
            if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
            {
              value = "20240101";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case DATUM_STERBE_BIS_F:
          try
          {
            if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
            {
              value = "20241231";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }

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
