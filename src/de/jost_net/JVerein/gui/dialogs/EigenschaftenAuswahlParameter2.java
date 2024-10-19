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
package de.jost_net.JVerein.gui.dialogs;

import java.util.ArrayList;

import de.jost_net.JVerein.server.EigenschaftenNode2;

public class EigenschaftenAuswahlParameter2
{
  private ArrayList<EigenschaftenNode2> eigenschaften;

  private String verknuepfung;

  public EigenschaftenAuswahlParameter2()
  {
    eigenschaften = new ArrayList<>();
  }

  public void add(EigenschaftenNode2 node)
  {
    eigenschaften.add(node);
  }

  public ArrayList<EigenschaftenNode2> getEigenschaften()
  {
    return eigenschaften;
  }

  public void setVerknuepfung(String verknuepfung)
  {
    this.verknuepfung = verknuepfung;
  }

  public String getVerknuepfung()
  {
    return verknuepfung;
  }
}
