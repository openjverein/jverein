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
import de.jost_net.JVerein.gui.menu.Mitgliedskonto2Menu;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class SollbuchungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchungen");

    final MitgliedskontoControl control = new MitgliedskontoControl(this);
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    group.addInput(control.getSuchName());
    group.addLabelPair("Von",
        control.getVondatum(MitgliedskontoControl.DATUM_MITGLIEDSKONTO));
    group.addLabelPair("Bis",
        control.getBisdatum(MitgliedskontoControl.DATUM_MITGLIEDSKONTO));
    group.addLabelPair("Differenz", control.getDifferenz());
    
    ButtonArea button = new ButtonArea();
    Button suchen = new Button("Suchen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        control.refreshMitgliedskontoList();
      }
    }, null, true, "search.png");
    button.addButton(suchen);
    group.addButtonArea(button);

    control.getMitgliedskontoList(new MitgliedskontoSollbuchungEditAction(),
        new Mitgliedskonto2Menu(), false).paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");
    buttons.addButton(new Button("Export",
        new MitgliedskontoExportAction(EXPORT_TYP.MITGLIEDSKONTO, null),
        control, false, "document-save.png"));
    buttons.paint(this.getParent());
  }
}
