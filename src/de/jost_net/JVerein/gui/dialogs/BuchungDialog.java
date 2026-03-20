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
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class BuchungDialog extends AbstractDialog<Boolean>
{
  private List<Buchung> list;

  private ArrayList<String> filterTexte;

  public BuchungDialog(int position, List<Buchung> list,
      ArrayList<String> filterText)
  {
    super(position);
    super.setSize(1200, 400);
    setTitle("Buchungen");
    this.filterTexte = filterText;
    this.list = list;
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    SimpleContainer cont = new SimpleContainer(parent, true, 1);
    for (String text : filterTexte)
    {
      cont.addText(text, false);
    }

    cont.addPart(new BuchungListPart(list, null, null));

    ButtonArea buttons = new ButtonArea();
    cont.addButtonArea(buttons);
    buttons.addButton("Schließen", e -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);

  }

  @Override
  protected void onEscape()
  {
    // Keine Oce werfen
    close();
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return true;
  }
}
