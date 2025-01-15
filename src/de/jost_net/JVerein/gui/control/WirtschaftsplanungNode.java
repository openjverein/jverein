package de.jost_net.JVerein.gui.control;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WirtschaftsplanungNode implements GenericObjectNode
{
  public enum Type {BUCHUNGSKLASSE, BUCHUNGSART, POSTEN, UNBEKANNT}

  Type type;

  private Buchungsklasse buchungsklasse;

  private Buchungsart buchungsart;

  private String posten;

  private double soll = 0;

  private double ist = 0;

  private WirtschaftsplanungNode parent;

  private List<WirtschaftsplanungNode> children;

  public WirtschaftsplanungNode(Buchungsklasse buchungsklasse, int art, WirtschaftsplanungZeile zeile)
      throws RemoteException
  {
    type = Type.BUCHUNGSKLASSE;
    this.soll = 0;
    ist = 0;
    this.buchungsklasse = buchungsklasse;

    Map<Long, WirtschaftsplanungNode> nodes = new HashMap<>();
    DBService service = Einstellungen.getDBService();
    String sql = "SELECT wirtschaftsplanitem.buchungsart, sum(wirtschaftsplanitem.soll)" +
        "FROM wirtschaftsplanitem, buchungsart " +
        "WHERE wirtschaftsplanitem.buchungsart = buchungsart.id " +
        "AND buchungsart.art = ? " +
        "AND wirtschaftsplanitem.buchungsklasse = ? " +
        "AND wirtschaftsplanitem.wirtschaftsplan = ? " +
        "GROUP BY wirtschaftsplanitem.buchungsart";

    service.execute(sql, new Object[] { art, buchungsklasse.getID(), zeile.getID() },
        resultSet -> {
          while (resultSet.next()) {
            DBIterator<Buchungsart> iterator = service.createList(
                Buchungsart.class);
            iterator.addFilter("id = ?", resultSet.getLong(1));
            if (!iterator.hasNext())
            {
              continue;
            }

            Buchungsart buchungsart = iterator.next();
            double soll = resultSet.getDouble(2);
            nodes.put(resultSet.getLong(1),
                new WirtschaftsplanungNode(this, buchungsart, art, zeile));
            nodes.get(resultSet.getLong(1)).setSoll(soll);
          }

          return nodes;
        });

    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sql = "SELECT buchung.buchungsart, sum(buchung.betrag) " +
          "FROM buchung, buchungsart " +
          "WHERE buchung.buchungsart = buchungsart.id " +
          "AND buchung.datum >= ? AND buchung.datum <= ? " +
          "AND buchungsart.art = ? " +
          "AND buchung.buchungsklasse = ? " +
          "GROUP BY buchung.buchungsart";
    }
    else
    {
      sql = "SELECT buchung.buchungsart, sum(buchung.betrag) " +
          "FROM buchung, buchungsart " +
          "WHERE buchung.buchungsart = buchungsart.id " +
          "AND buchung.datum >= ? AND buchung.datum <= ? " +
          "AND buchungsart.art = ? " +
          "AND buchungsart.buchungsklasse = ? " +
          "GROUP BY buchung.buchungsart";

    }

    service.execute(sql, new Object[] { zeile.getVon(), zeile.getBis(), art, buchungsklasse.getID() },
        resultSet -> {
          while (resultSet.next())
          {
            DBIterator<Buchungsart> iterator = service.createList(
                Buchungsart.class);
            Long key = resultSet.getLong(1);
            iterator.addFilter("id = ?", key);
            if (!iterator.hasNext())
            {
              continue;
            }

            Buchungsart buchungsart = iterator.next();
            double ist = resultSet.getDouble(2);

            if (nodes.containsKey(key))
            {
              nodes.get(key).setIst(ist);
            }
            else if (ist != 0)
            {
              nodes.put(key, new WirtschaftsplanungNode(this, buchungsart, art, zeile));
              nodes.get(key).setIst(ist);
            }
          }

          return nodes;
        });

    children = new ArrayList<>(nodes.values());
  }

  public WirtschaftsplanungNode(WirtschaftsplanungNode parent, Buchungsart buchungsart, int art, WirtschaftsplanungZeile zeile)
      throws RemoteException
  {
    type = Type.BUCHUNGSART;
    this.parent = parent;
    this.buchungsart = buchungsart;
    children = new ArrayList<>();
    this.soll = 0;
    ist = 0;

    DBService service = Einstellungen.getDBService();
    String sql = "SELECT wirtschaftsplanitem.posten, wirtschaftsplanitem.soll " +
        "FROM wirtschaftsplanitem, buchungsart " +
        "WHERE wirtschaftsplanitem.buchungsart = buchungsart.id " +
        "AND wirtschaftsplanitem.buchungsart = ? " +
        "AND wirtschaftsplanitem.buchungsklasse = ? " +
        "AND buchungsart.art = ? " +
        "AND wirtschaftsplanitem.wirtschaftsplan = ?";

    service.execute(sql, new Object[] { buchungsart.getID(), parent.getBuchungsklasse().getID(),
        art, zeile.getID() }, resultSet -> {
          while (resultSet.next()) {
            children.add(new WirtschaftsplanungNode(this, resultSet.getString(1), resultSet.getDouble(2)));
          }

          return children;
        });
  }

  public WirtschaftsplanungNode(WirtschaftsplanungNode parent, String posten, double soll)
  {
    type = Type.POSTEN;
    this.parent = parent;
    this.posten = posten;
    this.soll = soll;
    children = null;
  }

  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    if (children != null)
    {
      return PseudoIterator
          .fromArray(children.toArray(new GenericObject[children.size()]));
    }
    return null;
  }

  @Override
  public boolean hasChild(GenericObjectNode genericObjectNode)
      throws RemoteException
  {
    return false;
  }

  @Override
  public GenericObjectNode getParent() throws RemoteException
  {
    return parent;
  }

  @Override
  public GenericIterator getPossibleParents() throws RemoteException
  {
    return null;
  }

  @Override
  public GenericIterator getPath() throws RemoteException
  {
    return null;
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    switch (s) {
      case "buchungsklassebezeichnung":
        if (type == Type.BUCHUNGSKLASSE)
        {
          return buchungsklasse.getBezeichnung();
        }
        return "";
      case "buchungsartbezeichnung_posten":
        if (type == Type.BUCHUNGSART) {
          return buchungsart.getBezeichnung();
        }
        if (type == Type.POSTEN) {
          return posten;
        }
        return "";
      case "soll":
        return soll;
      case "ist":
        if (type == Type.POSTEN) {
          return "";
        }
        return ist;
      default:
        return null;
    }
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"buchungsklassebezeichnung", "buchungsartbezeichnung_posten", "soll", "ist"};
  }

  @Override
  public String getID() throws RemoteException
  {
    return null;
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return null;
  }

  @Override
  public boolean equals(GenericObject genericObject) throws RemoteException
  {
    return false;
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  public Buchungsklasse getBuchungsklasse()
  {
    return buchungsklasse;
  }

  public void setBuchungsklasse(Buchungsklasse buchungsklasse)
  {
    this.buchungsklasse = buchungsklasse;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungsart;
  }

  public void setBuchungsart(Buchungsart buchungsart)
  {
    this.buchungsart = buchungsart;
  }

  public String getPosten()
  {
    return posten;
  }

  public void setPosten(String posten)
  {
    this.posten = posten;
  }

  public double getSoll()
  {
    return soll;
  }

  public void setSoll(double soll)
  {
    this.soll = soll;
  }

  public double getIst()
  {
    return ist;
  }

  public void setIst(double ist)
  {
    this.ist = ist;
  }
}
