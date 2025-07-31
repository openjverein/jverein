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

public class AbrechnungParameterMap extends AbstractMap
{

  public AbrechnungParameterMap()
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

    map.put(AbrechnungParameterVar.DATUM_FAELLIGKEIT_U.getName(),
        control.getFaelligkeit().getValue());
    map.put(AbrechnungParameterVar.DATUM_FAELLIGKEIT_F.getName(),
        fromDate((Date) control.getFaelligkeit().getValue()));
    map.put(AbrechnungParameterVar.DATUM_STICHTAG_U.getName(),
        control.getStichtag().getValue());
    map.put(AbrechnungParameterVar.DATUM_STICHTAG_F.getName(),
        fromDate((Date) control.getStichtag().getValue()));
    map.put(AbrechnungParameterVar.DATUM_EINTRITT_U.getName(),
        control.getVondatum().getValue());
    map.put(AbrechnungParameterVar.DATUM_EINTRITT_F.getName(),
        fromDate((Date) control.getVondatum().getValue()));
    map.put(AbrechnungParameterVar.DATUM_EINGABE_U.getName(),
        control.getVonEingabedatum().getValue());
    map.put(AbrechnungParameterVar.DATUM_EINGABE_F.getName(),
        fromDate((Date) control.getVonEingabedatum().getValue()));
    map.put(AbrechnungParameterVar.DATUM_AUSTRITT_U.getName(),
        control.getBisdatum().getValue());
    map.put(AbrechnungParameterVar.DATUM_AUSTRITT_F.getName(),
        fromDate((Date) control.getBisdatum().getValue()));
    map.put(AbrechnungParameterVar.ZAHLUNGSGRUND.getName(),
        control.getZahlungsgrund().getValue());
    map.put(AbrechnungParameterVar.MODUS.getName(),
        control.getAbbuchungsmodus().getText());
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      map.put(AbrechnungParameterVar.ABREACHNUNGSMONAT.getName(),
          control.getAbrechnungsmonat().getValue());
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

    map.put(AbrechnungParameterVar.DATUM_FAELLIGKEIT_U.getName(),
        toDate("01.01.2024"));
    map.put(AbrechnungParameterVar.DATUM_FAELLIGKEIT_F.getName(), "20240101");
    map.put(AbrechnungParameterVar.DATUM_STICHTAG_U.getName(),
        toDate("01.01.2024"));
    map.put(AbrechnungParameterVar.DATUM_STICHTAG_F.getName(), "20240101");
    map.put(AbrechnungParameterVar.DATUM_EINTRITT_U.getName(),
        toDate("01.01.2024"));
    map.put(AbrechnungParameterVar.DATUM_EINTRITT_F.getName(), "20240101");
    map.put(AbrechnungParameterVar.DATUM_EINGABE_U.getName(),
        toDate("01.01.2024"));
    map.put(AbrechnungParameterVar.DATUM_EINGABE_F.getName(), "20240101");
    map.put(AbrechnungParameterVar.DATUM_AUSTRITT_U.getName(),
        toDate("01.01.2024"));
    map.put(AbrechnungParameterVar.DATUM_AUSTRITT_F.getName(), "20240101");
    map.put(AbrechnungParameterVar.ZAHLUNGSGRUND.getName(), "Zahlungsgrund");
    map.put(AbrechnungParameterVar.MODUS.getName(), "Alle");
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      map.put(AbrechnungParameterVar.ABREACHNUNGSMONAT.getName(), "Januar");
    }

    return map;
  }
}
