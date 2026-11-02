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

import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SaveNeuButton;
import de.jost_net.JVerein.gui.control.LehrgangDetailControl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class LehrgangDetailView extends AbstractDetailView
{
  private LehrgangDetailControl control;

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Lehrgang");
    control = new LehrgangDetailControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Lehrgang");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Lehrgangsart", control.getLehrgangsart());
    left.addLabelPair("Bezeichnung", control.getBezeichnung());
    left.addLabelPair("Veranstalter", control.getVeranstalter());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Am/von", control.getVon());
    right.addLabelPair("Bis", control.getBis());
    right.addLabelPair("Betrag", control.getBetrag());

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton(new HelpButton(DokumentationUtil.LEHRGANG));
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());
    buttons.addButton(new SaveButton(control));
    buttons.addButton(new SaveNeuButton(control));
    buttons.paint(this.getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }
}
