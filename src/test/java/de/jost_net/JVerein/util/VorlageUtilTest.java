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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import java.rmi.RemoteException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.VorlageTyp;

public class VorlageUtilTest
{

  @Test
  @DisplayName("VorlegenTyp default prüfen")
  void vorlagenDefaultTextTest() throws RemoteException
  {
    try (MockedStatic<Einstellungen> einstellungen = Mockito
        .mockStatic(Einstellungen.class))
    {
      einstellungen.when(() -> Einstellungen.isTest()).thenReturn(true);
      einstellungen
          .when(() -> Einstellungen
              .getEinstellung(argThat(p -> p.getType().equals(String.class))))
          .thenReturn("DE");
      einstellungen
          .when(() -> Einstellungen
              .getEinstellung(argThat(p -> p.getType().equals(Boolean.class))))
          .thenReturn(true);
      einstellungen
          .when(() -> Einstellungen
              .getEinstellung(argThat(p -> p.getType().equals(Integer.class))))
          .thenReturn(1);

      for (VorlageTyp vorlage : VorlageTyp.values())
      {

        String text = VorlageUtil.translate(VorlageUtil.getDummyMap(vorlage),
            vorlage.getDefault(), false);

        assertTrue(!text.contains("$"), vorlage.getKey()
            + " enthält nicht ersetzte Variablen (" + text + ")");
      }
    }
  }
}
