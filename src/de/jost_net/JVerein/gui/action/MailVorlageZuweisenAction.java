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

import de.jost_net.JVerein.gui.control.MailVorlageControl;
import de.jost_net.JVerein.gui.control.DruckMailControl;
import de.jost_net.JVerein.gui.control.MailControl;
import de.jost_net.JVerein.gui.dialogs.MailVorlagenAuswahlDialog;
import de.jost_net.JVerein.rmi.MailVorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MailVorlageZuweisenAction implements Action
{
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (context != null &&
          (context instanceof DruckMailControl ||
           context instanceof MailControl))
      {
        MailVorlagenAuswahlDialog mvad = new MailVorlagenAuswahlDialog(
            new MailVorlageControl(null),
            MailVorlagenAuswahlDialog.POSITION_CENTER, false);

        MailVorlage mv = mvad.open();
        if (!mvad.getAbort() && mv != null)
        {
          if (context instanceof DruckMailControl)
          {
            DruckMailControl kto = (DruckMailControl) context;
            kto.getBetreff().setValue(mv.getBetreff());
            kto.getTxt().setValue(mv.getTxt());
          }
          else if (context instanceof MailControl)
          {
            MailControl kto = (MailControl) context;
            kto.getBetreff().setValue(mv.getBetreff());
            kto.getTxt().setValue(mv.getTxt());
          }
        }
      }
      else
      {
        throw new ApplicationException("Keine Kontext ausgewählt");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText("Fehler bei der Zuweisung eine Mail Vorlage");
    }
  }
}
