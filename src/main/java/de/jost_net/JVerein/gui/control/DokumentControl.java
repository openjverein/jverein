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
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentShowAction;
import de.jost_net.JVerein.gui.menu.DokumentMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.DokumentPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.DokumentDetailView;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.server.AbstractJVereinDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class DokumentControl extends AbstractControl implements Savable
{
  private DokumentPart dopa;

  private FileInput datei;

  private JVereinTablePart docsList;

  private Button neuButton;

  private boolean enabled;

  private Settings settings = null;

  private Class<? extends AbstractDokument> clazz;

  public DokumentControl(AbstractView view, boolean enabled,
      Class<? extends AbstractDokument> clazz)
  {
    super(view);
    this.enabled = enabled;
    this.settings = new Settings(this.getClass());
    this.clazz = clazz;
  }

  // Wird nur aus der DokumentDetailView aufgerufen
  private AbstractDokument getDokument() throws RemoteException
  {
    Object doc = getCurrentObject();
    if (doc == null || !(doc instanceof AbstractDokument))
    {
      throw new RemoteException(
          "Programmfehler! Dokument fehlt oder hat falschen Typ");
    }
    return (AbstractDokument) doc;
  }

  // Wird nur aus der DokumentListe aufgerufen
  private AbstractJVereinDBObject getReferenzObject() throws RemoteException
  {
    Object obj = getCurrentObject();
    if (obj == null || !(obj instanceof AbstractJVereinDBObject))
    {
      throw new RemoteException(
          "Programmfehler! Referenz-Object fehlt oder hat falschen Typ");
    }
    return (AbstractJVereinDBObject) obj;
  }

  public FileInput getDatei()
  {
    if (datei != null)
    {
      return datei;
    }
    datei = new FileInput("", false)
    {
      @Override
      protected void customize(FileDialog fd)
      {
        fd.setFilterPath(settings.getString("buchung.dokument", ""));
      }
    };
    return datei;
  }

  public DokumentPart getDokumentPart() throws RemoteException
  {
    if (dopa != null)
    {
      return dopa;
    }
    dopa = new DokumentPart(getDokument());
    return dopa;
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

        GUI.startView(new DokumentDetailView(), doc);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Datenbankzugriff.", e);
      }

    }, null, false, "document-new.png");
    neuButton.setEnabled(enabled);
    return neuButton;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    AbstractDokument dokument = getDokument();
    File file = new File((String) datei.getValue());
    settings.setAttribute("buchung.dokument", file.getParent());
    dokument.setBemerkung((String) dopa.getBemerkung().getValue());
    dokument.setDatum((Date) dopa.getDatum().getValue());
    dokument.setFile(file);
    return dokument;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Dokuments";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
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
