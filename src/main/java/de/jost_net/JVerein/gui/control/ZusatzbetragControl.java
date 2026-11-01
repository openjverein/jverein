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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.ZusatzbetragDetailView;
import de.jost_net.JVerein.keys.Ausfuehrung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;

public class ZusatzbetragControl extends FilterControl
{

  private BetragSummaryTablePart zusatzbetraegeList;

  public ZusatzbetragControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (zusatzbetraegeList != null)
    {
      return zusatzbetraegeList;
    }

    DBIterator<Zusatzbetrag> zusatzbetraege = getIterator();

    zusatzbetraegeList = new BetragSummaryTablePart(zusatzbetraege, null);
    zusatzbetraegeList.addColumn("Nr", "id-int");
    zusatzbetraegeList.addColumn("Name", "mitglied");
    zusatzbetraegeList.addColumn("Erste Fälligkeit", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Nächste Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Letzte abgerechnete Fälligkeit",
        "ausfuehrung", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Intervall", "intervalltext");
    zusatzbetraegeList.addColumn("Nicht mehr ausführen ab", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Buchungstext", "buchungstext");
    zusatzbetraegeList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
    zusatzbetraegeList.addColumn("Zahlungsweg", "zahlungsweg", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return new Zahlungsweg((Integer) o).getText();
      }
    });
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    zusatzbetraegeList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Steuer", "steuer", o -> {
        if (o == null)
        {
          return "";
        }
        try
        {
          return ((Steuer) o).getName();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return "";
      }, false, Column.ALIGN_RIGHT);
    }
    zusatzbetraegeList.addColumn("Zahlt selbst", "mitgliedzahltselbst",
        new JaNeinFormatter(), false, Column.ALIGN_LEFT);
    zusatzbetraegeList
        .setContextMenu(new ZusatzbetraegeMenu(zusatzbetraegeList));
    zusatzbetraegeList.setMulti(true);
    zusatzbetraegeList.setAction(
        new EditAction(ZusatzbetragDetailView.class, zusatzbetraegeList));
    VorZurueckControl.setObjektListe(null, null);

    if (getFilterValue(Filter.AUSFUEHRUNG) == Ausfuehrung.AKTIVE)
    {
      nichtAktiveEliminieren(zusatzbetraegeList);
    }
    return zusatzbetraegeList;
  }

  public void TabRefresh()
  {
    try
    {
      if (zusatzbetraegeList != null)
      {
        DBIterator<Zusatzbetrag> zusatzbetraege = getIterator();
        zusatzbetraegeList.removeAll();
        while (zusatzbetraege.hasNext())
        {
          zusatzbetraegeList.addItem(zusatzbetraege.next());
        }
        if (getFilterValue(Filter.AUSFUEHRUNG) == Ausfuehrung.AKTIVE)
        {
          nichtAktiveEliminieren(zusatzbetraegeList);
        }
        zusatzbetraegeList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e);
    }
  }

  private DBIterator<Zusatzbetrag> getIterator() throws RemoteException
  {
    Ausfuehrung ausfuehrung = (Ausfuehrung) getFilterValue(Filter.AUSFUEHRUNG);
    DBIterator<Zusatzbetrag> zusatzbetraege = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    zusatzbetraege.join("mitglied");
    zusatzbetraege.addFilter(Zusatzbetrag.T_MITGLIED + " = mitglied.id");
    if (ausfuehrung == null || ausfuehrung == Ausfuehrung.AKTIVE)
    {
      if (getFilterValue(Filter.AUSFUEHRUNG_VON) != null)
      {
        zusatzbetraege.addFilter("ausfuehrung >= ?",
            (Date) getFilterValue(Filter.AUSFUEHRUNG_VON));
      }
      if (getFilterValue(Filter.AUSFUEHRUNG_BIS) != null)
      {
        zusatzbetraege.addFilter("ausfuehrung <= ?",
            (Date) getFilterValue(Filter.AUSFUEHRUNG_BIS));
      }
    }
    else if (ausfuehrung == Ausfuehrung.NOCH_NICHT)
    {
      zusatzbetraege.addFilter("ausfuehrung is null");
    }
    zusatzbetraege
        .setOrder("ORDER BY ausfuehrung DESC, faelligkeit DESC, name");
    return zusatzbetraege;
  }

  private void nichtAktiveEliminieren(JVereinTablePart table)
      throws RemoteException
  {
    List<?> li = table.getItems();
    Iterator<?> it = li.iterator();
    while (it.hasNext())
    {
      Zusatzbetrag z = (Zusatzbetrag) it.next();
      if (!z.isOffen(new Date()))
      {
        table.removeItem(z);
      }
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_DATEINAME, this);
  }
}
