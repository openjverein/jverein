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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.FormularfeldDetailView;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularfeldAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Formularfeld ff = null;
    Formular f = null;

    if (context != null && (context instanceof Formular))
    {
      f = (Formular) context;
      try
      {
        if (f.isNewObject())
        {
          throw new ApplicationException(
              "Vor dem Anlegen der Formularfelder muss das Formular gespeichert werden!");
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException(
            "Fehler bei der Erzeugung eines neuen Formularfeldes", e);
      }
    }
    if (context != null && (context instanceof Formularfeld))
    {
      ff = (Formularfeld) context;
    }
    else
    {
      try
      {
        ff = (Formularfeld) Einstellungen.getDBService().createObject(
            Formularfeld.class, null);
        ff.setFormular(f);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException(
            "Fehler bei der Erzeugung eines neuen Formularfeldes", e);
      }
    }
    // Wenn CurrentObject und View von aktueller und nächster View gleich
    // sind, wird die aktuelle View nicht in die History aufgenommen. Dadurch
    // führt der Zurückbutton auch bei "Speichern und neu" zur Liste zurück.
    if (GUI.getCurrentView().getClass().equals(FormularfeldDetailView.class))
    {
      GUI.getCurrentView().setCurrentObject(ff);
    }
    GUI.startView(FormularfeldDetailView.class, ff);
  }
}
