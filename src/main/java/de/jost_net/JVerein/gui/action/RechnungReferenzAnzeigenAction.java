/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung für Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2015 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Abrechnungslaufes
 */
public class RechnungReferenzAnzeigenAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Rechnung))
    {
      throw new ApplicationException("Keine Rechnung ausgewählt");
    }

    Rechnung re = (Rechnung) context;
    Rechnung ref;
    try
    {
      ref = (Rechnung) Einstellungen.getDBService().createObject(Rechnung.class,
          re.getReferenzrechnungID().toString());
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
      throw new ApplicationException(e);
    }
    GUI.startView(RechnungDetailView.class, ref);
  }
}
