/**********************************************************************
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
package de.jost_net.JVerein.keys;

import java.rmi.RemoteException;
import java.util.Calendar;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;

/**
 * Arbeitseinsatzjahre
 */
public class Suchjahr implements KeyEnum
{

  private int jahr;

  public Suchjahr(int key)
  {
    jahr = key;
  }

  public static Suchjahr[] getList()
  {
    try
    {
      DBIterator<Arbeitseinsatz> list = Einstellungen.getDBService()
          .createList(Arbeitseinsatz.class);
      list.setOrder("ORDER BY datum");
      Arbeitseinsatz ae = null;
      Calendar von = Calendar.getInstance();
      if (list.hasNext())
      {
        ae = list.next();
        von.setTime(ae.getDatum());
      }
      Calendar bis = Calendar.getInstance();

      int vonjahr = von.get(Calendar.YEAR);
      int bisjahr = bis.get(Calendar.YEAR);
      Suchjahr[] jahre = new Suchjahr[1 + bisjahr - vonjahr];

      int j = 0;
      for (int i = bisjahr; i >= vonjahr; i--)
      {
        jahre[j] = new Suchjahr(i);
        j++;
      }
      return jahre;
    }
    catch (RemoteException ex)
    {
      Logger.error("unable to build description", ex);
      return new Suchjahr[0];
    }
  }

  @Override
  public int getKey()
  {
    return jahr;
  }

  public String getText()
  {
    return toString();
  }

  @Override
  public String toString()
  {
    return ((Integer) jahr).toString();
  }
}
