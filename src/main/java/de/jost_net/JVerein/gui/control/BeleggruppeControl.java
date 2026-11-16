/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.BeleggruppeMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.BeleggruppeDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Beleggruppe;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class BeleggruppeControl extends FilterControl
{
  private JVereinTablePart beleggruppenList;

  public BeleggruppeControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (beleggruppenList != null)
    {
      return beleggruppenList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Beleggruppe> beleggruppen = service
        .createList(Beleggruppe.class);
    beleggruppen.setOrder("ORDER BY bezeichnung");

    beleggruppenList = new JVereinTablePart(beleggruppen, null);
    beleggruppenList.addColumn(getIdColumn(), false);
    beleggruppenList.addColumn(getGesperrtColumn(), false);
    beleggruppenList.addColumn("Bezeichnung", "bezeichnung");
    beleggruppenList.setContextMenu(new BeleggruppeMenu(beleggruppenList));
    beleggruppenList.setMulti(true);
    beleggruppenList.setAction(
        new EditAction(BeleggruppeDetailView.class, beleggruppenList));
    VorZurueckControl.setObjektListe(null, null);
    return beleggruppenList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BELEGGRUPPEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BELEGGRUPPEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BELEGGRUPPEN_DATEINAME);
  }

}
