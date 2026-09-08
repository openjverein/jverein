package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;

/**
 * PannelButton dem ein Shortcut zugewiesen werden kann.
 */
public class JVereinPanelButton extends PanelButton
{

  private KeyStroke stroke;

  Listener listener = new ShortcutListener();

  private Action action;

  public JVereinPanelButton(String icon, Action action, String tooltip,
      String shortcut)
  {
    super(icon, action, tooltip);
    this.action = action;
    this.stroke = SWTUtil.getKeyStroke(shortcut);
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    getControl().setOrientation(SWT.LEFT_TO_RIGHT);

    if (stroke != null)
    {
      GUI.getDisplay().addFilter(SWT.KeyDown, listener);

      // Wieder deaktivieren
      getControl().addDisposeListener(
          e -> GUI.getDisplay().removeFilter(SWT.KeyDown, listener));

      String text = getControl().getToolTipText() == null ? ""
          : getControl().getToolTipText();
      getControl().setToolTipText(text + " (" + stroke.format() + ")");
    }
  }

  private class ShortcutListener implements Listener
  {
    public void handleEvent(Event event)
    {
      if (getControl().getShell().equals(GUI.getDisplay().getActiveShell())
          && getControl().isEnabled() && stroke != null && stroke.isComplete())
      {
        if (event.stateMask == stroke.getModifierKeys()
            && (event.keyCode == stroke.getNaturalKey()
                || event.keyCode == Character
                    .toLowerCase(stroke.getNaturalKey())))
        {
          GUI.getDisplay().syncExec(() -> {
            try
            {
              action.handleAction(null);
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
