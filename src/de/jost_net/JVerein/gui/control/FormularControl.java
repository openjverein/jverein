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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.FormularLinkFormatter;
import de.jost_net.JVerein.gui.formatter.FormularartFormatter;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.menu.FormularMenu;
import de.jost_net.JVerein.gui.view.FormularDetailView;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.server.FormularImpl;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularControl extends FormularPartControl
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart formularList;

  private TextInput bezeichnung;

  private SelectInput art;

  private FileInput datei;

  private Formular formular;

  private IntegerInput zaehler;

  private SelectInput formlink;

  public FormularControl(AbstractView view, Formular formular)
  {
    super(view, formular);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Formular getFormular()
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
    if (!Einstellungen.getEinstellung().getSpendenbescheinigungenAnzeigen())
    {
      if (aktuelleFormularArt != FormularArt.SPENDENBESCHEINIGUNG)
      {
        list.remove(FormularArt.SPENDENBESCHEINIGUNG);
      }
      if (aktuelleFormularArt != FormularArt.SAMMELSPENDENBESCHEINIGUNG)
      {
        list.remove(FormularArt.SAMMELSPENDENBESCHEINIGUNG);
      }
    }
    art = new SelectInput(list, aktuelleFormularArt);
    return art;
  }

  public FileInput getDatei()
  {
    if (datei != null)
    {
      return datei;
    }
    datei = new FileInput("", false, new String[] { "*.pdf", "*.PDF" });
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
    Integer currentlyLinkedFormId = currentForm.getFormlink();
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
      for (int i=0;i<size;++i)
      {
        Object object = list.get(i);
        if (object == null) continue;
        // Cast to FormularImpl
        FormularImpl formimpl = (FormularImpl) object;
        // Remove current form object and stop comparing
        if (Integer.valueOf(formimpl.getID()) == Integer.valueOf(currentForm.getID()))
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

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    try
    {
      Formular f = getFormular();
      f.setBezeichnung((String) getBezeichnung(true).getValue());
      FormularArt fa = (FormularArt) getArt().getValue();
      f.setArt(fa);
      String dat = (String) getDatei().getValue();
      if (dat.length() > 0)
      {
        FileInputStream fis = new FileInputStream(dat);
        byte[] b = new byte[fis.available()];
        fis.read(b);
        fis.close();
        f.setInhalt(b);
      }

      int newZaehler = (int) getZaehler().getValue();
      f.setZaehler(newZaehler);
      f.setZaehlerToFormlink(newZaehler);

      Formular fl = (Formular) getFormlink().getValue();
      if (fl != null)
      {
        f.setFormlink(Integer.valueOf(fl.getID()));
      }
      else
      {
        f.setFormlink(null);
      }

      f.store();
      GUI.getStatusBar().setSuccessText("Formular gespeichert");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern des Formulares";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    catch (ApplicationException e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (FileNotFoundException e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText("Datei nicht gefunden");
    }
    catch (IOException e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText("Ein-/Ausgabe-Fehler");
    }
  }

  public Part getFormularList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Formular> formulare = service.createList(Formular.class);
    formulare.setOrder("ORDER BY art, bezeichnung");

    formularList = new TablePart(formulare,
        new EditAction(FormularDetailView.class));
    formularList.addColumn("Bezeichnung", "bezeichnung");
    formularList.addColumn("Art", "art", new FormularartFormatter(), false,
        Column.ALIGN_LEFT);
    formularList.addColumn("Fortlaufende Nr.", "zaehler");
    formularList.addColumn("Verknüpft mit", "formlink",
        new FormularLinkFormatter());
    formularList.setRememberColWidths(true);
    formularList.setContextMenu(new FormularMenu(this));
    formularList.setRememberOrder(true);
    formularList.removeFeature(FeatureSummary.class);
    formularList.setMulti(true);
    return formularList;
  }

  public void refreshFormularTable() throws RemoteException
  {
    formularList.removeAll();
    DBIterator<Formular> formulare = Einstellungen.getDBService()
        .createList(Formular.class);
    formulare.setOrder("ORDER BY art, bezeichnung");
    while (formulare.hasNext())
    {
      formularList.addItem(formulare.next());
    }
    formularList.sort();
  }

}
