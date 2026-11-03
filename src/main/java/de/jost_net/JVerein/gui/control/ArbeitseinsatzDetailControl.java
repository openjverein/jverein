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
package de.jost_net.JVerein.gui.control;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzPart;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ArbeitseinsatzDetailControl extends VorZurueckControl
    implements Savable
{
  private ArbeitseinsatzPart part = null;

  private Arbeitseinsatz aeins = null;

  public ArbeitseinsatzDetailControl(AbstractView view)
  {
    super(view);
  }

  public Arbeitseinsatz getArbeitseinsatz()
  {
    if (aeins != null)
    {
      return aeins;
    }
    aeins = (Arbeitseinsatz) getCurrentObject();
    return aeins;
  }

  public ArbeitseinsatzPart getPart()
  {
    if (part != null)
    {
      return part;
    }
    part = new ArbeitseinsatzPart(getArbeitseinsatz(), true);
    return part;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Arbeitseinsatz ae = getArbeitseinsatz();
    if (ae.isNewObject())
    {
      if (getPart().getMitglied().getValue() != null)
      {
        Mitglied m = (Mitglied) getPart().getMitglied().getValue();
        ae.setMitglied(Integer.parseInt(m.getID()));
      }
      else
      {
        ae.setMitglied(null);
      }
    }
    ae.setDatum((Date) part.getDatum().getValue());
    ae.setStunden((BigDecimal) part.getStunden().getValue());
    ae.setBemerkung((String) part.getBemerkung().getValue());
    return ae;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Arbeitseinsatzes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
