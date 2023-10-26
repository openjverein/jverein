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
package com.schlevoigt.JVerein.Queries;

import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;

public class BuchungsKorrekturQuery {

	private List<Buchung> ergebnis;

	public BuchungsKorrekturQuery() {
	}

	@SuppressWarnings("unchecked")
	public List<Buchung> get() throws RemoteException {
		final DBService service = Einstellungen.getDBService();

		DBIterator<Buchung> it = service.createList(Buchung.class);
		
		String text1 = "%SVWZ%";
		it.addFilter("upper(zweck) like ?", text1);
		
		it.setOrder("ORDER BY datum");

		this.ergebnis = PseudoIterator.asList(it);
		return ergebnis;
	}

	public int getSize() {
		return ergebnis.size();
	}

}
