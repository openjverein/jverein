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
import de.jost_net.JVerein.gui.action.HibiscusKontenImportAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.control.KontoControl;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class KontoListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Konten");

    KontoControl control = new KontoControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addLabelPair("Nummer", control.getSuchtext());
    left.addLabelPair("Bezeichnung", control.getSuchname());

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    right.addLabelPair("Status", control.getSuchStatus("Nur aktive Konten"));
    right.addInput(control.getSuchKontoart());

    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);

    control.getKontenList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.KONTEN, false, "question-circle.png");
    buttons.addButton("Hibiscus-Konten-Import",
        new HibiscusKontenImportAction(control), null, false, "walking.png");
    buttons.addButton("Neu", new NewAction(KontoDetailView.class, Konto.class),
        null,
        false,
        "document-new.png");
    buttons.paint(this.getParent());
  }
}
