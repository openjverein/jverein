/**********************************************************************
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
package de.jost_net.JVerein.io;

import java.rmi.RemoteException;
import java.util.Map;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.BuchungMap;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;

public class BuchungReportAusgabe extends AbstractAusgabe
{

  private Formular formular;

  public BuchungReportAusgabe(Formular formular)
  {
    this.formular = formular;
  }

  // Mailausgabe wird nicht unterstützt
  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    return null;
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Map<String, Object> map = new BuchungMap().getMap((Buchung) object, null);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSREPORT_DATEINAME, object,
        formular.getBezeichnung());
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    return formular;
  }
}
