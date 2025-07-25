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
import java.util.LinkedList;
import java.util.List;

import de.jost_net.JVerein.gui.control.VorZurueckControl;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class EditAction implements Action
{
  private Class<? extends AbstractView> viewClass;

  private JVereinTablePart part = null;

  public EditAction(Class<? extends AbstractView> viewClass)
  {
    this.viewClass = viewClass;
  }

  public EditAction(Class<? extends AbstractView> viewClass,
      JVereinTablePart part)
  {
    this.viewClass = viewClass;
    this.part = part;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
    {
      throw new ApplicationException("Kein Objekt ausgewählt");
    }
    if (context instanceof Object[])
    {
      throw new ApplicationException("Mehrere Objekte ausgewählt");
    }
    if (part != null)
    {
      try
      {
        LinkedList<Long> objektListe = new LinkedList<>();
        for (DBObject obj : (List<DBObject>) part.getItems(false))
        {
          objektListe.add(Long.valueOf(obj.getID()));
        }
        VorZurueckControl.setObjektListe(
            (Class<? extends DBObject>) context.getClass(), objektListe);
      }
      catch (NumberFormatException | RemoteException e)
      {
        //
      }
    }
    GUI.startView(viewClass, context);
  }
}
