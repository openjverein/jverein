package de.jost_net.JVerein.gui.parts;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;

public class ToolTipButton extends Button
{
  /**
   * @param title
   *          Beschriftung.
   * @param action
   *          Action, die beim Klick ausgefuehrt werden soll.
   */
  public ToolTipButton(String title, Action action, Object context,
      boolean defaultButton, String icon)
  {
    super(title, action, context, defaultButton, icon);
  }

  public void setToolTipText(String text)
  {
    this.button.setToolTipText(text);
  }
}
