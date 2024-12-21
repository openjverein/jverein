package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.WirtschaftsplanungNeuAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanungDetailPart;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

import java.util.ArrayList;

public class WirtschaftsplanungView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    if (! (this.getCurrentObject() instanceof WirtschaftsplanungZeile)) {
      throw new ApplicationException("Fehler beim Anzeigen des Wirtschaftsplans!");
    }
    GUI.getView().setTitle("Wirtschaftsplanung " + ((WirtschaftsplanungZeile) this.getCurrentObject()).getGeschaeftsjahr());

    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(control);
    uebersicht.paint(this.getParent());

    WirtschaftsplanungDetailPart detailPart = new WirtschaftsplanungDetailPart(control);
    detailPart.paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
            DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.addButton("Position hinzufügen", new WirtschaftsplanungNeuAction(control), control, false, "document-new.png");
    buttons.addButton("PDF", null, control, false); //TODO
    buttons.addButton("CSV", null, control, false); //TODO
    buttons.paint(this.getParent());
  }
}
