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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MittelverwendungControl;
import de.jost_net.JVerein.gui.parts.QuickAccessPart;
import de.jost_net.JVerein.gui.parts.VonBisPart;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.TabGroup;

public class MittelverwendungListeView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mittelverwendung");

    final MittelverwendungControl control = new MittelverwendungControl(this);
  
    VonBisPart vpart = new VonBisPart(control, false);
    vpart.paint(this.getParent());
    
    QuickAccessPart qpart = new QuickAccessPart(control, false);
    qpart.paint(this.getParent());

    final TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.addSelectionListener(new SelectionAdapter()
    {

      @Override
      public void widgetSelected(SelectionEvent evt)
      {
        TabItem item = folder.getSelection()[0];
        if (item.getText().startsWith("Mittelverwendungsreport (Zufluss"))
        {
          control.setSelectedTab(MittelverwendungControl.FLOW_REPORT);
        }
        else if (item.getText().startsWith("Mittelverwendungsreport (Saldo"))
        {
          control.setSelectedTab(MittelverwendungControl.SALDO_REPORT);
        }
      }
    });

    // Die verschiedenen Tabs
    TabGroup mittelverwendungFlow = new TabGroup(folder,
        "Mittelverwendungsreport (Zufluss basiert)", true, 1);
    control.getFlowTable().paint(mittelverwendungFlow.getComposite());
    TabGroup mittelverwendungSaldo = new TabGroup(folder,
        "Mittelverwendungsreport (Saldo basiert)", true, 1);
    control.getSaldoTable().paint(mittelverwendungSaldo.getComposite());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITTELVERWENDUNG, false, "question-circle.png");
    buttons.addButton(control.getCSVExportButton());
    buttons.addButton(control.getPDFExportButton());
    buttons.paint(this.getParent());
  }
}
