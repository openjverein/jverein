package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.control.SaldoControl;
<<<<<<< HEAD
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
=======
>>>>>>> 165f99cd (WirtschaftsplanungView skizziert)
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextInput;
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

<<<<<<< HEAD
    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(control);
    uebersicht.paint(this.getParent());
=======
    LabelGroup general = new LabelGroup(this.getParent(), "Übersicht");
    TextInput text = new TextInput(((WirtschaftsplanungZeile) this.getCurrentObject()).getPlanEinnahme().toString());
    text.disable();
    general.addLabelPair("Einnahmen Soll", text);
>>>>>>> 165f99cd (WirtschaftsplanungView skizziert)

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup links = new LabelGroup(group.getComposite(), "Einnahmen");
    TablePart test = new TablePart(new ArrayList<>(), null);
    links.addPart(test);
    LabelGroup rechts = new LabelGroup(group.getComposite(), "Ausgaben");
    TablePart test1 = new TablePart(new ArrayList<>(), null);
    rechts.addPart(test1);
  }
}
