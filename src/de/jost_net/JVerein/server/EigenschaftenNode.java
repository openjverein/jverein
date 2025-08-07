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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;

public class EigenschaftenNode implements GenericObjectNode
{

  private EigenschaftenNode parent = null;

  private Mitglied mitglied = null;

  private EigenschaftGruppe eigenschaftgruppe = null;

  private Eigenschaft eigenschaft = null;

  private ArrayList<GenericObjectNode> childrens;

  // Node Typen
  public static final int NONE = 0;

  public static final int ROOT = 1;

  public static final int EIGENSCHAFTGRUPPE = 2;

  public static final int EIGENSCHAFTEN = 3;

  private int nodetype = NONE;

  // Preset und Icon Definition
  public static final String UNCHECKED = "0";

  public static final String PLUS = "1";

  public static final String MINUS = "2";

  public static final String CHECKED = "3";

  public static final String CHECKED_PARTLY = "4";

  private String preset = UNCHECKED; // Gesetzter Status/Icon

  private String base = UNCHECKED; // Wert im Tree nach CHECKED bzw. MINUS

  private boolean onlyChecked = false; // Nur CHECKED, kein PLUS, MINUS etc.

  private Map<String, Config> config = new HashMap<>();

  private List<Long[]> eigenschaften = null;

  public EigenschaftenNode(Mitglied mitglied) throws RemoteException
  {
    this(mitglied, "", true, null);
  }

  public EigenschaftenNode(String vorbelegung, boolean onlyChecked,
      Mitglied[] mitglieder) throws RemoteException
  {
    this(null, vorbelegung, onlyChecked, mitglieder);
  }

