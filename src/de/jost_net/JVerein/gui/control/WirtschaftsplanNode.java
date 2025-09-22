/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;

public class WirtschaftsplanNode implements GenericObjectNode
{

  private final String ID = "id";

  private final String SUMME = "summe";

  Type type;

  private Buchungsklasse buchungsklasse;

  private Buchungsart buchungsart;

  private WirtschaftsplanItem wirtschaftsplanItem;

  private double soll;

  private double ist;

  private WirtschaftsplanNode parent;

  private List<WirtschaftsplanNode> children;

  public WirtschaftsplanNode(Buchungsklasse buchungsklasse, int art,
      Wirtschaftsplan wirtschaftsplan) throws RemoteException
  {
    type = Type.BUCHUNGSKLASSE;
    this.buchungsklasse = buchungsklasse;

    Map<String, WirtschaftsplanNode> nodes = new HashMap<>();
    DBService service = Einstellungen.getDBService();

    DBIterator<Buchungsart> buchungsartIterator = service
        .createList(Buchungsart.class);
    buchungsartIterator.addFilter("status != 1"); // Ignoriert inaktive
                                                  // Buchungsarten
    buchungsartIterator.addFilter("buchungsklasse = ?", buchungsklasse.getID());
    buchungsartIterator.addFilter("art = ?", art);
    while (buchungsartIterator.hasNext())
    {
      Buchungsart buchungsart = buchungsartIterator.next();
      nodes.put(buchungsart.getID(),
          new WirtschaftsplanNode(this, buchungsart, art, wirtschaftsplan));
    }

    ExtendedDBIterator<PseudoDBObject> extendedDBIterator = new ExtendedDBIterator<>(
        "wirtschaftsplanitem, buchungsart");
    extendedDBIterator.addColumn("wirtschaftsplanitem.buchungsart as" + ID);
    extendedDBIterator.addColumn("sum(wirtschaftsplanitem.soll) as " + SUMME);
    extendedDBIterator
        .addFilter("wirtschaftsplanitem.buchungsart = buchungsart.id");
    extendedDBIterator.addFilter("buchungsart.art = ?", art);
    extendedDBIterator.addFilter("wirtschaftsplanitem.buchungsklasse = ?",
        buchungsklasse.getID());
    extendedDBIterator.addFilter("wirtschaftsplanitem.wirtschaftsplan = ?",
        wirtschaftsplan.getID());
    extendedDBIterator.addGroupBy("wirtschaftsplanitem.buchungsart");

    while (extendedDBIterator.hasNext())
    {
      PseudoDBObject obj = extendedDBIterator.next();
      DBIterator<Buchungsart> iterator = service.createList(Buchungsart.class);
      iterator.addFilter("id = ?", obj.getAttribute(ID));
      if (!iterator.hasNext())
      {
        continue;
      }

      double soll = obj.getDouble(SUMME);
      String id = (String) obj.getAttribute(ID);
      // Falls die Buchungsklasse vom Standard abweicht, ist noch keine
      // Node vorhanden
      if (!nodes.containsKey(id))
      {
        Buchungsart buchungsart = service.createObject(Buchungsart.class, id);
        nodes.put(buchungsart.getID(),
            new WirtschaftsplanNode(this, buchungsart, art, wirtschaftsplan));
      }
      nodes.get(id).setSoll(soll);
    }

    extendedDBIterator = new ExtendedDBIterator<>("buchung, buchungsart");
    extendedDBIterator.addColumn("buchung.buchungsart as" + ID);
    extendedDBIterator.addColumn("sum(buchung.betrag) as " + SUMME);
    extendedDBIterator.addFilter("buchung.buchungsart = buchungsart.id");
    extendedDBIterator.addFilter("buchung.datum >= ?",
        wirtschaftsplan.getDatumVon());
    extendedDBIterator.addFilter("buchung.datum <= ?",
        wirtschaftsplan.getDatumBis());
    extendedDBIterator.addFilter("buchungsart.art = ?", art);

    if ((boolean) Einstellungen
        .getEinstellung(Einstellungen.Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      extendedDBIterator.addFilter("buchung.buchungsklasse = ?",
          buchungsklasse.getID());
    }
    else
    {
      extendedDBIterator.addFilter("buchungsart.buchungsklasse = ?",
          buchungsklasse.getID());
    }

    extendedDBIterator.addGroupBy("buchung.buchungsart");

    while (extendedDBIterator.hasNext())
    {
      PseudoDBObject obj = extendedDBIterator.next();
      DBIterator<Buchungsart> iterator = service.createList(Buchungsart.class);
      String key = (String) obj.getAttribute(ID);
      iterator.addFilter("id = ?", key);
      if (!iterator.hasNext())
      {
        continue;
      }

      double ist = obj.getDouble(SUMME);
      // Falls die Buchungsklasse vom Standard abweicht, ist noch keine
      // Node vorhanden
      if (!nodes.containsKey(key))
      {
        Buchungsart buchungsart = service.createObject(Buchungsart.class, key);
        nodes.put(buchungsart.getID(),
            new WirtschaftsplanNode(this, buchungsart, art, wirtschaftsplan));
      }
      nodes.get(key).setIst(ist);
    }

    children = new ArrayList<>(nodes.values());
  }

  public WirtschaftsplanNode(WirtschaftsplanNode parent,
      Buchungsart buchungsart, int art, Wirtschaftsplan wirtschaftsplan)
      throws RemoteException
  {
    type = Type.BUCHUNGSART;
    this.parent = parent;
    this.buchungsart = buchungsart;
    children = new ArrayList<>();

    DBService service = Einstellungen.getDBService();

    if (wirtschaftsplan.isNewObject())
    {
      WirtschaftsplanItem item = service.createObject(WirtschaftsplanItem.class,
          null);
      item.setBuchungsklasseId(parent.getBuchungsklasse().getID());
      item.setBuchungsartId(buchungsart.getID());
      item.setPosten(buchungsart.getBezeichnung());
      item.setSoll(0);

      children.add(new WirtschaftsplanNode(this, item));
      return;
    }

    DBIterator<WirtschaftsplanItem> iterator = service
        .createList(WirtschaftsplanItem.class);
    iterator.join("buchungsart");
    iterator.addFilter("wirtschaftsplanitem.buchungsart = buchungsart.id");
    iterator.addFilter("wirtschaftsplanitem.buchungsart = ?",
        buchungsart.getID());
    iterator.addFilter("wirtschaftsplanitem.buchungsklasse = ?",
        parent.getBuchungsklasse().getID());
    iterator.addFilter("buchungsart.art = ?", art);
    iterator.addFilter("wirtschaftsplanitem.wirtschaftsplan = ?",
        wirtschaftsplan.getID());

    while (iterator.hasNext())
    {
      WirtschaftsplanItem item = iterator.next();
      children.add(new WirtschaftsplanNode(this, item));
    }
  }

  public WirtschaftsplanNode(WirtschaftsplanNode parent,
      WirtschaftsplanItem wirtschaftsplanItem) throws RemoteException
  {
    type = Type.POSTEN;
    this.parent = parent;
    this.wirtschaftsplanItem = wirtschaftsplanItem;
    this.soll = wirtschaftsplanItem.getSoll();
    children = null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    if (children != null)
    {
      return PseudoIterator.fromArray(children.toArray(new GenericObject[0]));
    }
    return null;
  }

  @Override
  public boolean hasChild(GenericObjectNode genericObjectNode)
      throws RemoteException
  {
    if (!(genericObjectNode instanceof WirtschaftsplanNode))
    {
      return false;
    }
    return children.contains(genericObjectNode);
  }

  public void addChild(WirtschaftsplanNode child)
  {
    children.add(child);
  }

  public void removeChild(WirtschaftsplanNode node)
  {
    children.remove(node);
  }

  @Override
  public GenericObjectNode getParent() throws RemoteException
  {
    return parent;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public GenericIterator getPossibleParents() throws RemoteException
  {
    return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public GenericIterator getPath() throws RemoteException
  {
    return null;
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    switch (s)
    {
      case "buchungsklassebezeichnung":
        if (type == Type.BUCHUNGSKLASSE)
        {
          return buchungsklasse.getBezeichnung();
        }
        return "";
      case "buchungsartbezeichnung_posten":
        if (type == Type.BUCHUNGSART)
        {
          return buchungsart.getBezeichnung();
        }
        if (type == Type.POSTEN)
        {
          return wirtschaftsplanItem.getPosten();
        }
        return "";
      case "soll":
        return soll;
      case "ist":
        if (type == Type.POSTEN)
        {
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
    return new String[] { "buchungsklassebezeichnung",
        "buchungsartbezeichnung_posten", "soll", "ist" };
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

  public WirtschaftsplanItem getWirtschaftsplanItem()
  {
    return wirtschaftsplanItem;
  }

  public void setWirtschaftsplanItem(WirtschaftsplanItem wirtschaftsplanItem)
  {
    this.wirtschaftsplanItem = wirtschaftsplanItem;
  }

  public boolean hasLeaf()
  {
    if (type == Type.POSTEN)
    {
      return true;
    }

    return children.stream().anyMatch(WirtschaftsplanNode::hasLeaf);
  }

  public int anzahlLeafs()
  {
    if (type == Type.POSTEN)
    {
      return 1;
    }

    if (children.isEmpty())
    {
      return 0;
    }

    return children.stream().mapToInt(WirtschaftsplanNode::anzahlLeafs).sum();
  }

  public enum Type
  {
    BUCHUNGSKLASSE,
    BUCHUNGSART,
    POSTEN,
    UNBEKANNT
  }
}
