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
package de.jost_net.JVerein.Messaging;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;

public class VopKontoinhaberChangeMessageConsumer implements MessageConsumer
{

  @SuppressWarnings("rawtypes")
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[] { QueryMessage.class };
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    final QueryMessage m = (QueryMessage) message;
    final String iban = m.getName();
    final String newName = (String) m.getData();

    if (iban == null || newName == null)
      return;

    DBIterator<Mitglied> it = Einstellungen.getDBService()
        .createList(Mitglied.class);
    it.addFilter("lower(replace(iban,' ','')) = ?",
        iban.replace(" ", "").toLowerCase());

    while (it.hasNext())
    {
      Mitglied mitglied = it.next();
      mitglied.setKontoinhaber(newName);
      mitglied.store();
    }
  }

  @Override
  public boolean autoRegister()
  {
    // Per plugin.xml registriert.
    return false;
  }

}
