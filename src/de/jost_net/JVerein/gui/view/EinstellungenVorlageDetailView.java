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

import de.jost_net.JVerein.gui.action.VorlageVorschauAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.control.VorlageControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class EinstellungenVorlageDetailView extends AbstractDetailView
{

  private VorlageControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Vorlage");

    control = new VorlageControl(this);

    LabelGroup grName = new LabelGroup(getParent(), VorlageTyp
        .getByKey(Integer.valueOf(control.getVorlage().getID())).toString());
    grName.addLabelPair("Vorlage", control.getName());
    grName.addLabelPair("Vorschau", control.getVorschau());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_VORLAGEN, false, "question-circle.png");
    buttons.addButton("Variablen anzeigen",
        new InsertVariableDialogAction(control.getDummyMap()), control, false,
        "bookmark.png");
    buttons.addButton(new Button("Update Vorschau",
        new VorlageVorschauAction(), control, false, "view-refresh.png"));
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
