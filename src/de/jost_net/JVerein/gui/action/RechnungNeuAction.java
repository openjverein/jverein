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
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.dialogs.RechnungDialog;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Sollbuchung;
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
    Sollbuchung[] sollbs;
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context instanceof MitgliedskontoNode)
    {
      MitgliedskontoNode mkn = (MitgliedskontoNode) context;

      if (mkn.getType() == MitgliedskontoNode.SOLL)
      {
        try
        {
          context = Einstellungen.getDBService().createObject(Sollbuchung.class,
              mkn.getID());
        }
        catch (RemoteException e)
        {
          throw new ApplicationException("Fehler beim erstellen der Rechnung!");
        }
      }
    }
    if (context instanceof Sollbuchung)
    {
      sollbs = new Sollbuchung[] { (Sollbuchung) context };
    }
    else if (context instanceof Sollbuchung[])
    {
      sollbs = (Sollbuchung[]) context;
    }
    else
    {
      throw new ApplicationException("Keine Sollbuchung ausgewählt");
    }

    try
    {
      boolean mitRechnung = false;
      boolean mitErstattung = false;
      for (Sollbuchung sollb : sollbs)
      {
        if (sollb.getRechnung() != null)
        {
          continue;
        }
        if (sollb.getBetrag() > -0.005d)
        {
          mitRechnung = true;
        }
        else
        {
          mitErstattung = true;
        }
      }
      if (!mitRechnung && !mitErstattung)
      {
        throw new ApplicationException(
            "Es sind keine Sollbuchungen ohne Rechnung ausgewählt!");
      }
      RechnungDialog dialog = new RechnungDialog(mitRechnung, mitErstattung);
      if (!dialog.open())
      {
        return;
      }
      Formular formularRechnung = dialog.getFormularRechnung();
      Formular formularErstattung = dialog.getFormularErstattung();
      Date rechnungsdatum = dialog.getDatum();
      boolean sollbuchungsDatum = dialog.getSollbuchungsdatum();
      if ((mitRechnung && formularRechnung == null)
          || (mitErstattung && formularErstattung == null)
          || (rechnungsdatum == null && !sollbuchungsDatum))
      {
        return;
      }
      int erstellt = 0;
      int skip = 0;
      for (Sollbuchung sollb : sollbs)
      {
        if (sollb.getRechnung() != null)
        {
          skip++;
          continue;
        }
        Rechnung rechnung = (Rechnung) Einstellungen.getDBService()
            .createObject(Rechnung.class, null);
        if (sollb.getBetrag() > -0.005d)
        {
          rechnung.setFormular(formularRechnung);
        }
        else
        {
          rechnung.setFormular(formularErstattung);
        }

        if (sollbuchungsDatum)
        {
          rechnung.setDatum(sollb.getDatum());
        }
        else
        {
          rechnung.setDatum(rechnungsdatum);
        }
        rechnung.fill(sollb);
        rechnung.store();

        sollb.setRechnung(rechnung);
        sollb.updateForced();
        erstellt++;
      }
      if (erstellt == 0)
      {
        GUI.getStatusBar().setErrorText("Keine Rechnung erstellt, alle " + skip
            + " Sollbuchungen enthalten bereits Rechnungen.");
      }
      else
      {
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
      String fehler = "Fehler beim Erstellen der Rechnung: ";
      GUI.getStatusBar().setErrorText(fehler + e.getMessage());
      Logger.error(fehler, e);
      return;
    }
  }
}
