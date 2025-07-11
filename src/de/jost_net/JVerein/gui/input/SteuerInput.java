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

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SelectInput;

/**
 * Combo-Box, fuer die Auswahl der Steuer
 */
public class SteuerInput extends SelectInput
{

  /**
   * Steuer Select Input
   * 
   * @param def
   *          die vorausgewählte Steuer
   * @throws RemoteException
   */
  public SteuerInput(Steuer def) throws RemoteException
  {
    super(init(def) != null ? PseudoIterator.asList(init(def)) : null, def);
  }

  /**
   * @return initialisiert die Liste der Optionen.
   * @param def
   *          Die Vorausgewählte Steuer (Wird immer angezeigt, auch wen inaktiv)
   * @throws RemoteException
   */
  private static GenericIterator<Steuer> init(Steuer def) throws RemoteException
  {
    DBIterator<Steuer> it = Einstellungen.getDBService()
        .createList(Steuer.class);
    if (def == null)
    {
      it.addFilter("aktiv = true");
    }
    else
    {
      it.addFilter("aktiv = true or id = ?", def.getID());
    }

    return it;
  }
}
