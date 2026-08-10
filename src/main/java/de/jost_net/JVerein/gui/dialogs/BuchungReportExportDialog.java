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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.io.BuchungReportAusgabe;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungReportExportDialog extends AbstractDialog<Object>
{

  private ArrayList<Buchung> buchungen;

  private Settings settings = null;

  private LabelInput status = null;

  private FormularInput formular = null;

  public BuchungReportExportDialog(int position, Buchung[] buchungen)
  {
    super(position);
    this.buchungen = new ArrayList<Buchung>(Arrays.asList(buchungen));
    setTitle("Buchungsreport");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    LabelGroup group = new LabelGroup(parent, "Parameter");
    group.addLabelPair("Formular", getFormular());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", context -> {
      try
      {
        if (getFormular().getValue() == null)
        {
          status.setValue("Bitte Formular auswählen");
          status.setColor(Color.ERROR);
          return;
        }
        new BuchungReportAusgabe((Formular) getFormular().getValue())
            .aufbereiten(buchungen, Ausgabeart.PDF_EINZELN, null, null, false,
                false, false);
      }
      catch (Exception e)
      {
        Logger.error("Fehler bei der Buchungreport Ausgabe.", e);
        throw new ApplicationException(e.getMessage());
      }
      close();
    }, null, true, "ok.png");

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

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  private FormularInput getFormular() throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    formular = new FormularInput(FormularArt.BUCHUNGSREPORT,
        settings.getString("formular.key", ""));
    formular.addListener(event -> saveSettings());
    return formular;
  }

  private void saveSettings()
  {
    {
      Formular f;
      try
      {
        if (getFormular() != null)
        {
          f = (Formular) getFormular().getValue();
          settings.setAttribute("formular.key", f == null ? "" : f.getID());
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Speichern der Settings.", e);
      }
    }
  }
}
