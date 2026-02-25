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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;

public class AllgemeineMap extends AbstractMap
{
  public Map<String, Object> getMap(Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }
    for (AllgemeineVar var : AllgemeineVar.values())
    {
      Calendar calendar;
      Object value = null;
      switch (var)
      {
        case AKTUELLESDATUM:
          value = new SimpleDateFormat("yyyyMMdd").format(new Date());
          break;
        case AKTUELLEZEIT:
          value = new SimpleDateFormat("HHmmss").format(new Date());
          break;
        case TAGESDATUM:
          value = ttmmjjjj.format(new Date());
          break;
        case TAGESDATUMTT:
          value = tt.format(new Date());
          break;
        case TAGESDATUMMM:
          value = mm.format(new Date());
          break;
        case TAGESDATUMJJJJ:
          value = jjjj.format(new Date());
          break;
        case AKTUELLESJAHR:
          value = jjjj.format(new Date());
          break;
        case AKTUELLERMONAT:
          value = mmjjjj.format(new Date());
          break;
        case FOLGEMONAT:
          calendar = Calendar.getInstance();
          calendar.add(Calendar.MONTH, 1);
          value = mmjjjj.format(calendar.getTime());
          break;
        case VORMONAT:
          calendar = Calendar.getInstance();
          calendar.add(Calendar.MONTH, -1);
          value = mmjjjj.format(calendar.getTime());
          break;
        case FOLGEJAHR:
          calendar = Calendar.getInstance();
          calendar.add(Calendar.YEAR, 1);
          value = jjjj.format(calendar.getTime());
          break;
        case VORJAHR:
          calendar = Calendar.getInstance();
          calendar.add(Calendar.YEAR, -1);
          value = jjjj.format(calendar.getTime());
          break;
        case NAME:
          value = (String) Einstellungen.getEinstellung(Property.NAME);
          break;
        case STRASSE:
          value = (String) Einstellungen.getEinstellung(Property.STRASSE);
          break;
        case PLZ:
          value = (String) Einstellungen.getEinstellung(Property.PLZ);
          break;
        case ORT:
          value = (String) Einstellungen.getEinstellung(Property.ORT);
          break;
        case ABSENDER:
          value = (String) Einstellungen.getEinstellung(Property.NAME) + ", "
              + (String) Einstellungen.getEinstellung(Property.STRASSE) + ", "
              + (String) Einstellungen.getEinstellung(Property.PLZ) + " "
              + (String) Einstellungen.getEinstellung(Property.ORT);
          break;
        case STAAT:
          value = Staat
              .getByKey((String) Einstellungen.getEinstellung(Property.STAAT))
              .getText();
          break;
        case IBAN:
          value = new IBANFormatter()
              .format((String) Einstellungen.getEinstellung(Property.IBAN));
          break;
        case BIC:
          value = (String) Einstellungen.getEinstellung(Property.BIC);
          break;
        case BANK_NAME:
          String bic = (String) Einstellungen.getEinstellung(Property.BIC);
          value = "";
          if (!bic.isEmpty())
          {
            Bank b = Banken.getBankByBIC(bic.toUpperCase());
            if (b != null)
            {
              value = b.getBezeichnung();
            }
          }
          break;
        case GLAEUBIGER_ID:
          value = (String) Einstellungen.getEinstellung(Property.GLAEUBIGERID);
          break;
        case UST_ID:
          value = (String) Einstellungen.getEinstellung(Property.USTID);
          break;
        case STEUER_NR:
          value = (String) Einstellungen.getEinstellung(Property.STEUERNUMMER);
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
