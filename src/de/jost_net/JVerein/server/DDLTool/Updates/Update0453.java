package de.jost_net.JVerein.server.DDLTool.Updates;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0453 extends AbstractDDLUpdate
{
  public Update0453(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("einstellung",
        new Column("wirtschaftsplanung", COLTYPE.BOOLEAN, 0, null, false,
            false)));

    Table wirtschaftsplanung = new Table("wirtschaftsplanung");
    Column gj = new Column("geschaeftsjahr", COLTYPE.INTEGER, 4, null, true,
        false);
    wirtschaftsplanung.add(gj);
    Column buchungsart = new Column("buchungsart", COLTYPE.BIGINT, 4, null,
        true, false);
    wirtschaftsplanung.add(buchungsart);
    wirtschaftsplanung.setPrimaryKey(gj, buchungsart);
    wirtschaftsplanung.add(
        new Column("betrag", COLTYPE.DOUBLE, 10, "0.0", true, false));

    execute(createTable(wirtschaftsplanung));

    Index idx = new Index("ix_wirtschaftsplanung", false);
    idx.add(buchungsart);
    execute(idx.getCreateIndex("wirtschaftsplanung"));

    execute(createForeignKey("fk_wirtschaftsplanung", "wirtschaftsplanung",
        "buchungsart", "buchungsart", "id", "RESTRICT", "CASCADE"));

    execute("INSERT INTO wirtschaftsplanung(geschaeftsjahr, buchungsart, betrag) VALUES (2024, 1, 200), (2023, 1, 1000)");
  }
}
