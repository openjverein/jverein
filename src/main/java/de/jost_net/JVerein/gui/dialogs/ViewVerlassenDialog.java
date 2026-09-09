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
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class ViewVerlassenDialog extends AbstractDialog<Integer>
{
  public ViewVerlassenDialog(int position)
  {
    super(position);
  }

  private int data;

  public final static int SPEICHERN = 1;

  public final static int VERLASSEN = 2;

  public final static int ABBRECHEN = 3;

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    setTitle("Nicht gespeichert");

    Container container = new SimpleContainer(parent);
    container.addText("Der Eintrag wurde nicht gespeichert,\n"
        + "soll die Bearbeitung wirklich verlassen werden?", true);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new ButtonRtoL("Speichern", context -> {
      data = SPEICHERN;
      close();
    }, null, true, "document-save.png", "CTRL+S"));

    buttons.addButton(new ButtonRtoL("Ohne Speichern verlassen", context -> {
      data = VERLASSEN;
      close();
    }, null, false, "edit-undo.png", "CTRL+O"));

    buttons.addButton("Abbrechen", context -> {
      data = ABBRECHEN;
      close();
    }, null, false, "process-stop.png");

    container.addButtonArea(buttons);
  }

  @Override
  protected Integer getData()
  {
    return data;
  }
}
