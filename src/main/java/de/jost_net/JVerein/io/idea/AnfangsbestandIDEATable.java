package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

public class AnfangsbestandIDEATable extends AbstractIDEATable<Anfangsbestand>
{
  public AnfangsbestandIDEATable()
  {
    super("Anfangsbestand", "anfangsbestand.csv");

    primaryKey("id").text().value(Anfangsbestand::getID);

    column("konto").text().value(ab -> ab.getKonto().getID());
    reference("konto", KontoIDEATable.class, "id");

    column("datum").date(dateFormatString)
        .value(ab -> dateFormat.format(ab.getDatum()));

    column("betrag").numeric(2)
        .value(ab -> decimalFormat.format(ab.getBetrag()));
  }

  @Override
  public List<Anfangsbestand> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Anfangsbestand> list = Einstellungen.getDBService()
        .createList(Anfangsbestand.class);

    list.addFilter("datum between ? and ?", jahr.getBeginnGeschaeftsjahr(),
        jahr.getEndeGeschaeftsjahr());

    List<Anfangsbestand> result = new ArrayList<>();

    while (list.hasNext())
    {
      result.add(list.next());
    }

    return result;
  }
}
