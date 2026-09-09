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

import java.rmi.RemoteException;

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.dialogs.BelegAuswahlDialog;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.IBeleg;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegZuordnenAction implements Action
{
  private TablePart part;

  public BelegZuordnenAction(TablePart tablePart)
  {
    this.part = tablePart;
  }

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      BelegAuswahlDialog d = new BelegAuswahlDialog();
      BuchungDokument[] belege = d.open();
      if (belege != null)
      {
        try
        {
          DBTransaction.starten();
          for (BuchungDokument b : belege)
          {
            ((IBeleg) context).addBeleg(b);
          }
          DBTransaction.commit();
        }
        catch (RemoteException | ApplicationException e)
        {
          DBTransaction.rollback();
          throw e;
        }
        // Erst jetzt Dokumente anzeigen, falls bei einem Beleg ein Fehler
        // passiert
        for (BuchungDokument b : belege)
        {
          part.addItem(b);
        }
        GUI.getStatusBar().setSuccessText("Beleg(e) erfolgreich zugeordnet");
      }
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Öffnen des Dialogs", e);
      GUI.getStatusBar().setErrorText("Fehler beim Öffnen des Dialogs");
    }
  }

}
