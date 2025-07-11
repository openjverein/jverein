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

import java.util.Date;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.WiedervorlageControl;
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class WiedervorlageDetailView extends AbstractDetailView
{
  private WiedervorlageControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Wiedervorlage");
    control = new WiedervorlageControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Wiedervorlage");
    group.addLabelPair("Mitglied", control.getMitglied());
    group.addLabelPair("Datum", control.getDatum(true));
    group.addLabelPair("Vermerk", control.getVermerk());
    group.addLabelPair("Erledigung", control.getErledigung());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIEDERVORLAGE, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getVorButton());
    Geschaeftsjahr gj = new Geschaeftsjahr(new Date());
    // Parameter: Objekt Klasse, Tabellen Name, Order By, Filter,
    // Filter Parameter
    control.setObjektListe(Wiedervorlage.class, "wiedervorlage",
        "datum, mitglied", "datum >= ?", gj.getBeginnLetztesGeschaeftsjahr());
    buttons.addButton(new SaveButton(control));
    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
