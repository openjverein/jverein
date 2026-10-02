/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
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
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.swt.graphics.Color;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportSpalte;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;

public class TabelleExportParam
{
  ExportArt art = ExportArt.CSV;

  public static final Integer UNDEFINED = 0;

  public static final Integer CHECKED = 1;

  public static final Integer UNCHECKED = 2;

  public static final String ANZEIGEN = "anzeigen.";

  public static final String ORDER = "order";

  public static final String BREITE = "breite.";

  public static final String LINKS = "links";

  public static final String RECHTS = "rechts";

  public static final String OBEN = "oben";

  public static final String UNTEN = "unten";

  public static final String HINTERGRUND = "hintergrund";

  public static final String VORDERGRUND = "vordergrund";

  public static final String HEADER_TRANSPARENT = "header_transparent";

  public static final String ZELLEN_TRANSPARENT = "zellen_transparent";

  public static final String QUERFORMAT = "quer";

  public static final String NEGATIV_ROT = "negativ_rot";

  public static final String FONT_HEADER = "font_header";

  public static final String FONT_NORMAL = "font_normal";

  public static final String FONT_FETT = "font_fett";

  public static final String FONT_ITALIC = "font_italic";

  public static final String FONT_SIZE = "fontsize";

  public static final String FONT_SIZE_HEADER = "fontsize_header";

  public static final String HEADER_COLOR_RED = "header_color_red";

  public static final String HEADER_COLOR_BLUE = "header_color_blue";

  public static final String HEADER_COLOR_GREEN = "header_color_green";

  public static final String COLOR_RED = "color_red";

  public static final String COLOR_BLUE = "color_blue";

  public static final String COLOR_GREEN = "color_green";

  public static final String COLOR_RED2 = "color_red2";

  public static final String COLOR_BLUE2 = "color_green2";

  public static final String COLOR_GREEN2 = "color_blue2";

  // Defaults

  public static final int DEFAULT_LINKS = 20;

  public static final int DEFAULT_RECHTS = 20;

  public static final int DEFAULT_OBEN = 20;

  public static final int DEFAULT_UNTEN = 20;

  public static final String DEFAULT_HINTERGRUND = null;

  public static final String DEFAULT_VORDERGRUND = null;

  public static final boolean DEFAULT_QUERFORMAT = false;

  public static final boolean DEFAULT_NEGATIV_ROT = true;

  public static final String DEFAULT_FONT_HEADER = "FreeSans";

  public static final String DEFAULT_FONT_NORMAL = "FreeSans";

  public static final String DEFAULT_FONT_FETT = "FreeSans-Bold";

  public static final String DEFAULT_FONT_ITALIC = "FreeSans-Oblique";

  public static final int DEFAULT_FONT_SIZE = 8;

  public static final int DEFAULT_FONT_SIZE_HEADER = 8;

  public static final int DEFAULT_HEADER_COLOR_RED = 192;

  public static final int DEFAULT_HEADER_COLOR_BLUE = 192;

  public static final int DEFAULT_HEADER_COLOR_GREEN = 192;

  public static final int DEFAULT_COLOR_RED = 192;

  public static final int DEFAULT_COLOR_BLUE = 192;

  public static final int DEFAULT_COLOR_GREEN = 192;

  public static final int DEFAULT_COLOR_RED2 = 230;

  public static final int DEFAULT_COLOR_BLUE2 = 230;

  public static final int DEFAULT_COLOR_GREEN2 = 230;

  // Tabelle Attribute

  private String order = "";

  private Map<String, Integer> anzeigenMap;

  private Map<String, Integer> breiteMap;

  // PDF Attribute

  private int links = DEFAULT_LINKS;

  private int rechts = DEFAULT_RECHTS;

  private int oben = DEFAULT_OBEN;

  private int unten = DEFAULT_UNTEN;

  private String hintergrund = DEFAULT_HINTERGRUND;

  private String vordergrund = DEFAULT_VORDERGRUND;

  private boolean header_transparent = false;

