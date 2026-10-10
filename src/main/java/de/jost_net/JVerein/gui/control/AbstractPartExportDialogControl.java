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

package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.input.FontInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.ExportLayoutParam;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbstractPartExportDialogControl
{
  private static final int DEFAULT_LINKS = 20;

  private static final int DEFAULT_RECHTS = 20;

  private static final int DEFAULT_OBEN = 20;

  private static final int DEFAULT_UNTEN = 20;

  private static final String DEFAULT_HINTERGRUND = null;

  private static final String DEFAULT_VORDERGRUND = null;

  private static final boolean DEFAULT_QUERFORMAT = false;

  private static final boolean DEFAULT_NEGATIV_ROT = true;

  private static final String DEFAULT_FONT_HEADER = "FreeSans";

  private static final String DEFAULT_FONT_NORMAL = "FreeSans";

  private static final String DEFAULT_FONT_FETT = "FreeSans-Bold";

  private static final String DEFAULT_FONT_ITALIC = "FreeSans-Oblique";

  private static final int DEFAULT_FONT_SIZE = 8;

  private static final int DEFAULT_FONT_SIZE_HEADER = 8;

  private static final int DEFAULT_HEADER_COLOR_RED = 192;

  private static final int DEFAULT_HEADER_COLOR_BLUE = 192;

  private static final int DEFAULT_HEADER_COLOR_GREEN = 192;

  private static final int DEFAULT_COLOR_RED = 192;

  private static final int DEFAULT_COLOR_BLUE = 192;

  private static final int DEFAULT_COLOR_GREEN = 192;

  private static final int DEFAULT_COLOR_RED2 = 230;

  private static final int DEFAULT_COLOR_BLUE2 = 230;

  private static final int DEFAULT_COLOR_GREEN2 = 230;

  protected IntegerInput links;

  protected IntegerInput rechts;

  protected IntegerInput oben;

  protected IntegerInput unten;

  protected CheckboxInput querformat;

  protected FormularInput vordergrund;

  protected FormularInput hintergrund;

  protected CheckboxInput headerTransparent;

  protected CheckboxInput zellenTransparent;

  protected FontInput fontHeader;

  protected FontInput fontNormal;

  protected FontInput fontFett;

  protected FontInput fontItalic;

  protected IntegerInput fontsizeHeader;

  protected IntegerInput fontsize;

  protected CheckboxInput negativRot;

  protected ColorInput colorHeader;

  protected ColorInput colorTable;

  protected ColorInput colorTable2;

  protected JVereinTablePart spaltenList;

  protected List<ExportSpalte> colList;

  protected ExportLayoutParam params;

  protected ExportArt art;

  protected String settingPrefix;

  protected Settings settings;

  protected IJVereinPart part;

  protected AbstractPartExportDialog dialog;

  protected String title;

  protected String subtitle;

  public AbstractPartExportDialogControl(AbstractPartExportDialog dialog,
      Settings settings, String settingPrefix, ExportArt art, IJVereinPart part,
      String title, String subtitle)
  {
    this.dialog = dialog;
    this.settings = settings;
    this.settingPrefix = settingPrefix;
    this.art = art;
    this.part = part;
    this.title = title;
    this.subtitle = subtitle;
  }

  public String getTitle()
  {
    return title;
  }

  public String getSubtitle()
  {
    return subtitle;
  }

  public JVereinTablePart getSpaltenList()
  {
    return spaltenList;
  }

  public void createSpaltenList() throws ApplicationException
  {
    colList = new ArrayList<>();
    for (Column col : part.getAllColums())
    {
      int breite = settings.getInt(settingPrefix + "breite." + col.getName(),
          0);
      if (breite == 0)
      {
        Item i = dialog.getColumn(col.getName());
        if (i instanceof TreeColumn)
        {
          breite = ((TreeColumn) i).getWidth();
        }
        else if (i instanceof TableColumn)
        {
          breite = ((TableColumn) i).getWidth();
        }
      }
      colList.add(new ExportSpalte(col, breite));
    }
    String[] spaltenNamen = settings.getString(settingPrefix + "order", "")
        .split(",");
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
  }

  public IntegerInput getLinks()
  {
    if (links == null)
    {
      links = new IntegerInput(
          settings.getInt(settingPrefix + "links", DEFAULT_LINKS));
      links.setName("Links");
    }
    return links;
  }

  public IntegerInput getRechts()
  {
    if (rechts == null)
    {
      rechts = new IntegerInput(
          settings.getInt(settingPrefix + "rechts", DEFAULT_RECHTS));
      rechts.setName("Rechts");
    }
    return rechts;
  }

  public IntegerInput getOben()
  {
    if (oben == null)
    {
      oben = new IntegerInput(
          settings.getInt(settingPrefix + "oben", DEFAULT_OBEN));
      oben.setName("Oben");
    }
    return oben;
  }

  public IntegerInput getUnten()
  {
    if (unten == null)
    {
      unten = new IntegerInput(
          settings.getInt(settingPrefix + "unten", DEFAULT_UNTEN));
      unten.setName("Unten");
    }
    return unten;
  }

  public FormularInput getHintergrund() throws RemoteException
  {
    if (hintergrund == null)
    {
      hintergrund = new FormularInput(FormularArt.HINTERGRUND, settings
          .getString(settingPrefix + "hintergrund", DEFAULT_HINTERGRUND));
      hintergrund.setPleaseChoose("Kein Formular");
      hintergrund.setName("Formular Hintergrund");
    }
    return hintergrund;
  }

  public FormularInput getVordergrund() throws RemoteException
  {
    if (vordergrund == null)
    {
      vordergrund = new FormularInput(FormularArt.HINTERGRUND, settings
          .getString(settingPrefix + "vordergrund", DEFAULT_VORDERGRUND));
      vordergrund.setPleaseChoose("Kein Formular");
      vordergrund.setName("Formular Vordergrund");
    }
    return vordergrund;
  }

  public CheckboxInput getHeaderTransparent() throws RemoteException
  {
    if (headerTransparent == null)
    {
      headerTransparent = new CheckboxInput(settings.getBoolean(
          settingPrefix + "headerTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT)));
      headerTransparent.setName("Tabellen Header transparent");
    }
    return headerTransparent;
  }

  public CheckboxInput getZellenTransparent() throws RemoteException
  {
    if (zellenTransparent == null)
    {
      zellenTransparent = new CheckboxInput(settings.getBoolean(
          settingPrefix + "zellenTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT)));
      zellenTransparent.setName("Tabellen Zellen transparent");
    }
    return zellenTransparent;
  }

  public CheckboxInput getQuerformat()
  {
    if (querformat == null)
    {
      querformat = new CheckboxInput(
          settings.getBoolean(settingPrefix + "quer", DEFAULT_QUERFORMAT));
      querformat.setName("Querformat");
    }
    return querformat;
  }

  public FontInput getFontHeader() throws RemoteException
  {
    if (fontHeader == null)
    {
      fontHeader = new FontInput(settings
          .getString(settingPrefix + "font_header", DEFAULT_FONT_HEADER));
      fontHeader.setName("Schriftart");
    }
    return fontHeader;
  }

  public FontInput getFontNormal() throws RemoteException
  {
    if (fontNormal == null)
    {
      fontNormal = new FontInput(settings
          .getString(settingPrefix + "font_normal", DEFAULT_FONT_NORMAL));
      fontNormal.setName("Schriftart Standard");
    }
    return fontNormal;
  }

  public FontInput getFontFett() throws RemoteException
  {
    if (fontFett == null)
    {
      fontFett = new FontInput(
          settings.getString(settingPrefix + "font_fett", DEFAULT_FONT_FETT));
      fontFett.setName("Schriftart Fett");
    }
    return fontFett;
  }

  public FontInput getFontItalic() throws RemoteException
  {
    if (fontItalic == null)
    {
      fontItalic = new FontInput(settings
          .getString(settingPrefix + "font_italic", DEFAULT_FONT_ITALIC));
      fontItalic.setName("Schriftart Kursiv");
    }
    return fontItalic;
  }

  public IntegerInput getFontsize()
  {
    if (fontsize == null)
    {
      fontsize = new IntegerInput(
          settings.getInt(settingPrefix + "fontsize", DEFAULT_FONT_SIZE));
      fontsize.setName("Schriftgröße");
    }
    return fontsize;
  }

  public IntegerInput getFontsizeHeader()
  {
    if (fontsizeHeader == null)
    {
      fontsizeHeader = new IntegerInput(settings
          .getInt(settingPrefix + "fontsize_header", DEFAULT_FONT_SIZE_HEADER));
      fontsizeHeader.setName("Schriftgröße");
    }
    return fontsizeHeader;
  }

  public CheckboxInput getNegativRot()
  {
    if (negativRot == null)
    {
      negativRot = new CheckboxInput(settings
          .getBoolean(settingPrefix + "negativ_rot", DEFAULT_NEGATIV_ROT));
      negativRot.setName("Negative Werte in Rot");
    }
    return negativRot;
  }

  public ColorInput getHeaderColor()
  {
    if (colorHeader == null)
    {
      Color col = new Color(
          (int) settings.getInt(settingPrefix + "header_color_red",
              DEFAULT_HEADER_COLOR_RED),
          (int) settings.getInt(settingPrefix + "header_color_green",
              DEFAULT_HEADER_COLOR_GREEN),
          (int) settings.getInt(settingPrefix + "header_color_blue",
              DEFAULT_HEADER_COLOR_BLUE));
      colorHeader = new ColorInput(col, false);
      colorHeader.setName("Hintergrund Farbe");
    }
    return colorHeader;
  }

  public ColorInput getTableColor()
  {
    if (colorTable == null)
    {
      Color col = new Color(
          (int) settings.getInt(settingPrefix + "color_red", DEFAULT_COLOR_RED),
          (int) settings.getInt(settingPrefix + "color_green",
              DEFAULT_COLOR_GREEN),
          (int) settings.getInt(settingPrefix + "color_blue",
              DEFAULT_COLOR_BLUE));
      colorTable = new ColorInput(col, false);
      colorTable.setName("Hintergrund Farbe *");
    }
    return colorTable;
  }

  public ColorInput getTableColor2()
  {
    if (colorTable2 == null)
    {
      Color col = new Color(
          (int) settings.getInt(settingPrefix + "color_red2",
              DEFAULT_COLOR_RED2),
          (int) settings.getInt(settingPrefix + "color_green2",
              DEFAULT_COLOR_GREEN2),
          (int) settings.getInt(settingPrefix + "color_blue2",
              DEFAULT_COLOR_BLUE2));
      colorTable2 = new ColorInput(col, false);
      colorTable2.setName("Hintergrund Farbe *");
    }
    return colorTable2;
  }

  public void resetRaender()
  {
    links.setValue(DEFAULT_LINKS);
    rechts.setValue(DEFAULT_RECHTS);
    oben.setValue(DEFAULT_OBEN);
    unten.setValue(DEFAULT_UNTEN);
  }

  public void resetFormular() throws ApplicationException
  {
    try
    {
      hintergrund.setValue(DEFAULT_HINTERGRUND);
      vordergrund.setValue(DEFAULT_VORDERGRUND);
      headerTransparent.setValue((Boolean) Einstellungen
          .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT));
      zellenTransparent.setValue((Boolean) Einstellungen
          .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT));
      querformat.setValue(DEFAULT_QUERFORMAT);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Reset im Tabelle-Export-Dialog", e);
      throw new ApplicationException("Serverfehler");
    }
  }

  public void resetSchriftart()
  {
    fontHeader.setValue(DEFAULT_FONT_HEADER);
    fontNormal.setValue(DEFAULT_FONT_NORMAL);
    fontFett.setValue(DEFAULT_FONT_FETT);
    fontItalic.setValue(DEFAULT_FONT_ITALIC);
    fontsize.setValue(DEFAULT_FONT_SIZE);
    fontsizeHeader.setValue(DEFAULT_FONT_SIZE_HEADER);
    negativRot.setValue(DEFAULT_NEGATIV_ROT);
    Color col = new Color(DEFAULT_HEADER_COLOR_RED, DEFAULT_HEADER_COLOR_GREEN,
        DEFAULT_HEADER_COLOR_BLUE);
    colorHeader.setValue(col);
    col = new Color(DEFAULT_COLOR_RED, DEFAULT_COLOR_GREEN, DEFAULT_COLOR_BLUE);
    colorTable.setValue(col);
    if (colorTable2 != null)
    {
      col = new Color(DEFAULT_COLOR_RED2, DEFAULT_COLOR_GREEN2,
          DEFAULT_COLOR_BLUE2);
      colorTable2.setValue(col);
    }
  }

  public void setChecked()
  {
    if (colList == null || spaltenList == null)
    {
      return;
    }
    for (ExportSpalte sp : colList)
    {
      spaltenList.setChecked(sp,
          settings.getBoolean(
              settingPrefix + "anzeigen." + sp.getColumn().getName(),
              part.getColums().contains(sp.getColumn())));
    }
  }

  @SuppressWarnings("unchecked")
  public void setWidth() throws ApplicationException
  {
    if (spaltenList == null)
    {
      return;
    }
    try
    {
      for (ExportSpalte e : (List<ExportSpalte>) spaltenList.getItems(false))
      {
        Item c = dialog.getColumn(e.getColumn().getName());
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

  public void resetSpalten() throws ApplicationException
  {
    if (spaltenList == null)
    {
      return;
    }
    try
    {
      spaltenList.removeAll();
      colList = new ArrayList<>();
      for (Column i : part.getAllColums())
      {
        Item c = dialog.getColumn(i.getName());
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
  public void saveSettings(String prefix) throws RemoteException
  {
    if (spaltenList != null)
    {
      List<ExportSpalte> itemsChecked = spaltenList.getItems();
      List<String> spaltenNamen = new ArrayList<>();
      for (ExportSpalte sp : (List<ExportSpalte>) spaltenList.getItems(false))
      {
        settings.setAttribute(prefix + "anzeigen." + sp.getColumn().getName(),
            itemsChecked.contains(sp));
        if (art.equals(ExportArt.PDF))
        {
          settings.setAttribute(prefix + "breite." + sp.getColumn().getName(),
              sp.getBreite());
        }
        spaltenNamen.add(sp.getColumn().getName());
      }
      settings.setAttribute(prefix + "order", String.join(",", spaltenNamen));
    }

    if (art.equals(ExportArt.PDF))
    {
      settings.setAttribute(prefix + "links", (Integer) links.getValue());
      settings.setAttribute(prefix + "rechts", (Integer) rechts.getValue());
      settings.setAttribute(prefix + "oben", (Integer) oben.getValue());
      settings.setAttribute(prefix + "unten", (Integer) unten.getValue());

      settings.setAttribute(prefix + "hintergrund",
          hintergrund.getValue() == null ? ""
              : ((Formular) hintergrund.getValue()).getID());
      settings.setAttribute(prefix + "vordergrund",
          vordergrund.getValue() == null ? ""
              : ((Formular) vordergrund.getValue()).getID());

      settings.setAttribute(prefix + "headerTransparent",
          (Boolean) headerTransparent.getValue());
      settings.setAttribute(prefix + "zellenTransparent",
          (Boolean) zellenTransparent.getValue());

      settings.setAttribute(prefix + "quer", (Boolean) querformat.getValue());

      settings.setAttribute(prefix + "font_header",
          (String) fontHeader.getValue());
      settings.setAttribute(prefix + "font_normal",
          (String) fontNormal.getValue());
      settings.setAttribute(prefix + "font_fett", (String) fontFett.getValue());
      settings.setAttribute(prefix + "font_italic",
          (String) fontItalic.getValue());
      settings.setAttribute(prefix + "fontsize_header",
          (Integer) fontsizeHeader.getValue());
      settings.setAttribute(prefix + "fontsize", (Integer) fontsize.getValue());
      settings.setAttribute(prefix + "negativ_rot",
          (Boolean) negativRot.getValue());
      Color col = (Color) colorHeader.getValue();
      settings.setAttribute(prefix + "header_color_red",
          (Integer) col.getRed());
      settings.setAttribute(prefix + "header_color_green",
          (Integer) col.getGreen());
      settings.setAttribute(prefix + "header_color_blue",
          (Integer) col.getBlue());
      col = (Color) colorTable.getValue();
      settings.setAttribute(prefix + "color_red", (Integer) col.getRed());
      settings.setAttribute(prefix + "color_green", (Integer) col.getGreen());
      settings.setAttribute(prefix + "color_blue", (Integer) col.getBlue());
      if (colorTable2 != null)
      {
        col = (Color) colorTable2.getValue();
        settings.setAttribute(prefix + "color_red2", (Integer) col.getRed());
        settings.setAttribute(prefix + "color_green2",
            (Integer) col.getGreen());
        settings.setAttribute(prefix + "color_blue2", (Integer) col.getBlue());
      }
      else
      {
        settings.setAttribute(prefix + "color_red2", (String) null);
        settings.setAttribute(prefix + "color_green2", (String) null);
        settings.setAttribute(prefix + "color_blue2", (String) null);
      }
    }
  }

  // Schreibt die Settings in den Dialog
  public void loadSettings(String prefix) throws RemoteException
  {
    // Spalten
    if (spaltenList != null)
    {
      spaltenList.removeAll();
      colList = new ArrayList<>();
      for (Column col : part.getAllColums())
      {
        int breite = settings.getInt(prefix + "breite." + col.getName(), 0);
        colList.add(new ExportSpalte(col, breite));
      }
      String[] spaltenNamen = settings.getString(prefix + "order", "")
          .split(",");
      colList.sort(Comparator.comparingInt(obj -> Arrays.asList(spaltenNamen)
          .indexOf(obj.getColumn().getName())));
      for (ExportSpalte spalte : colList)
      {
        ExportSpalte item = new ExportSpalte(spalte.getColumn(),
            spalte.getBreite());
        spaltenList.addItem(item);
        spaltenList.setChecked(item, settings.getBoolean(
            prefix + "anzeigen." + spalte.getColumn().getName(), true));
      }

    }

    if (art.equals(ExportArt.PDF))
    {
      // Ränder
      links.setValue(settings.getInt(prefix + "links", DEFAULT_LINKS));
      rechts.setValue(settings.getInt(prefix + "rechts", DEFAULT_RECHTS));
      oben.setValue(settings.getInt(prefix + "oben", DEFAULT_OBEN));
      unten.setValue(settings.getInt(prefix + "unten", DEFAULT_UNTEN));

      // Formular

      hintergrund.setPreselected(FormularInput.initdefault(
          settings.getString(prefix + "hintergrund", DEFAULT_HINTERGRUND)));
      vordergrund.setPreselected(FormularInput.initdefault(
          settings.getString(prefix + "vordergrund", DEFAULT_VORDERGRUND)));
      headerTransparent.setValue(settings
          .getBoolean(prefix + "headerTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT)));
      zellenTransparent.setValue(settings
          .getBoolean(prefix + "zellenTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT)));
      querformat
          .setValue(settings.getBoolean(prefix + "quer", DEFAULT_QUERFORMAT));

      // Schriftart
      fontHeader.setValue(
          settings.getString(prefix + "font_header", DEFAULT_FONT_HEADER));
      fontNormal.setValue(
          settings.getString(prefix + "font_normal", DEFAULT_FONT_NORMAL));
      fontFett.setValue(
          settings.getString(prefix + "font_fett", DEFAULT_FONT_FETT));
      fontItalic.setValue(
          settings.getString(prefix + "font_italic", DEFAULT_FONT_ITALIC));
      fontsize
          .setValue(settings.getInt(prefix + "fontsize", DEFAULT_FONT_SIZE));
      fontsizeHeader.setValue(settings.getInt(prefix + "fontsize_header",
          DEFAULT_FONT_SIZE_HEADER));
      negativRot.setValue(
          settings.getBoolean(prefix + "negativ_rot", DEFAULT_NEGATIV_ROT));
      Color col = new Color(
          (int) settings.getInt(prefix + "header_color_red",
              DEFAULT_HEADER_COLOR_RED),
          (int) settings.getInt(prefix + "header_color_green",
              DEFAULT_HEADER_COLOR_GREEN),
          (int) settings.getInt(prefix + "header_color_blue",
              DEFAULT_HEADER_COLOR_BLUE));
      colorHeader.setValue(col);
      col = new Color(
          (int) settings.getInt(prefix + "color_red", DEFAULT_COLOR_RED),
          (int) settings.getInt(prefix + "color_green", DEFAULT_COLOR_GREEN),
          (int) settings.getInt(prefix + "color_blue", DEFAULT_COLOR_BLUE));
      colorTable.setValue(col);
      if (colorTable2 != null)
      {
        col = new Color(
            (int) settings.getInt(prefix + "color_red2", DEFAULT_COLOR_RED2),
            (int) settings.getInt(prefix + "color_green2",
                DEFAULT_COLOR_GREEN2),
            (int) settings.getInt(prefix + "color_blue2", DEFAULT_COLOR_BLUE2));
        colorTable2.setValue(col);
      }
    }
  }

  public Font getFont(String text, FontData[] data)
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

  public Font getFontHeader(BaseColor color)
  {
    return FontFactory.getFont((String) fontHeader.getValue(),
        BaseFont.IDENTITY_H, (Integer) fontsizeHeader.getValue(),
        Font.UNDEFINED, color);
  }

  public BaseColor getHintergrundHeader()
  {
    Color col = (Color) colorHeader.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  public BaseColor getHintergrundTabelle()
  {
    Color col = (Color) colorTable.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  public BaseColor getHintergrundTabelle2()
  {
    Color col = (Color) colorTable2.getValue();
    return new BaseColor(col.getRed(), col.getGreen(), col.getBlue());
  }

  public Font getFontNormal(BaseColor color)
  {
    return FontFactory.getFont((String) fontNormal.getValue(),
        BaseFont.IDENTITY_H, (Integer) fontsize.getValue(), Font.UNDEFINED,
        color);
  }

  public Font getFontFett(BaseColor color)
  {
    return FontFactory.getFont((String) fontFett.getValue(),
        BaseFont.IDENTITY_H, (Integer) fontsize.getValue(), Font.UNDEFINED,
        color);
  }

  public Font getFontKursiv(BaseColor color)
  {
    return FontFactory.getFont((String) fontItalic.getValue(),
        BaseFont.IDENTITY_H, (Integer) fontsize.getValue(), Font.UNDEFINED,
        color);
  }

  public void storeExportLayoutParam()
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
    if (colorTable2 != null)
    {
      params.setColorTable2(getHintergrundTabelle2());
    }
    params.setNegativRot((Boolean) negativRot.getValue());
  }

  public ExportLayoutParam getExportLayoutParam()
  {
    return params;
  }

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
}
