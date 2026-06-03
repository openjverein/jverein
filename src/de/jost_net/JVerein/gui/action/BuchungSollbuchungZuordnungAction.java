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
import de.jost_net.JVerein.DBTools.DBTransaction;
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
      Buchung[] buchungen = null;
      if (context instanceof Buchung)
      {
        buchungen = new Buchung[1];
        buchungen[0] = (Buchung) context;
      }
      if (context instanceof Buchung[])
      {
        buchungen = (Buchung[]) context;
      }
      if (buchungen.length == 0)
      {
        return;
      }
      if (buchungen[0].isNewObject())
      {
        return;
      }

      DBTransaction.starten();
      SollbuchungAuswahlDialog mkaz = new SollbuchungAuswahlDialog(buchungen[0],
          true);
      Object open = mkaz.open();

      if (!mkaz.getAbort())
      {
        // Sollbuchung entfernen
        if (open == null)
        {
          zuordnungLoeschen(buchungen);
        }
        else if (open instanceof Mitglied)
        {
          zuordnungMitglied(buchungen, (Mitglied) open);
        }
        else if (open instanceof Sollbuchung)
        {
          if (buchungen.length == 1)
          {
            zuordnungBuchungZuSollbuchung(buchungen[0], (Sollbuchung) open);
          }
          else
          {
            zuordnungBuchungenZuSollbuchung(buchungen, (Sollbuchung) open);
          }
        }
        else if (open instanceof Sollbuchung[])
        {
          if (buchungen.length == 1)
          {
            zuordnungBuchungZuSollbuchungen(buchungen[0], (Sollbuchung[]) open);
          }
          else
          {
            throw new ApplicationException(
                "Mehrere Buchungen mehreren Sollbuchungen zuordnen ist nicht möglich!");
          }
        }

        if (open == null)
        {
          GUI.getStatusBar()
              .setSuccessText("Sollbuchung von Buchung(en) gelöst");
        }
        else
        {
          GUI.getStatusBar().setSuccessText("Sollbuchung zugeordnet");
        }
        control.refreshBuchungsList();
      }
      DBTransaction.commit();
    }
    catch (OperationCanceledException oce)
    {
      DBTransaction.rollback();
      throw oce;
    }
    catch (ApplicationException ae)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(
          "Fehler bei der Zuordnung der Sollbuchung: " + ae.getMessage());
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      Logger.error("Fehler bei der Zuordnung der Sollbuchung", e);
      GUI.getStatusBar()
          .setErrorText("Fehler bei der Zuordnung der Sollbuchung");
    }
  }

  private void zuordnungLoeschen(Buchung[] buchungen)
      throws RemoteException, ApplicationException
  {
    for (Buchung buchung : buchungen)
    {
      buchung.setSollbuchung(null);
      buchung.store();
    }
  }

  private void zuordnungMitglied(Buchung[] buchungen, Mitglied mitglied)
      throws RemoteException, ApplicationException
  {
    Sollbuchung sollb = (Sollbuchung) Einstellungen.getDBService()
        .createObject(Sollbuchung.class, null);

    Double betrag = 0d;
    for (Buchung buchung : buchungen)
    {
      betrag += buchung.getBetrag();
    }

    sollb.setBetrag(betrag);
    sollb.setDatum(buchungen[0].getDatum());
    sollb.setMitglied(mitglied);
    sollb.setZahler(mitglied);
    sollb.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
    sollb.setZweck1(buchungen[0].getZweck());
    sollb.store();

    for (Buchung buchung : buchungen)
    {
      SollbuchungPosition sbp = (SollbuchungPosition) Einstellungen
          .getDBService().createObject(SollbuchungPosition.class, null);
      sbp.setBetrag(buchung.getBetrag());
      sbp.setBuchungsartId(buchung.getBuchungsartId());
      sbp.setBuchungsklasseId(buchung.getBuchungsklasseId());
      sbp.setSteuer(buchung.getSteuer());
      sbp.setDatum(buchung.getDatum());
      sbp.setZweck(buchung.getZweck());
      sbp.setSollbuchung(sollb.getID());
      sbp.store();
    }
  }

  private void zuordnungBuchungZuSollbuchung(Buchung buchung, Sollbuchung sollb)
      throws RemoteException, ApplicationException, Exception
  {
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
        Buchung restbuchung = SplitbuchungsContainer.autoSplit(buchung, sollb,
            false);
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

  private void zuordnungBuchungenZuSollbuchung(Buchung[] buchungen,
      Sollbuchung sollb) throws RemoteException, ApplicationException
  {
    // Mehrere Buchungen einer Sollbuchung zuordnen geht nur ohne Splitten.
    for (Buchung buchung : buchungen)
    {
      buchung.setSollbuchung(sollb);
      buchung.store();
    }
  }

  private void zuordnungBuchungZuSollbuchungen(Buchung buch,
      Sollbuchung[] sollbuchungen) throws Exception
  {
    if (buch.getSplitTyp() != null
        && (buch.getSplitTyp() == SplitbuchungTyp.GEGEN
            || buch.getSplitTyp() == SplitbuchungTyp.HAUPT))
    {
      throw new ApplicationException(
          "Haupt- oder Gegen-Buchungen können nicht mehreren Sollbuchungen zugeordnet werden!");
    }

    // Nach Datum sortieren, so dass die ältesten Buchngen als erstes
    // ausgeglichen werden
    Arrays.sort(sollbuchungen, (s1, s2) -> {
      try
      {
        return s1.getDatum().compareTo(s2.getDatum());
      }
      catch (RemoteException e)
      {
        return 0;
      }
    });

    Buchung buchung = buch;

    for (Sollbuchung s : sollbuchungen)
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
          buchung.setSollbuchung(sollbuchungen[sollbuchungen.length - 1]);
          buchung.store();
        }
      }
      catch (OperationCanceledException ignore)
      {
      }
    }
  }

}
