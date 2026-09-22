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
import org.eclipse.swt.graphics.Image;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.NavigationItem;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 */
public class JVereinNavigationItem extends AbstractItem
    implements NavigationItem
{

  private Action action;

  public JVereinNavigationItem(NavigationItem parent, String navitext,
      Action action)
  {
    this(parent, navitext, action, null);
  }

  public JVereinNavigationItem(NavigationItem parent, String navitext,
      Action action, String icon)
  {
    super(parent, navitext, action, icon);
    this.action = action;
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#getIconClose()
   */
  @Override
  public Image getIconClose()
  {
    if (action == null)
    {
      return SWTUtil.getImage(icon != null ? icon : "folder.png");
    }
    else
    {
      return SWTUtil.getImage(icon != null ? icon : "page.gif");
    }
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#getIconOpen()
   */
  @Override
  public Image getIconOpen()
  {
    if (action == null)
    {
      return SWTUtil.getImage(icon != null ? icon : "folder-open.png");
    }
    else
    {
      return SWTUtil.getImage(icon != null ? icon : "page.gif");
    }
  }

  /**
   * @see de.willuhn.jameica.gui.NavigationItem#isExpanded()
   */
  @Override
  public boolean isExpanded()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.Item#setEnabled(boolean, boolean)
   */
  @Override
  public void setEnabled(boolean enabled, boolean recursive)
      throws RemoteException
  {
    super.setEnabled(enabled, recursive);
    GUI.getNavigation().update(this);
  }

}
