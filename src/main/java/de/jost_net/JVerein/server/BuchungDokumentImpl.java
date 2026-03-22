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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.util.VorlageUtil;

public class BuchungDokumentImpl extends AbstractDokumentImpl
    implements BuchungDokument
{

  private static final long serialVersionUID = 1L;

  public BuchungDokumentImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "buchungdokument";
  }

  @Override
  protected String getVerzeichnis()
  {
    return "buchungen";
  }

  @Override
  protected String getDateiPfad() throws RemoteException
  {
    AbstractJVereinDBObject dbObject = Einstellungen.getDBService()
        .createObject(Buchung.class, getReferenz().toString());
    return VorlageUtil.getName(VorlageTyp.BUCHUNG_DOKUMENT_PFAD, dbObject);
  }

}
