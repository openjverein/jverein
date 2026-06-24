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
package de.jost_net.JVerein.io;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;

import de.jost_net.JVerein.rmi.Formular;

public class SaldoExportParam
{

  private Integer links;

  private Integer rechts;

  private Integer oben;

  private Integer unten;

  private Boolean querformat;

  private Formular vordergrund;

  private Formular hintergrund;

  private Boolean headerTransparent;

  private Boolean zellenTransparent;

  private Font fontHeader;

  private Font fontNormal;

  private Font fontFett;

  private Font fontItalic;

  private Integer fontsizeHeader;

  private Integer fontsize;

  private Boolean negativRot;

  private BaseColor colorHeader;

  private BaseColor colorTable;

  public Integer getLinks()
  {
    return links;
  }

  public void setLinks(Integer links)
  {
    this.links = links;
  }

  public Integer getRechts()
  {
    return rechts;
  }

  public void setRechts(Integer rechts)
  {
    this.rechts = rechts;
  }

  public Integer getOben()
  {
    return oben;
  }

  public void setOben(Integer oben)
  {
    this.oben = oben;
  }

  public Integer getUnten()
  {
    return unten;
  }

  public void setUnten(Integer unten)
  {
    this.unten = unten;
  }

  public Boolean getQuerformat()
  {
    return querformat;
  }

  public void setQuerformat(Boolean querformat)
  {
    this.querformat = querformat;
  }

  public Formular getVordergrund()
  {
    return vordergrund;
  }

  public void setVordergrund(Formular vordergrund)
  {
    this.vordergrund = vordergrund;
  }

  public Formular getHintergrund()
  {
    return hintergrund;
  }

  public void setHintergrund(Formular hintergrund)
  {
    this.hintergrund = hintergrund;
  }

  public Boolean getHeaderTransparent()
  {
    return headerTransparent;
  }

  public void setHeaderTransparent(Boolean headerTransparent)
  {
    this.headerTransparent = headerTransparent;
  }

  public Boolean getZellenTransparent()
  {
    return zellenTransparent;
  }

  public void setZellenTransparent(Boolean zellenTransparent)
  {
    this.zellenTransparent = zellenTransparent;
  }

  public Font getFontHeader()
  {
    return fontHeader;
  }

  public void setFontHeader(Font fontHeader)
  {
    this.fontHeader = fontHeader;
  }

  public Font getFontNormal()
  {
    return fontNormal;
  }

  public void setFontNormal(Font fontNormal)
  {
    this.fontNormal = fontNormal;
  }

  public Font getFontFett()
  {
    return fontFett;
  }

  public void setFontFett(Font fontFett)
  {
    this.fontFett = fontFett;
  }

  public Font getFontItalic()
  {
    return fontItalic;
  }

  public void setFontItalic(Font fontItalic)
  {
    this.fontItalic = fontItalic;
  }

  public Integer getFontsizeHeader()
  {
    return fontsizeHeader;
  }

  public void setFontsizeHeader(Integer fontsizeHeader)
  {
    this.fontsizeHeader = fontsizeHeader;
  }

  public Integer getFontsize()
  {
    return fontsize;
  }

  public void setFontsize(Integer fontsize)
  {
    this.fontsize = fontsize;
  }

  public Boolean getNegativRot()
  {
    return negativRot;
  }

  public void setNegativRot(Boolean negativRot)
  {
    this.negativRot = negativRot;
  }

  public BaseColor getColorHeader()
  {
    return colorHeader;
  }

  public void setColorHeader(BaseColor colorHeader)
  {
    this.colorHeader = colorHeader;
  }

  public BaseColor getColorTable()
  {
    return colorTable;
  }

  public void setColorTable(BaseColor colorTable)
  {
    this.colorTable = colorTable;
  }

}
