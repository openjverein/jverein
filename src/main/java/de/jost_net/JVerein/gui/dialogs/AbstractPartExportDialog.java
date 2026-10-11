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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TabFolder;
import com.itextpdf.text.DocumentException;
import de.jost_net.JVerein.gui.control.AbstractPartExportDialogControl;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.Fonts;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractPartExportDialog extends AbstractDialog<Boolean>
{
  public enum ExportArt
  {
    PDF,
    CSV
  }

  protected boolean success = false;

  protected Settings settings;

  protected String filename;

  protected ExportArt art;

  protected String settingPrefix;

  protected boolean supportTable2;

  protected AbstractPartExportDialogControl control = null;

  public AbstractPartExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename, String dialogTitel,
      IJVereinPart part) throws ApplicationException
  {
    this(settingPrefix, art, title, subtitle, filename, dialogTitel, part,
        false);
  }

  public AbstractPartExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename, String dialogTitel,
      IJVereinPart part, boolean supportTable2) throws ApplicationException
  {
    super(AbstractPartExportDialog.POSITION_CENTER);
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";
    this.supportTable2 = supportTable2;
    settings = new Settings(this.getClass());
    this.control = new AbstractPartExportDialogControl(this, settings,
        this.settingPrefix, art, part, title, subtitle);

    setTitle(dialogTitel);
    setSize(400, 750);

    Fonts.register();
  }

  @Override
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    control.createSpaltenList();
    createGui(parent);
    control.getSpaltenList().setDragDrop();
  }

  protected void createGui(Composite parent)
      throws RemoteException, ApplicationException
  {
    if (control.getSpaltenList() != null)
    {
      control.getSpaltenList().addColumn("Spalten", "column.name");
      control.getSpaltenList().setCheckable(true);
    }

    if (art.equals(ExportArt.PDF))
    {
      zeichnePDF(parent);
    }
    else if (control.getSpaltenList() != null)
    {
      control.getSpaltenList().paint(parent);
    }

    control.setChecked();

    ButtonArea b = new ButtonArea();

    b.addButton(new HelpButton(DokumentationUtil.ALLGEMEINES));

    if (art.equals(ExportArt.CSV))
    {
      b.addButton("Reset", c -> control.resetSpalten(), null, false,
          "edit-undo.png");
    }

    b.addButton("Starten", c -> export(), null, true, "walking.png");

    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    b.paint(parent);
  }

  protected void zeichnePDF(Composite parent)
      throws RemoteException, ApplicationException
  {
    TabFolder folder = new TabFolder(parent, SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    // Spalten
    if (control.getSpaltenList() != null)
    {
      TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
      control.getSpaltenList().addColumn("Breite", "breite", null, true);
      tabSpalten.addPart(control.getSpaltenList());
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Button("Breiten zurücksetzen",
          c -> control.setWidth(), null, false, "edit-undo.png"));
      buttons.addButton("Reset", c -> control.resetSpalten(), null, false,
          "edit-undo.png");
      tabSpalten.addButtonArea(buttons);
    }

    // Ränder
    TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
    tabRaender.addInput(control.getLinks());
    tabRaender.addInput(control.getRechts());
    tabRaender.addInput(control.getOben());
    tabRaender.addInput(control.getUnten());
    ButtonArea rbuttons = new ButtonArea();
    rbuttons.addButton("Reset", c -> control.resetRaender(), null, false,
        "edit-undo.png");
    tabRaender.addButtonArea(rbuttons);

    // Formular
    TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);
    tabFormular.addInput(control.getHintergrund());
    tabFormular.addInput(control.getVordergrund());
    tabFormular.addInput(control.getHeaderTransparent());
    tabFormular.addInput(control.getZellenTransparent());
    tabFormular.addInput(control.getQuerformat());
    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton("Reset", c -> control.resetFormular(), null, false,
        "edit-undo.png");
    tabFormular.addButtonArea(fbuttons);

    // Schriftart
    TabGroup tabFont = new TabGroup(folder, "Schriftart", true, 2);
    tabFont.addHeadline("Tabellen Spaltennamen");
    tabFont.addInput(control.getFontHeader());
    tabFont.addInput(control.getFontsizeHeader());
    tabFont.addInput(control.getHeaderColor());
    tabFont.addHeadline("Tabellen Inhalt");
    tabFont.addInput(control.getFontNormal());
    tabFont.addInput(control.getFontFett());
    tabFont.addInput(control.getFontItalic());
    tabFont.addInput(control.getFontsize());
    tabFont.addInput(control.getTableColor());
    if (supportTable2)
    {
      tabFont.addInput(control.getTableColor2());
    }
    tabFont.addInput(control.getNegativRot());
    tabFont.addSeparator();
    tabFont.addText("* Bei Zeilen mit Hintergrundfarbe", false);
    ButtonArea sbuttons = new ButtonArea();
    sbuttons.addButton("Reset", c -> control.resetSchriftart(), null, false,
        "edit-undo.png");
    tabFont.addButtonArea(sbuttons);
  }

  protected void export() throws ApplicationException
  {
    try
    {
      control.saveSettings(settingPrefix);

      String extension = "";
      switch (art)
      {
        case CSV:
          extension = ".csv";
          break;
        case PDF:
          extension = ".pdf";
          break;
      }

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");
      fd.setOverwrite(true);
      String path = settings.getString(settingPrefix + "lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }

      fd.setFileName(filename);
      fd.setFilterExtensions(new String[] { "*" + extension });

      final String p = fd.open();

      if (p == null || p.length() == 0)
      {
        throw new OperationCanceledException("Abgebrochen");
      }

      File file = new File(p);
      settings.setAttribute(settingPrefix + "lastdir", file.getParent());

      switch (art)
      {
        case CSV:
          exportCSV(file);
          break;
        case PDF:
          control.storeExportLayoutParam();
          exportPDF(file);
          break;
      }

      success = true;
      close();
    }
    catch (IOException | DocumentException e)
    {
      String fehler = "Fehler beim Export";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return success;
  }

  public void saveSettings(String prefix) throws RemoteException
  {
    control.saveSettings(prefix);
  }

  public void loadSettings(String prefix) throws RemoteException
  {
    control.loadSettings(prefix);
  }

  abstract void exportCSV(File file) throws IOException;

  abstract void exportPDF(File file)
      throws IOException, DocumentException, ApplicationException;

  abstract public Item getColumn(String name);
}
