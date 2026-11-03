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
import de.jost_net.JVerein.gui.control.LehrgangDetailControl;
import de.jost_net.JVerein.gui.dialogs.ForderungDialog;
import de.jost_net.JVerein.gui.view.LehrgangsteilnehmerDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangsmitgliederAction implements Action
{
  public enum ART
  {
    NEU,
    BEARBEITEN,
    LOESCHEN,
    MITGLIED_ANZEIGEN,
    FORDERUNG
  }

  private ART art;

  private LehrgangDetailControl control;

  public LehrgangsmitgliederAction(ART art, LehrgangDetailControl control)
  {
    this.art = art;
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (art == ART.NEU)
      {
        Lehrgangsteilnehmer tln = Einstellungen.getDBService()
            .createObject(Lehrgangsteilnehmer.class, null);
        tln.setLehrgang(Long.valueOf(control.getLehrgang().getID()));
        GUI.startView(LehrgangsteilnehmerDetailView.class, tln);
      }
      else
      {
        if (!(context instanceof PseudoDBObject)
            && !(context instanceof PseudoDBObject[]))
        {
          throw new ApplicationException("Keine Mitglied ausgewählt.");
        }

        PseudoDBObject[] object = null;
        if (context instanceof PseudoDBObject)
        {
          object = new PseudoDBObject[1];
          object[0] = (PseudoDBObject) context;
        }
        if (context instanceof PseudoDBObject[])
        {
          object = (PseudoDBObject[]) context;
        }

        if (object.length > 0)
        {
          String id;
          Lehrgangsteilnehmer[] tln = new Lehrgangsteilnehmer[object.length];
          for (int i = 0; i < object.length; i++)
          {
            id = object[i].getAttribute("id").toString();
            tln[i] = Einstellungen.getDBService()
                .createObject(Lehrgangsteilnehmer.class, id);
          }
          switch (art)
          {
            case BEARBEITEN:
              GUI.startView(LehrgangsteilnehmerDetailView.class, tln[0]);
              break;
            case LOESCHEN:
              new LehrgangsmitgliederDeleteAction(control).handleAction(tln);
              break;
            case MITGLIED_ANZEIGEN:
              GUI.startView(MitgliedDetailView.class, tln[0].getMitglied());
              break;
            case FORDERUNG:
              Mitglied[] mitglieder = new Mitglied[tln.length];
              for (int i = 0; i < tln.length; i++)
              {
                mitglieder[i] = tln[i].getMitglied();
              }
              ForderungDialog dialog = new ForderungDialog(
                  AbstractDialog.POSITION_CENTER, mitglieder,
                  control.getLehrgang().getBezeichnung(),
                  control.getLehrgang().getBetrag());
              dialog.open();
              break;
            default:
              break;
          }
        }
      }
    }
    catch (ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }
}
