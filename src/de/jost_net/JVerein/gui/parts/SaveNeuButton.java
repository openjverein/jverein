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

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Fertig konfigurierter Speichern und Neu Button
 */
public class SaveNeuButton extends Button
{

  /**
   * Erstellt den Speichern un NeuButton
   * 
   * @param control
   *          Das control
   */
  public SaveNeuButton(Savable control)
  {
    this(control, true);
  }

  /**
   * Erstellt den Speichern un NeuButton
   * 
   * @param control
   *          Das control
   * @param noHistory
   *          Keine View Historie
   */
  public SaveNeuButton(Savable control, boolean noHistory)
  {
    super("Speichern und neu", context -> {
      try
      {
        control.handleStore();
        AbstractView view = GUI.getCurrentView();
        DBObject object = (DBObject) view.getCurrentObject();
        new NewAction(view.getClass(), object.getClass(), noHistory)
            .handleAction(null);
        GUI.getStatusBar().setSuccessText("Gespeichert");
      }
      catch (ApplicationException ae)
      {
        GUI.getStatusBar().setErrorText(ae.getMessage());
      }
    }, null, false, "go-next.png");
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    if (((DBObject) GUI.getCurrentView().getCurrentObject()).isNewObject())
    {
      super.paint(parent);
    }
  }
}
