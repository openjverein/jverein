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
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;

public class EigenschaftenAuswahlParameter
{

  private Map<Eigenschaft, String> eigenschaftMap = new HashMap<>();

  private String verknuepfung = UND;

  public static final String ODER = "Oder";

  public static final String UND = "Und";

  public EigenschaftenAuswahlParameter(String value) throws RemoteException
  {
    StringTokenizer stt = new StringTokenizer(value, ",");
    StringBuilder text = new StringBuilder();
    while (stt.hasMoreElements())
    {
      if (text.length() > 0)
      {
        text.append(", ");
      }
      try
      {
        String s = stt.nextToken();
        if (s.equals(ODER))
        {
          verknuepfung = ODER;
          continue;
        }
        else if (s.equals(UND))
        {
          verknuepfung = UND;
          continue;
        }
        String eigenschaftId = s.substring(0, s.length() - 1);
        String plusMinus = s.substring(s.length() - 1);
        if (eigenschaftId.isEmpty()
            || !(plusMinus.equals(EigenschaftenNode.PLUS)
                || plusMinus.equals(EigenschaftenNode.MINUS)))
        {
          text = new StringBuilder();
          value = "";
          break;
        }
        String prefix = "+";
        if (plusMinus.equals(EigenschaftenNode.MINUS))
          prefix = "-";
        Eigenschaft ei = (Eigenschaft) Einstellungen.getDBService()
            .createObject(Eigenschaft.class, eigenschaftId);

        eigenschaftMap.put(ei, prefix);
      }
      catch (ObjectNotFoundException ignore)
      {
      }
    }
  }

  public EigenschaftenAuswahlParameter()
  {
  }

  public void add(Eigenschaft eigenschaft, String preset)
  {
    eigenschaftMap.put(eigenschaft, preset);
  }

  public Map<Eigenschaft, String> getEigenschaften()
  {
    return eigenschaftMap;
  }

  public void setVerknuepfung(String verknuepfung)
  {
    this.verknuepfung = verknuepfung;
  }

  public String getVerknuepfung()
  {
    return verknuepfung;
  }

  public String getIdString() throws RemoteException
  {
    if (eigenschaftMap.size() == 0)
    {
      return "";
    }
    StringBuilder id = new StringBuilder();
    id.append(verknuepfung);
    for (Entry<Eigenschaft, String> entry : eigenschaftMap.entrySet())
    {
      id.append(",");
      id.append(entry.getKey().getID() + entry.getValue());
    }
    return id.toString();
  }

  public String getString()
  {
    StringBuilder text = new StringBuilder();
    try
    {
      for (Entry<Eigenschaft, String> entry : eigenschaftMap.entrySet())
      {
        if (text.length() > 0)
        {
          text.append(", ");
        }
        text.append(
            (entry.getValue().equals(EigenschaftenNode.MINUS) ? "-" : "+")
                + entry.getKey().getBezeichnung());
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return text.toString();
  }

  @Override
  public String toString()
  {
    return getString();
  }
}
