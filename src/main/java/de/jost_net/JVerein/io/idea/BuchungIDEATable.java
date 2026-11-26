package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

public class BuchungIDEATable extends AbstractIDEATable<Buchung>
{
  public BuchungIDEATable()
  {
    super("Buchung", "buchung.csv");

    primaryKey("id").text().value(Buchung::getID);

    column("konto").text()
        .value(b -> b.getKonto() == null ? "" : b.getKonto().getID());
    reference("konto", KontoIDEATable.class, "id");

    column("name").text().value(Buchung::getName);

    column("buchungsart").text().value(
        b -> b.getBuchungsart() == null ? "" : b.getBuchungsart().getID());
    reference("buchungsart", BuchungsartIDEATable.class, "id");

    column("buchungsklasse").text()
        .value(b -> b.getBuchungsklasse() == null ? ""
            : b.getBuchungsklasse().getID());
    reference("buchungsklasse", BuchungsklasseIDEATable.class, "id");

    column("splitid").text()
        .value(b -> b.getSplitId() == null ? "" : b.getSplitId().toString());
    reference("splitid", BuchungIDEATable.class, "id");

    column("datum").date(dateFormatString)
        .value(b -> dateFormat.format(b.getDatum()));

    column("zweck").text().value(Buchung::getZweck);

    column("betrag").numeric(2).value(b -> decimalFormat.format(b.getBetrag()));

    column("steuer").text()
        .value(b -> b.getSteuer() == null ? "" : b.getSteuer().getID());
    reference("steuer", SteuerIDEATable.class, "id");
  }

  @Override
  public List<Buchung> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Buchung> list = Einstellungen.getDBService()
        .createList(Buchung.class);

    list.addFilter("datum between ? and ?", jahr.getBeginnGeschaeftsjahr(),
        jahr.getEndeGeschaeftsjahr());

    List<Buchung> result = new ArrayList<>();

    while (list.hasNext())
    {
      result.add(list.next());
    }

    return result;
  }
}
