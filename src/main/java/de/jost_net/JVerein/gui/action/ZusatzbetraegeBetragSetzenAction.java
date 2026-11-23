/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.BetragEingabeDialog;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Nächstes Fälligkeitsdatum eines Zusatzbetrages setzen.
 */
public class ZusatzbetraegeBetragSetzenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Zusatzbetrag)
        && !(context instanceof Zusatzbetrag[]))
    {
      throw new ApplicationException("Kein Zusatzbetrag ausgewählt");
    }
    try
    {
      Zusatzbetrag[] zusatzbetraege = null;
      if (context instanceof Zusatzbetrag)
      {
        zusatzbetraege = new Zusatzbetrag[1];
        zusatzbetraege[0] = (Zusatzbetrag) context;
      }
      if (context instanceof Zusatzbetrag[])
      {
        zusatzbetraege = (Zusatzbetrag[]) context;
      }
      if (zusatzbetraege != null && zusatzbetraege.length > 0
          && zusatzbetraege[0].isNewObject())
      {
        return;
      }

      BetragEingabeDialog pad = new BetragEingabeDialog(
          BetragEingabeDialog.POSITION_CENTER, zusatzbetraege[0].getBetrag());

      if (!pad.open())
      {
        int counter = 0;

        for (Zusatzbetrag zusatzbetrag : zusatzbetraege)
        {
          zusatzbetrag.setBetrag(pad.getBetrag());
          zusatzbetrag.store();
          counter++;
        }

        GUI.getStatusBar().setSuccessText(counter + " Beträge geändert");
      }

    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText("Fehler beim setzen der Beträge");
    }
  }
}
