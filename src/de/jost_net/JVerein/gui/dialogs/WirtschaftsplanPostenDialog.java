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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;

public class WirtschaftsplanPostenDialog
    extends AbstractDialog<WirtschaftsplanItem>
{
  private final WirtschaftsplanItem item;

  public WirtschaftsplanPostenDialog(WirtschaftsplanItem item)
      throws RemoteException
  {
    super(AbstractDialog.POSITION_CENTER);

    this.item = item;

    String title = "Bearbeite Posten " + item.getPosten() + " für Buchungsart " + item.getBuchungsart()
        .getBezeichnung() + " (" + item.getBuchungsklasse()
        .getBezeichnung() + ")";

    setTitle(title);
    setSize(620, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer group = new SimpleContainer(parent);

    TextInput postenInput = new TextInput(item.getPosten());
    group.addLabelPair("Posten Name", postenInput);
    DecimalInput sollInput = new DecimalInput(item.getSoll(),
        Einstellungen.DECIMALFORMAT);
    group.addLabelPair("Soll", sollInput);

    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton("OK", context -> {
      try
      {
        item.setPosten((String) postenInput.getValue());
        item.setSoll((Double) sollInput.getValue());
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      close();
    }, null, true, "ok.png");
    buttonArea.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttonArea.paint(parent);
  }

  @Override
  protected WirtschaftsplanItem getData() throws Exception
  {
    return item;
  }
}
