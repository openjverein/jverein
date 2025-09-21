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
import de.jost_net.JVerein.gui.control.AnfangsbestandControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;

public class AnfangsbestandDetailView extends AbstractDetailView
{
  private AnfangsbestandControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Anfangsbestand");

    control = new AnfangsbestandControl(this);
    final boolean editable = control.isAnfangsbestandEditable();

    LabelGroup group = new LabelGroup(getParent(), "Anfangsbestand");
    group.addLabelPair("Konto", control.getKonto());
    group.addLabelPair("Datum", control.getDatum());
    group.addLabelPair("Betrag", control.getBetrag());

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ANFANGSBESTAENDE, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    SaveButton saveButton = new SaveButton(control);
    saveButton.setEnabled(editable);
    buttons.addButton(saveButton);
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
