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

  public BuchungListeFilterMap()
  {

  }

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
    if (k != null)
    {
      map.put(BuchungListeFilterVar.KONTO_NR.getName(), k.getNummer());
      map.put(BuchungListeFilterVar.KONTO_BEZEICHNUNG.getName(),
          k.getBezeichnung());
    }
    else
    {
      map.put(BuchungListeFilterVar.KONTO_NR.getName(), "");
      map.put(BuchungListeFilterVar.KONTO_BEZEICHNUNG.getName(), "");
    }
    map.put(BuchungListeFilterVar.BUCHUNGSART.getName(),
        control.getSuchBuchungsart().getText());
    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      map.put(BuchungListeFilterVar.PROJEKT.getName(),
          control.getSuchProjekt().getText());
    }
    map.put(BuchungListeFilterVar.SPLITBUCHUNG.getName(),
        control.getSuchSplibuchung().getText());
    map.put(BuchungListeFilterVar.BETRAG.getName(),
        control.getSuchBetrag().getValue().toString());
    map.put(BuchungListeFilterVar.DATUM_VON_F.getName(),
        fromDate((Date) control.getVondatum().getValue()));
    map.put(BuchungListeFilterVar.DATUM_BIS_F.getName(),
        fromDate((Date) control.getBisdatum().getValue()));
    String u = (Boolean) control.getUngeprueft().getValue() ? "Ja" : "Nein";
    map.put(BuchungListeFilterVar.UNGEPRUEFT.getName(), u);
    map.put(BuchungListeFilterVar.ENTHALTENER_TEXT.getName(),
        control.getSuchtext().getValue().toString());
    map.put(BuchungListeFilterVar.MITGLIED_ZUGEORDNET.getName(),
        control.getSuchMitgliedZugeordnet().getText());
    map.put(BuchungListeFilterVar.MITGLIED_NAME.getName(),
        control.getMitglied().getValue().toString());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      map.put(BuchungListeFilterVar.STEUER.getName(),
          control.getSuchSteuer().getText());
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

    map.put(BuchungListeFilterVar.KONTO_NR.getName(), "888999");
    map.put(BuchungListeFilterVar.KONTO_BEZEICHNUNG.getName(), "Giro");
    map.put(BuchungListeFilterVar.BUCHUNGSART.getName(), "Beitrag");
    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      map.put(BuchungListeFilterVar.PROJEKT.getName(), "Projekt1");
    }
    map.put(BuchungListeFilterVar.SPLITBUCHUNG.getName(), "Alle");
    map.put(BuchungListeFilterVar.BETRAG.getName(), "100");
    map.put(BuchungListeFilterVar.DATUM_VON_F.getName(), "20240101");
    map.put(BuchungListeFilterVar.DATUM_BIS_F.getName(), "20241231");
    map.put(BuchungListeFilterVar.UNGEPRUEFT.getName(), "Nein");
    map.put(BuchungListeFilterVar.ENTHALTENER_TEXT.getName(), "Text");
    map.put(BuchungListeFilterVar.MITGLIED_ZUGEORDNET.getName(), "Beide");
    map.put(BuchungListeFilterVar.MITGLIED_NAME.getName(), "Willi");
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      map.put(BuchungListeFilterVar.STEUER.getName(), "Alle");
    }

    return map;
  }
}
