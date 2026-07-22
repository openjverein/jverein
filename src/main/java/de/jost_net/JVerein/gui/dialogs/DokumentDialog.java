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

import java.io.File;
import java.rmi.RemoteException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.rmi.AbstractDokument;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, zur Bearbeitung von Dokument-Infos
 */
public class DokumentDialog extends AbstractDialog<Boolean>
{

  private AbstractDokument dok = null;

  private Settings settings;

  private TextInput bemerkung;

  private FileInput datei;

  public DokumentDialog(AbstractDokument dok)
  {
    super(AbstractDialog.POSITION_CENTER);
    this.dok = dok;
    this.settings = new Settings(this.getClass());

    setTitle("Dokument-Infos");
    setSize(850, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    if (dok.isNewObject())
    {
      LabelGroup grDokument = new LabelGroup(parent, "Dokument");
      grDokument.addLabelPair("Datei", getDatei());
    }
    LabelGroup group = new LabelGroup(parent, "Infos");
    group.addLabelPair("Bemerkung", getBemerkung());
    if (dok.getPfad() != null)
    {
      group.addLabelPair("Pfad", getPfad());
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(new Button("Speichern", c -> speichern(), null, true,
        "document-save.png"));

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    buttons.paint(parent);
  }

  private void speichern() throws ApplicationException
  {
    try
    {
      if (datei != null)
      {
        File file = new File((String) datei.getValue());
        settings.setAttribute("lastdir", file.getParent());
        dok.setFile(file);
      }
      dok.setBemerkung((String) bemerkung.getValue());
      dok.store();
      close();
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Speichern");
      Logger.error("Fehler beim Speichern", e);
    }
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return true;
  }

  private TextInput getBemerkung() throws RemoteException
  {
    bemerkung = new TextInput(dok.getBemerkung(), 50);
    return bemerkung;
  }

  private Input getDatei()
  {
    datei = new FileInput("", false)
    {
      @Override
      protected void customize(FileDialog fd)
      {
        fd.setFilterPath(settings.getString("lastdir", ""));
      }
    };
    datei.setMandatory(true);
    return datei;
  }

  private Input getPfad() throws RemoteException
  {
    Input pfad = new TextInput(dok.getRootDir() + dok.getPfad());
    pfad.disable();
    return pfad;
  }
}
