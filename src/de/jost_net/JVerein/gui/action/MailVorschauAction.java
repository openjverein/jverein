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

import de.jost_net.JVerein.gui.control.MailControl;
import de.jost_net.JVerein.gui.control.MailVorlageControl;
import de.jost_net.JVerein.gui.dialogs.MailEmpfaengerAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.MailVorschauDialog;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;

public class MailVorschauAction implements Action
{
  private final AbstractControl control;

  public MailVorschauAction(AbstractControl control)
  {
    super();
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof MailEmpfaenger && control instanceof MailControl)
    {
      MailEmpfaenger m = (MailEmpfaenger) context;
      try
      {
        new MailVorschauDialog((MailControl) control, m,
            MailEmpfaengerAuswahlDialog.POSITION_CENTER);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Anzeigen der Vorschau");
      }
    }
    else if (context instanceof Mitglied && (control instanceof MailControl || control instanceof MailVorlageControl))
    {
      Mitglied m = (Mitglied) context;
      new MailVorschauDialog(control, m,
          MailEmpfaengerAuswahlDialog.POSITION_CENTER);
    }
    // Falls kein Mitglied übergeben wird, dann wird der Dummy angezeigt
    else if (context == null && control instanceof MailControl)
    {
      new MailVorschauDialog(control, null,
          MailEmpfaengerAuswahlDialog.POSITION_CENTER);
    }
    else
    {
      String name = "";
      if (context != null && context.getClass() != null)
      {
        name = context.getClass().getCanonicalName();
      }
      Logger.error("ShowVariablesDiaglog: Ungültige Klasse: " + name);
    }
  }
}
