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
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageUebernehmenAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class KontoauszugMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Kontoauszüge");

    final SollbuchungControl control = new SollbuchungControl(this);
    control.init("kontoauszug.", null, null);

    LabelGroup group = new LabelGroup(getParent(), "Filter");

    if (this.getCurrentObject() == null)
    {
      ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);
      SimpleContainer left = new SimpleContainer(cl.getComposite());
      left.addInput(control.getSuchMitgliedstyp(Mitgliedstypen.ALLE));
      left.addInput(control.getMitgliedStatus());
      left.addInput(control.getBeitragsgruppeAusw());

      SimpleContainer middle = new SimpleContainer(cl.getComposite());
      middle.addInput(control.getMailauswahl());
      middle.addInput(control.getDifferenz());
      middle.addLabelPair("Differenz Limit", control.getDoubleAusw());

      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addInput(control.getDatumvon());
      right.addInput(control.getDatumbis());
      right.addInput(control.getStichtag(false));
    }
    else
    {
      ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
      SimpleContainer left = new SimpleContainer(cl.getComposite());
      left.addInput(control.getDifferenz());
      left.addLabelPair("Differenz Limit", control.getDoubleAusw());

      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addInput(control.getDatumvon());
      right.addInput(control.getDatumbis());

      SimpleContainer cont1 = new SimpleContainer(getParent(), false);
      cont1.addHeadline("Info");
      cont1.addInput(control.getInfo());
    }

    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Parameter");

    cont.addInput(control.getAusgabeart());

    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addLabelPair("Text", control.getTxt());

    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSpeichernButton());
    group.addButtonArea(fbuttons);

    Map<String, Object> map = MitgliedMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.KONTOAUSZUG, false, "question-circle.png");
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
        control.getStartKontoauszugButton(this.getCurrentObject(), control));
    buttons.paint(this.getParent());
  }
}
