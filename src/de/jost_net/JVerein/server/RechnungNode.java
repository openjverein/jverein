/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl.DIFFERENZ;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;

public class RechnungNode implements GenericObjectNode
{
  private RechnungNode parent = null;

  private Mitglied mitglied = null;

  private Mitgliedskonto buchung = null;

  private ArrayList<GenericObjectNode> childrens;

  private boolean checked;

  public static final int NONE = 0;

  public static final int ROOT = 1;

  public static final int MITGLIED = 2;

  public static final int BUCHUNG = 3;

  private int nodetype = NONE;

  private Zahlungsweg zahlungsweg;

  @SuppressWarnings("unchecked")
  public RechnungNode(FilterControl control) throws RemoteException
  {
    childrens = new ArrayList<>();
    nodetype = ROOT;

    // Map mit Sollbuchungen im Zeitraum nach Zahlungsweg und MitgliedsID
    Map<Zahlungsweg, Map<String, ArrayList<Mitgliedskonto>>> mitgliedskontoMap = new HashMap<>();
    DBIterator<Mitgliedskonto> mitgliedskontoIterator = Einstellungen
        .getDBService().createList(Mitgliedskonto.class);

    mitgliedskontoIterator.addFilter("rechnung is null");
    if (control.getDatumvon().getValue() != null)
      mitgliedskontoIterator.addFilter("datum >= ? ",
          control.getDatumvon().getValue());
    if (control.getDatumbis().getValue() != null)
      mitgliedskontoIterator.addFilter("datum <= ?",
          control.getDatumbis().getValue());
    if ((Boolean) control.getOhneAbbucher().getValue())
      mitgliedskontoIterator.addFilter("zahlungsweg != ? ",
          Zahlungsweg.BASISLASTSCHRIFT);

    while (mitgliedskontoIterator.hasNext())
    {
      Mitgliedskonto mitgliedskonto = mitgliedskontoIterator.next();
      Map<String, ArrayList<Mitgliedskonto>> map = mitgliedskontoMap
          .get(new Zahlungsweg(mitgliedskonto.getZahlungsweg()));
      if (map == null)
      {
        map = new HashMap<>();
      }
      ArrayList<Mitgliedskonto> list = map.get(mitgliedskonto.getMitgliedId());
      if (list == null)
      {
        list = new ArrayList<Mitgliedskonto>();
        list.add(mitgliedskonto);
        map.put(mitgliedskonto.getMitgliedId(), list);
      }
      else
      {
        list.add(mitgliedskonto);
        map.replace(mitgliedskonto.getMitgliedId(), list);
      }
      if (mitgliedskontoMap
          .get(new Zahlungsweg(mitgliedskonto.getZahlungsweg())) == null)
      {
        mitgliedskontoMap.put(new Zahlungsweg(mitgliedskonto.getZahlungsweg()),
            map);
      }
      else
      {
        mitgliedskontoMap
            .replace(new Zahlungsweg(mitgliedskonto.getZahlungsweg()), map);
      }
    }

    // Map der Mitglieder mit Differenz nach Zahlungsweg
    Map<Zahlungsweg, ArrayList<String>> diffIdsMap = null;
    if (control.isDifferenzAktiv()
        && control.getDifferenz().getValue() != DIFFERENZ.EGAL)
    {
      String sql = "SELECT mitgliedskonto.mitglied, mitgliedskonto.zahlungsweg,"
          + " sum(mitgliedskonto.betrag), "
          + "sum(buchung.betrag) FROM mitgliedskonto "
          + "LEFT JOIN buchung on mitgliedskonto.id = buchung.mitgliedskonto "
          + "WHERE 1 = 1 ";

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      if (control.getDatumvon().getValue() != null)
        sql += " AND mitgliedskonto.datum >= '"
            + format.format((Date) control.getDatumvon().getValue()) + "'";
      if (control.getDatumbis().getValue() != null)
        sql += " AND mitgliedskonto.datum <= '"
            + format.format((Date) control.getDatumbis().getValue()) + "'";

      sql += " group by mitgliedskonto.mitglied, mitgliedskonto.zahlungsweg";

      if (control.getDifferenz().getValue() == DIFFERENZ.FEHLBETRAG)
      {
        sql += " having sum(buchung.betrag) < sum(mitgliedskonto.betrag)"
            + " OR (sum(buchung.betrag) IS null and sum(mitgliedskonto.betrag) > 0)";
      }
      else
      {
        sql += " having sum(buchung.betrag) > sum(mitgliedskonto.betrag)"
            + " OR (sum(buchung.betrag) IS null and sum(mitgliedskonto.betrag) < 0)";
      }

      diffIdsMap = (Map<Zahlungsweg, ArrayList<String>>) Einstellungen
          .getDBService().execute(sql, null, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              Map<Zahlungsweg, ArrayList<String>> map = new HashMap<>();
              while (rs.next())
              {
                Zahlungsweg z = new Zahlungsweg((Integer) rs.getObject(2));
                ArrayList<String> list = map.get(z);
                if (list == null)
                  list = new ArrayList<String>();
                list.add(rs.getString(1));
                if (map.get(z) == null)
                {
                  map.put(z, list);
                }
                else
                {
                  map.replace(z, list);
                }
              }
              return map;
            }
          });
    }

    for (Zahlungsweg weg : Zahlungsweg.getArray(false))
    {
      if (mitgliedskontoMap.get(weg) == null)
        continue;
      DBIterator<Mitglied> mitgliedIterator = Einstellungen.getDBService()
          .createList(Mitglied.class);
      if (control.isSuchnameAktiv()
          && !((String) control.getSuchname().getValue()).isEmpty())
      {
        mitgliedIterator.addFilter(
            " (upper(name) like upper(?) or upper(vorname) like upper(?)) ",
            new Object[] { "%" +control.getSuchname().getValue() + "%",
                "%" + control.getSuchname().getValue() + "%" });
      }
      // Bei Differenz ids Filtern
      if (diffIdsMap != null)
      {
        if (diffIdsMap.get(weg) == null)
          continue;
        ArrayList<String> diffIds = diffIdsMap.get(weg);
        if (diffIds.size() == 0)
          continue;
        mitgliedIterator.addFilter("id in (" + String.join(",", diffIds) + ")");
      }
      while (mitgliedIterator.hasNext())
      {
        Mitglied m = mitgliedIterator.next();
        if (mitgliedskontoMap.get(weg).get(m.getID()) == null)
          continue;
        childrens.add(new RechnungNode(weg,
            mitgliedskontoMap.get(weg).get(m.getID()), m));
      }
    }
  }

  private RechnungNode(Zahlungsweg weg,
      ArrayList<Mitgliedskonto> mitgliedskontoList, Mitglied mitglied)
      throws RemoteException
  {
    this.mitglied = mitglied;
    this.zahlungsweg = weg;

    childrens = new ArrayList<>();
    nodetype = MITGLIED;

    if (mitgliedskontoList == null)
      return;

    for (Mitgliedskonto mk : mitgliedskontoList)
    {
      childrens.add(new RechnungNode(mitglied, mk));
    }
  }

  private RechnungNode(Mitglied mitglied, Mitgliedskonto buchung)
  {
    this.mitglied = mitglied;
    this.buchung = buchung;

    childrens = new ArrayList<>();
    nodetype = BUCHUNG;
  }

  @Override
  public Object getAttribute(String name) throws RemoteException
  {
    switch (nodetype)
    {
      case ROOT:
      {
        switch (name)
        {
          case "name":
            return "Rechnungen";
          default:
            return null;
        }
      }
      case MITGLIED:
      {
        @SuppressWarnings("rawtypes")
        GenericIterator it1 = getChildren();
        double soll = 0.0;
        double ist = 0.0;
        while (it1.hasNext())
        {
          RechnungNode rn = (RechnungNode) it1.next();
          if (rn.getNodeType() == BUCHUNG)
          {
            soll += rn.getBuchung().getBetrag();
            ist += rn.getBuchung().getIstSumme();
          }
        }
        switch (name)
        {
          case "name":
            return Adressaufbereitung.getNameVorname(mitglied);
          case "zahlungsweg":
            return zahlungsweg.getText();
          case "soll":
            return soll;
          case "ist":
            return ist;
          case "differenz":
            return soll - ist;
          default:
            return null;
        }
      }
      case BUCHUNG:
      {
        switch (name)
        {
          case "name":
            return new JVDateFormatTTMMJJJJ().format(buchung.getDatum()) + ", "
                + (buchung.getZweck1() != null
                    && buchung.getZweck1().length() > 0 ? buchung.getZweck1()
                        : "");
          case "soll":
            return buchung.getBetrag();
          case "ist":
            return buchung.getIstSumme();
          case "differenz":
            return buchung.getBetrag() - buchung.getIstSumme();
          default:
            return null;
        }
      }
    }
    return "bla";
  }

  public Object getObject()
  {
    switch (nodetype)
    {
      case MITGLIED:
      {
        return mitglied;
      }
      case BUCHUNG:
      {
        return buchung;
      }
    }
    return null;
  }

  public int getNodeType()
  {
    return nodetype;
  }

  public Mitglied getMitglied()
  {
    return this.mitglied;
  }

  public Mitgliedskonto getBuchung()
  {
    return this.buchung;
  }

  public void setChecked(boolean checked)
  {
    this.checked = checked;
  }

  public boolean isChecked()
  {
    return checked;
  }

  @Override
  public String toString()
  {
    String ret = "";
    try
    {
      if (this.nodetype == ROOT)
      {
        return "--> ROOT";
      }
      if (this.nodetype == MITGLIED)
      {
        return "---> MITGLIED: " + Adressaufbereitung.getNameVorname(mitglied);
      }
      if (this.nodetype == BUCHUNG)
      {
        return "----> BUCHUNG: " + buchung.getDatum() + ";"
            + buchung.getZweck1() + ";" + buchung.getBetrag();
      }
    }
    catch (RemoteException e)
    {
      ret = e.getMessage();
    }
    return ret;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public GenericIterator getChildren() throws RemoteException
  {
    if (childrens == null)
    {
      return null;
    }
    return PseudoIterator
        .fromArray(childrens.toArray(new GenericObject[childrens.size()]));
  }

  public boolean removeChild(GenericObjectNode child)
  {
    return childrens.remove(child);
  }

  @Override
  public boolean hasChild(GenericObjectNode object) throws RemoteException
  {
    return childrens.size() > 0;
  }

  @Override
  public RechnungNode getParent() throws RemoteException
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
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "name", "zahlungsweg", "soll", "ist", "differenz" };
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
  public boolean equals(GenericObject other) throws RemoteException
  {
    return false;
  }
}
