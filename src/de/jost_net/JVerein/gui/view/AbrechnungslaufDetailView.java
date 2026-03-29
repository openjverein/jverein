/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung für Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2015 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.AbrechnungslaufControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;

public class AbrechnungslaufDetailView extends AbstractDetailView
{
  // Statische Variable, die den zuletzt ausgewählten Tab speichert.
  private static int tabindex = -1;

  private AbrechnungslaufControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Abrechnungslauf");

    control = new AbrechnungslaufControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Detaildaten");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    SimpleContainer rigth = new SimpleContainer(cl.getComposite());
    left.addInput(control.getDatum());
    left.addInput(control.getAbgeschlossen());
    left.addInput(control.getAbrechnungsmodus());
    left.addInput(control.getFaelligkeit());
    left.addInput(control.getAbrechnungStichtag());
    rigth.addInput(control.getEintrittsdatum());
    rigth.addInput(control.getAustrittsdatum());
    rigth.addInput(control.getZahlungsgrund());
    rigth.addInput(control.getZusatzAbrechnungen());
    rigth.addInput(control.getBemerkung());

    TabFolder folder = new TabFolder(getParent(), SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tabBuchung = new TabGroup(folder, "Buchungen", true, 1);
    ButtonAreaRtoL buchbuttons = new ButtonAreaRtoL();
    buchbuttons.addButton(control.getStartBuchungListeButton());
    buchbuttons.paint(tabBuchung.getComposite());
    control.getBuchungList().paint(tabBuchung.getComposite());

    TabGroup tabSollbuchung = new TabGroup(folder, "Sollbuchungen", true, 1);
    ButtonAreaRtoL sollbbuttons = new ButtonAreaRtoL();
    sollbbuttons.addButton(control.getStartSollbuchungListeButton());
    sollbbuttons.paint(tabSollbuchung.getComposite());
    control.getSollbuchungList().paint(tabSollbuchung.getComposite());

    TabGroup tabLastschriften = new TabGroup(folder, "Lastschriften", true, 1);
    control.getLastschriftList().paint(tabLastschriften.getComposite());

    if ((boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
    {
      TabGroup tabZusatzbetraege = new TabGroup(folder, "Zusatzbeträge", true,
          1);
      control.getZusatzbetraegeList().paint(tabZusatzbetraege.getComposite());
    }

    // Aktiver zuletzt ausgewählter Tab.
    if (tabindex != -1)
    {
      folder.setSelection(tabindex);
    }
    folder.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetSelected(SelectionEvent evt)
      {
        tabindex = folder.getSelectionIndex();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e)
      {
      }
    });

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ABRECHNUNGSLAUF, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }

}
