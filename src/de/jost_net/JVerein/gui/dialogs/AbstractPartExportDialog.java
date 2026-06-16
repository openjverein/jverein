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
import org.eclipse.swt.widgets.TabFolder;

import com.itextpdf.text.DocumentException;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
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

  private boolean success = false;

  protected Settings settings;

  protected String title;

  protected String subtitle;

  protected String filename;

  protected ExportArt art;

  protected String settingPrefix;

  protected IntegerInput links;

  protected IntegerInput rechts;

  protected IntegerInput oben;

  protected IntegerInput unten;

  protected CheckboxInput querformat;

  protected SelectInput vordergrund;

  protected SelectInput hintergrund;

  protected JVereinTablePart spaltenList;

  protected CheckboxInput headerTransparent;

  protected CheckboxInput zellenTransparent;

  public AbstractPartExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(AbstractPartExportDialog.POSITION_CENTER);
    this.title = title;
    this.subtitle = subtitle;
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";

    setTitle("Tabelle exportieren");
    setSize(400, SWT.DEFAULT);
  }

  protected void createGui(Composite parent, Action action)
      throws RemoteException
  {
    spaltenList.addColumn("Name", "text");
    spaltenList.setCheckable(true);

    if (art.equals(ExportArt.PDF))
    {
      spaltenList.addColumn("Breite", "data", null, true);
      TabFolder folder = new TabFolder(parent, SWT.BORDER);
      folder.setLayoutData(new GridData(GridData.FILL_BOTH));

      TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
      tabSpalten.addPart(spaltenList);
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Button("Breiten zurücksetzen", action, null, false,
          "eraser.png"));
      tabSpalten.addButtonArea(buttons);

      addRaenderFormularTabs(folder);
    }
    else
    {
      spaltenList.paint(parent);
    }

    setChecked();

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> export(), null, true, "ok.png");

    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    b.paint(parent);
  }

  protected void addRaenderFormularTabs(TabFolder folder) throws RemoteException
  {
    TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
    TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);

    links = new IntegerInput(settings.getInt(settingPrefix + "links", 20));
    rechts = new IntegerInput(settings.getInt(settingPrefix + "rechts", 20));
    oben = new IntegerInput(settings.getInt(settingPrefix + "oben", 20));
    unten = new IntegerInput(settings.getInt(settingPrefix + "unten", 20));
    tabRaender.addLabelPair("Links", links);
    tabRaender.addLabelPair("Rechts", rechts);
    tabRaender.addLabelPair("Oben", oben);
    tabRaender.addLabelPair("Unten", unten);

    // IntegerInput links2 = new IntegerInput(settings.getInt(id + "links2",
    // 20));
    // IntegerInput rechts2 = new IntegerInput(
    // settings.getInt(id + "rechts2", 20));
    // IntegerInput oben2 = new IntegerInput(settings.getInt(id + "oben2",
    // 20));
    // IntegerInput unten2 = new IntegerInput(settings.getInt(id + "unten2",
    // 20));
    // tabRaender.addLabelPair("Links ab 2. Seite", links2);
    // tabRaender.addLabelPair("Rechts ab 2. Seite", rechts2);
    // tabRaender.addLabelPair("Oben ab 2. Seite", oben2);
    // tabRaender.addLabelPair("Unten ab 2. Seite", unten2);

    hintergrund = new FormularInput(FormularArt.HINTERGRUND,
        settings.getString(settingPrefix + "hintergrund", ""));
    hintergrund.setPleaseChoose("Kein Formular");
    vordergrund = new FormularInput(FormularArt.HINTERGRUND,
        settings.getString(settingPrefix + "vordergrund", ""));
    vordergrund.setPleaseChoose("Kein Formular");
    headerTransparent = new CheckboxInput(settings
        .getBoolean(settingPrefix + "headerTransparent", (Boolean) Einstellungen
            .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT)));
    zellenTransparent = new CheckboxInput(settings
        .getBoolean(settingPrefix + "zellenTransparent", (Boolean) Einstellungen
            .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT)));
    querformat = new CheckboxInput(
        settings.getBoolean(settingPrefix + "quer", false));
    tabFormular.addLabelPair("Formular Hintergrund", hintergrund);
    tabFormular.addLabelPair("Formular Vordergrund", vordergrund);
    tabFormular.addLabelPair("Tabellen Header transparent", headerTransparent);
    tabFormular.addLabelPair("Tabellen Zellen transparent", zellenTransparent);
    tabFormular.addLabelPair("Querformat", querformat);
  }

  protected void export() throws ApplicationException
  {
    try
    {
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
          exportPDF(file);
          break;
      }
      saveSettings();

      FileViewer.show(file);

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

  protected void saveSettings() throws RemoteException
  {
    if (art.equals(ExportArt.PDF))
    {
      settings.setAttribute(settingPrefix + "links",
          (Integer) links.getValue());
      settings.setAttribute(settingPrefix + "rechts",
          (Integer) rechts.getValue());
      settings.setAttribute(settingPrefix + "oben", (Integer) oben.getValue());
      settings.setAttribute(settingPrefix + "unten",
          (Integer) unten.getValue());

      settings.setAttribute(settingPrefix + "hintergrund",
          hintergrund.getValue() == null ? null
              : ((Formular) hintergrund.getValue()).getID());
      settings.setAttribute(settingPrefix + "vordergrund",
          vordergrund.getValue() == null ? null
              : ((Formular) vordergrund.getValue()).getID());

      settings.setAttribute(settingPrefix + "headerTransparent",
          (Boolean) headerTransparent.getValue());
      settings.setAttribute(settingPrefix + "zellenTransparent",
          (Boolean) zellenTransparent.getValue());

      settings.setAttribute(settingPrefix + "quer",
          (Boolean) querformat.getValue());
    }
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return success;
  }

  abstract void setChecked();

  abstract void exportCSV(File file) throws IOException;

  abstract void exportPDF(File file) throws IOException, DocumentException;

}
