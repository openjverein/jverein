package de.jost_net.JVerein.Queries;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Differenz;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

public class RechnungQuery
{
  protected Map<Filter, Object> filter = null;

  public RechnungQuery(Map<Filter, Object> filter)
  {
    this.filter = filter;
  }

  @SuppressWarnings("unchecked")
  public GenericIterator<Rechnung> get()
      throws RemoteException, ApplicationException
  {
    DBIterator<Rechnung> rechnungenIt = Einstellungen.getDBService()
        .createList(Rechnung.class);

    boolean joinMitglied = false;
    for (Entry<Filter, Object> entry : filter.entrySet())
    {
      Object value = entry.getValue();

      switch (entry.getKey())
      {
        case VERSAND:
          switch ((SuchVersand) value)
          {
            case VERSAND:
              rechnungenIt.addFilter("rechnung.versanddatum IS NOT NULL");
              break;
            case NICHT_VERSAND:
              rechnungenIt.addFilter("rechnung.versanddatum IS NULL");
              break;
          }
          break;
        case DATUM_VON:
          rechnungenIt.addFilter("rechnung.datum >= ? ", value);
          break;
        case DATUM_BIS:
          rechnungenIt.addFilter("rechnung.datum <= ? ", value);
          break;
        case NAME:
          joinMitglied = true;
          rechnungenIt.addFilter(
              "((lower(mitglied.name) like ?) OR (lower(mitglied.vorname) like ?))",
              "%" + ((String) value).toLowerCase() + "%",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        case MAIL:
          switch ((MailAuswahl) value)
          {
            case OHNE:
              joinMitglied = true;
              rechnungenIt.addFilter(
                  "(mitglied.email is null or length(mitglied.email) = 0)");
              break;
            case MIT:
              joinMitglied = true;
              rechnungenIt.addFilter(
                  "(mitglied.email is  not null and length(mitglied.email) > 0)");
          }
          break;
        case OHNE_ABBUCHER:
          if ((boolean) value)
          {
            rechnungenIt.addFilter("rechnung.zahlungsweg <> ?",
                Zahlungsweg.BASISLASTSCHRIFT);
          }
          break;
        case DIFFERENZ:
          BigDecimal limit = (BigDecimal) filter.get(Filter.DIFFERENZ_LIMIT);
          if (limit == null)
          {
            limit = BigDecimal.ZERO;
          }
          // Es ist egal ob der Betrag positiv oder negativ eingetragen
          // wurde
          limit = limit.abs();

          ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
              Sollbuchung.TABLE_NAME);
          it.addColumn(Sollbuchung.T_RECHNUNG + " as rid");
          it.addColumn("sum(COALESCE(buchung.ist,0) - COALESCE("
              + Sollbuchung.T_BETRAG + ",0) ) as dif");
          it.leftJoin(
              "(SELECT sum(COALESCE((betrag),0)) AS ist,"
                  + Buchung.T_SOLLBUCHUNG + " FROM buchung GROUP BY "
                  + Buchung.T_SOLLBUCHUNG + ") AS buchung",
              Buchung.T_SOLLBUCHUNG + " = " + Sollbuchung.TABLE_NAME_ID);

          if (value == Differenz.FEHLBETRAG)
          {
            it.addHaving("dif < ?", limit.negate());
          }
          else
          {
            it.addHaving("dif > ?", limit);
          }
          it.addGroupBy(Sollbuchung.T_RECHNUNG);
          ArrayList<String> diffIds = new ArrayList<>();
          while (it.hasNext())
          {
            diffIds.add(String.valueOf(it.next().getAttribute("rid")));
          }
          if (diffIds.size() == 0)
          {
            return PseudoIterator.fromArray(new GenericObject[] {});
          }
          rechnungenIt
              .addFilter("rechnung.id in (" + String.join(",", diffIds) + ")");
          break;
        case DIFFERENZ_LIMIT:
          // Wird oben getestet
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    if (joinMitglied)
    {
      rechnungenIt.join("mitglied");
      rechnungenIt.addFilter("mitglied.id = rechnung.mitglied");
    }

    return rechnungenIt;
  }
}
