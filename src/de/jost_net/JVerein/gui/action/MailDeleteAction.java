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

import de.jost_net.JVerein.rmi.Mail;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Mail
 */
public class MailDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Mail ausgewählt");
    }
    Mail[] mails = null;
    if (context instanceof Mail)
    {
      mails = new Mail[] { (Mail) context };
    }
    else if (context instanceof Mail[])
    {
      mails = (Mail[]) context;
    }
    else
    {
      return;
    }
    String mehrzahl = mails.length > 1 ? "s" : "";
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle("Mail" + mehrzahl + " löschen");
    d.setText("Wollen Sie diese Mail" + mehrzahl + " wirklich löschen?");

    try
    {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
      for (Mail m : mails)
      {
        if (m.isNewObject())
        {
          continue;
        }
        m.delete();
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Löschen der Mail", e);
      return;
    }
    GUI.getStatusBar().setSuccessText("Mail gelöscht.");
  }
}
