/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.BuchungsklasseSaldoZeile;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;

public class MittelverwendungFlowSaldoList extends AbstractSaldoList
{
  public MittelverwendungFlowSaldoList(Action action, Date datumvon, Date datumbis)
  {
    super(action, datumvon, datumbis, false);
  }

  protected void createBuchungsklasse(Buchungsklasse buchungsklasse,
      ArrayList<BuchungsklasseSaldoZeile> zeile) throws RemoteException 
  {
    Buchungsart buchungsart = null;
    String buchungsklasseId = null;
    String bezeichnung = "Nicht zugeordnet";
    if (buchungsklasse != null)
    {
      buchungsklasseId = buchungsklasse.getID();
      bezeichnung = buchungsklasse.getBezeichnung();
    }
    
    DBIterator<Buchungsart> buchungsartenIt = service
        .createList(Buchungsart.class);
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      if (buchungsklasseId != null)
        buchungsartenIt.addFilter("buchungsklasse = ?", buchungsklasseId );
      else
        buchungsartenIt.addFilter("buchungsklasse is null");
    }
    buchungsartenIt.setOrder("order by nummer");
    suBukEinnahmen = Double.valueOf(0);
    suBukAusgaben = Double.valueOf(0);
    suBukUmbuchungen = Double.valueOf(0);
    suBukNetto.clear();
    suBukSteuer.clear();

