package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.OpenWirtschaftsplanungAction;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.keys.Kontoart;
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

    String startGJ = Einstellungen.getEinstellung().getBeginnGeschaeftsjahr();
    sql = "WITH buchung_mit_gj AS ( " + "SELECT buchung.*, CASE WHEN datum >= TO_DATE( ? || EXTRACT(YEAR FROM datum), 'DD.MM.YYYY') THEN " + "EXTRACT(YEAR FROM datum) ELSE EXTRACT(YEAR FROM datum) - 1 " + "END AS geschaeftsjahr " + "FROM buchung " + ") SELECT buchung_mit_gj.geschaeftsjahr, SUM(buchung_mit_gj.betrag) AS ist " + "FROM buchung_mit_gj, wirtschaftsplanung, buchungsart, konto " + "WHERE buchung_mit_gj.geschaeftsjahr = wirtschaftsplanung.geschaeftsjahr " + "AND buchung_mit_gj.buchungsart = buchungsart.id " + "AND buchung_mit_gj.konto = konto.id " + "AND buchungsart.art = ? " + "AND konto.kontoart < ? " + "GROUP BY buchung_mit_gj.geschaeftsjahr";

    service.execute(sql, new Object[] { startGJ, 0, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getInt(1)))
            {
              zeileMap.get(resultSet.getInt(1))
                  .setIstEinnahme(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    service.execute(sql, new Object[] { startGJ, 1, Kontoart.LIMIT.getKey() },
        resultSet -> {
          while (resultSet.next())
          {
            if (zeileMap.containsKey(resultSet.getInt(1)))
            {
              zeileMap.get(resultSet.getInt(1))
                  .setIstAusgabe(resultSet.getDouble(2));
            }
          }

          return resultSet;
        });

    wirtschaftsplaene = new TablePart(new ArrayList<>(zeileMap.values()), new OpenWirtschaftsplanungAction());

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.DECIMALFORMAT);

    wirtschaftsplaene.addColumn("Geschäftsjahr", "geschaeftsjahr");
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Soll", "planSaldo", formatter);
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Ist", "istSaldo", formatter);
    wirtschaftsplaene.addColumn("Saldo Differenz", "differenz", formatter);

    return wirtschaftsplaene;
  }
}
