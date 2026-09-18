package de.jost_net.JVerein.gui.parts;

import de.willuhn.jameica.gui.Action;

public class NewButton extends ButtonRtoL
{
  public NewButton(Action action)
  {
    this(action, null);
  }

  public NewButton(Action action, Object context)
  {
    super("Neu", action, context, false, "document-new.png", "CTRL+N");
  }
}
