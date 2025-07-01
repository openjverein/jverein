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
import de.jost_net.JVerein.gui.input.SaveButton;
import de.jost_net.JVerein.gui.control.BeitragsgruppeControl;
import de.jost_net.JVerein.gui.util.SimpleVerticalContainer;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;

public class BeitragsgruppeDetailView extends AbstractDetailView
{

  private BeitragsgruppeControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Beitragsgruppe");

    control = new BeitragsgruppeControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Beitrag");
    group.addLabelPair("Bezeichnung", control.getBezeichnung(true));

    if ((Boolean) Einstellungen.getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN))
    {
      group.addLabelPair("Sekundäre Beitragsgruppe", control.getSekundaer());
    }

    switch (Beitragsmodel
        .getByKey((Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)))
    {
      case GLEICHERTERMINFUERALLE:
      case MONATLICH12631:
      {
        group.addLabelPair("Betrag", control.getBetrag());
        break;
      }
      case FLEXIBEL:
      {
        group.addLabelPair("Betrag monatlich", control.getBetragMonatlich());
        group.addLabelPair("Betrag vierteljährlich",
            control.getBetragVierteljaehrlich());
        group.addLabelPair("Betrag halbjährlich",
            control.getBetragHalbjaehrlich());
        group.addLabelPair("Betrag jährlich", control.getBetragJaehrlich());
        break;
      }
    }
    
    group.addLabelPair("Beitragsart", control.getBeitragsArt());
    group.addLabelPair("Buchungsart", control.getBuchungsart());
    if ((Boolean) Einstellungen.getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    }

    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) != Beitragsmodel.FLEXIBEL.getKey()
        && (Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
    {
      Input[] altersstaffel = control.getAltersstaffel();
      if (altersstaffel != null)
      {
        Container cont = new LabelGroup(getParent(), "Altersstaffel");
        SimpleVerticalContainer svc = new SimpleVerticalContainer(
            cont.getComposite(), true, 3);
        svc.addCheckbox(control.getIsAltersstaffel(), "Nach Alter gestaffelte Beiträge verwenden");
        for (Input inp : altersstaffel)
        {
          svc.addInput(inp);
        }
        svc.arrangeVertically();
      }
    }
    
    if ((Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ))
    {
      LabelGroup groupAe = new LabelGroup(getParent(), "Arbeitseinsatz");
      groupAe.addLabelPair("Stunden", control.getArbeitseinsatzStunden());
      groupAe.addLabelPair("Betrag", control.getArbeitseinsatzBetrag());
    }

    group.addLabelPair("Notiz", control.getNotiz());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BEITRAGSGRUPPEN, false, "question-circle.png");
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
