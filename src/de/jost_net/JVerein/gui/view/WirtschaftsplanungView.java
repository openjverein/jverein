/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungView extends AbstractView
{
  @Override
  public void bind() throws Exception
  {
    if (! (this.getCurrentObject() instanceof WirtschaftsplanungZeile)) {
      throw new ApplicationException("Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung vom " +
            new JVDateFormatTTMMJJJJ().format(((WirtschaftsplanungZeile) this.getCurrentObject()).getVon()) +
            " bis " +
            new JVDateFormatTTMMJJJJ().format(((WirtschaftsplanungZeile) this.getCurrentObject()).getBis()));

    final WirtschaftsplanungControl control = new WirtschaftsplanungControl(this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(control);
    uebersicht.paint(this.getParent());

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen");
    Part treeEinnahmen = control.getEinnahmen();
    einnahmen.addPart(treeEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben");
    Part treeAusgaben = control.getAusgaben();
    ausgaben.addPart(treeAusgaben);
  }
}
