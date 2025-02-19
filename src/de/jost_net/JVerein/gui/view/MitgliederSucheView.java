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
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;

public class MitgliederSucheView extends AbstractMitgliedSucheView
{

  public MitgliederSucheView() throws RemoteException
  {
    control.init("mitglied.", "zusatzfeld.", "zusatzfelder.");
    control.getSuchMitgliedstyp(Mitgliedstypen.MITGLIED).getValue();
  }

  @Override
  public String getTitle()
  {
    return "Mitglieder";
  }

  @Override
  public void getFilter() throws RemoteException
  {
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getMitgliedStatus());
    if (Einstellungen.getEinstellung().getExterneMitgliedsnummer())
      left.addInput(control.getSuchExterneMitgliedsnummer());
    else
      left.addInput(control.getSuchMitgliedsnummer());
    DialogInput eigenschaftenInput = control.getEigenschaftenAuswahl();
    left.addInput(eigenschaftenInput);
    control.updateEigenschaftenAuswahlTooltip();
    left.addInput(control.getBeitragsgruppeAusw());
    if (Einstellungen.getEinstellung().hasZusatzfelder())
    {
      DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
      left.addInput(zusatzfelderInput);
      control.updateZusatzfelderAuswahlTooltip();
    }

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getSuchname());
    middle.addInput(control.getGeburtsdatumvon());
    middle.addInput(control.getGeburtsdatumbis());
    middle.addInput(control.getSuchGeschlecht());
    middle.addInput(control.getStichtag());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getEintrittvon());
    right.addInput(control.getEintrittbis());
    right.addInput(control.getAustrittvon());
    right.addInput(control.getAustrittbis());
    right.addInput(control.getMailauswahl());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getProfileButton());
    buttons.addButton(control.getResetButton());
    buttons.addButton(control.getSuchenButton());
    group.addButtonArea(buttons);
  }

  @Override
  public Action getDetailAction()
  {
    return new MitgliedDetailAction();
  }

  @Override
  public Button getHilfeButton()
  {
    return new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSUCHE, false, "question-circle.png");
  }

}