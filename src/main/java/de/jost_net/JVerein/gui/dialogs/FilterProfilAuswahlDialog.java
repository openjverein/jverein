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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.parts.ButtonAreaRtoL;
import de.jost_net.JVerein.gui.parts.DeleteButton;
import de.jost_net.JVerein.gui.parts.HelpButton;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.NewButton;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.KeyEnum;
import de.jost_net.JVerein.keys.Filter.FilterArt;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Suchprofil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FilterProfilAuswahlDialog extends AbstractDialog<Object>
{
  private Settings settings;

  private SelectInput profilname;

  private FilterControl control;

  private AbstractView view;

  private JVereinTablePart filterList;

  public FilterProfilAuswahlDialog(Settings settings, FilterControl control,
      AbstractView view) throws RemoteException, ApplicationException
  {
    super(FilterProfilAuswahlDialog.POSITION_CENTER);

    this.settings = settings;
    this.control = control;
    this.view = view;
    setTitle("Filter Profile");
    setSize(600, 400);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, null);
    group.addInput(getProfilname());
    getTablePart().paint(parent);

    ButtonAreaRtoL buttons = new ButtonAreaRtoL();
    buttons.addButton(new HelpButton(DokumentationUtil.SUCHPROFIL));

    buttons.addButton(new NewButton(context -> {
      handleNeu();
    }));

    buttons.addButton("Speichern", context -> {
      handleSpeichern(null, false);
    }, null, false, "document-save.png", "CTRL+S");

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
    }, null, true, "view-refresh.png");

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttons.paint(parent);
  }

  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (filterList != null)
    {
      return filterList;
    }
    filterList = new JVereinTablePart(
        getList((Suchprofil) getProfilname().getValue()), null);
    filterList.addColumn("Feld", "key");
    filterList.addColumn("Wert", "value");
    return filterList;
  }

  private void refreshList()
  {
    try
    {
      if (filterList != null)
      {
        filterList.removeAll();
        for (Entry<String, String> en : getList(
            (Suchprofil) getProfilname().getValue()))
        {
          filterList.addItem(en);
        }
        filterList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e);
    }
  }

  // Generiert die Attribute
  private List<Entry<String, String>> getList(Suchprofil item)
  {
    try
    {
      if (item != null)
      {
        List<Entry<String, String>> attributes = new ArrayList<>();
        ByteArrayInputStream bis = new ByteArrayInputStream(item.getInhalt());
        Properties p = new Properties();
        p.loadFromXML(bis);
        for (Entry<Object, Object> entry : p.entrySet())
        {
          String value = (String) entry.getValue();
          Filter f = Filter.getByKey("filter_" + (String) entry.getKey());
          if (f != null && value != null && !value.isBlank())
          {
            if (f.getArray() != null)
            {
              KeyEnum[] enums = f.getArray();
              for (KeyEnum en : enums)
              {
                try
                {
                  if (en.getKey() == Integer.parseInt(value))
                  {
                    value = en.toString();
                    break;
                  }
                }
                catch (Exception ex)
                {
                  String error = "Fehler beim Auswerten des Key: " + value
                      + " von Filter: " + f.getAnzeigeText();
                  Logger.error(error, ex);
                }
              }
            }
            else if (f.getDbObject() != null)
            {
              if (value.equals("0"))
              {
                if (f.getDbObject() == Steuer.class)
                {
                  value = FilterControl.OHNE_STEUER;
                }
                else if (f.getDbObject() == Projekt.class)
                {
                  value = FilterControl.OHNE_PROJEKT;
                }
              }
              else
              {
                try
                {
                  DBObject obj = Einstellungen.getDBService()
                      .createObject(f.getDbObject(), value);
                  value = (String) obj
                      .getAttribute((String) obj.getPrimaryAttribute());
                }
                catch (Exception ex)
                {
                  String error = "Fehler beim Erzeugen der Instanz: " + value
                      + " von Filter: " + f.getAnzeigeText();
                  Logger.error(error, ex);
                }
              }
            }
            else if (f.getArt() == FilterArt.EIGENSCHAFTEN)
            {
              String prefix = "Und: ";
              if (value.startsWith("Oder"))
              {
                prefix = "Oder: ";
              }
              value = prefix
                  + new EigenschaftenAuswahlParameter(value).toString();
            }
            attributes.add(Map.entry(f.getAnzeigeText(), value));
          }
        }
        return attributes;
      }
    }
    catch (Exception e)
    {
      String error = "Fehler beim Lesen der Attribute.";
      Logger.error(error, e);
    }
    return new ArrayList<Entry<String, String>>();
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
          settings.getString(control.getSettingsPrefix() + "profilid", null));
    }
    catch (ObjectNotFoundException e)
    {
      // Dann kein spezifisches Profil ausgewählt
    }
    profilname = new SelectInput(PseudoIterator.asList(profile), sp1);
    profilname.setName("Profilname");
    profilname.addListener(event -> {
      refreshList();
    });
    return profilname;
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
        // Profil erzeugen und speichern
        Suchprofil sp = (Suchprofil) Einstellungen.getDBService()
            .createObject(Suchprofil.class, null);
        sp.setClazz(view.getClass().getName());
        sp.setBezeichnung(name);
        handleSpeichern(sp, true);
        // Da der Dialog nicht geschlossen wird, muss das neue Profil in die
        // Auswahl aufgenommen werden und angezeigt werden
        DBService service = Einstellungen.getDBService();
        DBIterator<Suchprofil> profile = service.createList(Suchprofil.class);
        profile.addFilter("clazz = ?", view.getClass().getName());
        profile.setOrder("ORDER BY bezeichnung");
        profilname.setList(PseudoIterator.asList(profile));
        profilname.setPreselected(sp);
        refreshList();
      }
    }
    catch (OperationCanceledException ex)
    {
      return;
    }
    catch (Exception e)
    {
      // Abbruch
      String text = "Fehler beim Anlegen eines Profils.";
      Logger.error(text, e);
      GUI.getStatusBar().setErrorText(text);
      return;
    }
  }

  // Speichert das ausgewählte Profil mit aktuellen Settings
  private void handleSpeichern(Suchprofil item, boolean neu)
  {
    try
    {
      if (item == null)
      {
        item = (Suchprofil) getProfilname().getValue();
      }
      if (item == null)
      {
        showDialog();
        return;
      }

      if (!neu && !confirm("Profil Speichern",
          "Soll das ausgewählte Profil \"" + item.getBezeichnung()
              + "\" mit den aktuellen Filtern überschrieben werden?"))
      {
        return;
      }

      // Überschreiben eines ausgewählten Suchprofils
      storeSettings(settings, item);
      item.store();
      settings.setAttribute(control.getSettingsPrefix() + "profilid",
          item.getID());
      settings.setAttribute(control.getSettingsPrefix() + "profilname",
          item.getBezeichnung());
      if (!neu)
      {
        refreshList();
      }

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
        showDialog();
        return;
      }

      if (settings.getString(control.getSettingsPrefix() + "profilid", "")
          .equals(item.getID()))
      {
        settings.setAttribute(control.getSettingsPrefix() + "profilid",
            (String) null);
        settings.setAttribute(control.getSettingsPrefix() + "profilname",
            (String) null);
      }
      item.delete();

      // Da der Dialog nicht geschlossen wird, muss ein anderes Profil angezeigt
      // werden
      DBService service = Einstellungen.getDBService();
      DBIterator<Suchprofil> profile = service.createList(Suchprofil.class);
      profile.addFilter("clazz = ?", view.getClass().getName());
      profile.setOrder("ORDER BY bezeichnung");
      profilname.setList(PseudoIterator.asList(profile));
      refreshList();

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
        showDialog();
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
      settings.setAttribute(prefix + "profilid", item.getID());
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

  @Override
  protected Object getData()
  {
    return null;
  }
}
