/**********************************************************************
 *
 * Copyright (c) 2025 Johann Maierhofer
 * All rights reserved.
 * 
 *
 **********************************************************************/

package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.gui.control.listener.ShortcutListener;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * Ein Button.
 */
public class ButtonRtoL extends Button implements Part
{
  private Action action;

  private Object context;

  private String title;

  private String shortcut;

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
    this(title, action, null, false);
  }

  public ButtonRtoL(String title, Action action, Object context)
  {
    this(title, action, context, false);
  }

  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton)
  {
    this(title, action, context, defaultButton, null);
  }

  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton, String icon)
  {
    this(title, action, context, defaultButton, icon, null);
  }

  public ButtonRtoL(String title, Action action, Object context,
      boolean defaultButton, String icon, String shortcut)
  {
    super(title, action, context, defaultButton, icon);

    this.action = action;
    this.context = context;
    this.title = title;
    this.shortcut = shortcut;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    button.setOrientation(SWT.LEFT_TO_RIGHT);

    if (shortcut != null)
    {
      Listener listener = new ShortcutListener(button, shortcut, action,
          context);

      GUI.getDisplay().addFilter(SWT.KeyDown, listener);
      GUI.getDisplay().addFilter(SWT.MouseDown, listener);

      // Wieder deaktivieren
      button.addDisposeListener(e -> {
        GUI.getDisplay().removeFilter(SWT.KeyDown, listener);
        GUI.getDisplay().removeFilter(SWT.MouseDown, listener);
      });

      if (button.getToolTipText() == null || button.getToolTipText().isBlank())
      {
        KeyStroke stroke = SWTUtil.getKeyStroke(shortcut);
        button.setToolTipText(title + " ("
            + SWTKeySupport.getKeyFormatterForPlatform().format(stroke) + ")");
      }
    }
  }
}
