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

import de.jost_net.JVerein.gui.action.ArbeitseinsatzAbrechnenAction;
import de.jost_net.JVerein.gui.action.ArbeitseinsatzAuswertungAction;
import de.jost_net.JVerein.gui.action.ArbeitseinsatzZusatzbetraegeAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.control.ArbeitseinsatzControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class ArbeitseinsatzListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Arbeitseinsätze");

    final ArbeitseinsatzControl control = new ArbeitseinsatzControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getFilterInput(Filter.NAME));
    left.addInput(control.getFilterInput(Filter.BEMERKUNG));

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    Input von = control.getFilterInput(Filter.DATUM_VON);
    right.addInput(von);
    Input bis = control.getFilterInput(Filter.DATUM_BIS);
    right.addInput(bis);

    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(von, bis);
    fbuttons.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(von, bis);
    fbuttons.addButton(vor);
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck.setToolTipText("Datumsbereich zurück");
    vor.setToolTipText("Datumsbereich vowärts");

    control.getTablePart().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ARBEITSEINSATZ, false, "question-circle.png");
    buttons.addButton("Auswertung", new ArbeitseinsatzAuswertungAction(),
        control, false, "screwdriver.png");
    buttons.addButton("Zusatzbeträge generieren",
        new ArbeitseinsatzZusatzbetraegeAction(), control, false,
        "euro-sign.png");
    buttons.addButton("Abrechnung", new ArbeitseinsatzAbrechnenAction(),
        control, false, "lastschrift.png");
    buttons.addButton("Neu",
        new NewAction(ArbeitseinsatzDetailView.class, Arbeitseinsatz.class),
        control, false, "document-new.png");
    buttons.paint(this.getParent());

    GUI.getView().addPanelButton(control.exportButton(ExportArt.PDF));
    GUI.getView().addPanelButton(control.exportButton(ExportArt.CSV));
    GUI.getView().addPanelButton(control.getSpaltenPanelButton());
  }
}
