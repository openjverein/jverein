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

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TableColumnReplacerTest
{

  private TableColumnReplacer tcr;

  private String testColumn;

  private String testReplaceColumn;

  @BeforeEach
  void setUp()
  {
    tcr = new TableColumnReplacer();
    testColumn = "TestColumn1";
    testReplaceColumn = "ABC";
  }

  @Test
  void addColumnNecessary()
  {

    tcr.addColumn(testColumn, true);
    tcr.setColumnReplacement(testColumn, testReplaceColumn);
    assertEquals(true, tcr.getNecessaryColumns().containsKey(testColumn));
  }

  @Test
  void addColumnOptional()
  {

    tcr.addColumn(testColumn, false);
    tcr.setColumnReplacement(testColumn, testReplaceColumn);
    assertEquals(true, tcr.getOptionalColumns().containsKey(testColumn));
  }

  @Test
  void removeColumnNecessary()
  {

    tcr.addColumn(testColumn, true);
    tcr.setColumnReplacement(testColumn, testReplaceColumn);
    assertEquals(true, tcr.getNecessaryColumns().containsKey(testColumn));
    tcr.removeColumn(testColumn);
    assertEquals(false, tcr.getNecessaryColumns()
        .containsKey(testColumn));
  }

  @Test
  void removeColumnOptional()
  {

    tcr.addColumn(testColumn, false);
    tcr.setColumnReplacement(testColumn, testReplaceColumn);
    assertEquals(true, tcr.getOptionalColumns().containsKey(testColumn));
    tcr.removeColumn(testColumn);
    assertEquals(false, tcr.getOptionalColumns().containsKey(testColumn));
  }

  @Test
  void allNecessaryColumnsAvailableTrue()
  {

    tcr.addColumn(testColumn, true);
    tcr.setColumnReplacement(testColumn, testReplaceColumn);
    assertEquals(true, tcr.allNecessaryColumnsAvailable());
  }

  @Test
  void allNecessaryColumnsAvailableTrueAfterRemove()
  {

    tcr.addColumn(testColumn, true);
    tcr.removeColumn(testColumn);
    assertEquals(true, tcr.allNecessaryColumnsAvailable());
  }

  @Test
  void allNecessaryColumnsAvailableFalse()
  {

    tcr.addColumn(testColumn, true);
    assertEquals(false, tcr.allNecessaryColumnsAvailable());
  }

}
