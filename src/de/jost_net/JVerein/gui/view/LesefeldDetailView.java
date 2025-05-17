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

package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import java.rmi.RemoteException;

/**
 * Ein View zum Bearbeiten von Skripten für ein Lesefeld.
 */
public class LesefeldDetailView extends AbstractView
{

  private final LesefeldAuswerter lesefeldAuswerter;

  private Lesefeld lesefeld;

  private TextAreaInput textAreaInputScriptCode;

  private TextAreaInput textAreaInputScriptResult;

  private TextInput textInputScriptName;

  private AbstractInput mitglied;

  private Mitglied selectedMitglied;

  public LesefeldDetailView(LesefeldAuswerter lesefeldAuswerter,
      Lesefeld lesefeld, Mitglied mitglied)
  {
    this.lesefeldAuswerter = lesefeldAuswerter;
    this.lesefeld = lesefeld;
    this.selectedMitglied = mitglied;
  }

  @Override
  public void bind() throws Exception
  {
    Composite parent = getParent();
    SimpleContainer container = new SimpleContainer(parent, true);

    container.addLabelPair("Mitglied", getMitglied());

    // Auf diese Input-Felder sollte nur über die Funktionen
    // updateLesefeldFromGUI() und updateScriptResult() zugegriffen werden.
    textInputScriptName = new TextInput(
        lesefeld != null ? lesefeld.getBezeichnung() : "");
    container.addLabelPair("Skript-Name", textInputScriptName);

    textAreaInputScriptCode = new TextAreaInput(
        lesefeld != null ? lesefeld.getScript() : "");
    container.addLabelPair("Skript", textAreaInputScriptCode);

    textAreaInputScriptResult = new TextAreaInput("");
    container.addLabelPair("Ausgabe", textAreaInputScriptResult);

    if (lesefeld != null)
      updateScriptResult();

    ButtonArea buttonArea = new ButtonArea();
    Button button = new Button("Aktualisieren", context -> updateScriptResult(),
        null, false, "view-refresh.png");
    buttonArea.addButton(button);
    button = new Button("Variablen anzeigen",
        new InsertVariableDialogAction(lesefeldAuswerter.getMap(), false), null,
        false, "bookmark.png");
    buttonArea.addButton(button);
    button = new Button("Speichern", new SaveLesefeldAction(), null, false,
        "document-save.png");
    buttonArea.addButton(button);
    buttonArea.paint(this.getParent());
  }

  /**
   * Aktualisiert lokales Feld lesefeld mit den vom Nutzer eingegebenen Daten
   * aus der GUI. Dabei wird ggf. lesefeld initialisiert und die Eindeutigkeit
   * des Namens des Skriptes sichergestellt.
   */
  private boolean updateLesefeldFromGUI()
  {
    if (textInputScriptName.getValue().toString().isEmpty())
    {
      GUI.getStatusBar().setErrorText("Bitte Skript-Namen eingeben.");
      return false;
    }
    try
    {
      for (Lesefeld lesefeld : lesefeldAuswerter.getLesefelder())
      {
        // Bezeichnung von Lesefeld muss eindeutig sein.
        if (lesefeld.getBezeichnung().equals(textInputScriptName.getValue()))
        {
          String currentid = lesefeld.getID();
          if (this.lesefeld == null || !this.lesefeld.getID()
              .equalsIgnoreCase(currentid))
          {
            GUI.getStatusBar()
                .setErrorText("Bitte eindeutigen Skript-Namen eingeben!");
            return false;
          }
        }
      }

      // erstelle neues lesefeld, wenn nötig.
      if (lesefeld == null)
        lesefeld = Einstellungen.getDBService()
            .createObject(Lesefeld.class, null);

      lesefeld.setBezeichnung((String) textInputScriptName.getValue());
      lesefeld.setScript((String) textAreaInputScriptCode.getValue());
      lesefeld.setEvaluatedContent(
          (String) textAreaInputScriptResult.getValue());
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
   *     Skript-Ausgabe-Feld geschrieben).
   */
  private boolean updateScriptResult()
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
      textAreaInputScriptResult.setValue(result);
    }

    return success;
  }

  private final class SaveLesefeldAction implements Action
  {

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      if (updateScriptResult())
      {

        lesefeldAuswerter.addLesefelderDefinition(lesefeld);

        try
        {
          lesefeld.store();
          GUI.getStatusBar().setSuccessText("Skript gespeichert.");
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar()
              .setErrorText("Skript kann nicht gespeichert werden.");
          Logger.error("Skript kann nicht gespeichert werden.", e);
        }
      }
      else
        GUI.getStatusBar().setErrorText(
            "Skript enthält Fehler. Kann nicht gespeichert werden.");
    }
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    mitglied = new MitgliedInput().getMitgliedInput(mitglied, selectedMitglied,
        Einstellungen.getEinstellung().getMitgliedAuswahl());
    mitglied.addListener(new MitgliedListener());
    mitglied.setMandatory(true);
    return mitglied;
  }

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
        lesefeldAuswerter.setMap(
            new MitgliedMap().getMap(selectedMitglied, null, true));
        updateScriptResult();
      }
      catch (RemoteException e)
      {
        String fehler = "Fehler beim Auswählen des Mitgliedes";
        Logger.error(fehler, e);
        GUI.getStatusBar().setErrorText(fehler);
      }
    }
  }

}
