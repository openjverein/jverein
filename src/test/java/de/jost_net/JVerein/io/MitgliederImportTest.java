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
package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import de.jost_net.JVerein.io.MitgliederImport.ZeilenDurchlauf;

class MitgliederImportTest
{
  /**
   * Öffnet die CSV-Resource so wie MitgliederImport.doImport und liefert pro
   * Zeile in der Reihenfolge des ZeilenDurchlaufs "externemitgliedsnummer:Zeilennummer".
   */
  private List<String> durchlaufen(String resource)
      throws SQLException, ClassNotFoundException, URISyntaxException
  {
    File file = new File(getClass().getResource("/" + resource).toURI());
    String fil = file.getName();
    int pos = fil.lastIndexOf('.');

    Properties props = new Properties();
    props.put("separator", ";");
    props.put("suppressHeaders", "false");
    props.put("charset", "UTF-8");
    props.put("fileExtension", fil.substring(pos));

    Class.forName("org.relique.jdbc.csv.CsvDriver");
    try (Connection conn = DriverManager
        .getConnection("jdbc:relique:csv:" + file.getParent(), props);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
        ResultSet results = stmt
            .executeQuery("SELECT * FROM \"" + fil.substring(0, pos) + "\""))
    {
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results);
      List<String> reihenfolge = new ArrayList<>();
      while (durchlauf.next())
      {
        reihenfolge.add(results.getString("externemitgliedsnummer") + ":"
            + results.getRow());
      }
      return reihenfolge;
    }
  }

  @Test
  void zeilenMitExternerZahlerIdKommenZuletzt() throws Exception
  {
    // Datei: 2 (Angehöriger), 1 (Vollzahler), 3 (Angehöriger), 4 (Einzelperson)
    assertEquals(List.of("1:2", "4:4", "2:1", "3:3"),
        durchlaufen("mitglieder-import-familie.csv"));
  }

  @Test
  void ohneSpalteExternezahleridBleibtReihenfolge() throws Exception
  {
    assertEquals(List.of("2:1", "1:2", "3:3"),
        durchlaufen("mitglieder-import-ohne-externezahlerid.csv"));
  }

  @Test
  void leereZelleZaehltAlsOhneExternezahlerid() throws Exception
  {
    File file = new File(
        getClass().getResource("/mitglieder-import-familie.csv").toURI());
    Properties props = new Properties();
    props.put("separator", ";");
    props.put("suppressHeaders", "false");
    props.put("charset", "UTF-8");
    props.put("fileExtension", ".csv");
    Class.forName("org.relique.jdbc.csv.CsvDriver");
    try (Connection conn = DriverManager
        .getConnection("jdbc:relique:csv:" + file.getParent(), props);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
        ResultSet results = stmt.executeQuery(
            "SELECT * FROM \"mitglieder-import-familie\""))
    {
      results.next(); // Zeile 1: Angehöriger A
      assertEquals("1", ZeilenDurchlauf.getExterneZahlerId(results));
      results.next(); // Zeile 2: Vollzahler, Zelle leer
      assertNull(ZeilenDurchlauf.getExterneZahlerId(results));
    }
  }
}
