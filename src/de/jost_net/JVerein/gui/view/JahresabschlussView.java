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
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

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
    
    LabelGroup group = new LabelGroup(getParent(), "Jahresabschluss", true);
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Von", control.getVon());
    left.addLabelPair("Bis", control.getBis());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    if (Einstellungen.getEinstellung().getMittelverwendung())
    {
      left.addLabelPair("Datum", control.getDatum());
      right.addLabelPair("Name", control.getName());
      right.addLabelPair("Rest Verwendungsr�ckstand Vorjahr",
          control.getVerwendungsrueckstand());
      right.addLabelPair("Zwanghafte satzungsgem��e Weitergabe von Mitteln",
          control.getZwanghafteWeitergabe());
    }
    else
    {
      right.addLabelPair("Datum", control.getDatum());
      right.addLabelPair("Name", control.getName());
    }
    left.addLabelPair("Anfangsbest�nde Folgejahr",
        control.getAnfangsbestaende());
    if (Einstellungen.getEinstellung().getAfaInJahresabschluss())
      right.addLabelPair("Erzeuge Abschreibungen", control.getAfaberechnung());
    group.addPart(control.getJahresabschlussSaldo());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.JAHRESABSCHLUSS, false, "question-circle.png");
    if (Einstellungen.getEinstellung().getMittelverwendung())
    {
      buttons.addButton(control.getZurueck());
    }
    Button save = new Button("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    save.setEnabled(control.isSaveEnabled());
    buttons.addButton(save);
    buttons.paint(this.getParent());
  }
}
