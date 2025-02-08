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
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanungMenu;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungView extends AbstractView
{
  private static int EINNAHME = 0;
  private static int AUSGABE = 1;

  @Override
  public void bind() throws Exception
  {
    if (!(this.getCurrentObject() instanceof WirtschaftsplanungZeile))
    {
      throw new ApplicationException(
          "Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung");

    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(
        this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(
        control);
    uebersicht.paint(this.getParent());
    control.setUebersicht(uebersicht);

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen", true);
    TreePart treeEinnahmen = control.getEinnahmen();
    treeEinnahmen.setContextMenu(new WirtschaftsplanungMenu(EINNAHME, control));
    einnahmen.addPart(treeEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben", true);
    TreePart treeAusgaben = control.getAusgaben();
    treeAusgaben.setContextMenu(new WirtschaftsplanungMenu(AUSGABE, control));
    ausgaben.addPart(treeAusgaben);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.addButton("CSV", context -> control.starteAuswertung(
        WirtschaftsplanungControl.AUSWERTUNG_CSV), null, false, "xsd.png");
    buttons.addButton("PDF", context -> control.starteAuswertung(
        WirtschaftsplanungControl.AUSWERTUNG_PDF), null, false, "file-pdf.png");
    buttons.addButton("Speichern", context -> control.handleStore(), null,
        false, "document-save.png");
    buttons.paint(this.getParent());
  }
}
