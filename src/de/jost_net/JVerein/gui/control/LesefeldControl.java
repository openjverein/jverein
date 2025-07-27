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

import bsh.EvalError;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.LesefeldMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LesefeldDetailView;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LesefeldControl extends VorZurueckControl implements Savable
{
  private de.willuhn.jameica.system.Settings settings;

  private JVereinTablePart lesefeldList;

  private TablePart lesefeldMitgliedList;

  private Lesefeld lesefeld = null;

  private AbstractInput suchMitglied;

  private AbstractInput mitglied;

  private TextInput scriptName;

  private TextAreaInput scriptCode;

  private TextAreaInput scriptResult;

  private Mitglied selectedMitglied = null;

  private final LesefeldAuswerter lesefeldAuswerter = new LesefeldAuswerter();

  private boolean ersterTabAufruf = true;

  public LesefeldControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
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
   * @return Das Lesefeld welches im LesefeldDetailView bearbeitet wird.
   * 
   */
  public Lesefeld getLesefeld()
  {
    if (lesefeld != null)
    {
      return lesefeld;
    }
    lesefeld = (Lesefeld) getCurrentObject();
    return lesefeld;
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
    suchMitglied = new MitgliedInput().getMitgliedInput(mitglied,
        selectedMitglied,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    suchMitglied.addListener(new SuchMitgliedListener());
    initLesefelder();
    lesefeldAuswerter.evalAlleLesefelder();
    return suchMitglied;
  }

  /**
   * Listener, der Änderungen bei suchMitglied überwacht.
   * 
   */
  public class SuchMitgliedListener implements Listener
  {

    SuchMitgliedListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Mitglied selected = (Mitglied) getSuchMitglied().getValue();
        if (selected == null || selected == selectedMitglied)
          return;
        selectedMitglied = selected;
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
   * 
   * @throws RemoteException
   * @return Das Mitglied Feld welches im LesefeldDetailView für die Anzeige der
   *         Lesefelder verwendet wird.
   * 
   */
  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    mitglied = new MitgliedInput().getMitgliedInput(mitglied, selectedMitglied,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    mitglied.addListener(new MitgliedListener());
    initLesefelder();
    return mitglied;
  }

  /**
   * Listener, der Änderungen bei mitglied überwacht.
   * 
   */
  public class MitgliedListener implements Listener
  {

    MitgliedListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Mitglied selected = (Mitglied) getMitglied().getValue();
        if (selected == null || selected == selectedMitglied)
          return;
        selectedMitglied = selected;
        lesefeldAuswerter
            .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
        updateScriptResult();
        settings.setAttribute("mitglied", selectedMitglied.getID());
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
   * Name des bearbeiteten Lesefeldes Scripts.
   * 
   * @throws RemoteException
   * @return Eingabefeld für den Script Namen.
   * 
   */
  public TextInput getScriptName() throws RemoteException
  {
    if (scriptName != null)
    {
      return scriptName;
    }
    scriptName = new TextInput(
        getLesefeld() != null ? lesefeld.getBezeichnung() : "");
    scriptName.setMandatory(true);
    return scriptName;
  }

  /**
   * Code des bearbeiteten Lesefeldes Scripts
   * 
   * @throws RemoteException
   * @return Eingabefeld für den Script Code.
   * 
   */
  public TextAreaInput getScriptCode() throws RemoteException
  {
    if (scriptCode != null)
    {
      return scriptCode;
    }
    scriptCode = new TextAreaInput(
        getLesefeld() != null ? lesefeld.getScript() : "");
    scriptCode.setMandatory(true);
    return scriptCode;
  }

  /**
   * Ausgabe des bearbeiteten Lesefeldes Scripts
   * 
   * @throws RemoteException
   * @return Ausgabefeld für die Script Ausgabe.
   * 
   */
  public TextAreaInput getScriptResult() throws RemoteException
  {
    if (scriptResult != null)
    {
      return scriptResult;
    }
    scriptResult = new TextAreaInput(
        lesefeld != null ? lesefeld.getEvaluatedContent() : "");
    scriptResult.setEnabled(false);
    return scriptResult;
  }

  /**
   * @throws EvalError
   * @return Button zur Anzeige der verfügbaren Variablen.
   * 
   */
  public Button getVariablenAnzeigenButton() throws EvalError
  {
    return new Button("Variablen anzeigen",
        new InsertVariableDialogAction(lesefeldAuswerter.getMap(), false), null,
        false, "bookmark.png");
  }

  /**
   * @return Button zur Aktualisierung der Ausgabe eines Scriptes
   * 
   */
  public Button getAktualisierenButton()
  {
    return new Button("Aktualisieren", context -> {
      updateScriptResult();
    }, this, false, "view-refresh.png");
  }

  /**
   * Bereitet das Lesesefeld Objekt mit aktuellen Werten zum Speichern vor.
   * 
   * @throws RemoteException
   * @throws ApplicationException
   * @return Das vorbereitete Lesesefeld Objekt
   */
  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    if (updateScriptResult())
    {
      lesefeldAuswerter.addLesefelderDefinition(lesefeld);
    }
    else
    {
      throw new ApplicationException(
          "Skript enthält Fehler. Kann nicht gespeichert werden.");
    }
    return getLesefeld();
  }

  /**
   * Speichert das Lesesefeld Objekt mit aktuellen Werten.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Lesefeldes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  /**
   * Initialisiert die Lesefelder im Lesefeld Auswerter mit den Daten aus der
   * Datenbank.
   * 
   * @throws RemoteException
   * 
   * 
   */
  private void initLesefelder() throws RemoteException
  {
    lesefeldAuswerter.setLesefelderDefinitionsFromDatabase();
    // Die Map darf nur gesetzt werden wenn es ein Mitglied gibt!
    if (selectedMitglied != null)
    {
      lesefeldAuswerter
          .setMap(new MitgliedMap().getMap(selectedMitglied, null, true));
    }
  }

  /**
   * @throws RemoteException
   * @return Die Lesefelder Tabelle für den LesefeldListeView.
   * 
   */
  public Part getLesefelderList() throws RemoteException
  {
    if (lesefeldList == null)
    {
      lesefeldList = new JVereinTablePart(lesefeldAuswerter.getLesefelder(),
          null);
      lesefeldList.addColumn("Skript-Name", "bezeichnung");
      lesefeldList.addColumn("Ausgabe", "ausgabe");
      lesefeldList.setContextMenu(new LesefeldMenu(lesefeldList));
      lesefeldList.setRememberColWidths(true);
      lesefeldList.setRememberOrder(true);
      lesefeldList.addFeature(new FeatureSummary());
      lesefeldList
          .setAction(new EditAction(LesefeldDetailView.class, lesefeldList));
      VorZurueckControl.setObjektListe(null, null);
    }
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
      Logger.error("Fehler", ex);
    }
  }

  /**
   * Aktualisiert lokales Feld lesefeld mit den vom Nutzer eingegebenen Daten
   * aus der GUI. Dabei wird ggf. lesefeld initialisiert und die Eindeutigkeit
   * des Namens des Skriptes sichergestellt.
   */
  private boolean updateLesefeldFromGUI()
  {
    if (scriptName.getValue().toString().isEmpty())
    {
      GUI.getStatusBar().setErrorText("Bitte Skript-Namen eingeben.");
      return false;
    }
    try
    {
      String lesefeldid = getLesefeld().getID();
      if (lesefeldid == null)
      {
        lesefeldid = "0";
      }
      for (Lesefeld lesefeld : lesefeldAuswerter.getLesefelder())
      {
        // Bezeichnung von Lesefeld muss eindeutig sein.
        if (lesefeld.getBezeichnung().equals(scriptName.getValue()))
        {
          String currentid = lesefeld.getID();
          if (!lesefeldid.equalsIgnoreCase(currentid))
          {
            GUI.getStatusBar()
                .setErrorText("Bitte eindeutigen Skript-Namen eingeben!");
            return false;
          }
        }
      }
      lesefeld.setBezeichnung((String) scriptName.getValue());
      lesefeld.setScript((String) scriptCode.getValue());
      lesefeld.setEvaluatedContent((String) scriptResult.getValue());
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Holt akutelles Skript von GUI, evaluiert dieses und schreibt Ergebnis
   * zurück in die GUI.
   *
   * @return true bei Erfolg, sonst false (Fehlermeldung wird in
   *         Skript-Ausgabe-Feld geschrieben).
   */
  public boolean updateScriptResult()
  {
    if (!updateLesefeldFromGUI())
      return false;
    String result = "";
    boolean success = true;
    try
    {
      result = (String) lesefeldAuswerter.eval(lesefeld.getScript());
      if (result == null)
      {
        result = "Skript-Fehler: Skript muss Rückgabewert liefern.";
        success = false;
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      result = "Skript-Fehler:\r\n" + e.getMessage();
      success = false;
    }
    finally
    {
      scriptResult.setValue(result);
    }
    return success;
  }

  /**
   * 
   * @throws RemoteException
   * @return Die Lesefelder Tabelle für den Tab im MitgliedDetailView.
   * 
   */
  public Part getLesefeldMitgliedList() throws RemoteException
  {
    if (lesefeldMitgliedList != null)
    {
      return lesefeldMitgliedList;
    }
    lesefeldMitgliedList = new TablePart(lesefeldAuswerter.getLesefelder(),
        new EditAction(LesefeldDetailView.class));
    lesefeldMitgliedList.addColumn("Skript-Name", "bezeichnung");
    lesefeldMitgliedList.addColumn("Ausgabe", "ausgabe");
    lesefeldMitgliedList.setContextMenu(new LesefeldMenu(null));
    lesefeldMitgliedList.setRememberColWidths(true);
    lesefeldMitgliedList.setRememberOrder(true);
    return lesefeldMitgliedList;
  }

  /**
   * Initialisiert den Lesefeldauswerter. Es werden die Lesefelder aus der
   * Datenbank gelesen und alle evaluiert. Dann stehen auch die Ausgaben für die
   * Tabelle bereit.
   * 
   * @throws RemoteException
   * 
   */
  public void initLesefeldMitgliedList(Mitglied m) throws RemoteException
  {
    // Bei neuem Mitglied noch nichts anzeigen
    if (m != null && m.getID() != null)
    {
      selectedMitglied = m;
      // Mitglied in Settings speichern damit es beim Editieren oder der Neu
      // Action im LesefeldDetailView erscheint.
      settings.setAttribute("mitglied", m.getID());
      initLesefelder();
      lesefeldAuswerter.evalAlleLesefelder();
    }
  }

  /**
   * Werden die Lesefelder bei Mitgliedern im Tab angezeigt, dann werden sie
   * erst gezeichnet wenn der Tab selektiert wird. Vorher wird nur eine leere
   * nicht sichtbare Liste gezeichnet. Der Update wird immer aufgerufen wenn der
   * Tab selektiert wird. Die Lesefelder müssen aber pro Mitglied nur einmal
   * gefüllt werden.
   * 
   * @throws RemoteException
   * 
   */
  public void updateLesefeldMitgliedList(Mitglied m) throws RemoteException
  {
    // Bei neuem Mitglied noch nichts anzeigen
    if (m != null && m.getID() != null)
    {
      if (ersterTabAufruf)
      {
        initLesefeldMitgliedList(m);
        lesefeldAuswerter.evalAlleLesefelder();
        lesefeldMitgliedList.removeAll();
        for (Lesefeld lesefeld : lesefeldAuswerter.getLesefelder())
        {
          lesefeldMitgliedList.addItem(lesefeld);
        }
        ersterTabAufruf = false;
      }
    }
  }
}
