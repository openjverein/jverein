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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Löschen einer EigenschaftGruppe.
 */
public class EigenschaftGruppeDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null || !(context instanceof EigenschaftGruppe))
    {
      throw new ApplicationException("Keine Eigenschaften-Gruppe ausgewählt");
    }
    try
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) context;
      if (eg.isNewObject())
      {
        return;
      }

      DBIterator<Eigenschaft> it = Einstellungen.getDBService()
          .createList(Eigenschaft.class);
      it.addFilter("eigenschaftgruppe = ?", new Object[] { eg.getID() });

      try
      {
        if (it.size() > 0)
        {
          SimpleDialog sd = new SimpleDialog(SimpleDialog.POSITION_CENTER);
          sd.setTitle("Eigenschaften-Gruppe löschen");
          sd.setText(String.format(
              "Die Eigenschaften-Gruppe kann nicht gelöscht werden. Sie enthält noch %d Eigenschaft(en).",
              it.size()));
          sd.open();
          return;
        }
        else
        {
          YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
          d.setTitle("Eigenschaften-Gruppe löschen");
          d.setText("Wollen Sie diese Eigenschaften-Gruppe wirklich löschen?");
          Boolean choice = (Boolean) d.open();
          if (!choice.booleanValue())
            return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen der Eigenschaften-Gruppe", e);
        return;
      }

      eg.delete();
      GUI.getStatusBar().setSuccessText("Eigenschaften-Gruppe gelöscht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen der Eigenschaften-Gruppe.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
