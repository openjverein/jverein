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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class BuchungGeprueftAction implements Action
{
  private boolean geprueft;

  public BuchungGeprueftAction(boolean geprueft)
  {
    this.geprueft = geprueft;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Buchung[] buchungen = null;
    if (context instanceof Buchung)
    {
      buchungen = new Buchung[1];
      buchungen[0] = (Buchung) context;
    }
    else if (context instanceof Buchung[])
    {
      buchungen = (Buchung[]) context;
    }
    if (buchungen == null)
    {
      return;
    }
    if (buchungen.length == 0)
    {
      return;
    }

    try
    {
      boolean sync = (Boolean) Einstellungen
          .getEinstellung(Property.GEPRUEFTSYNCHRONISIEREN);
      for (Buchung b : buchungen)
      {
        b.setGeprueft(geprueft);
        b.store(false);

        // ggfs. mit Hibiscus syncronisieren
        // wir verwenden hier die SynTAX-MessageQueue, da diese bereits
        // existiert und somit keine Änderung an Hibiscus nötig ist
        if (sync && b.getUmsatzid() != null)
        {
          String hid = b.getUmsatzid().toString();
          if (hid != null && hid.length() > 0)
            Application.getMessagingFactory()
                .getMessagingQueue("syntax.buchung.markchecked")
                .sendMessage(new QueryMessage(Boolean.toString(geprueft), hid));
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
  }
}
