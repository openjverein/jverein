/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Beleggruppe;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeleggruppeDetailControl extends VorZurueckControl
    implements Savable
{

  private Input bezeichnung;

  private Beleggruppe beleggruppe;

  public BeleggruppeDetailControl(AbstractView view)
  {
    super(view);
  }

  private Beleggruppe getBeleggruppe()
  {
    if (beleggruppe != null)
    {
      return beleggruppe;
    }
    beleggruppe = (Beleggruppe) getCurrentObject();
    return beleggruppe;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getBeleggruppe().getBezeichnung(), 50);
    bezeichnung.setMandatory(true);
    return bezeichnung;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Beleggruppe b = getBeleggruppe();
    b.setBezeichnung((String) getBezeichnung().getValue());
    return b;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim speichern der Beleggruppe";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
