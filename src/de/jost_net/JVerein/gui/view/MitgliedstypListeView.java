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
import de.jost_net.JVerein.gui.action.MitgliedstypDefaultAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.MitgliedstypControl;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class MitgliedstypListeView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Mitgliedstypen");

    MitgliedstypControl control = new MitgliedstypControl(this);

    control.getMitgliedstypList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ADRESSTYPEN, false, "question-circle.png");
    buttons.addButton("Neu",
        new NewAction(MitgliedstypDetailView.class, Mitgliedstyp.class), null,
        false, "document-new.png");

    DBIterator<Mitgliedstyp> mtIt = Einstellungen.getDBService()
        .createList(Mitgliedstyp.class);
    mtIt.addFilter(Mitgliedstyp.JVEREINID + " >= " + Mitgliedstyp.ID_MITGLIED
        + " AND " + Mitgliedstyp.JVEREINID + " <= " + Mitgliedstyp.ID_SPENDER);
    if (mtIt.size() == 0)
    {
      buttons.addButton("Default-Mitgliedstypen einrichten",
          new MitgliedstypDefaultAction());
    }
    buttons.paint(this.getParent());
  }
}
