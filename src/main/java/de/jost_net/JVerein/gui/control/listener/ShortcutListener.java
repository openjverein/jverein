package de.jost_net.JVerein.gui.control.listener;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;

public class ShortcutListener implements Listener
{
  private static Object SHORTCUT_HANDLED = new Object();

  private String shortcut;

  private Object context;

  private Action action;

  private Control control;

  public ShortcutListener(Control control, String shortcut, Action action,
      Object context)
  {
    this.control = control;
    this.shortcut = shortcut;
    this.action = action;
    this.context = context;
  }

  public void handleEvent(Event event)
  {
    KeyStroke stroke = SWTUtil.getKeyStroke(shortcut);
    if (control.getShell().equals(GUI.getDisplay().getActiveShell())
        && control.isEnabled() && stroke != null && stroke.isComplete())
    {
      if ((((event.stateMask & SWT.MODIFIER_MASK) == stroke.getModifierKeys())
          && (event.keyCode == stroke.getNaturalKey()
              || event.keyCode == Character
                  .toLowerCase(stroke.getNaturalKey())))
          // statt Pfeiltasten auch spezielle Maustasten unterstützen
          || (stroke.getNaturalKey() == SWT.ARROW_LEFT && event.button == 4)
          || (stroke.getNaturalKey() == SWT.ARROW_RIGHT && event.button == 5))
      {
        // Shortcut dar nur einmal ausgeführt werden
        if (event.data == SHORTCUT_HANDLED)
        {
          return;
        }
        event.data = SHORTCUT_HANDLED;
        try
        {
          action.handleAction(context);
        }
        catch (ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }
  }
}