  private boolean zellen_transparent = false;

  private boolean querformat = DEFAULT_QUERFORMAT;

  private boolean negativ_rot = DEFAULT_NEGATIV_ROT;

  private String font_header = DEFAULT_FONT_HEADER;

  private String font_normal = DEFAULT_FONT_NORMAL;

  private String font_fett = DEFAULT_FONT_FETT;

  private String font_italic = DEFAULT_FONT_ITALIC;

  private int font_size = DEFAULT_FONT_SIZE;

  private int font_size_header = DEFAULT_FONT_SIZE_HEADER;

  private int header_color_red = DEFAULT_HEADER_COLOR_RED;

  private int header_color_blue = DEFAULT_HEADER_COLOR_BLUE;

  private int header_color_green = DEFAULT_HEADER_COLOR_GREEN;

  private int color_red = DEFAULT_COLOR_RED;

  private int color_blue = DEFAULT_COLOR_BLUE;

  private int color_green = DEFAULT_COLOR_GREEN;

  private int color_red2 = DEFAULT_COLOR_RED2;

  private int color_blue2 = DEFAULT_COLOR_BLUE2;

  private int color_green2 = DEFAULT_COLOR_GREEN2;

  public TabelleExportParam(ExportArt art)
  {
    this.art = art;
  }

  public void importFromSettings(AbstractPartExportDialog dialog,
      Settings settings, String settingPrefix, boolean mitSpalten)
      throws RemoteException
  {
    anzeigenMap = new HashMap<>();
    breiteMap = new HashMap<>();
    if (mitSpalten)
    {
      order = settings.getString(settingPrefix + ORDER, "");
      for (Column col : dialog.part.getAllColums())
      {
        String name = col.getName();
        anzeigenMap.put(name,
            settings.getInt(settingPrefix + ANZEIGEN + name, UNDEFINED));
        if (art.equals(ExportArt.PDF))
        {
          breiteMap.put(name,
              settings.getInt(settingPrefix + BREITE + name, 0));
        }
      }
    }

    if (art.equals(ExportArt.PDF))
    {
      // Ränder
      links = settings.getInt(settingPrefix + LINKS, DEFAULT_LINKS);
      rechts = settings.getInt(settingPrefix + RECHTS, DEFAULT_RECHTS);
      oben = settings.getInt(settingPrefix + OBEN, DEFAULT_OBEN);
      unten = settings.getInt(settingPrefix + UNTEN, DEFAULT_UNTEN);

      // Formular
      hintergrund = settings.getString(settingPrefix + HINTERGRUND,
          DEFAULT_HINTERGRUND);
      vordergrund = settings.getString(settingPrefix + VORDERGRUND,
          DEFAULT_VORDERGRUND);
      header_transparent = settings.getBoolean(
          settingPrefix + HEADER_TRANSPARENT, (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT));
      zellen_transparent = settings.getBoolean(
          settingPrefix + ZELLEN_TRANSPARENT, (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT));
      querformat = settings.getBoolean(settingPrefix + QUERFORMAT,
          DEFAULT_QUERFORMAT);

      // Schriftart
      font_header = settings.getString(settingPrefix + FONT_HEADER,
          DEFAULT_FONT_HEADER);
      font_normal = settings.getString(settingPrefix + FONT_NORMAL,
          DEFAULT_FONT_NORMAL);
      font_fett = settings.getString(settingPrefix + FONT_FETT,
          DEFAULT_FONT_FETT);
      font_italic = settings.getString(settingPrefix + FONT_ITALIC,
          DEFAULT_FONT_ITALIC);
      font_size = settings.getInt(settingPrefix + FONT_SIZE, DEFAULT_FONT_SIZE);
      font_size_header = settings.getInt(settingPrefix + FONT_SIZE_HEADER,
          DEFAULT_FONT_SIZE_HEADER);
      negativ_rot = settings.getBoolean(settingPrefix + NEGATIV_ROT,
          DEFAULT_NEGATIV_ROT);

      header_color_red = settings.getInt(settingPrefix + HEADER_COLOR_RED,
          DEFAULT_HEADER_COLOR_RED);
      header_color_green = settings.getInt(settingPrefix + HEADER_COLOR_GREEN,
          DEFAULT_HEADER_COLOR_GREEN);
      header_color_blue = settings.getInt(settingPrefix + HEADER_COLOR_BLUE,
          DEFAULT_HEADER_COLOR_BLUE);

      color_red = settings.getInt(settingPrefix + COLOR_RED, DEFAULT_COLOR_RED);
      color_green = settings.getInt(settingPrefix + COLOR_GREEN,
          DEFAULT_COLOR_GREEN);
      color_blue = settings.getInt(settingPrefix + COLOR_BLUE,
          DEFAULT_COLOR_BLUE);
      if (dialog.supportTable2)
      {
        color_red2 = settings.getInt(settingPrefix + COLOR_RED2,
            DEFAULT_COLOR_RED2);
        color_green2 = settings.getInt(settingPrefix + COLOR_GREEN2,
            DEFAULT_COLOR_GREEN2);
        color_blue2 = settings.getInt(settingPrefix + COLOR_BLUE2,
            DEFAULT_COLOR_BLUE2);
      }
    }
  }

