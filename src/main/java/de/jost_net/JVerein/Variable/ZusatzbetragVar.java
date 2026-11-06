/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.Variable;

public enum ZusatzbetragVar
{
  ERSTE_FAELLIGKEIT("zusatzbetrag_erste_faelligkeit"),
  NAECHSTE_FAELLIGKEIT("zusatzbetrag_naechste_faelligkeit"),
  NICHT_MEHR_AUSFUEHREN("zusatzbetrag_nicht_mehr_ausfuehren"),
  INTERVALL("zusatzbetrag_intervall"),
  BETRAG("zusatzbetrag_betrag");

  private String name;

  ZusatzbetragVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
