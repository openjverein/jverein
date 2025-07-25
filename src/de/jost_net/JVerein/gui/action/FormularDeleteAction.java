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
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Formulares
 */
public class FormularDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null || !(context instanceof Formular))
    {
      throw new ApplicationException("Kein Formular ausgewählt");
    }
    try
    {
      Formular f = (Formular) context;
      if (f.isNewObject())
      {
        return;
      }

      DBIterator<Spendenbescheinigung> spb = Einstellungen.getDBService()
          .createList(Spendenbescheinigung.class);
      spb.addFilter("formular = ?", new Object[] { f.getID() });
      if (spb.size() > 0)
      {
        throw new ApplicationException(String.format(
            "Forular '%s' kann nicht gelöscht werden. Es ist bei %d Spendenbescheinigung(en) hinterlegt.",
            f.getBezeichnung(), spb.size()));
      }

      // Do not delete a form if it is linked by other forms
      if (f.hasFormlinks())
      {
        SimpleDialog sd = new SimpleDialog(AbstractDialog.POSITION_CENTER);
        sd.setTitle("Formularabhängigkeit");
        sd.setText(String.format(
            "Das Formular kann nicht gelöscht werden. Es ist noch mit %d Formular(en) verknüpft.",
            f.getLinked().size()));
        try
        {
          sd.open();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
        }
        return;
      }
      // Do not delete a form if it is linked to another
      Long formlink = f.getFormlink();
      if (formlink > 0)
      {
        Formular fo = (Formular) Einstellungen.getDBService()
            .createObject(Formular.class, String.valueOf(formlink));

        SimpleDialog sd = new SimpleDialog(AbstractDialog.POSITION_CENTER);
        sd.setTitle("Formularabhängigkeit");
        sd.setText(String.format(
            "Das Formular kann nicht gelöscht werden. Es ist mit dem Formular \"%s\" verknüpft.",
            fo.getBezeichnung()));
        try
        {
          sd.open();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
        }
        return;
      }

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Formular löschen");
      d.setText(("Wollen Sie dieses Formular wirklich löschen?"));

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
        Logger.error("Fehler beim Löschen des Formulares", e);
        return;
      }
      f.delete();
      GUI.getStatusBar().setSuccessText("Formular gelöscht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen des Formulars";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
