/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0450 extends AbstractDDLUpdate
{
  public Update0450(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    {
      // Tebelle rechnung
      Table t = new Table("rechnung");
      Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
      t.add(pk);
      t.setPrimaryKey(pk);

      Column datum = new Column("datum", COLTYPE.DATE, 10, null, true, false);
      t.add(datum);

      Column mitglied = new Column("mitglied", COLTYPE.BIGINT, 10, null, true,
          false);
      t.add(mitglied);

      Column formular = new Column("formular", COLTYPE.BIGINT, 10, null, true,
          false);
      t.add(formular);

      Column betrag = new Column("betrag", COLTYPE.DOUBLE, 10, null, true,
          false);
      t.add(betrag);

      Column personenart = new Column("personenart", COLTYPE.VARCHAR, 1, null,
          false, false);
      t.add(personenart);

      Column anrede = new Column("anrede", COLTYPE.VARCHAR, 40, null, false,
          false);
      t.add(anrede);

      Column titel = new Column("titel", COLTYPE.VARCHAR, 40, null, false,
          false);
      t.add(titel);

      Column name = new Column("name", COLTYPE.VARCHAR, 40, null, true, false);
      t.add(name);

      Column vorname = new Column("vorname", COLTYPE.VARCHAR, 40, null, false,
          false);
      t.add(vorname);

      Column strasse = new Column("strasse", COLTYPE.VARCHAR, 40, null, false,
          false);
      t.add(strasse);

      Column adressierungszusatz = new Column("adressierungszusatz",
          COLTYPE.VARCHAR, 40, null, false, false);
      t.add(adressierungszusatz);

      Column plz = new Column("plz", COLTYPE.VARCHAR, 10, null, false, false);
      t.add(plz);

      Column ort = new Column("ort", COLTYPE.VARCHAR, 40, null, false, false);
      t.add(ort);

      Column staat = new Column("staat", COLTYPE.VARCHAR, 50, null, false,
          false);
      t.add(staat);

      Column geschlecht = new Column("geschlecht", COLTYPE.VARCHAR, 1, null,
          false, false);
      t.add(geschlecht);

      Column mandatid = new Column("mandatid", COLTYPE.VARCHAR, 10, null, false,
          false);
      t.add(mandatid);

      Column mandatdatum = new Column("mandatdatum", COLTYPE.DATE, 10, null,
          false, false);
      t.add(mandatdatum);

      Column bic = new Column("bic", COLTYPE.VARCHAR, 11, null, false, false);
      t.add(bic);

      Column iban = new Column("iban", COLTYPE.VARCHAR, 34, null, false, false);
      t.add(iban);

      execute(this.createTable(t));

      // Indexes und ForeignKeys in rechnung
      Index idx = new Index("ixRechnung1", false);
      idx.add(mitglied);
      execute(idx.getCreateIndex("rechnung"));

      execute(this.createForeignKey("fk_rechnung", "rechnung", "mitglied",
          "mitglied", "id", "CASCADE", "RESTRICT"));

      idx = new Index("ixRechnung2", false);
      idx.add(formular);
      execute(idx.getCreateIndex("rechnung"));

      execute(this.createForeignKey("fk_rechnung2", "rechnung", "formular",
          "formular", "id", "RESTRICT", "RESTRICT"));

      // Spalte rechnung in mitgliedskonto
      Column col = new Column("rechnung", COLTYPE.BIGINT, 0, null, false,
          false);
      execute(addColumn("mitgliedskonto", col));

      // Index und ForeignKey in mitgliedskonto
      idx = new Index("ixMitgliedskonto5", false);
      idx.add(col);
      execute(idx.getCreateIndex("mitgliedskonto"));

      execute(this.createForeignKey("fkMitgliedskonto5", "mitgliedskonto",
          "rechnung", "rechnung", "id", "SET NULL", "RESTRICT"));

    }
  }
}
