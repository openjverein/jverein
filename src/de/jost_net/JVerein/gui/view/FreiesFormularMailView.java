package de.jost_net.JVerein.gui.view;

import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.FreieFormulareControl;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.server.MitgliedImpl;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class FreiesFormularMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Freie Formulare");

    final FreieFormulareControl control = new FreieFormulareControl(this);
    control.init("freieformulare.","zusatzfeld.", "zusatzfelder.");

    LabelGroup group = new LabelGroup(getParent(), "Filter");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getSuchMitgliedstyp(Mitgliedstypen.ALLE));
    left.addInput(control.getMitgliedStatus());
    left.addInput(control.getBeitragsgruppeAusw());
    left.addInput(control.getMailauswahl());
    
    SimpleContainer mid = new SimpleContainer(cl.getComposite());
    mid.addInput(control.getSuchname());
    mid.addInput(control.getGeburtsdatumvon());
    mid.addInput(control.getGeburtsdatumbis());
    mid.addInput(control.getSuchGeschlecht());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    DialogInput eigenschaftenInput = control.getEigenschaftenAuswahl();
    right.addInput(eigenschaftenInput);
    control.updateEigenschaftenAuswahlTooltip();
    right.addInput(control.getStichtag());
    if (Einstellungen.getEinstellung().hasZusatzfelder())
    {
      DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
      right.addInput(zusatzfelderInput);
      control.updateZusatzfelderAuswahlTooltip();
    }

    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Parameter");
    cont.addLabelPair("Formular",
        control.getFormular(FormularArt.FREIESFORMULAR));
    cont.addInput(control.getAusgabeart());

    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addLabelPair("Text", control.getTxt());

    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSpeichernButton());
    group.addButtonArea(fbuttons);

    Map<String, Object> map = new MitgliedMap().getMap(MitgliedImpl.getDummy(),
        null);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FREIESFORMULAR, false, "question-circle.png");
    buttons.addButton(new Button("Mail-Vorlage",
        new MailVorlageZuweisenAction(), control, false, "view-refresh.png"));
    buttons.addButton("Variablen anzeigen", new InsertVariableDialogAction(map),
        control, false, "bookmark.png");
    buttons
        .addButton(new Button("Vorschau", new MailTextVorschauAction(map, true),
        control, false, "edit-copy.png"));
    buttons.addButton(
        control.getStartFreieFormulareButton(this.getCurrentObject(), control));
    buttons.paint(this.getParent());
  }
}
