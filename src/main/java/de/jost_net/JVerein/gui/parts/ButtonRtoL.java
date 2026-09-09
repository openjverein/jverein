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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;

/**
 * Ein Button.
 */
public class ButtonRtoL extends Button implements Part
{
  Listener listener = new ShortcutListener();

  private Action action;

  private Object context;

  private String title;

  private KeyStroke stroke;

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
    this.stroke = SWTUtil.getKeyStroke(shortcut);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    button.setOrientation(SWT.LEFT_TO_RIGHT);

    if (stroke != null)
    {
      GUI.getDisplay().addFilter(SWT.KeyDown, listener);
      GUI.getDisplay().addFilter(SWT.MouseDown, listener);

      // Wieder deaktivieren
      button.addDisposeListener(e -> {
        GUI.getDisplay().removeFilter(SWT.KeyDown, listener);
        GUI.getDisplay().removeFilter(SWT.MouseDown, listener);
      });

      if (button.getToolTipText() == null || button.getToolTipText().isBlank())
      {
        button.setToolTipText(title + " (" + stroke.format() + ")");
      }
    }
  }

  private class ShortcutListener implements Listener
  {
    public void handleEvent(Event event)
    {
      if (button.getShell().equals(GUI.getDisplay().getActiveShell())
          && button.isEnabled() && stroke != null && stroke.isComplete())
      {
        if ((event.stateMask == stroke.getModifierKeys()
            && (event.keyCode == stroke.getNaturalKey()
                || event.keyCode == Character
                    .toLowerCase(stroke.getNaturalKey())))
            // statt Pfeiltasten auch spezielle Maustasten unterstützen
            || (stroke.getNaturalKey() == SWT.ARROW_LEFT && event.button == 4)
            || (stroke.getNaturalKey() == SWT.ARROW_RIGHT && event.button == 5))
        {
          GUI.getDisplay().syncExec(() -> {
            try
            {
              action.handleAction(context);
            }
            catch (ApplicationException e)
            {
              GUI.getStatusBar().setErrorText(e.getMessage());
            }
          });
          event.doit = false;
        }
      }
    }
  }

}
