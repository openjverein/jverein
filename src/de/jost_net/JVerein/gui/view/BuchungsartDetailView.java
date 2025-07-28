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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.gui.input.SaveNeuButton;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.control.BuchungsartControl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;

public class BuchungsartDetailView extends AbstractDetailView
{
  private BuchungsartControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Buchungsart");

    control = new BuchungsartControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Buchungsart");
    group.addLabelPair("Nummer", control.getNummer(true));
    group.addLabelPair("Bezeichnung", control.getBezeichnung());
    group.addLabelPair("Art", control.getArt());
    group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    group.addLabelPair("Spende", control.getSpende());
    group.addLabelPair("Abschreibung", control.getAbschreibung());
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      group.addLabelPair("Steuer", control.getSteuer());
    }
    // TODO Jo Dokumentation nachpflegen
    group.addLabelPair("Status", control.getStatus());
    group.addLabelPair("Suchbegriff", control.getSuchbegriff());
    group.addLabelPair("Suchbegriff ist ein regulärer Ausdruck",
        control.getRegexp());

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGSART, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
