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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.FontInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.TabelleExportProfilePart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
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
import de.willuhn.jameica.gui.parts.Column;
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

  protected String title;

  protected String subtitle;

  protected String filename;

  protected ExportArt art;

  protected String settingPrefix;

  public IntegerInput links;

  public IntegerInput rechts;

  public IntegerInput oben;

  public IntegerInput unten;

  public CheckboxInput querformat;

  public SelectInput vordergrund;

  public SelectInput hintergrund;

  public CheckboxInput headerTransparent;

  public CheckboxInput zellenTransparent;

  public SelectInput fontHeader;

  public SelectInput fontNormal;

  public SelectInput fontFett;

  public SelectInput fontItalic;

  public IntegerInput fontsizeHeader;

  public IntegerInput fontsize;

  public CheckboxInput negativRot;

  public ColorInput colorHeader;

  public ColorInput colorTable;

  public ColorInput colorTable2;

  public boolean supportTable2;

  private ExportLayoutParam params;

  public IJVereinPart part;

  public List<ExportSpalte> colList;

  public JVereinTablePart spaltenList;

  public TabelleExportParam guiparams;

  public AbstractPartExportDialog(String settingPrefix, ExportArt art,
      String title, String subtitle, String filename, String dialogTitel,
      IJVereinPart part) throws ApplicationException
  {
    super(AbstractPartExportDialog.POSITION_CENTER);
    this.title = title;
    this.subtitle = subtitle;
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";
    this.part = part;
    setTitle(dialogTitel);
    setSize(400, 700);
  }

  protected void createGui(Composite parent, Action action)
      throws RemoteException, ApplicationException
  {
    // Falls nicht in paint() passiert
    if (guiparams == null)
    {
      guiparams = new TabelleExportParam(art);
      guiparams.importFromSettings(this, settings, settingPrefix,
          spaltenList != null);
    }

    if (spaltenList != null)
    {
      spaltenList.addColumn("Spalten", "column.name");
      spaltenList.setCheckable(true);
    }

    if (this instanceof TablePartExportDialog)
    {
      new TabelleExportProfilePart(this, settingPrefix).paint(parent);
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

    b.addButton(new HelpButton(DokumentationUtil.ALLGEMEINES));

    if (art.equals(ExportArt.CSV))
    {
      b.addButton("Reset", c -> resetSpalten(), null, false, "edit-undo.png");
    }

    b.addButton("Starten", c -> export(), null, true, "walking.png");

    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    b.paint(parent);
  }

  private void resetRaender()
  {
    // Ränder
    links.setValue(TabelleExportParam.DEFAULT_LINKS);
    rechts.setValue(TabelleExportParam.DEFAULT_RECHTS);
    oben.setValue(TabelleExportParam.DEFAULT_OBEN);
    unten.setValue(TabelleExportParam.DEFAULT_UNTEN);
  }

  private void resetFormular() throws ApplicationException
  {
    // Formular
    try
    {
      hintergrund.setValue(TabelleExportParam.DEFAULT_HINTERGRUND);
      vordergrund.setValue(TabelleExportParam.DEFAULT_VORDERGRUND);
      headerTransparent.setValue((Boolean) Einstellungen
          .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT));
      zellenTransparent.setValue((Boolean) Einstellungen
          .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT));
      querformat.setValue(TabelleExportParam.DEFAULT_QUERFORMAT);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Reset im Tabelle-Export-Dialog", e);
      throw new ApplicationException("Serverfehler");
    }
  }

  private void resetSchriftart()
  {
    // Schriftart
    fontHeader.setValue(TabelleExportParam.DEFAULT_FONT_HEADER);
    fontNormal.setValue(TabelleExportParam.DEFAULT_FONT_NORMAL);
    fontFett.setValue(TabelleExportParam.DEFAULT_FONT_FETT);
    fontItalic.setValue(TabelleExportParam.DEFAULT_FONT_ITALIC);
    fontsize.setValue(TabelleExportParam.DEFAULT_FONT_SIZE);
    fontsizeHeader.setValue(TabelleExportParam.DEFAULT_FONT_SIZE_HEADER);
    negativRot.setValue(TabelleExportParam.DEFAULT_NEGATIV_ROT);
    Color col = new Color(TabelleExportParam.DEFAULT_HEADER_COLOR_RED,
        TabelleExportParam.DEFAULT_HEADER_COLOR_GREEN,
        TabelleExportParam.DEFAULT_HEADER_COLOR_BLUE);
    colorHeader.setValue(col);
    col = new Color(TabelleExportParam.DEFAULT_COLOR_RED,
        TabelleExportParam.DEFAULT_COLOR_GREEN,
        TabelleExportParam.DEFAULT_COLOR_BLUE);
    colorTable.setValue(col);
    col = new Color(TabelleExportParam.DEFAULT_COLOR_RED2,
        TabelleExportParam.DEFAULT_COLOR_GREEN2,
        TabelleExportParam.DEFAULT_COLOR_BLUE2);
    colorTable2.setValue(col);
  }

  protected void zeichnePDF(Composite parent, Action action)
      throws RemoteException, ApplicationException
  {
    TabFolder folder = new TabFolder(parent, SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    // Spalten
    if (spaltenList != null)
    {
      TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
      spaltenList.addColumn("Breite", "breite", null, true);
      tabSpalten.addPart(spaltenList);
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Button("Breiten zurücksetzen", action, null, false,
          "edit-undo.png"));
      buttons.addButton("Reset", c -> resetSpalten(), null, false,
          "edit-undo.png");
      tabSpalten.addButtonArea(buttons);
    }

    TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
    TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);
    TabGroup tabFont = new TabGroup(folder, "Schriftart", true, 2);

    // Ränder
    links = new IntegerInput(guiparams.getLinks());
    rechts = new IntegerInput(guiparams.getRechts());
    oben = new IntegerInput(guiparams.getOben());
    unten = new IntegerInput(guiparams.getUnten());
    tabRaender.addLabelPair("Links", links);
    tabRaender.addLabelPair("Rechts", rechts);
    tabRaender.addLabelPair("Oben", oben);
    tabRaender.addLabelPair("Unten", unten);
    ButtonArea rbuttons = new ButtonArea();
    rbuttons.addButton("Reset", c -> resetRaender(), null, false,
        "edit-undo.png");
    tabRaender.addButtonArea(rbuttons);

    // Formular
    hintergrund = new FormularInput(FormularArt.HINTERGRUND,
        guiparams.getHintergrund());
    hintergrund.setPleaseChoose("Kein Formular");
    vordergrund = new FormularInput(FormularArt.HINTERGRUND,
        guiparams.getVordergrund());
    vordergrund.setPleaseChoose("Kein Formular");
    headerTransparent = new CheckboxInput(guiparams.isHeader_transparent());
    zellenTransparent = new CheckboxInput(guiparams.isZellen_transparent());
    querformat = new CheckboxInput(guiparams.isQuerformat());
    tabFormular.addLabelPair("Formular Hintergrund", hintergrund);
    tabFormular.addLabelPair("Formular Vordergrund", vordergrund);
    tabFormular.addLabelPair("Tabellen Header transparent", headerTransparent);
    tabFormular.addLabelPair("Tabellen Zellen transparent", zellenTransparent);
    tabFormular.addLabelPair("Querformat", querformat);
    ButtonArea fbuttons = new ButtonArea();
    fbuttons.addButton("Reset", c -> resetFormular(), null, false,
        "edit-undo.png");
    tabFormular.addButtonArea(fbuttons);

    // Schriftart
    fontHeader = new FontInput(guiparams.getFont_header());
    fontNormal = new FontInput(guiparams.getFont_normal());
    fontFett = new FontInput(guiparams.getFont_fett());
    fontItalic = new FontInput(guiparams.getFont_italic());
    fontsize = new IntegerInput(guiparams.getFont_size());
    fontsizeHeader = new IntegerInput(guiparams.getFont_size_header());
    negativRot = new CheckboxInput(guiparams.isNegativ_rot());
    Color col = new Color(guiparams.getHeader_color_red(),
        guiparams.getHeader_color_green(), guiparams.getHeader_color_blue());
    colorHeader = new ColorInput(col, false);
    col = new Color(guiparams.getColor_red(), guiparams.getColor_green(),
        guiparams.getColor_blue());
    colorTable = new ColorInput(col, false);
    col = new Color(guiparams.getColor_red2(), guiparams.getColor_green2(),
        guiparams.getColor_blue2());
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
    ButtonArea sbuttons = new ButtonArea();
    sbuttons.addButton("Reset", c -> resetSchriftart(), null, false,
        "edit-undo.png");
    tabFont.addButtonArea(sbuttons);
  }

  protected void export() throws ApplicationException
  {
    try
    {
      saveSettings();

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
          storeExportLayoutParam();
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
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    guiparams = new TabelleExportParam(art);
    guiparams.importFromSettings(this, settings, settingPrefix, true);
    colList = new ArrayList<>();
    for (Column col : part.getAllColums())
    {
      int breite = 0;
      if (art.equals(ExportArt.PDF))
      {
        breite = guiparams.getBreiteMap().get(col.getName());
        if (breite == 0)
        {
          Item i = getColumn(col.getName());
          if (i instanceof TreeColumn)
          {
            breite = ((TreeColumn) i).getWidth();
          }
          else if (i instanceof TableColumn)
          {
            breite = ((TableColumn) i).getWidth();
          }
        }
      }
      colList.add(new ExportSpalte(col, breite));
    }
    String[] spaltenNamen = guiparams.getOrder().split(",");
    colList.sort(Comparator.comparingInt(
        obj -> Arrays.asList(spaltenNamen).indexOf(obj.getColumn().getName())));

    spaltenList = new JVereinTablePart(colList, null)
    {
      // Sortieren verhindern
      @Override
      protected void orderBy(int index)
      {
        return;
      }
    };
    if (art.equals(ExportArt.PDF))
    {
      spaltenList.addChangeListener((object, attribute, newValue) -> {
        try
        {
          ((ExportSpalte) object).setBreite(Integer.parseInt(newValue));
        }
        catch (Exception e)
        {
          throw new ApplicationException("Ungültiger Wert");
        }
      });
    }
    createGui(parent, c -> setWidth());
    spaltenList.setDragDrop();
  }

  public void saveSettings() throws RemoteException
  {
    guiparams.importfromGui(this, settings, settingPrefix);
    guiparams.exportToSettings(this, settings, settingPrefix,
        spaltenList != null);
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

  void resetSpalten() throws ApplicationException
  {
    try
    {
      spaltenList.removeAll();
      colList = new ArrayList<>();
      for (Column i : part.getAllColums())
      {
        Item c = getColumn(i.getName());
        int breite = 0;
        if (c instanceof TreeColumn)
        {
          breite = ((TreeColumn) c).getWidth();
        }
        else if (c instanceof TableColumn)
        {
          breite = ((TableColumn) c).getWidth();
        }
        colList.add(new ExportSpalte(i, breite));
        ExportSpalte item = new ExportSpalte(i, breite);
        spaltenList.addItem(item);
        spaltenList.setChecked(item, breite > 0);
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler beim zurücksetzen der Spalten", re);
      throw new ApplicationException("Fehler beim zurücksetzen der Spalten");
    }
  }

  @SuppressWarnings("unchecked")
  void setWidth() throws ApplicationException
  {
    try
    {
      for (ExportSpalte e : (List<ExportSpalte>) spaltenList.getItems(false))
      {
        Item c = getColumn(e.getColumn().getName());
        int breite = 0;
        if (c instanceof TreeColumn)
        {
          breite = ((TreeColumn) c).getWidth();
        }
        else if (c instanceof TableColumn)
        {
          breite = ((TableColumn) c).getWidth();
        }
        e.setBreite(breite);
        spaltenList.updateItem(e, e);
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler beim zurücksetzen der Breiten", re);
      throw new ApplicationException("Fehler beim zurücksetzen der Breiten");
    }
  }

  void setChecked()
  {
    for (ExportSpalte sp : colList)
    {
      Integer checked = guiparams.getAnzeigenMap()
          .get(sp.getColumn().getName());
      Boolean ischecked = part.getColums().contains(sp.getColumn());
      if (checked == TabelleExportParam.CHECKED)
      {
        ischecked = true;
      }
      else if (checked == TabelleExportParam.UNCHECKED)
      {
        ischecked = false;
      }
      spaltenList.setChecked(sp, ischecked);
    }
  }

  abstract void exportCSV(File file) throws IOException;

  abstract void exportPDF(File file)
      throws IOException, DocumentException, ApplicationException;

  abstract Item getColumn(String name);

  /**
   * Hilfsklasse für die Spalten inkl. Breite
   */
  public class ExportSpalte
  {
    private int breite;

    private Column column;

    private int align;

    public ExportSpalte(Column column, int breite)
    {
      this.column = column;
      this.breite = breite;
    }

    public void setBreite(int breite)
    {
      this.breite = breite;
    }

    public int getBreite()
    {
      return breite;
    }

    public void setAlign(int align)
    {
      this.align = align;
    }

    public int getAlign()
    {
      return align;
    }

    public Column getColumn()
    {
      return column;
    }
  }

  public Settings getSettings()
  {
    return settings;
  }
}
