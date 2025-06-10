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
import de.jost_net.JVerein.gui.action.WiedervorlageAction;
import de.jost_net.JVerein.gui.control.WiedervorlageControl;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class WiedervorlageListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Wiedervorlagen");
    
    final WiedervorlageControl control = new WiedervorlageControl(this);
    
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getSuchname());
    left.addLabelPair("Vermerk", control.getSuchtext());
    
    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getDatumvon());
    middle.addInput(control.getDatumbis());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Erledigung von", control.getEingabedatumvon());
    right.addLabelPair("Erledigung bis", control.getEingabedatumbis());
    right.addLabelPair("Ohne Erledigung", control.getCheckboxAuswahl());
    
    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck1 = control.getZurueckButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(zurueck1);
    ToolTipButton vor1 = control.getVorButton(control.getDatumvon(),
        control.getDatumbis());
    fbuttons.addButton(vor1);
    ToolTipButton zurueck2 = control.getZurueckButton(
        control.getEingabedatumvon(), control.getEingabedatumbis());
    fbuttons.addButton(zurueck2);
    ToolTipButton vor2 = control.getVorButton(control.getEingabedatumvon(),
        control.getEingabedatumbis());
    fbuttons.addButton(vor2);
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck1.setToolTipText("Datumsbereich zurück");
    vor1.setToolTipText("Datumsbereich vowärts");
    zurueck2.setToolTipText("Erledigung Datumsbereich zurück");
    vor2.setToolTipText("Erledigung Datumsbereich vowärts");
    
    control.getWiedervorlageList().paint(this.getParent());
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIEDERVORLAGE, false, "question-circle.png");
    buttons.addButton("Neu", new WiedervorlageAction(null), 
        null, false, "document-new.png");
    buttons.paint(this.getParent());
  }
}
