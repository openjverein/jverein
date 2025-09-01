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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Loeschen eines Eintrags oder Einträge.
 */
public class DeleteAction implements Action
{
  private String name = "";

  private String namen = "";

  private String attribut = "";

  @Override
  public void handleAction(Object context) throws ApplicationException
  {

    JVereinDBObject objekt = null;
    JVereinDBObject[] ote = null;
    int length;
    if (context instanceof JVereinDBObject)
    {
      objekt = (JVereinDBObject) context;
      length = 1;
      try
      {
        name = objekt.getObjektName();
        namen = objekt.getObjektNameMehrzahl();
      }
      catch (RemoteException e)
      {
        // Das kann nicht passieren ist aber nötig wegen der
        // throws RemoteException Deklaration in JVereinDBObject
      }
    }
    else if (context instanceof JVereinDBObject[])
    {
      ote = (JVereinDBObject[]) context;
      if (ote.length == 0)
      {
        throw new ApplicationException("Kein Objekt ausgewählt");
      }
      length = ote.length;
      try
      {
        name = ote[0].getObjektName();
        namen = ote[0].getObjektNameMehrzahl();
      }
      catch (RemoteException e)
      {
        // Das kann nicht passieren ist aber nötig wegen der
        // throws RemoteException Deklaration in JVereinDBObject
      }
    }
    else
    {
      throw new ApplicationException("Kein Objekt ausgewählt");
    }

    // final wegen BackgroundTask
    final JVereinDBObject[] objekte = ote;

    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(name + " löschen");
    d.setText(String.format("Wollen Sie %d %s wirklich löschen?", length,
        (length == 1 ? name : namen)));
    Boolean choice;
    try
    {
      choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
    }
    catch (Exception e1)
    {
      String fehler = "Fehler beim öffnen des Abfrage Dialogs.";
      Logger.error(fehler, e1);
      throw new ApplicationException(fehler);
    }

    // Bei nur einem Objekt direkt löschen
    if (objekt != null)
    {
      try
      {
        if (objekt.isNewObject())
        {
          return;
        }
        attribut = getAttribute(objekt);
        objekt.delete();
        GUI.getStatusBar().setSuccessText(name + " gelöscht.");
      }
      catch (ApplicationException e2)
      {
        String fehler = "Fehler beim Löschen von " + name + " " + attribut
            + ": ";
        GUI.getStatusBar().setErrorText(fehler + e2.getMessage());
      }
      catch (RemoteException e1)
      {
        String fehler = "Fehler beim Löschen von " + name + " " + attribut
            + ". Es wird eventuell von anderen Objekten benutzt.";
        GUI.getStatusBar().setErrorText(fehler);
        Logger.error(fehler, e1);
      }
      return;
    }

    BackgroundTask t = new BackgroundTask()
    {
      private boolean interrupted = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        int count = 0;
        int skip = 0;
        monitor.setStatusText("Lösche " + objekte.length + " " + namen);
        for (JVereinDBObject o : objekte)
        {
          if (isInterrupted())
          {
            throw new OperationCanceledException();
          }
          try
          {
            if (o.isNewObject())
            {
              skip++;
              continue;
            }
            attribut = getAttribute(o);
            o.delete();
            count++;
          }
          catch (ApplicationException e2)
          {
            skip++;
            String fehler = "Fehler beim Löschen von " + name + " " + attribut
                + ": ";
            monitor.setStatusText(fehler + e2.getMessage());
          }
          catch (RemoteException e3)
          {
            skip++;
            String fehler = "Fehler beim Löschen von " + name + " " + attribut
                + ". Es wird eventuell von anderen Objekten benutzt.";
            monitor.setStatusText(fehler);
            Logger.error(fehler, e3);
          }
          monitor.setPercentComplete(100 * (count + skip) / objekte.length);
        }
        monitor.setPercentComplete(100);
        monitor.setStatusText(count + " " + namen + " gelöscht.");
        monitor.setStatusText(skip + " " + namen + " übersprungen.");
      }

      @Override
      public void interrupt()
      {
        interrupted = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return interrupted;
      }
    };
    Application.getController().start(t);
  }

  private String getAttribute(JVereinDBObject objekt)
  {
    Object obj;
    try
    {
      obj = objekt.getAttribute(objekt.getPrimaryAttribute());
      if (obj instanceof String)
      {
        if (objekt.getPrimaryAttribute().equals("id"))
        {
          return "mit Nr. " + (String) obj;
        }
        else
        {
          return (String) obj;
        }
      }
      else
      {
        return "";
      }
    }
    catch (RemoteException e)
    {
      return "";
    }
  }
}
