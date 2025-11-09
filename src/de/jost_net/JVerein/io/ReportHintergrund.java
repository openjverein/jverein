package de.jost_net.JVerein.io;

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

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Setzen eines Hintergrundes bei Reports.
 */
class ReportHintergrund extends PdfPageEventHelper
{

  private PdfImportedPage importedPage;

  public ReportHintergrund(PdfImportedPage importedPage)
  {
    this.importedPage = importedPage;
  }

  @Override
  public void onStartPage(PdfWriter writer, Document document)
  {
    if (importedPage != null)
    {
      PdfContentByte contentByte = writer.getDirectContentUnder();
      contentByte.addTemplate(importedPage, 0, 0);
    }
  }
}
