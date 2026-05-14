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

import java.util.Calendar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Ein Dialog, zur Auswahl eines Kalenderjahres.
 */
public class JahrAuswahlDialog extends AbstractDialog<Integer>
{
  private SelectInput jahr;

  private Integer selectedJahr = null;

  public JahrAuswahlDialog(int position)
  {
    super(position);
    setTitle("Jahr auswählen");
    setSize(250, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Jahr", getJahr());
    ButtonArea b = new ButtonArea();
    b.addButton("Übernehmen", c -> {
      selectedJahr = (Integer) getJahr().getValue();
      close();
    }, null, false, "ok.png");
    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected Integer getData() throws Exception
  {
    return selectedJahr;
  }

  public SelectInput getJahr()
  {
    if (jahr != null)
    {
      return jahr;
    }
    Calendar cal = Calendar.getInstance();
    jahr = new SelectInput(
        new Object[] { cal.get(Calendar.YEAR), cal.get(Calendar.YEAR) - 1,
            cal.get(Calendar.YEAR) - 2, cal.get(Calendar.YEAR) - 3,
            cal.get(Calendar.YEAR) - 4, cal.get(Calendar.YEAR) - 5,
            cal.get(Calendar.YEAR) - 6, cal.get(Calendar.YEAR) - 7 },
        cal.get(Calendar.YEAR));
    return jahr;
  }
}
