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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.DokumentMessage;
import de.jost_net.JVerein.gui.action.DokumentShowAction;
import de.jost_net.JVerein.gui.menu.DokumentMenu;
import de.jost_net.JVerein.gui.parts.DokumentPart;
import de.jost_net.JVerein.gui.view.DokumentDetailView;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class DokumentControl extends AbstractControl
{

  private AbstractDokument doc;

  private DokumentPart dopa;

  private FileInput datei;

  private TablePart docsList;

  private Button neuButton;

  private Button speichernButton;

  private String verzeichnis;

  private boolean enabled;

  private DokumentMessageConsumer mc = null;

  private Settings settings = null;

  public DokumentControl(AbstractView view, String verzeichnis, boolean enabled)
  {
    super(view);
    this.verzeichnis = verzeichnis;
    this.enabled = enabled;
    this.settings = new Settings(this.getClass());
  }

  private AbstractDokument getDokument() throws RemoteException
  {
    doc = (AbstractDokument) getCurrentObject();
    if (doc == null)
    {
      throw new RemoteException("Programmfehler! Dokument fehlt");
    }
    return doc;
  }

  public FileInput getDatei()
  {
    if (datei != null)
    {
      return datei;
    }
    datei = new PathFileInput("", false,
        settings.getString("buchung.dokument", ""));
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

  public Button getNeuButton(final AbstractDokument doc)
  {
    neuButton = new Button("Neues Dokument", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        GUI.startView(new DokumentDetailView(verzeichnis), doc);
      }
    }, null, false, "document-new.png");
    neuButton.setEnabled(enabled);
    return neuButton;
  }

  public Button getSpeichernButton(final String verzeichnis)
  {
    speichernButton = new Button("Speichern", context -> speichern(verzeichnis),
        null, true, "document-save.png");
    return speichernButton;
  }

  private void speichern(String verzeichnis) throws ApplicationException
  {
    try
    {
      File file = new File((String) datei.getValue());
      if (file.isDirectory())
      {
        throw new ApplicationException(
            "Verzeichnisse können nicht gespeichert werden.");
      }
      if (!file.exists())
      {
        throw new ApplicationException("Datei existiert nicht");
      }

      settings.setAttribute("buchung.dokument", file.getParent());

      dokumentSpeichern(verzeichnis, file,
          (String) dopa.getBemerkung().getValue(),
          (Date) dopa.getDatum().getValue(), doc);
      speichernButton.setEnabled(false);
      GUI.getStatusBar().setSuccessText("Dokument gespeichert");
    }
    catch (IOException e)
    {
      throw new ApplicationException("Allgemeiner Ein-/Ausgabe-Fehler");
    }
  }

  /**
   * Speichert des Dokuments
   * 
   * @throws ApplicationException
   */
  private void dokumentSpeichern(String verzeichnis, File file,
      String bemerkung, Date datum, AbstractDokument doc)
      throws ApplicationException
  {
    try (FileInputStream fis = new FileInputStream(file);)
    {
      if (fis.available() <= 0)
      {
        throw new ApplicationException("Datei ist leer");
      }
      // Dokument speichern
      String locverz = verzeichnis + doc.getReferenz();
      QueryMessage qm = new QueryMessage(locverz, fis);
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);
      // Satz in die DB schreiben
      doc.setBemerkung(
          bemerkung.length() > 50 ? bemerkung.substring(0, 50) : bemerkung);
      String uuid = qm.getData().toString();
      doc.setUUID(uuid);
      doc.setDatum(datum);
      doc.store();
      // Zusätzliche Eigenschaft speichern
      Map<String, String> map = new HashMap<>();
      map.put("filename", file.getName());
      qm = new QueryMessage(uuid, map);
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.putmeta").sendMessage(qm);
    }
    catch (FileNotFoundException e)
    {
      throw new ApplicationException("Datei existiert nicht");
    }
    catch (IOException e)
    {
      throw new ApplicationException("Allgemeiner Ein-/Ausgabe-Fehler");
    }
  }

  public Part getDokumenteList(AbstractDokument doc) throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<AbstractDokument> docs = service.createList(doc.getClass());
    docs.addFilter("referenz = ?", new Object[] { doc.getReferenz() });
    docs.setOrder("ORDER BY datum desc");

    docsList = new TablePart(docs, new DokumentShowAction());
    docsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    docsList.addColumn("Bemerkung", "bemerkung");
    docsList.setRememberColWidths(true);
    docsList.setContextMenu(new DokumentMenu(enabled));
    docsList.setRememberOrder(true);
    docsList.setMulti(true);
    this.mc = new DokumentMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    return docsList;
  }

  public void setDragDrop(Composite composit,
      Class<? extends AbstractDokument> dokumentClass)
  {
    DropTarget target = new DropTarget(composit,
        DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener()
    {

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

      public void drop(DropTargetEvent event)
      {
        if (event.data == null)
        {
          event.detail = DND.DROP_NONE;
          GUI.getStatusBar()
              .setErrorText("Fehler bem Hinzufügen der Datei(en)");
          return;
        }
        try
        {
          for (String filename : (String[]) event.data)
          {
            doc = Einstellungen.getDBService().createObject(dokumentClass,
                null);
            doc.setReferenz(
                Long.valueOf(((GenericObject) getCurrentObject()).getID()));
            File file = new File(filename);
            dokumentSpeichern(verzeichnis + ".", file, file.getName(),
                new Date(), doc);
          }
          refreshTable();
        }
        catch (ApplicationException | RemoteException e)
        {
          GUI.getStatusBar()
              .setErrorText("Fehler bem Hinzufügen der Datei(en)");
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

  public void refreshTable() throws RemoteException
  {
    docsList.removeAll();
    DBIterator<AbstractDokument> docs = Einstellungen.getDBService()
        .createList(doc.getClass());
    docs.addFilter("referenz = ?", new Object[] { doc.getReferenz() });
    docs.setOrder("ORDER BY datum desc");
    while (docs.hasNext())
    {
      docsList.addItem(docs.next());
    }
    docsList.sort();
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class DokumentMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[] { DokumentMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            DokumentMessage dm = (DokumentMessage) message;
            doc = (AbstractDokument) dm.getObject();

            if (docsList == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory()
                  .unRegisterMessageConsumer(DokumentMessageConsumer.this);
              return;
            }
            refreshTable();
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns
            // wieder
            Logger.error("Dokumenteliste konnte nicht aktualisiert werden", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(DokumentMessageConsumer.this);
          }
        }
      });
    }
  }

  /**
   * FileInput mit Angabe des Ordners
   */
  private class PathFileInput extends FileInput
  {

    private String path;

    public PathFileInput(String file, boolean save, String path)
    {
      super(file, save);
      this.path = path;
    }

    @Override
    protected void customize(FileDialog fd)
    {
      fd.setFilterPath(path);
    }
  }

  public void deregisterDocumentConsumer()
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(mc);
  }
}
