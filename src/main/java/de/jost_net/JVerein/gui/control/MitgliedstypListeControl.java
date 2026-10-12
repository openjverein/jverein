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
import de.jost_net.JVerein.gui.menu.MitgliedstypMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.MitgliedstypDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class MitgliedstypListeControl extends FilterControl
{
  private JVereinTablePart mitgliedstypList;

  public MitgliedstypListeControl(AbstractView view)
  {
    super(view, MitgliedstypControl.class);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (mitgliedstypList != null)
    {
      return mitgliedstypList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Mitgliedstyp> mtIt = service.createList(Mitgliedstyp.class);
    mtIt.setOrder("ORDER BY " + Mitgliedstyp.BEZEICHNUNG);

    mitgliedstypList = new JVereinTablePart(mtIt, null);
    mitgliedstypList.addColumn(getGesperrtColumn(), false);
    mitgliedstypList.addColumn("Bezeichnung", Mitgliedstyp.BEZEICHNUNG);
    mitgliedstypList.addColumn("Bezeichnung Plural",
        Mitgliedstyp.BEZEICHNUNG_PLURAL);
    mitgliedstypList.addColumn("ID", "id");
    mitgliedstypList.setContextMenu(new MitgliedstypMenu(mitgliedstypList));
    mitgliedstypList.setMulti(true);
    mitgliedstypList.setAction(
        new EditAction(MitgliedstypDetailView.class, mitgliedstypList));
    VorZurueckControl.setObjektListe(null, null);
    return mitgliedstypList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.MITGLIEDSTYPEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.MITGLIEDSTYPEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.MITGLIEDSTYPEN_DATEINAME);
  }
}
