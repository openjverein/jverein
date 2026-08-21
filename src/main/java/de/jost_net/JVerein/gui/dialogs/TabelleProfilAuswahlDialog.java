/**********************************************************************
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
package de.jost_net.JVerein.gui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TabelleProfilAuswahlDialog extends AbstractDialog<Object>
{
  private Settings settings;

  private String tablePartId;

  private SelectInput profilname;

  private TextAreaInput attributes;

  private Settings tablesettings;

  public TabelleProfilAuswahlDialog(IJVereinPart tablePart)
      throws RemoteException, ApplicationException
  {
    super(TabelleProfilAuswahlDialog.POSITION_CENTER);
    this.tablePartId = tablePart.getTablePartID(null, tablePart.getTableName());
    this.tablesettings = tablePart.getSettings();
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    setTitle("Spalten/Export Profile");
    setSize(605, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, null);
    group.addInput(getProfilname());
    group.addInput(getAttributes());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ALLGEMEIN, false, "question-circle.png");

    buttons.addButton("Neu", context -> {
      handleNeu();
    }, null, true, "document-new.png");

    buttons.addButton("Überchreiben", context -> {
      if (!confirm("Profil überschreiben",
          "Soll das ausgewählte Profil wirklich überschrieben werden?"))
      {
        return;
      }
      handleSpeichern(null);
    }, null, false, "document-save.png");

    buttons.addButton("Löschen", context -> {
      if (!confirm("Profil löschen",
          "Soll das ausgewählte Profil wirklich gelöscht werden?"))
      {
        return;
      }
      handleLoeschen();
    }, null, false, "user-trash-full.png");

    buttons.addButton("Anwenden", context -> {
      if (!confirm("Profil anwenden",
          "Soll das ausgewählte Profil wirklich angewendet werden?\n"
              + "Es überschreibt die Einstellungen des Spaltenauswahl Dialogs\n"
              + "und der CSV/PDF Export Dialoge."))
      {
        return;
      }
      handleAnwenden();
    }, null, false, "view-refresh.png");

    buttons.addButton("Reset", context -> {
      if (!confirm("Einstellungen zurücksetzen",
          "Sollen die Einstellungen des Spaltenauswahl Dialogs\n"
              + "und der CSV/PDF Export Dialoge wirklich zurückgesetzt werden?"))
      {
        return;
      }
      handleReset();
    }, null, false, "edit-undo.png");

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttons.paint(parent);
  }

  private boolean confirm(String Titel, String Text) throws ApplicationException
  {
    YesNoDialog dialog = new YesNoDialog(AbstractDialog.POSITION_CENTER);
    dialog.setTitle(Titel);
    dialog.setText(Text);
    try
    {
      return (boolean) dialog.open();
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }

  private SelectInput getProfilname() throws RemoteException
  {
    if (profilname != null)
    {
      return profilname;
    }
    StringTokenizer stt = new StringTokenizer(
        settings.getString(tablePartId + "profile", ""), ",");
    List<String> profile = new ArrayList<>();
    while (stt.hasMoreElements())
    {
      profile.add(stt.nextToken());
    }
    profilname = new SelectInput(profile,
        settings.getString(tablePartId + "profilname", ""));
    profilname.setName("Profilname");
    profilname.addListener(event -> {
      try
      {
        getAttributes().setValue(getText((String) getProfilname().getValue()));
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Lesen der Attribute.", e);
      }
    });
    return profilname;
  }

  public TextAreaInput getAttributes() throws RemoteException
  {
    if (attributes != null)
    {
      return attributes;
    }
    String text = "";
    if (getProfilname().getValue() != null)
    {
      text = getText((String) getProfilname().getValue());
    }
    attributes = new TextAreaInput(text);
    attributes.setHeight(200);
    attributes.setName("Attribute");
    attributes.disable();
    return attributes;
  }

  // Erzeugt ein neues Profil mit aktuellen Settings und speichert es
  private void handleNeu()
  {
    try
    {
      @SuppressWarnings("unchecked")
      List<String> list = getProfilname().getList();
      ProfilnameNeuDialog pnd = new ProfilnameNeuDialog(
          ProfilnameNeuDialog.POSITION_CENTER, list);
      String name = pnd.open();
      if (name != null)
      {
        handleSpeichern(name);
      }
    }
    catch (OperationCanceledException ex)
    {
      return;
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Anlegen eines Profil.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Speichert das ausgewählte Profil mit aktuellen Settings
  private void handleSpeichern(String item)
  {
    try
    {
      if (item == null)
      {
        item = (String) getProfilname().getValue();
      }
      if (item == null)
      {
        return;
      }
      @SuppressWarnings("unchecked")
      List<String> list = getProfilname().getList();
      if (!list.contains(item))
      {
        list.add(item);
        Collections.sort(list);
        StringBuilder text = new StringBuilder();
        for (String string : list)
        {
          if (text.length() > 0)
          {
            text.append(",");
          }
          text.append(string);
        }
        settings.setAttribute(tablePartId + "profile", text.toString());
      }
      settings.setAttribute(tablePartId + "profilname", item);
      // Spaltenauswahl Attribute speichern
      settings.setAttribute(tablePartId + item + ".SPALTEN",
          getSettings(tablesettings, tablePartId));
      // CSV/PDF Export Attribute speichern
      settings.setAttribute(tablePartId + item + ".EXPORT",
          getSettings(new Settings(TablePartExportDialog.class), tablePartId));
      close();
      GUI.getStatusBar().setSuccessText("Profil " + item + " gespeichert.");
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Speichern eines Profil.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Löscht ein Profil aus der Liste und setzt die Werte auf ""
  private void handleLoeschen()
  {
    try
    {
      String item = (String) getProfilname().getValue();
      if (item == null)
      {
        return;
      }
      @SuppressWarnings("unchecked")
      List<String> list = getProfilname().getList();
      list.remove(item);
      getProfilname().setList(list);
      StringBuilder text = new StringBuilder();
      for (String string : list)
      {
        if (text.length() > 0)
        {
          text.append(",");
        }
        text.append(string);
      }
      settings.setAttribute(tablePartId + "profile", text.toString());
      settings.setAttribute(tablePartId + "profilname",
          (String) profilname.getValue());
      settings.setAttribute(tablePartId + item + ".SPALTEN", (String) null);
      settings.setAttribute(tablePartId + item + ".EXPORT", (String) null);
      close();
      GUI.getStatusBar().setSuccessText("Profil " + item + "  gelöscht.");
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Löschen eines Profil.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Wendet die gespeicherten Settings des Profils in der Liste an
  private void handleAnwenden()
  {
    try
    {
      String item = (String) getProfilname().getValue();
      if (item == null)
      {
        return;
      }
      // Settings für die Spaltenauswahl setzen
      setSettings(tablesettings,
          settings.getString(tablePartId + item + ".SPALTEN", ""));
      // Settings für die Export Dialoge setzen
      setSettings(new Settings(TablePartExportDialog.class),
          settings.getString(tablePartId + item + ".EXPORT", ""));
      settings.setAttribute(tablePartId + "profilname", item);
      close();
      GUI.getCurrentView().reload();
      GUI.getStatusBar().setSuccessText("Profil " + item + " angewendet.");
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Anwenden eines Profil.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Löscht alle Settings der Tabelle bis auf den Ausgabepfad bei CSV und PDF
  // Export
  private void handleReset()
  {
    try
    {
      resetSettings(tablesettings, tablePartId);
      Settings s = new Settings(TablePartExportDialog.class);
      String csvDir = s.getString(tablePartId + "CSV.lastdir", "");
      String pdfDir = s.getString(tablePartId + "PDF.lastdir", "");
      resetSettings(s, tablePartId);
      if (!csvDir.isEmpty())
      {
        s.setAttribute(tablePartId + "CSV.lastdir", csvDir);
      }
      if (!pdfDir.isEmpty())
      {
        s.setAttribute(tablePartId + "PDF.lastdir", pdfDir);
      }
      close();
      GUI.getCurrentView().reload();
      GUI.getStatusBar().setSuccessText(
          "Alle Spalten und CSV/PDF Export Einstellungen gelöscht.");
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Löschen der Settings.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Setzt die Settings zurück
  private void resetSettings(Settings s, String prefix)
  {
    for (String key : s.getAttributes())
    {
      if (key.startsWith(prefix))
      {
        s.setAttribute(key, (String) null);
      }
    }
  }

  // Wandelt die Settings in einen Property XML String um, damit er gespeichert
  // werden kann
  private String getSettings(Settings s, String prefix)
      throws IOException, RemoteException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Properties prop = new Properties();
    for (String key : s.getAttributes())
    {
      if (key.startsWith(prefix))
      {
        prop.put(key.substring(prefix.length()), s.getString(key, ""));
      }
    }
    prop.storeToXML(bos, "sicherung", "UTF8");
    return bos.toString();
  }

  // Speichert in Settings die Attribute aus data
  private void setSettings(Settings s, String data)
      throws InvalidPropertiesFormatException, IOException
  {
    ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
    Properties p = new Properties();
    p.loadFromXML(bis);
    for (Object o : p.keySet())
    {
      String key = (String) o;
      s.setAttribute(tablePartId + key, p.getProperty(key));
    }
  }

  // Generiert den Text für das Attribute TextArea
  private String getText(String item)
  {
    StringBuilder text = new StringBuilder();
    text.append("Profil: " + item + "\n");
    try
    {
      if (item != null)
      {
        String s = settings.getString(tablePartId + item + ".SPALTEN", "");
        ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
        Properties p = new Properties();
        p.loadFromXML(bis);
        text.append("Spaltenauswahl\n");
        text.append(p.toString());
        s = settings.getString(tablePartId + item + ".EXPORT", "");
        bis = new ByteArrayInputStream(s.getBytes());
        p = new Properties();
        p.loadFromXML(bis);
        text.append("\n\nCSV/PDF Export\n");
        text.append(p.toString());
        return text.toString();
      }
    }
    catch (Exception e)
    {
      String error = "Fehler beim Lesen der Attribute.";
      Logger.error(error, e);
      return error;
    }
    return "";
  }

  @Override
  protected Object getData()
  {
    return null;
  }
}
