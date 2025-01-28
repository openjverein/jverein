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

import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchung.
 */
public class BuchungDeleteAction implements Action
{
  private boolean splitbuchung;

  private BuchungsControl control;

  public BuchungDeleteAction(BuchungsControl control, boolean splitbuchung)
  {
    this.splitbuchung = splitbuchung;
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Buchung) && !(context instanceof Buchung[])))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      else if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b == null)
      {
        return;
      }
      if (b.length == 0)
      {
        return;
      }
      if (b[0].isNewObject())
      {
        return;
      }
      
      // Check ob einer der Buchungen
      // eine Spendenbescheinigung zugeordnet ist
      boolean spendenbescheinigung = false;
      for (Buchung bu : b)
      {
        if (bu.getSpendenbescheinigung() != null)
        {
          spendenbescheinigung = true;
          break;
        }
      }
      
      String text = "";
      if (!spendenbescheinigung)
      {
        text = "Wollen Sie diese Buchung" + (b.length > 1 ? "en" : "")
            + " wirklich löschen?";
      }
      else
      {
        if (b.length == 1)
        {
         text = "Die Buchung gehört zu einer Spendenbescheinigung.\n"
              + "Sie können nur zusammen gelöscht werden.\n"
              + "Beide löschen?";
        }
        else
        {
          text = "Mindestens eine Buchung gehört zu einer Spendenbescheinigung.\n"
              + "Sie können nur zusammen gelöscht werden.\n"
              + "Jeweils auch die Spendenbescheinigungen löschen?";
        }
      }
      
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Buchung" + (b.length > 1 ? "en" : "") + " löschen");
      d.setText(text);
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen der Buchung", e);
        return;
      }
      
      int count = 0;
      for (Buchung bu : b)
      {
        Jahresabschluss ja = bu.getJahresabschluss();
        if (ja != null)
        {
          throw new ApplicationException(String.format(
              "Buchung wurde bereits am %s von %s abgeschlossen.",
              new JVDateFormatTTMMJJJJ().format(ja.getDatum()), ja.getName()));
        }
        Spendenbescheinigung spb = bu.getSpendenbescheinigung();
        if(spb != null)
        {
          throw new ApplicationException(
              "Buchung kann nicht bearbeitet werden. Sie ist einer Spendenbescheinigung zugeordnet.");
        }
        if (bu.getSplitId() == null)
        {
          if (bu.getSpendenbescheinigung() != null)
            bu.getSpendenbescheinigung().delete();
          bu.delete();
          count++;
          control.getBuchungsList();
        }
        else if (splitbuchung)
        {
          if (bu.getDependencyId() == -1)
          {
            bu.setDelete(true);
            Application.getMessagingFactory()
                .sendMessage(new BuchungMessage(bu));
            count++;
          }
          else
          {
            for (Buchung buchung_tmp : SplitbuchungsContainer.get())
            {
              if (buchung_tmp.getDependencyId() == bu.getDependencyId())
              {
                buchung_tmp.setDelete(true);
                Application.getMessagingFactory()
                    .sendMessage(new BuchungMessage(buchung_tmp));
                count++;
              }
            }
          }
        }
      }
      if (count > 0)
      {
        GUI.getStatusBar().setSuccessText(String.format(
            "%d Buchung" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        GUI.getStatusBar().setErrorText("Keine Buchung gelöscht");
      }
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen der Buchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
