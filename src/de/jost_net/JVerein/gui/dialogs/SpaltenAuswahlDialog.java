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
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpaltenAuswahlDialog extends AbstractDialog<List<TableColumn>>
{

  private TablePart spaltenList;

  private List<TableColumn> list;

  private List<TableColumn> listOrig;

  private Settings settings;

  private String settingPrefix;

  public SpaltenAuswahlDialog(List<TableColumn> tableColumns,
      String settingPrefix)
  {
    super(SpaltenAuswahlDialog.POSITION_CENTER);
    // Kopieren, damit original nicht verändert wird
    this.listOrig = new ArrayList<>(tableColumns);
    setTitle("Spalten auswählen");
    setSize(400, SWT.DEFAULT);
    settings = new Settings(this.getClass());
    this.settingPrefix = settingPrefix;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup options = new LabelGroup(parent, "");
    options.addPart(getList());

    List<String> auswahl = Arrays
        .asList(settings.getString(settingPrefix + "auswahl", "").split(","));
    if (!auswahl.get(0).isBlank())
    {
      for (TableColumn item : listOrig)
      {
        spaltenList.setChecked(item, auswahl.contains(item.getText()));
      }
    }

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> {
      try
      {
        list = spaltenList.getItems();
        settings.setAttribute(settingPrefix + "auswahl", list.stream()
            .map(item -> item.getText()).collect(Collectors.joining(",")));
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Serverfehler");
      }
      close();
    }, null, true, "ok.png");
    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  private Part getList()
  {
    if (spaltenList != null)
    {
      return spaltenList;
    }
    spaltenList = new JVereinTablePart(listOrig, null);
    spaltenList.addColumn("Name", "text");
    spaltenList.setCheckable(true);
    spaltenList.setRememberColWidths(true);

    return spaltenList;
  }

  @Override
  protected List<TableColumn> getData() throws Exception
  {
    return list;
  }

}
