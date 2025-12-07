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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CheckerTest
{
  @Test
  void test01()
  {
    assertTrue(EmailValidator.isValid("willi.wichtig@jverein.de"));
  }

  @Test
  void test02()
  {
    assertFalse(EmailValidator.isValid(null));
  }

  @Test
  void test05()
  {
    assertFalse(EmailValidator.isValid("willi wichtig@jverein.de"));
  }

  @Test
  void test06()
  {
    assertFalse(EmailValidator.isValid("willi@wichtig@jverein.de"));
  }

  @Test
  void test07()
  {
    assertFalse(EmailValidator.isValid("willi.wichtig.jverein.de"));
  }

  @Test
  void test08()
  {
    assertFalse(EmailValidator.isValid("willi.wichtig@jvereinde"));
  }

  @Test
  void test09()
  {
    assertFalse(EmailValidator.isValid("willi.wichtig.@jverein.de"));
  }

  @Test
  void test10()
  {
    assertTrue(EmailValidator.isValid("jupp.schmitz@köln.de"));
  }

  @Test
  void test11()
  {
    assertTrue(EmailValidator.isValid("name@internetsite.shop"));
  }

  @Test
  void test12()
  {
    assertTrue(EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse@example-zwei.tld;"));
  }

  @Test
  void test13()
  {
    assertFalse(EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse@example-zweitld;"));
  }

  @Test
  void test14()
  {
    assertFalse(EmailValidator.isValid(
        "Gruppenname: erste.adresse@example-eins.tld, zweite.adresse;"));
  }

  @Test
  void test15()
  {
    assertFalse(EmailValidator.isValid("willi.wichtig"));
  }

}
