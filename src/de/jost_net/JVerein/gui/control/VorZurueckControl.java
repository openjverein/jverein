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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import de.jost_net.JVerein.Einstellungen;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;

public class VorZurueckControl extends AbstractControl
{
  private Class<? extends AbstractView> viewClass;

  private Class<? extends DBObject> objectClass;

  private LinkedList<Long> objektListe = null;

  public VorZurueckControl(AbstractView view)
  {
    super(view);
    if (view != null)
    {
      this.viewClass = view.getClass();
    }
  }

  ResultSetExtractor rs = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws RemoteException, SQLException
    {
      LinkedList<Long> list = new LinkedList<>();
      while (rs.next())
      {
        list.add(rs.getLong(1));
      }
      return list;
    }
  };

  @SuppressWarnings("unchecked")
  public void setObjektListe(Class<? extends DBObject> objectClass,
      String tabelle, String order, String filter, Object objekt)
  {
    this.objectClass = objectClass;
    try
    {
      final DBService service = Einstellungen.getDBService();

      String sql = "SELECT id FROM " + tabelle;
      if (filter != null)
      {
        sql += " WHERE " + filter;
      }
      if (order != null)
      {
        sql += " ORDER BY (" + order + ")";
      }

      if (objekt == null)
      {
        objektListe = (LinkedList<Long>) service.execute(sql, new Object[] {},
            rs);
      }
      else if (filter != null && objekt != null)
      {
        objektListe = (LinkedList<Long>) service.execute(sql,
            new Object[] { objekt }, rs);
      }

    }
    catch (RemoteException e)
    {
      //
    }
  }

  /**
   * Buttons
   */
  public Button getZurueckButton()
  {
    Button button = new Button("", context -> {
      if (objektListe == null || viewClass == null)
      {
        return;
      }
      DBObject object = (DBObject) getCurrentObject();
      try
      {
        int index = objektListe.indexOf(Long.valueOf(object.getID()));
        if (index > 0 && index < objektListe.size())
        {
          DBObject instanz = Einstellungen.getDBService()
              .createObject(objectClass, objektListe.get(index - 1).toString());
          GUI.startView(viewClass, instanz);
        }
      }
      catch (RemoteException e)
      {
        //
      }

    }, null, false, "go-previous.png");
    try
    {
      if (((DBObject) getCurrentObject()).isNewObject())
      {
        button.setEnabled(false);
      }
    }
    catch (RemoteException e)
    {
      //
    }
    return button;
  }

  public Button getVorButton()
  {
    Button button = new Button("", context -> {
      if (objektListe == null || viewClass == null)
      {
        return;
      }
      DBObject object = (DBObject) getCurrentObject();
      try
      {
        int index = objektListe.indexOf(Long.valueOf(object.getID()));
        if (index >= 0 && index < objektListe.size() - 1)
        {
          DBObject instanz = Einstellungen.getDBService()
              .createObject(objectClass, objektListe.get(index + 1).toString());
          GUI.startView(viewClass, instanz);
        }
      }
      catch (RemoteException e)
      {
        //
      }

    }, null, false, "go-next.png");
    try
    {
      if (((DBObject) getCurrentObject()).isNewObject())
      {
        button.setEnabled(false);
      }
    }
    catch (RemoteException e)
    {
      //
    }
    return button;
  }

}
