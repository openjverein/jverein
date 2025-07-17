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
import java.util.LinkedList;

import de.jost_net.JVerein.Einstellungen;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;

public class VorZurueckControl extends AbstractControl
{
  static Class<? extends AbstractView> viewClass;

  static Class<? extends DBObject> objectClass;

  static LinkedList<Long> objektListe = null;

  private Button zurueck;

  private Button vor;

  public VorZurueckControl(AbstractView view)
  {
    super(view);
    if (view != null)
    {
      VorZurueckControl.viewClass = view.getClass();
    }
  }

  public static void setObjektListe(Class<? extends DBObject> objectClass,
      LinkedList<Long> objektListe)
  {
    VorZurueckControl.objectClass = objectClass;
    VorZurueckControl.objektListe = objektListe;
  }

  /**
   * Buttons
   */
  public Button getZurueckButton()
  {
    zurueck = new Button("", context -> {
      DBObject object = (DBObject) getCurrentObject();
      if (objektListe == null || viewClass == null
          || object.getClass() != objectClass)
      {
        return;
      }
      try
      {
        int index = objektListe.indexOf(Long.valueOf(object.getID()));
        if (index > 0 && index < objektListe.size())
        {
          DBObject instanz = Einstellungen.getDBService()
              .createObject(objectClass, objektListe.get(index - 1).toString());
          // Neuen View nicht in die History aufnehmen
          GUI.getCurrentView().setCurrentObject(instanz);
          GUI.startView(viewClass, instanz);
        }
      }
      catch (RemoteException e)
      {
        //
      }

    }, null, false, "go-previous.png");
    return zurueck;
  }

  public Button getVorButton()
  {
    vor = new Button("", context -> {
      DBObject object = (DBObject) getCurrentObject();
      if (objektListe == null || viewClass == null
          || object.getClass() != objectClass)
      {
        return;
      }
      try
      {
        int index = objektListe.indexOf(Long.valueOf(object.getID()));
        if (index >= 0 && index < objektListe.size() - 1)
        {
          DBObject instanz = Einstellungen.getDBService()
              .createObject(objectClass, objektListe.get(index + 1).toString());
          // Neuen View nicht in die History aufnehmen
          GUI.getCurrentView().setCurrentObject(instanz);
          GUI.startView(viewClass, instanz);
        }
      }
      catch (RemoteException e)
      {
        //
      }

    }, null, false, "go-next.png");
    return vor;
  }

}
