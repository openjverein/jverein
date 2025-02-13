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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.SollbuchungAuswahlDialog;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Mitgliedskonto zuordnen.
 */
public class BuchungSollbuchungZuordnungAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung)
        && !(context instanceof Buchung[]))
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
      Mitgliedskonto mk = null;

      if (!mkaz.getAbort())
      {
        if (open instanceof Mitgliedskonto)
        {
          mk = (Mitgliedskonto) open;
        }
        else if (open instanceof Mitglied)
        {
          Mitglied m = (Mitglied) open;
          mk = (Mitgliedskonto) Einstellungen.getDBService().createObject(
              Mitgliedskonto.class, null);

          Double betrag = 0d;
          for (Buchung buchung : b)
          {
            betrag += buchung.getBetrag();
          }

          mk.setBetrag(betrag);
          mk.setDatum(b[0].getDatum());
          mk.setMitglied(m);
          Long zahlerId = m.getZahlerID();
          if (zahlerId != null)
          {
            mk.setZahlerId(zahlerId);
          }
          else
          {
            mk.setZahler(m);
          }
          mk.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
          mk.setZweck1(b[0].getZweck());
          mk.store();

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
            sbp.setDatum(buchung.getDatum());
            sbp.setZweck(buchung.getZweck());
            sbp.setSollbuchung(mk.getID());
            sbp.store();
          }
        }

        if (open instanceof Mitgliedskonto[])
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

          b[0].transactionBegin();
          Mitgliedskonto[] mks = (Mitgliedskonto[]) open;
          mk = mks[0];
          Buchung buchung = b[0];

          double summe = 0d;
          for (Mitgliedskonto m : mks)
          {
            summe += m.getBetrag();
          }
          if (buchung.getBetrag() != summe)
          {
            YesNoDialog dialog = new YesNoDialog(YesNoDialog.POSITION_CENTER);
            dialog.setTitle("Buchung splitten");
            dialog.setText(
                "Die Summe der Sollbuchungen entspricht nicht dem Betrag der Buchung.\n"
                    + "Soll die Buchung trotzdem anhand der Sollbuchungspositionen\n"
                    + "gesplittet und eine Restbuchung erzeugt werden?");
            if (!((Boolean) dialog.open()).booleanValue())
            {
              throw new OperationCanceledException();
            }
          }
          try
          {
            for (Mitgliedskonto m : mks)
            {
              if (buchung == null)
              {
                // Wenn keine Restbuchung existiert muss eine neue erstellt
                // werden
                buchung = (Buchung) Einstellungen.getDBService()
                    .createObject(Buchung.class, null);

                buchung.setBetrag(0);
                buchung.setDatum(b[0].getDatum());
                buchung.setKonto(b[0].getKonto());
                buchung.setName(b[0].getName());
                buchung.setZweck(b[0].getZweck());
                buchung.setSplitTyp(SplitbuchungTyp.SPLIT);
                buchung.setSplitId(b[0].getSplitId());
                buchung.setBuchungsartId(b[0].getBuchungsartId());
                buchung.setBuchungsklasseId(b[0].getBuchungsklasseId());

                SplitbuchungsContainer.init(b[0]);
                SplitbuchungsContainer.add(buchung);
                SplitbuchungsContainer.store();
              }
              buchung = SplitbuchungsContainer.autoSplit(buchung, m, true);
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
        else
        {
          if (b.length == 1)
          {
            SplitbuchungsContainer.autoSplit(b[0], mk, false);
          }
          else
          {
            for (Buchung buchung : b)
            {
              buchung.setMitgliedskonto(mk);
              buchung.store();
            }
          }
        }

        if (mk == null)
        {
          GUI.getStatusBar().setSuccessText("Sollbuchung gelöscht");
        } 
        else
        {
          GUI.getStatusBar().setSuccessText("Sollbuchung zugeordnet");
        }
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException ae)
    {
      throw new ApplicationException(ae.getLocalizedMessage());
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText(
          "Fehler bei der Zuordnung der Sollbuchung");
    }
  }
}
