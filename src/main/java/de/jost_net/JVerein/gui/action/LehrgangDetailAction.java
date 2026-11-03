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

import de.jost_net.JVerein.gui.view.LehrgangDetailView;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class LehrgangDetailAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Lehrgang lg = null;
    try
    {
      if (context instanceof Lehrgangsteilnehmer)
      {
        Lehrgangsteilnehmer l = (Lehrgangsteilnehmer) context;
        lg = l.getLehrgang();
      }
      if (lg != null)
      {
        GUI.startView(new LehrgangDetailView(), lg);
      }
      else
      {
        throw new ApplicationException("Kein Lehrgang gefunden.");
      }
    }
    catch (OperationCanceledException | ApplicationException ex)
    {
      throw ex;
    }
    catch (Exception e)
    {
      throw new ApplicationException("Fehler beim Öffnen der Lehrgangsanzeige.",
          e);
    }
  }
}
