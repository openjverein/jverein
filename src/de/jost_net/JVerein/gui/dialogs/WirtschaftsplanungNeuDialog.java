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
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;
import java.util.Date;

public class WirtschaftsplanungNeuDialog
    extends AbstractDialog<WirtschaftsplanungZeile>
{
  private final Wirtschaftsplan wirtschaftsplan;

  public WirtschaftsplanungNeuDialog() throws RemoteException
  {
    super(POSITION_CENTER);

    wirtschaftsplan = Einstellungen.getDBService()
        .createObject(Wirtschaftsplan.class, null);

    setTitle("Neuen Wirtschaftsplan - Zeitraum");
    setSize(420, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    SimpleContainer group = new SimpleContainer(parent);

    DateInput von = new DateInput();
    group.addLabelPair("Von", von);
    DateInput bis = new DateInput();
    group.addLabelPair("Bis", bis);

    ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton("OK", context -> {
      try
      {
        if (((Date) von.getValue()).after(
            (Date) bis.getValue()) || von.getValue().equals(bis.getValue()))
        {
          throw new ApplicationException(
              "Startdatum muss vor Enddatum liegen!");
        }

        wirtschaftsplan.setDatumVon((Date) von.getValue());
        wirtschaftsplan.setDatumBis((Date) bis.getValue());
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      close();
    }, null, false, "ok.png");
    buttonArea.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttonArea.paint(parent);
  }

  @Override
  protected WirtschaftsplanungZeile getData() throws Exception
  {
    return new WirtschaftsplanungZeile(wirtschaftsplan);
  }
}
