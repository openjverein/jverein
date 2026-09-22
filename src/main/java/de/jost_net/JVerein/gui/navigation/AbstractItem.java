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
package de.jost_net.JVerein.gui.navigation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Item;
import de.willuhn.logging.Logger;

/**
 */
public class AbstractItem implements Item
{

  private Item parent = null;

  private Action action;

  private String text;

  private ArrayList<Item> children;

  private boolean enabled = true;

  protected String icon;

  public AbstractItem(Item parent, String text, Action action, String icon)
  {
    this.parent = parent;
    this.action = action;
    this.text = text;
    this.icon = icon;
    children = new ArrayList<>();
  }

  /**
   * /**
   * 
   * @see de.willuhn.jameica.gui.Item#addChild(de.willuhn.jameica.gui.Item)
   */
  @Override
  public void addChild(Item i)
  {
    children.add(i);
  }

  /**
   * @see de.willuhn.jameica.gui.Item#getAction()
   */
  @Override
  public Action getAction()
  {
    return action;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#getName()
   */
  @Override
  public String getName()
  {
    return text;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return this.enabled;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#setEnabled(boolean, boolean)
   */
  @Override
  public void setEnabled(boolean enabled, boolean recursive)
      throws RemoteException
  {
    this.enabled = enabled;

    if (recursive)
    {
      for (int i = 0; i < this.children.size(); ++i)
      {
        Item child = (Item) this.children.get(i);
        child.setEnabled(enabled, recursive);
      }
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  @SuppressWarnings("unchecked")
  @Override
  public GenericIterator<Item> getChildren() throws RemoteException
  {
    return PseudoIterator
        .fromArray(children.toArray(new AbstractItem[children.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getParent()
   */
  @Override
  public GenericObjectNode getParent()
  {
    return this.parent;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  @SuppressWarnings({ "unchecked" })
  @Override
  public GenericIterator<Item> getPath() throws RemoteException
  {
    List<Item> list = new ArrayList<>();
    if (this.parent != null)
    {
      try
      {
        list = PseudoIterator.asList(this.parent.getPath());
      }
      catch (UnsupportedOperationException ignore)
      {
        // getPath() ist bei AbstractItemXml nich implementiert, das brauchen wr
        // für die ID aber auch nicht.
      }
    }
    list.add(this);
    return PseudoIterator
        .fromArray((Item[]) list.toArray(new Item[list.size()]));
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  @Override
  public GenericIterator<?> getPossibleParents() throws RemoteException
  {
    throw new RemoteException("not implemented");
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  @Override
  public boolean hasChild(GenericObjectNode arg0)
  {
    return false;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof AbstractItem))
      return false;
    return this.getID().equals(arg0.getID());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute(String arg0)
  {
    return getName();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  @Override
  public String[] getAttributeNames()
  {
    return new String[] { "name" };
  }

  /**
   * @throws RemoteException
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  @Override
  public String getID() throws RemoteException
  {
    String id = "";
    GenericIterator<Item> p = getPath();
    while (p.hasNext())
    {
      id += (id.length() > 0 ? "." : "") + p.next().getName();
    }
    return id;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  @Override
  public String getPrimaryAttribute()
  {
    return "name";
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  @Override
  public String getExtendableID()
  {
    try
    {
      return getID();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim bestimmen der Menü-ID", e);
      return "";
    }
  }

}
