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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.FontInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.ExportLayoutParam;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
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

  protected SelectInput fontHeader;

  protected SelectInput fontNormal;

  protected SelectInput fontFett;

  protected SelectInput fontItalic;

  protected IntegerInput fontsizeHeader;

  protected IntegerInput fontsize;

  protected CheckboxInput negativRot;

  protected ColorInput colorHeader;

  protected ColorInput colorTable;

  protected ColorInput colorTable2;

  protected boolean supportTable2;

  private ExportLayoutParam params;

  public AbstractPartExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename, String dialogTitel)
      throws ApplicationException
  {
    super(AbstractPartExportDialog.POSITION_CENTER);
    this.title = title;
    this.subtitle = subtitle;
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";

    setTitle(dialogTitel);
    setSize(400, SWT.DEFAULT);
  }

  protected void createGui(Composite parent, Action action)
      throws RemoteException
  {
    if (spaltenList != null)
    {
      spaltenList.addColumn("Name", "text");
      spaltenList.setCheckable(true);
    }

    if (art.equals(ExportArt.PDF))
    {
      zeichnePDF(parent, action);
    }
    else if (spaltenList != null)
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

  protected void zeichnePDF(Composite parent, Action action)
      throws RemoteException
  {
    TabFolder folder = new TabFolder(parent, SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    // Spalten
    if (spaltenList != null)
    {
      TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
      spaltenList.addColumn("Breite", "data", null, true);
      tabSpalten.addPart(spaltenList);
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Button("Breiten zurücksetzen", action, null, false,
          "eraser.png"));
      tabSpalten.addButtonArea(buttons);
    }

    TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
    TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);
    TabGroup tabFont = new TabGroup(folder, "Schriftart", true, 2);

    // Ränder
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

    // Formular
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

    // Schriftart
    fontHeader = new FontInput(
        settings.getString(settingPrefix + "font_header", "FreeSans"));
    fontNormal = new FontInput(
        settings.getString(settingPrefix + "font_normal", "FreeSans"));
    fontFett = new FontInput(
        settings.getString(settingPrefix + "font_fett", "FreeSans-Bold"));
    fontItalic = new FontInput(
        settings.getString(settingPrefix + "font_italic", "FreeSans-Oblique"));
    fontsize = new IntegerInput(settings.getInt(settingPrefix + "fontsize", 8));
    fontsizeHeader = new IntegerInput(
        settings.getInt(settingPrefix + "fontsize_header", 8));
    negativRot = new CheckboxInput(
        settings.getBoolean(settingPrefix + "negativ_rot", true));
    Color col = new Color(
        (int) settings.getInt(settingPrefix + "header_color_red", 192),
        (int) settings.getInt(settingPrefix + "header_color_green", 192),
        (int) settings.getInt(settingPrefix + "header_color_blue", 192));
    colorHeader = new ColorInput(col, false);
    col = new Color((int) settings.getInt(settingPrefix + "color_red", 192),
        (int) settings.getInt(settingPrefix + "color_green", 192),
        (int) settings.getInt(settingPrefix + "color_blue", 192));
    colorTable = new ColorInput(col, false);
    col = new Color((int) settings.getInt(settingPrefix + "color_red2", 230),
        (int) settings.getInt(settingPrefix + "color_green2", 230),
        (int) settings.getInt(settingPrefix + "color_blue2", 230));
    colorTable2 = new ColorInput(col, false);
    tabFont.addHeadline("Tabellen Spaltennamen");
    tabFont.addLabelPair("Schriftart", fontHeader);
    tabFont.addLabelPair("Schriftgröße", fontsizeHeader);
    tabFont.addLabelPair("Hintergrund Farbe", colorHeader);
    tabFont.addHeadline("Tabellen Inhalt");
    tabFont.addLabelPair("Schriftart Standard", fontNormal);
    tabFont.addLabelPair("Schriftart Fett", fontFett);
    tabFont.addLabelPair("Schriftart Kursiv", fontItalic);
    tabFont.addLabelPair("Schriftgröße", fontsize);
    tabFont.addLabelPair("Hintergrund Farbe *", colorTable);
    if (supportTable2)
    {
      tabFont.addLabelPair("Hintergrund Farbe *", colorTable2);
    }
    tabFont.addLabelPair("Negative Werte in Rot", negativRot);
    tabFont.addSeparator();
    tabFont.addText("* Bei Zeilen mit Hintergrundfarbe", false);
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

      storeExportLayoutParam();
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

      settings.setAttribute(settingPrefix + "font_header",
          (String) fontHeader.getValue());
      settings.setAttribute(settingPrefix + "font_normal",
          (String) fontNormal.getValue());
      settings.setAttribute(settingPrefix + "font_fett",
          (String) fontFett.getValue());
      settings.setAttribute(settingPrefix + "font_italic",
          (String) fontItalic.getValue());
      settings.setAttribute(settingPrefix + "fontsize_header",
          (Integer) fontsizeHeader.getValue());
      settings.setAttribute(settingPrefix + "fontsize",
          (Integer) fontsize.getValue());
      settings.setAttribute(settingPrefix + "negativ_rot",
          (Boolean) negativRot.getValue());
      Color col = (Color) colorHeader.getValue();
      settings.setAttribute(settingPrefix + "header_color_red",
          (Integer) col.getRed());
      settings.setAttribute(settingPrefix + "header_color_green",
          (Integer) col.getGreen());
      settings.setAttribute(settingPrefix + "header_color_blue",
          (Integer) col.getBlue());
      col = (Color) colorTable.getValue();
      settings.setAttribute(settingPrefix + "color_red",
          (Integer) col.getRed());
      settings.setAttribute(settingPrefix + "color_green",
          (Integer) col.getGreen());
      settings.setAttribute(settingPrefix + "color_blue",
          (Integer) col.getBlue());
      if (supportTable2)
      {
        col = (Color) colorTable2.getValue();
        settings.setAttribute(settingPrefix + "color_red2",
            (Integer) col.getRed());
        settings.setAttribute(settingPrefix + "color_green2",
            (Integer) col.getGreen());
        settings.setAttribute(settingPrefix + "color_blue2",
            (Integer) col.getBlue());
      }
    }
  }

  protected Font getFont(String text, FontData[] data)
  {
    BaseColor color = BaseColor.BLACK;
    try
    {
      String text2 = text.replaceAll("\\.", "").replaceAll("\\,", "\\.");
      Double value = Double.valueOf(text2);
      if (value < 0)
      {
        color = BaseColor.RED;
      }
    }
    catch (NumberFormatException ex)
    {
      // Dann bleibt es Schwarz
    }
    for (FontData fdata : data)
    {
      switch (fdata.getStyle())
      {
        case SWT.BOLD:
          return getFontFett(color);
        case SWT.ITALIC:
          return getFontKursiv(color);
        case SWT.NORMAL:
          return getFontNormal(color);
      }
    }
    return null;
  }

  protected Font getFontHeader(BaseColor color)
  {
    return FontFactory.getFont(
        "/fonts/" + (String) fontHeader.getValue() + ".ttf",
        BaseFont.IDENTITY_H, (Integer) fontsizeHeader.getValue(),
        Font.UNDEFINED, color);
  }

  protected BaseColor getHintergrundHeader()
  {
    Color col = (Color) colorHeader.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  protected BaseColor getHintergrundTabelle()
  {
    Color col = (Color) colorTable.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  protected BaseColor getHintergrundTabelle2()
  {
    Color col = (Color) colorTable2.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  protected Font getFontNormal(BaseColor color)
  {
    return FontFactory.getFont(
        "/fonts/" + (String) fontNormal.getValue() + ".ttf",
        BaseFont.IDENTITY_H, (Integer) fontsize.getValue(), Font.UNDEFINED,
        color);
  }

  protected Font getFontFett(BaseColor color)
  {
    return FontFactory.getFont(
        "/fonts/" + (String) fontFett.getValue() + ".ttf", BaseFont.IDENTITY_H,
        (Integer) fontsize.getValue(), Font.UNDEFINED, color);
  }

  protected Font getFontKursiv(BaseColor color)
  {
    return FontFactory.getFont(
        "/fonts/" + (String) fontItalic.getValue() + ".ttf",
        BaseFont.IDENTITY_H, (Integer) fontsize.getValue(), Font.UNDEFINED,
        color);
  }

  protected void storeExportLayoutParam()
  {
    params = new ExportLayoutParam();
    params.setTitle(title);
    params.setSubtitle(subtitle);
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
    if (supportTable2)
    {
      params.setColorTable2(getHintergrundTabelle2());
    }
    params.setNegativRot((Boolean) negativRot.getValue());
  }

  protected ExportLayoutParam getExportLayoutParam()
  {
    return params;
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
