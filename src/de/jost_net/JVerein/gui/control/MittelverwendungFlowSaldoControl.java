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
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.AbstractView;

public class MittelverwendungFlowSaldoControl extends BuchungsklasseSaldoControl
{
  public MittelverwendungFlowSaldoControl(AbstractView view)
      throws RemoteException
  {
    super(view);
    mitUmbuchung = false;
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    // Wir erweitern den Iterator aus BuchungsklassealdoControl
    ExtendedDBIterator<PseudoDBObject> it = super.getIterator();

    // Bei der Mittelverwendung verwenden wir nur Geldkonten und zweckfremde
    // Anlagen.
    String filter = "konto.kontoart is null OR (buchungsart.art != ? AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)))"
        + "OR (buchungsart.art = ? AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?))) ";

    if (mitSteuer)
    {
      filter += "OR st.steuerbetrag is not null";
    }
    it.addFilter(filter, ArtBuchungsart.UMBUCHUNG, Kontoart.GELD.getKey(),
        Kontoart.ANLAGE.getKey(), Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
        ArtBuchungsart.UMBUCHUNG, Kontoart.SCHULDEN.getKey(),
        Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey());

    // Die Umbuchungen der Schulden und zweckgebundenen Anlagen ziehen wir von
    // den Einnahmen bzw. Ausgaben ab.
    if (mitSteuer)
    {
      // Nettobetrag berechnen und steuerbetrag der Steuerbuchungsart
      // hinzurechnen
      it.addColumn("COALESCE(SUM("
          + "(CASE WHEN buchungsart.art = ? AND buchung.betrag > 0 "
          + "THEN -1 WHEN buchungsart.art != ? THEN 0 ELSE 1 END) * "
          + "CAST(buchung.betrag * 100 / (100 + "
          // Anlagenkonto immer Bruttobeträge.
          // Alte Steuerbuchungen mit dependencyid lassen wir bestehen ohne
          // Netto zu berehnen.
          + "CASE WHEN konto.kontoart = ? OR buchung.dependencyid > -1 THEN 0 ELSE COALESCE(steuer.satz,0) END"
          + ") AS DECIMAL(10,2))),0) "
          + "+ CASE WHEN buchungsart.art = ? THEN COALESCE(SUM(st.steuerbetrag),0) ELSE 0 END AS "
          + AUSGABEN, ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.AUSGABE,
          Kontoart.ANLAGE.getKey(), ArtBuchungsart.AUSGABE);

      it.addColumn("COALESCE(SUM("
          + "(CASE WHEN buchungsart.art = ? AND buchung.betrag < 0 "
          + "THEN -1 WHEN buchungsart.art != ? THEN 0 ELSE 1 END) * "
          + "CAST(buchung.betrag * 100 / (100 + "
          // Anlagenkonto immer Bruttobeträge.
          // Alte Steuerbuchungen mit dependencyid lassen wir bestehen ohne
          // Netto zu berehnen.
          + "CASE WHEN konto.kontoart = ? OR buchung.dependencyid > -1 THEN 0 ELSE COALESCE(steuer.satz,0) END"
          + ") AS DECIMAL(10,2))),0) "
          + "+ CASE WHEN buchungsart.art = ? THEN COALESCE(SUM(st.steuerbetrag),0) ELSE 0 END AS "
          + EINNAHMEN, ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.EINNAHME,
          Kontoart.ANLAGE.getKey(), ArtBuchungsart.EINNAHME);
    }
    else
    {
      it.addColumn("SUM(CASE WHEN buchungsart.art = ? AND buchung.betrag > 0"
          + " THEN -buchung.betrag WHEN buchungsart.art != ? THEN 0 ELSE buchung.betrag END) AS "
          + AUSGABEN, ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.AUSGABE);

      it.addColumn("SUM(CASE WHEN buchungsart.art = ? AND buchung.betrag < 0"
          + " THEN -buchung.betrag WHEN buchungsart.art != ? THEN 0 ELSE buchung.betrag END) AS "
          + EINNAHMEN, ArtBuchungsart.UMBUCHUNG, ArtBuchungsart.EINNAHME);
    }

    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return "Mittelverwendungs-Saldo";
  }
}
