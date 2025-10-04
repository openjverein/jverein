/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.WirtschaftsplanExportAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanMenu;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.EditTreePart;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SaveNeuButton;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanDetailView extends AbstractDetailView
{
  private WirtschaftsplanControl control;

  @Override
  public void bind() throws Exception
  {
    if (!(this.getCurrentObject() instanceof Wirtschaftsplan))
    {
      throw new ApplicationException(
          "Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung");

    control = new WirtschaftsplanControl(this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(
        control);
    uebersicht.paint(this.getParent());
    control.setUebersicht(uebersicht);

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen",
        true);
    EditTreePart treeEinnahmen = control.getEinnahmen();
    treeEinnahmen.setContextMenu(
        new WirtschaftsplanMenu(WirtschaftsplanImpl.EINNAHME, control));
    einnahmen.addPart(treeEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben",
        true);
    EditTreePart treeAusgaben = control.getAusgaben();
    treeAusgaben.setContextMenu(
        new WirtschaftsplanMenu(WirtschaftsplanImpl.AUSGABE, control));
    ausgaben.addPart(treeAusgaben);

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();

    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");

    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());

    buttons.addButton("CSV",
        context -> control
            .starteAuswertung(WirtschaftsplanControl.AUSWERTUNG_CSV),
        null, false, "xsd.png");
    buttons.addButton("PDF", new WirtschaftsplanExportAction(),
        getCurrentObject(), false, "file-pdf.png");
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
