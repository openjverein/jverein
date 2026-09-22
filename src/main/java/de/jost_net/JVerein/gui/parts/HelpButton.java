package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.gui.action.DokumentationAction;

public class HelpButton extends ButtonRtoL
{
  public HelpButton(String eintrag)
  {
    super("Hilfe", new DokumentationAction(), eintrag, false,
        "question-circle.png", "F1");
  }
}
