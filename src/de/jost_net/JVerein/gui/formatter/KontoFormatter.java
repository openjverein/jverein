/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.formatter;

import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

import java.rmi.RemoteException;

public class KontoFormatter implements Formatter
{
  @Override
  public String format(Object o)
  {
    if (!(o instanceof Konto))
    {
      Logger.error("Kein Konto übergeben");
      return "";
    }

    Konto k = (Konto) o;
    try
    {
      return k.getBezeichnung();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return "";
  }
}
