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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public abstract class AbstractJVereinControl extends AbstractControl
{

  public AbstractJVereinControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Diese Funktion muss implementiert werden und das setzen der Werte im
   * JVereinDBObject übernehmen. Das Speichern soll in handleStore() erfolgen
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public abstract void fill() throws RemoteException, ApplicationException;
}
