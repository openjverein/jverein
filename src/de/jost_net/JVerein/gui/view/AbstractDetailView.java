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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.dialogs.ViewVerlassenDialog;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractDetailView extends AbstractView
{

  /**
   * Diese Funktion muss implementiert werden und den Controller zurückliefern
   * 
   * @return Savable das Control
   */
  protected abstract Savable getControl();

  @Override
  public void unbind() throws OperationCanceledException, ApplicationException
  {
    JVereinDBObject o = null;
    try
    {
      boolean error = false;
      try
      {
        o = (JVereinDBObject) getControl().prepareStore();
      }
      catch (RemoteException | ApplicationException e)
      {
        error = true;
        Logger.error("Fehler bei unbind prepareStore", e);
      }

      // Wenn beim prepareStore() eine Exception geworfen wird, ist es
      // warscheinlich, dass etwas ungültiges eingegeben wurde. Also wurde etwas
      // verändert und wir fragen auch nach.
      if (o == null || o.isChanged() || error || getControl().hasChanged())
      {
        ViewVerlassenDialog dialog = new ViewVerlassenDialog(
            AbstractDialog.POSITION_CENTER);

        switch (dialog.open())
        {
          case ViewVerlassenDialog.SPEICHERN:
            try
            {
              getControl().handleStore();
            }
            catch (ApplicationException e)
            {
              // Wir schicken ein Message damit der Eintrag in der History
              // erhalten bleibt
              Application.getMessagingFactory()
                  .getMessagingQueue("jameica.gui.view.unbind.fail")
                  .sendSyncMessage(new QueryMessage());
              GUI.getStatusBar().setErrorText(e.getMessage());
              throw new OperationCanceledException(e);
            }
            // Einen MessageConsumer anhängen, damit die Erfolgsmeldung nach dem
            // Laden der neuen View angezeigt wird.
            Application.getMessagingFactory()
                .getMessagingQueue("jameica.gui.view.bind")
                .registerMessageConsumer(new SaveMessageConsumer());
            break;
          case ViewVerlassenDialog.VERLASSEN:
            break;
          case ViewVerlassenDialog.ABBRECHEN:
            throw new OperationCanceledException();
        }
      }
    }
    catch (OperationCanceledException e)
    {
      // Wir schicken ein Message damit der Eintrag in der History erhalten
      // bleibt
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.gui.view.unbind.fail")
          .sendSyncMessage(new QueryMessage());
      throw new OperationCanceledException(e);
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim testen auf Änderungen: ";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler + e.getMessage());
    }
  }

  private class SaveMessageConsumer implements MessageConsumer
  {

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return null;
    }

    @Override
    public void handleMessage(Message message) throws Exception
    {
      GUI.getStatusBar().setSuccessText("Gespeichert");

      // MessageConsumer wieder entfernen
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.gui.view.bind")
          .unRegisterMessageConsumer(this);
    }

    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
