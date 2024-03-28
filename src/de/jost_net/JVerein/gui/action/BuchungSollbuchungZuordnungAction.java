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
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.dialogs.SollbuchungAuswahlDialog;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Mitgliedskonto zuordnen.
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
      if (b == null)
      {
        return;
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
          mk.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
          mk.setZweck1(b[0].getZweck());
          mk.store();
        }

        for (Buchung buchung : b)
        {
          buchung.setMitgliedskonto(mk);
          buchung.store();
        }
        control.getBuchungsList();

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
