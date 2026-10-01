package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

public class BuchungsklasseIDEATable extends AbstractIDEATable<Buchungsklasse>
{

  public BuchungsklasseIDEATable()
  {
    super("Buchungsklasse", "buchungsklasse.csv");

    primaryKey("id").text().value(Buchungsklasse::getID);

    column("bezeichnung").text().value(Buchungsklasse::getBezeichnung);

    column("nummer").text().value(Buchungsklasse::getNummer);
  }

  @Override
  public List<Buchungsklasse> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);

    List<Buchungsklasse> result = new ArrayList<>();
    while (list.hasNext())
    {
      result.add(list.next());
    }
    return result;
  }

}
