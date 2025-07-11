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
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.gui.control.LehrgangControl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class LehrgangDetailView extends AbstractDetailView
{
  private LehrgangControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Lehrgang");
    control = new LehrgangControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Lehrgang");
    group.addLabelPair("Mitglied", control.getMitglied());
    group.addLabelPair("Lehrgangsart", control.getLehrgangsart());
    group.addLabelPair("Am/von", control.getVon());
    group.addLabelPair("Bis", control.getBis());
    group.addLabelPair("Veranstalter", control.getVeranstalter());
    group.addLabelPair("Ergebnis", control.getErgebnis());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.LEHRGANG, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getVorButton());
    Geschaeftsjahr gj = new Geschaeftsjahr(new Date());
    // Parameter: Objekt Klasse, Tabellen Name, Order By, Filter,
    // Filter Parameter
    control.setObjektListe(Lehrgang.class, "lehrgang", "von, mitglied",
        "von >= ?", gj.getBeginnLetztesGeschaeftsjahr());
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
