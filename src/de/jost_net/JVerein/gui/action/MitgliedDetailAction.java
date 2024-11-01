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
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class MitgliedDetailAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitglied m = null;
    try
    {
      if (context != null && context instanceof FamilienbeitragNode)
      {
        FamilienbeitragNode fbn = (FamilienbeitragNode) context;
        m = fbn.getMitglied();
      }
      else if (context != null && context instanceof ArbeitseinsatzZeile)
      {
        ArbeitseinsatzZeile aez = (ArbeitseinsatzZeile) context;
        m = (Mitglied) aez.getAttribute("mitglied");
      }
      else if (context != null && (context instanceof Mitglied))
      {
        m = (Mitglied) context;
      }
      else if (context != null && (context instanceof Mitgliedskonto))
      {
        Mitgliedskonto mk = (Mitgliedskonto) context;
        m = mk.getMitglied();
      }
      else if (context != null && (context instanceof Wiedervorlage))
      {
        Wiedervorlage w = (Wiedervorlage) context;
        m = w.getMitglied();
      }
      else if (context != null && (context instanceof Zusatzbetrag))
      {
        Zusatzbetrag z = (Zusatzbetrag) context;
        m = z.getMitglied();
      }
      else if (context != null && (context instanceof Lehrgang))
      {
        Lehrgang l = (Lehrgang) context;
        m = l.getMitglied();
      }
      else if (context != null && (context instanceof Lastschrift))
      {
        Lastschrift l = (Lastschrift) context;
        m = l.getMitglied();
      }
      else
      {
        m = (Mitglied) Einstellungen.getDBService().createObject(
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
          m.setPersonenart(pa);
        }
        else
        {
          m.setPersonenart("n");
        }
      }
      if (m.getAdresstyp() == null || m.getAdresstyp().getID().equals("1"))
      {
        GUI.startView(new MitgliedDetailView(), m);
      }
      else
      {
        GUI.startView(new NichtMitgliedDetailView(), m);
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
