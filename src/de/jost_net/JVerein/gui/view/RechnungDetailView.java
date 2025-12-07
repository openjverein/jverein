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
import de.jost_net.JVerein.gui.control.RechnungControl;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class RechnungDetailView extends AbstractDetailView
{

  RechnungControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Rechnungen");

    control = new RechnungControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Rechnung");

    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);
    SimpleContainer left = new SimpleContainer(cl.getComposite());

    left.addInput(control.getNummer());
    left.addInput(control.getRechnungsdatum());
    left.addInput(control.getMitglied());
    left.addInput(control.getZahler());
    left.addInput(control.getBetrag());
    left.addInput(control.getRechnungFormular());
    left.addInput(control.getKommentar());
    left.addInput(control.getPersonenart());

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getGeschlecht());
    middle.addInput(control.getAnrede());
    middle.addInput(control.getTitel());
    middle.addInput(control.getName());
    middle.addInput(control.getVorname());
    middle.addInput(control.getStrasse());
    middle.addInput(control.getAdressierungszusatz());
    middle.addInput(control.getLeitwegID());

    SimpleContainer rigth = new SimpleContainer(cl.getComposite());
    rigth.addInput(control.getPlz());
    rigth.addInput(control.getOrt());
    rigth.addInput(control.getStaat());
    rigth.addInput(control.getIban());
    rigth.addInput(control.getBic());
    rigth.addInput(control.getMandatdatum());
    rigth.addInput(control.getMandatid());
    rigth.addInput(control.getZahlungsweg());

    LabelGroup cont = new LabelGroup(getParent(), "Rechnungspositionen", true);
    cont.addPart(control.getSollbuchungPositionListPart());

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.RECHNUNG, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(control.getRechnungDruckUndMailButton());
    buttons.addButton(control.getMahnungDruckUndMailButton());
    buttons.addButton(new SaveButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
