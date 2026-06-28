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

import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.AuswertungControl;
import de.jost_net.JVerein.keys.Filter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class AuswertungMitgliedStatistikView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mitgliederstatistik");

    final AuswertungControl control = new AuswertungControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Parameter");

    Input input = control.getFilterInput(Filter.STICHTAG);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.DAY_OF_MONTH, 31);
    input.setValue(new Date(cal.getTimeInMillis()));
    group.addInput(input);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.STATISTIKMITGLIEDER, false, "question-circle.png");
    buttons.addButton(control.getStartStatistikButton());
    buttons.paint(this.getParent());
  }
}
