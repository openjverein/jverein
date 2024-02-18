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
import de.jost_net.JVerein.gui.action.SplitbuchungAufloesenAction;
import de.jost_net.JVerein.gui.action.SplitbuchungNeuAction;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;

public class SplitBuchungView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Splitbuchungen");

    final BuchungsControl control = new BuchungsControl(this);
    InfoPanel   info = new InfoPanel();
    info.setText(SplitbuchungsContainer.getText());
    info.setTitle("Info");
    info.setIcon("gtk-info.png");
    info.paint(getParent());
    control.getSplitBuchungsList().paint(getParent());
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPLITBUCHUNG, false, "question-circle.png");
    buttons.addButton("Neu", new SplitbuchungNeuAction(),
        control.getCurrentObject(), false, "document-new.png");
    buttons.addButton("Aufl�sen", new SplitbuchungAufloesenAction(control),
        control.getCurrentObject(), false, "unlocked.png");
    buttons.addButton(control.getSammelueberweisungButton());

    buttons.addButton("Speichern", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        try
        {
          if (SplitbuchungsContainer.get().size() != 0)
          {
            SplitbuchungsContainer.store();
            GUI.getStatusBar().setSuccessText(String.format
              ("%s Splitbuchungen gespeichert", SplitbuchungsContainer.getAnzahl()));
          }
          else
          {
            GUI.getStatusBar().setErrorText("Hauptbuchung fehlt");
          }
          control.refreshSplitbuchungen();
        }
        catch (Exception e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "document-save.png");
    buttons.paint(getParent());
  }
}
