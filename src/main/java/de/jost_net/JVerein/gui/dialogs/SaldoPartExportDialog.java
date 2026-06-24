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
import de.jost_net.JVerein.io.SaldoExportParam;
import de.jost_net.JVerein.rmi.Formular;
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
    SaldoExportParam params = new SaldoExportParam();
    params.setLinks((Integer) links.getValue());
    params.setRechts((Integer) rechts.getValue());
    params.setOben((Integer) oben.getValue());
    params.setUnten((Integer) unten.getValue());
    params.setQuerformat((Boolean) querformat.getValue());
    params.setVordergrund((Formular) vordergrund.getValue());
    params.setHintergrund((Formular) hintergrund.getValue());
    params.setHeaderTransparent((Boolean) headerTransparent.getValue());
    params.setZellenTransparent((Boolean) zellenTransparent.getValue());
    params.setFontsize((Integer) fontsize.getValue());
    params.setFontsizeHeader((Integer) fontsizeHeader.getValue());
    params.setFontHeader(getFontHeader(null));
    params.setFontNormal(getFontNormal(null));
    params.setFontFett(getFontFett(null));
    params.setFontItalic(getFontKursiv(null));
    params.setColorHeader(getHintergrundHeader());
    params.setColorTable(getHintergrundTabelle());
    params.setNegativRot((Boolean) negativRot.getValue());

    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        export.export(zeile, file, title, subtitle, params);
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
