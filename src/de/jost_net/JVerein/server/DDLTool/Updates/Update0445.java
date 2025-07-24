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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0445 extends AbstractDDLUpdate
{
  public Update0445(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    {
      execute(addColumn("konto",
          new Column("kommentar", COLTYPE.VARCHAR, 1024, null, false, false)));

      execute(addColumn("konto", new Column("anlagenkonto", COLTYPE.BOOLEAN, 0,
          "FALSE", false, false)));

      execute(addColumn("einstellung", new Column("summenanlagenkonto",
          COLTYPE.BOOLEAN, 0, "FALSE", false, false)));

      execute(addColumn("konto",
          new Column("anlagenart", COLTYPE.BIGINT, 0, null, false, false)));
      Index idx = new Index("ixKonto2", false);
      Column col = new Column("anlagenart", COLTYPE.BIGINT, 0, null, false,
          false);
      idx.add(col);
      execute(idx.getCreateIndex("konto"));
      execute(createForeignKey("fkKonto2", "konto", "anlagenart", "buchungsart",
          "id", "RESTRICT", "NO ACTION"));

      execute(addColumn("konto",
          new Column("anlagenklasse", COLTYPE.BIGINT, 0, null, false, false)));
      idx = new Index("ixKonto3", false);
      col = new Column("anlagenklasse", COLTYPE.BIGINT, 0, null, false, false);
      idx.add(col);
      execute(idx.getCreateIndex("konto"));
      execute(createForeignKey("fkKonto3", "konto", "anlagenklasse",
          "buchungsklasse", "id", "RESTRICT", "NO ACTION"));

      execute(addColumn("konto",
          new Column("afaart", COLTYPE.BIGINT, 0, null, false, false)));
      idx = new Index("ixKonto4", false);
      col = new Column("afaart", COLTYPE.BIGINT, 0, null, false, false);
      idx.add(col);
      execute(idx.getCreateIndex("konto"));
      execute(createForeignKey("fkKonto4", "konto", "afaart", "buchungsart",
          "id", "RESTRICT", "NO ACTION"));

      execute(addColumn("konto",
          new Column("nutzungsdauer", COLTYPE.INTEGER, 1, null, false, false)));

      execute(addColumn("konto",
          new Column("betrag", COLTYPE.DOUBLE, 1, null, false, false)));

      execute(addColumn("konto",
          new Column("anschaffung", COLTYPE.DATE, 10, null, false, false)));

      execute(addColumn("buchungsart", new Column("abschreibung",
          COLTYPE.BOOLEAN, 0, "FALSE", false, false)));

      execute(addColumn("konto",
          new Column("afastart", COLTYPE.DOUBLE, 1, null, false, false)));

      execute(addColumn("konto",
          new Column("afadauer", COLTYPE.DOUBLE, 1, null, false, false)));

      execute(addColumn("konto",
          new Column("afarestwert", COLTYPE.DOUBLE, 1, null, false, false)));

      execute(addColumn("konto",
          new Column("afamode", COLTYPE.INTEGER, 1, null, false, false)));

      execute(addColumn("einstellung",
          new Column("afarestwert", COLTYPE.DOUBLE, 1, "1.0", false, false)));

      execute(addColumn("einstellung", new Column("afainjahresabschluss",
          COLTYPE.BOOLEAN, 0, "TRUE", false, false)));

      execute(addColumn("buchung",
          new Column("abschluss", COLTYPE.BIGINT, 0, null, false, false)));
      idx = new Index("ixBuchung8", false);
      col = new Column("abschluss", COLTYPE.BIGINT, 0, null, false, false);
      idx.add(col);
      execute(idx.getCreateIndex("buchung"));
      execute(createForeignKey("fkBuchung8", "buchung", "abschluss",
          "jahresabschluss", "id", "CASCADE", "NO ACTION"));
    }
  }
}
