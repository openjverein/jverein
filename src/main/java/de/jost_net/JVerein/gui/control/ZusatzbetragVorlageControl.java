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
import de.jost_net.JVerein.gui.menu.ZusatzbetragVorlageMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.view.ZusatzbetragVorlageDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragVorlageControl extends FilterControl
{

  private BetragSummaryTablePart zusatzbetragVorlageList;

  public ZusatzbetragVorlageControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public BetragSummaryTablePart getTablePart() throws RemoteException
  {
    if (zusatzbetragVorlageList != null)
    {
      return zusatzbetragVorlageList;
    }
    DBIterator<ZusatzbetragVorlage> zusatzbetragsvorlagen = Einstellungen
        .getDBService().createList(ZusatzbetragVorlage.class);
    zusatzbetragsvorlagen.setOrder("ORDER BY buchungstext");

    zusatzbetragVorlageList = new BetragSummaryTablePart(zusatzbetragsvorlagen,
        null);
    zusatzbetragVorlageList.addColumn("Nr", "id-int");
    zusatzbetragVorlageList.addColumn("Erste Fälligkeit", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetragVorlageList.addColumn("Nächste Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetragVorlageList.addColumn("Intervall", "intervalltext");
    zusatzbetragVorlageList.addColumn("Nicht mehr ausführen ab", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetragVorlageList.addColumn("Buchungstext", "buchungstext");
    zusatzbetragVorlageList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    zusatzbetragVorlageList.addColumn("Zahlungsweg", "zahlungsweg",
        o -> new Zahlungsweg((Integer) o).getText());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      zusatzbetragVorlageList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    zusatzbetragVorlageList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      zusatzbetragVorlageList.addColumn("Steuer", "steuer", o -> {
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
    zusatzbetragVorlageList.addColumn("Zahlt selbst", "mitgliedzahltselbst",
        new JaNeinFormatter(), false, Column.ALIGN_LEFT);
    zusatzbetragVorlageList
        .setContextMenu(new ZusatzbetragVorlageMenu(zusatzbetragVorlageList));
    zusatzbetragVorlageList.setMulti(true);
    zusatzbetragVorlageList.setAction(new EditAction(
        ZusatzbetragVorlageDetailView.class, zusatzbetragVorlageList));
    VorZurueckControl.setObjektListe(null, null);

    return zusatzbetragVorlageList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_VORLAGEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_VORLAGEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_VORLAGEN_DATEINAME);
  }
}
