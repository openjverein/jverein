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

import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatMM;
import de.jost_net.JVerein.util.JVDateFormatMMMM;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;

public class AbrechnungsParameterMap
{

  public Map<String, Object> getMap(AbrechnungSEPAParam param,
      Map<String, Object> inma)
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
    JVDateFormatTTMMJJJJ dateformatTTMMJJJJ = new JVDateFormatTTMMJJJJ();
    for (AbrechnungsParameterVar var : AbrechnungsParameterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ABBUCHUNGSMODUS:
          value = Abrechnungsmodi.get(param.abbuchungsmodus);
          break;
        case ABRECHNUNGSMONAT:
          value = Monat.getByKey(param.abrechnungsmonat).getText();
          break;
        case FAELLIGKEIT:
          value = "";
          if (param.faelligkeit != null)
          {
            value = dateformatTTMMJJJJ.format(param.faelligkeit);
          }
          break;
        case KOMPAKTEABBUCHUNG:
          value = param.kompakteabbuchung ? "J" : "N";
          break;
        case KURSTEILNEHMER:
          value = "";
          if (param.kursteilnehmer != null)
          {
            value = param.kursteilnehmer ? "J" : "N";
          }
          break;
        case SEPAPRINT:
          value = param.sepaprint ? "J" : "N";
          break;
        case STICHTAG:
          value = "";
          if (param.stichtag != null)
          {
            value = dateformatTTMMJJJJ.format(param.stichtag);
          }
          break;
        case STICHTAG_MONAT:
          value = "";
          if (param.stichtag != null)
          {
            value = new JVDateFormatMM().format(param.stichtag);
          }
          break;
        case STICHTAG_MONAT_TEXT:
          value = "";
          if (param.stichtag != null)
          {
            value = new JVDateFormatMMMM().format(param.stichtag);
          }
          break;
        case STICHTAG_JAHR:
          value = "";
          if (param.stichtag != null)
          {
            value = new JVDateFormatJJJJ().format(param.stichtag);
          }
          break;
        case VERWENDUNGSZWECK:
          value = param.verwendungszweck;
          break;
        case VONDATUM:
          value = "";
          if (param.vondatum != null)
          {
            value = dateformatTTMMJJJJ.format(param.vondatum);
          }
          break;
        case EINGABEVONDATUM:
          value = "";
          if (param.voneingabedatum != null)
          {
            value = dateformatTTMMJJJJ.format(param.voneingabedatum);
          }
          break;
        case BISDATUM:
          value = "";
          if (param.bisdatum != null)
          {
            value = dateformatTTMMJJJJ.format(param.bisdatum);
          }
          break;
        case ZUSATZBETRAEGE:
          value = "";
          if (param.zusatzbetraege != null)
          {
            value = param.zusatzbetraege ? "J" : "N";
          }
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
