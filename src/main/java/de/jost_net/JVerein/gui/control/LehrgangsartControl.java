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
import de.jost_net.JVerein.gui.menu.LehrgangsartMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LehrgangsartDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.util.ApplicationException;

public class LehrgangsartControl extends FilterControl
{
  private JVereinTablePart lehrgangsartList;

  public LehrgangsartControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (lehrgangsartList != null)
    {
      return lehrgangsartList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Lehrgangsart> lehrgangsarten = service
        .createList(Lehrgangsart.class);
    lehrgangsarten.setOrder("ORDER BY bezeichnung");

    lehrgangsartList = new JVereinTablePart(lehrgangsarten, null);
    lehrgangsartList.addColumn(getGesperrtColumn(), false);
    lehrgangsartList.addColumn("Bezeichnung", "bezeichnung");
    lehrgangsartList.addColumn("Von/am", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgangsartList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgangsartList.addColumn("Veranstalter", "veranstalter");
    lehrgangsartList.setContextMenu(new LehrgangsartMenu(lehrgangsartList));
    lehrgangsartList.setMulti(true);
    lehrgangsartList.setAction(
        new EditAction(LehrgangsartDetailView.class, lehrgangsartList));
    VorZurueckControl.setObjektListe(null, null);
    return lehrgangsartList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSARTEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSARTEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSARTEN_DATEINAME);
  }
}
