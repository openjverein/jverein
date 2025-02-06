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

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.menu.ZusatzbetragVorlageMenu;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;

/**
 * Ein Dialog, zur Auswahl und Bearbeitung von Zusatzbetrag-Vorlagen
 */
public class ZusatzbetragVorlageDialog
    extends AbstractDialog<ZusatzbetragVorlage>
{

  private ZusatzbetragVorlage selected = null;

  private TablePart tab;

  public ZusatzbetragVorlageDialog()
  {
    super(AbstractDialog.POSITION_CENTER);

    setTitle("Zusatzbetrag-Vorlagen");
    setSize(850, 350);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    this.getZusatzbetragVorlagen().paint(parent);
    ButtonArea b = new ButtonArea();
    b.addButton("Übernehmen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        selected = (ZusatzbetragVorlage) tab.getSelection();
        close();
      }
    }, null, true, "ok.png");
    b.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        selected = null;
        close();
      }
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected ZusatzbetragVorlage getData() throws Exception
  {
    return this.selected;
  }

  private TablePart getZusatzbetragVorlagen() throws RemoteException
  {
    if (this.tab != null)
    {
      return this.tab;
    }
    DBIterator<ZusatzbetragVorlage> dbi = Einstellungen.getDBService()
        .createList(ZusatzbetragVorlage.class);
    dbi.setOrder("order by buchungstext");
    this.tab = new TablePart(dbi, new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        selected = (ZusatzbetragVorlage) context;
        close();
      }
    });
    tab.addColumn("Erste Fälligkeit", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    tab.addColumn("Nächste Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    tab.addColumn("Intervall", "intervalltext");
    tab.addColumn("Nicht mehr ausführen ab", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    tab.addColumn("Buchungstext", "buchungstext");
    tab.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      tab.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    tab.addColumn("Buchungsart", "buchungsart", new BuchungsartFormatter());
    tab.addColumn("Zahlungsweg", "zahlungsweg", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        return new Zahlungsweg((Integer) o).getText();
      }
    });
    tab.setContextMenu(new ZusatzbetragVorlageMenu());
    tab.setRememberColWidths(true);
    tab.setRememberOrder(true);
    tab.addFeature(new FeatureSummary());

    return this.tab;
  }
}
