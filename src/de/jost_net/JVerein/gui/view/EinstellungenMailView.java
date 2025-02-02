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
import de.jost_net.JVerein.gui.control.EinstellungControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class EinstellungenMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Mail");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent());

    ColumnLayout cols1 = new ColumnLayout(scrolled.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cols1.getComposite());

    left.addHeadline("Server Einstellungen");
    left.addLabelPair("Server", control.getSmtpServer());
    left.addLabelPair("Port", control.getSmtpPort());
    left.addLabelPair("Benutzer", control.getSmtpAuthUser());
    left.addLabelPair("Passwort", control.getSmtpAuthPwd());
    left.addLabelPair("Absenderadresse", control.getSmtpFromAddress());
    left.addLabelPair("Anzeigename", control.getSmtpFromAnzeigename());
    left.addLabelPair("SSL verwenden", control.getSmtpSsl());
    left.addLabelPair("StartTLS verwenden", control.getSmtpStarttls());
    left.addLabelPair("Pause zwischen Mailversand in Millisek.",
        control.getMailVerzoegerung());
    left.addLabelPair("Immer Cc an Adresse", control.getAlwaysCcTo());
    left.addLabelPair("Immer Bcc an Adresse", control.getAlwaysBccTo());

    SimpleContainer right = new SimpleContainer(cols1.getComposite());
    right.addHeadline("IMAP 'Gesendete'-Ordner");
    right.addLabelPair("Kopie in 'Gesendete'-Ordner IMAP ablegen",
        control.getCopyToImapFolder());
    right.addLabelPair("IMAP Server", control.getImapHost());
    right.addLabelPair("IMAP Port", control.getImapPort());
    right.addLabelPair("IMAP Benutzer", control.getImapAuthUser());
    right.addLabelPair("IMAP Passwort", control.getImapAuthPwd());
    right.addLabelPair("IMAP SSL verwenden", control.getImap_ssl());
    right.addLabelPair("IMAP StartTLS verwenden", control.getImap_starttls());
    right.addLabelPair("IMAP 'Gesendete'-Ordername",
        control.getImapSentFolder());

    LabelGroup einstellung = new LabelGroup(scrolled.getComposite(),
        "Einstellungen");
    einstellung.addLabelPair("Signatur", control.getMailSignatur());
    einstellung.addLabelPair("Anhang", control.getAnhangSpeichern());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_MAIL, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreMail();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
