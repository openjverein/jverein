package de.jost_net.JVerein.gui.parts;

import de.willuhn.jameica.gui.Action;

public class DeleteButton extends ButtonRtoL
{
  public DeleteButton(Action action)
  {
    this(action, null);
  }

  public DeleteButton(Action action, Object context)
  {
    super("Löschen", action, context, false, "user-trash-full.png", "Del");
  }
}
