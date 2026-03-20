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
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.AnlagenlisteControl;
import de.jost_net.JVerein.gui.control.UmsatzsteuerSaldoControl;
import de.jost_net.JVerein.gui.view.BuchungsartDetailView;
import de.jost_net.JVerein.gui.view.BuchungsklasseDetailView;
import de.jost_net.JVerein.gui.view.KontoDetailView;
import de.jost_net.JVerein.gui.view.ProjektDetailView;
import de.jost_net.JVerein.gui.view.SteuerDetailView;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SaldoDetailAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      PseudoDBObject o = (PseudoDBObject) context;
      if (o.getAttribute(AnlagenlisteControl.KONTO_ID) != null)
      {
        String id = o.getAttribute(AnlagenlisteControl.KONTO_ID).toString();

        Konto konto = Einstellungen.getDBService().createObject(Konto.class,
            id);
        GUI.startView(KontoDetailView.class, konto);
      }
      else if (o.getAttribute(AbstractSaldoControl.BUCHUNGSART_ID) != null
          && o.getInteger(AbstractSaldoControl.BUCHUNGSART_ID) > 0)
      {
        String id = o.getAttribute(AbstractSaldoControl.BUCHUNGSART_ID)
            .toString();

        Buchungsart buchungsart = Einstellungen.getDBService()
            .createObject(Buchungsart.class, id);
        GUI.startView(BuchungsartDetailView.class, buchungsart);
      }
      else if (o.getAttribute(AbstractSaldoControl.PROJEKT_ID) != null)
      {
        String id = o.getAttribute(AbstractSaldoControl.PROJEKT_ID).toString();

        Projekt projekt = Einstellungen.getDBService()
            .createObject(Projekt.class, id);
        GUI.startView(ProjektDetailView.class, projekt);
      }
      else if (o.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_ID) != null)
      {
        String id = o.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_ID)
            .toString();

        Buchungsklasse buchungsklasse = Einstellungen.getDBService()
            .createObject(Buchungsklasse.class, id);
        GUI.startView(BuchungsklasseDetailView.class, buchungsklasse);
      }
      else if (o.getAttribute(UmsatzsteuerSaldoControl.STEUER_ID) != null)
      {
        String id = o.getAttribute(UmsatzsteuerSaldoControl.STEUER_ID)
            .toString();

        Steuer steuer = Einstellungen.getDBService().createObject(Steuer.class,
            id);
        GUI.startView(SteuerDetailView.class, steuer);
      }
      else
      {
        throw new ApplicationException("Eintrag kann nicht bearbeitet werden");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }
}
