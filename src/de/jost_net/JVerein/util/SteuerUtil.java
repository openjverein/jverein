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
package de.jost_net.JVerein.util;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Zusatzbetrag;

public class SteuerUtil
{
  /**
   * Speichert die Steuer aus der Buchungsart in den zugeordnete Buchungen
   * 
   * @return anzahl geänderte Zeilen
   * @throws RemoteException
   */
  public static int setSteuerToBuchung() throws RemoteException
  {
    // Nur Buchungen auf Geldkonten können Steuern haben
    // Buchungen
    String sql = "update buchung "
        + "set steuer = (select buchungsart.steuer from buchungsart "
        + "where buchung.buchungsart = buchungsart.id) "
        + "where exists (select id from buchungsart where buchungsart.id = buchung.buchungsart "
        + "and steuer is not null) "
        + "and exists (select id from konto where konto.id = buchung.konto and konto.kontoart = "
        + Kontoart.GELD.getKey() + ") "
        + "and buchungsart is not null and steuer is null";

    int anzahlBuchungen = Einstellungen.getDBService().executeUpdate(sql, null);

    // Sollbuchungspositionen
    sql = "update sollbuchungposition "
        + "set steuer = (select buchungsart.steuer from buchungsart "
        + "where sollbuchungposition.buchungsart = buchungsart.id) "
        + "where exists (select id from buchungsart where buchungsart.id = sollbuchungposition.buchungsart "
        + "and steuer is not null)"
        + "and buchungsart is not null and steuer is null";

    int anzahlSollbuchungpositionen = Einstellungen.getDBService()
        .executeUpdate(sql, null);

    // Zusatzbeträge
    sql = "update " + Zusatzbetrag.TABLE_NAME
        + " set steuer = (select buchungsart.steuer from buchungsart "
        + "where " + Zusatzbetrag.T_BUCHUNGSART + " = buchungsart.id) "
        + "where exists (select id from buchungsart where buchungsart.id = "
        + Zusatzbetrag.T_BUCHUNGSART + " and steuer is not null)"
        + "and buchungsart is not null and steuer is null";

    int anzahlZusatzbetraege = Einstellungen.getDBService().executeUpdate(sql,
        null);

    // Zusatzbeträge-Vorlagen
    sql = "update zusatzbetragvorlage "
        + "set steuer = (select buchungsart.steuer from buchungsart "
        + "where zusatzbetragvorlage.buchungsart = buchungsart.id) "
        + "where exists (select id from buchungsart where buchungsart.id = zusatzbetragvorlage.buchungsart "
        + "and steuer is not null)"
        + "and buchungsart is not null and steuer is null";

    int anzahlZusatzbetraegeVorlagen = Einstellungen.getDBService()
        .executeUpdate(sql, null);

    // Beitragsgruppen
    sql = "update beitragsgruppe "
        + "set steuer = (select buchungsart.steuer from buchungsart "
        + "where beitragsgruppe.buchungsart = buchungsart.id) "
        + "where exists (select id from buchungsart where buchungsart.id = beitragsgruppe.buchungsart "
        + "and steuer is not null)"
        + "and buchungsart is not null and steuer is null";

    int anzahlZusatzbeträge = Einstellungen.getDBService().executeUpdate(sql,
        null);

    return anzahlBuchungen + anzahlSollbuchungpositionen + anzahlZusatzbetraege
        + anzahlZusatzbetraegeVorlagen + anzahlZusatzbeträge;
  }
}
