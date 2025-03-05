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
import de.jost_net.JVerein.gui.control.SollbuchungPositionControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class SollbuchungPositionDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Sollbuchungsposition");

    final SollbuchungPositionControl control = new SollbuchungPositionControl(
        this);

    LabelGroup group = new LabelGroup(getParent(), "Sollbuchungsposition");
    group.addLabelPair("Datum", control.getDatum());
    group.addLabelPair("Zweck", control.getZweck());
    group.addLabelPair("Betrag", control.getBetrag());
    group.addLabelPair("Buchungsart", control.getBuchungsart());
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (control.handleStore())
        {
          try
          {
            GUI.startView(SollbuchungDetailView.class.getName(),
                control.getPosition().getSollbuchung());
          }
          catch (RemoteException e)
          {
            //
          }
          GUI.getStatusBar().setSuccessText("Sollbuchungsposition gespeichert");
        }
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
