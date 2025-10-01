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

public class AuswertungMitgliedFilterMap extends AbstractMap
{

  public AuswertungMitgliedFilterMap()
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

    map.put(AuswertungMitgliedFilterVar.MITGLIEDSCHAFT.getName(),
        control.getMitgliedStatus().getText());
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        map.put(AuswertungMitgliedFilterVar.EXT_MITGLIEDSNUMMER.getName(),
            control.getSuchExterneMitgliedsnummer().getValue().toString());
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungMitgliedFilterVar.EIGENSCHAFTEN.getName(),
        control.getEigenschaftenAuswahl().getText());
    map.put(AuswertungMitgliedFilterVar.BEITRAGSGRUPPE.getName(),
        control.getBeitragsgruppeAusw().getText());
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        map.put(AuswertungMitgliedFilterVar.ZUSATZFELDER.getName(),
            control.getZusatzfelderAuswahl().getText());
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungMitgliedFilterVar.MAIL.getName(),
        control.getMailauswahl().getText());
    map.put(AuswertungMitgliedFilterVar.GESCHLECHT.getName(),
        control.getSuchGeschlecht().getText());

    map.put(AuswertungMitgliedFilterVar.STICHTAG_F.getName(),
        fromDate((Date) control.getStichtag(false).getValue()));

    map.put(AuswertungMitgliedFilterVar.DATUM_GEBURT_VON_F.getName(),
        fromDate((Date) control.getGeburtsdatumvon().getValue()));
    map.put(AuswertungMitgliedFilterVar.DATUM_GEBURT_BIS_F.getName(),
        fromDate((Date) control.getGeburtsdatumbis().getValue()));

    map.put(AuswertungMitgliedFilterVar.DATUM_EINTRITT_VON_F.getName(),
        fromDate((Date) control.getEintrittvon().getValue()));
    map.put(AuswertungMitgliedFilterVar.DATUM_EINTRITT_BIS_F.getName(),
        fromDate((Date) control.getEintrittbis().getValue()));

    map.put(AuswertungMitgliedFilterVar.DATUM_AUSTRITT_VON_F.getName(),
        fromDate((Date) control.getAustrittvon().getValue()));
    map.put(AuswertungMitgliedFilterVar.DATUM_AUSTRITT_BIS_F.getName(),
        fromDate((Date) control.getAustrittbis().getValue()));

    if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
    {
      map.put(AuswertungMitgliedFilterVar.DATUM_STERBE_VON_F.getName(),
          fromDate((Date) control.getSterbedatumvon().getValue()));
      map.put(AuswertungMitgliedFilterVar.DATUM_STERBE_BIS_F.getName(),
          fromDate((Date) control.getSterbedatumbis().getValue()));
    }

    map.put(AuswertungMitgliedFilterVar.SORTIERUNG.getName(),
        control.getSortierung().getText());
    map.put(AuswertungMitgliedFilterVar.UEBERSCHRIFT.getName(),
        control.getAuswertungUeberschrift().getValue().toString());
    map.put(AuswertungMitgliedFilterVar.AUSGABE.getName(),
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

    map.put(AuswertungMitgliedFilterVar.MITGLIEDSCHAFT.getName(), "Angemeldet");
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        map.put(AuswertungMitgliedFilterVar.EXT_MITGLIEDSNUMMER.getName(),
            "Ext45");
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungMitgliedFilterVar.EIGENSCHAFTEN.getName(),
        "+Eigenschaft");
    map.put(AuswertungMitgliedFilterVar.BEITRAGSGRUPPE.getName(), "Alle");
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        map.put(AuswertungMitgliedFilterVar.ZUSATZFELDER.getName(),
            "Kein Feld ausgewählt");
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }
    map.put(AuswertungMitgliedFilterVar.MAIL.getName(), "Alle");
    map.put(AuswertungMitgliedFilterVar.GESCHLECHT.getName(), "Alle");

    map.put(AuswertungMitgliedFilterVar.STICHTAG_F.getName(), "20240101");

    map.put(AuswertungMitgliedFilterVar.DATUM_GEBURT_VON_F.getName(),
        "20000101");
    map.put(AuswertungMitgliedFilterVar.DATUM_GEBURT_BIS_F.getName(),
        "20241231");

    map.put(AuswertungMitgliedFilterVar.DATUM_EINTRITT_VON_F.getName(),
        "20240101");
    map.put(AuswertungMitgliedFilterVar.DATUM_EINTRITT_BIS_F.getName(),
        "20241231");

    map.put(AuswertungMitgliedFilterVar.DATUM_AUSTRITT_VON_F.getName(),
        "20240101");
    map.put(AuswertungMitgliedFilterVar.DATUM_AUSTRITT_BIS_F.getName(),
        "20241231");

    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
      {
        map.put(AuswertungMitgliedFilterVar.DATUM_STERBE_VON_F.getName(),
            "20240101");
        map.put(AuswertungMitgliedFilterVar.DATUM_STERBE_BIS_F.getName(),
            "20241231");
      }
    }
    catch (RemoteException e)
    {
      // Keine unterstützen
    }

    map.put(AuswertungMitgliedFilterVar.SORTIERUNG.getName(), "Name, Vorname");
    map.put(AuswertungMitgliedFilterVar.UEBERSCHRIFT.getName(), "Überschrift");
    map.put(AuswertungMitgliedFilterVar.AUSGABE.getName(),
        "Mitgliederliste PDF");

    return map;
  }
}
