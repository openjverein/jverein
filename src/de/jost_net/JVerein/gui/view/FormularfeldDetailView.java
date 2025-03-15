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

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.FormularfeldAction;
import de.jost_net.JVerein.gui.control.FormularfeldControl;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class FormularfeldDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Formularfeld");
    Formularfeld ff = (Formularfeld) getCurrentObject();

    final FormularfeldControl control = new FormularfeldControl(this,
        ff.getFormular());

    LabelGroup group = new LabelGroup(getParent(), "Formularfeld");
    group.addLabelPair("Name", control.getName());
    group.addLabelPair("Seite", control.getSeite());
    group.addLabelPair("Von links", control.getX());
    group.addLabelPair("Von unten", control.getY());
    group.addLabelPair("Schriftart", control.getFont());
    group.addLabelPair("Schriftgr��e", control.getFontsize());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.FORMULARE, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          control.handleStore();
          GUI.startPreviousView();
          GUI.getStatusBar().setSuccessText("Formularfeld gespeichert");
        }
        catch (ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "document-save.png");

    buttons.addButton(new Button("Speichern und neu", context -> {
      try
      {
        control.handleStore();

        new FormularfeldAction().handleAction(ff.getFormular());
        GUI.getStatusBar().setSuccessText("Formularfeld gespeichert");
      }
      catch (ApplicationException | RemoteException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }, null, false, "go-next.png"));

    buttons.paint(this.getParent());
  }
}
