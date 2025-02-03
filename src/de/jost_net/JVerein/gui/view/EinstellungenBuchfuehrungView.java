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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ScrolledContainer;

public class EinstellungenBuchfuehrungView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Einstellungen Buchführung");

    final EinstellungControl control = new EinstellungControl(this);

    ScrolledContainer cont = new ScrolledContainer(getParent());

    cont.addLabelPair("Beginn Geschäftsjahr (TT.MM.)",
        control.getBeginnGeschaeftsjahr());
    cont.addLabelPair("Intervall für aktive Konten (Jahre)",
        control.getUnterdrueckungKonten());
    cont.addLabelPair("Buchungsarten die seit x Monaten nicht benutzt werden unterdrücken",
        control.getUnterdrueckungLaenge());
    cont.addLabelPair("Anlagen Restwert", control.getAfaRestwert());
    cont.addInput(control.getAutoBuchunguebernahme());
    cont.addInput(control.getAutomatischeBuchungskorrekturHibiscus());
    cont.addInput(control.getUnterdrueckungOhneBuchung());
    cont.addInput(control.getKontonummerInBuchungsliste());
    cont.addInput(control.getOptiert());
    cont.addInput(control.getFreieBuchungsklasse());
    cont.addInput(control.getSplitPositionZweck());
    cont.addInput(control.getGeprueftSynchronisieren());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.EINSTELLUNGEN_BUCHFUEHRUNG, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        control.handleStoreBuchfuehrung();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }
}
