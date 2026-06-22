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
package de.jost_net.jverein.io;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.jverein.Einstellungen;
import de.jost_net.jverein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.jverein.util.UniversalDateFormat;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class VelocityTool
{
  static
  {
    try
    {
      Velocity.init();
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Velocity Init", e);
    }
  }

  public static String eval(Map<String, Object> map, String text)
      throws ApplicationException
  {
    return eval(map, text, false);
  }

  public static String eval(Map<String, Object> map, String text,
      boolean kuerzen) throws ApplicationException
  {
    VelocityContext context = new VelocityContext(
        new HashMap<String, Object>(map));
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    context.put("udateformat", new UniversalDateFormat());

    StringWriter wtext = new StringWriter();
    try
    {
      Velocity.evaluate(context, wtext, "LOG", text);
    }
    catch (Exception e)
    {
      String t = "Fehler bei der Aufbereitung des Textes ("
          + text.split("\n")[0] + ")";
      Logger.error(t, e);
      throw new ApplicationException(t + ": " + e.getMessage().split("\n")[0]);
    }
    String txt = wtext.getBuffer().toString();
    if (kuerzen && txt.length() >= 140)
    {
      txt = txt.substring(0, 136) + "...";
    }
    return txt;
  }
}
