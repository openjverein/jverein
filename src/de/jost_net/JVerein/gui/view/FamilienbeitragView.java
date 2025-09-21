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
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class FamilienbeitragView extends AbstractView
{
  final MitgliedControl control = new MitgliedControl(this);

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Familienbeitrag");

    control.init("familie.", null, null);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    group.addInput(control.getMitgliedStatus());

    control.getFamilienbeitraegeTree().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FAMILIENBEITRAG, false, "question-circle.png");
    buttons.paint(this.getParent());
  }

  @Override
  public void unbind() throws OperationCanceledException, ApplicationException
  {
    control.deregisterFamilienbeitragConsumer();
    super.unbind();
  }
}
