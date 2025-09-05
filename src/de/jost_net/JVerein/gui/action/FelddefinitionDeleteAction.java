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
import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Löschen einer Felddefiniton
 */
public class FelddefinitionDeleteAction extends DeleteAction
{
  // Speichert, ob die Felddefinition bei Mitgliedern verwendet wird
  private boolean verwendung = false;

  @Override
  protected String getText(JVereinDBObject object[])
      throws RemoteException, ApplicationException
  {
    if (object == null || object.length == 0
        || !(object[0] instanceof Felddefinition))
    {
      throw new ApplicationException("Kein Zusatzfeld ausgewählt");
    }

    // Check ob eine Felddefinition bei Mitgliedern verwendet wird
    for (JVereinDBObject o : object)
    {
      DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
          .createList(Zusatzfelder.class);
      it.addFilter("felddefinition=?", new Object[] { o.getID() });
      if (it.size() > 0)
      {
        verwendung = true;
      }
    }

    if (!verwendung)
    {
      return "Wollen Sie " + object.length + " Zusatzfeld"
          + (object.length > 1 ? "er" : "") + " wirklich löschen?";
    }
    else
    {
      if (object.length == 1)
      {
        return "Das Zusatzfeld wird bereits von Mitgliedern verwendet.\n"
            + "Soll es trotzdem gelöscht werden?";
      }
      else
      {
        return "Mindestens ein Zusatzfeld wird bereits von Mitgliedern verwendet.\n"
            + "Auch bereits verwendete Zusatzfelder löschen?";
      }
    }
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Felddefinition))
    {
      return;
    }

    DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
        .createList(Zusatzfelder.class);
    it.addFilter("felddefinition=?", new Object[] { object.getID() });
    if (it.size() > 0 && selection == YesNoCancelDialog.NO)
    {
      throw new ApplicationException(
          "Übersprungen, da es bereits von Mitgliedern verwendet wird.");
    }
    while (it.hasNext())
    {
      Zusatzfelder zf1 = it.next();
      Zusatzfelder zf2 = (Zusatzfelder) Einstellungen.getDBService()
          .createObject(Zusatzfelder.class, zf1.getID());
      zf2.delete();
    }
    object.delete();
  }

  @Override
  protected boolean getMitNo(JVereinDBObject object[])
  {
    return verwendung && object.length > 1;
  }
}
