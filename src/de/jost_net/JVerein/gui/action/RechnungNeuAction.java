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
import de.jost_net.JVerein.gui.dialogs.RechnungDialog;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungNeuAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitgliedskonto[] mks;
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context instanceof Mitgliedskonto)
    {
      mks = new Mitgliedskonto[] { (Mitgliedskonto) context };
    }
    else if (context instanceof Mitgliedskonto[])
    {
      mks = (Mitgliedskonto[]) context;
    }
    else
    {
      throw new ApplicationException("Keine Sollbuchung ausgewählt");
    }

    try
    {
      RechnungDialog dialog = new RechnungDialog();
      if (!dialog.open())
      {
        return;
      }
      Formular formular = dialog.getFormular();
      Date rechnungsdatum = dialog.getDatum();
      if (formular == null || rechnungsdatum == null)
      {
        return;
      }
      int erstellt = 0;
      int skip = 0;
      for (Mitgliedskonto mk : mks)
      {
        if (mk.getRechnung() != null)
        {
          skip++;
          continue;
        }
        Rechnung rechnung = (Rechnung) Einstellungen.getDBService()
            .createObject(Rechnung.class, null);

        rechnung.setFormular(formular);
        rechnung.setDatum(rechnungsdatum);
        rechnung.fill(mk);
        rechnung.store();

        mk.setRechnung(rechnung);
        mk.store();
        erstellt++;
      }
      if (erstellt == 0)
      {
        GUI.getStatusBar().setErrorText("Keine Rechnung erstellt, alle " + skip
            + " Sollbuchungen enthalten bereits Rechnungen.");
      }
      else {
        GUI.getCurrentView().reload();
        GUI.getStatusBar().setSuccessText(erstellt + " Rechnung(en) erstellt"
            + (skip > 0 ? ", " + skip + " vorhandene übersprungen." : "."));
      }
    }
    catch (OperationCanceledException ignore)
    {

    }
    catch (Exception e)
    {
      String fehler = "Fehler beim erstellen der Rechnung";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
      return;
    }
  }
}
