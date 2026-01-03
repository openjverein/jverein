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
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.control.RechnungControl;
import de.jost_net.JVerein.gui.control.RechnungControl.TYP;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;

public class Rechnungsausgabe extends AbstractAusgabe
{
  RechnungControl.TYP typ;

  private Formular formular;

  public Rechnungsausgabe(RechnungControl.TYP typ, Formular formular)
  {
    this.typ = typ;
    this.formular = formular;
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Mitglied m = ((Rechnung) object).getZahler();
    String filename = "";
    if (typ == TYP.RECHNUNG)
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
    return filename + "#" + typ.name();
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Rechnung re = (Rechnung) object;
    Map<String, Object> map = new RechnungMap().getMap(re, null);
    map = new MitgliedMap().getMap(re.getMitglied(), map);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      Rechnung rechnung = (Rechnung) object;
      if (typ == TYP.RECHNUNG)
      {
        return VorlageUtil.getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME,
            rechnung, rechnung.getMitglied());
      }
      else
      {
        return VorlageUtil.getName(VorlageTyp.MAHNUNG_MITGLIED_DATEINAME,
            rechnung, rechnung.getMitglied());
      }
    }
    else
    {
      if (typ == TYP.RECHNUNG)
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
      aufbereitung.addZUGFeRD((Rechnung) object, typ == TYP.MAHNUNG);
    }
  }

  @Override
  protected Formular getFormular(DBObject object) throws RemoteException
  {
    if (typ == TYP.MAHNUNG)
    {
      return formular;
    }
    return ((Rechnung) object).getFormular();
  }
}
