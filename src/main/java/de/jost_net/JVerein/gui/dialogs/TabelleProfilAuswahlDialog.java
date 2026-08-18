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
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.AbstractTablePart;
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

  public TabelleProfilAuswahlDialog(IJVereinPart tablePart)
      throws RemoteException, ApplicationException
  {
    super(TabelleProfilAuswahlDialog.POSITION_CENTER);
    this.tablePartId = tablePart.getTablePartID(tablePartId, null);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    setTitle("Spalten/Export Profile");
    setSize(545, SWT.DEFAULT);
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
      handleSpeichern(null);
    }, null, false, "document-save.png");

    buttons.addButton("Löschen", context -> {
      handleLoeschen();
    }, null, false, "user-trash-full.png");

    buttons.addButton("Anwenden", context -> {
      handleAnwenden();
    }, null, false, "view-refresh.png");

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttons.paint(parent);
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
      ;
    {
      text = getText((String) getProfilname().getValue());
    }
    attributes = new TextAreaInput(text, 1024);
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
      settings.setAttribute(tablePartId + item + "." + "SPALTEN",
          getSettings(new Settings(AbstractTablePart.class), tablePartId));
      settings.setAttribute(tablePartId + item + "." + "EXPORT",
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
      settings.setAttribute(tablePartId + item + "." + "SPALTEN", "");
      settings.setAttribute(tablePartId + item + "." + "EXPORT", "");
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
      Settings s = new Settings(AbstractTablePart.class);
      String spalten = settings.getString(tablePartId + item + "." + "SPALTEN",
          "");
      ByteArrayInputStream bis = new ByteArrayInputStream(spalten.getBytes());
      Properties p = new Properties();
      p.loadFromXML(bis);
      for (Object o : p.keySet())
      {
        String key = (String) o;
        s.setAttribute(tablePartId + key, p.getProperty(key));
      }

      // Settings für die Export Dialoge setzen
      s = new Settings(TablePartExportDialog.class);
      String exports = settings.getString(tablePartId + item + "." + "EXPORT",
          "");
      bis = new ByteArrayInputStream(exports.getBytes());
      p = new Properties();
      p.loadFromXML(bis);
      for (Object o : p.keySet())
      {
        String key = (String) o;
        s.setAttribute(tablePartId + key, p.getProperty(key));
      }
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

  // Wandelt die Settings in einen Property String um, damit er gespeichert
  // werden kann
  private String getSettings(Settings s, String prefix)
      throws IOException, RemoteException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Properties prop = new Properties();
    for (String key : s.getAttributes())
    {
      String entry = s.getString(key, "");
      if (key.startsWith(prefix))
      {
        prop.put(key.substring(prefix.length()), entry);
      }
    }
    prop.storeToXML(bos, "sicherung", "UTF8");
    return bos.toString();
  }

  private String getText(String item)
  {
    StringBuilder text = new StringBuilder();
    text.append("Profil: " + item + "\n");
    try
    {
      if (item != null)
      {
        String s = settings.getString(tablePartId + item + "." + "SPALTEN", "");
        ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
        Properties p = new Properties();
        p.loadFromXML(bis);
        text.append("Spaltenauswahl\n");
        text.append(p.toString());
        s = settings.getString(tablePartId + item + "." + "EXPORT", "");
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
      Logger.error("Fehler beim Lesen der Attribute.", e);
    }
    return "";
  }

  @Override
  protected Object getData()
  {
    return null;
  }
}
