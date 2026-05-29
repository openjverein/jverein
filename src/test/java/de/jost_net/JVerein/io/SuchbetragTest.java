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
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.jost_net.JVerein.io.Suchbetrag.Suchstrategie;

class SuchbetragTest
{
  @Test
  void test01() throws Exception
  {
    Suchbetrag sb = new Suchbetrag(null);
    assertEquals(Suchstrategie.KEINE, sb.getSuchstrategie());
  }

  @Test
  void test02() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.GLEICH, sb.getSuchstrategie());
  }

  @Test
  void test02m() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("-1,23");
    assertEquals(BigDecimal.valueOf(-1.23), sb.getBetrag());
    assertEquals(Suchstrategie.GLEICH, sb.getSuchstrategie());
  }

  @Test
  void test03() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("=1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.GLEICH, sb.getSuchstrategie());
  }

  @Test
  void test04() throws Exception
  {
    Suchbetrag sb = new Suchbetrag(">1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.GRÖSSER, sb.getSuchstrategie());
  }

  @Test
  void test05() throws Exception
  {
    Suchbetrag sb = new Suchbetrag(">=1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.GRÖSSERGLEICH, sb.getSuchstrategie());
  }

  @Test
  void test06() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("<1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.KLEINER, sb.getSuchstrategie());
  }

  @Test
  void test07() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("<=1,23");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(Suchstrategie.KLEINERGLEICH, sb.getSuchstrategie());
  }

  @Test
  void test08() throws Exception
  {
    Suchbetrag sb = new Suchbetrag("1,23..2,34");
    assertEquals(BigDecimal.valueOf(1.23), sb.getBetrag());
    assertEquals(BigDecimal.valueOf(2.34), sb.getBetrag2());
    assertEquals(Suchstrategie.BEREICH, sb.getSuchstrategie());
  }

  @Test
  void test09() throws Exception
  {
    try
    {
      new Suchbetrag("1,23...2,34");
      fail("Hier sollte eine Exception aufgetreten sein");
    }
    catch (Exception e)
    {
      assertEquals("Wert ungültig", e.getMessage());
    }
  }

  @Test
  void test10() throws Exception
  {
    try
    {
      new Suchbetrag("=>1,23");
      fail("Hier sollte eine Exception aufgetreten sein");
    }
    catch (Exception e)
    {
      assertEquals("Wert ungültig", e.getMessage());
    }
  }

  @Test
  void test11() throws Exception
  {
    try
    {
      new Suchbetrag("1.23");
      fail("Hier sollte eine Exception aufgetreten sein");
    }
    catch (Exception e)
    {
      assertEquals("Wert ungültig", e.getMessage());
    }
  }

  @Test
  void test12() throws Exception
  {
    try
    {
      new Suchbetrag("X1,23");
      fail("Hier sollte eine Exception aufgetreten sein");
    }
    catch (Exception e)
    {
      assertEquals("Wert ungültig", e.getMessage());
    }
  }
}
