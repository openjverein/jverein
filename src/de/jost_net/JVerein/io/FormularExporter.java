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
import java.io.IOException;
import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public abstract class FormularExporter implements Exporter
{

  @Override
  public abstract String getName();

  @Override
  public abstract IOFormat[] getIOFormats(Class<?> objectType);

  protected File file;

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws ApplicationException, IOException
  {
    this.file = file;
    open();
    DBIterator<Buchungsklasse> it = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    it.setOrder("order by nummer");
    if (it.size() == 0)
    {
      throw new ApplicationException("Es existieren keine Buchungsklassen");
    }
    while (it.hasNext())
    {
      Formular formular = (Formular) it.next();
      addFormular(formular);

      DBIterator<Formularfeld> it2 = Einstellungen.getDBService()
          .createList(Formularfeld.class);
      it2.addFilter("formular = ?", formular.getID());
      while (it2.hasNext())
      {
        Formularfeld formularfeld = it2.next();
        addFormularfeld(formularfeld);
      }
    }
    close();
  }

  @Override
  public String getDateiname()
  {
    return "kontenrahmen";
  }

  protected abstract void open() throws IOException;

  protected abstract void addFormular(Formular formular) throws RemoteException;

  protected abstract void addFormularfeld(Formularfeld formularfeld)
      throws RemoteException;

  protected abstract void close() throws IOException;
}
