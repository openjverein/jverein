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
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.SteuerControl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class SteuerDetailView extends AbstractDetailView
{

  SteuerControl control = null;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Steuer");
    control = new SteuerControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Steuer");
    group.addLabelPair("Name", control.getName());
    group.addLabelPair("Satz", control.getSatz());
    group.addLabelPair("Buchungsart", control.getBuchungsart());
    group.addLabelPair("Aktiv", control.getAktiv());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.STEUER, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton("Speichern", e -> {
      control.handleStore();
      GUI.getStatusBar().setSuccessText("Steuer gespeichert");
    }, null, true, "document-save.png");
    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }

}
