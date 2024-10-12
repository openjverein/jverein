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
import de.jost_net.JVerein.gui.control.JahresabschlussControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.LabelGroup;

public class JahresabschlussView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Jahresabschluss");

    final JahresabschlussControl control = new JahresabschlussControl(this);

    String text = control.getInfo();
    if (text != null && !text.isEmpty())
    {
      InfoPanel   info = new InfoPanel();
      info.setText(text);
      info.setTitle("Info");
      info.setIcon("gtk-info.png");
      info.paint(getParent());
    }
    
    LabelGroup group = new LabelGroup(getParent(), "Jahresabschluss");
    group.addLabelPair("Von", control.getVon());
    group.addLabelPair("Bis", control.getBis());
    group.addLabelPair("Datum", control.getDatum());
    group.addLabelPair("Name", control.getName());
    group.addLabelPair("Anfangsbestände Folgejahr",
        control.getAnfangsbestaende());
    if (Einstellungen.getEinstellung().getAfaInJahresabschluss())
      group.addLabelPair("Erzeuge Abschreibungen", control.getAfaberechnung());
    group.addPart(control.getJahresabschlussSaldo());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.JAHRESABSCHLUSS, false, "question-circle.png");
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
