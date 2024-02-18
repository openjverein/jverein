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
import java.util.ArrayList;

import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SplitbuchungBulkAufloesenAction implements Action
{
  private ArrayList<Long> geloescht = new ArrayList<>();
  private Long splitid;
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Buchung) && !(context instanceof Buchung[])))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      else if (context instanceof Buchung[])
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
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Splitbuchung" + (b.length > 1 ? "en" : "") + " auflösen");
      d.setText("Wollen Sie diese Splituchung" + (b.length > 1 ? "en" : "")
          + " wirklich auflösen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Auflösen der Splituchung", e);
        return;
      }
      for (Buchung bu : b)
      {
        Jahresabschluss ja = bu.getJahresabschluss();
        if (ja != null)
        {
          throw new ApplicationException(String.format(
              "Buchung wurde bereits am %s von %s abgeschlossen.",
              new JVDateFormatTTMMJJJJ().format(ja.getDatum()), ja.getName()));
        }
        splitid = bu.getSplitId();
        if (!geloescht.contains(splitid))
        {
          SplitbuchungsContainer.init(bu);
          SplitbuchungsContainer.aufloesen();
          geloescht.add(splitid);
        }
      }
      int count = geloescht.size();
      if (count > 0)
      {
        GUI.getStatusBar().setSuccessText(String.format(
            "%d Splituchung" + (count != 1 ? "en" : "") + " aufgelöst.", count));
      }
      else
      {
        GUI.getStatusBar().setErrorText("Keine Splituchung aufgelöst");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Auflösen einer Splituchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
    finally
    {
      Application.getMessagingFactory().sendMessage(new BuchungMessage(null));
    }
  }
}
