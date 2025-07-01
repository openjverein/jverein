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

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;

public class NichtMitgliedListeView extends AbstractMitgliedListeView
{
  public NichtMitgliedListeView() throws RemoteException
  {
    control.init("nichtmitglied.", "nichtzusatzfeld.", "nichtzusatzfelder.");
    control.getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED).getValue();
  }

  @Override
  public String getTitle()
  {
    return "Nicht-Mitglieder";
  }

  @Override
  public void getFilter() throws RemoteException
  {
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getSuchname());
    left.addInput(control.getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED));
    DialogInput eigenschaftenInput = control.getEigenschaftenAuswahl();
    left.addInput(eigenschaftenInput);
    control.updateEigenschaftenAuswahlTooltip();
    if (Einstellungen.hasZusatzfelder())
    {
      DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
      left.addInput(zusatzfelderInput);
      control.updateZusatzfelderAuswahlTooltip();
    }
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getGeburtsdatumvon());
    right.addInput(control.getGeburtsdatumbis());
    right.addInput(control.getSuchGeschlecht());
    right.addInput(control.getMailauswahl());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getResetButton());
    buttons.addButton(control.getSuchenButton());
    group.addButtonArea(buttons);
  }

  @Override
  public Action getDetailAction()
  {
    return new NichtMitgliedDetailAction();
  }

  @Override
  public Button getHilfeButton()
  {
    return new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.ADRESSEN, false, "question-circle.png");
  }
}