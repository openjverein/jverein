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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.eclipse.swt.widgets.Composite;

import com.itextpdf.text.DocumentException;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class SaldoPartExportDialog extends AbstractPartExportDialog
{
  private ArrayList<PseudoDBObject> zeile;

  private ISaldoExport export;

  public SaldoPartExportDialog(ISaldoExport export,
      ArrayList<PseudoDBObject> zeile, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename);
    this.export = export;
    this.zeile = zeile;
    settings = new Settings(this.getClass());
  }

  @Override
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    createGui(parent, null);
  }

  @Override
  protected void exportCSV(File file) throws IOException
  {
    // Nicht unterstützt
  }

  @Override
  protected void exportPDF(File file) throws IOException, DocumentException
  {

    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        export.export(zeile, file, title, subtitle, getSaldoExportParam());
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }

  @Override
  void setChecked()
  {
    // Kein Spalten Tab
  }

}
