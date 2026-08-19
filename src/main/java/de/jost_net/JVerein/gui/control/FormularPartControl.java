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

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.FormularfeldNeuAction;
import de.jost_net.JVerein.gui.action.FormularfelderExportAction;
import de.jost_net.JVerein.gui.action.FormularfelderImportAction;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.menu.FormularfeldMenu;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.FormularfeldDetailView;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.server.FormularImpl;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularPartControl extends VorZurueckControl implements Savable
{

  private TextInput bezeichnung;

  private SelectInput art;

  private FileInput datei;

  private Formular formular;

  private IntegerInput zaehler;

  private SelectInput formlink;

  private ButtonRtoL exportButton;

  private ButtonRtoL importButton;

  private ButtonRtoL neuButton;

  protected JVereinTablePart formularfelderList;

  public FormularPartControl(AbstractView view)
  {
    super(view);
  }

  private Formular getFormular()
  {
    if (formular != null)
    {
      return formular;
    }
    formular = (Formular) getCurrentObject();
    return formular;
  }

  public TextInput getBezeichnung(boolean withFocus) throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getFormular().getBezeichnung(), 50);
    if (withFocus)
    {
      bezeichnung.focus();
    }
    bezeichnung.setMandatory(true);
    return bezeichnung;
  }

  public SelectInput getArt() throws RemoteException
  {
    if (art != null)
    {
      return art;
    }
    FormularArt aktuelleFormularArt = getFormular().getArt();
    ArrayList<FormularArt> list = new ArrayList<FormularArt>(
        Arrays.asList(FormularArt.values()));
    if (!(Boolean) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
    {
      if (aktuelleFormularArt != FormularArt.SPENDENBESCHEINIGUNG)
      {
        list.remove(FormularArt.SPENDENBESCHEINIGUNG);
      }
      if (aktuelleFormularArt != FormularArt.SAMMELSPENDENBESCHEINIGUNG)
      {
        list.remove(FormularArt.SAMMELSPENDENBESCHEINIGUNG);
      }
      if (aktuelleFormularArt != FormularArt.SACHSPENDENBESCHEINIGUNG)
      {
        list.remove(FormularArt.SACHSPENDENBESCHEINIGUNG);
      }
    }
    if (!(Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      if (aktuelleFormularArt != FormularArt.RECHNUNG)
      {
        list.remove(FormularArt.RECHNUNG);
      }
      if (aktuelleFormularArt != FormularArt.MAHNUNG)
      {
        list.remove(FormularArt.MAHNUNG);
      }
    }
    art = new SelectInput(list, aktuelleFormularArt);
    art.addListener(event -> {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      boolean enabled = art.getValue() != FormularArt.HINTERGRUND;
      importButton.setEnabled(enabled);
      exportButton.setEnabled(enabled);
      neuButton.setEnabled(enabled);
    });
    return art;
  }

  public FileInput getDatei() throws RemoteException
  {
    if (datei != null)
    {
      return datei;
    }
    datei = new FileInput("", false, new String[] { "*.pdf", "*.PDF" });
    if (getFormular().isNewObject())
    {
      datei.setMandatory(true);
    }
    return datei;
  }

  public IntegerInput getZaehler() throws RemoteException
  {
    if (zaehler != null)
    {
      return zaehler;
    }
    zaehler = new IntegerInput(getFormular().getZaehler());

    // Deactivate the input field if form is linked to another form
    if (getFormular().getFormlink() > 0)
    {
      zaehler.setEnabled(false);
    }
    return zaehler;
  }

  public SelectInput getFormlink() throws RemoteException
  {
    if (formlink != null)
    {
      return formlink;
    }

    Formular currentForm = getFormular();
    Long currentlyLinkedFormId = currentForm.getFormlink();
    // Create select box
    if (currentlyLinkedFormId != 0)
    {
      formlink = new FormularInput(null, currentlyLinkedFormId.toString());
    }
    else
    {
      formlink = new FormularInput(null);
    }

    // Remove current form from select list
    if (currentForm.getID() != null)
    {
      @SuppressWarnings("unchecked")
      List<SelectInput> list = formlink.getList();
      int size = list.size();
      for (int i = 0; i < size; ++i)
      {
        Object object = list.get(i);
        if (object == null)
          continue;
        // Cast to FormularImpl
        FormularImpl formimpl = (FormularImpl) object;
        // Remove current form object and stop comparing
        if (formimpl.getID().equals(currentForm.getID()))
        {
          list.remove(i);
          formlink.setList(list);
          break;
        }
      }
    }

    // Deactivate the select box if it has linked forms
    if (currentForm.hasFormlinks())
    {
      formlink.setPleaseChoose("Verknüpft");
      formlink.disable();
    }
    else
    {
      formlink.setPleaseChoose("Keine");
    }
    return formlink;
  }

  public ButtonRtoL getExportButton() throws RemoteException
  {
    if (exportButton != null)
    {
      return exportButton;
    }
    exportButton = new ButtonRtoL("Export", new FormularfelderExportAction(),
        formular, false, "document-save.png");
    if (getFormular().getArt() == FormularArt.HINTERGRUND)
    {
      exportButton.setEnabled(false);
    }
    return exportButton;
  }

  public ButtonRtoL getImportButton() throws RemoteException
  {
    if (importButton != null)
    {
      return importButton;
    }
    importButton = new ButtonRtoL("Import",
        new FormularfelderImportAction(this), formular, false,
        "file-import.png");
    if (getFormular().getArt() == FormularArt.HINTERGRUND)
    {
      importButton.setEnabled(false);
    }
    return importButton;
  }

  public ButtonRtoL getNeuButton() throws RemoteException
  {
    if (neuButton != null)
    {
      return neuButton;
    }
    neuButton = new ButtonRtoL("Neu", new FormularfeldNeuAction(), formular,
        false, "document-new.png");
    if (getFormular().getArt() == FormularArt.HINTERGRUND)
    {
      neuButton.setEnabled(false);
    }
    return neuButton;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Formular f = getFormular();
    f.setBezeichnung((String) getBezeichnung(true).getValue());
    FormularArt fa = (FormularArt) getArt().getValue();
    f.setArt(fa);
    f.setZaehler((int) getZaehler().getValue());

    Formular fl = (Formular) getFormlink().getValue();
    if (fl != null)
    {
      f.setFormlink(Long.valueOf(fl.getID()));
    }
    else
    {
      f.setFormlink(null);
    }
    return f;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Formular f = (Formular) prepareStore();
      f.setZaehlerToFormlink((int) getZaehler().getValue());
      String dat = (String) getDatei().getValue();
      if (dat.length() > 0)
      {
        FileInputStream fis = new FileInputStream(dat);
        byte[] b = new byte[fis.available()];
        fis.read(b);
        fis.close();
        f.setInhalt(b);
      }

      f.store();
    }
    catch (IOException e)
    {
      String fehler = "Fehler beim Speichern des Formulares";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (formularfelderList != null)
    {
      return formularfelderList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Formularfeld> formularfelder = service
        .createList(Formularfeld.class);
    formularfelder.addFilter("formular = ?", new Object[] { formular.getID() });
    formularfelder.setOrder("ORDER BY seite, x, y");

    formularfelderList = new JVereinTablePart(formularfelder,
        new EditAction(FormularfeldDetailView.class));
    formularfelderList.addColumn("Name", "name", o -> {
      String s = (String) o;
      if (s.contains("\n"))
      {
        return s.substring(0, s.indexOf("\n"));
      }
      else
      {
        return s;
      }
    });
    formularfelderList.addColumn("Seite", "seite");
    formularfelderList.addColumn("Von links", "x");
    formularfelderList.addColumn("Von unten", "y");
    formularfelderList.addColumn("Schriftart", "font");
    formularfelderList.addColumn("Schriftgröße", "fontsize");
    formularfelderList.addColumn("Ausrichtung", "ausrichtung");

    formularfelderList.setContextMenu(new FormularfeldMenu());
    formularfelderList.removeFeature(FeatureSummary.class);
    formularfelderList.setMulti(true);
    return formularfelderList;
  }

  public void refreshTable() throws RemoteException
  {
    formularfelderList.removeAll();
    GenericIterator<Formularfeld> formularfelder = formular
        .getFormularfelder(0);
    while (formularfelder.hasNext())
    {
      formularfelderList.addItem(formularfelder.next());
    }
    formularfelderList.sort();
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARFELDER_TITEL,
        getBezeichnungString());
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARFELDER_SUBTITEL,
        getBezeichnungString());
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARFELDER_DATEINAME,
        getBezeichnungString());
  }

  private String getBezeichnungString()
  {
    String bezeichnung = "";
    try
    {
      bezeichnung = getBezeichnung(false).getValue().toString();
    }
    catch (RemoteException e)
    {
      Logger.error("Kann Bezeichnung nicht lesen", e);
    }
    return bezeichnung;
  }
}
