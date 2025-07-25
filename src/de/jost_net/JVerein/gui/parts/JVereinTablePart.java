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
package de.jost_net.JVerein.gui.parts;

import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;

public class JVereinTablePart extends TablePart
{

  /**
   * Erzeugt eine neue leere Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(Action action)
  {
    super(action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(@SuppressWarnings("rawtypes") GenericIterator list,
      Action action)
  {
    super(list, action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(@SuppressWarnings("rawtypes") List list,
      Action action)
  {
    super(list, action);
  }

  public void setAction(Action action)
  {
    this.action = action;
  }

}
