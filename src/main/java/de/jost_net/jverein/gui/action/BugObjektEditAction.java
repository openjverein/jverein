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
package de.jost_net.jverein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.jverein.gui.view.BuchungDetailView;
import de.jost_net.jverein.gui.view.LastschriftDetailView;
import de.jost_net.jverein.gui.view.MitgliedDetailView;
import de.jost_net.jverein.gui.view.NichtMitgliedDetailView;
import de.jost_net.jverein.gui.view.RechnungDetailView;
import de.jost_net.jverein.gui.view.SollbuchungDetailView;
import de.jost_net.jverein.rmi.Buchung;
import de.jost_net.jverein.rmi.Lastschrift;
import de.jost_net.jverein.rmi.Mitglied;
import de.jost_net.jverein.rmi.Mitgliedstyp;
import de.jost_net.jverein.rmi.Rechnung;
import de.jost_net.jverein.rmi.Sollbuchung;
import de.jost_net.jverein.server.Bug;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Editieren von im Bug referenzierten Objekten
 */
public class BugObjektEditAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Bug))
    {
      throw new ApplicationException("Kein Bug ausgewählt");
    }

    Bug bug = (Bug) context;
    Object object = bug.getObject();
    if (object instanceof Mitglied)
    {
      Mitglied m = (Mitglied) object;
      try
      {
        if (m.getMitgliedstyp() == null
            || m.getMitgliedstyp().getID().equals(Mitgliedstyp.MITGLIED))
        {
          GUI.startView(new MitgliedDetailView(), m);
        }
        else
        {
          GUI.startView(new NichtMitgliedDetailView(), m);
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Anzeigen eines Mitgliedes",
            e);
      }
    }
    if (object instanceof Lastschrift)
    {
      GUI.startView(LastschriftDetailView.class, object);
    }
    if (object instanceof Rechnung)
    {
      GUI.startView(RechnungDetailView.class, object);
    }
    if (object instanceof Sollbuchung)
    {
      GUI.startView(SollbuchungDetailView.class, object);
    }
    if (object instanceof Buchung)
    {
      GUI.startView(BuchungDetailView.class, object);
    }
  }
}
