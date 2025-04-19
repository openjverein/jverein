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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.control.BuchungsartControl;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class BuchungsartDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Buchungsart");

    final BuchungsartControl control = new BuchungsartControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Buchungsart");
    group.addLabelPair("Nummer", control.getNummer(true));
    group.addLabelPair("Bezeichnung", control.getBezeichnung());
    group.addLabelPair("Art", control.getArt());
    group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    group.addLabelPair("Spende", control.getSpende());
    group.addLabelPair("Abschreibung", control.getAbschreibung());
    if (Einstellungen.getEinstellung().getOptiert())
    {
      group.addLabelPair("Steuer", control.getSteuer());
    }
    // TODO Jo Dokumentation nachpflegen
    group.addLabelPair("Status", control.getStatus());
    group.addLabelPair("Suchbegriff", control.getSuchbegriff());
    group.addLabelPair("Suchbegriff ist ein regulärer Ausdruck",
        control.getRegexp());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGSART, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        try
        {
          control.handleStore();
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

        new NewAction(BuchungsartDetailView.class, Buchungsart.class, true)
            .handleAction(null);
        GUI.getStatusBar().setSuccessText("Buchungsart gespeichert");
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }, null, false, "go-next.png"));

    buttons.paint(this.getParent());
  }
}
