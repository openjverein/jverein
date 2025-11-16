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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.Kontoart;
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

public class WirtschaftsplanNode
    implements GenericObjectNode, Comparable<WirtschaftsplanNode>
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
    this(buchungsklasse, art, wirtschaftsplan, (Boolean) Einstellungen
        .getEinstellung(Property.VERBINDLICHKEITEN_FORDERUNGEN));
  }

  public WirtschaftsplanNode(Buchungsklasse buchungsklasse, int art,
      Wirtschaftsplan wirtschaftsplan, boolean mitVerbindlichkeitenForderungen)
      throws RemoteException
  {
    type = Type.BUCHUNGSKLASSE;
    this.buchungsklasse = buchungsklasse;

    final boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    final boolean mitSteuer = (Boolean) Einstellungen
        .getEinstellung(Property.OPTIERTPFLICHT);

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
    extendedDBIterator.addColumn("wirtschaftsplanitem.buchungsart as " + ID);
    extendedDBIterator.addColumn("sum(wirtschaftsplanitem.soll) as " + SUMME);
    extendedDBIterator
        .addFilter("wirtschaftsplanitem.buchungsart = buchungsart.id");
    extendedDBIterator.addFilter("buchungsart.art = ?", art);
    extendedDBIterator.addFilter("wirtschaftsplanitem.buchungsklasse = ?",
        buchungsklasse.getID());
    extendedDBIterator.addFilter("wirtschaftsplanitem.wirtschaftsplan = ?",
        wirtschaftsplan.getID());
    extendedDBIterator.addGroupBy("wirtschaftsplanitem.buchungsart");

    double sollSumme = 0d;
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
      String id = obj.getAttribute(ID).toString();
      // Falls die Buchungsklasse vom Standard abweicht, ist noch keine
      // Node vorhanden
      if (!nodes.containsKey(id))
      {
        Buchungsart buchungsart = service.createObject(Buchungsart.class, id);
        nodes.put(buchungsart.getID(),
            new WirtschaftsplanNode(this, buchungsart, art, wirtschaftsplan));
      }
      nodes.get(id).setSoll(soll);
      sollSumme += soll;
    }
    setSoll(sollSumme);

    ExtendedDBIterator<PseudoDBObject> istIt = new ExtendedDBIterator<>(
        "buchungsart");
    istIt.leftJoin("buchung",
        "buchung.buchungsart = buchungsart.id and buchung.datum >= ? and buchung.datum <= ?",
        wirtschaftsplan.getDatumVon(), wirtschaftsplan.getDatumBis());
    if (mitVerbindlichkeitenForderungen)
    {
      istIt.leftJoin("konto",
          "buchung.konto = konto.id AND (konto.kontoart < ?  OR konto.kontoart > ?)",
          Kontoart.LIMIT.getKey(), Kontoart.LIMIT_RUECKLAGE.getKey());
    }
    else
    {
      istIt.leftJoin("konto", "buchung.konto = konto.id AND konto.kontoart < ?",
          Kontoart.LIMIT.getKey());
    }

    istIt.addColumn("buchungsart.id as " + ID);
    istIt.addColumn("COUNT(buchung.id) as anzahl");
    istIt.addFilter("buchungsart.art = ?", art);
    // Wenn Kontoarten ausgefiltert werden, ist konto.id=null, diese müssen wir
    // raussortieren. Allerdings nur, wenn buchung.konto nicht null ist, sonst
    // handelt es sich nämlich um berechnete Steuerbuchungen
    istIt.addFilter("!(buchung.konto is NULL xor konto.id is NULL)");

    if (mitSteuer)
    {
      // Nettobetrag berechnen und steuerbetrag der Steuerbuchungsart
      // hinzurechnen
      istIt.addColumn("COALESCE(SUM(CAST(buchung.betrag * 100 / (100 + "
          // Anlagenkonto immer Bruttobeträge.
          // Alte Steuerbuchungen mit dependencyid lassen wir bestehen ohne
          // Netto zu berehnen.
          + "CASE WHEN konto.kontoart = ? OR buchung.dependencyid > -1 THEN 0 ELSE COALESCE(steuer.satz,0) END"
          + ") AS DECIMAL(10,2))),0) + COALESCE(SUM(st.steuerbetrag),0) AS "
          + SUMME, Kontoart.ANLAGE.getKey());

      // Für die Steuerbträge auf der Steuerbuchungsart machen wir ein Subselect
      String subselect = "(SELECT steuer.buchungsart, steuer.buchungsklasse,"
          + " SUM(CAST(buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2))) AS steuerbetrag "
          + " FROM buchung"
          // Keine Steuer bei Anlagekonten
          + " JOIN konto on buchung.konto = konto.id and konto.kontoart < ? and konto.kontoart != ?";

      // Wenn die Steuer in der Buchung steht, können wir sie direkt nehmen,
      // sonst müssen wir den Umweg über die Buchungsart nehmen.
      if (steuerInBuchung)
      {
        subselect += " JOIN steuer ON steuer.id = buchung.steuer ";
      }
      else
      {
        subselect += " JOIN buchungsart ON buchung.buchungsart = buchungsart.id "
            + " JOIN steuer ON steuer.id = buchungsart.steuer ";
      }
      subselect += " WHERE datum >= ? and datum <= ? "
          // Keine Steuer bei alten Steuerbuchungen mit dependencyid
          + " AND (buchung.dependencyid is null or  buchung.dependencyid = -1)"
          + " GROUP BY steuer.buchungsart";
      if ((boolean) Einstellungen
          .getEinstellung(Einstellungen.Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        subselect += ",steuer.buchungsklasse";
      }
      subselect += ") AS st ";
      istIt.leftJoin(subselect, "st.buchungsart = buchungsart.id ",
          Kontoart.LIMIT.getKey(), Kontoart.ANLAGE.getKey(),
          wirtschaftsplan.getDatumVon(), wirtschaftsplan.getDatumBis());

      if (steuerInBuchung)
      {
        istIt.leftJoin("steuer", "steuer.id = buchung.steuer");
      }
      else
      {
        istIt.leftJoin("steuer", "steuer.id = buchungsart.steuer");
      }
    }
    else
    {
      istIt.addColumn("COALESCE(SUM(buchung.betrag),0) AS " + SUMME);
    }

    if ((boolean) Einstellungen
        .getEinstellung(Einstellungen.Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      istIt.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchung.buchungsklasse"
              + (mitSteuer ? " OR buchungsklasse.id = st.buchungsklasse" : ""));
      istIt.addGroupBy("buchungsklasse.id");
      istIt.addFilter("buchungsklasse.id = ?", buchungsklasse.getID());
    }
    else
    {
      istIt.addFilter("buchungsart.buchungsklasse = ?", buchungsklasse.getID());
    }

    istIt.addGroupBy("buchungsart.id");
    istIt.addHaving("anzahl > 0 OR abs(" + SUMME + ") >= 0.01");

    double istSumme = 0d;
    while (istIt.hasNext())
    {
      PseudoDBObject obj = istIt.next();
      DBIterator<Buchungsart> iterator = service.createList(Buchungsart.class);
      if (obj.getAttribute(ID) == null)
      {
        continue;
      }
      String key = obj.getAttribute(ID).toString();
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
      istSumme += ist;
    }

    children = new ArrayList<>(nodes.values());
    setIst(istSumme);
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

      setWirtschaftsplanItem(item);
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
    double sollSumme = 0d;
    while (iterator.hasNext())
    {
      WirtschaftsplanItem item = iterator.next();
      sollSumme += item.getSoll();
      children.add(new WirtschaftsplanNode(this, item));
      setWirtschaftsplanItem(item);
    }
    setSoll(sollSumme);
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
      children.sort(null);
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
          return new BuchungsklasseFormatter().format(buchungsklasse);
        }
        if (type == Type.BUCHUNGSART)
        {
          return new BuchungsartFormatter().format(buchungsart);
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
  public int compareTo(WirtschaftsplanNode o)
  {
    try
    {
      if (type == Type.POSTEN)
      {
        return this.wirtschaftsplanItem.getPosten()
            .compareTo(o.wirtschaftsplanItem.getPosten());
      }
      switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
      {
        case BuchungsartSort.NACH_NUMMER:
          if (type == Type.BUCHUNGSART)
          {
            return this.getBuchungsart().getNummer()
                - o.getBuchungsart().getNummer();
          }
          break;
        case BuchungsartSort.NACH_BEZEICHNUNG:
        default:
          if (type == Type.BUCHUNGSART)
          {
            return this.getBuchungsart().getBezeichnung()
                .compareTo(o.getBuchungsart().getBezeichnung());
          }
      }
      return 0;
    }
    catch (RemoteException e)
    {
      return 0;
    }
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "buchungsklassebezeichnung", "soll", "ist" };
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
