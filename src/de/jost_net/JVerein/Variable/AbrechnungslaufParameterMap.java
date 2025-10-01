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
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.keys.Beitragsmodel;

public class AbrechnungslaufParameterMap extends AbstractMap
{

  public AbrechnungslaufParameterMap()
  {

  }

  public Map<String, Object> getMap(AbrechnungSEPAControl control,
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

    map.put(AbrechnungslaufParameterVar.DATUM_FAELLIGKEIT_F.getName(),
        fromDate((Date) control.getFaelligkeit().getValue()));
    map.put(AbrechnungslaufParameterVar.DATUM_STICHTAG_F.getName(),
        fromDate((Date) control.getStichtag().getValue()));
    map.put(AbrechnungslaufParameterVar.DATUM_EINTRITT_F.getName(),
        fromDate((Date) control.getVondatum().getValue()));
    map.put(AbrechnungslaufParameterVar.DATUM_EINGABE_F.getName(),
        fromDate((Date) control.getVonEingabedatum().getValue()));
    map.put(AbrechnungslaufParameterVar.DATUM_AUSTRITT_F.getName(),
        fromDate((Date) control.getBisdatum().getValue()));
    map.put(AbrechnungslaufParameterVar.ZAHLUNGSGRUND.getName(),
        control.getZahlungsgrund().getValue().toString());
    map.put(AbrechnungslaufParameterVar.MODUS.getName(),
        control.getAbbuchungsmodus().getText());
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      map.put(AbrechnungslaufParameterVar.ABREACHNUNGSMONAT.getName(),
          control.getAbrechnungsmonat().getText());
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

    map.put(AbrechnungslaufParameterVar.DATUM_FAELLIGKEIT_F.getName(),
        "20240101");
    map.put(AbrechnungslaufParameterVar.DATUM_STICHTAG_F.getName(), "20240101");
    map.put(AbrechnungslaufParameterVar.DATUM_EINTRITT_F.getName(), "20240101");
    map.put(AbrechnungslaufParameterVar.DATUM_EINGABE_F.getName(), "20240101");
    map.put(AbrechnungslaufParameterVar.DATUM_AUSTRITT_F.getName(), "20240101");
    map.put(AbrechnungslaufParameterVar.ZAHLUNGSGRUND.getName(),
        "Zahlungsgrund");
    map.put(AbrechnungslaufParameterVar.MODUS.getName(), "Alle");
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      map.put(AbrechnungslaufParameterVar.ABREACHNUNGSMONAT.getName(),
          "Januar");
    }

    return map;
  }
}
