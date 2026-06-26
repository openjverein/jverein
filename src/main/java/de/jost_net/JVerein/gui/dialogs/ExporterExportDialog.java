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
import org.eclipse.swt.widgets.Composite;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.io.Exporter;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.IOFormat;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ExporterExportDialog extends AbstractPartExportDialog
{
  private Object[] objects = null;

  private IOFormat format;

  private Exporter exporter;

  private boolean open;

  public ExporterExportDialog(Exporter exporter, Object[] objects,
      IOFormat format, String settingPrefix, ExportArt art, String title,
      String subtitle, String filename, boolean open)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename, "Report generieren");
    this.exporter = exporter;
    this.objects = objects;
    this.format = format;
    this.open = open;
    supportTable2 = exporter.hasColortable2(objects);
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
        try
        {
          exporter.doExport(objects, format, file, getExportLayoutParam(),
              monitor);
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          GUI.getStatusBar().setSuccessText(
              String.format("Daten exportiert nach %s", file.getParent()));
          monitor.setStatusText(
              String.format("Daten exportiert nach %s", file.getParent()));

          if (open)
          {
            FileViewer.show(file);
          }
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String s = file.getParent();
          Logger.error("error while writing objects to " + s, e);
          ApplicationException ae = new ApplicationException(
              String.format("Fehler beim Exportieren der Daten in %s", s), e);
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
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
