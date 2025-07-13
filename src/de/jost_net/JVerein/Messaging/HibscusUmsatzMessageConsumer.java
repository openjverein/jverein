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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;

/**
 * Wird benachrichtigt, wenn eine Buchung aus Hibiscus als geprueft/ungeprueft
 * markiert wurde. Insofern wir eine Buchung haben, die aus diesem Umsatz
 * erzeugt wurde, uebernehmen wir den Status dann auch gleich in JVerein.
 */
public class HibscusUmsatzMessageConsumer implements MessageConsumer
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[] { QueryMessage.class };
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (!(Boolean) Einstellungen.getEinstellung(Property.GEPRUEFTSYNCHRONISIEREN))
      return;

    final QueryMessage m = (QueryMessage) message;
    final String name = m.getName();
    final Object data = m.getData();

    if (name == null || data == null)
      return;

    final boolean state = Boolean.valueOf(name);

    // Wir muessen hier nichtmal auf Umsatz casten - uns genuegt die ID des
    // Datensatzes
    if (!(data instanceof GenericObject))
      return;

    final GenericObject o = (GenericObject) data;
    final String id = o.getID();
    if (id == null || id.length() == 0)
      return;

    DBIterator<Buchung> list = Einstellungen.getDBService()
        .createList(Buchung.class);
    list.addFilter("umsatzid = ?", id);
    if (!list.hasNext())
      return;

    Buchung b = list.next();
    b.setGeprueft(state);
    b.store(false);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    // Per plugin.xml registriert.
    return false;
  }

}

