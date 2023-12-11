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
package de.jost_net.JVerein.gui.formatter;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

public class FormularLinkFormatter implements Formatter
{
  @Override
  public String format(Object o)
  {
    if (o == null)
    {
      return "";
    }
    if (o instanceof Long)
    {
      try
      {
        Formular f = (Formular) Einstellungen.getDBService()
            .createObject(Formular.class, o.toString());
        if (f == null) 
        {
          return "Das Formular mit der ID " + o.toString() + " wurde gelöscht.";
        }
        return f.getBezeichnung();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    }

    return "";
  }
}