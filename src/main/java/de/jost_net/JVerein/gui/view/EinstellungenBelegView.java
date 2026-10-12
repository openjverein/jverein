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

import de.jost_net.JVerein.gui.control.EinstellungControl;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;

public class EinstellungenBelegView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Belege");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer cont = new ScrolledContainer(getParent(), 1);

    LabelGroup groupMitglied = new LabelGroup(cont.getComposite(),
        "Mitglieds-Belege");
    groupMitglied.addLabelPair("Verzeichnis für Mitglieds-Dokumente *",
        control.getMitgliedsDokumentVerzeichnis());

    LabelGroup groupBuchung = new LabelGroup(cont.getComposite(),
        "Buchungs-Belege");
    groupBuchung.addLabelPair("Verzeichnis für Buchungs-Belege *",
        control.getBuchungsDokumentVerzeichnis());
    groupBuchung.addInput(control.getBelegBuchungNummer());
    groupBuchung.addInput(control.getBelegBuchungZaehler());

    LabelGroup groupAllgemein = new LabelGroup(cont.getComposite(),
        "Allgemeine Belege");
    groupAllgemein.addLabelPair("Verzeichnis für Allgemeine Belege *",
        control.getBelegeAllgemeinVerzeichnis());
    groupAllgemein.addInput(control.getBelegSonstigesNummer());
    groupAllgemein.addInput(control.getBelegSonstigesZaehler());

    cont.addText(
        "* Die Unterordner unter dem die Dokumente angelegt werden,"
            + " können unter Vorlagen (Vorlageart: Pfad) eingestellt werden.",
        false);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(
        new HelpButton(DokumentationUtil.EINSTELLUNGEN_VERZEICHNISSE));
    buttons.addButton(new SaveButton(o -> control.handleStoreBelege()));
    buttons.paint(this.getParent());
  }
}
