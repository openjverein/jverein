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
import de.jost_net.JVerein.gui.menu.EigenschaftMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.EigenschaftDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class EigenschaftListeControl extends FilterControl
{
  private JVereinTablePart eigenschaftList;

  public EigenschaftListeControl(AbstractView view)
  {
    super(view, EigenschaftControl.class);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (eigenschaftList != null)
    {
      return eigenschaftList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Eigenschaft> eigenschaften = service
        .createList(Eigenschaft.class);
    eigenschaften.setOrder("ORDER BY bezeichnung");

    eigenschaftList = new JVereinTablePart(eigenschaften, null);
    eigenschaftList.addColumn(getGesperrtColumn(), false);
    eigenschaftList.addColumn("Name", "name");
    eigenschaftList.addColumn("Bezeichnung", "bezeichnung");
    eigenschaftList.addColumn("Gruppe", "eigenschaftgruppe");
    eigenschaftList.setContextMenu(new EigenschaftMenu(eigenschaftList));
    eigenschaftList.setRememberState(true);
    eigenschaftList.setMulti(true);
    eigenschaftList.setAction(
        new EditAction(EigenschaftDetailView.class, eigenschaftList));
    VorZurueckControl.setObjektListe(null, null);
    return eigenschaftList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_DATEINAME);
  }

}
