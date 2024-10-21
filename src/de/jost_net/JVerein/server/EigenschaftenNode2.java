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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;

public class EigenschaftenNode2 implements GenericObjectNode
{

  private EigenschaftenNode2 parent = null;

  private Mitglied mitglied = null;

  private EigenschaftGruppe eigenschaftgruppe = null;

  private Eigenschaft eigenschaft = null;

  private Eigenschaften eigenschaften = null;

  private ArrayList<GenericObjectNode> childrens;

  private String preset = UNCHECKED;

  public static final int NONE = 0;

  public static final int ROOT = 1;

  public static final int EIGENSCHAFTGRUPPE = 2;

  public static final int EIGENSCHAFTEN = 3;

  private int nodetype = NONE;

  private Map<String, String> eigenschaftids = new HashMap<>();
  
  public static final String UNCHECKED = "0";

  public static final String PLUS = "1";

  public static final String MINUS = "2";

  public EigenschaftenNode2(Mitglied mitglied) throws RemoteException
  {
    this(mitglied, "", false);
  }

  public EigenschaftenNode2(String vorbelegung, boolean ohnePflicht)
      throws RemoteException
  {
    this(null, vorbelegung, ohnePflicht);
  }

  private EigenschaftenNode2(Mitglied mitglied, String vorbelegung,
      boolean ohnePflicht) throws RemoteException
  {
    this.mitglied = mitglied;
    StringTokenizer stt = new StringTokenizer(vorbelegung, ",");
    while (stt.hasMoreElements())
    {
      String s = stt.nextToken();
      eigenschaftids.put(s.substring(0,s.length()-1), s.substring(s.length()-1));
    }
    childrens = new ArrayList<>();
    nodetype = ROOT;
    DBIterator<EigenschaftGruppe> it = Einstellungen.getDBService()
        .createList(EigenschaftGruppe.class);
    if (ohnePflicht)
    {
      it.addFilter(
          "(PFLICHT <> true OR PFLICHT IS NULL) AND (MAX1 <> true OR MAX1 IS NULL)");
    }
    it.setOrder("order by bezeichnung");
    while (it.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) it.next();
      childrens.add(new EigenschaftenNode2(this, mitglied, eg, eigenschaftids));
    }
  }

  private EigenschaftenNode2(EigenschaftenNode2 parent, Mitglied mitglied,
      EigenschaftGruppe eg, Map<String, String> eigenschaftsids)
      throws RemoteException
  {
    this.parent = parent;
    this.mitglied = mitglied;
    childrens = new ArrayList<>();
    this.eigenschaftgruppe = eg;
    nodetype = EIGENSCHAFTGRUPPE;
    DBIterator<Eigenschaft> it = Einstellungen.getDBService()
        .createList(Eigenschaft.class);
    it.addFilter("eigenschaftgruppe = ?",
        new Object[] { eigenschaftgruppe.getID() });
    it.setOrder("order by bezeichnung");
    while (it.hasNext())
    {
      Eigenschaft eigenschaft = (Eigenschaft) it.next();
      Eigenschaften eigenschaften = null;
      if (mitglied != null)
      {
        DBIterator<Eigenschaften> it2 = Einstellungen.getDBService()
            .createList(Eigenschaften.class);
        it2.addFilter("mitglied = ? AND eigenschaft = ?",
            new Object[] { mitglied.getID(), eigenschaft.getID() });
        if (it2.hasNext())
        {
          eigenschaften = (Eigenschaften) it2.next();
        }
      }
      childrens.add(new EigenschaftenNode2(this, mitglied, eigenschaft,
          eigenschaften, eigenschaftsids));
    }
  }

  private EigenschaftenNode2(EigenschaftenNode2 parent, Mitglied mitglied,
      Eigenschaft eigenschaft, Eigenschaften eigenschaften,
      Map<String, String> eigenschaftids) throws RemoteException
  {
    this.parent = parent;
    nodetype = EIGENSCHAFTEN;
    this.mitglied = mitglied;
    this.eigenschaft = eigenschaft;
    this.eigenschaften = eigenschaften;
    String eigenschaftenKey = this.eigenschaft.getID();
    if (eigenschaftids.containsKey(eigenschaftenKey))
    {
      preset = eigenschaftids.get(eigenschaftenKey);
    }
  }

  @Override
  @SuppressWarnings("rawtypes")
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
  public EigenschaftenNode2 getParent()
  {
    return parent;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public GenericIterator getPath()
  {
    return null;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public GenericIterator getPossibleParents()
  {
    return null;
  }

  @Override
  public boolean hasChild(GenericObjectNode object)
  {
    return childrens.size() > 0;
  }

  @Override
  public boolean equals(GenericObject other)
  {
    return false;
  }

  @Override
  public Object getAttribute(String name) throws RemoteException
  {
    switch (nodetype)
    {
      case ROOT:
      {
        return "Eigenschaften";
      }
      case EIGENSCHAFTGRUPPE:
      {
        return eigenschaftgruppe.getBezeichnung();
      }
      case EIGENSCHAFTEN:
      {
        return eigenschaft.getBezeichnung();
      }
    }
    return null;
  }

  @Override
  public String[] getAttributeNames()
  {
    return null;
  }

  @Override
  public String getID()
  {
    return null;
  }

  @Override
  public String getPrimaryAttribute()
  {
    return null;
  }

  public Object getObject()
  {
    switch (nodetype)
    {
      case ROOT:
      {
        return mitglied;
      }
      case EIGENSCHAFTGRUPPE:
      {
        return eigenschaftgruppe;
      }
      case EIGENSCHAFTEN:
      {
        return eigenschaft;
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

  public Eigenschaft getEigenschaft()
  {
    return this.eigenschaft;
  }

  public Eigenschaften getEigenschaften()
  {
    return this.eigenschaften;
  }

  public String getPreset()
  {
    return preset;
  }
  
  public void incPreset()
  {
    switch (preset)
    {
      case EigenschaftenNode2.UNCHECKED:
        preset = EigenschaftenNode2.PLUS;
        break;
      case EigenschaftenNode2.PLUS:
        preset = EigenschaftenNode2.MINUS;
        break;
      case EigenschaftenNode2.MINUS:
        preset = EigenschaftenNode2.UNCHECKED;
        break;
    }
  }
}
