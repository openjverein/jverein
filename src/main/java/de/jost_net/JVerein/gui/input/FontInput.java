/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;

import de.jost_net.JVerein.keys.Fonts;
import de.willuhn.jameica.gui.input.SelectInput;

/**
 * Combo-Box, fuer die Font Auswahl.
 */
public class FontInput extends SelectInput
{

  public FontInput(String font) throws RemoteException
  {
    super(Fonts.values(), Fonts.getByName(font));
    setName("Font");
    setAttribute("name");
  }

  @Override
  public Object getValue()
  {
    return ((Fonts) super.getValue()).getName();
  }
}
