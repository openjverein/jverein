package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

public class SteuerIDEATable extends AbstractIDEATable<Steuer>
{
  public SteuerIDEATable()
  {
    super("Steuer", "steuer.csv");

    primaryKey("id").text().value(s -> s.getID());

    column("name").text().value(s -> s.getName());

    column("satz").numeric(2).value(s -> decimalFormat.format(s.getSatz()));

    column("buchungsart").text().value(
        s -> s.getBuchungsart() == null ? "" : s.getBuchungsart().getID());
    reference("buchungsart", BuchungsartIDEATable.class, "id");

    column("buchungsklasse").text()
        .value(s -> s.getBuchungsklasse() == null ? ""
            : s.getBuchungsklasse().getID());
    reference("buchungsklasse", BuchungsklasseIDEATable.class, "id");
  }

  @Override
  public List<Steuer> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Steuer> list = Einstellungen.getDBService()
        .createList(Steuer.class);

    List<Steuer> result = new ArrayList<Steuer>();

    while (list.hasNext())
      result.add(list.next());

    return result;
  }
}
