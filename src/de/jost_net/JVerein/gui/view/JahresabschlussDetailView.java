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
import de.jost_net.JVerein.Einstellungen.Property;
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

public class JahresabschlussDetailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Jahresabschluss");

    final JahresabschlussControl control = new JahresabschlussControl(this);

    String text = control.getInfo();
    if (text != null && !text.isEmpty())
    {
      InfoPanel info = new InfoPanel();
      info.setText(text);
      info.setTitle("Info");
      info.setIcon("gtk-info.png");
      info.paint(getParent());
    }

    LabelGroup group = new LabelGroup(getParent(), "Jahresabschluss");
    ColumnLayout cl;
    if ((Boolean) Einstellungen.getEinstellung(Property.MITTELVERWENDUNG))
    {
      cl = new ColumnLayout(group.getComposite(), 3);
    }
    else
    {
      cl = new ColumnLayout(group.getComposite(), 2);
    }

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Von", control.getDatumvon());
    left.addLabelPair("Bis", control.getDatumbis());
    left.addLabelPair("", control.getAnfangsbestaende());

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addLabelPair("Datum", control.getDatum());
    middle.addLabelPair("Name", control.getName());
    if ((Boolean) Einstellungen.getEinstellung(Property.AFAINJAHRESABSCHLUSS))
    {
      middle.addLabelPair("", control.getAfaberechnung());
    }

    if ((Boolean) Einstellungen.getEinstellung(Property.MITTELVERWENDUNG))
    {
      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addLabelPair("Rest Verwendungsrückstand \naus dem Vorjahr",
          control.getVerwendungsrueckstand());
      right.addLabelPair("Zwanghafte satzungsgemäße\nWeitergabe von Mitteln",
          control.getZwanghafteWeitergabe());
    }

    control.getSaldoList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.JAHRESABSCHLUSS, false, "question-circle.png");

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
