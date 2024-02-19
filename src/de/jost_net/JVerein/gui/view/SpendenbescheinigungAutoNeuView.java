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
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.SpendenbescheinigungAutoNeuControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.LabelGroup;

public class SpendenbescheinigungAutoNeuView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Spendenbescheinigungen automatisch neu erzeugen");

    SpendenbescheinigungAutoNeuControl control = new SpendenbescheinigungAutoNeuControl(
        this);
    
    InfoPanel   info = new InfoPanel();
    info.setTitle("Info");
    info.setIcon("gtk-info.png");
    double betrag = Einstellungen.getEinstellung().getSpendenbescheinigungminbetrag();
    if (betrag == 0)
    {
      info.setText("Es wurden nur Mitglieder ber�cksichtigt, bei denen Strasse, "
          + "PLZ und Ort eingetragen sind.");
    }
    else
    {
    info.setText(String.format("Es wurden nur Mitglieder ber�cksichtigt, bei denen Strasse, "
        + "PLZ und Ort eingetragen sind."+'\n'+"Auch wurden nur Spendenbescheinigungen "
        + "generiert deren Betrag gr��er oder gleich %s Euro ist.", betrag));
    info.setComment("Siehe Administration->Einstellungen->Spendenbescheinigungen->Mindestbetrag");
    }
    info.paint(getParent());
    LabelGroup group = new LabelGroup(getParent(), "Jahr");
    group.addLabelPair("Jahr", control.getJahr());
    // TODO Unterscheidung Einzel/Sammel-Best�tigung: zwei Felder
    group.addLabelPair("Formular-Einzelbest.", control.getFormular());
    group.addLabelPair("Formular-Sammelbest.",
        control.getFormularSammelbestaetigung());

    control.getSpendenbescheinigungTree().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
            DokumentationUtil.SPENDENBESCHEINIGUNG, false, "question-circle.png");
    buttons.addButton(control.getSpendenbescheinigungErstellenButton());
    buttons.paint(getParent());
  }
}
