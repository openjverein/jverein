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

import java.util.ArrayList;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.dialogs.GutschriftDialog;
import de.jost_net.JVerein.io.Gutschrift;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GutschriftAction implements Action
{
  private IGutschriftProvider[] providerArray = null;

  private DBService service;

  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      service = Einstellungen.getDBService();

      if (context instanceof TablePart)
      {
        TablePart tp = (TablePart) context;
        context = tp.getSelection();
      }
      if (context instanceof IGutschriftProvider)
      {
        providerArray = new IGutschriftProvider[] {
            (IGutschriftProvider) context };
      }
      else if (context instanceof IGutschriftProvider[])
      {
        providerArray = (IGutschriftProvider[]) context;
      }
      else if (context instanceof Abrechnungslauf)
      {
        Abrechnungslauf lauf = (Abrechnungslauf) context;
        DBIterator<Sollbuchung> sollbIt = service.createList(Sollbuchung.class);
        sollbIt.addFilter(Sollbuchung.ABRECHNUNGSLAUF + " = ?",
            Integer.valueOf(lauf.getID()));
        sollbIt.setOrder("ORDER BY " + Sollbuchung.MITGLIED);
        if (sollbIt.size() == 0)
        {
          throw new ApplicationException(
              "Der Abrechnungslauf enthält keine Sollbuchungen!");
        }
        providerArray = new IGutschriftProvider[sollbIt.size()];
        PseudoIterator.asList(sollbIt).toArray(providerArray);
      }
      else if (context instanceof MitgliedskontoNode)
      {
        MitgliedskontoNode mkn = (MitgliedskontoNode) context;

        if (mkn.getType() == MitgliedskontoNode.SOLL)
        {
          providerArray = new IGutschriftProvider[] { Einstellungen
              .getDBService().createObject(Sollbuchung.class, mkn.getID()) };
        }
      }
      else if (context instanceof MitgliedskontoNode[])
      {
        MitgliedskontoNode[] mknodes = (MitgliedskontoNode[]) context;

        ArrayList<IGutschriftProvider> list = new ArrayList<>();
        for (MitgliedskontoNode mkn : mknodes)
        {
          if (mkn.getType() == MitgliedskontoNode.SOLL)
          {
            list.add(service.createObject(Sollbuchung.class, mkn.getID()));
          }
        }
        if (list.size() > 0)
        {
          providerArray = new IGutschriftProvider[list.size()];
          list.toArray(providerArray);
        }
        else
        {
          throw new ApplicationException("Keine Sollbuchung ausgewählt!");
        }
      }
      else
      {
        throw new ApplicationException(
            "Keine Sollbuchung, Rechnung, Abrechnungslauf, Lastschrift oder Mitglied ausgewählt!");
      }

      GutschriftDialog dialog = new GutschriftDialog(
          providerArray[0] instanceof Mitglied);
      if (!dialog.open())
      {
        return;
      }
      new Gutschrift(dialog, providerArray);
    }
    catch (ApplicationException | OperationCanceledException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Datenbank Zugriff!";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }
}
