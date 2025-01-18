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
import java.util.Arrays;
import java.util.HashMap;

import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class SyntaxExportAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof Buchung)
    {
      context = new Buchung[] { (Buchung) context };
    }
    if (!(context instanceof Buchung[]))
    {
      throw new ApplicationException("Ungültiger context");
    }
    Buchung[] buchungen = (Buchung[]) context;
    Arrays.sort(buchungen, (b, b2) -> {
      try
      {
        return b.getID().compareTo(b2.getID());
      }
      catch (RemoteException e)
      {
        return 0;
      }
    });

    ArrayList<HashMap<String, Object>> buchunglist = new ArrayList<>();
    for (Buchung u : buchungen)
    {
      HashMap<String, Object> buchungMap = new HashMap<>();

      try
      {
        // Wenn keine Buchungsart vorhanden ist können wir auch nicht
        // exportieren
        // Split-Gegenbuchungen kennt Syntax nicht
        if (u.getBuchungsart() == null
            || (u.getSplitTyp() != null
                && u.getSplitTyp() == SplitbuchungTyp.GEGEN))
        {
          continue;
        }
        buchungMap.put("buchungsartkonto", u.getBuchungsart().getNummer());
        buchungMap.put("kommentar", u.getKommentar());
        buchungMap.put("zweck", u.getZweck() + ", " + u.getName());
        buchungMap.put("betrag", u.getBetrag());
        buchungMap.put("datum", u.getDatum());
        buchungMap.put("id", u.getID());
        buchungMap.put("splitid", u.getSplitId());
        if (u.getKonto().getKommentar().trim().matches("^[0-9]{3,10}$"))
        {
          buchungMap.put("gegenkonto", u.getKonto().getKommentar());
        }
        else
        {
          buchungMap.put("gegenkonto", u.getKonto().getNummer());
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Lesen der Buchung.");
      }
      buchunglist.add(buchungMap);
    }

    QueryMessage msg = new QueryMessage(buchunglist);
    Application.getMessagingFactory().getMessagingQueue("syntax.buchung.import")
        .sendSyncMessage(msg);
  }

}
