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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.Filter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class AuswertungMitgliedView extends AbstractView
{
  final MitgliedControl control = new MitgliedControl(this);

  public AuswertungMitgliedView() throws RemoteException
  {
    control.init("mitglied.", "zusatzfeld.", "zusatzfelder.");
  }

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Auswertung Mitglieder");

    LabelGroup group = new LabelGroup(getParent(), "Filter");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    // left
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getFilterInput(Filter.MITGLIEDSCHAFT_STATUS));
    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
    {
      left.addInput(control.getFilterInput(Filter.EXTERNEMITGLIEDSNUMMER));
    }
    left.addInput(control.getFilterInput(Filter.EIGENSCHAFTEN));
    left.addInput(control.getFilterInput(Filter.BEITRAGSGRUPPE));

    if ((Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER))
    {
      left.addInput(control.getFilterInput(Filter.ZUSATZFELD));
    }

    // middle
    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getFilterInput(Filter.MAIL));
    middle.addInput(control.getFilterInput(Filter.GEBURTSDATUM_VON));
    middle.addInput(control.getFilterInput(Filter.GEBURTSDATUM_BIS));
    middle.addInput(control.getFilterInput(Filter.GESCHLECHT));
    middle.addInput(control.getFilterInput(Filter.STICHTAG));

    // right
    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addInput(control.getFilterInput(Filter.EINTRITT_VON));
    right.addInput(control.getFilterInput(Filter.EINTRITT_BIS));
    right.addInput(control.getFilterInput(Filter.AUSTRITT_VON));
    right.addInput(control.getFilterInput(Filter.AUSTRITT_BIS));

    if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
    {
      right.addInput(control.getFilterInput(Filter.STERBEDATUM_VON));
      right.addInput(control.getFilterInput(Filter.STERBEDATUM_BIS));
    }

    ButtonArea filterbuttons = new ButtonArea();
    filterbuttons.addButton(control.getResetButton());
    filterbuttons.addButton(control.getSpeichernButton());
    group.addButtonArea(filterbuttons);

    // Zweite Gruppe: Ausgabe
    LabelGroup group2 = new LabelGroup(getParent(), "Ausgabe");

    ColumnLayout cl2 = new ColumnLayout(group2.getComposite(), 2);
    SimpleContainer left2 = new SimpleContainer(cl2.getComposite());
    SimpleContainer right2 = new SimpleContainer(cl2.getComposite());

    left2.addInput(control.getSortierung());
    left2.addInput(control.getAuswertungUeberschrift());
    right2.addInput(control.getAusgabe());

    // Button-Bereich
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.AUSWERTUNGMITGLIEDER, false, "question-circle.png");
    buttons.addButton(control.getVorlagenCsvEditButton());
    buttons.addButton(control.getStartAuswertungButton());
    buttons.paint(getParent());
  }
}
