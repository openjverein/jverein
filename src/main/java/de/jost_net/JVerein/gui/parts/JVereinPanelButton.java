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
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.util.SWTUtil;

/**
 * PannelButton dem ein Shortcut zugewiesen werden kann.
 */
public class JVereinPanelButton extends PanelButton
{

  private String shortcut;

  private Action action;

  public JVereinPanelButton(String icon, Action action, String tooltip,
      String shortcut)
  {
    super(icon, action, tooltip);
    this.action = action;
    this.shortcut = shortcut;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    getControl().setOrientation(SWT.LEFT_TO_RIGHT);

    if (shortcut != null)
    {
      Listener listener = new ShortcutListener(getControl(), shortcut, action,
          null);

      GUI.getDisplay().addFilter(SWT.KeyDown, listener);

      // Wieder deaktivieren
      getControl().addDisposeListener(
          e -> GUI.getDisplay().removeFilter(SWT.KeyDown, listener));

      String text = getControl().getToolTipText() == null ? ""
          : getControl().getToolTipText();
      KeyStroke stroke = SWTUtil.getKeyStroke(shortcut);
      getControl().setToolTipText(text + " ("
          + SWTKeySupport.getKeyFormatterForPlatform().format(stroke) + ")");
    }
  }
}
