package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.DeleteAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;

public class DeleteMenueItem extends CheckedContextMenuItem
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
