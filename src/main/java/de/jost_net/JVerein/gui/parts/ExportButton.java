package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.gui.action.ExportAction;
import de.jost_net.JVerein.gui.control.AbstractListeControl;

public class ExportButton extends ButtonRtoL
{
  public ExportButton(String doku, AbstractListeControl control)
  {
    super("Export", new ExportAction(doku), control, false, "document-save.png",
        "Ctrl+E");
  }
}
