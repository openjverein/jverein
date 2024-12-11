package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.TablePart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WirtschaftsplanungControl extends AbstractControl
{
  private de.willuhn.jameica.system.Settings settings;

  private TablePart wirtschaftsplaene;

  /**
   * Erzeugt einen neuen AbstractControl der fuer die angegebene View.
   *
   * @param view
   *     die View, fuer die dieser WirtschaftsplanungControl zustaendig ist.
   */
  public WirtschaftsplanungControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Part getWirtschaftsplanungList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();

    //TODO: Filter Rückstellungen etc.
    String sql = "SELECT geschaeftsjahr, SUM(betrag) FROM wirtschaftsplanung, buchungsart WHERE wirtschaftsplanung.buchungsart = buchungsart.id AND buchungsart.art = ? GROUP BY geschaeftsjahr";

    Map<Integer, WirtschaftsplanungZeile> zeileMap = new HashMap<>();

    service.execute(sql, new Object[] { 0 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getInt(1)))
        {
          zeileMap.get(resultSet.getInt(1))
              .setPlanEinnahme(resultSet.getDouble(2));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              resultSet.getInt(1));
          zeile.setPlanEinnahme(resultSet.getDouble(2));
          zeileMap.put(resultSet.getInt(1), zeile);
        }
      }
      return resultSet;
    });

    service.execute(sql, new Object[] { 1 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getInt(1)))
        {
          zeileMap.get(resultSet.getInt(1))
              .setPlanAusgabe(resultSet.getDouble(2));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              resultSet.getInt(1));
          zeile.setPlanAusgabe(resultSet.getDouble(2));
          zeileMap.put(resultSet.getInt(1), zeile);
        }
      }
      return resultSet;
    });

    wirtschaftsplaene = new TablePart(new ArrayList<>(zeileMap.values()), null);

    wirtschaftsplaene.addColumn("Geschäftsjahr", "geschaeftsjahr");
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istEinnahme",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istAusgabe",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));

    return wirtschaftsplaene;
  }
}
