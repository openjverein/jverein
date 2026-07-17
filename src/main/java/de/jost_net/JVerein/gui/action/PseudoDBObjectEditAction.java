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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class PseudoDBObjectEditAction implements Action
{
  private Class<? extends DBObject> objectClass;

  private Class<? extends AbstractView> viewClass;

  public PseudoDBObjectEditAction(Class<? extends DBObject> objectClass,
      Class<? extends AbstractView> viewClass)
  {
    this.objectClass = objectClass;
    this.viewClass = viewClass;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (!(context instanceof PseudoDBObject))
      {
        throw new ApplicationException("Kein Objekt ausgewählt");
      }
      PseudoDBObject o = (PseudoDBObject) context;
      if (o.getAttribute(PseudoDBObject.ID) != null)
      {
        String id = o.getAttribute(PseudoDBObject.ID).toString();
        Object object = Einstellungen.getDBService().createObject(objectClass,
            id);
        GUI.startView(viewClass, object);
      }
      else
      {
        throw new ApplicationException("Eintrag kann nicht bearbeitet werden");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }
}
