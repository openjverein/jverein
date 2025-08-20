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

import de.willuhn.datasource.rmi.DBObject;

import java.rmi.RemoteException;

public interface WirtschaftsplanItem extends DBObject
{
  void setId(String id) throws RemoteException;

  Wirtschaftsplan getWirtschaftsplan() throws RemoteException;

  void setWirtschaftsplanId(String wirtschaftsplanId) throws RemoteException;

  Buchungsart getBuchungsart() throws RemoteException;

  void setBuchungsartId(String buchungsartId) throws RemoteException;

  Buchungsklasse getBuchungsklasse() throws RemoteException;

  void setBuchungsklasseId(String buchungsklasseId) throws RemoteException;

  String getPosten() throws RemoteException;

  void setPosten(String posten) throws RemoteException;

  double getSoll() throws RemoteException;

  void setSoll(double soll) throws RemoteException;
}
