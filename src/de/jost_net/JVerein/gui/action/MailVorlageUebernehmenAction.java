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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.IMailText;
import de.jost_net.JVerein.rmi.MailVorlage;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Übernahme einer MailVorlage
 */
public class MailVorlageUebernehmenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof IMailText))
    {
      throw new ApplicationException("Keine Mail Information vorhanden!");
    }

    try
    {
      String betreff = ((IMailText) context).getBetreffString();
      String text = ((IMailText) context).getTxtString();
      if (betreff == null || betreff.isEmpty())
      {
        throw new ApplicationException("Bitte Betreff eingeben!");
      }
      DBIterator<MailVorlage> vorlagen = Einstellungen.getDBService()
          .createList(MailVorlage.class);
      vorlagen.addFilter("betreff = ?", betreff);
      if (!vorlagen.hasNext())
      {
        MailVorlage v = (MailVorlage) Einstellungen.getDBService()
            .createObject(MailVorlage.class, null);
        v.setBetreff(betreff);
        v.setTxt(text);
        v.store();
        GUI.getStatusBar().setSuccessText("Mail-Vorlage erzeugt");
      }
      else
      {
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle("Als Vorlage übernehmen");
        d.setText(
            "Es existiert bereits eine Vorlage mit diesem Betreff.\nSoll sie überschrieben werden?");

        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
        MailVorlage v = vorlagen.next();
        v.setTxt(text);
        v.store();
        GUI.getStatusBar().setSuccessText("Mail-Vorlage überschrieben");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException e)
    {
      throw new ApplicationException(e.getMessage());
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Erzeugen der Mail-Vorlage";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
