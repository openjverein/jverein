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
package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import javax.mail.internet.AddressException;

import org.junit.jupiter.api.Test;

class CheckerTest
{

  @Test
  void test01()
  {
    assertDoesNotThrow(
        () -> EmailValidator.isValid("willi.wichtig@jverein.de"));
  }

  @Test
  void test02()
  {
    assertThrows(AddressException.class, () -> EmailValidator.isValid(null));
  }

  @Test
  void test05()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi wichtig@jverein.de"));
  }

  @Test
  void test06()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi@wichtig@jverein.de"));
  }

  @Test
  void test07()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi.wichtig.jverein.de"));
  }

  @Test
  void test08()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi.wichtig@jvereinde"));
  }

  @Test
  void test09()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi.wichtig.@jverein.de"));
  }

  @Test
  void test10()
  {
    assertDoesNotThrow(() -> EmailValidator.isValid("jupp.schmitz@köln.de"));
  }

  @Test
  void test11()
  {
    assertDoesNotThrow(() -> EmailValidator.isValid("name@internetsite.shop"));
  }

  @Test
  void test12()
  {
    assertDoesNotThrow(() -> EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse@example-zwei.tld;"));
  }

  @Test
  void test13()
  {
    assertThrows(AddressException.class, () -> EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse@example-zweitld;"));
  }

  @Test
  void test14()
  {
    assertThrows(AddressException.class, () -> EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse;"));
  }

  @Test
  void test15()
  {
    assertThrows(AddressException.class,
        () -> EmailValidator.isValid("willi.wichtig"));
  }

}
