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
import java.util.ArrayList;
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

  public RechnungNode(FilterControl control) throws RemoteException
  {
    childrens = new ArrayList<>();
    nodetype = ROOT;

    Map<String, ArrayList<Mitgliedskonto>> mitgliedskontoMap = new HashMap<>();
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

    if (control.isDifferenzAktiv()
        && control.getDifferenz().getValue() != DIFFERENZ.EGAL)
    {
      String sql = "SELECT mitgliedskonto.id, mitgliedskonto.betrag, "
          + "sum(buchung.betrag) FROM mitgliedskonto "
          + "LEFT JOIN buchung on mitgliedskonto.id = buchung.mitgliedskonto "
          + "group by mitgliedskonto.id ";
      if (control.getDifferenz().getValue() == DIFFERENZ.FEHLBETRAG)
      {
        sql += "having sum(buchung.betrag) < mitgliedskonto.betrag or "
            + "sum(buchung.betrag) is null and mitgliedskonto.betrag > 0 ";
      }
      else
      {
        sql += "having sum(buchung.betrag) > mitgliedskonto.betrag ";
      }

      @SuppressWarnings("unchecked")
      ArrayList<String> diffIds = (ArrayList<String>) Einstellungen
          .getDBService().execute(sql, null, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              ArrayList<String> list = new ArrayList<>();
              while (rs.next())
              {
                list.add(rs.getString(1));
              }
              return list;
            }
          });
      if (diffIds.size() == 0)
        return;
      mitgliedskontoIterator
          .addFilter("id in (" + String.join(",", diffIds) + ")");
    }

    while (mitgliedskontoIterator.hasNext())
    {
      Mitgliedskonto mitgliedskonto = mitgliedskontoIterator.next();
      ArrayList<Mitgliedskonto> list = mitgliedskontoMap
          .get(mitgliedskonto.getMitgliedId());
      if (list == null)
      {
        list = new ArrayList<Mitgliedskonto>();
        list.add(mitgliedskonto);
        mitgliedskontoMap.put(mitgliedskonto.getMitgliedId(), list);
      }
      else
      {
        list.add(mitgliedskonto);
        mitgliedskontoMap.replace(mitgliedskonto.getMitgliedId(), list);
      }
    }

    DBIterator<Mitglied> mitgliedterator = Einstellungen.getDBService()
        .createList(Mitglied.class);
    if (control.isSuchnameAktiv()
        && !((String) control.getSuchname().getValue()).isEmpty())
    {
      mitgliedterator.addFilter(
          " (upper(name) like upper(?) or upper(vorname) like upper(?)) ",
          new Object[] { control.getSuchname().getValue(),
              control.getSuchname().getValue() });
    }

    while (mitgliedterator.hasNext())
    {
      Mitglied m = mitgliedterator.next();
      if (mitgliedskontoMap.get(m.getID()) == null)
        continue;
      childrens.add(new RechnungNode(mitgliedskontoMap.get(m.getID()), m));
    }
  }

  private RechnungNode(ArrayList<Mitgliedskonto> mitgliedskontoList,
      Mitglied mitglied) throws RemoteException
  {
    this.mitglied = mitglied;

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
        return "Rechnungen";
      }
      case MITGLIED:
      {
        @SuppressWarnings("rawtypes")
        GenericIterator it1 = getChildren();
        double betrag = 0.0;
        while (it1.hasNext())
        {
          RechnungNode rn = (RechnungNode) it1.next();
          if (rn.getNodeType() == BUCHUNG)
          {
            betrag += rn.getBuchung().getBetrag();
          }
        }
        return Adressaufbereitung.getNameVorname(mitglied) + " ("
            + Einstellungen.DECIMALFORMAT.format(betrag) + ")";
      }
      case BUCHUNG:
      {
        return new JVDateFormatTTMMJJJJ().format(buchung.getDatum()) + ", "
            + (buchung.getZweck1() != null && buchung.getZweck1().length() > 0
                ? buchung.getZweck1()
                : "")
            + ", " + Einstellungen.DECIMALFORMAT.format(buchung.getBetrag());
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
    return null;
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
