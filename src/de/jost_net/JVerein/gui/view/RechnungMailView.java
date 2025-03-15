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

import java.util.Map;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.MailTextVorschauAction;
import de.jost_net.JVerein.gui.action.MailVorlageUebernehmenAction;
import de.jost_net.JVerein.gui.action.MailVorlageZuweisenAction;
import de.jost_net.JVerein.gui.control.RechnungControl;
import de.jost_net.JVerein.server.MitgliedImpl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class RechnungMailView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Rechnungen");

    final RechnungControl control = new RechnungControl(this);
    control.init(RechnungControl.TYP.RECHNUNG.name() + ".mail.", null, null);

    if (this.getCurrentObject() == null)
    {
      LabelGroup group = new LabelGroup(getParent(), "Filter");
      ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
      
      SimpleContainer left = new SimpleContainer(cl.getComposite());
      left.addInput(control.getSuchname());
      left.addInput(control.getDifferenz());
      left.addLabelPair("Ohne Abbucher", control.getOhneAbbucher());
      
      SimpleContainer right = new SimpleContainer(cl.getComposite());
      right.addInput(control.getDatumvon());
      right.addInput(control.getDatumbis());
      right.addInput(control.getMailauswahl());
      
      ButtonArea filterbuttons = new ButtonArea();
      filterbuttons.addButton(control.getResetButton());
      filterbuttons.addButton(control.getSpeichernButton());
      group.addButtonArea(filterbuttons);
    }
    else
    {
      SimpleContainer cont1 = new SimpleContainer(getParent(), false);
      cont1.addHeadline("Info");
      cont1.addInput(control.getInfo());
    }
        
    SimpleContainer cont = new SimpleContainer(getParent(), true);
    cont.addHeadline("Parameter");
    
    cont.addInput(control.getAusgabeart());
    //cont.addInput(control.getAusgabesortierung());

    cont.addHeadline("Mail");
    cont.addInput(control.getBetreff());
    cont.addLabelPair("Text", control.getTxt());

    Map<String, Object> map = RechnungMap.getDummyMap(null);
    map = new MitgliedMap().getMap(MitgliedImpl.getDummy(), map);
    map = new AllgemeineMap().getMap(map);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.RECHNUNG_MAIL, false, "question-circle.png");
    buttons.addButton(new Button("Mail-Vorlage", new MailVorlageZuweisenAction(),
        control, false, "view-refresh.png"));
    buttons.addButton("Variablen anzeigen", new InsertVariableDialogAction(map),
        control, false, "bookmark.png");
    buttons.addButton(new Button("Vorschau",
        new MailTextVorschauAction(map, true), control, false,
        "edit-copy.png"));
    buttons.addButton(
        new Button("Als Vorlage übernehmen", new MailVorlageUebernehmenAction(),
        control, false, "document-new.png"));
    buttons.addButton(control.getStartRechnungButton(this.getCurrentObject()));
    buttons.paint(this.getParent());
  }
}
