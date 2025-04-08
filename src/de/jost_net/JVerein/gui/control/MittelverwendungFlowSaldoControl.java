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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class MittelverwendungFlowSaldoControl extends BuchungsklasseSaldoControl
{
  private TablePart saldoList;

  public MittelverwendungFlowSaldoControl(AbstractView view)
      throws RemoteException
  {
    super(view);
    mitSteuer = false;
    mitUmbuchung = false;
  }

  @Override
  public TablePart getSaldoList() throws ApplicationException
  {
    try
    {
      if (saldoList != null)
      {
        return saldoList;
      }
      saldoList = new TablePart(getList(), null)
      {
        // Sortieren verhindern
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn("Buchungsklasse", GRUPPE, null,
          false);
      saldoList.addColumn("Buchungsart", BUCHUNGSART);
      saldoList.addColumn("Einnahmen", EINNAHMEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Ausgaben", AUSGABEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Anzahl", ANZAHL);
      saldoList.setRememberColWidths(true);
      saldoList.removeFeature(FeatureSummary.class);
      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    // Wir erweitern den Iterator aus BuchungsklassealdoControl
    ExtendedDBIterator<PseudoDBObject> it = super.getIterator();

    // Bei der Mittelverwendung verwenden wir nur Geldkonten und zweckfremde
    // Anlagen.
    // Die Umbuchungen der Schulden und zweckgebundenen Anlagen ziehen wir von
    // den Einnahmen bzw. Ausgaben ab.
    it.addFilter(
        "(buchungsart.art != ? AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)))"
            + "OR (buchungsart.art = ? AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)))",
        ArtBuchungsart.UMBUCHUNG, Kontoart.GELD.getKey(),
        Kontoart.ANLAGE.getKey(), Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
        ArtBuchungsart.UMBUCHUNG, Kontoart.SCHULDEN.getKey(),
        Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey());

    // Damit wir die Umbuchungen mit in die Einnahmen und Ausgaben Spalten
    // aufnehmen können, müsen wir hier die Beträge berechnen.
    it.addColumn("SUM(CASE WHEN buchungsart.art = ? AND buchung.betrag > 0"
        + " THEN -buchung.betrag WHEN buchungsart.art != ? THEN 0 ELSE buchung.betrag END) AS "
        + AUSGABEN,
        ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.AUSGABE);
    it.addColumn("SUM(CASE WHEN buchungsart.art = ? AND buchung.betrag < 0"
        + " THEN -buchung.betrag WHEN buchungsart.art != ? THEN 0 ELSE buchung.betrag END) AS "
        + EINNAHMEN,
        ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.EINNAHME);
    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return "Mittelverwendungs-Saldo";
  }
}
