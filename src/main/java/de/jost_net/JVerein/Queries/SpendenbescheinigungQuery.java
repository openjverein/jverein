package de.jost_net.JVerein.Queries;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.SuchSpendenart;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungQuery
{

  private Map<Filter, Object> filter = null;

  private boolean and = false;

  private String sql = "";

  public SpendenbescheinigungQuery(Map<Filter, Object> filter)
  {
    this.filter = filter;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Spendenbescheinigung> get()
      throws RemoteException, ApplicationException
  {
    SuchSpendenart suchSpendenart = (SuchSpendenart) filter
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
    list.addFilter("id in ("
        + ids.stream().map(String::valueOf).collect(Collectors.joining(","))
        + ")");
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
    if (filter.get(Filter.MAIL) != null)
    {
      sql += "left join mitglied on (spendenbescheinigung.mitglied = mitglied.id) ";
    }
    Object spart = filter.get(Filter.SPENDENART);
    if (spart != null && spart != SuchSpendenart.GELDSPENDE
        && spart != SuchSpendenart.SACHSPENDE)
    {
      sql += "left join buchung on (spendenbescheinigung.id = buchung.spendenbescheinigung) ";
    }

    for (Entry<Filter, Object> entry : filter.entrySet())
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
}
