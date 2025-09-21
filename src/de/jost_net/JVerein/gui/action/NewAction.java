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
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.IMitglied;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class NewAction implements Action
{
  private Class<? extends AbstractView> viewClass;

  private Class<? extends DBObject> objectClass;

  private boolean noHistory = false;

  private Mitglied mitglied = null;

  public NewAction(Class<? extends AbstractView> viewClass,
      Class<? extends DBObject> objectClass)
  {
    this(viewClass, objectClass, false, null);
  }

  public NewAction(Class<? extends AbstractView> viewClass,
      Class<? extends DBObject> objectClass, boolean noHistory)
  {
    this(viewClass, objectClass, noHistory, null);
  }

  public NewAction(Class<? extends AbstractView> viewClass,
      Class<? extends DBObject> objectClass, Mitglied mitglied)
  {
    this(viewClass, objectClass, false, mitglied);
  }

  public NewAction(Class<? extends AbstractView> viewClass,
      Class<? extends DBObject> objectClass, boolean noHistory,
      Mitglied mitglied)
  {
    this.viewClass = viewClass;
    this.objectClass = objectClass;
    this.noHistory = noHistory;
    this.mitglied = mitglied;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      DBObject object = Einstellungen.getDBService().createObject(objectClass,
          null);
      if (mitglied != null)
      {
        if (mitglied.getID() == null)
        {
          throw new ApplicationException(
              "Neues Mitglied bitte erst speichern.");
        }
        if (object instanceof IMitglied)
        {
          ((IMitglied) object)
              .setMitglied(Integer.valueOf(mitglied.getID()).intValue());
        }
      }
      if (noHistory)
      {
        // Wenn CurrentObject und View von aktueller und nächster View gleich
        // sind, wird die aktuelle View nicht in die History aufgenommen.
        // Dadurch führt der Zurückbutton auch bei "Speichern und neu" zur Liste
        // zurück.
        if (GUI.getCurrentView().getClass().equals(viewClass))
        {
          GUI.getCurrentView().setCurrentObject(object);
        }
      }
      GUI.startView(viewClass, object);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung eines neuen " + objectClass.getSimpleName(),
          e);
    }
  }
}
