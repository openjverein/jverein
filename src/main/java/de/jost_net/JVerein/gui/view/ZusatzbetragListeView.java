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

import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeImportAction;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.NewButton;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class ZusatzbetragListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Zusatzbeträge");

    final ZusatzbetragControl control = new ZusatzbetragControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    group.addInput(control.getFilterInput(Filter.AUSFUEHRUNG));
    Input von = control.getFilterInput(Filter.AUSFUEHRUNG_VON);
    group.addInput(von);
    Input bis = control.getFilterInput(Filter.AUSFUEHRUNG_BIS);
    group.addInput(bis);

    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(von, bis);
    fbuttons.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(von, bis);
    fbuttons.addButton(vor);
    fbuttons.addButton(control.getProfileButton(this));
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck.setToolTipText("Datumsbereich zurück");
    vor.setToolTipText("Datumsbereich vowärts");

    control.getTablePart().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new HelpButton(DokumentationUtil.ZUSATZBETRAEGE));
    buttons.addButton("Vorlagen",
        new StartViewAction(ZusatzbetragVorlageListeView.class), null, false,
        "euro-sign.png");
    buttons.addButton("Import", new ZusatzbetraegeImportAction(), null, false,
        "file-import.png");
    buttons.addButton(new NewButton(
        new NewAction(ZusatzbetragDetailView.class, Zusatzbetrag.class)));
    buttons.paint(this.getParent());

    GUI.getView().addPanelButton(control.exportButton(ExportArt.PDF));
    GUI.getView().addPanelButton(control.exportButton(ExportArt.CSV));
    GUI.getView().addPanelButton(control.getSpaltenPanelButton());
  }
}
