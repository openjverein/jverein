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
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.BuchungsklasseMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.BuchungsklasseDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class BuchungsklasseListeControl extends FilterControl
{
  private JVereinTablePart buchungsklassenList;

  public BuchungsklasseListeControl(AbstractView view)
  {
    super(view, BuchungsklasseControl.class);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (buchungsklassenList != null)
    {
      return buchungsklassenList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Buchungsklasse> buchungsklassen = service
        .createList(Buchungsklasse.class);
    buchungsklassen.setOrder("ORDER BY nummer");

    buchungsklassenList = new JVereinTablePart(buchungsklassen, null);
    buchungsklassenList.addColumn(getGesperrtColumn(), false);
    buchungsklassenList.addColumn("Nummer", "nummer");
    buchungsklassenList.addColumn("Bezeichnung", "bezeichnung");
    buchungsklassenList
        .setContextMenu(new BuchungsklasseMenu(buchungsklassenList));
    buchungsklassenList.setMulti(true);
    buchungsklassenList.setAction(
        new EditAction(BuchungsklasseDetailView.class, buchungsklassenList));
    VorZurueckControl.setObjektListe(null, null);
    return buchungsklassenList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSKLASSEN_DATEINAME);
  }

}