  // Parameter aus den Settings lesen
  @SuppressWarnings("unchecked")
  public void importFromGui(AbstractPartExportDialog dialog, Settings settings,
      String prefix) throws RemoteException
  {
    anzeigenMap = new HashMap<>();
    breiteMap = new HashMap<>();
    if (dialog.spaltenList != null)
    {
      List<ExportSpalte> itemsChecked = dialog.spaltenList.getItems();
      List<String> spaltenNamen = new ArrayList<>();
      for (ExportSpalte sp : (List<ExportSpalte>) dialog.spaltenList
          .getItems(false))
      {
        String name = sp.getColumn().getName();
        anzeigenMap.put(name, itemsChecked.contains(sp) ? CHECKED : UNCHECKED);
        if (art.equals(ExportArt.PDF))
        {
          breiteMap.put(name, sp.getBreite());
        }
        spaltenNamen.add(name);
      }
      order = String.join(",", spaltenNamen);
    }

    if (art.equals(ExportArt.PDF))
    {
      links = (int) dialog.links.getValue();
      rechts = (int) dialog.rechts.getValue();
      oben = (int) dialog.oben.getValue();
      unten = (int) dialog.unten.getValue();

      hintergrund = dialog.hintergrund.getValue() == null ? ""
          : ((Formular) dialog.hintergrund.getValue()).getID();
      vordergrund = dialog.vordergrund.getValue() == null ? ""
          : ((Formular) dialog.vordergrund.getValue()).getID();

      header_transparent = (boolean) dialog.headerTransparent.getValue();
      zellen_transparent = (boolean) dialog.zellenTransparent.getValue();

      querformat = (boolean) dialog.querformat.getValue();

      font_header = (String) dialog.fontHeader.getValue();
      font_normal = (String) dialog.fontNormal.getValue();
      font_fett = (String) dialog.fontFett.getValue();
      font_italic = (String) dialog.fontItalic.getValue();
      font_size_header = (int) dialog.fontsizeHeader.getValue();
      font_size = (int) dialog.fontsize.getValue();
      negativ_rot = (boolean) dialog.negativRot.getValue();
      Color col = (Color) dialog.colorHeader.getValue();
      header_color_red = (int) col.getRed();
      header_color_green = (int) col.getGreen();
      header_color_blue = (int) col.getBlue();
      col = (Color) dialog.colorTable.getValue();
      color_red = (int) col.getRed();
      color_green = (int) col.getGreen();
      color_blue = (int) col.getBlue();
      if (dialog.supportTable2)
      {
        col = (Color) dialog.colorTable2.getValue();
        color_red2 = (int) col.getRed();
        color_green2 = (int) col.getGreen();
        color_blue2 = (int) col.getBlue();
      }
    }
  }

