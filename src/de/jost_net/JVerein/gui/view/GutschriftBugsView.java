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
import de.jost_net.JVerein.gui.control.GutschriftBugsControl;
import de.jost_net.JVerein.gui.control.GutschriftControl;
import de.jost_net.JVerein.gui.dialogs.GutschriftDialog;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.logging.Logger;

public class GutschriftBugsView extends AbstractView
{
  private GutschriftControl gcontrol;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Gutschrift Fehler");

    GutschriftBugsControl control = new GutschriftBugsControl(this);

    control.getBugsList().paint(this.getParent());

    gcontrol = (GutschriftControl) getCurrentObject();

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.GUTSCHRIFT, false, "question-circle.png");
    buttons.addButton("Zum Dialog zurück", c -> {
      try
      {
        GutschriftDialog dialog = new GutschriftDialog(
            gcontrol.getProviderArray(),
            gcontrol.getProviderArray()[0] instanceof Mitglied, false);
        dialog.open();
      }
      catch (Exception e)
      {
        String fehler = "Fehler beim Datenbank Zugriff!";
        Logger.error(fehler, e);
      }
    }, null, false, "go-next.png");
    buttons.paint(this.getParent());
  }
}
