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
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Monat;

public class AbrechnungslaufParameterMap extends AbstractMap
{

  public Map<String, Object> getMap(AbrechnungSEPAParam param,
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

    for (AbrechnungslaufParameterVar var : AbrechnungslaufParameterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_FAELLIGKEIT_F:
          value = fromDate(param.faelligkeit);
          break;
        case DATUM_STICHTAG_F:
          value = fromDate(param.stichtag);
          break;
        case DATUM_EINTRITT_F:
          value = fromDate(param.vondatum);
          break;
        case DATUM_EINGABE_F:
          value = fromDate(param.voneingabedatum);
          break;
        case DATUM_AUSTRITT_F:
          value = fromDate(param.bisdatum);
          break;
        case ZAHLUNGSGRUND:
          value = param.verwendungszweck;
          break;
        case MODUS:
          value = param.abbuchungsausgabe.getText();
          break;
        case ABREACHNUNGSMONAT:
          if ((Integer) Einstellungen.getEinstellung(
              Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
          {
            value = Monat.getByKey(param.abrechnungsmonat).getText();
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
    for (AbrechnungslaufParameterVar var : AbrechnungslaufParameterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_FAELLIGKEIT_F:
          value = "20240101";
          break;
        case DATUM_STICHTAG_F:
          value = "20240101";
          break;
        case DATUM_EINTRITT_F:
          value = "20240101";
          break;
        case DATUM_EINGABE_F:
          value = "20240101";
          break;
        case DATUM_AUSTRITT_F:
          value = "20240101";
          break;
        case ZAHLUNGSGRUND:
          value = "Zahlungsgrund";
          break;
        case MODUS:
          value = "Alle";
          break;
        case ABREACHNUNGSMONAT:
          if ((Integer) Einstellungen.getEinstellung(
              Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
          {
            value = "Januar";
          }
          break;
      }
      map.put(var.getName(), value);
    }

    return map;
  }
}
