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
package de.jost_net.JVerein.gui.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;

public class EigenschaftenUtil
{
  private HashMap<String, EigenschaftGruppe> gruppen;

  private ArrayList<Eigenschaft> eigenschaftenList;
  
  private HashMap<String, String> auswahl;

  public EigenschaftenUtil(String eigenschaften) throws RemoteException
  {
    gruppen = new HashMap<>();
    eigenschaftenList = new ArrayList<>();
    auswahl = new HashMap<>();
    if (eigenschaften == null || eigenschaften.length() == 0)
    {
      return;
    }

    StringTokenizer st = new StringTokenizer(eigenschaften, ",");
    while (st.hasMoreTokens())
    {
      try
      {
        String s = st.nextToken();
        Eigenschaft ei = (Eigenschaft) Einstellungen.getDBService()
            .createObject(Eigenschaft.class, s.substring(0,s.length()-1));
        EigenschaftGruppe eg = ei.getEigenschaftGruppe();
        gruppen.put(eg.getID(), eg);
        eigenschaftenList.add(ei);
        auswahl.put(ei.getID(), s.substring(s.length()-1));
      }
      catch (ObjectNotFoundException e)
      {
        Logger.error("Eigenschaft wurde zwischenzeitlich gel�scht");
      }
    }

  }

  public ArrayList<EigenschaftGruppe> getGruppen() throws RemoteException
  {
    ArrayList<EigenschaftGruppe> ret = new ArrayList<>();
    for (String key : gruppen.keySet())
    {
      ret.add((EigenschaftGruppe) Einstellungen.getDBService()
          .createObject(EigenschaftGruppe.class, key));
    }
    return ret;
  }

  public ArrayList<Eigenschaft> get(EigenschaftGruppe eg) throws RemoteException
  {
    ArrayList<Eigenschaft> ret = new ArrayList<>();
    for (Eigenschaft ei : eigenschaftenList)
    {
      if (ei.getEigenschaftGruppe().getID().equals(eg.getID()))
      {
        ret.add(ei);
      }
    }
    return ret;
  }

  public ArrayList<Eigenschaft> getEigenschaften()
  {
    return eigenschaftenList;
  }
  
  public String getAuswahl(String eigenschaftId)
  {
    return auswahl.get(eigenschaftId);
  }
}
