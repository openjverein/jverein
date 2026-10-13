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
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.menu.RechnungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.keys.Differenz;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
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
    GenericIterator<Rechnung> rechnungen = getRechnungIterator();
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
        GenericIterator<Rechnung> rechnungen = getRechnungIterator();
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

  @SuppressWarnings("unchecked")
  private GenericIterator<Rechnung> getRechnungIterator()
      throws RemoteException, ApplicationException
  {
    DBIterator<Rechnung> rechnungenIt = Einstellungen.getDBService()
        .createList(Rechnung.class);

    boolean joinMitglied = false;
    for (Entry<Filter, Object> entry : getFilter().entrySet())
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
          Double limit = (Double) getFilter().get(Filter.DIFFERENZ_LIMIT);
          if (limit == null)
          {
            limit = 0.005d;
          }
          // Es ist egal ob der Betrag positiv oder negativ eingetragen
          // wurde
          limit = Math.abs(limit);

          ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
              Sollbuchung.TABLE_NAME);
          it.addColumn(Sollbuchung.T_RECHNUNG + " as rid");
          it.addColumn("sum(cast(COALESCE(buchung.ist,0) - COALESCE("
              + Sollbuchung.T_BETRAG + ",0) AS DECIMAL(10,2))) as dif");
          it.leftJoin(
              "(SELECT sum(COALESCE((betrag),0)) AS ist,"
                  + Buchung.T_SOLLBUCHUNG + " FROM buchung GROUP BY "
                  + Buchung.T_SOLLBUCHUNG + ") AS buchung",
              Buchung.T_SOLLBUCHUNG + " = " + Sollbuchung.TABLE_NAME_ID);

          if (value == Differenz.FEHLBETRAG)
          {
            it.addHaving("dif < ?", -limit);
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
