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
import de.jost_net.JVerein.gui.action.FormularAnzeigeAction;
import de.jost_net.JVerein.gui.action.FormularfeldAction;
import de.jost_net.JVerein.gui.action.FormularfelderExportAction;
import de.jost_net.JVerein.gui.action.FormularfelderImportAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.gui.control.FormularControl;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class FormularDetailView extends AbstractDetailView
{
  private FormularControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Formular");

    control = new FormularControl(this, (Formular) getCurrentObject());

    LabelGroup group = new LabelGroup(getParent(), "Formular");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Bezeichnung", control.getBezeichnung(true));
    left.addLabelPair("Art", control.getArt());
    left.addLabelPair("Datei", control.getDatei());
    
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Fortlaufende Nr.", control.getZaehler());
    right.addLabelPair("Formularverknüpfung", control.getFormlink());
    
    LabelGroup cont = new LabelGroup(getParent(), "Formularfelder", true);
    
    ButtonArea buttons1 = new ButtonArea();
    buttons1.addButton("Export", new FormularfelderExportAction(),
        getCurrentObject(), false, "document-save.png");
    buttons1.addButton("Import", new FormularfelderImportAction(control),
        getCurrentObject(), false, "file-import.png");
    buttons1.addButton("Neu", new FormularfeldAction(), getCurrentObject(),
        false, "document-new.png");

    // Diese Zeilen werden gebraucht um die Buttons rechts zu plazieren
    GridLayout layout = new GridLayout();
    Composite comp = new Composite(cont.getComposite(), SWT.NONE);
    comp.setLayout(layout);
    comp.setLayoutData(new GridData(GridData.END));
    
    buttons1.paint(cont.getComposite());
    
    cont.addPart(control.getFormularfeldList());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FORMULARE, false, "question-circle.png");
    buttons.addButton("Anzeigen", new FormularAnzeigeAction(),
        getCurrentObject(), false, "edit-copy.png");

    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
