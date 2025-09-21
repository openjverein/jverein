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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;

public class MitgliedListeView extends AbstractMitgliedListeView
{

  public MitgliedListeView() throws RemoteException
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
    TabFolder folder = new TabFolder(group.getComposite(),
        SWT.V_SCROLL | SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    // Erster Tab
    TabGroup tab1 = new TabGroup(folder, "Allgemein", true, 3);
    SimpleContainer left = new SimpleContainer(tab1.getComposite());
    left.addInput(control.getMitgliedStatus());
    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      left.addInput(control.getSuchExterneMitgliedsnummer());
    else
      left.addInput(control.getSuchMitgliedsnummer());
    left.addInput(control.getSuchname());

    SimpleContainer middle = new SimpleContainer(tab1.getComposite());
    middle.addInput(control.getBeitragsgruppeAusw());
    DialogInput eigenschaftenInput = control.getEigenschaftenAuswahl();
    middle.addInput(eigenschaftenInput);
    control.updateEigenschaftenAuswahlTooltip();
    if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
    {
      DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
      middle.addInput(zusatzfelderInput);
      control.updateZusatzfelderAuswahlTooltip();
    }

    SimpleContainer right = new SimpleContainer(tab1.getComposite());
    right.addInput(control.getSuchGeschlecht());
    right.addInput(control.getMailauswahl());
    right.addInput(control.getStichtag());

    // Zeiter Tab
    TabGroup tab2 = new TabGroup(folder, "Datum", true, 3);
    SimpleContainer left2 = new SimpleContainer(tab2.getComposite());
    left2.addInput(control.getGeburtsdatumvon());
    left2.addInput(control.getGeburtsdatumbis());

    SimpleContainer middle2 = new SimpleContainer(tab2.getComposite());
    middle2.addInput(control.getEintrittvon());
    middle2.addInput(control.getEintrittbis());

    SimpleContainer right2 = new SimpleContainer(tab2.getComposite());
    right2.addInput(control.getAustrittvon());
    right2.addInput(control.getAustrittbis());

    // Dritter Tab
    TabGroup tab3 = new TabGroup(folder, "Mitgliedskonto", true, 2);
    SimpleContainer left3 = new SimpleContainer(tab3.getComposite());
    left3.addInput(control.getDifferenz());
    left3.addLabelPair("Differenz Limit", control.getDoubleAusw());

    SimpleContainer right3 = new SimpleContainer(tab3.getComposite());
    right3.addInput(control.getDatumvon());
    right3.addInput(control.getDatumbis());

    // Buttons
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
