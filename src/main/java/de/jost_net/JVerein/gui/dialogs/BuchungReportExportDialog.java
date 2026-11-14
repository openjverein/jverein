/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.BuchungReportExportDialogControl;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class BuchungReportExportDialog extends AbstractDialog<Object>
{

  private BuchungReportExportDialogControl control;

  public BuchungReportExportDialog(int position, Buchung[] buchungen,
      BuchungsControl bcontrol)
  {
    super(position);
    setTitle("Buchungsreport");
    control = new BuchungReportExportDialogControl(this, buchungen, bcontrol);
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    LabelGroup group = new LabelGroup(parent, "Parameter");
    group.addInput(control.getFormular());
    if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG))
    {
      group.addLabelPair("Als Buchungsdokument speichern",
          control.getBuchungsdokumentSpeichern());
      group.addInput(control.getDatum());
    }
    group.addInput(control.getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getUebernehmenButton());
    buttons.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    group.addButtonArea(buttons);
  }

  @Override
  protected Object getData()
  {
    return null;
  }

}
