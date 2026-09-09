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
package de.jost_net.JVerein.gui.parts;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Messaging.BelegRemoveMessage;
import de.jost_net.JVerein.gui.action.BelegNewAction;
import de.jost_net.JVerein.gui.action.BelegZuordnenAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.BelegMenu;
import de.jost_net.JVerein.gui.util.DragnDropUtil;
import de.jost_net.JVerein.gui.view.BelegDetailView;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.IBeleg;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class BelegPart implements Part
{
  private AutoUpdateTablePart docsList;

  private IBeleg currentObject;

  private BelegMessageConsumer consumer = new BelegMessageConsumer();

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup grDokument = new LabelGroup(parent, "Dokumente", true);

    grDokument.getComposite().setLayout(new GridLayout(1, false));

    ButtonArea butts = new ButtonArea();
    butts.addButton("Beleg zuordnen", new BelegZuordnenAction(getTablePart()),
        getCurrentObject(), false, "document-new.png");
    butts.addButton("Neu", new BelegNewAction(), getCurrentObject(), false,
        "document-new.png");
    butts.paint(grDokument.getComposite());
    grDokument.addPart(getTablePart());

    grDokument.getComposite().addDisposeListener(e -> {
      Application.getMessagingFactory().unRegisterMessageConsumer(consumer);
    });

    DragnDropUtil.setDragDrop(grDokument.getComposite(), f -> addFile(f));

    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 150;
    grDokument.getComposite().setLayoutData(gridData);

    Application.getMessagingFactory().registerMessageConsumer(consumer);
  }

  private IBeleg getCurrentObject()
  {
    if (currentObject != null)
    {
      return currentObject;
    }
    currentObject = (IBeleg) GUI.getCurrentView().getCurrentObject();
    return currentObject;
  }

  public TablePart getTablePart() throws RemoteException
  {
    if (docsList != null)
    {
      return docsList;
    }
    docsList = new AutoUpdateTablePart(getCurrentObject().getBelegList(), null);
    docsList.setTableName("Dokumente");
    docsList.addColumn("Belegnummer", "belegnummer");
    docsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    docsList.addColumn("Bemerkung", "bemerkung");
    docsList.addColumn("Pfad", "vollpfad");
    docsList.addColumn("Buchungen", "buchungsdokumentbuchung.size");
    docsList.setContextMenu(new BelegMenu(docsList, getCurrentObject()));
    docsList.setMulti(true);
    docsList.setAction(new EditAction(BelegDetailView.class, docsList));

    return docsList;
  }

  private void addFile(String filename)
  {
    try
    {
      if (getCurrentObject() instanceof JVereinDBObject
          && ((JVereinDBObject) getCurrentObject()).isNewObject())
      {
        throw new ApplicationException(
            ((JVereinDBObject) getCurrentObject()).getObjektName()
                + " bitte erst speichern.");
      }

      DBTransaction.starten();
      BuchungDokument document = Einstellungen.getDBService()
          .createObject(BuchungDokument.class, null);
      File file = new File(filename);

      document.setBemerkung(file.getName());
      document.setDatum(new Date());

      document.setFile(file);
      document.store();
      getCurrentObject().addBeleg(document);

      DBTransaction.commit();
      docsList.addItem(document);
    }
    catch (RemoteException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar()
          .setErrorText("Fehler beim Hinzufügen der Datei: " + e.getMessage());
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class BelegMessageConsumer implements MessageConsumer
  {

    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class<?>[] { BelegRemoveMessage.class };
    }

    @Override
    public void handleMessage(Message message) throws Exception
    {
      GUI.getDisplay().syncExec(() -> {
        if (docsList != null)
        {
          BelegRemoveMessage m = (BelegRemoveMessage) message;
          docsList.removeItem(m.getObject());
        }
      });
    }

    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
