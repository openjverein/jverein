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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class FormularAuswahlDialog extends AbstractDialog<Formular>
{

  private FormularInput formular;

  private Formular data;

  public FormularAuswahlDialog()
  {
    super(SWT.CENTER);
    setTitle("Formular auswählen");
  }

  @Override
  protected Formular getData() throws Exception
  {
    return data;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addText(
        "Bitte Formular, das für die\n"
            + "Rechnung(en) verwendet werden soll, auswählen.",
        true);
    formular = new FormularInput(FormularArt.RECHNUNG);
    group.addLabelPair("Formular", formular);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Rechnung(en) erstellen", context -> {
      if (formular.getValue() == null)
      {
        return;
      }
      data = (Formular) formular.getValue();
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }
}
