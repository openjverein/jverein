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
import de.jost_net.JVerein.gui.control.DbBereinigenControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class DbBereinigenView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Datenbank-Bereinigung");

    final DbBereinigenControl control = new DbBereinigenControl(this);

    ScrolledContainer cl = new ScrolledContainer(getParent(), 1);

    LabelGroup grouprechnungen = new LabelGroup(cl.getComposite(),
        "Rechnungen");
    ColumnLayout rcl = new ColumnLayout(grouprechnungen.getComposite(), 2);
    SimpleContainer rleft = new SimpleContainer(rcl.getComposite());
    rleft.addLabelPair("Löschen", control.getRechnungenLoeschen());
    SimpleContainer rright = new SimpleContainer(rcl.getComposite());
    rright.addLabelPair("Rechnungsdatum älter als",
        control.getDatumAuswahlRechnungen());

    LabelGroup groupspendenbescheinigungen = new LabelGroup(cl.getComposite(),
        "Spendenbescheinigungen");
    ColumnLayout scl = new ColumnLayout(
        groupspendenbescheinigungen.getComposite(), 2);
    SimpleContainer sleft = new SimpleContainer(scl.getComposite());
    sleft.addLabelPair("Löschen", control.getSpendenbescheinigungenLoeschen());
    SimpleContainer sright = new SimpleContainer(scl.getComposite());
    sright.addLabelPair("Spendedatum älter als",
        control.getDatumAuswahlSpendenbescheinigungen());

    LabelGroup groupbuchungen = new LabelGroup(cl.getComposite(), "Buchungen");
    ColumnLayout bcl = new ColumnLayout(groupbuchungen.getComposite(), 2);
    SimpleContainer bleft = new SimpleContainer(bcl.getComposite());
    bleft.addLabelPair("Löschen", control.getBuchungenLoeschen());
    SimpleContainer bright = new SimpleContainer(bcl.getComposite());
    bright.addLabelPair("Datum älter als", control.getDatumAuswahlBuchungen());
    bright.addLabelPair("Zugeordnete Sollbuchungen löschen",
        control.getSollbuchungenLoeschen());

    LabelGroup grouplastschriften = new LabelGroup(cl.getComposite(),
        "Lastschriften");
    ColumnLayout lcl = new ColumnLayout(grouplastschriften.getComposite(), 2);
    SimpleContainer lleft = new SimpleContainer(lcl.getComposite());
    lleft.addLabelPair("Löschen", control.getLastschriftenLoeschen());
    SimpleContainer lright = new SimpleContainer(lcl.getComposite());
    lright.addLabelPair("Fälligkeit älter als",
        control.getDatumAuswahlLastschriften());

    LabelGroup groupabrechnungslauf = new LabelGroup(cl.getComposite(),
        "Abrechnungsläufe");
    ColumnLayout acl = new ColumnLayout(groupabrechnungslauf.getComposite(), 2);
    SimpleContainer aleft = new SimpleContainer(acl.getComposite());
    aleft.addLabelPair("Löschen", control.getAbrechnungslaufLoeschen());
    SimpleContainer aright = new SimpleContainer(acl.getComposite());
    aright.addLabelPair("Fälligkeit älter als",
        control.getDatumAuswahlAbrechnungslauf());

    LabelGroup groupjahresabschluss = new LabelGroup(cl.getComposite(),
        "Jahresabschlüsse");
    ColumnLayout jcl = new ColumnLayout(groupjahresabschluss.getComposite(), 2);
    SimpleContainer jleft = new SimpleContainer(jcl.getComposite());
    jleft.addLabelPair("Löschen", control.getJahresabschlussLoeschen());
    SimpleContainer jright = new SimpleContainer(jcl.getComposite());
    jright.addLabelPair("Bis Datum älter als",
        control.getDatumAuswahlJahresabschluss());

    LabelGroup groupmails = new LabelGroup(cl.getComposite(), "Mails");
    ColumnLayout mcl = new ColumnLayout(groupmails.getComposite(), 2);
    SimpleContainer mleft = new SimpleContainer(mcl.getComposite());
    mleft.addLabelPair("Löschen", control.getMailsLoeschen());
    SimpleContainer mright = new SimpleContainer(mcl.getComposite());
    mright.addLabelPair("Versand älter als", control.getDatumAuswahlMails());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BEREINIGEN, false, "question-circle.png");
    buttons.addButton(control.getStartLoeschenButton());
    buttons.paint(this.getParent());
  }
}
