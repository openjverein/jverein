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

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.LesefeldControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * Ein View zum Bearbeiten von Skripten für ein Lesefeld.
 */
public class LesefeldDetailView extends AbstractDetailView
{
  private LesefeldControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Lesefeld");
    control = new LesefeldControl(this);

    SimpleContainer container = new SimpleContainer(getParent(), true);

    container.addLabelPair("Mitglied", control.getMitglied());
    container.addLabelPair("Skript-Name", control.getScriptName());
    container.addLabelPair("Skript", control.getScriptCode());
    container.addLabelPair("Ausgabe", control.getScriptResult());

    if (control.getLesefeld() != null)
    {
      control.updateScriptResult();
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.LESEFELDER, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(control.getAktualisierenButton());
    buttons.addButton(control.getVariablenAnzeigenButton());
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
