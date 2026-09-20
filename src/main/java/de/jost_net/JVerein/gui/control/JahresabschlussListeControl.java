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
import de.jost_net.JVerein.gui.menu.JahresabschlussMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.JahresabschlussDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;

public class JahresabschlussListeControl extends AbstractJVereinControl
{

  private JVereinTablePart jahresabschlussList;

  public JahresabschlussListeControl(AbstractView view) throws RemoteException
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (jahresabschlussList != null)
    {
      return jahresabschlussList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Jahresabschluss> jahresabschluesse = service
        .createList(Jahresabschluss.class);
    jahresabschluesse.setOrder("ORDER BY von desc");

    jahresabschlussList = new JVereinTablePart(jahresabschluesse, null);
    jahresabschlussList.addColumn("Nr", "id-int");
    jahresabschlussList.addColumn("Von", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Name", "name");
    jahresabschlussList
        .setContextMenu(new JahresabschlussMenu(jahresabschlussList));
    jahresabschlussList.setAction(
        new EditAction(JahresabschlussDetailView.class, jahresabschlussList));
    VorZurueckControl.setObjektListe(null, null);
    return jahresabschlussList;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.JAHRESABSCHLUESSE_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.JAHRESABSCHLUESSE_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.JAHRESABSCHLUESSE_DATEINAME);
  }
}
