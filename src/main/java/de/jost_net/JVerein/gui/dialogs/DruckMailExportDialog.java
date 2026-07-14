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

import de.jost_net.JVerein.io.ExportLayoutParam;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class DruckMailExportDialog extends AbstractPartExportDialog
{

  public DruckMailExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename, "Report generieren");
    settings = new Settings(this.getClass());
  }

  @Override
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    createGui(parent, null);
  }

  @Override
  protected void export() throws ApplicationException
  {
    try
    {
      saveSettings();
      storeExportLayoutParam();
      success = true;
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Export";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
    close();
  }

  public ExportLayoutParam getParams()
  {
    return getExportLayoutParam();
  }

  @Override
  protected void exportCSV(File file) throws IOException
  {
    // Nicht unterstützt
  }

  @Override
  protected void exportPDF(File file) throws IOException, DocumentException
  {
    // Nicht unterstützt
  }

  @Override
  void setChecked()
  {
    // Kein Spalten Tab
  }

}
