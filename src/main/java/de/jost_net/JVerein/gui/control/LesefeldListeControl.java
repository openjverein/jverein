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
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.LesefeldMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LesefeldDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.logging.Logger;

public class LesefeldListeControl extends FilterControl
{

  private AbstractInput suchMitglied;

  private JVereinTablePart lesefeldList;

  private Mitglied selectedMitglied = null;

  private final LesefeldAuswerter lesefeldAuswerter = new LesefeldAuswerter();

  public LesefeldListeControl(AbstractView view)
  {
    super(view, LesefeldControl.class);
    String m = settings.getString("mitglied", "");
    if (m.length() > 0)
    {
      // Das Mitglied wird aus den Settings genommen.
      // Dadurch können wir das Mitglied beim durchblättern übergeben.
      try
      {
        selectedMitglied = (Mitglied) Einstellungen.getDBService()
            .createObject(Mitglied.class, m);
      }
      catch (RemoteException e)
      {
        // Nichts tun
      }
    }
    if (selectedMitglied == null)
    {
      try
      {
        // Falls kein Mitglied in den Settings gefunden wurde,
        // dann nehmen wir das erste Mitglied in der Liste.
        DBIterator<Mitglied> it = Einstellungen.getDBService()
            .createList(Mitglied.class);
        it.setOrder("order by name, vorname");
        if (it.hasNext())
        {
          selectedMitglied = it.next();
          settings.setAttribute("mitglied", selectedMitglied.getID());
        }
      }
      catch (RemoteException ex)
      {
        String fehler = "Fehler beim Auswählen des Mitgliedes";
        Logger.error(fehler, ex);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }

  /**
   * 
   * @throws RemoteException
   * @return Das Mitglied Feld welches im LesefeldListeView für die Anzeige der
   *         Lesefelder verwendet wird.
   * 
   */
  public Input getSuchMitglied() throws RemoteException
  {
    if (suchMitglied != null)
    {
      return suchMitglied;
    }
    suchMitglied = new MitgliedInput().getMitgliedInput(suchMitglied,
        selectedMitglied,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    suchMitglied.addListener(new SuchMitgliedListener());
    // In LesefelderListeView alle Lesefelder aus der DB lesen und evaluieren
    // Dadurch kann die evaluierte Ausgabe in der Liste angezeigt werden
    lesefeldAuswerter.setLesefelderDefinitionsFromDatabase();
    if (selectedMitglied != null)
    {
      lesefeldAuswerter
          .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
      // Mitglied in Settings speichern damit es beim Editieren oder der Neu
      // Action im LesefeldDetailView erscheint.
      settings.setAttribute("mitglied", selectedMitglied.getID());
    }
    lesefeldAuswerter.evalAlleLesefelder();
    return suchMitglied;
  }

  /**
   * Listener, der Änderungen bei suchMitglied überwacht.
   * 
   */
  private class SuchMitgliedListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Mitglied selected = (Mitglied) getSuchMitglied().getValue();
        if (selected == null || selected == selectedMitglied)
          return;
        selectedMitglied = selected;
        // Wenn sich das Mitglied im LesefelderListeView ändert, dann alle
        // Lesefelder neu evaluieren, geladen sind sie ja schon
        lesefeldAuswerter
            .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
        lesefeldAuswerter.evalAlleLesefelder();
        settings.setAttribute("mitglied", selectedMitglied.getID());
        TabRefresh();
      }
      catch (RemoteException e)
      {
        String fehler = "Fehler beim Auswählen des Mitgliedes";
        Logger.error(fehler, e);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }

  /**
   * @throws RemoteException
   * @return Die Lesefelder Tabelle für den LesefeldListeView.
   * 
   */
  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (lesefeldList != null)
    {
      return lesefeldList;
    }
    // Wir holen die Lesefelder vom lesefeldAuswerter weil bei denen auch die
    // evaluierte Ausgabe gesetzt ist
    lesefeldList = new JVereinTablePart(lesefeldAuswerter.getLesefelder(),
        null);
    lesefeldList.addColumn("Skript-Name", "bezeichnung");
    lesefeldList.addColumn("Erste Zeile der Script-Ausgabe", "ausgabe");
    lesefeldList.setContextMenu(new LesefeldMenu(lesefeldList));
    lesefeldList.setMulti(true);
    lesefeldList
        .setAction(new EditAction(LesefeldDetailView.class, lesefeldList));
    VorZurueckControl.setObjektListe(null, null);
    return lesefeldList;
  }

  /**
   * Baut die LesefelderList neu auf.
   * 
   */
  protected void TabRefresh()
  {
    if (lesefeldList == null)
    {
      return;
    }
    lesefeldList.removeAll();
    try
    {
      List<Lesefeld> lesefelder = lesefeldAuswerter.getLesefelder();
      for (Lesefeld l : lesefelder)
      {
        lesefeldList.addItem(l);
      }
      lesefeldList.sort();
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Refresh der Tabelle", ex);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LESEFELDER_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LESEFELDER_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LESEFELDER_DATEINAME);
  }
}
