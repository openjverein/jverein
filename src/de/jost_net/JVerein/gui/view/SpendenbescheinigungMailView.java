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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.SpendenbescheinigungMailControl;
import de.jost_net.JVerein.gui.util.JameicaUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class SpendenbescheinigungMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigung-Mail");

    final SpendenbescheinigungMailControl control = new SpendenbescheinigungMailControl(this);

    Composite comp = new Composite(this.getParent(), SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_BOTH));

    GridLayout layout = new GridLayout(2, false);
    comp.setLayout(layout);

    JameicaUtil.addLabel("Betreff", comp, GridData.VERTICAL_ALIGN_CENTER);
    control.getBetreff().paint(comp);
    JameicaUtil.addLabel("Text", comp, GridData.VERTICAL_ALIGN_BEGINNING);
    control.getTxt().paint(comp);
    JameicaUtil.addLabel("Info", comp, GridData.VERTICAL_ALIGN_BEGINNING);
    control.getInfo().paint(comp);
    
    control.init(this.getCurrentObject());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPENDENBESCHEINIGUNG, false, "question-circle.png");
    buttons.addButton(control.getStartButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
