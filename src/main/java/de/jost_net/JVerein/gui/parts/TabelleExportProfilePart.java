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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.gui.dialogs.ProfilnameNeuDialog;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TabelleExportProfilePart implements Part
{

  private SelectInput profilname;

  private AbstractPartExportDialog dialog;

  private String settingPrefix;

  private Settings settings;

  public TabelleExportProfilePart(AbstractPartExportDialog dialog,
      Settings settings, String settingPrefix)
      throws RemoteException, ApplicationException
  {
    this.dialog = dialog;
    this.settings = settings;
    this.settingPrefix = settingPrefix;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent, "Profile");
    group.addInput(getProfilname());

    ButtonArea buttons = new ButtonArea();

    buttons.addButton(new NewButton(context -> {
      handleNeu();
    }));

    buttons.addButton("Speichern", context -> {
      handleSpeichern(null, false);
    }, null, false, "document-save.png");

    buttons.addButton(new DeleteButton(context -> {
      if (!confirm("Profil löschen",
          "Soll das ausgewählte Profil wirklich gelöscht werden?"))
      {
        return;
      }
      handleLoeschen();
    }));

    buttons.addButton("Anwenden", context -> {
      handleAnwenden();
    }, null, false, "view-refresh.png");

    group.addButtonArea(buttons);
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

  private void showDialog()
  {
    SimpleDialog sd = new SimpleDialog(AbstractDialog.POSITION_CENTER);
    sd.setText("Bitte ein Profil auswählen!");
    try
    {
      sd.open();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
  }

  private SelectInput getProfilname() throws RemoteException
  {
    if (profilname != null)
    {
      return profilname;
    }
    StringTokenizer stt = new StringTokenizer(
        settings.getString(settingPrefix + "profile", ""), ",");
    List<String> profile = new ArrayList<>();
    while (stt.hasMoreElements())
    {
      profile.add(stt.nextToken());
    }
    profilname = new SelectInput(profile,
        settings.getString(settingPrefix + "profilname", ""));
    profilname.setName("Profilname");
    return profilname;
  }

  // Erzeugt ein neues Profil mit aktuellen Daten und speichert es
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
        handleSpeichern(name, true);
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
  private void handleSpeichern(String item, boolean neu)
  {
    try
    {
      if (item == null)
      {
        item = (String) getProfilname().getValue();
      }
      if (item == null)
      {
        showDialog();
        return;
      }

      if (!neu && !confirm("Profil Speichern", "Soll das ausgewählte Profil \""
          + item
          + "\" mit den aktuellen Dialog Einstellungen überschrieben werden?"))
      {
        return;
      }

      @SuppressWarnings("unchecked")
      List<String> list = getProfilname().getList();
      if (!list.contains(item))
      {
        list.add(item);
        Collections.sort(list);
        settings.setAttribute(settingPrefix + "profile",
            StringUtils.join(list, ","));
      }
      settings.setAttribute(settingPrefix + "profilname", item);
      // Dialog Attribute als Profil speichern
      dialog.saveSettings(settingPrefix + "profil." + item + ".");

      profilname.setList(list);
      profilname.setPreselected(item);
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

  // Löscht ein Profil aus der Liste
  private void handleLoeschen()
  {
    try
    {
      String item = (String) getProfilname().getValue();
      if (item == null)
      {
        showDialog();
        return;
      }
      @SuppressWarnings("unchecked")
      List<String> list = getProfilname().getList();
      list.remove(item);
      getProfilname().setList(list);
      settings.setAttribute(settingPrefix + "profile",
          StringUtils.join(list, ","));
      settings.setAttribute(settingPrefix + "profilname",
          (String) profilname.getValue());
      String prefix = settingPrefix + "profil." + item + ".";
      for (String key : settings.getAttributes())
      {
        if (key.startsWith(prefix))
        {
          // Alle Attribute des Profils löschen
          settings.setAttribute(key, (String) null);
        }
      }
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

  // Wendet die gespeicherten Settings des Profils im Dialog an
  private void handleAnwenden()
  {
    try
    {
      String item = (String) getProfilname().getValue();
      if (item == null)
      {
        showDialog();
        return;
      }
      // Die Profil Settings in Dialog einlesen
      dialog.loadSettings(settingPrefix + "profil." + item + ".");
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
}
