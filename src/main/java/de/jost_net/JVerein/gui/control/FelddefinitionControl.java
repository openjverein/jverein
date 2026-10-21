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
import de.jost_net.JVerein.gui.formatter.DatentypFormatter;
import de.jost_net.JVerein.gui.menu.FelddefinitionMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.ZusatzfeldDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.util.ApplicationException;

public class FelddefinitionControl extends FilterControl
{

  private JVereinTablePart felddefinitionList;

  public FelddefinitionControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (felddefinitionList != null)
    {
      return felddefinitionList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Felddefinition> fdef = service.createList(Felddefinition.class);
    felddefinitionList = new JVereinTablePart(fdef, null);
    felddefinitionList.addColumn(getGesperrtColumn(), false);
    felddefinitionList.addColumn("Name", "name");
    felddefinitionList.addColumn("Label", "label");
    felddefinitionList.addColumn("Datentyp", "datentyp",
        new DatentypFormatter(), false, Column.ALIGN_LEFT);
    felddefinitionList.addColumn("Länge", "laenge");
    felddefinitionList
        .setContextMenu(new FelddefinitionMenu(felddefinitionList));
    felddefinitionList.setAction(
        new EditAction(ZusatzfeldDetailView.class, felddefinitionList));
    felddefinitionList.setMulti(true);
    VorZurueckControl.setObjektListe(null, null);
    return felddefinitionList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZFELDER_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZFELDER_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZFELDER_DATEINAME);
  }

}
