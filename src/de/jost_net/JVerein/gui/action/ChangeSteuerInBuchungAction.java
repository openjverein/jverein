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

import org.eclipse.swt.SWT;

import de.jost_net.JVerein.Einstellungen;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.logging.Logger;

public class ChangeSteuerInBuchungAction implements Action
{

  @Override
  public void handleAction(Object context)
  {
    CheckboxInput input = (CheckboxInput) context;

    if ((boolean) input.getValue())
    {
      YesNoDialog dialog = new YesNoDialog(SWT.CENTER);
      dialog.setTitle("Migration Steur in Buchung");
      // TODO ist der Text verständlich?
      dialog.setText("Soll die Steuer aus den Buchungsarten in die\n"
          + "Buchungen und Sollbuchungspositionen übernommen werden?\n"
          + "Das wir für alle bisherigen Buchungen und Sollbuchungspositionen gemacht,\n "
          + "so dass die bisherige Steuer erhalten bleibt.");
      try
      {
        if ((boolean) dialog.open())
        {
          try
          {
            String sql = "update buchung join buchungsart on buchungsart.id = buchung.buchungsart "
                + "set buchung.steuer = buchungsart.steuer "
                + "where buchung.steuer is null";

            int anzahlBuchungen = Einstellungen.getDBService()
                .executeUpdate(sql, null);

            sql = "update sollbuchungposition join buchungsart on buchungsart.id = sollbuchungposition.buchungsart "
                + "set sollbuchungposition.steuer = buchungsart.steuer"
                + "where sollbuchungposition.steuer is null";

            int anzahlSollbuchungpositionen = Einstellungen.getDBService()
                .executeUpdate(sql, null);

            GUI.getStatusBar()
                .setSuccessText("Steuer in " + anzahlBuchungen
                    + " Buchungen und " + anzahlSollbuchungpositionen
                    + " Sollbuchungpositionen gespeichert");
          }
          catch (RemoteException re)
          {
            String fehler = "Fehler beim speichern der Steuer in den Buchungen";
            Logger.error(fehler, re);
            GUI.getStatusBar().setErrorText(fehler);
          }
        }
      }
      catch (Exception ex)
      {
        String fehler = "Fehler beim öffnen des Steuer-In-Buchung Dialogs";
        Logger.error(fehler, ex);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }
}
