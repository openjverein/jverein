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
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.view.BuchungView;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;

public class BuchungNeuAction implements Action
{
  private BuchungsControl control;

  public BuchungNeuAction(BuchungsControl control)
  {
    this.control = control;
  }
  
  @Override
  public void handleAction(Object context)
  {
    Buchung buch;
    try
    {
      buch = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
          null);
      Konto konto = (Konto) control.getSuchKonto().getValue();
      if (null != konto)
      {
        if (konto.getKontoArt() == Kontoart.ANLAGE)
        {
          buch.setBuchungsartId(konto.getAfaartId());
        }
        buch.setDatum(new Date());
        buch.setKonto(konto);
      }
      else
      {
        String kontoid = control.getSettings().getString(control.getSettingsPrefix() + "kontoid", "");
        if (kontoid != null && !kontoid.isEmpty())
        {
          Konto k = null;
          try
          {
            k = (Konto) Einstellungen.getDBService().createObject(Konto.class, kontoid);
            if (null != k)
            {
              if (k.getKontoArt() == Kontoart.ANLAGE)
              {
                buch.setBuchungsartId(k.getAfaartId());
              }
              buch.setDatum(new Date());
              buch.setKonto(k);
            }
          }
          catch (ObjectNotFoundException ex)
          {
            // Das Konto aus den Settings gibt es nicht!
          }
        }
      }
      // Wenn CurrentObject und View von aktueller und nächster View gleich
      // sind, wird die atuelle View nicht in die History aufgenommen. Dadurch
      // führt der Zurückbutton auch bei "Speichern und neu" zur Liste zurück.
      GUI.getCurrentView().setCurrentObject(buch);
      GUI.startView(BuchungView.class, buch);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }
}
