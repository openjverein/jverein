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
import de.jost_net.JVerein.gui.action.MailNeuAction;
import de.jost_net.JVerein.gui.control.MailControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class MailListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mails");

    MailControl control = new MailControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Mail Empfänger", control.getSuchname());
    left.addLabelPair("Betreff", control.getSuchtext());

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addLabelPair("Bearbeitung von", control.getEingabedatumvon());
    middle.addLabelPair("Bearbeitung bis", control.getEingabedatumbis());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Versand von", control.getDatumvon());
    right.addLabelPair("Versand bis", control.getDatumbis());

    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);

    control.getMailList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MAIL, false, "question-circle.png");
    buttons.addButton("Neu", new MailNeuAction(), null, false,
        "document-new.png");
    buttons.paint(this.getParent());
  }
}
