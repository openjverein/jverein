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
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.util.Datum;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Jahresabschlusses
 */
public class JahresabschlussDeleteAction extends DeleteAction
{
  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Jahresabschluss))
    {
      return;
    }

    Jahresabschluss a = (Jahresabschluss) object;
    a.delete();
    DBIterator<Anfangsbestand> it = Einstellungen.getDBService()
        .createList(Anfangsbestand.class);
    it.addFilter("datum = ?", new Object[] { Datum.addTage(a.getBis(), 1) });
    while (it.hasNext())
    {
      Anfangsbestand a1 = it.next();
      Anfangsbestand a2 = (Anfangsbestand) Einstellungen.getDBService()
          .createObject(Anfangsbestand.class, a1.getID());
      a2.delete();
    }
  }

  @Override
  protected boolean supportsMulti()
  {
    return false;
  }
}
