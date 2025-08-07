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
import de.jost_net.JVerein.gui.control.KontoControl;
import de.jost_net.JVerein.keys.Kontoart;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hibiscus-Konten importieren
 */
public class HibiscusKontenImportAction implements Action
{

  private KontoControl control;

  public HibiscusKontenImportAction(KontoControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
    {
      // 1) Wir zeigen einen Dialog an, in dem der User das Konto auswählt
      KontoAuswahlDialog d = new KontoAuswahlDialog(
          KontoAuswahlDialog.POSITION_CENTER);
      try
      {
        context = d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        Logger.error("Error while choosing konto", e);
        GUI.getStatusBar().setErrorText("Fehler bei der Auswahl des Kontos.");
      }
    }

    if (context == null || !(context instanceof Konto))
      return;

    final Konto k = (Konto) context;
    try
    {
      de.jost_net.JVerein.rmi.Konto jvereinkonto = (de.jost_net.JVerein.rmi.Konto) Einstellungen
          .getDBService()
          .createObject(de.jost_net.JVerein.rmi.Konto.class, null);
      jvereinkonto.setNummer(k.getKontonummer());
      jvereinkonto.setBezeichnung(k.getBezeichnung());
      jvereinkonto.setHibiscusId(Integer.valueOf(k.getID()));
      jvereinkonto.setKontoArt(Kontoart.GELD);
      jvereinkonto.store();
      control.refreshTable();
      GUI.getStatusBar().setSuccessText(
          String.format("Konto %s importiert.", k.getKontonummer()));
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e.getMessage());
    }
  }
}
