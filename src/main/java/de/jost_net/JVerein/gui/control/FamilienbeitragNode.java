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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.logging.Logger;

public class FamilienbeitragNode implements GenericObjectNode
{
  public static final int ROOT = 0;

  public static final int ZAHLER = 1;

  public static final int ANGEHOERIGER = 2;

  private int type = ROOT;

  private String id;

  private Mitglied mitglied;

  private FamilienbeitragNode parent = null;

  private ArrayList<FamilienbeitragNode> children;
  
  private Input status;

  public FamilienbeitragNode(Input status) throws RemoteException
  {
    this.status = status;
    this.parent = null;
    this.type = ROOT;
    this.children = new ArrayList<>();
    DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    it.addFilter("beitragsart != ?",
        new Object[] { ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey() });
    
    Set<String> set = new HashSet<String>();
    DBIterator<Mitglied> angIt = Einstellungen.getDBService()
        .createList(Mitglied.class);
    angIt.addFilter("zahlerid is not null and zahlerid != 0");
    if (status.getValue().equals("Angemeldet"))
      angIt.addFilter("austritt is null");
    if (status.getValue().equals("Abgemeldet"))
      angIt.addFilter("austritt is not null");
    while(angIt.hasNext())
    {
      Mitglied a = angIt.next();
      if(!set.contains(a.getVollZahlerID().toString()))
      {
        set.add(a.getVollZahlerID().toString());
      }
    }
    while (it.hasNext())
    {
      Beitragsgruppe bg = it.next();
      DBIterator<Mitglied> it2 = Einstellungen.getDBService()
          .createList(Mitglied.class);
      it2.addFilter("beitragsgruppe = ?", new Object[] { bg.getID() });
      if (status.getValue().equals("Angemeldet"))
        it2.addFilter("austritt is null");
      if (status.getValue().equals("Abgemeldet"))
        it2.addFilter("austritt is not null");
      it2.setOrder("ORDER BY name, vorname");
      while (it2.hasNext())
      {
        Mitglied m = it2.next();
        //nur anzeigen wenn es angehörige gibt
        if(!set.contains(m.getID()))
          continue;
        FamilienbeitragNode fbn = new FamilienbeitragNode(this, m);
        children.add(fbn);
      }
    }
  }

  public FamilienbeitragNode(FamilienbeitragNode parent, Mitglied m)
      throws RemoteException
  {
    this.status = parent.status;
    this.parent = parent;
    this.mitglied = m;
    this.id = mitglied.getID();
    this.type = ZAHLER;
    this.children = new ArrayList<>();
    DBIterator<Mitglied> it = Einstellungen.getDBService()
        .createList(Mitglied.class);
    it.addFilter("zahlerid = ?", new Object[] { m.getID() });
    if (status.getValue().equals("Angemeldet"))
      it.addFilter("austritt is null");
    if (status.getValue().equals("Abgemeldet"))
      it.addFilter("austritt is not null");
    while (it.hasNext())
    {
      FamilienbeitragNode fbn = new FamilienbeitragNode(this, it.next(), 1);
      children.add(fbn);
    }
  }

  public FamilienbeitragNode(FamilienbeitragNode parent, Mitglied m, int dummy)
  {
    this.parent = parent;
    this.type = ANGEHOERIGER;
    this.mitglied = m;
    try
    {
      this.id = m.getID();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    this.children = new ArrayList<>();
  }

  public int getType()
  {
    return type;
  }

  public Mitglied getMitglied()
  {
    return mitglied;
  }

  @Override
  public String getPrimaryAttribute()
  {
    return null;
  }

  @Override
  public String getID()
  {
    return id;
  }

  @Override
  public String[] getAttributeNames()
  {
    return new String[] { "name", "vorname", "blz", "konto" };
  }

  @Override
  public Object getAttribute(String name)
  {
    try
    {
      if (mitglied == null)
      {
        return "Familienbeiträge";
      }
      Date d = null;
      if (getMitglied().getAustritt() != null)
      {
        d = getMitglied().getAustritt();
      }
      JVDateFormatTTMMJJJJ jvttmmjjjj = new JVDateFormatTTMMJJJJ();
      return Adressaufbereitung.getNameVorname(mitglied)
          + (d != null
              ? ", Austritt: " + jvttmmjjjj.format(d)
              : "")
          + (mitglied.getIban().length() > 0
              ? ", " + mitglied.getBic() + ", " + mitglied.getIban()
              : "");
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return null;
  }

  @Override
  public boolean equals(GenericObject other)
  {
    return false;
  }

  @Override
  public boolean hasChild(GenericObjectNode object)
  {
    return children.size() > 0;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public GenericIterator getPossibleParents()
  {
    return null;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public GenericIterator getPath()
  {
    return null;
  }

  @Override
  public GenericObjectNode getParent()
  {
    return parent;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public GenericIterator getChildren() throws RemoteException
  {
    if (children != null)
    {
      return PseudoIterator
          .fromArray(children.toArray(new GenericObject[children.size()]));
    }
    return null;
  }

  public void remove()
  {
    if (parent != null)
    {
      parent.children.remove(this);
    }
  }

}
