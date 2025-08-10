/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;

/**
 * Ein Button.
 */
public class Button extends de.willuhn.jameica.gui.parts.Button
{
  /**
   * ct.
   * 
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   */
  public Button(String title, Action action)
  {
    super(title, action, null, false);
  }

  public Button(String title, Action action, Object context)
  {
    super(title, action, context, false);
  }

  public Button(String title, Action action, Object context,
      boolean defaultButton)
  {
    super(title, action, context, defaultButton, null);
  }

  public Button(String title, Action action, Object context,
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
