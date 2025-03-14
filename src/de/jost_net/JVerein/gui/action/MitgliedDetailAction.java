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
import de.jost_net.JVerein.gui.control.FamilienbeitragNode;
import de.jost_net.JVerein.gui.dialogs.PersonenartDialog;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.io.ArbeitseinsatzZeile;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class MitgliedDetailAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitglied mitglied;
    try
    {
      if (context instanceof FamilienbeitragNode)
      {
        FamilienbeitragNode fbn = (FamilienbeitragNode) context;
        mitglied = fbn.getMitglied();
      }
      else if (context instanceof Arbeitseinsatz)
      {
        Arbeitseinsatz aeins = (Arbeitseinsatz) context;
        mitglied = aeins.getMitglied();
      }
      else if (context instanceof ArbeitseinsatzZeile)
      {
        ArbeitseinsatzZeile aez = (ArbeitseinsatzZeile) context;
        mitglied = (Mitglied) aez.getAttribute("mitglied");
      }
      else if (context instanceof Mitglied)
      {
        mitglied = (Mitglied) context;
      }
      else if (context instanceof Sollbuchung)
      {
        Sollbuchung s = (Sollbuchung) context;
        mitglied = s.getMitglied();
      }
      else if (context instanceof Wiedervorlage)
      {
        Wiedervorlage w = (Wiedervorlage) context;
        mitglied = w.getMitglied();
      }
      else if (context instanceof Zusatzbetrag)
      {
        Zusatzbetrag z = (Zusatzbetrag) context;
        mitglied = z.getMitglied();
      }
      else if (context instanceof Lehrgang)
      {
        Lehrgang l = (Lehrgang) context;
        mitglied = l.getMitglied();
      }
      else if (context instanceof Lastschrift)
      {
        Lastschrift l = (Lastschrift) context;
        mitglied = l.getMitglied();
      }
      else if (context instanceof Rechnung)
      {
        Rechnung r = (Rechnung) context;
        mitglied = r.getMitglied();
      }
      else if (context instanceof Spendenbescheinigung)
      {
        Spendenbescheinigung s = (Spendenbescheinigung) context;
        mitglied = s.getMitglied();
      }
      else if ((context instanceof Buchung ) && ((Buchung) context).getSollbuchung() != null ) {
        mitglied = ((Buchung) context).getSollbuchung().getMitglied();
      }
      else
      {
        mitglied = (Mitglied) Einstellungen.getDBService().createObject(
            Mitglied.class, null);
        if (Einstellungen.getEinstellung().getJuristischePersonen())
        {
          PersonenartDialog pad = new PersonenartDialog(
              PersonenartDialog.POSITION_CENTER);
          String pa = pad.open();
          if (pa == null)
          {
            return;
          }
          mitglied.setPersonenart(pa);
        }
        else
        {
          mitglied.setPersonenart("n");
        }
      }
      // Wenn CurrentObject und View von aktueller und nächster View gleich
      // sind, wird die aktuelle View nicht in die History aufgenommen. Dadurch
      // führt der Zurückbutton auch bei "Speichern und neu" zur Liste zurück.
      if (GUI.getCurrentView().getClass().equals(MitgliedDetailView.class)
          || GUI.getCurrentView().getClass()
              .equals(NichtMitgliedDetailView.class))
      {
        GUI.getCurrentView().setCurrentObject(mitglied);
      }
      GUI.getCurrentView().setCurrentObject(mitglied);
      if (mitglied.getMitgliedstyp() == null || mitglied.getMitgliedstyp()
          .getID().equals(String.valueOf(Mitgliedstyp.MITGLIED)))
      {
        GUI.startView(new MitgliedDetailView(), mitglied);
      }
      else
      {
        GUI.startView(new NichtMitgliedDetailView(), mitglied);
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung eines neuen Mitgliedes", e);
    }
  }
}
