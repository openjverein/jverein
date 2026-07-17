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
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.server.AbstractJVereinDBObject;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class PseudoDBObjectDeleteAction extends DeleteAction
{
  private Class<? extends DBObject> objectClass;

  public PseudoDBObjectDeleteAction(Class<? extends DBObject> objectClass)
  {
    this.objectClass = objectClass;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (!(context instanceof PseudoDBObject
          || context instanceof PseudoDBObject[]))
      {
        throw new ApplicationException("Kein Objekt ausgewählt");
      }

      PseudoDBObject[] pseudoObjects;
      if (context instanceof PseudoDBObject)
      {
        pseudoObjects = new PseudoDBObject[] { (PseudoDBObject) context };
      }
      else
      {
        pseudoObjects = (PseudoDBObject[]) context;
      }

      List<AbstractJVereinDBObject> jvereinDBObjects = new ArrayList<>();
      for (PseudoDBObject o : pseudoObjects)
      {
        if (o.getAttribute(PseudoDBObject.ID) != null)
        {
          jvereinDBObjects.add(Einstellungen.getDBService().createObject(
              objectClass, o.getAttribute(PseudoDBObject.ID).toString()));
        }
      }

      super.handleAction(
          jvereinDBObjects.toArray(new AbstractJVereinDBObject[0]));
    }
    catch (RemoteException e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }

  protected void doFinally() throws RemoteException, ApplicationException
  {
    GUI.getCurrentView().reload();
  }
}
