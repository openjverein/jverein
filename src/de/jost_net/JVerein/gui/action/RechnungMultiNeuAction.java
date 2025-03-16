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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.dialogs.RechnungDialog;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungMultiNeuAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Sollbuchung[] sollbs;
    // if (context instanceof TablePart)
    // {
    // TablePart tp = (TablePart) context;
    // context = tp.getSelection();
    // }
    if (context instanceof Sollbuchung[])
    {
      sollbs = (Sollbuchung[]) context;
    }
    else
    {
      throw new ApplicationException("Keine Sollbuchungen ausgewählt");
    }

    try
    {
      RechnungDialog dialog = new RechnungDialog();
      if (!dialog.open())
      {
        throw new OperationCanceledException();
      }
      Formular formular = dialog.getFormular();
      Date rechnungsdatum = dialog.getDatum();
      if (formular == null || rechnungsdatum == null)
      {
        throw new OperationCanceledException();
      }

      String mitglied = sollbs[0].getMitgliedId();
      Long zahler = sollbs[0].getZahlerId();
      Integer zahlungsweg = sollbs[0].getZahlungsweg();
      for (Sollbuchung sollb : sollbs)
      {
        if (sollb.getRechnung() != null)
        {
          throw new ApplicationException(
              "Zu mindestens einer Sollbuchung existiert bereits eine Rechnung");
        }
        if (!mitglied.equals(sollb.getMitgliedId()))
        {
          throw new ApplicationException(
              "Es sind nicht alle Sollbuchungen dem gleichen Mitglied zugeordnet.");
        }
        if ((zahler == null && sollb.getZahlerId() != null)
            || (zahler != null && !zahler.equals(sollb.getZahlerId())))
        {
          throw new ApplicationException(
              "Es sind nicht alle Sollbuchungen dem gleichen Zahler zugeordnet.");
        }
        if (zahlungsweg == null || sollb.getZahlungsweg() == null
            || !zahlungsweg.equals(sollb.getZahlungsweg()))
        {
          throw new ApplicationException(
              "Es haben nicht alle Sollbuchungen den gleichen Zahlungsweg.");
        }
      }

      DBTransaction.starten();
      Rechnung rechnung = (Rechnung) Einstellungen.getDBService()
          .createObject(Rechnung.class, null);
      rechnung.setFormular(formular);
      rechnung.setDatum(rechnungsdatum);
      rechnung.fill(sollbs[0]);
      rechnung.store();

      for (Sollbuchung sollb : sollbs)
      {
        sollb.setRechnung(rechnung);
        sollb.store();
      }
      DBTransaction.commit();
      GUI.getCurrentView().reload();
      GUI.getStatusBar().setSuccessText("Rechnung erstellt.");
    }
    catch (OperationCanceledException ignore)
    {
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(e.getMessage());
      return;
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      String fehler = "Fehler beim erstellen der Rechnung";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
      return;
    }
  }
}
