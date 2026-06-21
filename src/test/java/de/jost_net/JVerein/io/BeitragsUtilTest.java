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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.util.ApplicationException;

public class BeitragsUtilTest
{

  @Test
  @DisplayName("Ein- und Austrittsdatum des Mitglieds testen")
  void mitgliedAktivTest() throws RemoteException, ApplicationException
  {
    try (MockedStatic<Einstellungen> einstellungen = Mockito
        .mockStatic(Einstellungen.class))
    {
      einstellungen
          .when(
              () -> Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
          .thenReturn(true);

      Beitragsgruppe bg = mock(Beitragsgruppe.class);
      doReturn(10d).when(bg).getBetrag();

      Mitglied m = mock(Mitglied.class);

      Calendar calendar = Calendar.getInstance();
      calendar.set(2026, 01, 15);
      Date stichtag = calendar.getTime();

      calendar.set(2026, 02, 01);
      doReturn(calendar.getTime()).when(m).getEintritt();
      assertEquals(0d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, stichtag, m));

      calendar.set(2026, 01, 01);
      doReturn(calendar.getTime()).when(m).getEintritt();
      assertEquals(10d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, stichtag, m));

      calendar.set(2026, 02, 01);
      doReturn(calendar.getTime()).when(m).getAustritt();
      assertEquals(10d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, stichtag, m));

      calendar.set(2026, 01, 01);
      doReturn(calendar.getTime()).when(m).getAustritt();
      assertEquals(0d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, stichtag, m));
    }
  }

  @Test
  @DisplayName("Beitragshöhe testen")
  void testBeitrag() throws ApplicationException, RemoteException
  {
    try (MockedStatic<Einstellungen> einstellungen = Mockito
        .mockStatic(Einstellungen.class))
    {
      einstellungen
          .when(
              () -> Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
          .thenReturn(true);

      Mitglied m = mock(Mitglied.class);
      doReturn(new Date()).when(m).getEintritt();

      Beitragsgruppe bg = mock(Beitragsgruppe.class);

      doReturn(10d).when(bg).getBetrag();
      doReturn(15d).when(bg).getBetragMonatlich();
      doReturn(42d).when(bg).getBetragVierteljaehrlich();
      doReturn(85d).when(bg).getBetragHalbjaehrlich();
      doReturn(167d).when(bg).getBetragJaehrlich();

      assertEquals(10d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, new Date(), m));

      assertEquals(10d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.MONATLICH), bg, new Date(),
              m));

      assertEquals(30d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.VIERTELJAEHRLICH), bg,
              new Date(), m));

      assertEquals(60d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.HALBJAEHRLICH), bg,
              new Date(), m));

      assertEquals(120d,
          BeitragsUtil.getBeitrag(Beitragsmodel.MONATLICH12631, null,
              new Zahlungsrhythmus(Zahlungsrhythmus.JAEHRLICH), bg, new Date(),
              m));

      assertEquals(15d, BeitragsUtil.getBeitrag(Beitragsmodel.FLEXIBEL,
          Zahlungstermin.MONATLICH, null, bg, new Date(), m));

      assertEquals(42d, BeitragsUtil.getBeitrag(Beitragsmodel.FLEXIBEL,
          Zahlungstermin.VIERTELJAEHRLICH1, null, bg, new Date(), m));

      assertEquals(85d, BeitragsUtil.getBeitrag(Beitragsmodel.FLEXIBEL,
          Zahlungstermin.HALBJAEHRLICH1, null, bg, new Date(), m));

      assertEquals(167d, BeitragsUtil.getBeitrag(Beitragsmodel.FLEXIBEL,
          Zahlungstermin.JAERHLICH01, null, bg, new Date(), m));
    }
  }

  @Test
  @DisplayName("Altersstaffel testen")
  void alterstaffelTest() throws RemoteException, ApplicationException
  {
    try (MockedStatic<Einstellungen> einstellungen = Mockito
        .mockStatic(Einstellungen.class))
    {
      einstellungen
          .when(
              () -> Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
          .thenReturn(true);

      einstellungen
          .when(
              () -> Einstellungen.getEinstellung(Property.BEITRAGALTERSSTUFEN))
          .thenReturn("0-6,7-18,19-99");

      Beitragsgruppe bg = mock(Beitragsgruppe.class);
      doReturn(true).when(bg).getHasAltersstaffel();

      Mitglied m = mock(Mitglied.class);
      doReturn(new Date()).when(m).getEintritt();

      Altersstaffel altersstaffel1 = mock(Altersstaffel.class);
      doReturn(5d).when(altersstaffel1).getBetrag();

      Altersstaffel altersstaffel2 = mock(Altersstaffel.class);
      doReturn(7d).when(altersstaffel2).getBetrag();

      Altersstaffel altersstaffel3 = mock(Altersstaffel.class);
      doReturn(9d).when(altersstaffel3).getBetrag();

      doReturn(altersstaffel1).when(bg).getAltersstaffel(0);
      doReturn(altersstaffel2).when(bg).getAltersstaffel(1);
      doReturn(altersstaffel3).when(bg).getAltersstaffel(2);

      doReturn(null).when(m).getAlter();
      assertThrows(ApplicationException.class,
          () -> BeitragsUtil.getBeitrag(Beitragsmodel.GLEICHERTERMINFUERALLE,
              null, null, bg, new Date(), m));

      doReturn(6).when(m).getAlter();
      assertEquals(5d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, new Date(), m));

      doReturn(7).when(m).getAlter();
      assertEquals(7d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, new Date(), m));

      doReturn(20).when(m).getAlter();
      assertEquals(9d, BeitragsUtil.getBeitrag(
          Beitragsmodel.GLEICHERTERMINFUERALLE, null, null, bg, new Date(), m));

      doReturn(110).when(m).getAlter();
      assertThrows(ApplicationException.class,
          () -> BeitragsUtil.getBeitrag(Beitragsmodel.GLEICHERTERMINFUERALLE,
              null, null, bg, new Date(), m));
    }
  }
}
