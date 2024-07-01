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

import java.util.Date;

import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.gui.dialogs.BuchungenDeleteDialog;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchung.
 */
public class BuchungenDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    
    try
    {
      BuchungenDeleteDialog d = new BuchungenDeleteDialog(BuchungenDeleteDialog.POSITION_CENTER);
      Date choice = (Date) d.open();
      if (choice == null)
      {
        return;
      }

      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("datum < ?", choice);
      int count = 0;
      Buchung b = null;
      while (it.hasNext())
      {
        b = it.next();
        try
        {
          if (d.getLoeschen() && (b.getMitgliedskonto() != null))
          {
            b.getMitgliedskonto().delete();
          }
        }
        catch (Exception e)
        {
          // Das kann passieren wenn der Sollbuchung mehrere Buchungen 
          // zugeordnet waren. Dann existiert die Sollbuchung nicht mehr  
          // bei den weiteren Buchungen da das Query vorher erfolgt ist
        }
        b.delete();
        count++;
      }
      if (count > 0)
      {
        GUI.getStatusBar().setSuccessText(String.format(
            "%d Buchung" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        GUI.getStatusBar().setErrorText("Keine Buchung gelöscht");
      }
      Application.getMessagingFactory().sendMessage(new BuchungMessage(b));
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Buchungen.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
