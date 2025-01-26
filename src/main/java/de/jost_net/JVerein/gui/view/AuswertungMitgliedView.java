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
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class AuswertungMitgliedView extends AbstractView
{  
  final MitgliedControl control = new MitgliedControl(this);
  
  public AuswertungMitgliedView() throws RemoteException
  {
    control.init("mitglied.", "zusatzfeld.", "zusatzfelder.");
  }

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Auswertung Mitglieder");

    LabelGroup group = new LabelGroup(getParent(), "Filter");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    // left
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getMitgliedStatus());
    if (Einstellungen.getEinstellung().getExterneMitgliedsnummer())
    {
      left.addInput(control.getSuchExterneMitgliedsnummer());
    }
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

    // middle
    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getMailauswahl());
    middle.addInput(control.getGeburtsdatumvon());
    middle.addInput(control.getGeburtsdatumbis());
    middle.addInput(control.getSuchGeschlecht());
    middle.addInput(control.getStichtag(false));

    // right
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getEintrittvon());
    right.addInput(control.getEintrittbis());
    right.addInput(control.getAustrittvon());
    right.addInput(control.getAustrittbis());

    if (Einstellungen.getEinstellung().getSterbedatum())
    {
      right.addInput(control.getSterbedatumvon());
      right.addInput(control.getSterbedatumbis());
    }
    
    ButtonArea filterbuttons = new ButtonArea();
    filterbuttons.addButton(control.getResetButton());
    filterbuttons.addButton(control.getSpeichernButton());
    group.addButtonArea(filterbuttons);

    // Zweite Gruppe: Ausgabe
    LabelGroup group2 = new LabelGroup(getParent(), "Ausgabe");

    ColumnLayout cl2 = new ColumnLayout(group2.getComposite(), 2);
    SimpleContainer left2 = new SimpleContainer(cl2.getComposite());
    SimpleContainer right2 = new SimpleContainer(cl2.getComposite());

    left2.addInput(control.getSortierung());
    left2.addInput(control.getAuswertungUeberschrift());
    right2.addInput(control.getAusgabe());

    // Button-Bereich
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.AUSWERTUNGMITGLIEDER, false, "question-circle.png");
    buttons.addButton(control.getVorlagenCsvEditButton());
    buttons.addButton(control.getStartAuswertungButton());
    buttons.paint(getParent());
  }
}
