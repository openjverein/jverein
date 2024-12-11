package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
<<<<<<< HEAD
import de.jost_net.JVerein.gui.action.WirtschaftsplanungNeuAction;
=======
>>>>>>> 39c603e0 (Added WirtschaftsplanungListView)
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
<<<<<<< HEAD
    buttons.addButton("Neu", new WirtschaftsplanungNeuAction(control), control, false, "document-new.png");
=======
>>>>>>> 39c603e0 (Added WirtschaftsplanungListView)
    buttons.paint(this.getParent());
  }
}
