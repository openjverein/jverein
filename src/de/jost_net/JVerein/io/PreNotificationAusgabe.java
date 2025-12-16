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
package de.jost_net.JVerein.io;

import java.rmi.RemoteException;
import java.util.Map;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;

public class PreNotificationAusgabe extends AbstractAusgabe
{
  private Formular formular;

  public PreNotificationAusgabe(Formular formular)
  {
    this.formular = formular;
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Lastschrift ls = (Lastschrift) object;
    String filename = (ls.getMitglied() == null ? "" : ls.getMitglied().getID())
        + "#lastschrift#" + object.getID() + "#";
    String email = StringTool.toNotNullString(ls.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += ls.getName() + ls.getVorname();
    }
    return filename + "#PreNotification";
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Map<String, Object> map = new LastschriftMap().getMap((Lastschrift) object,
        null);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      return VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_MITGLIED_DATEINAME,
          object, ((Lastschrift) object).getMitglied());
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_DATEINAME);
    }
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    return formular;
  }

}
