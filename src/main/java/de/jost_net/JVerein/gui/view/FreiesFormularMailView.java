package de.jost_net.JVerein.gui.view;

import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageUebernehmenAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.FreieFormulareControl;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.FormularArt;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
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
    control.init("freieformulare.", "zusatzfeld.", "zusatzfelder.");

    if (this.getCurrentObject() == null)
    {
      LabelGroup group = new LabelGroup(getParent(), "Filter");

      ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);
      SimpleContainer left = new SimpleContainer(cl.getComposite());
      left.addInput(control.getFilterInput(Filter.MITGLIEDSTYP));
      left.addInput(control.getFilterInput(Filter.MITGLIEDSCHAFT_STATUS));
      left.addInput(control.getFilterInput(Filter.BEITRAGSGRUPPE));
      left.addInput(control.getFilterInput(Filter.MAIL));

      SimpleContainer mid = new SimpleContainer(cl.getComposite());
      mid.addInput(control.getFilterInput(Filter.NAME));
      mid.addInput(control.getFilterInput(Filter.GEBURTSDATUM_VON));
      mid.addInput(control.getFilterInput(Filter.GEBURTSDATUM_BIS));
      mid.addInput(control.getFilterInput(Filter.GESCHLECHT));

      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addInput(control.getFilterInput(Filter.EIGENSCHAFTEN));
      right.addInput(control.getFilterInput(Filter.STICHTAG));
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        right.addInput(control.getFilterInput(Filter.ZUSATZFELD));
      }

      ButtonArea fbuttons = new ButtonArea();
      fbuttons.addButton(control.getResetButton());
      fbuttons.addButton(control.getSpeichernButton());
      group.addButtonArea(fbuttons);
    }
    else
    {
      SimpleContainer cont1 = new SimpleContainer(getParent(), false);
      cont1.addHeadline("Info");
      cont1.addInput(control.getInfo());
    }

    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Parameter");
    cont.addLabelPair("Formular",
        control.getFormular(FormularArt.FREIESFORMULAR));
    cont.addInput(control.getAusgabeart());

    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addLabelPair("Text", control.getTxt());

    Map<String, Object> map = MitgliedMap.getDummyMap(null);
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
        new Button("Als Vorlage übernehmen", new MailVorlageUebernehmenAction(),
            control, false, "document-new.png"));
    buttons.addButton(
        control.getDruckMailMitgliederButton(this.getCurrentObject(), null));
    buttons.addButton(
        control.getStartFreieFormulareButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
