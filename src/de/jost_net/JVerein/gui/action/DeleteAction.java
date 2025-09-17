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

import org.eclipse.swt.graphics.Image;

import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
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
  protected String name = "";

  protected String namen = "";

  private String attribut = "";

  private Integer selection = YesNoCancelDialog.CANCEL;

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    JVereinDBObject[] ote = null;
    if (context instanceof JVereinDBObject)
    {
      ote = new JVereinDBObject[] { (JVereinDBObject) context };
      attribut = getAttribute(ote[0]);
    }
    else if (context instanceof JVereinDBObject[] && supportsMulti())
    {
      ote = (JVereinDBObject[]) context;
    }
    else if (context instanceof JVereinDBObject[] && !supportsMulti())
    {
      // Das sollte nicht passieren, da es schon im Menü oder der Tabelle
      // abgefangen wird. Sicherheitshalber wird es auch hier geprüft.
      throw new ApplicationException(
          "Auswahl mehrerer Einträge für dieses Objekt ist nicht unterstützt.");
    }
    else
    {
      throw new ApplicationException("Kein Objekt ausgewählt.");
    }

    if (ote.length == 0)
    {
      throw new ApplicationException("Kein Objekt ausgewählt.");
    }

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

    // final wegen BackgroundTask
    final JVereinDBObject[] objekte = ote;

    // Den Text un Button Info für den Dialog holen.
    // Er kann von abgeleiteten Klassen geliefert werden
    String text = "";
    boolean mitNo = false;
    Image image = null;
    try
    {
      text = getText(objekte);
      mitNo = getMitNo(objekte);
      image = getImage();
    }
    catch (ApplicationException e)
    {
      String fehler = "Fehler beim Löschen von " + name + " " + attribut + ": ";
      GUI.getStatusBar().setErrorText(fehler + e.getMessage());
      return;
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen von " + name + " " + attribut + ".";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
      return;
    }

    YesNoCancelDialog d = new YesNoCancelDialog(YesNoDialog.POSITION_CENTER,
        mitNo);
    d.setTitle((objekte.length > 1 ? namen : name) + " löschen");
    d.setText(text);
    if (image != null)
    {
      d.setSideImage(image);
    }
    try
    {
      selection = (Integer) d.open();
      if (selection == YesNoCancelDialog.CANCEL)
      {
        throw new OperationCanceledException();
      }
    }
    catch (OperationCanceledException ex)
    {
      throw ex;
    }
    catch (Exception e1)
    {
      String fehler = "Fehler beim öffnen des Abfrage Dialogs.";
      Logger.error(fehler, e1);
      throw new ApplicationException(fehler);
    }

    // Bei nur einem Objekt direkt löschen
    if (objekte.length == 1)
    {
      try
      {
        if (objekte[0].isNewObject() && !isNewAllowed())
        {
          return;
        }
        attribut = getAttribute(objekte[0]);
        doDelete(objekte[0], selection);
        doFinally();
        GUI.getStatusBar().setSuccessText(name + " gelöscht.");
      }
      catch (ApplicationException e1)
      {
        String fehler = "Fehler beim Löschen von " + name + " " + attribut
            + ": ";
        GUI.getStatusBar().setErrorText(fehler + e1.getMessage());
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
            if (o.isNewObject() && !isNewAllowed())
            {
              skip++;
              continue;
            }
            attribut = getAttribute(o);
            doDelete(o, selection);
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
        try
        {
          doFinally();
        }
        catch (ApplicationException e2)
        {
          skip++;
          String fehler = "Fehler beim Löschen von " + namen + ": ";
          monitor.setStatusText(fehler + e2.getMessage());
        }
        catch (RemoteException e3)
        {
          skip++;
          String fehler = "Fehler beim Löschen von " + namen;
          monitor.setStatusText(fehler);
          Logger.error(fehler, e3);
        }
        monitor.setPercentComplete(100);
        monitor.setStatusText(count + " " + namen + " gelöscht.");
        if (skip > 0)
        {
          monitor.setStatusText(skip + " " + namen + " übersprungen.");
        }
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

  /**
   * Liefert das primary Attribut des Objekt für die Ausgabe bei
   * Fehlermeldungen.
   * 
   * @param object
   *          Das zu löschende Objekte
   * @return Das primary Attribut des Objekt
   */
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
          return "'" + (String) obj + "'";
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

  /**
   * Die Methode liefert den Text für die Ausgabe im YesNoCancel Dialog. Die
   * Methode kann von abgeleiteten Klassen überschrieben werden.
   * 
   * @param object[]
   *          Die zu löschenden Objekte
   * @throws RemoteException
   * @throws ApplicationException
   * @return Der Text für die Ausgabe im YesNoCancel Dialog
   */
  protected String getText(JVereinDBObject object[])
      throws RemoteException, ApplicationException
  {
    return String.format("Wollen Sie %d %s wirklich löschen?", object.length,
        (object.length == 1 ? name : namen));
  }

  /**
   * Die Methode führt die Löschoperation des Objekts durch. Die Methode kann
   * von abgeleiteten Klassen überschrieben werden.
   * 
   * @param object
   *          Das zu löschende Objekt
   * @param selection
   *          Selektierte Auswahl im YesNoCancel Dialog
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    object.delete();
  }

  /**
   * Die Methode wird nach doDelete aufgerufen, bei Multi Selection nach dem
   * letzten doDelete. Die Methode kann von abgeleiteten Klassen überschrieben
   * werden.
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  protected void doFinally() throws RemoteException, ApplicationException
  {
    // Ist für abgeleitete Klassen gedacht
  }

  /**
   * Die Methode ibt zurück ob der YesNoCancel Dialog mit dem No Button
   * angezeigt werden soll. Die Methode kann von abgeleiteten Klassen
   * überschrieben werden.
   * 
   * @param object[]
   *          Die zu löschenden Objekte
   * @return YesNoCancel Dialog mit oder ohne No Button
   */
  protected boolean getMitNo(JVereinDBObject object[])
  {
    return false;
  }

  /**
   * Die Methode liefert ein Image für die Anzeige im YesNoCancel Dialog. Die
   * Methode kann von abgeleiteten Klassen überschrieben werden.
   * 
   * @return Das Image für die Anzeige im YesNoCancel Dialog
   */
  protected Image getImage()
  {
    return null;
  }

  /**
   * Die Methode liefert zurück ob auch neue Objekte gelöscht werden können. Die
   * Methode kann von abgeleiteten Klassen überschrieben werden.
   * 
   * @return Neue Objekte löschen Ja/Nein
   */
  protected boolean isNewAllowed()
  {
    return false;
  }

  /**
   * Support für Multi Selection. Die Methode kann von abgeleiteten Klassen
   * überschrieben werden.
   * 
   * @return Ob Multi Selection erlaubt ist
   */
  protected boolean supportsMulti()
  {
    return true;
  }
}
