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
package de.jost_net.JVerein.util;

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Vorlage;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;

public class VorlageUtil
{

  // Namen Generierung aus Vorlagen Muster
  public static String getName(VorlageTyp typ)
  {
    return getName(typ, null, null);
  }

  public static String getName(VorlageTyp typ, Object obj)
  {
    return getName(typ, obj, null);
  }

  public static String getName(VorlageTyp typ, Object obj, Mitglied mitglied)
  {
    Map<String, Object> map = null;
    String muster = "";
    try
    {
      map = new AllgemeineMap().getMap(null);
      muster = getVorlageMuster(typ);
      switch (typ)
      {
        case SPENDENBESCHEINIGUNG:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case RECHNUNG_MITGLIED:
        case MAHNUNG_MITGLIED:
          // Ein Dokument pro Mitglied
          map = new RechnungMap().getMap((Rechnung) obj, map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case KONTOAUSZUG_MITGLIED:
        case PRENOTIFICATION_MITGLIED:
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case FREIES_FORMULAR:
          map.put("formular_name", (String) obj);
          break;
        case FREIES_FORMULAR_MITGLIED:
          map = new MitgliedMap().getMap(mitglied, map);
          map.put("formular_name", (String) obj);
          break;
        case RECHNUNG:
        case MAHNUNG:
        case KONTOAUSZUG:
        case CT1_AUSGABE:
        case PRENOTIFICATION:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        default:
          Logger.error("Dateiname Typ nicht implementiert: " + typ.toString());
          return "";
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return translate(map, muster);
  }

  // Dummy Namen Generierung aus Vorlagen Muster
  public static String getDummyName(VorlageTyp typ)
  {
    String muster = "";
    try
    {
      muster = getVorlageMuster(typ);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return getDummyName(typ, muster);
  }

  // Dummy Namen Generierung aus Vorlagen Muster
  public static String getDummyName(VorlageTyp typ, String muster)
  {
    return translate(getDummyMap(typ), muster);
  }

  public static Map<String, Object> getDummyMap(VorlageTyp typ)
  {
    Map<String, Object> map = null;
    try
    {
      map = new AllgemeineMap().getMap(null);
      switch (typ)
      {
        case SPENDENBESCHEINIGUNG:
          map = SpendenbescheinigungMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case RECHNUNG_MITGLIED:
        case MAHNUNG_MITGLIED:
          map = RechnungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case KONTOAUSZUG_MITGLIED:
        case PRENOTIFICATION_MITGLIED:
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIES_FORMULAR:
          map.put("formular_name", "Freies Formular");
          break;
        case FREIES_FORMULAR_MITGLIED:
          map = MitgliedMap.getDummyMap(map);
          map.put("formular_name", "Freies Formular");
          break;
        case RECHNUNG:
        case MAHNUNG:
        case KONTOAUSZUG:
        case CT1_AUSGABE:
        case PRENOTIFICATION:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        default:
          Logger.error("Dateiname Typ nicht implementiert: " + typ.toString());
          break;
      }
    }
    catch (RemoteException e)
    {
      //
    }
    return map;
  }

  public static String translate(Map<String, Object> map, String inString)
  {
    Velocity.init();
    VelocityContext context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    VarTools.add(context, map);
    StringWriter wdateiname = new StringWriter();
    String in = inString.replaceAll("-\\$", " \\$");
    Velocity.evaluate(context, wdateiname, "LOG", in);
    String str = wdateiname.toString();
    str = str.replaceAll(" ", "-");
    return str;
  }

  public static String getVorlageMuster(VorlageTyp typ) throws RemoteException
  {
    DBIterator<Vorlage> vorlagen = Einstellungen.getDBService()
        .createList(Vorlage.class);
    vorlagen.addFilter("key = ?", typ.getKey());
    if (vorlagen.hasNext())
    {
      return vorlagen.next().getMuster();
    }
    return "";
  }
}
