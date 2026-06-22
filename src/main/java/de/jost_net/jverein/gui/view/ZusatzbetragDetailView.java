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
package de.jost_net.jverein.gui.view;

import java.util.Map;

import de.jost_net.jverein.gui.action.DokumentationAction;
import de.jost_net.jverein.gui.action.InsertVariableDialogAction;
import de.jost_net.jverein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.jverein.gui.control.Savable;
import de.jost_net.jverein.gui.control.ZusatzbetragControl;
import de.jost_net.jverein.gui.parts.ButtonAreaRtoL;
import de.jost_net.jverein.gui.parts.SaveButton;
import de.jost_net.jverein.gui.parts.SaveNeuButton;
import de.jost_net.jverein.gui.parts.ZusatzbetragPart;
import de.jost_net.jverein.variable.AllgemeineMap;
import de.jost_net.jverein.variable.MitgliedMap;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;

public class ZusatzbetragDetailView extends AbstractDetailView
{

  private ZusatzbetragControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Zusatzbetrag");
    control = new ZusatzbetragControl(this);

    final ZusatzbetragPart part = control.getZusatzbetragPart();
    part.paint(getParent());

    LabelGroup group2 = new LabelGroup(getParent(), "Vorlagen");
    group2.addLabelPair("Als Vorlage speichern", control.getVorlage());

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = MitgliedMap.getDummyMap(map);

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ZUSATZBETRAEGE, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton("Buchungstext Variablen anzeigen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    buttons.addButton("Vorlagen", new ZusatzbetragVorlageAuswahlAction(part),
        null, false, "view-refresh.png");
    buttons.addButton(new SaveButton(control));
    buttons.addButton(
        new SaveNeuButton(control, control.getZusatzbetrag().getMitglied()));
    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
