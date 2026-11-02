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
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MitgliedsArt;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LastschriftControl extends FilterControl
{

  private BetragSummaryTablePart lastschriftList;

  public LastschriftControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (lastschriftList != null)
    {
      return lastschriftList;
    }
    lastschriftList = new BetragSummaryTablePart(getLastschriften(), null);
    lastschriftList.addColumn("Nr", "id-int");
    lastschriftList.addColumn(getAbgeschlossenColumn(), false);
    lastschriftList.addColumn("Versanddatum", "versanddatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("Abrechnungslauf", "abrechnungslauf");
    lastschriftList.addColumn("Name", "name");
    lastschriftList.addColumn("Vorname", "vorname");
    lastschriftList.addColumn("Email", "email");
    lastschriftList.addColumn("Zweck", "verwendungszweck");
    lastschriftList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
    lastschriftList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("IBAN", "iban", new IBANFormatter());
    lastschriftList.addColumn("Mandat", "mandatid");
    lastschriftList.addColumn("Mandatdatum", "mandatdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.setContextMenu(new LastschriftMenu(lastschriftList));
    lastschriftList.setMulti(true);
    lastschriftList.setAction(
        new EditAction(LastschriftDetailView.class, lastschriftList));
    VorZurueckControl.setObjektListe(null, null);
    return lastschriftList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (lastschriftList == null)
    {
      return;
    }
    try
    {
      lastschriftList.removeAll();
      DBIterator<Lastschrift> lastschriften = getLastschriften();
      while (lastschriften.hasNext())
      {
        lastschriftList.addItem(lastschriften.next());
      }
      lastschriftList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Lastschrift> getLastschriften()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Lastschrift> lastschriften = service
        .createList(Lastschrift.class);
    lastschriften.join("abrechnungslauf");
    lastschriften.addFilter("abrechnungslauf.id = lastschrift.abrechnungslauf");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case MITGLIEDART:
          lastschriften.join("mitglied");
          lastschriften.addFilter("mitglied.id = lastschrift.mitglied");
          if (((MitgliedsArt) value).equals(MitgliedsArt.MITGLIED))
          {
            lastschriften.addFilter(
                Mitglied.MITGLIEDSTYP + " = " + Mitgliedstyp.MITGLIED);
          }
          else if (((MitgliedsArt) value).equals(MitgliedsArt.NICHT_MITGLIED))
          {
            lastschriften.addFilter(
                Mitglied.MITGLIEDSTYP + " > " + Mitgliedstyp.MITGLIED);
          }
          break;
        case NAME:
          lastschriften.addFilter("(lower(lastschrift.name) like ?)",
              value.toString() + "%");
          break;
        case ZWECK:
          lastschriften.addFilter("(lower(verwendungszweck) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_FAELLIGKEI_VON:
          lastschriften.addFilter("faelligkeit >= ?", value);
          break;
        case DATUM_FAELLIGKEI_BIS:
          lastschriften.addFilter("faelligkeit <= ?", value);
          break;
        case ABRECHNUNGSLAUF_AB:
          lastschriften.addFilter("abrechnungslauf >= ?", value);
          break;
        case VERSAND:
          switch ((SuchVersand) value)
          {
            case VERSAND:
              lastschriften.addFilter("versanddatum IS NOT NULL");
              break;
            case NICHT_VERSAND:
              lastschriften.addFilter("versanddatum IS NULL");
              break;
          }
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }

    lastschriften.setOrder("ORDER BY name");

    return lastschriften;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_DATEINAME, this);
  }
}
