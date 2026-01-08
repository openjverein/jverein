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
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class EinstellungenAllgemeinView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Allgemein");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent(), 1);

    LabelGroup verein = new LabelGroup(scrolled.getComposite(), "Vereinsdaten",
        false);
    ColumnLayout cols1 = new ColumnLayout(verein.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cols1.getComposite());
    left.addLabelPair("Name", control.getName(true));
    left.addLabelPair("Straße", control.getStrasse());
    left.addLabelPair("PLZ", control.getPlz());
    left.addLabelPair("Ort", control.getOrt());
    left.addLabelPair("Staat", control.getStaat());
    SimpleContainer right = new SimpleContainer(cols1.getComposite());
    TextInput bic = control.getBic(); // vor IBAN initialisieren, da IBAN eine
                                      // Referenz auf bic benötigt!
    right.addLabelPair("IBAN", control.getIban());
    right.addLabelPair("BIC", bic);
    right.addLabelPair("Gläubiger-ID", control.getGlaeubigerID());
    right.addLabelPair("USt-ID", control.getUstID());

    LabelGroup pflicht = new LabelGroup(scrolled.getComposite(),
        "Pflichtfelder", false);
    ColumnLayout cols2 = new ColumnLayout(pflicht.getComposite(), 3);
    SimpleContainer left2 = new SimpleContainer(cols2.getComposite());
    left2.addLabelPair("Mitglieder Eintrittsdatum",
        control.getEintrittsdatumPflicht());
    left2.addLabelPair("Mitglieder Geburtsdatum",
        control.getGeburtsdatumPflicht());
    SimpleContainer middle2 = new SimpleContainer(cols2.getComposite());
    middle2.addLabelPair("Nicht-Mitglieder Geburtsdatum",
        control.getNichtMitgliedGeburtsdatumPflicht());
    SimpleContainer right2 = new SimpleContainer(cols2.getComposite());
    right2.addLabelPair("Kursteilnehmer Geburtsdatum",
        control.getKursteilnehmerGebPflicht());
    right2.addLabelPair("Kursteilnehmer Geschlecht",
        control.getKursteilnehmerGesPflicht());

    LabelGroup pflichteigenschaften = new LabelGroup(scrolled.getComposite(),
        "Pflicht Eigenschaften", false);
    ColumnLayout cols3 = new ColumnLayout(pflichteigenschaften.getComposite(),
        1);
    SimpleContainer left3 = new SimpleContainer(cols3.getComposite());
    left3.addLabelPair("Auch für Juristische Personen (Mitglied)",
        control.getJMitgliedPflichtEigenschaften());
    left3.addLabelPair("Auch für Juristische Personen (Nicht-Mitglied)",
        control.getJNichtMitgliedPflichtEigenschaften());
    left3.addLabelPair("Auch für Nicht-Mitglieder",
        control.getNichtMitgliedPflichtEigenschaften());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_ALLGEMEIN, false,
        "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreAllgemein();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
