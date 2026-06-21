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
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
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
    TabFolder folder = new TabFolder(group.getComposite(),
        SWT.V_SCROLL | SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    // Erster Tab
    TabGroup tab1 = new TabGroup(folder, "Allgemein", true, 3);
    SimpleContainer left = new SimpleContainer(tab1.getComposite());
    left.addInput(control.getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED));
    left.addInput(control.getSuchname());

    SimpleContainer middle = new SimpleContainer(tab1.getComposite());
    middle.addInput(control.getSuchGeschlecht());
    middle.addInput(control.getMailauswahl());

    SimpleContainer right = new SimpleContainer(tab1.getComposite());
    DialogInput eigenschaftenInput = control.getEigenschaftenAuswahl();
    right.addInput(eigenschaftenInput);
    control.updateEigenschaftenAuswahlTooltip();
    if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
    {
      DialogInput zusatzfelderInput = control.getZusatzfelderAuswahl();
      right.addInput(zusatzfelderInput);
      control.updateZusatzfelderAuswahlTooltip();
    }

    // Zeiter Tab
    TabGroup tab2 = new TabGroup(folder, "Datum", true, 1);
    SimpleContainer left2 = new SimpleContainer(tab2.getComposite());
    left2.addInput(control.getGeburtsdatumvon());
    left2.addInput(control.getGeburtsdatumbis());

    // Dritter Tab
    TabGroup tab3 = new TabGroup(folder, "Mitgliedskonto", true, 2);
    SimpleContainer left3 = new SimpleContainer(tab3.getComposite());
    left3.addInput(control.getDifferenz());
    left3.addLabelPair("Differenz Limit", control.getDoubleAusw());

    SimpleContainer right3 = new SimpleContainer(tab3.getComposite());
    right3.addInput(control.getDatumvon());
    right3.addInput(control.getDatumbis());

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
