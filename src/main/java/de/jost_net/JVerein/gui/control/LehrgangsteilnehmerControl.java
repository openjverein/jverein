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
import de.jost_net.JVerein.gui.menu.LehrgangsteilnehmerMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LehrgangsteilnehmerDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangsteilnehmerControl extends FilterControl
{

  private JVereinTablePart teilnehmerList;

  public LehrgangsteilnehmerControl(AbstractView view)
  {
    super(view);
  }

  private DBIterator<Lehrgangsteilnehmer> getIterator()
      throws RemoteException, ApplicationException
  {
    DBIterator<Lehrgangsteilnehmer> teilnehmer = Einstellungen.getDBService()
        .createList(Lehrgangsteilnehmer.class);
    teilnehmer.join("mitglied");
    teilnehmer.addFilter("mitglied.id = lehrgangsteilnehmer.mitglied");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NAME:
          String suchName = "%" + value.toString().toLowerCase() + "%";
          teilnehmer.addFilter(
              "(lower(name) like ? " + "or lower(vorname) like ?)", suchName,
              suchName);
          break;
        case VERANSTALTER:
          teilnehmer.addFilter("lower(veranstalter) like ?",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case BEZEICHNUNG:
          teilnehmer.addFilter("lower(bezeichnung) like ?",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case LEHRGANGSART:
          teilnehmer.addFilter("lehrgangsart = ?",
              ((Lehrgangsart) value).getID());
          break;
        case LEHRGANG:
          teilnehmer.addFilter("lehrgang = ?", ((Lehrgang) value).getID());
          break;
        case DATUM_VON:
          // Auch Lehrgänge die nach Von Datum enden, also auch teilweise in das
          // Von/Bis Fenster fallen
          teilnehmer.addFilter("(von >= ? OR bis >= ?)", value, value);
          break;
        case DATUM_BIS:
          // Auch Lehrgänge die nach Bis Datum enden, aber noch im Von/Bis
          // Fenster
          // beginnen
          teilnehmer.addFilter("von <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    return teilnehmer;
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (teilnehmerList != null)
    {
      return teilnehmerList;
    }
    DBIterator<Lehrgangsteilnehmer> lehrgaenge = getIterator();
    teilnehmerList = new JVereinTablePart(lehrgaenge, null);
    teilnehmerList.addColumn("Nr", "id-int");
    teilnehmerList.addColumn("Name", "mitglied");
    teilnehmerList.addColumn("Lehrgang", "lehrgang");
    teilnehmerList.addColumn("Lehrgangsart", "lehrgangsart");
    teilnehmerList.addColumn("Bezeichnung", "bezeichnung");
    teilnehmerList.addColumn("Von/am", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    teilnehmerList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    teilnehmerList.addColumn("Veranstalter", "veranstalter");
    teilnehmerList.addColumn("Ergebnis", "ergebnis");
    teilnehmerList.setContextMenu(new LehrgangsteilnehmerMenu(teilnehmerList));
    teilnehmerList.setMulti(true);
    teilnehmerList.setAction(
        new EditAction(LehrgangsteilnehmerDetailView.class, teilnehmerList));
    VorZurueckControl.setObjektListe(null, null);

    return teilnehmerList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (teilnehmerList == null)
      {
        return;
      }
      teilnehmerList.removeAll();
      DBIterator<Lehrgangsteilnehmer> lehrgaenge = getIterator();
      while (lehrgaenge.hasNext())
      {
        teilnehmerList.addItem(lehrgaenge.next());
      }
      teilnehmerList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSTEILNEHMER_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSTEILNEHMER_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANGSTEILNEHMER_DATEINAME, this);
  }
}
