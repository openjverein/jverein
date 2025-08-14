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
package de.jost_net.JVerein.gui.dialogs;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

public class DropdownDialog<T> extends AbstractDialog<T>
{
  private final List<T> auswahl;

  private SelectInput auswahlInput;

  private T value;

  public DropdownDialog(List<T> auswahl)
  {
    super(AbstractDialog.POSITION_CENTER);

    this.auswahl = auswahl;
    setTitle("Auswahl");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer group = new SimpleContainer(parent);

    auswahlInput = new SelectInput(auswahl,
        auswahl.isEmpty() ? null : auswahl.get(0));
    group.addLabelPair("Bitte wählen", auswahlInput);

    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton("OK", e -> {
      //noinspection unchecked
      value = (T) auswahlInput.getValue();
      close();
    }, null, true, "ok.png");
    buttonArea.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttonArea.paint(parent);
  }

  @Override
  protected T getData() throws Exception
  {
    return value;
  }
}
