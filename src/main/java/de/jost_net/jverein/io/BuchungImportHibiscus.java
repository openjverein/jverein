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
package de.jost_net.jverein.io;

import java.io.File;

import de.jost_net.jverein.gui.dialogs.BuchungsuebernahmeDialog;
import de.jost_net.jverein.gui.view.BuchungListeView;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ProgressMonitor;

public class BuchungImportHibiscus implements Importer
{

  @Override
  public void doImport(Object context, IOFormat format, File file,
      String encoding, ProgressMonitor monitor) throws Exception
  {
    BuchungsuebernahmeDialog d = new BuchungsuebernahmeDialog(
        BuchungsuebernahmeDialog.POSITION_CENTER);
    if (d.open())
    {
      new Buchungsuebernahme(true);
    }
    else
    {
      throw new OperationCanceledException();
    }
  }

  @Override
  public String getName()
  {
    return "Hibiscus-Buchungsimport";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != BuchungListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return BuchungImportHibiscus.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        // Kein import aus einer Datei!
        return null;
      }
    };
    return new IOFormat[] { f };
  }
}
