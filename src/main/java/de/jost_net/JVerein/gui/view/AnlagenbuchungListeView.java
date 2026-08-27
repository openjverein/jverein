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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.AnlagenbuchungExportAction;
import de.jost_net.JVerein.gui.action.AnlagenbuchungImportAction;
import de.jost_net.JVerein.gui.action.BuchungNeuAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.FilterControl.Kontenfilter;
import de.jost_net.JVerein.gui.control.BuchungsHeaderControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.Filter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;

public class AnlagenbuchungListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Anlagenbuchungen");

    final BuchungsControl control = new BuchungsControl(this,
        Kontenfilter.ANLAGEKONTO);
    control.init("anlagenkonto.", null, null);

    TabFolder folder = new TabFolder(getParent(), SWT.V_SCROLL | SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    // Erster Tab
    TabGroup tabAllgemein = new TabGroup(folder, "Filter", true, 2);
    ColumnLayout cl = new ColumnLayout(tabAllgemein.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    SimpleContainer right = new SimpleContainer(cl.getComposite());

    left.addInput(control.getFilterInput(Filter.KONTO));
    left.addInput(control.getFilterInput(Filter.BUCHUNGSART));
    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      left.addInput(control.getFilterInput(Filter.PROJEKT));
    }
    left.addInput(control.getFilterInput(Filter.SPLITBUCHUNG));

    Input datumVon = control.getFilterInput(Filter.DATUM_VON);
    datumVon.setMandatory(true);
    right.addInput(datumVon);
    Input datumBis = control.getFilterInput(Filter.DATUM_BIS);
    datumBis.setMandatory(true);
    control.setInitVonBis(true);
    right.addInput(datumBis);
    right.addInput(control.getFilterInput(Filter.ENTHALTENER_TEXT));
    right.addInput(control.getFilterInput(Filter.BETRAG));

    ButtonArea buttons1 = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(datumVon, datumBis);
    buttons1.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(datumVon, datumBis);
    buttons1.addButton(vor);
    buttons1.addButton(control.getResetButton());
    buttons1.addButton(control.getSuchenButton());
    tabAllgemein.addButtonArea(buttons1);
    zurueck.setToolTipText("Datumsbereich zurück");
    vor.setToolTipText("Datumsbereich vowärts");

    // Zweiter Tab
    final BuchungsHeaderControl headerControl = new BuchungsHeaderControl(this,
        control);
    TabGroup tabKonto = new TabGroup(folder, "Konto Kenndaten", true, 4);
    ColumnLayout c2 = new ColumnLayout(tabKonto.getComposite(), 2);
    SimpleContainer left2 = new SimpleContainer(c2.getComposite());
    SimpleContainer right2 = new SimpleContainer(c2.getComposite());
    left2.addLabelPair("Konto:", headerControl.getKontoNameInput());
    right2.addLabelPair("Vorjahr", new LabelInput(""));
    left2.addLabelPair("Anfangssaldo:",
        headerControl.getAktJahrAnfangsSaldoInput());
    right2.addLabelPair("Anfangssaldo:",
        headerControl.getVorJahrAnfangsSaldoInput());
    left2.addLabelPair("Einnahmen:", headerControl.getAktJahrEinnahmenInput());
    right2.addLabelPair("Einnahmen:", headerControl.getVorJahrEinnahmenInput());
    left2.addLabelPair("Ausgaben:", headerControl.getAktJahrAusgabenInput());
    right2.addLabelPair("Ausgaben:", headerControl.getVorJahrAusgabenInput());
    left2.addLabelPair("Saldo:", headerControl.getAktJahrSaldoInput());
    right2.addLabelPair("Saldo:", headerControl.getVorJahrSaldoInput());

    control.getTablePart().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ANLAGENBUCHUNGEN, false, "question-circle.png");
    if (!control.getGeldkonto() && !(Boolean) Einstellungen
        .getEinstellung(Property.AFAINJAHRESABSCHLUSS))
      buttons.addButton(control.getAfaButton());
    buttons.addButton("Import", new AnlagenbuchungImportAction(), null, false,
        "file-import.png");
    buttons.addButton(new Button("Export", new AnlagenbuchungExportAction(),
        control, false, "document-save.png"));
    buttons.addButton("Neu", new BuchungNeuAction(control), control, false,
        "document-new.png");
    buttons.paint(this.getParent());

    GUI.getView().addPanelButton(control.exportButton(ExportArt.PDF));
    GUI.getView().addPanelButton(control.exportButton(ExportArt.CSV));
    GUI.getView().addPanelButton(control.getSpaltenPanelButton());
  }
}
