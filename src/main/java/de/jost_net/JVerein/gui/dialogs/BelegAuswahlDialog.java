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

import de.jost_net.JVerein.gui.control.BelegListControl;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class BelegAuswahlDialog extends AbstractDialog<BuchungDokument[]>
{
  private BuchungDokument[] data;

  private BuchungDokument[] auswahl;

  public BelegAuswahlDialog()
  {
    super(BelegAuswahlDialog.POSITION_CENTER);
    super.setSize(1200, 400);
    setTitle("Belege");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    final BelegListControl control = new BelegListControl(null);

    LabelGroup group = new LabelGroup(parent, "Filter");
    group.addInput(control.getFilterInput(Filter.NUMMER));
    group.addInput(control.getFilterInput(Filter.BEZEICHNUNG));
    group.addLabelPair("Nicht zugeordnet",
        control.getFilterInput(Filter.NICHT_ZUGEORDNET));

    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);

    JVereinTablePart table = control.getTablePart();
    table.paint(parent);
    table.addSelectionListener(e -> {
      if (e.data instanceof BuchungDokument)
      {
        auswahl = new BuchungDokument[] { (BuchungDokument) e.data };
      }
      else
      {
        auswahl = (BuchungDokument[]) e.data;
      }
    });
    table.setAction(context -> {
      if (context instanceof BuchungDokument)
      {
        data = new BuchungDokument[] { (BuchungDokument) context };
      }
      else
      {
        data = (BuchungDokument[]) context;
      }
      close();
    });

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Abbrechen", e -> close(), null, false,
        "process-stop.png");
    buttons.addButton("Auswahl Zuordnen", e -> {
      if (auswahl == null)
      {
        throw new ApplicationException("Kein Beleg ausgewählt");
      }
      data = auswahl;
      close();
    }, null, true, "document-save.png");
    buttons.paint(parent);
  }

  @Override
  protected void onEscape()
  {
    // Keine Oce werfen
    close();
  }

  @Override
  protected BuchungDokument[] getData() throws Exception
  {
    return this.data;
  }
}
