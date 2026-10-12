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
import de.jost_net.JVerein.gui.menu.LehrgangMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LehrgangDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangListeControl extends FilterControl
{

  private JVereinTablePart lehrgaengeList;

  public LehrgangListeControl(AbstractView view)
  {
    super(view, LehrgangControl.class);
  }

  private DBIterator<Lehrgang> getIterator()
      throws RemoteException, ApplicationException
  {
    DBIterator<Lehrgang> lehrgaenge = Einstellungen.getDBService()
        .createList(Lehrgang.class);
    lehrgaenge.join("mitglied");
    lehrgaenge.addFilter("mitglied.id = lehrgang.mitglied");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NAME:
          String suchName = "%" + value.toString().toLowerCase() + "%";
          lehrgaenge.addFilter(
              "(lower(name) like ? " + "or lower(vorname) like ?)", suchName,
              suchName);
          break;
        case VERANSTALTER:
          lehrgaenge.addFilter("lower(veranstalter) like ? ",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case BEZEICHNUNG:
          lehrgaenge.addFilter("lower(bezeichnung) like ? ",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case LEHRGANGSART:
          lehrgaenge.addFilter("lehrgangsart = ?",
              ((Lehrgangsart) value).getID());
          break;
        case DATUM_VON:
          lehrgaenge.addFilter("von >= ?", value);
          break;
        case DATUM_BIS:
          lehrgaenge.addFilter("bis <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    return lehrgaenge;
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (lehrgaengeList != null)
    {
      return lehrgaengeList;
    }
    DBIterator<Lehrgang> lehrgaenge = getIterator();
    lehrgaengeList = new JVereinTablePart(lehrgaenge, null);
    lehrgaengeList.addColumn("Nr", "id-int");
    lehrgaengeList.addColumn("Name", "mitglied");
    lehrgaengeList.addColumn("Lehrgangsart", "lehrgangsart");
    lehrgaengeList.addColumn("Bezeichnung", "bezeichnung");
    lehrgaengeList.addColumn("Von/am", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgaengeList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgaengeList.addColumn("Veranstalter", "veranstalter");
    lehrgaengeList.addColumn("Ergebnis", "ergebnis");
    lehrgaengeList.setContextMenu(new LehrgangMenu(lehrgaengeList));
    lehrgaengeList.setMulti(true);
    lehrgaengeList
        .setAction(new EditAction(LehrgangDetailView.class, lehrgaengeList));
    VorZurueckControl.setObjektListe(null, null);

    return lehrgaengeList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (lehrgaengeList == null)
      {
        return;
      }
      lehrgaengeList.removeAll();
      DBIterator<Lehrgang> lehrgaenge = getIterator();
      while (lehrgaenge.hasNext())
      {
        lehrgaengeList.addItem(lehrgaenge.next());
      }
      lehrgaengeList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGAENGE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGAENGE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGAENGE_DATEINAME, this);
  }
}
