/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;
import java.util.Date;

public interface Wirtschaftsplan extends JVereinDBObject
{
  void setId(String id) throws RemoteException;

  String getBezeichung() throws RemoteException;

  void setBezeichnung(String bezeichnung) throws RemoteException;

  Date getDatumVon() throws RemoteException;

  void setDatumVon(Date date) throws RemoteException;

  Date getDatumBis() throws RemoteException;

  void setDatumBis(Date date) throws RemoteException;

  double getIstVerbindlichkeiten() throws RemoteException;

  double getIstRuecklagenAufgeloest() throws RemoteException;

  double getIstAusgabe() throws RemoteException;

  double getIstForderungen() throws RemoteException;

  double getIstRuecklagenGebildet() throws RemoteException;

  double getIstEinnahme() throws RemoteException;

  double getPlanAusgabe() throws RemoteException;

  double getPlanEinnahme() throws RemoteException;

  double getPlanRuecklagenGebildet() throws RemoteException;

  double getPlanRuecklagenAufgeloest() throws RemoteException;
}
