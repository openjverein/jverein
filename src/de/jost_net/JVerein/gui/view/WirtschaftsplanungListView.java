package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class WirtschaftsplanungListView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Wirtschaftsplanung");

    WirtschaftsplanungControl control = new WirtschaftsplanungControl(this);

    control.getWirtschaftsplanungList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.paint(this.getParent());
  }
}
