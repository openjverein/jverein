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

  public AuswertungNichtMitgliedFilterMap()
  {

  }

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

    map.put(AuswertungNichtMitgliedFilterVar.MITGLIEDSTYP.getName(),
        control.getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED).getText());
    map.put(AuswertungNichtMitgliedFilterVar.EIGENSCHAFTEN.getName(),
        control.getEigenschaftenAuswahl().getText());
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        map.put(AuswertungNichtMitgliedFilterVar.ZUSATZFELDER.getName(),
            control.getZusatzfelderAuswahl().getText());
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungNichtMitgliedFilterVar.MAIL.getName(),
        control.getMailauswahl().getText());
    map.put(AuswertungNichtMitgliedFilterVar.GESCHLECHT.getName(),
        control.getSuchGeschlecht().getText());

    map.put(AuswertungNichtMitgliedFilterVar.DATUM_GEBURT_VON_F.getName(),
        fromDate((Date) control.getGeburtsdatumvon().getValue()));
    map.put(AuswertungNichtMitgliedFilterVar.DATUM_GEBURT_BIS_F.getName(),
        fromDate((Date) control.getGeburtsdatumbis().getValue()));

    map.put(AuswertungNichtMitgliedFilterVar.SORTIERUNG.getName(),
        control.getSortierung().getText());
    map.put(AuswertungNichtMitgliedFilterVar.UEBERSCHRIFT.getName(),
        control.getAuswertungUeberschrift().getValue().toString());
    map.put(AuswertungNichtMitgliedFilterVar.AUSGABE.getName(),
        control.getAusgabe().getText());

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

    map.put(AuswertungNichtMitgliedFilterVar.MITGLIEDSTYP.getName(),
        "Spender/in");
    map.put(AuswertungNichtMitgliedFilterVar.EIGENSCHAFTEN.getName(),
        "+Eigenschaft");
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        map.put(AuswertungNichtMitgliedFilterVar.ZUSATZFELDER.getName(),
            "Kein Feld ausgewählt");
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungNichtMitgliedFilterVar.MAIL.getName(), "Alle");
    map.put(AuswertungNichtMitgliedFilterVar.GESCHLECHT.getName(), "Alle");

    map.put(AuswertungNichtMitgliedFilterVar.DATUM_GEBURT_VON_F.getName(),
        "20000101");
    map.put(AuswertungNichtMitgliedFilterVar.DATUM_GEBURT_BIS_F.getName(),
        "20241231");

    map.put(AuswertungNichtMitgliedFilterVar.SORTIERUNG.getName(),
        "Name, Vorname");
    map.put(AuswertungNichtMitgliedFilterVar.UEBERSCHRIFT.getName(),
        "Überschrift");
    map.put(AuswertungNichtMitgliedFilterVar.AUSGABE.getName(),
        "Mitgliederliste PDF");

    return map;
  }
}
