package de.jost_net.JVerein.io.idea;

import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;

/**
 * Implementierung fuer die Konto-Tabelle.
 */
public class KontoIDEATable extends AbstractIDEATable<Konto>
{
  public KontoIDEATable()
  {
    super("Konto", "konto.csv");

    primaryKey("id").text().value(Konto::getID);

    column("bezeichnung").text().value(Konto::getBezeichnung);

    column("nummer").text().value(Konto::getNummer);

    column("eroeffnung").date(dateFormatString)
        .value(k -> k.getEroeffnung() == null ? ""
            : dateFormat.format(k.getEroeffnung()));

    column("aufloesung").date(dateFormatString)
        .value(k -> k.getAufloesung() == null ? ""
            : dateFormat.format(k.getAufloesung()));

    column("buchungsart").text().value(
        k -> k.getBuchungsart() == null ? "" : k.getBuchungsart().getID());
    reference("buchungsart", BuchungsartIDEATable.class, "id");

    column("anlagenart").text()
        .value(k -> k.getAnlagenart() == null ? "" : k.getAnlagenart().getID());
    reference("anlagenart", BuchungsartIDEATable.class, "id");

    column("anlagenklasse").text().value(k -> k.getBuchungsklasse() == null ? ""
        : k.getBuchungsklasse().getID());
    reference("anlagenklasse", BuchungsklasseIDEATable.class, "id");

    column("afaart").text()
        .value(k -> k.getAfaart() == null ? "" : k.getAfaart().getID());
    reference("afaart", BuchungsartIDEATable.class, "id");

    column("nutzungsdauer").numeric(0)
        .value(k -> k.getNutzungsdauer() == null ? ""
            : k.getNutzungsdauer().toString());

    column("betrag").numeric(2).value(
        k -> k.getBetrag() == null ? "" : decimalFormat.format(k.getBetrag()));

    column("anschaffung").date(dateFormatString)
        .value(k -> k.getAnschaffung() == null ? ""
            : dateFormat.format(k.getAnschaffung()));

    column("kontoart").text().value(k -> k.getKontoArt().getText());
  }

  @Override
  public List<Konto> getLines(Geschaeftsjahr jahr) throws Exception
  {
    DBIterator<Konto> list = Einstellungen.getDBService()
        .createList(Konto.class);

    List<Konto> result = new ArrayList<>();

    while (list.hasNext())
    {
      result.add(list.next());
    }

    return result;
  }

}
