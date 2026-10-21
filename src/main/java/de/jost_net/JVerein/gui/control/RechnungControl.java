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
import de.jost_net.JVerein.Queries.RechnungQuery;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.menu.RechnungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungControl extends FilterControl
{

  private BetragSummaryTablePart rechnungList;

  public RechnungControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public BetragSummaryTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (rechnungList != null)
    {
      return rechnungList;
    }
    GenericIterator<Rechnung> rechnungen = new RechnungQuery(getFilter()).get();
    rechnungList = new BetragSummaryTablePart(rechnungen, null);
    rechnungList.addColumn(getGesperrtColumn(), false);
    rechnungList.addColumn("Nr", "nummer");
    rechnungList.addColumn("Versanddatum", "versanddatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    rechnungList.addColumn("Rechnungsdatum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    rechnungList.addColumn("Mitglied", "mitglied");
    rechnungList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Ist", "ist",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Differenz", "differenz",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    rechnungList.addColumn("Zahlungsweg", "zahlungsweg",
        new ZahlungswegFormatter());
    rechnungList.addColumn("Rechnungstext", "rechnungstext");
    rechnungList.addColumn("Referenz Rechnung", "refrechnung");
    rechnungList.addColumn("Erstattungsbetrag", "erstattungsbetrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));

    rechnungList.setContextMenu(new RechnungMenu(rechnungList));
    rechnungList.setMulti(true);
    rechnungList
        .setAction(new EditAction(RechnungDetailView.class, rechnungList));
    VorZurueckControl.setObjektListe(null, null);
    return rechnungList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (rechnungList != null)
      {
        rechnungList.removeAll();
        GenericIterator<Rechnung> rechnungen = new RechnungQuery(getFilter())
            .get();
        while (rechnungen.hasNext())
        {
          rechnungList.addItem(rechnungen.next());
        }
        rechnungList.sort();
      }
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.RECHNUNGEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.RECHNUNGEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.RECHNUNGEN_DATEINAME, this);
  }
}
