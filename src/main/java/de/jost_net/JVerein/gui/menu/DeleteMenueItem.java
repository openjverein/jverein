package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.DeleteAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

public class DeleteMenueItem extends ContextMenuItem
{

  public DeleteMenueItem()
  {
    this(new DeleteAction());
  }

  public DeleteMenueItem(Action action)
  {
    super("Löschen", action, "user-trash-full.png");
    setShortcut("Del");
  }
}
