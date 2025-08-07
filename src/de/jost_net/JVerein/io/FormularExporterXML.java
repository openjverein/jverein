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
import java.rmi.RemoteException;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class FormularExporterXML implements Exporter
{
  @Override
  public String getName()
  {
    return "Formular XML-Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Formular.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return FormularExporterXML.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.xml" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname()
  {
    return "formular";
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws RemoteException, ApplicationException,
      FileNotFoundException, DocumentException, IOException
  {
    XmlWriter writer = new XmlWriter(
        new BufferedOutputStream(new FileOutputStream(file)));

    int count = 1;
    if (objects[0] instanceof DBObject[])
    {
      objects = (Object[]) objects[0];
    }
    for (Object o : objects)
    {
      DBObject dbObject = (DBObject) o;
      writer.write(dbObject);

      DBIterator<Formularfeld> formularfeldIt = Einstellungen.getDBService()
          .createList(Formularfeld.class);
      formularfeldIt.addFilter("formular = ?", dbObject.getID());
      while (formularfeldIt.hasNext())
      {
        writer.write(formularfeldIt.next());
      }
      monitor.setPercentComplete(count * 100 / objects.length);
    }

    writer.close();
  }

}
