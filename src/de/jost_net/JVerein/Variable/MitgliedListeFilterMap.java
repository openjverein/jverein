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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.FilterControl;

public class MitgliedListeFilterMap extends AbstractMap
{

  public Map<String, Object> getMap(FilterControl control,
      Map<String, Object> inma) throws RemoteException
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

    for (MitgliedListeFilterVar var : MitgliedListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_AUSTRITT_VON_F:
          value = fromDate((Date) control.getAustrittvon().getValue());
          break;
        case DATUM_AUSTRITT_BIS_F:
          value = fromDate((Date) control.getAustrittbis().getValue());
          break;
        case BEITRAGSGRUPPE:
          value = control.getBeitragsgruppeAusw().getText();
          break;
        case DATUM_BIS_F:
          value = fromDate((Date) control.getDatumbis().getValue());
          break;
        case DATUM_VON_F:
          value = fromDate((Date) control.getDatumvon().getValue());
          break;
        case DIFFERENZ:
          value = control.getDifferenz().getText();
          break;
        case DIFFERENZ_LIMIT:
          Double limit = (Double) control.getDoubleAusw().getValue();
          if (limit != null)
          {
            value = Einstellungen.DECIMALFORMAT.format(limit);
          }
          else
          {
            value = "";
          }
          break;
        case EIGENSCHAFTEN:
          value = control.getEigenschaftenAuswahl().getText();
          break;
        case DATUM_EINTRITT_VON_F:
          value = fromDate((Date) control.getEintrittvon().getValue());
          break;
        case DATUM_EINTRITT_BIS_F:
          value = fromDate((Date) control.getEintrittbis().getValue());
          break;
        case DATUM_GEBURT_VON_F:
          value = fromDate((Date) control.getGeburtsdatumvon().getValue());
          break;
        case DATUM_GEBURT_BIS_F:
          value = fromDate((Date) control.getGeburtsdatumbis().getValue());
          break;
        case EXT_MITGLIEDSNUMMER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
            {
              value = control.getSuchExterneMitgliedsnummer().getValue()
                  .toString();
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case GESCHLECHT:
          value = control.getSuchGeschlecht().getText();
          break;
        case MAIL:
          value = control.getMailauswahl().getText();
          break;
        case MITGLIEDSCHAFT:
          value = control.getMitgliedStatus().getText();
          break;
        case MITGLIEDSNUMMER:
          try
          {
            if (!(Boolean) Einstellungen
                .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
            {
              Object o = control.getSuchMitgliedsnummer().getValue();
              if (o != null)
              {
                value = o.toString();
              }
              else
              {
                value = "";
              }
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case NAME:
          value = control.getSuchname().getValue().toString();
          break;
        case STICHTAG_F:
          value = fromDate((Date) control.getStichtag().getValue());
          break;
        case ZUSATZFELDER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.USEZUSATZFELDER))
            {
              value = control.getZusatzfelderAuswahl().getText();
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
  {
    Map<String, Object> map = null;
    if (inMap == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inMap;
    }
    for (MitgliedListeFilterVar var : MitgliedListeFilterVar.values())
    {
      Object value = null;
      switch (var)
      {
        case DATUM_AUSTRITT_VON_F:
          value = "20240101";
          break;
        case DATUM_AUSTRITT_BIS_F:
          value = "20241231";
          break;
        case BEITRAGSGRUPPE:
          value = "Alle";
          break;
        case DATUM_VON_F:
          value = "20240101";
          break;
        case DATUM_BIS_F:
          value = "20241231";
          break;
        case DIFFERENZ:
          value = "Fehlbetrag";
          break;
        case DIFFERENZ_LIMIT:
          value = "100";
          break;
        case EIGENSCHAFTEN:
          value = "+Eigenschaft";
          break;
        case DATUM_EINTRITT_VON_F:
          value = "20240101";
          break;
        case DATUM_EINTRITT_BIS_F:
          value = "20241231";
          break;
        case DATUM_GEBURT_BIS_F:
          value = "20241231";
          break;
        case DATUM_GEBURT_VON_F:
          value = "20000101";
          break;
        case EXT_MITGLIEDSNUMMER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
            {
              value = "Ext45";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case GESCHLECHT:
          value = "Alle";
          break;
        case MAIL:
          value = "Alle";
          break;
        case MITGLIEDSCHAFT:
          value = "Angemeldet";
          break;
        case MITGLIEDSNUMMER:
          try
          {
            if (!(Boolean) Einstellungen
                .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
            {
              value = "45";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
        case NAME:
          value = "Meier";
          break;
        case STICHTAG_F:
          value = "20240101";
          break;
        case ZUSATZFELDER:
          try
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.USEZUSATZFELDER))
            {
              value = "Kein Feld ausgewählt";
            }
          }
          catch (RemoteException e)
          {
            // Keine unterstützen
          }
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
