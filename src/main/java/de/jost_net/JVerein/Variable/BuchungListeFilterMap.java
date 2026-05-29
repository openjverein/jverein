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
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.rmi.Konto;

public class BuchungListeFilterMap extends AbstractMap
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

    Konto k = (Konto) control.getSuchKonto().getValue();
    for (BuchungListeFilterVar var : BuchungListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case KONTO_NR:
          if (k != null)
          {
            value = k.getNummer();
          }
          break;
        case KONTO_BEZEICHNUNG:
          if (k != null)
          {
            value = k.getBezeichnung();
          }
          break;
        case BUCHUNGSART:
          value = control.getSuchBuchungsart().getText();
          break;
        case PROJEKT:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = control.getSuchProjekt().getText();
          }
        case SPLITBUCHUNG:
          value = control.getSuchSplibuchung().getText();
          break;
        case BETRAG:
          value = control.getSuchBetrag().getValue().toString();
          break;
        case DATUM_VON_F:
          value = fromDate((Date) control.getVondatum().getValue());
          break;
        case DATUM_BIS_F:
          value = fromDate((Date) control.getBisdatum().getValue());
          break;
        case UNGEPRUEFT:
          value = (Boolean) control.getUngeprueft().getValue() ? "Ja" : "Nein";
          break;
        case ENTHALTENER_TEXT:
          value = control.getSuchtext().getValue().toString();
          break;
        case MITGLIED_ZUGEORDNET:
          value = control.getSuchMitgliedZugeordnet().getText();
          break;
        case MITGLIED_NAME:
          value = control.getMitglied().getValue().toString();
          break;
        case STEUER:
          if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
          {
            value = control.getSuchSteuer().getText();
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
    for (BuchungListeFilterVar var : BuchungListeFilterVar.values())
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
          break;
        case PROJEKT:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = "Projekt1";
          }
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
        case UNGEPRUEFT:
          value = "Nein";
          break;
        case ENTHALTENER_TEXT:
          value = "Text";
          break;
        case MITGLIED_ZUGEORDNET:
          value = "Beide";
          break;
        case MITGLIED_NAME:
          value = "Willi";
        case STEUER:
          if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
          {
            value = "Alle";
          }
          break;
      }
      map.put(var.getName(), value);
    }

    return map;
  }
}