  private EigenschaftenNode(Mitglied mitglied, String vorbelegung,
      boolean onlyChecked, Mitglied[] mitglieder) throws RemoteException
  {
    this.onlyChecked = onlyChecked;
    nodetype = ROOT;
    if (!vorbelegung.isEmpty())
    {
      // Aufruf aus (Nicht-)Mitglied Filter Dialog oder Auswertungen
      // (Nicht-)Mitglied,
      // Werte (PLUS oder MINUS) aus Settings lesen
      StringTokenizer stt = new StringTokenizer(vorbelegung, ",");
      while (stt.hasMoreElements())
      {
        String s = stt.nextToken();
        config.put(s.substring(0, s.length() - 1), // substring ist
                                                   // Eigenschaft.Id
            // letztes Zeichen PLUS oder MINUS
            new Config(s.substring(s.length() - 1),
                EigenschaftenNode.UNCHECKED));
      }
    }
    else if (mitglied != null)
    {
      // Aufruf aus (Nicht-)Mitglied Detail View Lasche Eigenschaften
      // Gesetzte Eigenschaften (CHECKED) aus Datenbank lesen
      this.mitglied = mitglied;
      List<Long[]> eigenschaften = getEigenschaften(); // [Mitglied.Id,
                                                       // Eigenschaft.Id]
      for (Long[] value : eigenschaften)
      {
        if (value[0].toString().equals(mitglied.getID()))
        {
          config.put(value[1].toString(), new Config(EigenschaftenNode.CHECKED,
              EigenschaftenNode.UNCHECKED));
        }
      }
    }
    else if (mitglieder != null)
    {
      // Aufruf aus Mitglied Kontext Menü -> Eigenschaften
      // Gesetzte Eigenschaften (CHECKED) aus Datenbank lesen
      Map<Long, Long> counters = new HashMap<>(); // <Eigenschaft.Id, Anzahl>
      Long counter = null;
      List<Long[]> eigenschaften = getEigenschaften(); // [Mitglied.Id,
                                                       // Eigenschaft.Id]
      for (Long[] value : eigenschaften)
      {
        counter = counters.get(value[1]);
        for (Mitglied m : mitglieder)
        {
          if (value[0].toString().equals(m.getID()))
          {
            if (counter == null)
              // Erster Eintrag für Eigenschaft gefunden
              counters.put(value[1], 1l);
            else
              // Weiterer Eintrag für Eigenschaft gefunden, Anzahl
              // inkrementieren
              // Neuer Eintrag überschreibt alten Eintrag
              counters.put(value[1], ++counter);
          }
        }
      }
      for (Long key : counters.keySet())
      {
        if (counters.get(key) == mitglieder.length)
        {
          // Bei allen Mitgliedern ist die Eigenschaft gesetzt
          config.put(key.toString(),
              new Config(EigenschaftenNode.CHECKED, EigenschaftenNode.CHECKED));
        }
        else if (counters.get(key) != 0)
        {
          // Bei mindesten einem Mitglied ist die Eigenschaft gesetzt
          config.put(key.toString(),
              new Config(EigenschaftenNode.CHECKED_PARTLY,
                  EigenschaftenNode.CHECKED_PARTLY));
        }
      }
    }
    childrens = new ArrayList<>();
    DBIterator<EigenschaftGruppe> it = Einstellungen.getDBService()
        .createList(EigenschaftGruppe.class);
    it.setOrder("order by bezeichnung");
    while (it.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) it.next();
      childrens.add(new EigenschaftenNode(this, onlyChecked, eg, config));
    }
  }

  private EigenschaftenNode(EigenschaftenNode parent, boolean onlyChecked,
      EigenschaftGruppe eg, Map<String, Config> config) throws RemoteException
  {
    this.parent = parent;
    this.onlyChecked = onlyChecked;
    this.eigenschaftgruppe = eg;
    nodetype = EIGENSCHAFTGRUPPE;
    childrens = new ArrayList<>();
    DBIterator<Eigenschaft> it = Einstellungen.getDBService()
        .createList(Eigenschaft.class);
    it.addFilter("eigenschaftgruppe = ?",
        new Object[] { eigenschaftgruppe.getID() });
    it.setOrder("order by bezeichnung");
    while (it.hasNext())
    {
      Eigenschaft eigenschaft = (Eigenschaft) it.next();
      childrens.add(new EigenschaftenNode(this, onlyChecked, eigenschaft,
          eigenschaftgruppe, config));
    }
  }

  private EigenschaftenNode(EigenschaftenNode parent, boolean onlyChecked,
      Eigenschaft eigenschaft, EigenschaftGruppe eg, Map<String, Config> config)
      throws RemoteException
  {
    this.parent = parent;
    nodetype = EIGENSCHAFTEN;
    this.onlyChecked = onlyChecked;
    this.eigenschaft = eigenschaft;
    this.eigenschaftgruppe = eg;
    String eigenschaftenKey = this.eigenschaft.getID();
    if (config.containsKey(eigenschaftenKey))
    {
      preset = config.get(eigenschaftenKey).preset;
      base = config.get(eigenschaftenKey).base;
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
  public EigenschaftenNode getParent()
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

  public EigenschaftGruppe getEigenschaftGruppe()
  {
    return this.eigenschaftgruppe;
  }

  public String getPreset()
  {
    return preset;
  }

  public void incPreset()
  {
    if (!onlyChecked)
    {
      switch (preset)
      {
        // In Mitglied Kontext Menü -> Eigenschaften und
        // im Filter Dialog gibt es PLUS und MINUS etc.
        // Die ersten drei Eigenschaften sind in "base" gespeichert
        case EigenschaftenNode.UNCHECKED:
        case EigenschaftenNode.CHECKED:
        case EigenschaftenNode.CHECKED_PARTLY:
          preset = EigenschaftenNode.PLUS;
          break;
        case EigenschaftenNode.PLUS:
          preset = EigenschaftenNode.MINUS;
          break;
        case EigenschaftenNode.MINUS:
          preset = base;
          break;
      }
    }
    else
    {
      // In (Nicht-)Mitglied Detail View Lasche Eigenschaften und
      // Mail Empfänger Auswahl Dialog -> Eigenschaften
      // gibt es nur UNCHECKED und CHECKED
      switch (preset)
      {
        case EigenschaftenNode.UNCHECKED:
          preset = EigenschaftenNode.CHECKED;
          break;
        case EigenschaftenNode.CHECKED:
          preset = EigenschaftenNode.UNCHECKED;
          break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<Long[]> getEigenschaften() throws RemoteException
  {
    if (eigenschaften != null)
    {
      return eigenschaften;
    }
    // Eigenschaften lesen
    final DBService service = Einstellungen.getDBService();
    String sql = "SELECT eigenschaften.* from eigenschaften ";
    eigenschaften = (List<Long[]>) service.execute(sql, new Object[] {},
        new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            List<Long[]> list = new ArrayList<>();
            while (rs.next())
            {
              list.add(new Long[] { rs.getLong(2), rs.getLong(3) }); // Mitglied.Id,
                                                                     // Eigenschaft.Id
            }
            return list;
          }
        });
    return eigenschaften;
  }

  @SuppressWarnings("rawtypes")
  public ArrayList<EigenschaftenNode> getCheckedNodes() throws RemoteException
  {
    // Liefert alle EIGENSCHAFTEN Nodes die nicht UNCHECKED sind
    // Momentan nur für ROOT gebraucht
    ArrayList<EigenschaftenNode> checkednodes = new ArrayList<>();
    if (this.nodetype == EigenschaftenNode.ROOT)
    {
      GenericIterator rootit = this.getChildren();
      while (rootit.hasNext())
      {
        GenericObjectNode gruppe = (GenericObjectNode) rootit.next();
        GenericIterator groupit = gruppe.getChildren();
        while (groupit.hasNext())
        {
          EigenschaftenNode eigenschaft = (EigenschaftenNode) groupit.next();
          if (!eigenschaft.getPreset().equals(EigenschaftenNode.UNCHECKED))
          {
            checkednodes.add(eigenschaft);
          }
        }
      }
    }
    return checkednodes;
  }

  @SuppressWarnings("rawtypes")
  public ArrayList<EigenschaftGruppe> getPflichtGruppen() throws RemoteException
  {
    // Liefert alle EIGENSCHAFTGRUPPEn bei den Pflicht gesetzt ist
    ArrayList<EigenschaftGruppe> plichtGruppen = new ArrayList<>();
    if (this.nodetype == EigenschaftenNode.ROOT)
    {
      GenericIterator rootit = this.getChildren();
      while (rootit.hasNext())
      {
        EigenschaftenNode gruppenNode = (EigenschaftenNode) rootit.next();
        EigenschaftGruppe gruppe = gruppenNode.getEigenschaftGruppe();
        if (gruppe.getPflicht())
          plichtGruppen.add(gruppe);
      }
    }
    return plichtGruppen;
  }

  @SuppressWarnings("rawtypes")
  public ArrayList<EigenschaftGruppe> getMax1Gruppen() throws RemoteException
  {
    // Liefert alle EIGENSCHAFTGRUPPEn bei den Pflicht gesetzt ist
    ArrayList<EigenschaftGruppe> max1Gruppen = new ArrayList<>();
    if (this.nodetype == EigenschaftenNode.ROOT)
    {
      GenericIterator rootit = this.getChildren();
      while (rootit.hasNext())
      {
        EigenschaftenNode gruppenNode = (EigenschaftenNode) rootit.next();
        EigenschaftGruppe gruppe = gruppenNode.getEigenschaftGruppe();
        if (gruppe.getMax1())
          max1Gruppen.add(gruppe);
      }
    }
    return max1Gruppen;
  }

  @SuppressWarnings("rawtypes")
  public EigenschaftenNode getEigenschaftenNode(String eigenschaftId)
      throws RemoteException
  {
    // Liefert den EigenschaftenNode einer Eigenschaft
    // Momentan nur für ROOT gebraucht
    EigenschaftenNode eigenschaftenNode = null;
    if (this.nodetype == EigenschaftenNode.ROOT)
    {
      GenericIterator rootit = this.getChildren();
      while (rootit.hasNext())
      {
        GenericObjectNode gruppe = (GenericObjectNode) rootit.next();
        GenericIterator groupit = gruppe.getChildren();
        while (groupit.hasNext())
        {
          eigenschaftenNode = (EigenschaftenNode) groupit.next();
          Eigenschaft eigenschaft = eigenschaftenNode.getEigenschaft();
          if (eigenschaft.getID().equals(eigenschaftId))
            return eigenschaftenNode;
        }
      }
    }
    return eigenschaftenNode;
  }

  @SuppressWarnings("rawtypes")
  public EigenschaftGruppe getEigenschaftGruppe(String gruppeId)
      throws RemoteException
  {
    // Liefert die EigenschaftGruppe zur Id
    // Momentan nur für ROOT gebraucht
    EigenschaftGruppe eigenschaftGruppe = null;
    if (this.nodetype == EigenschaftenNode.ROOT)
    {
      GenericIterator rootit = this.getChildren();
      while (rootit.hasNext())
      {
        EigenschaftenNode gruppeNode = (EigenschaftenNode) rootit.next();
        eigenschaftGruppe = gruppeNode.getEigenschaftGruppe();
        if (eigenschaftGruppe.getID().equals(gruppeId))
          return eigenschaftGruppe;
      }
    }
    return eigenschaftGruppe;
  }

  // Speichert den Startwert für eine Eigenschaft
  private class Config
  {
    public String preset;

    public String base;

    public Config(String preset, String base)
    {
      this.preset = preset;
      this.base = base;
    }
  }
}