  // Parameter aus dem Profile XML einlesen
  public void importFromXML(String data)
      throws InvalidPropertiesFormatException, IOException
  {
    anzeigenMap = new HashMap<>();
    breiteMap = new HashMap<>();
    ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
    Properties p = new Properties();
    p.loadFromXML(bis);
    for (Object o : p.keySet())
    {
      String key = (String) o;
      setAttribute(key, p.getProperty(key));
    }
  }

  private void setAttribute(String key, Object value)
  {
    if (key.startsWith(ANZEIGEN))
    {
      anzeigenMap.put(key.substring(ANZEIGEN.length()),
          Integer.valueOf((String) value));
    }
    else if (key.startsWith(BREITE))
    {
      breiteMap.put(key.substring(BREITE.length()),
          Integer.valueOf((String) value));
    }
    else if (key.equals(ORDER))
    {
      order = (String) value;
    }
    else if (key.equals(LINKS))
    {
      links = Integer.valueOf((String) value);
    }
    else if (key.equals(RECHTS))
    {
      rechts = Integer.valueOf((String) value);
    }
    else if (key.equals(OBEN))
    {
      oben = Integer.valueOf((String) value);
    }
    else if (key.equals(UNTEN))
    {
      unten = Integer.valueOf((String) value);
    }
    else if (key.equals(HINTERGRUND))
    {
      hintergrund = (String) value;
    }
    else if (key.equals(VORDERGRUND))
    {
      vordergrund = (String) value;
    }
    else if (key.equals(HEADER_TRANSPARENT))
    {
      header_transparent = Boolean.valueOf((String) value);
    }
    else if (key.equals(ZELLEN_TRANSPARENT))
    {
      zellen_transparent = Boolean.valueOf((String) value);
    }
    else if (key.equals(QUERFORMAT))
    {
      querformat = Boolean.valueOf((String) value);
    }
    else if (key.equals(NEGATIV_ROT))
    {
      negativ_rot = Boolean.valueOf((String) value);
    }
    else if (key.equals(FONT_HEADER))
    {
      font_header = (String) value;
    }
    else if (key.equals(FONT_NORMAL))
    {
      font_normal = (String) value;
    }
    else if (key.equals(FONT_FETT))
    {
      font_fett = (String) value;
    }
    else if (key.equals(FONT_ITALIC))
    {
      font_italic = (String) value;
    }
    else if (key.equals(FONT_SIZE))
    {
      font_size = Integer.valueOf((String) value);
    }
    else if (key.equals(FONT_SIZE_HEADER))
    {
      font_size_header = Integer.valueOf((String) value);
    }
    else if (key.equals(HEADER_COLOR_RED))
    {
      header_color_red = Integer.valueOf((String) value);
    }
    else if (key.equals(HEADER_COLOR_BLUE))
    {
      header_color_blue = Integer.valueOf((String) value);
    }
    else if (key.equals(HEADER_COLOR_GREEN))
    {
      header_color_green = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_RED))
    {
      color_red = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_BLUE))
    {
      color_blue = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_GREEN))
    {
      color_green = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_RED2))
    {
      color_red2 = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_BLUE2))
    {
      color_blue2 = Integer.valueOf((String) value);
    }
    else if (key.equals(COLOR_GREEN2))
    {
      color_green2 = Integer.valueOf((String) value);
    }
  }

  // Parameter in den Settings speichern
  public void exportToSettings(AbstractPartExportDialog dialog,
      Settings settings, String prefix, boolean mitSpalten)
      throws RemoteException
  {
    if (mitSpalten)
    {
      for (Entry<String, Integer> entry : anzeigenMap.entrySet())
      {
        settings.setAttribute(prefix + ANZEIGEN + entry.getKey(),
            entry.getValue());
      }
      if (art.equals(ExportArt.PDF))
      {
        for (Entry<String, Integer> entry : breiteMap.entrySet())
        {
          settings.setAttribute(prefix + BREITE + entry.getKey(),
              entry.getValue());
        }
      }
      settings.setAttribute(prefix + ORDER, order);
    }

    if (art.equals(ExportArt.PDF))
    {
      settings.setAttribute(prefix + LINKS, links);
      settings.setAttribute(prefix + RECHTS, rechts);
      settings.setAttribute(prefix + OBEN, oben);
      settings.setAttribute(prefix + UNTEN, unten);

      settings.setAttribute(prefix + HINTERGRUND, hintergrund);
      settings.setAttribute(prefix + VORDERGRUND, vordergrund);

      settings.setAttribute(prefix + HEADER_TRANSPARENT, header_transparent);
      settings.setAttribute(prefix + ZELLEN_TRANSPARENT, zellen_transparent);

      settings.setAttribute(prefix + QUERFORMAT, querformat);

      settings.setAttribute(prefix + FONT_HEADER, font_header);
      settings.setAttribute(prefix + FONT_NORMAL, font_normal);
      settings.setAttribute(prefix + FONT_FETT, font_fett);
      settings.setAttribute(prefix + FONT_ITALIC, font_italic);
      settings.setAttribute(prefix + FONT_SIZE_HEADER, font_size_header);
      settings.setAttribute(prefix + FONT_SIZE, font_size);
      settings.setAttribute(prefix + NEGATIV_ROT, negativ_rot);
      settings.setAttribute(prefix + HEADER_COLOR_RED, header_color_red);
      settings.setAttribute(prefix + HEADER_COLOR_GREEN, header_color_green);
      settings.setAttribute(prefix + HEADER_COLOR_BLUE, header_color_blue);
      settings.setAttribute(prefix + COLOR_RED, color_red);
      settings.setAttribute(prefix + COLOR_GREEN, color_green);
      settings.setAttribute(prefix + COLOR_BLUE, color_blue);
      if (dialog.supportTable2)
      {
        settings.setAttribute(prefix + COLOR_RED2, color_red2);
        settings.setAttribute(prefix + COLOR_GREEN2, color_green2);
        settings.setAttribute(prefix + COLOR_BLUE2, color_blue2);
      }
      else
      {
        settings.setAttribute(prefix + COLOR_RED2, (String) null);
        settings.setAttribute(prefix + COLOR_GREEN2, (String) null);
        settings.setAttribute(prefix + COLOR_BLUE2, (String) null);
      }
    }
  }

  // Settings in das GUI schreiben
  public void exportToGui(AbstractPartExportDialog dialog, Settings settings,
      String prefix) throws RemoteException
  {

    // Spalten
    if (dialog.spaltenList != null)
    {
      dialog.spaltenList.removeAll();
      dialog.colList = new ArrayList<>();

      for (Column col : dialog.part.getAllColums())
      {
        int breite = 0;
        if (art.equals(ExportArt.PDF))
        {
          breite = breiteMap.get(col.getName());
        }
        dialog.colList.add(dialog.new ExportSpalte(col, breite));
      }

      String[] spaltenNamen = order.split(",");
      dialog.colList.sort(Comparator.comparingInt(obj -> Arrays
          .asList(spaltenNamen).indexOf(obj.getColumn().getName())));
      for (ExportSpalte spalte : dialog.colList)
      {
        ExportSpalte item = dialog.new ExportSpalte(spalte.getColumn(),
            spalte.getBreite());
        dialog.spaltenList.addItem(item);
        boolean isChecked = false;
        if (anzeigenMap.get(spalte.getColumn().getName()) == CHECKED)
        {
          isChecked = true;
        }
        dialog.spaltenList.setChecked(item, isChecked);
      }
    }

    if (art.equals(ExportArt.PDF))
    {
      // Ränder
      dialog.links.setValue(links);
      dialog.rechts.setValue(rechts);
      dialog.oben.setValue(oben);
      dialog.unten.setValue(unten);

      // Formular
      dialog.hintergrund.setPreselected(FormularInput.initdefault(hintergrund));
      dialog.vordergrund.setPreselected(FormularInput.initdefault(vordergrund));
      dialog.headerTransparent.setValue(header_transparent);
      dialog.zellenTransparent.setValue(zellen_transparent);
      dialog.querformat.setValue(querformat);

      // Schriftart
      dialog.fontHeader.setValue(font_header);
      dialog.fontNormal.setValue(font_normal);
      dialog.fontFett.setValue(font_fett);
      dialog.fontItalic.setValue(font_italic);
      dialog.fontsize.setValue(font_size);
      dialog.fontsizeHeader.setValue(font_size_header);
      dialog.negativRot.setValue(negativ_rot);
      Color col = new Color(header_color_red, header_color_green,
          header_color_blue);
      dialog.colorHeader.setValue(col);
      col = new Color(color_red, color_green, color_blue);
      dialog.colorTable.setValue(col);
      if (dialog.supportTable2)
      {
        col = new Color(color_red2, color_green2, color_blue2);
        dialog.colorTable2.setValue(col);
      }
    }
  }

  // Parameter als Profile XML in den Settings speichern
  public void exportToXML(AbstractPartExportDialog dialog,
      Settings settings, String prefix, boolean mitSpalten) throws IOException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Properties prop = new Properties();

    if (mitSpalten)
    {
      for (Entry<String, Integer> entry : anzeigenMap.entrySet())
      {
        prop.put(ANZEIGEN + entry.getKey(), Integer.toString(entry.getValue()));
      }
      if (art.equals(ExportArt.PDF))
      {
        for (Entry<String, Integer> entry : breiteMap.entrySet())
        {
          prop.put(BREITE + entry.getKey(), Integer.toString(entry.getValue()));
        }
      }
      prop.put(ORDER, order);
    }

    if (art.equals(ExportArt.PDF))
    {
      prop.put(LINKS, Integer.toString(links));
      prop.put(RECHTS, Integer.toString(rechts));
      prop.put(OBEN, Integer.toString(oben));
      prop.put(UNTEN, Integer.toString(unten));

      prop.put(HINTERGRUND, hintergrund);
      prop.put(VORDERGRUND, vordergrund);

      prop.put(HEADER_TRANSPARENT, Boolean.toString(header_transparent));
      prop.put(ZELLEN_TRANSPARENT, Boolean.toString(zellen_transparent));

      prop.put(QUERFORMAT, Boolean.toString(querformat));

      prop.put(FONT_HEADER, font_header);
      prop.put(FONT_NORMAL, font_normal);
      prop.put(FONT_FETT, font_fett);
      prop.put(FONT_ITALIC, font_italic);
      prop.put(FONT_SIZE_HEADER, Integer.toString(font_size_header));
      prop.put(FONT_SIZE, Integer.toString(font_size));
      prop.put(NEGATIV_ROT, Boolean.toString(negativ_rot));
      prop.put(HEADER_COLOR_RED, Integer.toString(header_color_red));
      prop.put(HEADER_COLOR_GREEN, Integer.toString(header_color_green));
      prop.put(HEADER_COLOR_BLUE, Integer.toString(header_color_blue));
      prop.put(COLOR_RED, Integer.toString(color_red));
      prop.put(COLOR_GREEN, Integer.toString(color_green));
      prop.put(COLOR_BLUE, Integer.toString(color_blue));
      if (dialog.supportTable2)
      {
        prop.put(COLOR_RED2, Integer.toString(color_red2));
        prop.put(COLOR_GREEN2, Integer.toString(color_green2));
        prop.put(COLOR_BLUE2, Integer.toString(color_blue2));
      }
    }
    prop.storeToXML(bos, "sicherung", "UTF8");
    settings.setAttribute(prefix, bos.toString());
  }

  // Getter und Setter

  public int getLinks()
  {
    return links;
  }

  public void setLinks(int links)
  {
    this.links = links;
  }

  public int getRechts()
  {
    return rechts;
  }

  public void setRechts(int rechts)
  {
    this.rechts = rechts;
  }

  public int getOben()
  {
    return oben;
  }

  public void setOben(int oben)
  {
    this.oben = oben;
  }

  public int getUnten()
  {
    return unten;
  }

  public void setUnten(int unten)
  {
    this.unten = unten;
  }

  public String getHintergrund()
  {
    return hintergrund;
  }

  public void setHintergrund(String hintergrund)
  {
    this.hintergrund = hintergrund;
  }

  public String getVordergrund()
  {
    return vordergrund;
  }

  public void setVordergrund(String vordergrund)
  {
    this.vordergrund = vordergrund;
  }

  public boolean isQuerformat()
  {
    return querformat;
  }

  public void setQuerformat(boolean querformat)
  {
    this.querformat = querformat;
  }

  public boolean isNegativ_rot()
  {
    return negativ_rot;
  }

  public void setNegativ_rot(boolean negativ_rot)
  {
    this.negativ_rot = negativ_rot;
  }

  public String getFont_header()
  {
    return font_header;
  }

  public void setFont_header(String font_header)
  {
    this.font_header = font_header;
  }

  public String getFont_normal()
  {
    return font_normal;
  }

  public void setFont_normal(String font_normal)
  {
    this.font_normal = font_normal;
  }

  public String getFont_fett()
  {
    return font_fett;
  }

  public void setFont_fett(String font_fett)
  {
    this.font_fett = font_fett;
  }

  public String getFont_italic()
  {
    return font_italic;
  }

  public void setFont_italic(String font_italic)
  {
    this.font_italic = font_italic;
  }

  public int getFont_size()
  {
    return font_size;
  }

  public void setFont_size(int font_size)
  {
    this.font_size = font_size;
  }

  public int getFont_size_header()
  {
    return font_size_header;
  }

  public void setFont_size_header(int font_size_header)
  {
    this.font_size_header = font_size_header;
  }

  public int getHeader_color_red()
  {
    return header_color_red;
  }

  public void setHeader_color_red(int header_color_red)
  {
    this.header_color_red = header_color_red;
  }

  public int getHeader_color_blue()
  {
    return header_color_blue;
  }

  public void setHeader_color_blue(int header_color_blue)
  {
    this.header_color_blue = header_color_blue;
  }

  public int getHeader_color_green()
  {
    return header_color_green;
  }

  public void setHeader_color_green(int header_color_green)
  {
    this.header_color_green = header_color_green;
  }

  public int getColor_red()
  {
    return color_red;
  }

  public void setColor_red(int color_red)
  {
    this.color_red = color_red;
  }

  public int getColor_blue()
  {
    return color_blue;
  }

  public void setColor_blue(int color_blue)
  {
    this.color_blue = color_blue;
  }

  public int getColor_green()
  {
    return color_green;
  }

  public void setColor_green(int color_green)
  {
    this.color_green = color_green;
  }

  public int getColor_red2()
  {
    return color_red2;
  }

  public void setColor_red2(int color_red2)
  {
    this.color_red2 = color_red2;
  }

  public int getColor_blue2()
  {
    return color_blue2;
  }

  public void setColor_blue2(int color_blue2)
  {
    this.color_blue2 = color_blue2;
  }

  public int getColor_green2()
  {
    return color_green2;
  }

  public void setColor_green2(int color_green2)
  {
    this.color_green2 = color_green2;
  }

  public boolean isHeader_transparent()
  {
    return header_transparent;
  }

  public void setHeader_transparent(boolean header_transparent)
  {
    this.header_transparent = header_transparent;
  }

  public boolean isZellen_transparent()
  {
    return zellen_transparent;
  }

  public void setZellen_transparent(boolean zellen_transparent)
  {
    this.zellen_transparent = zellen_transparent;
  }

  public String getOrder()
  {
    return order;
  }

  public void setOrder(String order)
  {
    this.order = order;
  }

  public Map<String, Integer> getAnzeigenMap()
  {
    return anzeigenMap;
  }

  public Map<String, Integer> getBreiteMap()
  {
    return breiteMap;
  }
}
