/**********************************************************************
 *
 * Copyright (c) 2025 Johann Maierhofer
 * All rights reserved.
 * 
 *
 **********************************************************************/

package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;

/**
 * Ein Button.
 */
public class ButtonRtoL extends Button implements Part
{
  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   */
  public ButtonRtoL(String title, Action action)
  {
    super(title, action, null, false);
  }

  public ButtonRtoL(String title, Action action, Object context)
  {
    super(title, action, context, false);
  }

  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton)
  {
    super(title, action, context, defaultButton, null);
  }

  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton, String icon)
  {
    super(title, action, context, defaultButton, icon);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    button.setOrientation(SWT.LEFT_TO_RIGHT);
  }

}
