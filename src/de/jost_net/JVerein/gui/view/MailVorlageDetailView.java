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

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.MailVorschauAction;
import de.jost_net.JVerein.gui.action.OpenInsertVariableDialogAction;
import de.jost_net.JVerein.gui.control.MailVorlageControl;
import de.jost_net.JVerein.server.MitgliedImpl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

import java.util.Map;

public class MailVorlageDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mail-Vorlage");

    final MailVorlageControl control = new MailVorlageControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Mail-Vorlage");
    group.addInput(control.getBetreff(true));
    SimpleContainer t = new SimpleContainer(getParent(), true);
    t.addPart(control.getTxt());

    Map<String, Object> map = new MitgliedMap().getMap(MitgliedImpl.getDummy(),
        new AllgemeineMap().getMap(null));

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MAILVORLAGE, false, "question-circle.png");
    buttons.addButton("Variablen anzeigen",
        new OpenInsertVariableDialogAction(), map, false, "bookmark.png");
    buttons.addButton(new Button("Vorschau", new MailVorschauAction(control),
        MitgliedImpl.getDummy(), false, "edit-copy.png"));
    buttons.addButton("Speichern", context -> control.handleStore(), null, true,
        "document-save.png");
    buttons.paint(this.getParent());
  }
}
