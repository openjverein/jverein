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

import de.jost_net.JVerein.gui.control.Savable;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Fertig konfigurierter Speichern Button
 */
public class SaveButton extends ButtonRtoL
{
  /**
   * Erstellt den Speichern Button
   * 
   * @param control
   *          Das control
   */
  public SaveButton(Savable control)
  {
    super("Speichern", context -> {
      try
      {
        control.handleStore();
        GUI.getStatusBar().setSuccessText("Gespeichert");
      }
      catch (ApplicationException ae)
      {
        GUI.getStatusBar().setErrorText(ae.getMessage());
      }
    }, null, true, "document-save.png");
  }

}
