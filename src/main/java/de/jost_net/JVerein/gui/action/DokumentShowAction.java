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

import java.io.File;
import java.io.IOException;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Anzeigen von Dokumenten
 */
public class DokumentShowAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Beleg))
    {
      throw new ApplicationException("Kein Beleg ausgewählt");
    }
    try
    {
      Beleg ad = (Beleg) context;
      File file = ad.getFile();
      if (file != null)
      {
        FileViewer.show(file);
      }
    }
    catch (IOException e)
    {
      String fehler = "Fehler beim Anzeigen des Dokuments";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler + ": " + e.getMessage());
    }
  }
}
