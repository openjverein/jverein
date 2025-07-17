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
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SollbuchungDetailView extends AbstractDetailView
{
  private SollbuchungControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchung");

    control = new SollbuchungControl(this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent(), 1);
    LabelGroup group = new LabelGroup(scrolled.getComposite(), "Sollbuchung");
    ColumnLayout cols = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cols.getComposite());
    left.addLabelPair("Mitglied", control.getMitglied());
    left.addLabelPair("Datum", control.getDatum());
    left.addLabelPair("Zahlungsweg", control.getZahlungsweg());

    SimpleContainer right = new SimpleContainer(cols.getComposite());
    right.addLabelPair("Zahler", control.getZahler());
    right.addLabelPair("Verwendungszweck", control.getZweck1());
    control.getBetrag().setMandatory(true);
    right.addLabelPair("Betrag", control.getBetrag());

    boolean hasRechnung = control.hasRechnung();

    LabelGroup cont = new LabelGroup(scrolled.getComposite(),
        "Sollbuchungspositionen");
    
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
    cont.addPart(control.getSollbuchungPositionListPart(hasRechnung));

    LabelGroup buch = new LabelGroup(scrolled.getComposite(),
        "Zugeordnete Buchungen");
    buch.addPart(control.getBuchungListPart());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");
    if (!control.getSollbuchung().isNewObject())
    {
      buttons.addButton(control.getZurueckButton());
      buttons.addButton(control.getVorButton());
    }
    Button save = new SaveButton(control);
    save.setEnabled(!hasRechnung);
    buttons.addButton(save);

    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
