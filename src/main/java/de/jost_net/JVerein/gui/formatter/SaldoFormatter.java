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

import org.eclipse.swt.widgets.TableItem;

import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.logging.Logger;

public class SaldoFormatter implements TableFormatter
{
  @Override
  public void format(TableItem item)
  {
    if (item == null)
      return;
    try
    {
      PseudoDBObject o = (PseudoDBObject) item.getData();
      if (o == null || o.getAttribute(AbstractSaldoControl.ART) == null)
        return;

      switch ((Integer) o.getAttribute(AbstractSaldoControl.ART))
      {
        case AbstractSaldoControl.ART_HEADER:
          item.setBackground(Color.COMMENT.getSWTColor());
          break;
        case AbstractSaldoControl.ART_SALDOFOOTER:
        case AbstractSaldoControl.ART_SALDOGEWINNVERLUST:
        case AbstractSaldoControl.ART_GESAMTSALDOFOOTER:
        case AbstractSaldoControl.ART_GESAMTGEWINNVERLUST:
          item.setFont(Font.BOLD.getSWTFont());
          break;
        case AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN:
          item.setFont(Font.ITALIC.getSWTFont());
          break;
        case AbstractSaldoControl.ART_DETAIL:
        case AbstractSaldoControl.ART_LEERZEILE:
          break;
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Formattieren des Saldos", e);
    }
  }
}
