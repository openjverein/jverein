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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

public class PreNotificationAusgabe extends AbstractAusgabe
{
  private Formular formular;

  private Ausgabeart art;

  public PreNotificationAusgabe(Formular formular)
  {
    this.formular = formular;
  }

  @Override
  public void aufbereiten(ArrayList<? extends DBObject> list, Ausgabeart art,
      String betreff, String text, boolean pdfa, boolean encrypt)
      throws IOException, ApplicationException, DocumentException
  {
    this.art = art;
    super.aufbereiten(list, art, betreff, text, pdfa, encrypt);
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    Lastschrift ls = (Lastschrift) object;
    String filename = (ls.getMitglied() == null ? "" : ls.getMitglied().getID())
        + "#lastschrift#" + object.getID() + "#";
    String email = StringTool.toNotNullString(ls.getEmail());
    if (email.length() > 0)
    {
      filename += email;
    }
    else
    {
      filename += ls.getName() + ls.getVorname();
    }
    return filename + "#PreNotification";
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Map<String, Object> map = new LastschriftMap().getMap((Lastschrift) object,
        null);
    return new AllgemeineMap().getMap(map);
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      return VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_MITGLIED_DATEINAME,
          object, ((Lastschrift) object).getMitglied());
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.PRENOTIFICATION_DATEINAME);
    }
  }

  @Override
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object)
      throws IOException, DocumentException, ApplicationException
  {
    // Bei Mailversand ist nicht zwingend ein Formular notwendig
    if (art != Ausgabeart.MAIL || formular != null)
    {
      super.createPDF(formular, aufbereitung, file, object);
    }
  }

  @Override
  protected void closeDocument(FormularAufbereitung aufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    // Bei Mailversand ist nicht zwingend ein Formular notwendig, daher auch
    // nicht schließen
    if (art != Ausgabeart.MAIL || formular != null)
    {
      super.closeDocument(aufbereitung, object);
    }
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    return formular;
  }

}
