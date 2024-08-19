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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.PreNotificationControl;
import de.jost_net.JVerein.keys.FormularArt;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class PreNotificationView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("SEPA Pre-Notification");

    final PreNotificationControl control = new PreNotificationControl(this);
    control.init("prenotification." , null, null);

    if (this.getCurrentObject() == null)
    {
      LabelGroup group = new LabelGroup(getParent(), "Filter");
      group.addInput(control.getAbrechnungslaufAusw(10));
    }
    
    TabFolder folder = control.getFolder(getParent());
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tabMailPDF = new TabGroup(folder, "Mail + PDF");
    SimpleContainer grtabMailPDF = new SimpleContainer(tabMailPDF.getComposite(), true);

    grtabMailPDF.addHeadline("Parameter");
    grtabMailPDF.addInput(control.getOutput());
    grtabMailPDF.addInput(control.getPdfModus());
    grtabMailPDF.addLabelPair("Formular",
        control.getFormular(FormularArt.SEPA_PRENOTIFICATION));

    grtabMailPDF.addHeadline("Mail");

    grtabMailPDF.addInput(control.getBetreff());
    grtabMailPDF.addInput(control.getTxt());

    ButtonArea buttons1 = new ButtonArea();
    buttons1.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.PRENOTIFICATION, false, "question-circle.png");
    buttons1.addButton(new Button("Mail-Vorlage", new MailVorlageZuweisenAction(),
        control, false, "view-refresh.png"));
    buttons1.addButton(control.getStartButton(this.getCurrentObject()));
    grtabMailPDF.addButtonArea(buttons1);

    TabGroup tab2 = new TabGroup(folder, "1 ct-Überweisung");
    SimpleContainer grtab2 = new SimpleContainer(tab2.getComposite(), true);

    grtab2.addInput(control.getct1Ausgabe());
    grtab2.addInput(control.getAusfuehrungsdatum());
    grtab2.addInput(control.getVerwendungszweck());
    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.PRENOTIFICATION, false, "question-circle.png");
    buttons2.addButton(
        control.getStart1ctUeberweisungButton(this.getCurrentObject()));
    grtab2.addButtonArea(buttons2);
  }
}
