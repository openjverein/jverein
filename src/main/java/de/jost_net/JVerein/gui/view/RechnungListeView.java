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
import de.jost_net.JVerein.gui.control.RechnungControl;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class RechnungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Rechnungen");

    RechnungControl control = new RechnungControl(this);
    control.init(RechnungControl.TYP.RECHNUNG.name() + ".", null, null);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getSuchname());
    left.addInput(control.getMailauswahl());
    left.addInput(control.getSuchVersand());

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getDifferenz());
    middle.addLabelPair("Differenz Limit", control.getDoubleAusw());
    middle.addLabelPair("Ohne Abbucher", control.getOhneAbbucher());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getDatumvon());
    right.addInput(control.getDatumbis());

    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(vor);
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck.setToolTipText("Datumsbereich zurück");
    vor.setToolTipText("Datumsbereich vowärts");

    control.getRechnungList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.RECHNUNG, false, "question-circle.png");
    buttons.paint(this.getParent());
  }

}
