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
package de.jost_net.JVerein.gui.control.listener;

/**
 * Der Listener wird aufgerufen, wenn eine Tree-Spalte/-Zelle bearbeitet werden
 * soll. Wenn der Listener <code>false</code> zurück gibt, wird das Bearbeiten
 * abgebrochen.
 */
public interface TreeEditListener
{

  /**
   * Wird aufgerufen, wenn der Wert eines Feldes geaendert wurde.
   * 
   * @param object
   *          das zugehoerige Fachobjekt.
   * @param attribute
   *          der Name des geaenderten Attributes.
   */
  public boolean editItem(Object object, String attribute);

}
