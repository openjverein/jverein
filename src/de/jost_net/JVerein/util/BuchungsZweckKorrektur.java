/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.util;

import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import org.apache.commons.lang.StringUtils;

import java.rmi.RemoteException;

public class BuchungsZweckKorrektur
{

  public static String getBuchungsZweckKorrektur(String value,
      boolean withRealLineBreak)
  {
    if (value == null)
    {
      return null;
    }
    try
    {
      Transfer t = new Verwendungszweck(value);
      String s = StringUtils.trimToNull(
          VerwendungszweckUtil.getTag(t, Tag.SVWZ));
      if (!withRealLineBreak)
      {
        s = s.replaceAll("\r\n", "|");
        s = s.replaceAll("\r", "|");
        s = s.replaceAll("\n", "|");
      }
      return s;
    }
    catch (RemoteException ex)
    {
      return null;
    }
  }

  // Dummy Klasse um die Methode "VerwendungszweckUtil.getTag(t, Tag.SVWZ)"
  // verwenden zu können
  private static final class Verwendungszweck implements Transfer
  {
    String zweck;

    public Verwendungszweck(String zweck)
    {
      this.zweck = zweck;
    }

    @Override
    public String getGegenkontoNummer() throws RemoteException
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getGegenkontoBLZ() throws RemoteException
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getGegenkontoName() throws RemoteException
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public double getBetrag() throws RemoteException
    {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public String getZweck() throws RemoteException
    {
      // TODO Auto-generated method stub
      return zweck;
    }

    @Override
    public String getZweck2() throws RemoteException
    {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String[] getWeitereVerwendungszwecke() throws RemoteException
    {
      // TODO Auto-generated method stub
      return null;
    }
  }
}

