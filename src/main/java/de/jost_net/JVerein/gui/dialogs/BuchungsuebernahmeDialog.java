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

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.BuchungsuebernahmeControl;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;

public class BuchungsuebernahmeDialog extends AbstractDialog<Boolean>
{

  Boolean start = false;

  public BuchungsuebernahmeDialog(int position)
  {
    super(position);
    setTitle("Hibiscus-Buchungen-Import");
    setSize(900, 600);
  }

  @Override
  public void paint(Composite parent) throws Exception
  {
    final BuchungsuebernahmeControl control = new BuchungsuebernahmeControl();

    control.getKontenList().paint(parent);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGSUEBERNAHME, false, "question-circle.png");
    buttons.addButton("Import starten", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        start = true;
        close();
      }
    }, null, true, "file-import.png");
    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, false, "process-stop.png");
    buttons.paint(parent);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return start;
  }
}
