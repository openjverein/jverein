/**********************************************************************
* Copyright (c) by Alexander Dippe
* This program is free software: you can redistribute it and/or modify it under the terms of the 
* GNU General Public License as published by the Free Software Foundation, either version 3 of the 
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
* even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
* the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with this program.  If not, 
* see <http://www.gnu.org/licenses/>.
* 
* https://openjverein.github.io
**********************************************************************/
package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;

import de.jost_net.JVerein.gui.dialogs.MailAbfrageDialog;
import de.jost_net.JVerein.gui.dialogs.MailAbfrageDialog.Auswahl;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * E-Mail senden Formular anhand des zugewiesenen Spenders
 */
public class SpendenbescheinigungSendAction implements Action
{
  private de.willuhn.jameica.system.Settings settings;
  
  public SpendenbescheinigungSendAction()
  {
    super();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Versenden einer E-Mail mit der Spendenbescheinigung im Anhang 
   * für Mitglieder deren Spendenbescheinigung im View ausgewählt ist.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Spendenbescheinigung[] spbArr = null;
    // Prüfung des Contexs, vorhanden, eine oder mehrere
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Spendenbescheinigung ausgewählt");
    }
    else if (context instanceof Spendenbescheinigung)
    {
      spbArr = new Spendenbescheinigung[] { (Spendenbescheinigung) context };
    }
    else if (context instanceof Spendenbescheinigung[])
    {
      spbArr = (Spendenbescheinigung[]) context;
    }
    else
    {
      return;
    }
    
    try
    {
      String text = "Für das Versenden werden gedruckte Spendenbescheinigungen benötigt."
          + "\nWenn sie schon gedruckt wurden brauchen sie nicht neu gedruckt werden.";
      MailAbfrageDialog d = new MailAbfrageDialog(text, MailAbfrageDialog.POSITION_CENTER);
      d.setTitle("Spendenbescheinigungen versenden");
      d.setPanelText("Spendenbescheinigungen drucken?");
      
      MailAbfrageDialog.Auswahl choice = (MailAbfrageDialog.Auswahl) d.open();
      if (choice == null)
      {
        return;
      }
      else if (choice == Auswahl.DRUCKEN_STANDARD)
      {
        SpendenbescheinigungPrintAction action = new SpendenbescheinigungPrintAction(true, true);
        action.handleAction(spbArr);
      }
      else if (choice == Auswahl.DRUCKEN_INDIVIDUELL)
      {
        SpendenbescheinigungPrintAction action = new SpendenbescheinigungPrintAction(false, true);
        action.handleAction(spbArr);
      }

      GUI.startView(SpendenbescheinigungMailView.class, spbArr);
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }
}
