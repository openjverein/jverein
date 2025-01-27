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
import java.util.ArrayList;

import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Formularfeldes
 */
public class SollbuchungPositionDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null || !(context instanceof SollbuchungPosition))
    {
      throw new ApplicationException("Keine Sollbuchungsposition ausgewählt");
    }
    try
    {
      SollbuchungPosition position = (SollbuchungPosition) context;
      if (position.isNewObject())
      {
        return;
      }

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Sollbuchungsposition löschen");
      d.setText("Wollen Sie diese Sollbuchungsposition wirklich löschen?");

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
        Logger.error("Fehler beim Löschen der Sollbuchungsposition", e);
        return;
      }
      position.delete();
      // Betrag in Sollbuchung neu berechnen
      Double betrag = 0.0;
      Mitgliedskonto sollb = position.getSollbuchung();
      ArrayList<SollbuchungPosition> sollbpList = sollb
          .getSollbuchungPositionList();
      for (SollbuchungPosition sollp : sollbpList)
      {
        betrag += sollp.getBetrag();
      }
      sollb.setBetrag(betrag);
      sollb.store();
      GUI.startView(SollbuchungDetailView.class.getName(), sollb);
      GUI.getStatusBar().setSuccessText("Sollbuchungsposition gelöscht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen der Sollbuchungsposition";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
