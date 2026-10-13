/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
import de.jost_net.JVerein.Queries.SollbuchungQuery;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

public class SollbuchungListeControl extends FilterControl
{

  private BetragSummaryTablePart sollbuchungenList;

  private boolean umwandeln;

  public SollbuchungListeControl(AbstractView view)
  {
    super(view, SollbuchungControl.class);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    // dieser Aufruf wird nur für den Spalten-PanelButton gebraucht, da muss die
    // Tabelle schon existieren
    return sollbuchungenList;
  }

  public JVereinTablePart getTablePart(Action action, boolean umwandeln,
      boolean multi) throws RemoteException
  {
    if (sollbuchungenList != null)
    {
      return sollbuchungenList;
    }
    this.umwandeln = umwandeln;
    @SuppressWarnings("rawtypes")
    GenericIterator sollbuchungen = new SollbuchungQuery(getFilter(), umwandeln,
        null).get();

    sollbuchungenList = new BetragSummaryTablePart(sollbuchungen, null);
    sollbuchungenList.addColumn("Nr", "id-int");
    sollbuchungenList.addColumn(getAbgeschlossenColumn(), false);
    sollbuchungenList.addColumn("Datum", Sollbuchung.DATUM,
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    sollbuchungenList.addColumn("Abrechnungslauf", Sollbuchung.ABRECHNUNGSLAUF);
    sollbuchungenList.addColumn("Mitglied", Sollbuchung.MITGLIED);
    sollbuchungenList.addColumn("Zahler", Sollbuchung.ZAHLER);
    sollbuchungenList.addColumn("Zweck", Sollbuchung.ZWECK1);
    sollbuchungenList.addColumn("Betrag", Sollbuchung.BETRAG,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    sollbuchungenList.addColumn("Zahlungsweg", Sollbuchung.ZAHLUNGSWEG,
        new Formatter()
        {
          @Override
          public String format(Object o)
          {
            return new Zahlungsweg((Integer) o).getText();
          }
        });
    sollbuchungenList.addColumn("Zahlungseingang", Sollbuchung.ISTSUMME,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      sollbuchungenList.addColumn("Rechnung", "rechnung.nummer");
    }
    sollbuchungenList.setMulti(multi);
    if (action == null)
    {
      sollbuchungenList.setContextMenu(new SollbuchungMenu(sollbuchungenList));
      sollbuchungenList.setAction(
          new EditAction(SollbuchungDetailView.class, sollbuchungenList));
      VorZurueckControl.setObjektListe(null, null);
    }
    else
    {
      sollbuchungenList.setAction(action);
    }

    return sollbuchungenList;
  }

  @Override
  protected void TabRefresh()
  {
    if (sollbuchungenList != null)
    {
      try
      {
        @SuppressWarnings("rawtypes")
        GenericIterator sollbuchungen = new SollbuchungQuery(getFilter(),
            umwandeln, null).get();
        sollbuchungenList.removeAll();
        if (sollbuchungen != null)
        {
          while (sollbuchungen.hasNext())
          {
            sollbuchungenList.addItem(sollbuchungen.next());
          }
        }
        sollbuchungenList.sort();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Refresh der Tabelle", e);
      }
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.SOLLBUCHUNGEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.SOLLBUCHUNGEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.SOLLBUCHUNGEN_DATEINAME, this);
  }

}
