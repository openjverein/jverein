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

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageUebernehmenAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.SpendenbescheinigungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SpendenbescheinigungMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigungen");

    final SpendenbescheinigungControl control = new SpendenbescheinigungControl(
        this);
    control.init("spenden.", null, null);

    if (this.getCurrentObject() == null)
    {
      LabelGroup group = new LabelGroup(getParent(), "Filter");
      ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

      SimpleContainer left = new SimpleContainer(cl.getComposite());
      left.addInput(control.getSuchname());
      left.addInput(control.getMailauswahl());
      left.addInput(control.getSuchSpendenart());

      SimpleContainer middle = new SimpleContainer(cl.getComposite());
      middle.addLabelPair("Bescheinigungsdatum von", control.getDatumvon());
      middle.addLabelPair("Bescheinigungsdatum bis", control.getDatumbis());

      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addLabelPair("Spendedatum von", control.getEingabedatumvon());
      right.addLabelPair("Spendedatum bis", control.getEingabedatumbis());

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
    cont2.addInput(control.getAdressblatt());

    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Mail / Anschreiben");
    cont.addInput(control.getBetreff());
    cont.addInput(control.getTxt());

    Map<String, Object> map = SpendenbescheinigungMap.getDummyMap(null);
    map = MitgliedMap.getDummyMap(map);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPENDENBESCHEINIGUNGMAIL, false,
        "question-circle.png");
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
    buttons.addButton(control.getStartButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
