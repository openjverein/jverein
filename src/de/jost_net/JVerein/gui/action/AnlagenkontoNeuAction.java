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
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.dialogs.AnlagenkontoNeuDialog;
import de.jost_net.JVerein.gui.view.KontoView;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class AnlagenkontoNeuAction implements Action
{
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
 
    if (context == null || !(context instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    Buchung buchung = (Buchung) context;
    Konto konto = null;
    try
    {
      DBTransaction.starten();
      AnlagenkontoNeuDialog d = new AnlagenkontoNeuDialog(
          AnlagenkontoNeuDialog.POSITION_CENTER, buchung);
      konto = (Konto) d.open();

      if (konto != null && !konto.isNewObject())
      {
        Anfangsbestand anf = (Anfangsbestand) Einstellungen.getDBService().createObject(
            Anfangsbestand.class, null);
        anf.setKonto(konto);
        anf.setDatum(buchung.getDatum());
        anf.setBetrag(0d);
        anf.store();
        
        Buchung bu = (Buchung) Einstellungen.getDBService().
            createObject(Buchung.class, null);
        bu.setKonto(konto);
        bu.setName(buchung.getName());
        bu.setBetrag(-buchung.getBetrag());
        bu.setZweck(buchung.getZweck());
        bu.setDatum(buchung.getDatum());
        if (buchung.getBuchungsart() != null)
          bu.setBuchungsartId(buchung.getBuchungsartId());
        if (buchung.getProjekt() != null)
          bu.setProjektID(buchung.getProjektID());
        bu.setKommentar(buchung.getKommentar());
        bu.store();
        DBTransaction.commit();
        GUI.startView(new KontoView(), konto);
      }
      DBTransaction.rollback();
    }
    catch (OperationCanceledException oce)
    {
      DBTransaction.rollback();
      throw oce;
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(
          "Fehler bei der Erstellung des Anlagenkontos: " + e.getMessage());
    }
  }
}
