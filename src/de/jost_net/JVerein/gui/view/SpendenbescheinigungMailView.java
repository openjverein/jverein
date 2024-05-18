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
import de.jost_net.JVerein.gui.control.SpendenbescheinigungMailControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TextPart;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SpendenbescheinigungMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigung-Mail");

    final SpendenbescheinigungMailControl control = new SpendenbescheinigungMailControl(this);

    SimpleContainer cont1 = new SimpleContainer(getParent(), true);
    cont1.addHeadline("Info");
    TextPart part = new TextPart(control.getInfoText(getCurrentObject()));
    part.paint(cont1.getComposite());
    
    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addInput(control.getTxt());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPENDENBESCHEINIGUNG, false, "question-circle.png");
    buttons.addButton(control.getStartButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
