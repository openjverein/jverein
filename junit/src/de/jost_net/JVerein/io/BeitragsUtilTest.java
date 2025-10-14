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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.util.ApplicationException;

@Disabled
public class BeitragsUtilTest
{
  public BeitragsUtilTest() throws RemoteException
  {
    super();
  }

  @Test
  void test01() throws ApplicationException
  {
    try
    {
      Beitragsgruppe bg = getBeitragsgruppe();
      assertEquals(10d,
          BeitragsUtil.getBeitrag(Beitragsmodel.GLEICHERTERMINFUERALLE, null,
              null, bg, new Date(), null));
      assertEquals(10d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.MONATLICH), bg, new Date(),
              null));
      assertEquals(30d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.VIERTELJAEHRLICH), bg,
              new Date(), null));
      assertEquals(60d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.HALBJAEHRLICH), bg,
              new Date(), null));
      assertEquals(120d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.JAEHRLICH), bg, new Date(),
              null));
      assertEquals(20d, BeitragsUtil.getBeitrag(Beitragsmodel.FLEXIBEL,
          Zahlungstermin.MONATLICH, null, bg, new Date(), null));

    }
    catch (RemoteException e)
    {
      e.printStackTrace();
    }
  }

  private Beitragsgruppe getBeitragsgruppe() throws RemoteException
  {
    Beitragsgruppe bg = (Beitragsgruppe) Einstellungen.getDBService()
        .createObject(Beitragsgruppe.class, null);
    bg.setBezeichnung("Test");
    bg.setBetrag(10d);
    bg.setBetragMonatlich(20d);
    bg.setBetragVierteljaehrlich(60d);
    bg.setBetragHalbjaehrlich(120d);
    bg.setBetragJaehrlich(240d);
    return bg;
  }
}
