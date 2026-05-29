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
package de.jost_net.JVerein.io;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.view.IAuswertung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Mitgliedstyp;

public abstract class MitgliedAbstractPDF implements IAuswertung
{

  protected MitgliedControl control;

  protected Mitgliedstyp mitgliedstyp;

  protected String title = "";

  protected String subtitle = "";

  public MitgliedAbstractPDF(MitgliedControl control) throws RemoteException
  {
    this.control = control;
  }

  @Override
  public void beforeGo(String title, MitgliedAuswahl mitgliedAuswahl)
      throws RemoteException
  {
    mitgliedstyp = (Mitgliedstyp) control.getFilter().get(Filter.MITGLIEDSTYP);
    if (mitgliedstyp == null
        && mitgliedAuswahl.equals(MitgliedAuswahl.MITGLIEDER))
    {
      mitgliedstyp = Einstellungen.getDBService()
          .createObject(Mitgliedstyp.class, Mitgliedstyp.MITGLIED);
    }
    String ueberschrift = (String) control.getAuswertungUeberschrift()
        .getValue();
    if (ueberschrift.length() > 0)
    {
      subtitle = ueberschrift;
    }
    this.title = title;
  }
}
