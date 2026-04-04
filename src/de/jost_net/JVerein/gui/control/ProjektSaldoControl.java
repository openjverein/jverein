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
import java.util.List;

import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;

public class ProjektSaldoControl extends BuchungsklasseSaldoControl
{
  public ProjektSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    gruppenBezeichnung = "Projekt";
    mitOhneBuchungsart = false;
    mitBuchungsklasseSpalte = true;
    spalteGruppe = PROJEKT;
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    // Den Iterator aus BuchungsklasseSaldo erweitern um nach Projekten statt
    // nach Buchungsklassen zu gruppieren
    ExtendedDBIterator<PseudoDBObject> it = super.getIterator();

    it.addColumn("projekt.bezeichnung as " + PROJEKT);
    it.addColumn("buchung.projekt as " + PROJEKT_ID);

    it.join("projekt", "buchung.projekt = projekt.id");
    it.addGroupBy("projekt.id");
    it.addGroupBy("projekt.bezeichnung");

    return it;
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getSteuerIterator()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = super.getSteuerIterator();
    it.join("projekt", "buchung.projekt = projekt.id");
    it.addGroupBy("buchung.projekt");
    it.addColumn("projekt.id as " + PROJEKT_ID);
    it.addColumn("projekt.bezeichnung as " + PROJEKT);

    return it;
  }

  @Override
  protected void sortList(List<PseudoDBObject> list) throws RemoteException
  {
    super.sortList(list);
    list.sort((o1, o2) -> {
      try
      {
        if (o1.getAttribute(PROJEKT) == null)
        {
          return 1;
        }
        if (o2.getAttribute(PROJEKT) == null)
        {
          return -1;
        }

        return ((String) o1.getAttribute(PROJEKT))
            .compareTo((String) o2.getAttribute(PROJEKT));
      }
      catch (RemoteException e)
      {
        return 0;
      }
    });
  }

  @Override
  protected String getAuswertungTitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_TITEL, this);
  }

  @Override
  protected String getAuswertungSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_SUBTITEL, this);
  }

  @Override
  protected String getDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_DATEINAME, this);
  }
}
