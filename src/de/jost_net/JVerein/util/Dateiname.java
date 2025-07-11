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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.DateinameTyp;
import de.jost_net.JVerein.rmi.DateinamenVorlage;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.logging.Logger;

/**
 * <p>
 * Generierung eines Dateinamens unter Berücksichtigung von vorgegebener
 * Variablen
 * </p>
 * 
 * <dl>
 * <dt>a$</dt>
 * <dd>Aufgabe (z. B. auswertung, abbuchung)</dd>
 * <dt>d$</dt>
 * <dd>Aktuelles Datum (z. B. 20080101) oder das Datum des Dokumentes, wenn
 * übergeben</dd>
 * <dt>$z</dt>
 * <dd>Aktuelle Zeit (z. B. 120503) oder vom übergebenen Datum</dd>
 * <dt>$s</dt>
 * <dd>Sortierung. Wird nicht immer gefüllt. Ggfls. Leerstring.</dd>
 * <dt>$n</dt>
 * <dd>Name des Mitglieds. Wird nicht immer gefüllt. Ggfls. Leerstring.</dd>
 * <dt>$v</dt>
 * <dd>Vorname des Mitglieds. Wird nicht immer gefüllt. Ggfls. Leerstring.</dd>
 * <dt>$o</dt>
 * <dd>Bezeichnung des Dokuments. Wird nicht immer gefüllt. Ggfls. Leerstring.</dd>
 * </dl>
 */

public class Dateiname
{

  private Date datum = new Date();

  private String mitgliedName;

  private String mitgliedVorname;

  private String aufgabe;

  private String muster;

  private String extension;

  private String sortierung;

  /**
   * Konstruktor
   * 
   * @param aufgabe
   * @param sortierung
   * @param muster
   * @param extension
   */
  public Dateiname(String aufgabe, String sortierung, String muster,
      String extension)
  {
    this.aufgabe = aufgabe;
    this.muster = muster;
    this.extension = extension;
    this.sortierung = sortierung;
    this.mitgliedName = "";
    this.mitgliedVorname = "";
  }

  /**
   * Konstruktor für die Erzeugung eines Dateinames für ein Mitgliedsdokument
   * 
   * @param mitglied
   *        Das Mitglied
   * @param datum
   *        Datum des Dokuments
   * @param aufgabe
   *        Aufgabe (z.B. Spendenbescheinigung)
   * @param muster
   *        Dateinamenmuster
   * @param extension
   *        Extension der Datei
   */
  public Dateiname(Mitglied mitglied, Date datum, String aufgabe,
      String muster, String extension)
  {
    this.mitgliedName = "";
    this.mitgliedVorname = "";
    if (mitglied != null)
    {
      try
      {
        this.mitgliedName = mitglied.getName();
        this.mitgliedVorname = mitglied.getVorname();
      }
      catch (RemoteException re)
      {
        //
      }
    }
    if (datum != null)
    {
      this.datum = datum;
    }
    this.aufgabe = aufgabe;
    this.muster = muster;
    this.extension = extension;
    this.sortierung = "";
  }

  /**
   * Konstruktor für die Erzeugung eines Dateinames für ein Mitglied
   * 
   * @param name
   *        Name des Mitglieds
   * @param vorname
   *        Vorname des Mitglieds
   * @param datum
   *        Datum des Dokuments
   * @param aufgabe
   *        Aufgabe (z.B. Spendenbescheinigung)
   * @param muster
   *        Dateinamenmuster
   * @param extension
   *        Extension der Datei
   */
  public Dateiname(String name, String vorname, Date datum, String aufgabe,
      String muster, String extension)
  {
    this.mitgliedName = name;
    this.mitgliedVorname = vorname;
    if (datum != null)
    {
      this.datum = datum;
    }
    this.aufgabe = aufgabe;
    this.muster = muster;
    this.extension = extension;
    this.sortierung = "";
  }

  /**
   * Gibt den aufbereiteten String zurück. Wurde ein leeres Muster übergeben,
   * wird ein Leerstring zurückgegeben.
   */
  public String get()
  {
    if (muster != null && muster.length() > 0)
    {
      String ret = muster;
      // a$ = Aufgabe, s$ = Sortierung, d$ = Datum, z$ = Zeit
      ret = ret.replace("a$", aufgabe);
      ret = ret.replace("d$", new SimpleDateFormat("yyyyMMdd").format(datum));
      ret = ret.replace("z$", new SimpleDateFormat("HHmmss").format(datum));
      ret = ret.replace("s$", sortierung);
      // n$ = Name, v$ = Vorname, d$ = Datum
      ret = ret.replace("o$", aufgabe); // wird für den Umstieg von 2.0 -> 2.1
                                        // zur Kompatibilität benötigt.
      ret = ret.replace("n$", mitgliedName);
      ret = ret.replace("v$", mitgliedVorname);
      ret = ret.replace("/", " ");
      return ret + "." + extension;
    }
    return "";
  }

  public static String getDateiname(DateinameTyp typ)
  {
    return getDateiname(typ, null, null);
  }

  public static String getDateiname(DateinameTyp typ, Object obj)
  {
    return getDateiname(typ, obj, null);
  }

  public static String getDateiname(DateinameTyp typ, Object obj,
      Mitglied mitglied)
  {
    Map<String, Object> map = null;
    String dateiname = "";
    try
    {
      map = new AllgemeineMap().getMap(null);
      dateiname = ((DateinamenVorlage) Einstellungen.getDBService()
          .createObject(DateinamenVorlage.class, String.valueOf(typ.getKey())))
              .getDateiname();
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
    return translate(map, dateiname);
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
}
