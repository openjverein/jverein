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

import de.jost_net.JVerein.gui.control.BelegControl;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class BelegAuswahlDialog extends AbstractDialog<Beleg[]>
{
  private Beleg[] data;

  private Beleg[] auswahl;

  public BelegAuswahlDialog()
  {
    super(BelegAuswahlDialog.POSITION_CENTER);
    super.setSize(1200, 700);
    setTitle("Belege");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    final BelegControl control = new BelegControl(null);

    LabelGroup group = new LabelGroup(parent, "Filter");
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 3);

    SimpleContainer left = new SimpleContainer(cl.getComposite());
    left.addInput(control.getFilterInput(Filter.BELEGNUMMER));
    left.addInput(control.getFilterInput(Filter.BEMERKUNG));
    left.addLabelPair("Nicht zugeordnet",
        control.getFilterInput(Filter.NICHT_ZUGEORDNET));

    SimpleContainer middle = new SimpleContainer(cl.getComposite());
    middle.addInput(control.getFilterInput(Filter.BELEG_ART));
    middle.addInput(control.getFilterInput(Filter.BELEG_GRUPPE));
    middle.addInput(control.getFilterInput(Filter.KONTO));

    SimpleContainer right = new SimpleContainer(cl.getComposite());
    Input von = control.getFilterInput(Filter.DATUM_VON);
    right.addInput(von);
    Input bis = control.getFilterInput(Filter.DATUM_BIS);
    right.addInput(bis);
    right.addInput(control.getFilterInput(Filter.MITGLIED_SELECT));

    ButtonArea fbuttons = new ButtonArea();
    ToolTipButton zurueck = control.getZurueckButton(von, bis);
    fbuttons.addButton(zurueck);
    ToolTipButton vor = control.getVorButton(von, bis);
    fbuttons.addButton(vor);
    fbuttons.addButton(control.getResetButton());
    fbuttons.addButton(control.getSuchenButton());
    group.addButtonArea(fbuttons);
    zurueck.setToolTipText("Datumsbereich zurück");
    vor.setToolTipText("Datumsbereich vowärts");

    JVereinTablePart table = control.getTablePart();
    table.paint(parent);
    table.addSelectionListener(e -> {
      if (e.data instanceof Beleg)
      {
        auswahl = new Beleg[] { (Beleg) e.data };
      }
      else
      {
        auswahl = (Beleg[]) e.data;
      }
    });
    table.setAction(context -> {
      if (context instanceof Beleg)
      {
        data = new Beleg[] { (Beleg) context };
      }
      else
      {
        data = (Beleg[]) context;
      }
      close();
    });

    ButtonArea buttons = new ButtonArea();

    buttons.addButton("Auswahl Zuordnen", e -> {
      if (auswahl == null)
      {
        throw new ApplicationException("Kein Beleg ausgewählt");
      }
      data = auswahl;
      close();
    }, null, true, "document-save.png");
    buttons.addButton("Abbrechen", e -> close(), null, false,
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
  protected Beleg[] getData() throws Exception
  {
    return this.data;
  }
}
