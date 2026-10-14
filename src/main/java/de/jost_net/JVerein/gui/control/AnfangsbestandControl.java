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
import de.jost_net.JVerein.gui.menu.AnfangsbestandMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.AnfangsbestandDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnfangsbestandControl extends FilterControl
{

  private JVereinTablePart anfangsbestandList;

  public AnfangsbestandControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (anfangsbestandList != null)
    {
      return anfangsbestandList;
    }
    anfangsbestandList = new JVereinTablePart(getAnfangsstaende(), null);
    anfangsbestandList.addColumn("Nr", "id-int");
    anfangsbestandList.addColumn(getAbgeschlossenColumn(), false);
    anfangsbestandList.addColumn("Nummer", "nummer");
    anfangsbestandList.addColumn("Bezeichnung", "bezeichnung");
    anfangsbestandList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    anfangsbestandList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    anfangsbestandList
        .setContextMenu(new AnfangsbestandMenu(anfangsbestandList));
    anfangsbestandList.setMulti(true);
    anfangsbestandList.setAction(
        new EditAction(AnfangsbestandDetailView.class, anfangsbestandList));
    VorZurueckControl.setObjektListe(null, null);
    return anfangsbestandList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (anfangsbestandList == null)
    {
      return;
    }
    anfangsbestandList.removeAll();
    try
    {
      DBIterator<Anfangsbestand> anfangsbestaende = getAnfangsstaende();
      while (anfangsbestaende.hasNext())
      {
        anfangsbestandList.addItem(anfangsbestaende.next());
      }
      anfangsbestandList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Anfangsbestand> getAnfangsstaende()
      throws RemoteException, ApplicationException
  {
    DBIterator<Anfangsbestand> anfangsbestaende = Einstellungen.getDBService()
        .createList(Anfangsbestand.class);
    anfangsbestaende.join("konto");
    anfangsbestaende.addFilter("konto.id = anfangsbestand.konto");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case BEZEICHNUNG:
          anfangsbestaende.addFilter("(lower(bezeichnung) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case NUMMER:
          anfangsbestaende.addFilter("(lower(nummer) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_VON:
          anfangsbestaende.addFilter("datum >= ?", value);
          break;
        case DATUM_BIS:
          anfangsbestaende.addFilter("datum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    anfangsbestaende.setOrder("ORDER BY konto, datum desc");
    return anfangsbestaende;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ANFANGSBESTAENDE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ANFANGSBESTAENDE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ANFANGSBESTAENDE_DATEINAME, this);
  }
}
