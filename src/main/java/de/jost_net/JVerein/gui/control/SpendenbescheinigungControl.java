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
import java.util.ArrayList;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.SpendenbescheinigungQuery;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.SpendenbescheinigungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungControl extends FilterControl
{

  private BetragSummaryTablePart spbList;

  public SpendenbescheinigungControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (spbList != null)
    {
      return spbList;
    }
    spbList = new BetragSummaryTablePart(
        new SpendenbescheinigungQuery(getFilter()).get(), null);
    spbList.addColumn("Nr", "id-int");
    spbList.addColumn(getGesperrtColumn(), false);
    spbList.addColumn("Versanddatum", "versanddatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    spbList.addColumn("Spender", "mitglied");
    spbList.addColumn("Spendenart", "spendenart",
        o -> new Spendenart((Integer) o).getText(), false, Column.ALIGN_LEFT);
    spbList.addColumn("Bescheinigungsdatum", "bescheinigungsdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    spbList.addColumn("Spendedatum", "spendedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    spbList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    spbList.addColumn("Zeile 1", "zeile1");
    spbList.addColumn("Zeile 2", "zeile2");
    spbList.addColumn("Zeile 3", "zeile3");
    spbList.addColumn("Zeile 4", "zeile4");
    spbList.addColumn("Zeile 5", "zeile5");
    spbList.addColumn("Zeile 6", "zeile6");
    spbList.addColumn("Zeile 7", "zeile7");

    spbList.setContextMenu(new SpendenbescheinigungMenu(spbList));
    spbList.setMulti(true);
    spbList.setAction(
        new EditAction(SpendenbescheinigungDetailView.class, spbList));
    VorZurueckControl.setObjektListe(null, null);
    return spbList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (spbList != null)
    {
      try
      {
        ArrayList<Spendenbescheinigung> spbs = new SpendenbescheinigungQuery(
            getFilter()).get();
        spbList.removeAll();
        for (Spendenbescheinigung spb : spbs)
        {
          spbList.addItem(spb);
        }
        spbList.sort();
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler beim Refresh der Tabelle", e1);
      }
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.SPENDENBESCHEINIGUNGEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.SPENDENBESCHEINIGUNGEN_SUBTITEL,
        this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.SPENDENBESCHEINIGUNGEN_DATEINAME,
        this);
  }
}
