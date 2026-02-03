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

import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatMM;
import de.jost_net.JVerein.util.JVDateFormatMMMM;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;

public class AbrechnungsParameterMap
{

  public AbrechnungsParameterMap()
  {

  }

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
    map.put(AbrechnungsParameterVar.ABBUCHUNGSMODUS.getName(),
        Abrechnungsmodi.get(param.abbuchungsmodus));
    map.put(AbrechnungsParameterVar.ABRECHNUNGSMONAT.getName(),
        Monat.getByKey(param.abrechnungsmonat));
    if (param.faelligkeit != null)
    {
      map.put(AbrechnungsParameterVar.FAELLIGKEIT.getName(),
          new JVDateFormatTTMMJJJJ().format(param.faelligkeit));
    }
    else
    {
      map.put(AbrechnungsParameterVar.FAELLIGKEIT.getName(), "");
    }
    map.put(AbrechnungsParameterVar.KOMPAKTEABBUCHUNG.getName(),
        param.kompakteabbuchung ? "J" : "N");
    if (param.kursteilnehmer != null)
    {
      map.put(AbrechnungsParameterVar.KURSTEILNEHMER.getName(),
          param.kursteilnehmer ? "J" : "N");
    }
    else
    {
      map.put(AbrechnungsParameterVar.KURSTEILNEHMER.getName(), "");
    }
    map.put(AbrechnungsParameterVar.SEPAPRINT.getName(),
        param.sepaprint ? "J" : "N");
    if (param.stichtag != null)
    {
      map.put(AbrechnungsParameterVar.STICHTAG.getName(),
          new JVDateFormatTTMMJJJJ().format(param.stichtag));
      map.put(AbrechnungsParameterVar.STICHTAG_MONAT.getName(),
          new JVDateFormatMM().format(param.stichtag));
      map.put(AbrechnungsParameterVar.STICHTAG_MONAT_TEXT.getName(),
          new JVDateFormatMMMM().format(param.stichtag));
      map.put(AbrechnungsParameterVar.STICHTAG_JAHR.getName(),
          new JVDateFormatJJJJ().format(param.stichtag));
    }
    else
    {
      map.put(AbrechnungsParameterVar.STICHTAG.getName(), "");
      map.put(AbrechnungsParameterVar.STICHTAG_MONAT.getName(), "");
      map.put(AbrechnungsParameterVar.STICHTAG_MONAT_TEXT.getName(), "");
      map.put(AbrechnungsParameterVar.STICHTAG_JAHR.getName(), "");
    }
    map.put(AbrechnungsParameterVar.VERWENDUNGSZWECK.getName(),
        param.verwendungszweck);
    if (param.vondatum != null)
    {
      map.put(AbrechnungsParameterVar.VONDATUM.getName(),
          new JVDateFormatTTMMJJJJ().format(param.vondatum));
    }
    else
    {
      map.put(AbrechnungsParameterVar.VONDATUM.getName(), "");
    }
    if (param.bisdatum != null)
    {
      map.put(AbrechnungsParameterVar.BISDATUM.getName(),
          new JVDateFormatTTMMJJJJ().format(param.bisdatum));
    }
    else
    {
      map.put(AbrechnungsParameterVar.BISDATUM.getName(), "");
    }
    if (param.zusatzbetraege != null)
    {
      map.put(AbrechnungsParameterVar.ZUSATZBETRAEGE.getName(),
          param.zusatzbetraege ? "J" : "N");
    }
    else
    {
      map.put(AbrechnungsParameterVar.ZUSATZBETRAEGE.getName(), "");
    }
    return map;
  }
}
