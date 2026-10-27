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
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.SteuerMenue;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SteuerDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.util.ApplicationException;

public class SteuerControl extends FilterControl
{

  private JVereinTablePart steuerList;

  public SteuerControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (steuerList != null)
    {
      return steuerList;
    }
    DBIterator<Steuer> steuern = Einstellungen.getDBService()
        .createList(Steuer.class);

    steuerList = new JVereinTablePart(steuern, null);
    steuerList.addColumn(getGesperrtColumn(), false);
    steuerList.addColumn("Name", "name");
    steuerList.addColumn("Steuersatz", "satz", o -> {
      return Einstellungen.BIGDECIMALFORMAT.format(o) + "%";
    }, false, Column.ALIGN_RIGHT);
    steuerList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      steuerList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    steuerList.addColumn("Aktiv", "aktiv", new JaNeinFormatter());
    steuerList.setContextMenu(new SteuerMenue(steuerList));
    steuerList.setMulti(true);
    steuerList.setCheckable(false);
    steuerList.setAction(new EditAction(SteuerDetailView.class, steuerList));
    return steuerList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh

  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.STEUERN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.STEUERN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.STEUERN_DATEINAME);
  }

}
