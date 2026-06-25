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
import java.util.ArrayList;

import de.willuhn.jameica.gui.input.SelectInput;

/**
 * Combo-Box, fuer die Font Auswahl.
 */
public class FontInput extends SelectInput
{

  public FontInput(String font) throws RemoteException
  {
    super(init(), font);
    setName("Font");
  }

  /**
   * @return initialisiert die Liste der Fonts.
   * @throws RemoteException
   */
  private static ArrayList<String> init()
  {
    ArrayList<String> fonts = new ArrayList<>();
    fonts.add("Carlito-Regular");
    fonts.add("Carlito-Bold");
    fonts.add("Carlito-Italic");
    fonts.add("Carlito-BoldItalic");
    fonts.add("PTSans-Regular");
    fonts.add("PTSans-Bold");
    fonts.add("PTSans-Italic");
    fonts.add("PTSans-BoldItalic");
    fonts.add("FreeSans");
    fonts.add("FreeSans-Bold");
    fonts.add("FreeSans-BoldOblique");
    fonts.add("FreeSans-Oblique");
    fonts.add("Courier Prime");
    fonts.add("Courier Prime Bold");
    fonts.add("Courier Prime Bold Italic");
    fonts.add("Courier Prime Italic");
    fonts.add("LiberationSans-Bold");
    fonts.add("LiberationSans-BoldItalic");
    fonts.add("LiberationSans-Italic");
    fonts.add("LiberationSans-Regular");
    fonts.add("LiberationSerif-Bold");
    fonts.add("LiberationSerif-BoldItalic");
    fonts.add("LiberationSerif-Italic");
    fonts.add("LiberationSerif-Regular");
    return fonts;
  }

}
