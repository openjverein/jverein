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
package de.jost_net.jverein.gui.action;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import de.jost_net.jverein.io.FormularAufbereitung;
import de.jost_net.jverein.rmi.Formular;
import de.jost_net.jverein.variable.AllgemeineMap;
import de.jost_net.jverein.variable.LastschriftMap;
import de.jost_net.jverein.variable.MitgliedMap;
import de.jost_net.jverein.variable.RechnungMap;
import de.jost_net.jverein.variable.SpendenbescheinigungMap;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularAnzeigeAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Formular formular = null;

    if (context != null && (context instanceof Formular))
    {
      formular = (Formular) context;
      try
      {
        if (formular.isNewObject())
        {
          throw new ApplicationException(
              "Vor der Anzeige des Formulars muss dieses gespeichert werden!");
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException("Fehler bei der Anzeige eines Formulars",
            e);
      }
    }
    else
    {
      throw new ApplicationException("Kein Formular zur Anzeige ausgewählt");
    }
    try
    {
      final File file = File.createTempFile("formular", ".pdf");

      Map<String, Object> map = new AllgemeineMap().getMap(null);
      switch (formular.getArt())
      {
        case SPENDENBESCHEINIGUNG:
        case SAMMELSPENDENBESCHEINIGUNG:
        case SACHSPENDENBESCHEINIGUNG:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIESFORMULAR:
          map = MitgliedMap.getDummyMap(map);
          break;
        case SEPA_PRENOTIFICATION:
          map = MitgliedMap.getDummyMap(map);
          map = LastschriftMap.getDummyMap(map);
          break;
        case RECHNUNG:
        case MAHNUNG:
          map = MitgliedMap.getDummyMap(map);
          map = RechnungMap.getDummyMap(map);
          break;
        case HINTERGRUND:
          break;
      }
      FormularAufbereitung fab = new FormularAufbereitung(file, false, false);
      fab.writeForm(formular, map);
      fab.closeFormular();
      fab.showFormular();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
    catch (IOException e)
    {
      throw new ApplicationException(e);
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei der Anzeige eines Formulares", e);
      throw new ApplicationException(e);
    }
  }
}
