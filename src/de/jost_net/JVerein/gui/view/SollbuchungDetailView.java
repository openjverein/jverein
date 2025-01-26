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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.SollbuchungPositionNeuAction;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class SollbuchungDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchung");

    final MitgliedskontoControl control = new MitgliedskontoControl(this);
    LabelGroup grBuchung = new LabelGroup(getParent(), "Sollbuchung");
    grBuchung.addLabelPair("Mitglied", control.getMitglied());
    grBuchung.addLabelPair("Zahler", control.getZahler());
    grBuchung.addLabelPair("Datum", control.getDatum());
    grBuchung.addLabelPair("Verwendungszweck", control.getZweck1());
    grBuchung.addLabelPair("Zahlungsweg", control.getZahlungsweg());
    control.getBetrag().setMandatory(true);
    grBuchung.addLabelPair("Betrag", control.getBetrag());

    boolean hasRechnung = control.hasRechnung();
    
    LabelGroup cont = new LabelGroup(getParent(), "Sollbuchungspositionen", true);
    
    ButtonArea buttons1 = new ButtonArea();
    Button neu = new Button("Neu", new SollbuchungPositionNeuAction(),
        getCurrentObject(), false, "document-new.png");
    neu.setEnabled(!hasRechnung);
    buttons1.addButton(neu);

    // Diese Zeilen werden gebraucht um die Buttons rechts zu plazieren
    GridLayout layout = new GridLayout();
    Composite comp = new Composite(cont.getComposite(), SWT.NONE);
    comp.setLayout(layout);
    comp.setLayoutData(new GridData(GridData.END));

    buttons1.paint(cont.getComposite());
    cont.addPart(control.getBuchungenList(hasRechnung));

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");

    Button save = new Button("Speichern", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    save.setEnabled(!hasRechnung);
    buttons.addButton(save);

    buttons.paint(this.getParent());
  }
}
