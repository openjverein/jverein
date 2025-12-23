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
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;

public class FreiesFormularAusgabe extends AbstractAusgabe
{

  private Formular formular;

  public FreiesFormularAusgabe(Formular formular)
  {
    this.formular = formular;
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Mitglied m = (Mitglied) object;
    String filename = object.getID() + "#freiesformular# #";
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename + "#" + formular.getBezeichnung();
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Map<String, Object> map = new MitgliedMap().getMap((Mitglied) object, null);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      return VorlageUtil.getName(VorlageTyp.FREIES_FORMULAR_MITGLIED_DATEINAME,
          formular.getBezeichnung(), (Mitglied) object);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.FREIES_FORMULAR_DATEINAME,
          formular.getBezeichnung());
    }
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    return formular;
  }
}
