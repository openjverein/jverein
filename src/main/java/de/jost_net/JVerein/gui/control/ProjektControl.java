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
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.ProjektMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.ProjektDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ProjektControl extends FilterControl
{

  private JVereinTablePart projektList;

  public ProjektControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (projektList != null)
    {
      return projektList;
    }
    projektList = new JVereinTablePart(getProjekte(), null);
    projektList.addColumn(getGesperrtColumn(), false);
    projektList.addColumn("Bezeichnung", "bezeichnung");
    projektList.addColumn("Startdatum", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    projektList.addColumn("Endedatum", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    projektList.setContextMenu(new ProjektMenu(projektList));
    projektList.setMulti(true);
    projektList.setAction(new EditAction(ProjektDetailView.class, projektList));
    VorZurueckControl.setObjektListe(null, null);
    return projektList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (projektList == null)
    {
      return;
    }
    projektList.removeAll();
    try
    {
      DBIterator<Projekt> projekte = getProjekte();
      while (projekte.hasNext())
      {
        projektList.addItem(projekte.next());
      }
      projektList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e1);
    }
  }

  private DBIterator<Projekt> getProjekte()
      throws RemoteException, ApplicationException
  {
    DBIterator<Projekt> projekte = Einstellungen.getDBService()
        .createList(Projekt.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case BEZEICHNUNG:
          projekte.addFilter("(lower(bezeichnung) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_START_VON:
          projekte.addFilter("startdatum >= ?", value);
          break;
        case DATUM_START_BIS:
          projekte.addFilter("startdatum <= ?", value);
          break;
        case DATUM_ENDE_VON:
          projekte.addFilter("endedatum >= ?", value);
          break;
        case DATUM_ENDE_BIS:
          projekte.addFilter("endedatum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    projekte.setOrder("ORDER BY bezeichnung");
    return projekte;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_DATEINAME, this);
  }
}
