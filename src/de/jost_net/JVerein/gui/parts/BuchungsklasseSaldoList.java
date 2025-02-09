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
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;

public class BuchungsklasseSaldoList extends AbstractSaldoList
{
  public BuchungsklasseSaldoList(Action action, Date datumvon, Date datumbis)
  {
    super(action, datumvon, datumbis, true);
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

    suBukSteuersatz.clear();
    DBIterator<Buchungsart> buchungsartenSteuerIt = service
        .createList(Buchungsart.class);
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      if (buchungsklasseId != null)
        buchungsartenSteuerIt.addFilter("buchungsklasse = ?", buchungsklasseId );
      else
        buchungsartenSteuerIt.addFilter("buchungsklasse is null");
    }
    buchungsartenSteuerIt.addFilter("steuersatz <> 0");
    while (buchungsartenSteuerIt.hasNext())
    {
      buchungsart = (Buchungsart) buchungsartenSteuerIt.next();
      Buchungsart steuer_buchungsart = buchungsart.getSteuerBuchungsart();
      if (steuer_buchungsart != null)
      {
        String steuer_buchungsart_id = steuer_buchungsart.getID();
        if (buchungsart.getArt() == ArtBuchungsart.EINNAHME)
        {
          suBukSteuersatz.put(steuer_buchungsart_id,
              buchungsart.getSteuersatz());
        }
        else if (buchungsart.getArt() == ArtBuchungsart.AUSGABE)
        {
          suBukSteuersatz.put(steuer_buchungsart_id,
              -buchungsart.getSteuersatz());
        }
      }
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
        sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
            + "where datum >= ? and datum <= ?  "
            + "and buchung.konto = konto.id "
            + "and konto.kontoart < ? "
            + "and buchung.buchungsart = buchungsart.id "
            + "and buchungsart.id = ? " + "and buchungsart.art = ?";
        einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
            ArtBuchungsart.EINNAHME }, rsd);
        suBukEinnahmen += einnahmen;
        ausgaben = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
            ArtBuchungsart.AUSGABE }, rsd);
        suBukAusgaben += ausgaben;
        umbuchungen = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
            ArtBuchungsart.UMBUCHUNG }, rsd);
        suBukUmbuchungen += umbuchungen;
      }
      else
      {
        // Buchungsklasse steht in Buchung
        if (buchungsklasseId != null)
        {
          // Buchungen der Buchungsklasse
          sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
              + "where datum >= ? and datum <= ?  "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? "
              + "and buchung.buchungsart = buchungsart.id "
              + "and buchungsart.id = ? " + "and buchungsart.art = ? "
              + "and buchung.buchungsklasse = ? ";
          einnahmen = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), ArtBuchungsart.EINNAHME,
                  buchungsklasseId },
              rsd);
          suBukEinnahmen += einnahmen;
          ausgaben = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), ArtBuchungsart.AUSGABE,
                  buchungsklasseId },
              rsd);
          suBukAusgaben += ausgaben;
          umbuchungen = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), ArtBuchungsart.UMBUCHUNG,
                  buchungsklasseId },
              rsd);
          suBukUmbuchungen += umbuchungen;
        }
        else
        {
          // Buchungen ohne Buchungsklasse
          sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
              + "where datum >= ? and datum <= ?  "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? "
              + "and buchung.buchungsart = buchungsart.id "
              + "and buchungsart.id = ? " + "and buchungsart.art = ? "
              + "and buchung.buchungsklasse is null ";
          einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
              ArtBuchungsart.EINNAHME },
              rsd);
          suBukEinnahmen += einnahmen;
          ausgaben = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
              ArtBuchungsart.AUSGABE },
              rsd);
          suBukAusgaben += ausgaben;
          umbuchungen = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(),
              ArtBuchungsart.UMBUCHUNG },
              rsd);
          suBukUmbuchungen += umbuchungen;
        }
      }

      if (buchungsart.getSteuersatz() > 0.0)
      {
        Double steuersatz = buchungsart.getSteuersatz();
        Double val = 0.0;
        if (buchungsart.getArt() == ArtBuchungsart.EINNAHME)
        {
          val = einnahmen;
        }
        else if (buchungsart.getArt() == ArtBuchungsart.AUSGABE)
        {
          steuersatz = -steuersatz;
          val = ausgaben;
        }
        if (!suBukNetto.containsKey(steuersatz))
        {
          suBukNetto.put(steuersatz, 0.0);
        }
        suBukNetto.put(steuersatz, suBukNetto.get(steuersatz) + val);
      }
      else if (suBukSteuersatz.containsKey(buchungsart.getID()))
      {
        Double steuersatz = suBukSteuersatz.get(buchungsart.getID());
        if (!suBukSteuer.containsKey(steuersatz))
        {
          suBukSteuer.put(steuersatz, 0.0);
        }
        suBukSteuer.put(steuersatz,
            suBukSteuer.get(steuersatz) + einnahmen + ausgaben + umbuchungen);
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
        "Gewinn/Verlust" + " " + bezeichnung,
        suBukEinnahmen + suBukAusgaben + suBukUmbuchungen));

    // Buchungsklasse Übersicht Steuern ausgeben
    Boolean first_row = true;
    for (Double steuersatz : suBukNetto.keySet())
    {
      String string_steuersatz = String.format("%.2f", Math.abs(steuersatz))
          + "% ";
      if (steuersatz > 0.0)
      {
        string_steuersatz += " MwSt.";
      }
      else
      {
        string_steuersatz += " VSt.";
      }
      if (!suBukSteuer.containsKey(steuersatz))
      {
        suBukSteuer.put(steuersatz, 0.0);
      }
      if (first_row)
      {
        zeile.add(new BuchungsklasseSaldoZeile(
            BuchungsklasseSaldoZeile.STEUERHEADER,
            "Steuern " + bezeichnung, string_steuersatz,
            suBukNetto.get(steuersatz), suBukSteuer.get(steuersatz)));
        first_row = false;
      }
      else
      {
        zeile.add(new BuchungsklasseSaldoZeile(
            BuchungsklasseSaldoZeile.STEUER, "", string_steuersatz,
            suBukNetto.get(steuersatz), suBukSteuer.get(steuersatz)));
      }

      // Werte für Gesamtübersicht addieren
      if (!suNetto.containsKey(steuersatz))
      {
        suNetto.put(steuersatz, 0.0);
        suSteuer.put(steuersatz, 0.0);
      }
      suNetto.put(steuersatz,
          suNetto.get(steuersatz) + suBukNetto.get(steuersatz));
      suSteuer.put(steuersatz,
          suSteuer.get(steuersatz) + suBukSteuer.get(steuersatz));

    }
  }

  protected void getSteuerUebersicht(ArrayList<BuchungsklasseSaldoZeile> zeile)
  {
    Boolean first_row = true;
    for (Double steuersatz : suNetto.keySet())
    {
      String string_steuersatz = String.format("%.2f", Math.abs(steuersatz))
          + "% ";
      if (steuersatz > 0.0)
      {
        string_steuersatz += " MwSt.";
      }
      else
      {
        string_steuersatz += " VSt.";
      }
      if (first_row)
      {
        zeile.add(
            new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.STEUERHEADER,
                "Gesamtübersicht Steuern", string_steuersatz,
                suNetto.get(steuersatz), suSteuer.get(steuersatz)));
        first_row = false;
      }
      else
      {
        zeile.add(new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.STEUER,
            "", string_steuersatz, suNetto.get(steuersatz),
            suSteuer.get(steuersatz)));
      }
    }
  }
}
