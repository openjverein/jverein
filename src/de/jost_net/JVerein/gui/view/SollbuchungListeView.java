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
import de.jost_net.JVerein.gui.action.MitgliedskontoExportAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoSollbuchungEditAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoExportAction.EXPORT_TYP;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SollbuchungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchungen");

    final MitgliedskontoControl control = new MitgliedskontoControl(this);
    control.init("sollbuchung.", null, null);
    
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getSuchname());
    left.addInput(control.getDifferenz());
    left.addLabelPair("Ohne Abbucher", control.getOhneAbbucher());
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getDatumvon());
    right.addInput(control.getDatumbis());
    right.addInput(control.getMailauswahl());
    
    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);

    control.getMitgliedskontoList(new MitgliedskontoSollbuchungEditAction(),
        new SollbuchungMenu(), false).paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");
    buttons.addButton(new Button("Export",
        new MitgliedskontoExportAction(EXPORT_TYP.MITGLIEDSKONTO, null),
        control, false, "document-save.png"));
    buttons.addButton("Neu", new MitgliedskontoSollbuchungEditAction(), 
        control, false, "document-new.png");
    buttons.paint(this.getParent());
  }
}