    while (buchungsartenIt.hasNext())
    {
      buchungsart = (Buchungsart) buchungsartenIt.next();
      if (getAnzahlBuchungen(buchungsklasseId, buchungsart) == 0)
      {
        continue;
      }
      String sql = null;
      if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
      {
        // Buchungsklasse steht in Buchungsart
        sql = getSummenKontoSql();
        // Summe aller Zuflüsse bei Geldkonten
        einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.GELD.getKey(), buchungsart.getID(),
            ArtBuchungsart.EINNAHME }, rsd);
        ausgaben = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.GELD.getKey(), buchungsart.getID(),
            ArtBuchungsart.AUSGABE }, rsd);
        sql = sql + " AND konto.zweck = ?";
        // Summe aller Zuflüsse bei nicht nutzungsgebundenen Anlagen
        einnahmen += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                buchungsart.getID(), ArtBuchungsart.EINNAHME,
                Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
            rsd);
        ausgaben += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                buchungsart.getID(), ArtBuchungsart.AUSGABE,
                Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
            rsd);
        // Summe Zuflüsse durch Umbuchung
        // Auszahlung aus Fremdkapital z.B. Darlehen,
        // Rückbuchung von zweckgebundenen Anlagen
        sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0";
        einnahmen -= (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.SCHULDEN.getKey(), Kontoart.ANLAGE.getKey(),
            Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
            ArtBuchungsart.UMBUCHUNG },
            rsd);
        sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0";
        ausgaben -= (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
                Kontoart.ANLAGE.getKey(),
                Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
                ArtBuchungsart.UMBUCHUNG },
            rsd);
        suBukEinnahmen += einnahmen;
        suBukAusgaben += ausgaben;
        umbuchungen = Double.valueOf(0);
      }
      else
      {
        // Buchungsklasse steht in Buchung
        if (buchungsklasseId != null)
        {
          // Buchungen der Buchungsklasse
          sql = getSummenKontoSql() + " AND buchung.buchungsklasse = ? ";
          // Summe aller Zuflüsse bei Geldkonten
          einnahmen = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.GELD.getKey(),
                  buchungsart.getID(), ArtBuchungsart.EINNAHME,
                  buchungsklasseId },
              rsd);
          ausgaben = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.GELD.getKey(),
                  buchungsart.getID(), ArtBuchungsart.AUSGABE,
                  buchungsklasseId },
              rsd);
          sql = sql + " AND konto.zweck = ?";
          // Summe aller Zuflüsse bei nicht nutzungsgebundenen Anlagen
          einnahmen += (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                  buchungsart.getID(), ArtBuchungsart.EINNAHME,
                  buchungsklasseId,
                  Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
              rsd);
          ausgaben += (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                  buchungsart.getID(), ArtBuchungsart.AUSGABE, buchungsklasseId,
                  Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
              rsd);
          // Summe Zuflüsse durch Umbuchung
          // Auszahlung aus Fremdkapital z.B. Darlehen,
          // Rückbuchung von zweckgebundenen Anlagen
          sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0"
              + " AND buchung.buchungsklasse = ? ";
          einnahmen -= (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
                  Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
                  ArtBuchungsart.UMBUCHUNG, buchungsklasseId },
              rsd);
          sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0"
              + " AND buchung.buchungsklasse = ? ";
          ausgaben -= (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
                  Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
                  ArtBuchungsart.UMBUCHUNG, buchungsklasseId },
              rsd);
          suBukEinnahmen += einnahmen;
          suBukAusgaben += ausgaben;
          umbuchungen = Double.valueOf(0);
        }
        else
        {
          // Buchungen ohne Buchungsklasse
          sql = getSummenKontoSql() + " AND buchung.buchungsklasse is null";
          // Summe aller Zuflüsse bei Geldkonten
          einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.GELD.getKey(), buchungsart.getID(),
              ArtBuchungsart.EINNAHME }, rsd);
          ausgaben = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.GELD.getKey(),
                  buchungsart.getID(), ArtBuchungsart.AUSGABE },
              rsd);
          sql = sql + " AND konto.zweck = ?";
          // Summe aller Zuflüsse bei nicht nutzungsgebundenen Anlagen
          einnahmen += (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                  buchungsart.getID(), ArtBuchungsart.EINNAHME,
                  Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
              rsd);
          ausgaben += (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
                  buchungsart.getID(), ArtBuchungsart.AUSGABE,
                  Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
              rsd);
          // Summe Zuflüsse durch Umbuchung
          // Auszahlung aus Fremdkapital z.B. Darlehen,
          // Rückbuchung von zweckgebundenen Anlagen
          sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0"
              + " AND buchung.buchungsklasse is null";
          einnahmen -= (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
                  Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
              ArtBuchungsart.UMBUCHUNG },
              rsd);
          sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0"
              + " AND buchung.buchungsklasse is null";
          ausgaben -= (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
                  Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), buchungsart.getID(),
                  ArtBuchungsart.UMBUCHUNG },
              rsd);
          suBukEinnahmen += einnahmen;
          suBukAusgaben += ausgaben;
          umbuchungen = Double.valueOf(0);
        }
      }

      if (Math.abs(einnahmen) >= LIMIT || Math.abs(ausgaben) >= LIMIT
          || Math.abs(umbuchungen) >= LIMIT
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        zeile.add(new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.DETAIL,
            buchungsart, einnahmen, ausgaben, umbuchungen));
      }
    }
    suEinnahmen += suBukEinnahmen;
    suAusgaben += suBukAusgaben;
    suUmbuchungen += suBukUmbuchungen;
    if (Math.abs(suBukEinnahmen) < LIMIT && Math.abs(suBukAusgaben) < LIMIT
        && Math.abs(suBukUmbuchungen) < LIMIT
        && Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
    {
      zeile.remove(zeile.size() - 1);
      return;
    }

    zeile.add(
        new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.SALDOFOOTER,
            "Salden" + " " + bezeichnung, suBukEinnahmen,
            suBukAusgaben, suBukUmbuchungen));
    zeile.add(new BuchungsklasseSaldoZeile(
        BuchungsklasseSaldoZeile.SALDOGEWINNVERLUST,
        "Zufluss/Abfluss" + " " + bezeichnung,
        suBukEinnahmen + suBukAusgaben + suBukUmbuchungen));
  }

  private String getSummenKontoSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.id = ?" + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenUmbuchungSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?))"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.id = ?" + " AND buchungsart.art = ?";
    return sql;
  }
}
