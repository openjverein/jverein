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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.MitgliedNextBGruppeNeuAction;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class MitgliedNextBGruppePart implements Part
{
  private MitgliedControl control;

  public MitgliedNextBGruppePart(MitgliedControl control)
  {
    this.control = control;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup cont = new LabelGroup(parent, "Zukünftige Beitragsgruppen");
    cont.getComposite().setLayout(new GridLayout(1, false));
    ButtonArea butts = new ButtonArea();
    butts.addButton(new Button("Beitragsgruppe hinzufügen",
        new MitgliedNextBGruppeNeuAction(control), null, false,
        "document-new.png"));
    butts.paint(cont.getComposite());
    control.getMitgliedBeitraegeTabelle().paint(cont.getComposite());
  }
}
