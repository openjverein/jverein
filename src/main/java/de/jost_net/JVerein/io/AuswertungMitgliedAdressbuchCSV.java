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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.gui.view.AuswertungMitgliedView;
import de.jost_net.JVerein.gui.view.AuswertungNichtMitgliedView;
import de.jost_net.JVerein.io.Adressbuch.Txt;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedAdressbuchCSV
    extends AuswertungMitgliedAbstractCSV
{

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    try
    {
      Mitglied[] list = (Mitglied[]) objects[0];
      Txt txt = new Txt(file, ";");
      for (Mitglied m : list)
      {
        txt.add(m);
      }
      txt.close();
      GUI.getStatusBar().setSuccessText(
          String.format("Auswertung fertig. %d Sätze.", list.length));
    }
    catch (IOException e)
    {
      throw new ApplicationException(e);
    }
  }

  @Override
  public String toString()
  {
    return getName();
  }

  @Override
  public String getName()
  {
    return "Adressbuchexport CSV";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != AuswertungMitgliedView.class
        && objectType != AuswertungNichtMitgliedView.class)
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
