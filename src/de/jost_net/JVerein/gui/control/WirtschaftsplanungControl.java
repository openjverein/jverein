package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.OpenWirtschaftsplanungAction;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WirtschaftsplanungControl extends AbstractControl
{
  private de.willuhn.jameica.system.Settings settings;

  private TablePart wirtschaftsplaene;

  private TablePart einnahmen;

  private TablePart ausgaben;

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

    String sql = "SELECT wirtschaftsplan.id, wirtschaftsplan.datum_von, wirtschaftsplan.datum_bis, SUM(wirtschaftsplanitem.soll) " +
        "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart " +
        "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " +
        "AND wirtschaftsplanitem.buchungsart = buchungsart.id " +
        "AND buchungsart.art = ? " +
        "GROUP BY wirtschaftsplan.id, wirtschaftsplan.datum_von, wirtschaftsplan.datum_bis";

    Map<Long, WirtschaftsplanungZeile> zeileMap = new HashMap<>();

    service.execute(sql, new Object[] { 0 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getLong(1)))
        {
          zeileMap.get(resultSet.getLong(1))
              .setPlanEinnahme(resultSet.getDouble(4));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              resultSet.getLong(1), resultSet.getDate(2), resultSet.getDate(3));
          zeile.setPlanEinnahme(resultSet.getDouble(4));
          zeileMap.put(resultSet.getLong(1), zeile);
        }
      }
      return resultSet;
    });

    service.execute(sql, new Object[] { 1 }, resultSet -> {
      while (resultSet.next())
      {
        if (zeileMap.containsKey(resultSet.getLong(1)))
        {
          zeileMap.get(resultSet.getLong(1))
              .setPlanAusgabe(resultSet.getDouble(4));
        }
        else
        {
          WirtschaftsplanungZeile zeile = new WirtschaftsplanungZeile(
              resultSet.getLong(1), resultSet.getDate(2), resultSet.getDate(3));
          zeile.setPlanAusgabe(resultSet.getDouble(4));
          zeileMap.put(resultSet.getLong(1), zeile);
        }
      }
      return resultSet;
    });

    sql = "SELECT wirtschaftsplan.id, SUM(buchung.betrag) AS ist " +
        "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart, buchung, konto " +
        "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " +
        "AND buchung.buchungsart = buchungsart.id " +
        "AND buchung.konto = konto.id " +
        "AND buchung.datum >= wirtschaftsplan.datum_von " +
        "AND buchung.datum <= wirtschaftsplan.datum_bis " +
        "AND buchungsart.art = ? " +
        "AND konto.kontoart < ? " +
        "GROUP BY wirtschaftsplan.id";

    service.execute(sql, new Object[] { 0, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getLong(1)))
            {
              zeileMap.get(resultSet.getLong(1))
                  .setIstEinnahme(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    service.execute(sql, new Object[] { 1, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getLong(1)))
            {
              zeileMap.get(resultSet.getLong(1))
                  .setIstAusgabe(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    wirtschaftsplaene = new TablePart(new ArrayList<>(zeileMap.values()), new OpenWirtschaftsplanungAction());

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);
    DateFormatter dateFormatter = new DateFormatter(new JVDateFormatTTMMJJJJ());

    wirtschaftsplaene.addColumn("ID", "id");
    wirtschaftsplaene.addColumn("Von", "datum_von", dateFormatter);
    wirtschaftsplaene.addColumn("Bis", "datum_bis", dateFormatter);
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Soll", "planSaldo", formatter);
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Ist", "istSaldo", formatter);
    wirtschaftsplaene.addColumn("Saldo Differenz", "differenz", formatter);

    return wirtschaftsplaene;
  }

  public WirtschaftsplanungZeile getWirtschaftsplanungZeile()
  {
    if (getCurrentObject() instanceof WirtschaftsplanungZeile) {
      return (WirtschaftsplanungZeile) getCurrentObject();
    }
    return null;
  }

  public TablePart getEinnahmen() throws RemoteException
  {
    WirtschaftsplanungZeile zeile = getWirtschaftsplanungZeile();

    if (zeile == null) {
      return null;
    }

    if (einnahmen == null) {
      einnahmen = new TablePart(null);
    }

    einnahmen.removeAll();

    DBService service = Einstellungen.getDBService();
    String sql = "SELECT buchungsart, betrag FROM wirtschaftsplanitem WHERE wirtschaftsplan = ?";
    service.execute(sql, new Object[] {zeile.getID()}, null);

    //TODO

    return einnahmen;
  }
}
