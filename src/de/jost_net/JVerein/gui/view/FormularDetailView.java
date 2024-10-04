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
import de.jost_net.JVerein.gui.action.FormularAnzeigeAction;
import de.jost_net.JVerein.gui.action.FormularfeldAction;
import de.jost_net.JVerein.gui.action.FormularfelderExportAction;
import de.jost_net.JVerein.gui.action.FormularfelderImportAction;
import de.jost_net.JVerein.gui.control.FormularControl;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class FormularDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Formular");

    final FormularControl control = new FormularControl(this, (Formular) getCurrentObject());

    LabelGroup group = new LabelGroup(getParent(), "Formular");
    group.addLabelPair("Bezeichnung", control.getBezeichnung(true));
    group.addLabelPair("Art", control.getArt());
    group.addLabelPair("Datei", control.getDatei());
    group.addLabelPair("Fortlaufende Nr.", control.getZaehler());
    group.addLabelPair("Formularverknüpfung", control.getFormlink());
    
    LabelGroup cont = new LabelGroup(getParent(), "Formularfelder", true);
    control.getFormularfeldList().paint(cont.getComposite());
    
    ButtonArea buttons1 = new ButtonArea();
    buttons1.addButton("Export", new FormularfelderExportAction(),
        getCurrentObject(), false, "document-save.png");
    buttons1.addButton("Import", new FormularfelderImportAction(control),
        getCurrentObject(), false, "file-import.png");
    buttons1.addButton("Neu", new FormularfeldAction(), getCurrentObject(),
        false, "document-new.png");
    cont.addButtonArea(buttons1);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FORMULARE, false, "question-circle.png");
    buttons.addButton("Anzeigen", new FormularAnzeigeAction(),
        getCurrentObject(), false, "edit-copy.png");

    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
