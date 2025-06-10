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
import de.jost_net.JVerein.gui.action.SollbuchungPositionNeuAction;
import de.jost_net.JVerein.gui.control.SollbuchungPositionControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

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
    if (Einstellungen.getEinstellung().getSteuerInBuchung())
    {
      group.addLabelPair("Steuer", control.getSteuer());
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
        try
        {
          control.handleStore();
          GUI.startPreviousView();
          GUI.getStatusBar().setSuccessText("Sollbuchungsposition gespeichert");
        }
        catch (ApplicationException | RemoteException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "document-save.png");

    buttons.addButton(new Button("Speichern und neu", context -> {
      try
      {
        control.handleStore();

        new SollbuchungPositionNeuAction()
            .handleAction(control.getPosition().getSollbuchung());
        GUI.getStatusBar().setSuccessText("Sollbuchungsposition gespeichert");
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
      catch (RemoteException e)
      {
        GUI.getStatusBar()
            .setErrorText("Fehler beim Speichern der Sollbuchungsposition: "
                + e.getMessage());
      }
    }, null, false, "go-next.png"));
    buttons.paint(this.getParent());
  }
}
