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
package de.jost_net.JVerein.server;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FelddefinitionImpl extends AbstractJVereinDBObject
    implements Felddefinition
{

  private static final long serialVersionUID = 1L;

  private Integer datentypeAlt;

  public FelddefinitionImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "felddefinition";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "name";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
          .createList(Zusatzfelder.class);
      it.addFilter("felddefinition = ?", getID());
      switch (getDatentyp())
      {
        case Datentyp.ZEICHENFOLGE:
          it.addFilter("feld IS NOT NULL AND TRIM(feld) != ''");
          break;
        case Datentyp.DATUM:
          it.addFilter("felddatum IS NOT NULL");
          break;
        case Datentyp.GANZZAHL:
          it.addFilter("feldganzzahl IS NOT NULL");
          break;
        case Datentyp.WAEHRUNG:
          it.addFilter("feldwaehrung IS NOT NULL");
          break;
        case Datentyp.JANEIN:
          it.addFilter("feldjanein IS TRUE");
          break;
      }
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException("Es ist noch bei Mitgliedern gesetzt.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Zusatzfeld kann nicht gelöscht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      // Prüfen, ob Konvertierung möglich ist
      if (datentypeAlt != null)
      {
        konvertiereTyp(true, datentypeAlt, getDatentyp());
      }

      if (getName() == null || getName().isEmpty())
      {
        throw new ApplicationException("Bitte Namen des Feldes eingeben");
      }
      if (getLabel() == null || getLabel().isEmpty())
      {
        throw new ApplicationException("Bitte Namen des Label eingeben");
      }
      String validChars = "abcdefghijklmnopqrstuvwxyz0123456789_";
      String testString = getName();
      for (int i = 0; i < testString.length(); i++)
      {
        char c = testString.charAt(i);
        if (validChars.indexOf(c) == -1)
          throw new ApplicationException(String.format(
              "Ungültiges Zeichen (%s) im Feldnamen an Position %d, nur 0-9, a-z, _ erlaubt!",
              c, i + 1));
      }
      Mitglied m = (Mitglied) Einstellungen.getDBService()
          .createObject(Mitglied.class, null);
      String[] namen = m.getAttributeNames();
      for (String s : namen)
      {
        if (getName().equals(s))
        {
          throw new ApplicationException(String.format(
              "%s ist ein reservierter Name und darf nicht verwendet werden.",
              getName()));
        }
      }
      if (getDatentyp() == Datentyp.ZEICHENFOLGE
          && (getLaenge() < 1 || getLaenge() > 1000))
      {
        throw new ApplicationException("Ungültige Feldlänge");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("insert check of felddefinition failed", e);
      throw new ApplicationException(
          "Felddefinition kann nicht gespeichert werden. Siehe system log");
    }
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    // Type Konvertieren
    if (datentypeAlt != null)
    {
      konvertiereTyp(false, datentypeAlt, getDatentyp());
    }
    super.store();
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  private void konvertiereTyp(boolean checkOnly, int typAlt, int typeNeu)
      throws RemoteException, ApplicationException
  {
    // Felddefinition enthält den bisherigen Stand, Datentyp ist die neue
    // Auswahl

    // Bei neuen Zusatzfeldern ist keine Prüfung möglich/erforderlich
    if (isNewObject())
    {
      return;
    }
    // Gibt es eine Veränderung? Wenn nicht: Ende der Prüfung
    if (typAlt == typeNeu)
    {
      return;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
        .createList(Zusatzfelder.class);
    it.addFilter("felddefinition = ?", getID());
    switch (typAlt)
    {
      case Datentyp.ZEICHENFOLGE:
        it.addFilter("feld is not null");
        break;
      case Datentyp.DATUM:
        it.addFilter("felddatum is not null");
        break;
      case Datentyp.GANZZAHL:
        it.addFilter("feldganzzahl is not null");
        break;
      case Datentyp.JANEIN:
        it.addFilter("feldjanein is not null");
        break;
      case Datentyp.WAEHRUNG:
        it.addFilter("feldwaehrung is not null");
        break;
    }
    // Wenn das Zusatzfeld noch bei keinem Mitglied angelegt wurde, kann der Typ
    // generell geändert werden.
    if (it.size() == 0)
    {
      return;
    }
    switch (typAlt)
    {
      case Datentyp.ZEICHENFOLGE:
        switch (typeNeu)
        {
          case Datentyp.DATUM:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              try
              {
                Date datum = sdf.parse(z.getFeld());
                if (!checkOnly)
                {
                  Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                      .createObject(Zusatzfelder.class, z.getID());
                  z1.setFeldDatum(datum);
                  z1.setFeld(null);
                  z1.store();
                }
              }
              catch (ParseException e)
              {
                fehlermeldung(1, z.getFeld());
              }
            }
            break;
          case Datentyp.GANZZAHL:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              try
              {
                int i = Integer.parseInt(z.getFeld());
                if (!checkOnly)
                {
                  Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                      .createObject(Zusatzfelder.class, z.getID());
                  z1.setFeldGanzzahl(i);
                  z1.setFeld(null);
                  z1.store();
                }
              }
              catch (NumberFormatException e)
              {
                fehlermeldung(1, z.getFeld());
              }
            }
            break;
          case Datentyp.JANEIN:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              String b = z.getFeld();
              Boolean bool = null;
              if (b == null || b.equals("") || b.equalsIgnoreCase("false")
                  || b.equalsIgnoreCase("nein"))
              {
                bool = Boolean.valueOf(false);
              }
              else if (b.equalsIgnoreCase("true") || b.equalsIgnoreCase("ja"))
              {
                bool = Boolean.valueOf(true);
              }
              else
              {
                fehlermeldung(1, z.getFeld());
              }
              if (!checkOnly)
              {
                Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                    .createObject(Zusatzfelder.class, z.getID());
                z1.setFeldJaNein(bool);
                z1.setFeld(null);
                z1.store();
              }

            }
            break;
          case Datentyp.WAEHRUNG:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              try
              {
                Number doub = Einstellungen.DECIMALFORMAT.parse(z.getFeld());
                if (!checkOnly)
                {
                  Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                      .createObject(Zusatzfelder.class, z.getID());
                  z1.setFeldWaehrung(BigDecimal.valueOf(doub.doubleValue()));
                  z1.setFeld(null);
                  z1.store();
                }
              }
              catch (ParseException e)
              {
                fehlermeldung(1, z.getFeld());
              }
            }
            break;
        }
        break;
      case Datentyp.DATUM:
        switch (typeNeu)
        {
          case Datentyp.ZEICHENFOLGE:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              if (!checkOnly)
              {
                Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                    .createObject(Zusatzfelder.class, z.getID());
                z1.setFeld(new JVDateFormatTTMMJJJJ().format(z.getFeldDatum()));
                z1.setFeldDatum(null);
                z1.store();
              }
            }
            return;
          default:
            fehlermeldung(2, "");
        }
        break;
      case Datentyp.GANZZAHL:
        switch (typeNeu)
        {
          case Datentyp.ZEICHENFOLGE:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              if (!checkOnly)
              {
                Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                    .createObject(Zusatzfelder.class, z.getID());
                z1.setFeld(z.getFeldGanzzahl() + "");
                z1.setFeldGanzzahl(null);
                z1.store();
              }
            }
            return;
          default:
            fehlermeldung(2, "");
        }
        break;
      case Datentyp.JANEIN:
        switch (typeNeu)
        {
          case Datentyp.ZEICHENFOLGE:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              if (!checkOnly)
              {
                Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                    .createObject(Zusatzfelder.class, z.getID());
                z1.setFeld(z.getFeldJaNein() ? "ja" : "nein");
                z1.setFeldJaNein(null);
                z1.store();
              }
            }
            return;
          default:
            fehlermeldung(2, "");
        }
        break;
      case Datentyp.WAEHRUNG:
        switch (typeNeu)
        {
          case Datentyp.ZEICHENFOLGE:
            while (it.hasNext())
            {
              Zusatzfelder z = it.next();
              if (!checkOnly)
              {
                Zusatzfelder z1 = (Zusatzfelder) Einstellungen.getDBService()
                    .createObject(Zusatzfelder.class, z.getID());
                z1.setFeld(
                    Einstellungen.DECIMALFORMAT.format(z.getFeldWaehrung()));
                z1.setFeldWaehrung(null);
                z1.store();
              }
            }
            return;
          default:
            fehlermeldung(2, "");
        }
        break;
    }

  }

  private void fehlermeldung(int fehlertyp, String wert)
      throws ApplicationException
  {
    switch (fehlertyp)
    {
      case 1:
        throw new ApplicationException(
            "Typkonvertierung kann nicht durchgeführt werden. Inhalt: " + wert);
      case 2:
        throw new ApplicationException(
            "Die Konvertierung der Datentypen ist nicht vorgesehen. Ggfls. zunächst in Zeichenfolge umwandeln.");
    }
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public String getLabel() throws RemoteException
  {
    return (String) getAttribute("label");
  }

  @Override
  public void setLabel(String label) throws RemoteException
  {
    setAttribute("label", label);
  }

  @Override
  public int getDatentyp() throws RemoteException
  {
    Integer i = (Integer) getAttribute("datentyp");
    if (i == null)
    {
      return 0;
    }
    else
    {
      return i;
    }
  }

  @Override
  public void setDatentyp(int datentyp) throws RemoteException
  {
    // Alten Datentyp für die Konvertierung speichern
    if (datentypeAlt == null && getDatentyp() != datentyp)
    {
      datentypeAlt = getDatentyp();
    }
    setAttribute("datentyp", datentyp);
  }

  @Override
  public int getLaenge() throws RemoteException
  {
    Integer i = (Integer) getAttribute("laenge");
    if (i == null)
    {
      return 0;
    }
    else
    {
      return i;
    }
  }

  @Override
  public void setLaenge(int laenge) throws RemoteException
  {
    setAttribute("laenge", laenge);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    return super.getAttribute(fieldName);
  }

  @Override
  public String getObjektName()
  {
    return "Zusatzfeld";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Zusatzfelder";
  }
}
