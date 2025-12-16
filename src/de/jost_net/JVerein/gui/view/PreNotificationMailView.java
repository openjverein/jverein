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
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageUebernehmenAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.PreNotificationControl;
import de.jost_net.JVerein.gui.control.PreNotificationControl.TYP;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class PreNotificationMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Pre-Notification");

    final PreNotificationControl control = new PreNotificationControl(this);
    control.init("prenotification.", null, null);

    if (getCurrentObject() == null
        || getCurrentObject() instanceof Abrechnungslauf)
    {
      LabelGroup group = new LabelGroup(getParent(), "Filter");
      group.addInput(control.getMailauswahl());
      if (getCurrentObject() == null)
      {
        group.addInput(control.getAbrechnungslaufAusw(10));
      }
    }
    else
    {
      SimpleContainer cont1 = new SimpleContainer(getParent(), false);
      cont1.addHeadline("Info");
      cont1.addInput(control.getInfo());
    }

    TabFolder folder = control.getFolder(getParent());
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tabMailPDF = new TabGroup(folder, "Mail + PDF");
    SimpleContainer grtabMailPDF = new SimpleContainer(
        tabMailPDF.getComposite(), true);

    grtabMailPDF.addHeadline("Parameter");
    grtabMailPDF.addInput(control.getAusgabeart());
    grtabMailPDF.addLabelPair("Formular",
        control.getFormular(FormularArt.SEPA_PRENOTIFICATION));

    grtabMailPDF.addHeadline("Mail");

    grtabMailPDF.addInput(control.getBetreff());
    grtabMailPDF.addInput(control.getTxt());

    Map<String, Object> map = LastschriftMap.getDummyMap(null);
    map = MitgliedMap.getDummyMap(map);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons1 = new ButtonArea();
    buttons1.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.PRENOTIFICATION, false, "question-circle.png");
    buttons1.addButton(new Button("Mail-Vorlage",
        new MailVorlageZuweisenAction(), control, false, "view-refresh.png"));
    buttons1.addButton("Variablen anzeigen",
        new InsertVariableDialogAction(map), control, false, "bookmark.png");
    buttons1
        .addButton(new Button("Vorschau", new MailTextVorschauAction(map, true),
            control, false, "edit-copy.png"));
    buttons1.addButton(
        new Button("Als Vorlage übernehmen", new MailVorlageUebernehmenAction(),
            control, false, "document-new.png"));
    buttons1.addButton(control.getDruckMailMitgliederButton(
        this.getCurrentObject(), TYP.DRUCKMAIL.toString()));
    buttons1.addButton(control.getStartButton(this.getCurrentObject()));
    addButtonArea(buttons1, grtabMailPDF.getComposite());

    TabGroup tab2 = new TabGroup(folder, "1 ct-Überweisung");
    SimpleContainer grtab2 = new SimpleContainer(tab2.getComposite(), true);

    grtab2.addInput(control.getct1Ausgabe());
    grtab2.addInput(control.getAusfuehrungsdatum());
    grtab2.addInput(control.getVerwendungszweck());
    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.PRENOTIFICATION, false, "question-circle.png");
    buttons2.addButton(control.getDruckMailMitgliederButton(
        this.getCurrentObject(), TYP.CENT1.toString()));
    buttons2.addButton(
        control.getStart1ctUeberweisungButton(this.getCurrentObject()));
    addButtonArea(buttons2, grtab2.getComposite());
  }

  /**
   * Fuegt eine neue ButtonArea ohne Seperator hinzu.
   * 
   * @param buttonArea
   *          die hinzuzufuegende Button-Area.
   * @param composite
   *          in den gezeichnet werden soll Code ist aus
   *          de.willuhn.jameica.gui.util.Container kopiert
   */
  public void addButtonArea(ButtonArea buttonArea, Composite composite)
  {
    try
    {
      final GridData g = new GridData(
          GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
      g.horizontalSpan = 2;
      final Composite comp = new Composite(composite, SWT.NONE);
      comp.setLayoutData(g);

      final GridLayout gl = new GridLayout();
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      comp.setLayout(gl);
      buttonArea.paint(comp);
    }
    catch (RemoteException e)
    {
      Logger.error("error while adding button area", e);
      Application.getMessagingFactory()
          .sendMessage(new StatusBarMessage(
              Application.getI18n().tr("Fehler beim Anzeigen des Buttons."),
              StatusBarMessage.TYPE_ERROR));
    }
  }
}
