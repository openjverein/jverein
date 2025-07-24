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
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class AuswertungNichtMitgliedView extends AbstractView
{
  final MitgliedControl control = new MitgliedControl(this);

  public AuswertungNichtMitgliedView() throws RemoteException
  {
    control.init("nichtmitglied.", "nichtzusatzfeld.", "nichtzusatzfelder.");
  }

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Auswertung Nicht-Mitglieder");

    LabelGroup group = new LabelGroup(getParent(), "Filter");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());

    left.addInput(control.getMailauswahl());
    SelectInput mitgliedsTyp = control
        .getSuchMitgliedstyp(Mitgliedstypen.NICHTMITGLIED);
    mitgliedsTyp.setPleaseChoose("Bitte auswählen");
    left.addInput(mitgliedsTyp);
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
    buttons.addButton(control.getStartAdressAuswertungButton());
    buttons.paint(getParent());
  }
}
