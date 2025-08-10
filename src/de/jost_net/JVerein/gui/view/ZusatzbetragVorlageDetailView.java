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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.ZusatzbetragVorlageControl;
import de.jost_net.JVerein.gui.parts.ButtonArea;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SaveNeuButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;

public class ZusatzbetragVorlageDetailView extends AbstractDetailView
{

  private ZusatzbetragVorlageControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Zusatzbetrag-Vorlage");
    control = new ZusatzbetragVorlageControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Zusatzbetrag-Vorlage");
    group.addLabelPair("Erste Fälligkeit ", control.getStartdatum(true));
    group.addLabelPair("Nächste Fälligkeit", control.getFaelligkeit());
    group.addLabelPair("Intervall", control.getIntervall());
    group.addLabelPair("Nicht mehr ausführen ab", control.getEndedatum());
    group.addLabelPair("Buchungstext", control.getBuchungstext());
    group.addLabelPair("Betrag", control.getBetrag());
    group.addLabelPair("Buchungsart", control.getBuchungsart());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      group.addLabelPair("Steuer", control.getSteuer());
    }
    group.addLabelPair("Zahlungsweg", control.getZahlungsweg());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ZUSATZBETRAEGE_VORLAGE, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
