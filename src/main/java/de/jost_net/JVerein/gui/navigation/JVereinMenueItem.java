package de.jost_net.JVerein.gui.navigation;

import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Image;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.MenuItem;
import de.willuhn.jameica.gui.util.SWTUtil;

public class JVereinMenueItem extends AbstractItem implements MenuItem
{

  private String shortcut;

  public JVereinMenueItem(MenuItem parent, String navitext, Action action,
      String icon)
  {
    this(parent, navitext, action, icon, null);
  }

  public JVereinMenueItem(MenuItem parent, String navitext, Action action,
      String icon, String shortcut)
  {
    super(parent, navitext, action, icon);
    this.shortcut = shortcut;
  }

  @Override
  public String getShortcut() throws RemoteException
  {
    return shortcut;
  }

  @Override
  public Image getIcon() throws RemoteException
  {
    return icon == null ? null : SWTUtil.getImage(icon);
  }

}
