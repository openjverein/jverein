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
package de.jost_net.JVerein.gui.input;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.keys.Staat;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.logging.Logger;

public class StaatSearchInput extends SearchInput
{
  public StaatSearchInput()
  {
    super();
  }

  @Override
  @SuppressWarnings("rawtypes")
  public List startSearch(String text)
  {
    List<Staat> list = new ArrayList<>();
    try
    {
      for (Staat staat : Staat.values())
      {
        if (staat.getText().contains(text.toUpperCase()))
        {
          list.add(staat);
        }
      }
      return list;
    }
    catch (Exception e)
    {
      Logger.error("Unable to load staat list", e);
      return null;
    }
  }

}
