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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.util.ApplicationException;

public class NichtMitgliedListeView extends AbstractMitgliedListeView
{
  public NichtMitgliedListeView() throws RemoteException
  {
    control.init("nichtmitglied.", "nichtzusatzfeld.", "nichtzusatzfelder.");
  }

  @Override
  public String getTitle()
  {
    return "Nicht-Mitglieder";
  }

  @Override
  protected void getFilter() throws RemoteException, ApplicationException
  {
    LabelGroup group = new LabelGroup(getParent(), "Filter");
    TabFolder folder = new TabFolder(group.getComposite(),
        SWT.V_SCROLL | SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    // Erster Tab
    TabGroup tab1 = new TabGroup(folder, "Allgemein", true, 3);
    SimpleContainer left = new SimpleContainer(tab1.getComposite());
    left.addInput(control.getFilterInput(Filter.MITGLIEDSTYP));
    left.addInput(control.getFilterInput(Filter.NAME));

    SimpleContainer middle = new SimpleContainer(tab1.getComposite());
    middle.addInput(control.getFilterInput(Filter.GESCHLECHT));
    middle.addInput(control.getFilterInput(Filter.MAIL));

    SimpleContainer right = new SimpleContainer(tab1.getComposite());
    right.addInput(control.getFilterInput(Filter.EIGENSCHAFTEN));
    if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
    {
      right.addInput(control.getFilterInput(Filter.ZUSATZFELD));
    }

    // Zeiter Tab
    TabGroup tab2 = new TabGroup(folder, "Datum", true, 1);
    SimpleContainer left2 = new SimpleContainer(tab2.getComposite());
    left2.addInput(control.getFilterInput(Filter.GEBURTSDATUM_VON));
    left2.addInput(control.getFilterInput(Filter.GEBURTSDATUM_BIS));

    // Dritter Tab
    TabGroup tab3 = new TabGroup(folder, "Mitgliedskonto", true, 2);
    SimpleContainer left3 = new SimpleContainer(tab3.getComposite());
    left3.addInput(control.getFilterInput(Filter.DIFFERENZ));
    left3.addLabelPair("Differenz Limit",
        control.getFilterInput(Filter.DIFFERENZ_LIMIT));

    SimpleContainer right3 = new SimpleContainer(tab3.getComposite());
    right3.addInput(control.getFilterInput(Filter.DATUM_VON));
    right3.addInput(control.getFilterInput(Filter.DATUM_BIS));

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getResetButton());
    buttons.addButton(control.getSuchenButton());
    group.addButtonArea(buttons);
  }

  @Override
  protected Action getDetailAction()
  {
    return new NichtMitgliedDetailAction();
  }

  @Override
  protected Button getHilfeButton()
  {
    return new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.ADRESSEN, false, "question-circle.png");
  }

  @Override
  protected MitgliedAuswahl getMitgliedAuswahl()
  {
    return MitgliedAuswahl.NICHTMITGLIEDER;
  }
}
