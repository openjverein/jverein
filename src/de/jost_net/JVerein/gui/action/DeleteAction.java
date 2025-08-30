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

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen eines Eintrags oder Einträge.
 */
public class DeleteAction implements Action
{
  private String name;

  private String namen;

  private String attribute;

  public DeleteAction(String name, String namen)
  {
    this.name = name;
    this.namen = namen;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
    {
      throw new ApplicationException("Kein Objekt ausgewählt");
    }
    DBObject[] objekte = null;
    if (context instanceof DBObject)
    {
      objekte = new DBObject[] { (DBObject) context };
    }
    else if (context instanceof DBObject[])
    {
      objekte = (DBObject[]) context;
    }

    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(name + " löschen");
    d.setText(String.format("Wollen Sie %d %s wirklich löschen?",
        objekte.length, (objekte.length == 1 ? name : namen)));
    Boolean choice;
    try
    {
      choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
    }
    catch (Exception e1)
    {
      Logger.error("Fehler beim Löschen von " + namen + ": ", e1);
      return;
    }

    try
    {
      DBTransaction.starten();
      for (DBObject o : objekte)
      {
        if (o.isNewObject())
        {
          continue;
        }
        attribute = (String) o.getAttribute(o.getPrimaryAttribute());
        o.delete();
      }
      DBTransaction.commit();
      GUI.getStatusBar()
          .setSuccessText((objekte.length == 1 ? name : namen) + " gelöscht.");
    }
    catch (RemoteException e2)
    {
      DBTransaction.rollback();
      String fehler = "Fehler beim Löschen von " + name + " " + attribute
          + ". Es wird eventuell von anderen Objekten referenziert.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e2);
    }
  }
}
