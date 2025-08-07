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

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.AbrechnungslaufControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class AbrechnungslaufDetailView extends AbstractDetailView
{

  private AbrechnungslaufControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Abrechnungslauf");

    control = new AbrechnungslaufControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Detaildaten");
    group.addInput(control.getDatum());
    group.addInput(control.getAbgeschlossen());
    group.addInput(control.getAbrechnungsmodus());
    group.addInput(control.getFaelligkeit());
    group.addInput(control.getAbrechnungStichtag());
    group.addInput(control.getEintrittsdatum());
    group.addInput(control.getAustrittsdatum());
    group.addInput(control.getZahlungsgrund());
    group.addInput(control.getZusatzAbrechnungen());
    group.addInput(control.getBemerkung());

    group = new LabelGroup(getParent(), "Statistikdaten");
    group.addInput(control.getStatistikBuchungen());
    group.addInput(control.getStatistikLastschriften());

    /*
     * FormTextPart text = new FormTextPart(); text.setText("<form>" +
     * "<p><b>Statistikdaten</b></p>" +
     * "<p>Hier fehlen noch die Statistikdaten des Abrechnungslaufs.</p>" +
     * "<p>Anzahl Buchungen, Offene Posten, etc.</p>" +
     * "<table><tr><td>Tst</td></tr></table>" + "</form>");
     * text.paint(getParent());
     */

    ButtonArea buttons = new ButtonArea();
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
