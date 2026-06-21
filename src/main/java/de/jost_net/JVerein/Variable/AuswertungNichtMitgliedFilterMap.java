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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;

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

    for (AuswertungNichtMitgliedFilterVar var : AuswertungNichtMitgliedFilterVar
        .values())
    {
      Object value = null;
      switch (var)
      {
        case MITGLIEDSTYP:
          value = control.getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED)
              .getText();
          break;
        case EIGENSCHAFTEN:
          value = control.getEigenschaftenAuswahl().getText();
          break;
        case ZUSATZFELDER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.USEZUSATZFELDER))
            {
              value = control.getZusatzfelderAuswahl().getText();
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
        case MAIL:
          value = control.getMailauswahl().getText();
          break;
        case GESCHLECHT:
          value = control.getSuchGeschlecht().getText();
          break;
        case DATUM_GEBURT_VON_F:
          value = fromDate((Date) control.getGeburtsdatumvon().getValue());
          break;
        case DATUM_GEBURT_BIS_F:
          value = fromDate((Date) control.getGeburtsdatumbis().getValue());
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
