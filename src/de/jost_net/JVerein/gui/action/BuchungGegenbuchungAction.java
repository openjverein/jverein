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
import de.jost_net.JVerein.gui.dialogs.KontoAuswahlDialog;
import de.jost_net.JVerein.gui.view.BuchungView;
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
      throw new ApplicationException("Keine Buchung ausgew�hlt");
    }
    Buchung b = null;
    Konto konto = null;
    Konto konto1 = null;
    try
    {
      final DBService service = Einstellungen.getDBService();
      DBIterator<Konto> konten = service.createList(Konto.class);
      b = (Buchung) context;
      while (konten.hasNext())
      {
        konto1 = konten.next();
        if ((konto1.getBuchungsart() != null) && (b.getBuchungsart() != null))
        {
          if (konto1.getBuchungsartId() == b.getBuchungsartId())
          {
            konto = konto1;
            break;
          }
        }
      }
      if (konto == null)
      {
        KontoAuswahlDialog d = new KontoAuswahlDialog(
            KontoAuswahlDialog.POSITION_CENTER, false, false, true);
        konto = (Konto) d.open();
      }
      if (konto != null)
      {
        b.setID(null);
        b.setSplitId(null);
        b.setKonto(konto);
        b.setBetrag(-b.getBetrag());
        b.setAuszugsnummer(null);
        b.setBlattnummer(null);
        GUI.startView(new BuchungView(), b);
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