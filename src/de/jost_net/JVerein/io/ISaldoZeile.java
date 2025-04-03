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

import de.willuhn.datasource.GenericObject;

public interface ISaldoZeile extends GenericObject
{
  public static final int UNDEFINED = 0;

  public static final int HEADER = 1;

  public static final int DETAIL = 2;

  public static final int SALDOFOOTER = 3;

  public static final int SALDOGEWINNVERLUST = 4;

  public static final int GESAMTSALDOFOOTER = 5;

  public static final int GESAMTGEWINNVERLUST = 6;

  public static final int STEUERHEADER = 7;

  public static final int STEUER = 8;

  public static final int NICHTZUGEORDNETEBUCHUNGEN = 9;

  public int getStatus();

  public String getMessage();

}
