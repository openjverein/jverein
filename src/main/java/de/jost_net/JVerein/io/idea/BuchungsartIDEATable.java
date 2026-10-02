package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

public class BuchungsartIDEATable extends AbstractIDEATable<Buchungsart>
{

  public BuchungsartIDEATable()
  {
    super("Buchungsart", "buchungsart.csv");

    primaryKey("id").text().value(Buchungsart::getID);

    column("bezeichnung").text().value(Buchungsart::getBezeichnung);

    column("nummer").text().value(Buchungsart::getNummer);

    column("art").text().value(k -> ArtBuchungsart.get(k.getArt()));

    column("buchungsklasse").text()
        .value(b -> b.getBuchungsklasse() == null ? ""
            : b.getBuchungsklasse().getID());
    reference("buchungsklasse", BuchungsklasseIDEATable.class, "id");

    column("steuer").text()
        .value(b -> b.getSteuer() == null ? "" : b.getSteuer().getID());
    reference("steuer", SteuerIDEATable.class, "id");
  }

  @Override
  public List<Buchungsart> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Buchungsart> list = Einstellungen.getDBService()
        .createList(Buchungsart.class);

    List<Buchungsart> result = new ArrayList<>();
    while (list.hasNext())
    {
      result.add(list.next());
    }
    return result;
  }

}
