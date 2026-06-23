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
package de.jost_net.JVerein.keys;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.willuhn.logging.Logger;

public enum Kontoart implements KeyEnum
{
  // LIMIT ist keine Kontoart, sondern dient zur Abgrenzung.
  // Ids unter dem Limit werden regulär im Buchungsklassensaldo, Kontensaldo
  // und der Wirtschaftsplanung
  // berücksichtigt.
  // Ids über dem Limit werden in diesen Salden ignoriert.
  // Ebenfals ist LIMIT_RUECKLAGE keine Kontoart, sondern grenzt die
  // Rücklagekonten ab.
  GELD(1, "Geldkonto", "Geldvermögen"),
  ANLAGE(2, "Anlagenkonto", "Anlagevermögen"),
  SCHULDEN(3, "Fremdkapital"),
  LIMIT(100, "-- Limit --"),
  RUECKLAGE_ZWECK_GEBUNDEN(101,
      "Zweckgebundene Rücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_BETRIEBSMITTEL(102,
      "Betriebsmittelrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INVESTITION(103, "Investitionsrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INSTANDHALTUNG(104,
      "Instandhaltungsrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_WIEDERBESCHAFFUNG(105,
      "Wiederbeschaffungsrücklage nach § 62 Abs. 1 Nr. 2 AO"),
  RUECKLAGE_FREI(106, "Freie Rücklage nach § 62 Abs. 1 Nr. 3 AO"),
  RUECKLAGE_ERWERB(107,
      "Rücklage für Gesellschaftsrechte nach § 62 Abs. 1 Nr. 4 AO"),
  VERMOEGEN(108, "Vermögen nach § 62 Abs. 3 und 4 AO"),
  RUECKLAGE_SONSTIG(109, "Sonstige Rücklagen und Vermögen"),
  LIMIT_RUECKLAGE(200, "-- Limit Rücklage --"),
  VERBINDLICHKEITEN(201, "Verbindlichkeiten"),
  FORDERUNGEN(202, "Forderungen");

  private final String text;

  private final int key;

  private String textVermoegen;

  private Kontoart(int key, String text)
  {
    this(key, text, text);
  }

  private Kontoart(int key, String text, String textVermoegen)
  {
    this.key = key;
    this.text = text;
    this.textVermoegen = textVermoegen;
  }

  @Override
  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public String getTextVermoegen()
  {
    return textVermoegen;
  }

  public static Kontoart getByKey(int key)
  {
    for (Kontoart art : Kontoart.values())
    {
      if (art.getKey() == key)
      {
        return art;
      }
    }
    return null;
  }

  /**
   * Gibt die Liste der Kontoarten zurück, gefiltert je nach Einstellungen.
   * 
   * @return
   * @throws RemoteException
   */
  public static Kontoart[] getList()
  {
    List<Kontoart> values = new ArrayList<Kontoart>();
    try
    {
      boolean anlage = (Boolean) Einstellungen
          .getEinstellung(Property.ANLAGENKONTEN);
      boolean ruecklage = (Boolean) Einstellungen
          .getEinstellung(Property.RUECKLAGENKONTEN);
      boolean verbindlichkeiten = (Boolean) Einstellungen
          .getEinstellung(Property.VERBINDLICHKEITEN_FORDERUNGEN);

      for (Kontoart ka : Kontoart.values())
      {
        if (ka.getKey() < Kontoart.LIMIT.getKey() && ka != Kontoart.ANLAGE)
        {
          values.add(ka);
        }
        else if (anlage && ka == Kontoart.ANLAGE)
        {
          values.add(ka);
        }
        else if (ruecklage && ka.getKey() > Kontoart.LIMIT.getKey()
            && ka.getKey() < Kontoart.LIMIT_RUECKLAGE.getKey())
        {
          values.add(ka);
        }
        else if (verbindlichkeiten
            && ka.getKey() > Kontoart.LIMIT_RUECKLAGE.getKey())
        {
          values.add(ka);
        }
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim holen der Kontoliste", e);
    }
    return values.toArray(new Kontoart[0]);
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
