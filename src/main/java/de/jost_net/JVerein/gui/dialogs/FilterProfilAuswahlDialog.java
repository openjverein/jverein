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
import java.util.List;
import java.util.Properties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Suchprofil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
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

public class FilterProfilAuswahlDialog extends AbstractDialog<Object>
{
  private Settings settings;

  private SelectInput profilname;

  private TextAreaInput attributes;

  private FilterControl control;

  private AbstractView view;

  public FilterProfilAuswahlDialog(Settings settings, FilterControl control,
      AbstractView view) throws RemoteException, ApplicationException
  {
    super(FilterProfilAuswahlDialog.POSITION_CENTER);

    this.settings = settings;
    this.control = control;
    this.view = view;
    setTitle("Filter Profile");
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
        DokumentationUtil.SUCHPROFIL, false, "question-circle.png");

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
      handleAnwenden();
    }, null, false, "view-refresh.png");

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
    DBService service = Einstellungen.getDBService();
    DBIterator<Suchprofil> profile = service.createList(Suchprofil.class);
    profile.addFilter("clazz = ?", view.getClass().getName());
    profile.setOrder("ORDER BY bezeichnung");
    Suchprofil sp1 = null;
    try
    {
      sp1 = (Suchprofil) Einstellungen.getDBService().createObject(
          Suchprofil.class,
          settings.getString(control.getSettingsPrefix() + "id", null));
    }
    catch (ObjectNotFoundException e)
    {
      // Dann kein spezifisches Profil ausgewählt
    }
    profilname = new SelectInput(PseudoIterator.asList(profile), sp1);
    profilname.setName("Profilname");
    profilname.addListener(event -> {
      try
      {
        getAttributes()
            .setValue(getText((Suchprofil) getProfilname().getValue()));
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
      text = getText((Suchprofil) getProfilname().getValue());
    }
    attributes = new TextAreaInput(text);
    attributes.setHeight(200);
    attributes.setName("Attribute");
    attributes.disable();
    return attributes;
  }

  // Erzeugt ein neues Profil mit aktuellen Settings und speichert es
  @SuppressWarnings("unchecked")
  private void handleNeu()
  {
    try
    {
      List<String> list = new ArrayList<>();
      for (Suchprofil sp : (List<Suchprofil>) getProfilname().getList())
      {
        list.add(sp.getBezeichnung());
      }
      ProfilnameNeuDialog pnd = new ProfilnameNeuDialog(
          ProfilnameNeuDialog.POSITION_CENTER, list);
      String name = pnd.open();
      if (name != null)
      {
        Suchprofil sp = (Suchprofil) Einstellungen.getDBService()
            .createObject(Suchprofil.class, null);
        sp.setClazz(view.getClass().getName());
        sp.setBezeichnung(name);
        handleSpeichern(sp);
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
  private void handleSpeichern(Suchprofil item)
  {
    try
    {
      if (item == null)
      {
        item = (Suchprofil) getProfilname().getValue();
      }
      if (item == null)
      {
        return;
      }

      // Überschreiben eines vorhandenen Suchprofils
      storeSettings(settings, item);
      item.store();
      settings.setAttribute(control.getSettingsPrefix() + "id", item.getID());
      settings.setAttribute(control.getSettingsPrefix() + "profilname",
          item.getBezeichnung());

      close();
      GUI.getStatusBar()
          .setSuccessText("Profil " + item.getBezeichnung() + " gespeichert.");
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
      Suchprofil item = (Suchprofil) getProfilname().getValue();
      if (item == null)
      {
        return;
      }

      if (settings.getString(control.getSettingsPrefix() + "id", "")
          .equals(item.getID()))
      {
        settings.setAttribute(control.getSettingsPrefix() + "id", "");
        settings.setAttribute(control.getSettingsPrefix() + "profilname", "");
      }
      item.delete();

      close();
      GUI.getStatusBar()
          .setSuccessText("Profil " + item.getBezeichnung() + "  gelöscht.");
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
      Suchprofil item = (Suchprofil) getProfilname().getValue();
      if (item == null)
      {
        return;
      }
      String prefix = control.getSettingsPrefix();
      ByteArrayInputStream bis = new ByteArrayInputStream(item.getInhalt());
      Properties p = new Properties();
      p.loadFromXML(bis);
      for (Object o : p.keySet())
      {
        String key = (String) o;
        settings.setAttribute(prefix + "filter_" + key, p.getProperty(key));
      }
      settings.setAttribute(prefix + "id", item.getID());
      settings.setAttribute(prefix + "profilname", item.getBezeichnung());

      close();
      GUI.getCurrentView().reload();
      GUI.getStatusBar()
          .setSuccessText("Profil " + item.getBezeichnung() + " angewendet.");
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

  /**
   * Settings werden in eine XML-Struktur serialisiert und als Byte-Array in das
   * Model übertragen
   * 
   * @param s
   *          Die zu speichernden Settings
   * @param sp1
   *          Das Model
   */
  private void storeSettings(Settings s, Suchprofil sp1)
      throws IOException, RemoteException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Properties prop = getSettings2Properties(s);
    prop.storeToXML(bos, "sicherung", "UTF8");
    sp1.setInhalt(bos.toByteArray());
  }

  /**
   * Settings können nicht direkt serialisiert werden. Daher werden sie in
   * Properties umgewandelt
   * 
   * @param settings
   *          Die umzuwandelnden Settings
   * @return Die Properties
   */
  private Properties getSettings2Properties(Settings settings)
  {
    Properties ret = new Properties();
    String prefix = control.getSettingsPrefix() + "filter_";
    for (String key : settings.getAttributes())
    {
      if (key.startsWith(prefix))
      {
        ret.put(key.substring(prefix.length()), settings.getString(key, ""));
      }
    }
    return ret;
  }

  // Generiert den Text für das Attribute TextArea
  private String getText(Suchprofil item)
  {
    try
    {
      if (item != null)
      {
        StringBuilder text = new StringBuilder();
        text.append("Profil: " + item.getBezeichnung() + "\n");
        ByteArrayInputStream bis = new ByteArrayInputStream(item.getInhalt());
        Properties p = new Properties();
        p.loadFromXML(bis);
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
