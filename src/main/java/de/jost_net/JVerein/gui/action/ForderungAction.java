/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung für Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2015 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.util.ArrayList;

import de.jost_net.JVerein.gui.dialogs.ForderungDialog;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Nicht wiederkehrende einmalige Abrechnung
 */
public class ForderungAction implements Action
{
  private Mitglied[] mitglieder = null;

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {

      if (context instanceof Mitglied)
      {
        mitglieder = new Mitglied[] { (Mitglied) context };
      }
      else if (context instanceof Mitglied[])
      {
        mitglieder = (Mitglied[]) context;
      }
      else if (context instanceof Lehrgangsteilnehmer)
      {
        mitglieder = new Mitglied[] {
            ((Lehrgangsteilnehmer) context).getMitglied() };
      }
      else if (context instanceof Lehrgangsteilnehmer[])
      {
        Lehrgangsteilnehmer[] lge = (Lehrgangsteilnehmer[]) context;
        ArrayList<Mitglied> list = new ArrayList<>();
        for (Lehrgangsteilnehmer lg : lge)
        {
          list.add(lg.getMitglied());
        }
        if (list.size() > 0)
        {
          mitglieder = new Mitglied[list.size()];
          list.toArray(mitglieder);
        }
        else
        {
          throw new ApplicationException("Keine Lehrgänge ausgewählt!");
        }
      }
      else if (context instanceof Lehrgang)
      {
        mitglieder = ((Lehrgang) context).getMitglieder();
        if (mitglieder == null || mitglieder.length == 0)
        {
          throw new ApplicationException("Der Lehrgang hat keine Teilnehmer!");
        }
      }
      else if (context instanceof Lehrgang[])
      {
        Lehrgang[] lge = (Lehrgang[]) context;
        ArrayList<Mitglied> list = new ArrayList<>();
        for (Lehrgang lg : lge)
        {
          for (Mitglied m : lg.getMitglieder())
          {
            list.add(m);
          }
        }
        if (list.size() > 0)
        {
          mitglieder = new Mitglied[list.size()];
          list.toArray(mitglieder);
        }
        else
        {
          throw new ApplicationException(
              "Die Lehrgänge haben keine Teilnehmer!");
        }
      }
      else
      {
        throw new ApplicationException("Keine Mitglieder ausgewählt!");
      }

      ForderungDialog dialog = new ForderungDialog(
          AbstractDialog.POSITION_CENTER, mitglieder);
      dialog.open();
    }
    catch (OperationCanceledException | ApplicationException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Datenbank Zugriff!";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
