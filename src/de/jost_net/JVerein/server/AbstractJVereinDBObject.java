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

package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Code fuer alle DB-Klassen in JVerein.
 */
public abstract class AbstractJVereinDBObject extends AbstractDBObject
    implements JVereinDBObject
{

  private static final long serialVersionUID = 1L;

  // Speichert ob Löschen ohne Delete Check gemacht wird
  protected boolean forcedDelete = false;

  public AbstractJVereinDBObject() throws RemoteException
  {
    super();
  }

  // Wir müssen das überschreiben, da hasChanged die id nach int casted.
  // Da wir in JVerein jedoch Long ids haben, wird es als Änderung gewerted.
  // Hier speichern wir die id immer als Long.
  @Override
  public Object setAttribute(String fieldName, Object value)
      throws RemoteException
  {
    if (fieldName == null)
      return null;
    if (value instanceof AbstractDBObject)
    {
      value = Long.parseLong(((AbstractDBObject) value).getID());
    }
    return super.setAttribute(fieldName, value);
  }

  @Override
  public boolean isChanged() throws RemoteException
  {
    return hasChanged();
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    if (isNewObject())
    {
      super.store();
      // Das ist hier nötig, da AbstractDBObject blöderweise die Properties beim
      // Insert nicht in der Map speichert und somit keine Änderungsüberwachnung
      // möglich ist. Wir laden das Object einfach neu aus der DB dabei wird die
      // Map gefüllt.
      load(getID());
    }
    else
    {
      super.store();
    }
  }

  // Löschen ohne Delete Check oder eingeschränktem Check
  @Override
  public void deleteForced() throws RemoteException, ApplicationException
  {
    this.forcedDelete = true;
    super.delete();
  }
}
