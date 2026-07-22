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

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentShowAction;
import de.jost_net.JVerein.gui.dialogs.DokumentDialog;
import de.jost_net.JVerein.gui.menu.DokumentMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.server.AbstractJVereinDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class DokumentControl
{
  private JVereinTablePart docsList;

  private Button neuButton;

  private boolean enabled;

  private Class<? extends AbstractDokument> clazz;

  public DokumentControl(boolean enabled,
      Class<? extends AbstractDokument> clazz)
  {
    this.enabled = enabled;
    this.clazz = clazz;
  }

  private AbstractJVereinDBObject getReferenzObject() throws RemoteException
  {
    Object obj = GUI.getCurrentView().getCurrentObject();
    if (obj == null || !(obj instanceof AbstractJVereinDBObject))
    {
      throw new RemoteException(
          "Programmfehler! Referenz-Object fehlt oder hat falschen Typ");
    }
    return (AbstractJVereinDBObject) obj;
  }

  public Button getNeuButton()
  {
    neuButton = new Button("Neues Dokument", context -> {
      try
      {
        AbstractJVereinDBObject object = getReferenzObject();
        if (object.isNewObject())
        {
          throw new ApplicationException(
              object.getObjektName() + " bitte erst speichern.");
        }

        AbstractDokument doc = (AbstractDokument) Einstellungen.getDBService()
            .createObject(clazz, null);
        doc.setReferenz(Long.valueOf(object.getID()));

        new DokumentDialog(doc).open();
        refreshTable();
      }
      catch (ApplicationException | OperationCanceledException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Dokument-Dialog.", e);
        throw new ApplicationException("Fehler beim Dokument-Dialog.");
      }

    }, null, false, "document-new.png");
    neuButton.setEnabled(enabled);
    return neuButton;
  }

  public JVereinTablePart getDokumenteList() throws RemoteException
  {
    if (docsList != null)
    {
      return docsList;
    }
    docsList = new AutoUpdateTablePart(getList(), new DokumentShowAction());
    docsList.setTableName("Dokumente");
    docsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    docsList.addColumn("Bemerkung", "bemerkung");
    docsList.addColumn("Pfad", "vollpfad");
    docsList.setContextMenu(new DokumentMenu(enabled));
    docsList.setMulti(true);

    return docsList;
  }

  private DBIterator<AbstractDokument> getList() throws RemoteException
  {
    DBIterator<AbstractDokument> docs = Einstellungen.getDBService()
        .createList(clazz);
    docs.addFilter("referenz = ?", getReferenzObject().getID());

    docs.setOrder("ORDER BY datum desc");
    return docs;
  }

  public void setDragDrop(Composite composit)
  {
    DropTarget target = new DropTarget(composit,
        DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener()
    {

      @Override
      public void dragEnter(DropTargetEvent event)
      {
        if (event.detail == DND.DROP_DEFAULT)
        {
          if ((event.operations & DND.DROP_COPY) != 0)
            event.detail = DND.DROP_COPY;
          else
            event.detail = DND.DROP_NONE;
        }
        for (int i = 0; i < event.dataTypes.length; i++)
        {
          if (fileTransfer.isSupportedType(event.dataTypes[i]))
          {
            event.currentDataType = event.dataTypes[i];
            // files should only be copied
            if (event.detail != DND.DROP_COPY)
              event.detail = DND.DROP_NONE;
            break;
          }
        }
      }

      @Override
      public void drop(DropTargetEvent event)
      {
        if (event.data == null)
        {
          event.detail = DND.DROP_NONE;
          GUI.getStatusBar().setErrorText("Fehler bem Hinzufügen der Datei");
          return;
        }
        try
        {
          AbstractJVereinDBObject object = getReferenzObject();
          if (object.isNewObject())
          {
            throw new ApplicationException(
                object.getObjektName() + " bitte erst speichern.");
          }
          for (String filename : (String[]) event.data)
          {
            AbstractDokument document = Einstellungen.getDBService()
                .createObject(clazz, null);
            document.setReferenz(Long.valueOf(object.getID()));
            File file = new File(filename);

            document.setBemerkung(file.getName());
            document.setDatum(new Date());
            document.setFile(file);
            document.store();
          }
          refreshTable();
        }
        catch (ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (RemoteException e)
        {
          GUI.getStatusBar().setErrorText("Fehler bem Hinzufügen der Datei");
        }
      }

      @Override
      public void dragLeave(DropTargetEvent event)
      {
      }

      @Override
      public void dragOperationChanged(DropTargetEvent event)
      {
      }

      @Override
      public void dragOver(DropTargetEvent event)
      {
      }

      @Override
      public void dropAccept(DropTargetEvent event)
      {
      }
    });
  }

  private void refreshTable() throws RemoteException
  {
    docsList.removeAll();
    DBIterator<AbstractDokument> docs = getList();
    while (docs.hasNext())
    {
      docsList.addItem(docs.next());
    }
    docsList.sort();
  }
}
