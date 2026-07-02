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

package de.jost_net.JVerein.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedAdressbuchCSV
    extends AuswertungMitgliedAbstractCSV
{
  private OutputStreamWriter out;

  private String separator = ";";

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    try
    {
      /*
       * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
       * objects[2] ist Mitgliedstyp
       */
      @SuppressWarnings("unchecked")
      ArrayList<Mitglied> list = (ArrayList<Mitglied>) objects[0];
      out = new OutputStreamWriter(
          new BufferedOutputStream(new FileOutputStream(file)));
      out.write("\"Name\"" + separator + "\"Vorname\"" + separator
          + "\"Strasse\"" + separator + "\"PLZ\"" + separator + "\"Ort\""
          + separator + "\"Staat\"" + separator + "\"Anzeigename\"" + separator
          + "\"Email\"" + separator + "\"TelefonPrivat\"" + separator
          + "\"TelefonMobil\"\n");

      for (Mitglied m : list)
      {
        out.write(getZeile(m));
      }
      out.close();
      GUI.getStatusBar().setSuccessText(
          String.format("Auswertung fertig. %d Sätze.", list.size()));
    }
    catch (IOException e)
    {
      throw new ApplicationException(e);
    }
  }

  public String getZeile(Mitglied mitglied) throws RemoteException
  {
    StringBuilder sb = new StringBuilder();
    sb.append("\"" + mitglied.getName() + "\"" + separator);
    sb.append("\"" + mitglied.getVorname() + "\"" + separator);
    sb.append("\"" + mitglied.getStrasse() + "\"" + separator);
    sb.append("\"" + mitglied.getPlz() + "\"" + separator);
    sb.append("\"" + mitglied.getOrt() + "\"" + separator);
    sb.append("\"" + (mitglied.getStaat() != null ? mitglied.getStaat() : "")
        + "\"" + separator);
    sb.append("\"" + mitglied.getVorname() + " " + mitglied.getName() + "\""
        + separator);
    sb.append("\"" + mitglied.getEmail() + "\"" + separator);
    sb.append("\"" + mitglied.getTelefonprivat() + "\"" + separator);
    sb.append("\"" + mitglied.getHandy() + "\"" + separator);
    sb.append("\n");
    return sb.toString();
  }

  @Override
  public String getName()
  {
    return "Adressbuchexport CSV";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != MitgliedListeView.class
        && objectType != NichtMitgliedListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungMitgliedAdressbuchCSV.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.csv" };
      }
    };
    return new IOFormat[] { f };
  }
}
