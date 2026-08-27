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
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungNeuAction implements Action
{
  private BuchungsControl control;

  public BuchungNeuAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Buchung buch = null;
    Konto konto = null;
    try
    {
      buch = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
          null);

      konto = (Konto) control.getFilterValue(Filter.KONTO);
      if (konto != null)
      {
        // Das Konto ist im Filterbereich ausgewählt
        updateBuchung(buch, konto);
      }
      else
      {
        String kontoid = control.getSettings().getString(
            control.getSettingsPrefix() + BuchungsControl.KONTO_ID, "");
        if (kontoid != null && !kontoid.isEmpty())
        {
          // Das ist das Konto welches als letztes benutzt wurde und darum in
          // den Settings gespeichert ist
          try
          {
            konto = (Konto) Einstellungen.getDBService()
                .createObject(Konto.class, kontoid);
          }
          catch (ObjectNotFoundException ex)
          {
            // Das Konto aus den Settings gibt es nicht (mehr)!
          }
          if (konto != null)
          {
            updateBuchung(buch, konto);
          }
        }
      }
      if (konto == null)
      {
        // Noch kein Konto gefunden, jetzt nehmen wir das erste passende Konto
        DBIterator<Konto> it = Einstellungen.getDBService()
            .createList(Konto.class);
        it.setLimit(1);
        if (control.getSettingsPrefix()
            .equals(BuchungsControl.ANLAGENKONTO_PREFIX))
        {
          it.addFilter("kontoart = ?", Kontoart.ANLAGE.getKey());
        }
        else
        {
          it.addFilter("kontoart != ?", Kontoart.ANLAGE.getKey());
        }
        if (it.hasNext())
        {
          konto = it.next();
        }
        if (konto != null)
        {
          // Es gibt ein Konto der entsprechenden Art
          updateBuchung(buch, konto);
        }
        else
        {
          // Wenn es kein Konto gibt, dann kann man auch keine Buchung erzeugen
          throw new ApplicationException(
              "Es existiert kein passendes Konto. Bitte erst ein Konto erzeugen!");
        }
      }

      // Wenn CurrentObject und View von aktueller und nächster View gleich
      // sind, wird die atuelle View nicht in die History aufgenommen. Dadurch
      // führt der Zurückbutton auch bei "Speichern und neu" zur Liste zurück.
      if (GUI.getCurrentView().getClass().equals(BuchungDetailView.class))
      {
        GUI.getCurrentView().setCurrentObject(buch);
      }
      GUI.startView(BuchungDetailView.class, buch);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler beim erzeugen einer Buchung!");
    }
  }

  private void updateBuchung(Buchung buch, Konto konto) throws RemoteException
  {
    buch.setDatum(new Date());
    if (konto.getKontoArt() == Kontoart.ANLAGE)
    {
      buch.setBuchungsartId(konto.getAfaartId());
    }
    buch.setKonto(konto);
  }
}
