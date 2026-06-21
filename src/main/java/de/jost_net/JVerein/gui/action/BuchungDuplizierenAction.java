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
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class BuchungDuplizierenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    Buchung b = (Buchung) context;
    try
    {
      Buchung bu = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
      bu.setKonto(b.getKonto());
      bu.setName(b.getName());
      bu.setIban(b.getIban());
      bu.setBetrag(b.getBetrag());
      bu.setZweck(b.getZweck());
      bu.setDatum(b.getDatum());
      bu.setArt(b.getArt());
      bu.setKommentar(b.getKommentar());
      if (b.getBuchungsart() != null)
        bu.setBuchungsartId(b.getBuchungsartId());
      if (b.getBuchungsklasse() != null)
        bu.setBuchungsklasseId(b.getBuchungsklasseId());
      if (b.getSteuer() != null)
      {
        bu.setSteuer(b.getSteuer());
      }
      if (b.getProjekt() != null)
        bu.setProjektID(b.getProjektID());
      bu.setAuszugsnummer(b.getAuszugsnummer());
      bu.setBlattnummer(b.getBlattnummer());
      bu.setVerzicht(b.getVerzicht());
      GUI.startView(new BuchungDetailView(), bu);
    }
    catch (Exception e)
    {
      throw new ApplicationException("Fehler beim duplizieren einer Buchung",
          e);
    }
  }
}
