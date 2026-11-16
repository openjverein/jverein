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

import de.jost_net.JVerein.gui.control.BuchungSplitDetailControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;

public class SplitbuchungDetailView extends AbstractDetailView
{
  private BuchungSplitDetailControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Splitbuchungen");

    control = new BuchungSplitDetailControl(this);

    int anzahl = control.getSplitBuchung().getAnzahl();
    if (anzahl > 1)
    {
      InfoPanel info = new InfoPanel();
      info.setText("Es werden " + anzahl + " Splitbuchungen erzeugt.\n"
          + "Geänderte Zwecke oder Kommentare in Splitpositionen werden bei allen Buchungen übernommen.");
      info.setTitle("Info");
      info.setIcon("gtk-info.png");
      info.paint(getParent());
    }
    control.getSplitBuchungsList().paint(getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new HelpButton(DokumentationUtil.SPLITBUCHUNG));

    buttons.addButton(control.getNewButton());
    buttons.addButton(control.getAufloesenButton());
    buttons.addButton(control.getSammelueberweisungButton());
    buttons.addButton(control.getSaveButton());

    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
