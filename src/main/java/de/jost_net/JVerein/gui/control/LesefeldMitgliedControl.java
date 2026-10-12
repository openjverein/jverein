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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.LesefeldMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LesefeldDetailView;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.system.Settings;

public class LesefeldMitgliedControl extends AbstractControl
{
  private Settings settings;

  private JVereinTablePart lesefeldMitgliedList;

  private Mitglied selectedMitglied = null;

  private final LesefeldAuswerter lesefeldAuswerter = new LesefeldAuswerter();

  private boolean ersterTabAufruf = true;

  public LesefeldMitgliedControl(AbstractView view)
  {
    super(view);
    settings = new Settings(LesefeldControl.class);
    settings.setStoreWhenRead(true);
  }

  /**
   * 
   * @throws RemoteException
   * @return Die Lesefelder Tabelle für den Tab im MitgliedDetailView.
   * 
   */
  public JVereinTablePart getLesefeldMitgliedList() throws RemoteException
  {
    if (lesefeldMitgliedList != null)
    {
      return lesefeldMitgliedList;
    }
    // Wir holen die Lesefelder vom lesefeldAuswerter weil bei denen auch die
    // evaluierte Ausgabe gesetzt ist
    lesefeldMitgliedList = new JVereinTablePart(
        lesefeldAuswerter.getLesefelder(),
        new EditAction(LesefeldDetailView.class));
    lesefeldMitgliedList.setTableName("Lesefelder");
    lesefeldMitgliedList.addColumn("Skript-Name", "bezeichnung");
    lesefeldMitgliedList.addColumn("Erste Zeile der Script-Ausgabe", "ausgabe");
    lesefeldMitgliedList.setContextMenu(new LesefeldMenu(null));
    lesefeldMitgliedList.setMulti(true);
    return lesefeldMitgliedList;
  }

  /**
   * Initialisiert den Lesefeldauswerter. Es werden die Lesefelder aus der
   * Datenbank gelesen und alle evaluiert. Dann stehen auch die Ausgaben für die
   * Tabelle bereit.
   * 
   * Lesen aus der Datenbank wird nur einmal gemacht.
   * 
   * @param m
   *          Das Mitglied
   * @throws RemoteException
   * 
   */
  public void initLesefeldMitgliedList(Mitglied m) throws RemoteException
  {
    // Bei neuem Mitglied noch nichts anzeigen
    if (m != null && m.getID() != null)
    {
      selectedMitglied = m;
      if (ersterTabAufruf)
      {
        // Im MitgliedDetailView werden alle Lesefelder einmalig aus der DB
        // gelesen. Später können sie mit unterschiedlichen Mitgliedern
        // evaluiert werden
        lesefeldAuswerter.setLesefelderDefinitionsFromDatabase();
      }
      ersterTabAufruf = false;
      lesefeldAuswerter
          .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
      // Mitglied in Settings speichern damit es beim Editieren oder der Neu
      // Action im LesefeldDetailView erscheint.
      settings.setAttribute("mitglied", selectedMitglied.getID());
      lesefeldAuswerter.evalAlleLesefelder();
    }
  }

  /**
   * Werden die Lesefelder bei Mitgliedern im Tab angezeigt, dann werden sie
   * erst gezeichnet wenn der Tab selektiert wird. Vorher wird nur eine leere
   * Liste gezeichnet. Der Update wird immer aufgerufen wenn der Tab selektiert
   * wird. Die Lesefelder müssen aber pro Mitglied nur einmal gefüllt werden.
   * 
   * @param m
   *          Das Mitglied
   * @throws RemoteException
   * 
   */
  public void updateLesefeldMitgliedList(Mitglied m, boolean force)
      throws RemoteException
  {
    // Wenn Lesefelder nicht aktiviert sind gibt es die Liste nicht
    if (lesefeldMitgliedList != null)
    {
      // Bei neuem Mitglied noch nichts anzeigen
      if (m != null && m.getID() != null)
      {
        // Force wird aus dem MitgliedDetailView verwendet wenn ein Mitglied
        // gespeichert wird. Dann muss ein Update gemacht werden weil sich die
        // Mitgliedsdaten geändert haben können.
        if (ersterTabAufruf || force)
        {
          initLesefeldMitgliedList(m);
          lesefeldMitgliedList.removeAll();
          for (Lesefeld lesefeld : lesefeldAuswerter.getLesefelder())
          {
            lesefeldMitgliedList.addItem(lesefeld);
          }
        }
      }
    }
  }

}
