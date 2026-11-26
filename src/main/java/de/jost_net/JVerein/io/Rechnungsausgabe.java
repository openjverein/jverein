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
package de.jost_net.JVerein.io;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.ZahlerMap;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.BelegTyp;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.server.IVersand;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class Rechnungsausgabe extends AbstractAusgabe
{

  private Formular formular;

  public Rechnungsausgabe(BelegTyp typ, Formular formular)
  {
    belegtyp = typ;
    this.formular = formular;
  }

  @Override
  public boolean checkVersendet(ArrayList<? extends DBObject> list,
      Ausgabeart art) throws RemoteException
  {
    // Mahnungen dürfen nur versendet werden, wenn die Rechnung schon verschickt
    // wurde
    if (belegtyp == BelegTyp.MAHNUNG)
    {
      checked = true;
      for (DBObject o : list)
      {
        IVersand v = (IVersand) o;
        if (v.getVersanddatum() == null)
        {
          YesNoDialog dialog = new YesNoDialog(YesNoDialog.POSITION_CENTER);
          dialog.setTitle("Rechnung nicht  "
              + (art == Ausgabeart.MAIL ? "gesendet" : "gedruckt"));
          dialog.setText(
              "Mindestens eine Rechnung wurde noch nicht als versendet markiert.\nSoll trotzdem eine Mahnung "
                  + (art == Ausgabeart.MAIL ? "versendet" : "gedruckt")
                  + " werden?");
          try
          {
            return (boolean) dialog.open();
          }
          catch (OperationCanceledException oce)
          {
            return false;
          }
          catch (Exception e)
          {
            Logger.error("Fehler beim trotzdem-Versenden Dialog", e);
          }
        }
      }
      return true;
    }
    else
    {
      return super.checkVersendet(list, art);
    }
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Mitglied m = ((Rechnung) object).getZahler();
    String filename = "";
    if (belegtyp == BelegTyp.RECHNUNG)
    {
      filename = m.getID() + "#rechnung#" + object.getID() + "#";
    }
    else
    {
      filename = m.getID() + "#mahnung#" + object.getID() + "#";
    }
    String email = StringTool.toNotNullString(m.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += m.getName() + m.getVorname();
    }
    return filename + "#" + belegtyp.name();
  }

  @Override
  protected Map<String, Object> getMap(DBObject object)
      throws RemoteException, ApplicationException
  {
    Rechnung re = (Rechnung) object;
    Map<String, Object> map = new RechnungMap().getMap(re, null);
    map = new MitgliedMap().getMap(re.getMitglied(), map);
    map = new ZahlerMap().getMap(re.getZahler(), map);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      Rechnung rechnung = (Rechnung) object;
      if (belegtyp == BelegTyp.RECHNUNG)
      {
        return VorlageUtil.getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME,
            rechnung);
      }
      else
      {
        return VorlageUtil.getName(VorlageTyp.MAHNUNG_MITGLIED_DATEINAME,
            rechnung);
      }
    }
    else
    {
      if (belegtyp == BelegTyp.RECHNUNG)
      {
        return VorlageUtil.getName(VorlageTyp.RECHNUNG_DATEINAME);
      }
      else
      {
        return VorlageUtil.getName(VorlageTyp.MAHNUNG_DATEINAME);
      }
    }
  }

  @Override
  protected void closeDocument(FormularAufbereitung aufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    super.closeDocument(aufbereitung, object);
    if (object != null)
    {
      aufbereitung.addZUGFeRD((Rechnung) object, belegtyp == BelegTyp.MAHNUNG);
    }
  }

  @Override
  protected Formular getFormular(DBObject object) throws RemoteException
  {
    if (belegtyp == BelegTyp.MAHNUNG)
    {
      return formular;
    }
    return ((Rechnung) object).getFormular();
  }
}
