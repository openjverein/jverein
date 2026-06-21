/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.io;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;

public abstract class BuchungAuswertungExportPDF implements Exporter
{
  protected double summe = 0;

  protected double summeeinnahmen = 0;

  protected double summeausgaben = 0;

  protected double summeumbuchungen = 0;

  protected boolean kontonummer_in_buchungsliste = false;

  protected boolean getKontonummer() throws RemoteException
  {
    if (Boolean.valueOf((Boolean) Einstellungen
        .getEinstellung(Property.KONTONUMMERINBUCHUNGSLISTE)))
    {
      return true;
    }
    return false;
  }

  protected ArrayList<Buchungsart> getBuchungsarten(BuchungQuery query)
      throws RemoteException
  {
    ArrayList<Buchungsart> buchungsarten = new ArrayList<>();
    if (!(query.getBuchungsart() != null
        && query.getBuchungsart().getID() == null))
    {
      DBIterator<Buchungsart> list = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      if (query.getBuchungsart() != null
          && query.getBuchungsart().getID() != null)
      {
        list.addFilter("id = ?",
            new Object[] { query.getBuchungsart().getID() });
      }

      list.setOrder("ORDER BY nummer");

      while (list.hasNext())
      {
        buchungsarten.add(list.next());
      }
    }
    return buchungsarten;
  }

  protected List<Buchung> getBuchungenEinerBuchungsart(List<Buchung> buchungen,
      Buchungsart bua, Buchungsklasse bukla) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() == null
          || !b.getBuchungsart().getNummer().equals(bua.getNummer()))
      {
        continue;
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (b.getBuchungsklasseId() == null
            || !b.getBuchungsklasse().getNummer().equals(bukla.getNummer()))
        {
          continue;
        }
      }
      liste.add(b);
    }
    return liste;
  }

  // Buchungen einer Buchungsart ohne Buchungsklasse
  protected List<Buchung> getBuchungenEinerBuchungsart(List<Buchung> buchungen,
      Buchungsart bua) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() == null
          || !b.getBuchungsart().getNummer().equals(bua.getNummer()))
      {
        continue;
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (b.getBuchungsklasseId() != null)
        {
          continue;
        }
      }
      liste.add(b);
    }
    return liste;
  }

  // Buchungen ohne Buchungsart
  protected List<Buchung> getBuchungenOhneBuchungsart(List<Buchung> buchungen)
      throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() != null)
      {
        continue;
      }
      liste.add(b);
    }
    return liste;
  }
}
