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
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;

public class SplitbuchungNeuAction implements Action
{
  @Override
  public void handleAction(Object context)
  {
    try
    {
      Buchung master = SplitbuchungsContainer.getMaster();
      Buchung buch = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
          null);
      buch.setAuszugsnummer(master.getAuszugsnummer());
      buch.setBlattnummer(master.getBlattnummer());
      buch.setDatum(master.getDatum());
      buch.setKommentar(master.getKommentar());
      buch.setKonto(master.getKonto());
      buch.setSollbuchung(master.getSollbuchung());
      buch.setName(master.getName());
      buch.setProjekt(master.getProjekt());
      buch.setSplitId(Long.valueOf(master.getID()));
      buch.setUmsatzid(master.getUmsatzid());
      buch.setZweck(master.getZweck());
      buch.setBuchungsartId(master.getBuchungsartId());
      buch.setBuchungsklasseId(master.getBuchungsklasseId());
      buch.setSpeicherung(false);
      buch.setSplitTyp(SplitbuchungTyp.SPLIT);
      buch.setBetrag(SplitbuchungsContainer.getSumme(SplitbuchungTyp.HAUPT).doubleValue() - SplitbuchungsContainer.getSumme(SplitbuchungTyp.SPLIT).doubleValue());

      // Wenn CurrentObject und View von aktueller und n�chster View gleich
      // sind, wird die aktuelle View nicht in die History aufgenommen. Dadurch
      // f�hrt der Zur�ckbutton auch bei "Speichern und neu" zur Liste zur�ck.
      if (GUI.getCurrentView().getClass().equals(BuchungDetailView.class))
      {
        GUI.getCurrentView().setCurrentObject(buch);
      }
      GUI.startView(BuchungDetailView.class, buch);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }
}
