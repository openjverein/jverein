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
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Abrechnungslaufes
 */
public class LehrgangsartDeleteAction extends DeleteAction
{

  List<String> benutzteLehrgangsarten = new ArrayList<>();

  @Override
  protected String getText(JVereinDBObject[] object)
      throws RemoteException, ApplicationException
  {
    if (object == null || object.length == 0
        || !(object[0] instanceof Lehrgangsart))
    {
      throw new ApplicationException("Keine Lehrgangsart ausgewählt");
    }
    for (JVereinDBObject o : object)
    {
      Lehrgangsart la = (Lehrgangsart) o;
      DBIterator<Lehrgangsteilnehmer> it = Einstellungen.getDBService()
          .createList(Lehrgangsteilnehmer.class);
      it.addFilter("lehrgangsart = ?", la.getID());
      it.addFilter("lehrgang IS NULL");
      it.setLimit(1);
      if (it.hasNext())
      {
        benutzteLehrgangsarten.add(la.getID());
      }
    }
    if (benutzteLehrgangsarten.size() > 0)
    {
      return ("Für mindestens eine Lehrgangsart existieren Lehrgangsteilnehmer "
          + "für die kein Lehrgang konfiguriert ist.\n"
          + "Sollen diese auch gelöscht werden?");
    }
    return String.format("Wollen Sie %d %s wirklich löschen?", object.length,
        (object.length == 1 ? name : namen));
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Lehrgangsart))
    {
      return;
    }

    try
    {
      if (benutzteLehrgangsarten.contains(((Lehrgangsart) object).getID())
          && selection == YesNoCancelDialog.NO)
      {
        throw new ApplicationException(
            "Übersprungen, da es bereits von Lehrgangsteilnehmern verwendet wird.");
      }
      object.delete();
    }
    catch (Exception e)
    {
      throw e;
    }
  }

  protected boolean getMitNo(JVereinDBObject[] object)
  {
    return benutzteLehrgangsarten.size() > 0 ? true : false;
  }
}
