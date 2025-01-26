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
import de.jost_net.JVerein.gui.control.LastschriftControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class LastschriftDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Lastschrift");

    final LastschriftControl control = new LastschriftControl(this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent(), 1);

    LabelGroup group = new LabelGroup(scrolled.getComposite(),
        "Daten der Lastschrift");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getPersonenart());
    left.addInput(control.getMitgliedstyp());
    left.addInput(control.getGeschlecht());
    left.addInput(control.getAnrede());
    left.addInput(control.getTitel());
    left.addInput(control.getName());
    left.addInput(control.getVorname());
    left.addInput(control.getStrasse());
    left.addInput(control.getAdressierungszusatz());
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getPLZ());
    right.addInput(control.getOrt());
    right.addInput(control.getStaat());
    right.addInput(control.getEmail());
    right.addInput(control.getVZweck());
    right.addInput(control.getMandatDatum());
    right.addInput(control.getIBAN());
    right.addInput(control.getBIC());
    right.addInput(control.getBetrag());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.LASTSCHRIFT, false, "question-circle.png");
    buttons.paint(this.getParent());
  }
}
