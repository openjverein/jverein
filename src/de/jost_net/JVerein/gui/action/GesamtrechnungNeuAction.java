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
import java.util.ArrayList;
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.dialogs.RechnungDialog;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GesamtrechnungNeuAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Sollbuchung[] sollbs;
    if (context instanceof Sollbuchung[])
    {
      sollbs = (Sollbuchung[]) context;
    }
    else if (context instanceof MitgliedskontoNode[])
    {
      MitgliedskontoNode[] mkns = (MitgliedskontoNode[]) context;

      ArrayList<Sollbuchung> sollArray = new ArrayList<>();
      for (MitgliedskontoNode mkn : mkns)
      {
        // Nur Sollbuchungen behandeln, Istbuchungen ignorieren wir
        if (mkn.getType() == MitgliedskontoNode.SOLL)
        {
          try
          {
            sollArray.add(Einstellungen.getDBService()
                .createObject(Sollbuchung.class, mkn.getID()));
          }
          catch (RemoteException e)
          {
            throw new ApplicationException(
                "Fehler beim erstellen der Rechnung!");
          }
        }
      }
      if (sollArray.size() <= 1)
      {
        throw new ApplicationException(
            "Es sind zu wenig Sollbuchungen ausgewählt.");
      }
      sollbs = sollArray.toArray(new Sollbuchung[sollArray.size()]);
    }
    else
    {
      throw new ApplicationException(
          "Es sind zu wenig Sollbuchungen ausgewählt.");
    }

    try
    {
      RechnungDialog dialog = new RechnungDialog();
      if (!dialog.open())
      {
        throw new OperationCanceledException();
      }
      Formular formularRechnung = dialog.getFormularRechnung();
      Formular formularErstattung = dialog.getFormularErstattung();
      Date rechnungsdatum = dialog.getDatum();
      boolean sollbuchungsDatum = dialog.getSollbuchungsdatum();
      if (formularRechnung == null || formularErstattung == null
          || (rechnungsdatum == null && !sollbuchungsDatum))
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
              "Es können nur Sollbuchungen eines Mitglieds zu einer Rechnung zusammengefasst werden.");
        }
        if ((zahler == null && sollb.getZahlerId() != null)
            || (zahler != null && !zahler.equals(sollb.getZahlerId())))
        {
          throw new ApplicationException(
              "Es können nur Sollbuchungen des gleichen Zahlers zu einer Rechnung zusammengefasst werden.");
        }
        if (zahlungsweg == null || sollb.getZahlungsweg() == null
            || !zahlungsweg.equals(sollb.getZahlungsweg()))
        {
          throw new ApplicationException(
              "Es können nur Sollbuchungen mit demselben Zahlungsweg zu einer Rechnung zusammengefasst werden.");
        }
      }

      DBTransaction.starten();
      Rechnung rechnung = (Rechnung) Einstellungen.getDBService()
          .createObject(Rechnung.class, null);
      rechnung.setFormular(formularRechnung);
      if (sollbuchungsDatum)
      {
        // Datum der letzten Sollbuchung finden und als Rechnungsdatum verwenden
        Date datum = null;
        for (Sollbuchung sb : sollbs)
        {
          Date d = sb.getDatum();
          if (datum == null || d.after(datum))
          {
            datum = d;
          }
        }
        rechnung.setDatum(datum);
      }
      else
      {
        rechnung.setDatum(rechnungsdatum);
      }
      rechnung.fill(sollbs[0]);
      rechnung.store();

      double summe = 0d;
      for (Sollbuchung sollb : sollbs)
      {
        summe += sollb.getBetrag();
        sollb.setRechnung(rechnung);
        sollb.updateForced();
      }
      rechnung.setBetrag(summe);
      if (summe <= -0.005d)
      {
        rechnung.setFormular(formularErstattung);
      }
      rechnung.store();

      DBTransaction.commit();
      GUI.getCurrentView().reload();
      GUI.getStatusBar().setSuccessText("Gesamtrechnung erstellt.");
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
      String fehler = "Fehler beim Erstellen der Gesamtrechnung";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
      return;
    }
  }
}
