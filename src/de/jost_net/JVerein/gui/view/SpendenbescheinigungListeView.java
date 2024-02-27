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
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAutoNeuAction;
import de.jost_net.JVerein.gui.control.SpendenbescheinigungControl;
import de.jost_net.JVerein.keys.Spendenart;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class SpendenbescheinigungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigungen");

    SpendenbescheinigungControl control = new SpendenbescheinigungControl(this);

    control.getSpendenbescheinigungList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPENDENBESCHEINIGUNG, false, "question-circle.png");
    buttons.addButton("Neu (Sachspende)", new SpendenbescheinigungAction(Spendenart.SACHSPENDE), null,
        false, "document-new.png");
    buttons.addButton("Neu (automatisch)",
        new SpendenbescheinigungAutoNeuAction(), null, false,
        "document-new.png");
    buttons.paint(this.getParent());
  }
}
