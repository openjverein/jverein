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
import java.util.Arrays;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.dialogs.SollbuchungAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Sollbuchung zuordnen.
 */
public class BuchungSollbuchungZuordnungAction implements Action
{
  private BuchungsControl control;

  public BuchungSollbuchungZuordnungAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || !(context instanceof Buchung) && !(context instanceof Buchung[]))
    {
      throw new ApplicationException("Keine Buchung(en) ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b.length == 0)
      {
        return;
      }
      if (b[0].isNewObject())
      {
        return;
      }
      SollbuchungAuswahlDialog mkaz = new SollbuchungAuswahlDialog(b[0]);
      Object open = mkaz.open();
      Sollbuchung sollb = null;

      if (!mkaz.getAbort())
      {
        if (open instanceof Sollbuchung)
        {
          sollb = (Sollbuchung) open;
        }
        else if (open instanceof Mitglied)
        {
          Mitglied m = (Mitglied) open;
          sollb = (Sollbuchung) Einstellungen.getDBService()
              .createObject(Sollbuchung.class, null);

          Double betrag = 0d;
          for (Buchung buchung : b)
          {
            betrag += buchung.getBetrag();
          }

          sollb.setBetrag(betrag);
          sollb.setDatum(b[0].getDatum());
          sollb.setMitglied(m);
          sollb.setZahlerId(m.getZahlerID());
          sollb.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
          sollb.setZweck1(b[0].getZweck());
          sollb.store();

          for (Buchung buchung : b)
          {
            SollbuchungPosition sbp = (SollbuchungPosition) Einstellungen
                .getDBService().createObject(SollbuchungPosition.class, null);
            sbp.setBetrag(buchung.getBetrag());
            if (buchung.getBuchungsartId() != null)
            {
              sbp.setBuchungsartId(buchung.getBuchungsartId());
            }
            if (buchung.getBuchungsklasseId() != null)
            {
              sbp.setBuchungsklasseId(buchung.getBuchungsklasseId());
            }
            if (buchung.getSteuer() != null)
            {
              sbp.setSteuer(buchung.getSteuer());
            }
            sbp.setDatum(buchung.getDatum());
            sbp.setZweck(buchung.getZweck());
            sbp.setSollbuchung(sollb.getID());
            sbp.store();
          }
        }

        // Sollbuchung entfernen
        if (open == null)
        {
          for (Buchung buchung : b)
          {
            buchung.setSollbuchung(null);
            buchung.store();
          }
          GUI.getStatusBar().setSuccessText("Sollbuchung von Buchung gelöst");
        }
        // Buchung mehreren Sollbuchungen zuordnen
        else if (open instanceof Sollbuchung[])
        {
          if (b.length > 1)
          {
            throw new ApplicationException(
                "Mehrere Buchungen mehreren Sollbuchungen zuordnen nicht möglich!");
          }
          if (b[0].getSplitTyp() != null
              && (b[0].getSplitTyp() == SplitbuchungTyp.GEGEN
                  || b[0].getSplitTyp() == SplitbuchungTyp.HAUPT))
          {
            throw new ApplicationException(
                "Haupt- oder Gegen-Buchungen können nicht mehreren Sollbuchungen zugeordnet werden!");
          }

          Sollbuchung[] sollbs = (Sollbuchung[]) open;
          // Nach Datum sortieren, so dass die ältesten Buchngen als erstes
          // ausgeglichen werden
          Arrays.sort(sollbs, (s1, s2) -> {
            try
            {
              return s1.getDatum().compareTo(s2.getDatum());
            }
            catch (RemoteException e)
            {
              return 0;
            }
          });

          sollb = sollbs[0];
          Buchung buchung = b[0];

          try
          {
            b[0].transactionBegin();
            for (Sollbuchung s : sollbs)
            {
              if (buchung == null)
              {
                // Wenn keine Restbuchung existiert wurde alles zugewiesen und
                // es ist nichts mehr für die restlichen Sollbuchungen übrig.
                break;
              }
              buchung = SplitbuchungsContainer.autoSplit(buchung, s, false);
            }
            if (buchung != null)
            {
              YesNoDialog dialog = new YesNoDialog(YesNoDialog.POSITION_CENTER);
              dialog.setTitle("Buchung splitten");
              dialog.setText(
                  "Der Betrag der Buchung ist größer als der Fehlbetrag der Sollbuchungen.\n"
                      + "Soll die Restbuchung auch der Sollbuchung zugewiesen werden?");
              try
              {
                if ((Boolean) dialog.open())
                {
                  buchung.setSollbuchung(sollbs[sollbs.length - 1]);
                  buchung.store();
                }
              }
              catch (OperationCanceledException ignore)
              {
              }
            }
            b[0].transactionCommit();
          }
          catch (Exception e)
          {
            b[0].transactionRollback();
            Logger.error("Fehler", e);
            throw new ApplicationException(
                "Fehler beim Splitten der Buchung: " + e.getLocalizedMessage());
          }
        }
        // Buchung einer Sollbuchung zuordnen
        else
        {
          if (b.length == 1)
          {
            Buchung buchung = b[0];
            if (Math.abs(buchung.getBetrag()
                - (sollb.getBetrag() - sollb.getIstSumme())) >= 0.01d)
            {
              YesNoCancelDialog dialog = new YesNoCancelDialog(
                  YesNoDialog.POSITION_CENTER, true);
              dialog.setTitle("Buchung splitten");
              dialog.setText(
                  "Die Fehlbetrag der Sollbuchung und der Betrag der Buchung stimmen nicht überein.\n"
                      + "Soll die Buchung automatisch gesplittet werden?\n"
                      + "Bei 'Nein' wird die Sollbuchung ohne Splitten zugeordnet.");
              int ret = dialog.open();
              if (ret == YesNoCancelDialog.YES)
              {
                Buchung restbuchung = SplitbuchungsContainer.autoSplit(buchung,
                    sollb, true);
                if (restbuchung != null)
                {
                  YesNoDialog restbuchungDialog = new YesNoDialog(
                      YesNoDialog.POSITION_CENTER);
                  restbuchungDialog.setTitle("Restbuchung zuordnen");
                  restbuchungDialog.setText(
                      "Der Betrag der Buchung ist größer als der Fehlbetrag der Sollbuchung.\n"
                          + "Soll die Restbuchung auch der Sollbuchung zugewiesen werden?");
                  try
                  {
                    if ((Boolean) restbuchungDialog.open())
                    {
                      restbuchung.setSollbuchung(sollb);
                      restbuchung.store();
                    }
                  }
                  catch (OperationCanceledException ignore)
                  {
                  }
                }
              }
              else if (ret == YesNoCancelDialog.NO)
              {
                // Bei NEIN ohne Splitten zuordnen
                buchung.setSollbuchung(sollb);
                buchung.store();
              }
              else if (ret == YesNoCancelDialog.CANCEL)
              {
                throw new OperationCanceledException();
              }

            }
            else
            {
              // Fehlbetrag und Buchungs-Betrag stimmen überein
              SplitbuchungsContainer.autoSplit(buchung, sollb, true);
            }
          }
          else
          {
            // Mehrere Buchungen einer Sollbuchung zuordnen geht nur ohne
            // Splitten.
            for (Buchung buchung : b)
            {
              buchung.setSollbuchung(sollb);
              buchung.store();
            }
          }
        }

        GUI.getStatusBar().setSuccessText("Sollbuchung zugeordnet");
        control.refreshBuchungsList();
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(
          "Fehler bei der Zuordnung der Sollbuchung " + ae.getMessage());
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei der Zuordnung der Sollbuchung", e);
      GUI.getStatusBar()
          .setErrorText("Fehler bei der Zuordnung der Sollbuchung");
    }
  }
}
