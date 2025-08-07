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
package de.jost_net.JVerein.gui.action;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import de.jost_net.JVerein.gui.dialogs.QRCodeImageDialog;
import de.jost_net.JVerein.io.VCardTool;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import ezvcard.Ezvcard;
import ezvcard.VCardVersion;

public class MitgliedVCardQRCodeAction implements Action
{

  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (context != null && context instanceof Mitglied)
      {
        ArrayList<Mitglied> mitgl = new ArrayList<>();
        mitgl.add((Mitglied) context);

        String qrCodeData = Ezvcard.write(VCardTool.getVCards(mitgl))
            .version(VCardVersion.V3_0).go();
        String charset = "UTF-8"; // or "ISO-8859-1"
        @SuppressWarnings("rawtypes")
        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        BitMatrix matrix = new MultiFormatWriter().encode(
            new String(qrCodeData.getBytes(charset), charset),
            BarcodeFormat.QR_CODE, 300, 300, hintMap);
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);

        QRCodeImageDialog dia = new QRCodeImageDialog(
            AbstractDialog.POSITION_MOUSE, bi);
        dia.open();
      }
      else
      {
        throw new ApplicationException("Kein Mitglied ausgewählt");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText("Fehler beim Generieren des QR Codes");
    }
  }
}
