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
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAction;
import de.jost_net.JVerein.gui.control.SpendenbescheinigungControl;
import de.jost_net.JVerein.keys.Spendenart;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SpendenbescheinigungView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigung");

    final SpendenbescheinigungControl control = new SpendenbescheinigungControl(
        this);

    ScrolledContainer scrolled = new ScrolledContainer(getParent());

    ColumnLayout cols1 = new ColumnLayout(scrolled.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cols1.getComposite());

    left.addHeadline("Spendenart");
    left.addLabelPair("Spendenart", control.getSpendenart());

    left.addHeadline("Spender");
    left.addLabelPair("Zeile 1", control.getZeile1(true));
    left.addLabelPair("Zeile 2", control.getZeile2());
    left.addLabelPair("Zeile 3", control.getZeile3());
    left.addLabelPair("Zeile 4", control.getZeile4());
    left.addLabelPair("Zeile 5", control.getZeile5());
    left.addLabelPair("Zeile 6", control.getZeile6());
    left.addLabelPair("Zeile 7", control.getZeile7());

    left.addHeadline("Datum");
    left.addLabelPair("Spende", control.getSpendedatum());
    left.addLabelPair("Bescheinigung", control.getBescheinigungsdatum());

    SimpleContainer right = new SimpleContainer(cols1.getComposite());

    right.addHeadline("Betrag");
    right.addLabelPair("Betrag", control.getBetrag());

    right.addHeadline("Ersatz f�r Aufwendungen");
    right.addLabelPair("Ersatz f�r Aufwendungen",
        control.getErsatzAufwendungen());

    right.addHeadline("Formular");
    right.addLabelPair("Formular", control.getFormular());

    right.addHeadline("Zusatzinformationen Sachspenden");
    right.addLabelPair("Bezeichnung Sachzuwendung",
        control.getBezeichnungSachzuwendung());
    right.addLabelPair("Herkunft", control.getHerkunftSpende());
    right.addLabelPair("Unterlagen Wertermittlung",
        control.getUnterlagenWertermittlung());

    /*
     * Betrag kann bei Geldspenden nicht ge�ndert werden
     */
    if (control.getSpendenbescheinigung().getAutocreate())
    {
      control.getSpendenart().setEnabled(false);
      control.getBetrag().setEnabled(false);
      // Buchnungen nur f�r Geldspenden
      LabelGroup grBuchungen = new LabelGroup(scrolled.getComposite(),
          "Buchungen");
      grBuchungen.addPart(control.getBuchungsList());
    }
    else
    {
      control.getSpendenart().setEnabled(false);
      control.getBetrag().setEnabled(true);
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.SPENDENBESCHEINIGUNG, false, "question-circle.png");
    buttons.addButton(control.getPDFStandardButton(false));
    buttons.addButton(control.getPDFStandardButton(true));
    buttons.addButton(control.getPDFIndividuellButton(false));
    buttons.addButton(control.getPDFIndividuellButton(true));
    buttons.addButton("Neu (Sachspende)", new SpendenbescheinigungAction(Spendenart.SACHSPENDE), null, false,
        "document-new.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
