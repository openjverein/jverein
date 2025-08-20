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

import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;

/**
 * Loeschen einer Zusatzbetragsvorlage.
 */
public class ZusatzbetragVorlageDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    ZusatzbetragVorlage[] z = null;
    if (context != null && (context instanceof ZusatzbetragVorlage || context instanceof ZusatzbetragVorlage[]))
    {
      if (context instanceof ZusatzbetragVorlage)
      {
        z = new ZusatzbetragVorlage[] { (ZusatzbetragVorlage) context };
      }
      else if (context instanceof ZusatzbetragVorlage[])
      {
        z = (ZusatzbetragVorlage[]) context;
      }
    }
    if (z == null)
    {
      throw new ApplicationException("Keine Zusatzbetrag-Vorlage ausgew�hlt");
    }

    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle("Zusatzbetrag-Vorlage l�schen");
    d.setText(String.format("Wollen Sie %d %s wirklich l�schen?", z.length,
        (z.length == 1 ? "Zusatzbetrag-Vorlage" : "Zusatzbetrag-Vorlagen")));
    Boolean choice;
    try
    {
      choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
    }
    catch (Exception e1)
    {
      Logger.error("Fehler", e1);
    }

    try
    {
      for (ZusatzbetragVorlage z1 : z)
      {
        if (z1.isNewObject())
        {
          continue;
        }
        z1.delete();
      }
      GUI.getStatusBar().setSuccessText("gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen von Zusatzbetrags-Vorlagen";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
