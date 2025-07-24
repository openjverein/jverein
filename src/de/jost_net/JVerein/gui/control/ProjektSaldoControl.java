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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.AbstractView;

public class ProjektSaldoControl extends BuchungsklasseSaldoControl
{
  public ProjektSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    gruppenBezeichnung = "Projekt";
    mitOhneBuchungsart = false;
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    // Den Iterator aus BuchungsklasseSaldo erweitern um nach Projekten statt
    // nach Buchungsklassenzu gruppieren
    ExtendedDBIterator<PseudoDBObject> it = super.getIterator();

    // Wir überschreiben das "buchungsklasse" Feld mit dem Projektname
    it.addColumn("projekt.bezeichnung as " + BUCHUNGSKLASSE);
    it.addGroupBy("projekt.id");
    it.addGroupBy("projekt.bezeichnung");
    String on = "projekt.id = buchung.projekt ";
    if (mitSteuer)
    {
      on += " OR projekt.id = st.projekt";
    }
    it.join("projekt", on);

    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
    {
      case BuchungsartSort.NACH_NUMMER:
        it.setOrder("ORDER BY projekt.bezeichnung, -buchungsart.nummer DESC ");
        break;
      case BuchungsartSort.NACH_BEZEICHNUNG_NR:
      default:
        it.setOrder(
            "ORDER BY projekt.bezeichnung, buchungsart.bezeichnung is NUll,"
                + "buchungsart.bezeichnung ");
        break;
    }
    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return "Projekt-Saldo";
  }
}
