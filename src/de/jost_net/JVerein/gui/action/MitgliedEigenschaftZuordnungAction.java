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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlDialog2;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter2;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.EigenschaftenNode2;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Eigenschaften an Mitglieder zuordnen.
 */
public class MitgliedEigenschaftZuordnungAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Mitglied) && !(context instanceof Mitglied[])))
    {
      throw new ApplicationException("Kein Mitglied ausgewählt");
    }
    Mitglied[] m = null;
    if (context instanceof Mitglied)
    {
      m = new Mitglied[] { (Mitglied) context };
    }
    else if (context instanceof Mitglied[])
    {
      m = (Mitglied[]) context;
    }
    int anzErfolgreich = 0;
    int anzBereitsVorhanden = 0;
    int anzGeloescht = 0;
    try
    {
      EigenschaftenAuswahlDialog2 ead = new EigenschaftenAuswahlDialog2("", true,
          false, new MitgliedControl(null), false, m);
      EigenschaftenAuswahlParameter2 param = ead.open();
      if (param == null || param.getEigenschaftenNodes() == null)
      {
        return;
      }
      Map<Long, Long[]> eigenschaften =  getEigenschaften();
      ArrayList<EigenschaftenNode2> nodes = param.getEigenschaftenNodes();
      for (EigenschaftenNode2 en : nodes)
      {
        if (en.getPreset().equals(EigenschaftenNode2.PLUS))
        {
          for (Mitglied mit : m)
          {
            Eigenschaften eig = (Eigenschaften) Einstellungen.getDBService()
                .createObject(Eigenschaften.class, null);
            eig.setEigenschaft(en.getEigenschaft().getID());
            eig.setMitglied(mit.getID());
            try
            {
              eig.store();
              anzErfolgreich++;
            }
            catch (RemoteException e)
            {
              if (e.getCause() instanceof SQLException)
              {
                anzBereitsVorhanden++;
              }
              else
              {
                throw new ApplicationException(e);
              }
            }
          }
        }
        else if (en.getPreset().equals(EigenschaftenNode2.MINUS))
        {
          for (Mitglied mit : m)
          {
            for  (Long key : eigenschaften.keySet())
            {
              Long[] entry = eigenschaften.get(key);
              if (entry[0].equals(Long.valueOf(mit.getID())) &&
                  entry[1].equals(Long.valueOf(en.getEigenschaft().getID())))
              {
                Eigenschaften eig = (Eigenschaften) Einstellungen.getDBService()
                    .createObject(Eigenschaften.class, key.toString());
                eig.delete();
                anzGeloescht++;
              }
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.error(
      "Fehler beim Bearbeiten von Eigenschaften", e);
      return;
    }
    GUI.getStatusBar().setSuccessText(
        String.format(
            "%d Eigenschaft(en) angelegt, %d waren bereits vorhanden, %d wurden gelöscht.",
            anzErfolgreich, anzBereitsVorhanden, anzGeloescht));
  }
  
  @SuppressWarnings("unchecked")
  private Map<Long, Long[]> getEigenschaften() throws RemoteException
  {
    // Eigenschaften lesen
    final DBService service = Einstellungen.getDBService();
    String sql = "SELECT eigenschaften.* from eigenschaften ";
    Map<Long, Long[]> mitgliedeigenschaften = (Map<Long, Long[]>) service.execute(sql,
        new Object[] { }, new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        Map<Long, Long[]> list = new HashMap<>();
        while (rs.next())
        {
          list.put(rs.getLong(1), new Long[] {rs.getLong(2), rs.getLong(3)});
        }
        return list;
      }
    });
    return mitgliedeigenschaften;
  }
}
