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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.SpendenbescheinigungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.SuchSpendenart;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungControl extends FilterControl
{

  private BetragSummaryTablePart spbList;

  private boolean and = false;

  private String sql = "";

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
    spbList = new BetragSummaryTablePart(getSpendenbescheinigungen(), null);
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
        spbList.removeAll();
        for (Spendenbescheinigung spb : getSpendenbescheinigungen())
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

  @SuppressWarnings("unchecked")
  private ArrayList<Spendenbescheinigung> getSpendenbescheinigungen()
      throws RemoteException, ApplicationException
  {
    SuchSpendenart suchSpendenart = (SuchSpendenart) getFilter()
        .get(Filter.SPENDENART);
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> queryIds = querySpendenbescheinigungen(suchSpendenart);

    // Bei GELDSPENDE_ECHT liefert das Query auch Splittbuchungen die neben
    // echten Geldbuchungen auch Geldbuchungen mit Verzicht haben.
    // Darum lesen wir nochmal mit ERSTATTUNGSVERZICHT. Wenn eine Id da
    // auch dabei ist dürfen wir die Splittbuchung nicht nehmen
    if (suchSpendenart == SuchSpendenart.GELDSPENDE_ECHT)
    {
      ArrayList<Long> erstattungsIds = querySpendenbescheinigungen(
          SuchSpendenart.ERSTATTUNGSVERZICHT);
      for (Long id : queryIds)
      {
        if (!erstattungsIds.contains(id))
        {
          ids.add(id);
        }
      }
    }
    else
    {
      ids = queryIds;
    }

    if (ids.size() == 0)
      return new ArrayList<Spendenbescheinigung>();

    DBIterator<Spendenbescheinigung> list = Einstellungen.getDBService()
        .createList(Spendenbescheinigung.class);
    list.addFilter("id in (" + StringUtils.join(ids, ",") + ")");
    list.setOrder(" ORDER BY bescheinigungsdatum desc, spendedatum desc ");
    ArrayList<Spendenbescheinigung> spendenbescheinigungen = list != null
        ? (ArrayList<Spendenbescheinigung>) PseudoIterator.asList(list)
        : null;
    return spendenbescheinigungen;
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Long> querySpendenbescheinigungen(
      SuchSpendenart suchSpendenart)
      throws RemoteException, ApplicationException
  {
    final DBService service = Einstellungen.getDBService();
    ArrayList<Object> bedingungen = new ArrayList<>();
    and = false;

    sql = "select DISTINCT spendenbescheinigung.id, bescheinigungsdatum from spendenbescheinigung ";
    if (getFilter().get(Filter.MAIL) != null)
    {
      sql += "left join mitglied on (spendenbescheinigung.mitglied = mitglied.id) ";
    }
    Object spart = getFilter().get(Filter.SPENDENART);
    if (spart != null && spart != SuchSpendenart.GELDSPENDE
        && spart != SuchSpendenart.SACHSPENDE)
    {
      sql += "left join buchung on (spendenbescheinigung.id = buchung.spendenbescheinigung) ";
    }

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case MAIL:
          switch ((MailAuswahl) value)
          {
            case OHNE:
              addCondition("(email is null or length(email) = 0) ");
              break;
            case MIT:
              addCondition("(email is not null and length(email) > 0) ");
          }
          break;
        case SPENDENART:
          switch ((SuchSpendenart) value)
          {
            case GELDSPENDE:
              addCondition("spendenart = ?");
              bedingungen.add(Spendenart.GELDSPENDE);
              break;
            case SACHSPENDE:
              addCondition("spendenart = ?");
              bedingungen.add(Spendenart.SACHSPENDE);
              break;
            case ERSTATTUNGSVERZICHT:
              addCondition("buchung.verzicht = 1");
              break;
            case GELDSPENDE_ECHT:
              addCondition(
                  "(buchung.verzicht != 1 or buchung.verzicht is null) AND spendenart = ?");
              bedingungen.add(Spendenart.GELDSPENDE);
              break;
            case SACHSPENDE_ERSTATTUNGSVERZICHT:
              addCondition("(buchung.verzicht = 1 OR spendenart = ?)");
              bedingungen.add(Spendenart.SACHSPENDE);
              break;
            default:
              break;
          }
          break;
        case ZEILE2:
          addCondition("(lower(zeile2) like ?)");
          bedingungen.add("%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_BESCHEINIGUNG_VON:
          addCondition("bescheinigungsdatum >= ?");
          bedingungen.add(value);
          break;
        case DATUM_BESCHEINIGUNG_BIS:
          addCondition("bescheinigungsdatum <= ?");
          bedingungen.add(value);
          break;
        case DATUM_SPENDE_VON:
          addCondition("spendedatum >= ?");
          bedingungen.add(value);
          break;
        case DATUM_SPENDE_BIS:
          addCondition("spendedatum <= ?");
          bedingungen.add(value);
          break;
        case VERSAND:
          switch ((SuchVersand) value)
          {
            case VERSAND:
              addCondition("versanddatum IS NOT NULL");
              break;
            case NICHT_VERSAND:
              addCondition("versanddatum IS NULL");
              break;
          }
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Long> list = new ArrayList<>();
        while (rs.next())
        {
          list.add(rs.getLong(1));
        }
        return list;
      }
    };

    return (ArrayList<Long>) service.execute(sql, bedingungen.toArray(), rs);
  }

  private void addCondition(String condition)
  {
    if (and)
    {
      sql += " AND ";
    }
    else
    {
      sql += "where ";
    }
    and = true;
    sql += condition;
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
