package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.OpenWirtschaftsplanungAction;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungPostenDialog;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.*;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WirtschaftsplanungControl extends AbstractControl
{
  private de.willuhn.jameica.system.Settings settings;

  private TablePart wirtschaftsplaene;

  private TreePart einnahmen;

  private TreePart ausgaben;

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

    String sql = "SELECT wirtschaftsplan.id, wirtschaftsplan.datum_von, wirtschaftsplan.datum_bis, SUM(wirtschaftsplanitem.soll) " + "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart " + "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " + "AND wirtschaftsplanitem.buchungsart = buchungsart.id " + "AND buchungsart.art = ? " + "GROUP BY wirtschaftsplan.id, wirtschaftsplan.datum_von, wirtschaftsplan.datum_bis";

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

    wirtschaftsplaene = new TablePart(new ArrayList<>(zeileMap.values()),
        new OpenWirtschaftsplanungAction());

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
    if (getCurrentObject() instanceof WirtschaftsplanungZeile)
    {
      return (WirtschaftsplanungZeile) getCurrentObject();
    }
    return null;
  }

  public Part getEinnahmen() throws RemoteException
  {
    einnahmen = generateTree(0);
    return einnahmen;
  }

  public Part getAusgaben() throws RemoteException {
    ausgaben = generateTree(1);
    return ausgaben;
  }


  private TreePart generateTree(int art) throws RemoteException {
    WirtschaftsplanungZeile zeile = getWirtschaftsplanungZeile();

    if (zeile == null)
    {
      return null;
    }

    Map<Long, WirtschaftsplanungNode> nodes = new HashMap<>();

    DBService service = Einstellungen.getDBService();
    String sql = "SELECT wirtschaftsplanitem.buchungsklasse, sum(soll) " +
            "FROM wirtschaftsplanitem, buchungsart " +
            "WHERE wirtschaftsplan = ? AND wirtschaftsplanitem.buchungsart = buchungsart.id AND buchungsart.art = ? " +
            "GROUP BY wirtschaftsplanitem.buchungsklasse";

    service.execute(sql, new Object[] { zeile.getID(), art }, resultSet -> {
      while (resultSet.next())
      {
        DBIterator<Buchungsklasse> iterator = service.createList(
                Buchungsklasse.class);
        iterator.addFilter("id = ?", resultSet.getLong(1));
        if (!iterator.hasNext())
        {
          continue;
        }

        Buchungsklasse buchungsklasse = iterator.next();
        double soll = resultSet.getDouble(2);
        nodes.put(resultSet.getLong(1),
                new WirtschaftsplanungNode(buchungsklasse, art, zeile));
        nodes.get(resultSet.getLong(1)).setSoll(soll);
      }

      return nodes;
    });

    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sql = "SELECT buchung.buchungsklasse, sum(buchung.betrag) " +
              "FROM buchung, buchungsart " +
              "WHERE buchung.buchungsart = buchungsart.id " +
              "AND buchung.datum >= ? AND buchung.datum <= ? " +
              "AND buchungsart.art = ? " +
              "GROUP BY buchung.buchungsklasse";

    }
    else
    {
      sql = "SELECT buchungsart.buchungsklasse, sum(buchung.betrag) " +
              "FROM buchung, buchungsart " +
              "WHERE buchung.buchungsart = buchungsart.id " +
              "AND buchung.datum >= ? AND buchung.datum <= ? " +
              "AND buchungsart.art = ? " +
              "GROUP BY buchungsart.buchungsklasse";

    }
    service.execute(sql, new Object[] { zeile.getVon(), zeile.getBis(), art },
            resultSet -> {
              while (resultSet.next())
              {
                DBIterator<Buchungsklasse> iterator = service.createList(
                        Buchungsklasse.class);
                Long key = resultSet.getLong(1);
                iterator.addFilter("id = ?", key);
                if (!iterator.hasNext())
                {
                  continue;
                }

                Buchungsklasse buchungsklasse = iterator.next();
                double ist = resultSet.getDouble(2);

                if (nodes.containsKey(key))
                {
                  nodes.get(key).setIst(ist);
                }
                else if (ist != 0)
                {
                  nodes.put(key, new WirtschaftsplanungNode(buchungsklasse, art, zeile));
                  nodes.get(key).setIst(ist);
                }
              }

              return nodes;
            });

    TreePart treePart = new TreePart(new ArrayList<>(nodes.values()), context -> {
      if (! (context instanceof WirtschaftsplanungNode)) {
        return;
      }

      WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

      if (node.getType() != WirtschaftsplanungNode.Type.POSTEN) {
        return;
      }

      WirtschaftsplanungPostenDialog dialog = new WirtschaftsplanungPostenDialog(node.getWirtschaftsplanItem());
      node.setWirtschaftsplanItem(dialog.open());
    });

    CurrencyFormatter formatter = new CurrencyFormatter("",
            Einstellungen.DECIMALFORMAT);
    treePart.addColumn("Buchungsklasse", "buchungsklassebezeichnung");
    treePart.addColumn("Buchungsart / Posten",
            "buchungsartbezeichnung_posten");
    treePart.addColumn("Soll", "soll", formatter);
    treePart.addColumn("Ist", "ist", formatter);

    return treePart;
  }
}