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
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.EigenschaftGruppeMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class EigenschaftGruppeControl extends FilterControl
{
  private JVereinTablePart eigenschaftgruppeList;

  public EigenschaftGruppeControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (eigenschaftgruppeList != null)
    {
      return eigenschaftgruppeList;
    }

    DBService service = Einstellungen.getDBService();
    DBIterator<EigenschaftGruppe> eigenschaftgruppe = service
        .createList(EigenschaftGruppe.class);
    eigenschaftgruppe.setOrder("ORDER BY bezeichnung");

    eigenschaftgruppeList = new JVereinTablePart(eigenschaftgruppe, null);
    eigenschaftgruppeList.addColumn(getGesperrtColumn(), false);
    eigenschaftgruppeList.addColumn("Name", "name");
    eigenschaftgruppeList.addColumn("Bezeichnung", "bezeichnung");
    eigenschaftgruppeList.addColumn("Pflicht", "pflicht",
        new JaNeinFormatter());
    eigenschaftgruppeList.addColumn("Max. 1 Eigenschaft", "max1",
        new JaNeinFormatter());
    eigenschaftgruppeList
        .setContextMenu(new EigenschaftGruppeMenu(eigenschaftgruppeList));
    eigenschaftgruppeList.setMulti(true);
    eigenschaftgruppeList.setAction(new EditAction(
        EigenschaftGruppeDetailView.class, eigenschaftgruppeList));
    VorZurueckControl.setObjektListe(null, null);
    return eigenschaftgruppeList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTENGRUPPEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTENGRUPPEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTENGRUPPEN_DATEINAME);
  }

}
