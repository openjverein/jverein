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
import de.jost_net.JVerein.gui.control.BuchungsControl.Kontenfilter;
import de.jost_net.JVerein.gui.dialogs.KontoAuswahlDialog;
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class BuchungGegenbuchungAction implements Action
{
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
 
    if (context == null || !(context instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    Buchung b = (Buchung) context;
    Konto konto = null;
    try
    {
      final DBService service = Einstellungen.getDBService();
      DBIterator<Konto> konten = service.createList(Konto.class);
      konten.addFilter("buchungsart = ?", b.getBuchungsartId());
      if (konten.size() > 0)
      {
        konto = (Konto) konten.next();
      }
      if (konto == null)
      {
        KontoAuswahlDialog d = new KontoAuswahlDialog(
            KontoAuswahlDialog.POSITION_CENTER, false, false, true, Kontenfilter.ALLE);
        konto = (Konto) d.open();
      }
      if (konto != null)
      {
        Buchung bu = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
            null);
        bu.setKonto(konto);
        bu.setName(b.getName());
        // Bei Anlagenkonto Netto Betrag verwenden
        if (konto.getKontoArt().equals(Kontoart.ANLAGE))
        {
          bu.setBetrag(-b.getNetto());
        }
        else
        {
          bu.setBetrag(-b.getBetrag());
        }
        bu.setZweck(b.getZweck());
        bu.setDatum(b.getDatum());
        if (b.getBuchungsart() != null)
          bu.setBuchungsartId(b.getBuchungsartId());
        if (b.getBuchungsklasse() != null)
          bu.setBuchungsklasseId(b.getBuchungsklasseId());
        if (b.getProjekt() != null)
          bu.setProjektID(b.getProjektID());
        
        GUI.startView(new BuchungDetailView(), bu);
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      GUI.getStatusBar().setErrorText("Fehler bei der Gegenbuchung.");
    }
  }
}
