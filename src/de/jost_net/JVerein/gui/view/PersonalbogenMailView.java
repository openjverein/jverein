/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
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
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.gui.control.PersonalbogenControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class PersonalbogenMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Personalbogen");

    final PersonalbogenControl control = new PersonalbogenControl(this);
    control.init("personalbogen.", "zusatzfeld.", "zusatzfelder.");

    if (this.getCurrentObject() == null)
    {
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
      if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
      {
        DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
        right.addInput(zusatzfelderInput);
        control.updateZusatzfelderAuswahlTooltip();
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

    SimpleContainer cont2 = new SimpleContainer(getParent(), false);
    cont2.addHeadline("Parameter");
    cont2.addInput(control.getAusgabeart());

    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addInput(control.getTxt());

    Map<String, Object> map = MitgliedMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.PERSONALBOGEN, false, "question-circle.png");
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
        control.getStartPersonalbogenButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
