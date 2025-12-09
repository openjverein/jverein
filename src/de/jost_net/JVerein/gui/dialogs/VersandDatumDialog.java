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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Ein Dialog, ueber den man die Personenart eines neuen Mitglieds auswählen
 * kann.
 */
public class VersandDatumDialog extends AbstractDialog<Date>
{

  private Date datum = null;

  private DateInput datuminput = null;

  private Boolean closed = true;

  public VersandDatumDialog(int position)
  {
    super(position);

    setTitle("Versanddatum setzen");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup options = new LabelGroup(parent, "");
    options.addLabelPair("Versanddatum", this.getDateInput());
    ButtonArea b = new ButtonArea();
    b.addButton("Übernehmen", c -> {
      closed = false;
      datum = (Date) getDateInput().getValue();
      close();
    }, null, false, "ok.png");
    b.addButton("Entfernen", c -> {
      closed = false;
      datum = null;
      close();
    }, null, false, "user-trash-full.png");
    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected Date getData() throws Exception
  {
    return this.datum;
  }

  private DateInput getDateInput()
  {
    if (datuminput != null)
    {
      return datuminput;
    }
    return datuminput = new DateInput(new Date());
  }

  public Boolean getClosed()
  {
    return closed;
  }
}
