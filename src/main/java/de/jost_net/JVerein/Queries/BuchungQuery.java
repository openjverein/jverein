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
package de.jost_net.JVerein.Queries;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.Suchbetrag;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.MitgliedZugeordnetFilter;
import de.jost_net.JVerein.keys.SplitbuchungFilter;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.util.ApplicationException;

public class BuchungQuery
{
  private List<Buchung> ergebnis;

  private boolean geldkonto;

  public String ordername = null;

  private HashMap<String, String> sortValues = new HashMap<String, String>();

  private Map<Filter, Object> filter;

  private void SortHashMap()
  {
    sortValues.put("ORDER_ID", "order by id");
    sortValues.put("ORDER_DATUM", "order by datum");
    sortValues.put("ORDER_DATUM_NAME", "order by datum, name");
    sortValues.put("ORDER_DATUM_ID", "order by datum, id");
    sortValues.put("ORDER_DATUM_ID_NAME", "order by datum, id, name");
    sortValues.put("ORDER_DATUM_AUSZUGSNUMMER",
        "order by datum, auszugsnummer");
    sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_NAME",
        "order by datum, auszugsnummer, name");
    sortValues.put("ORDER_DATUM_BLATTNUMMER", "order by datum, blattnummer");
    sortValues.put("ORDER_DATUM_BLATTNUMMER_NAME",
        "order by datum, blattnummer, name");
    sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_ID",
        "order by datum, auszugsnummer, id");
    sortValues.put("ORDER_DATUM_BLATTNUMMER_ID",
        "order by datum, blattnummer, id");
    sortValues.put("ORDER_DATUM_AUSZUGSNUMMER_BLATTNUMMER_ID",
        "order by datum, auszugsnummer, blattnummer, id");
    sortValues.put("DEFAULT", "order by datum");
  }

  public BuchungQuery(Map<Filter, Object> filter, boolean geldkonto)
      throws ApplicationException
  {
    this.filter = filter;
    this.geldkonto = geldkonto;
  }

  public String getOrder(String value)
  {
    SortHashMap();
    String newvalue = null;
    if (value == null)
    {
      return sortValues.get("DEFAULT");
    }
    else
    {
      newvalue = value.replaceAll(", ", "_");
      newvalue = newvalue.toUpperCase();
      newvalue = "ORDER_" + newvalue;
      return sortValues.get(newvalue);
    }
  }

  public void setOrdername(String value)
  {
    if (value != null)
    {
      ordername = value;
    }
  }

  @SuppressWarnings("unchecked")
  public List<Buchung> get() throws RemoteException, ApplicationException
  {
    final DBService service = Einstellungen.getDBService();
    DBIterator<Buchung> it = service.createList(Buchung.class);

    if (!geldkonto && filter.get(Filter.KONTO) == null)
    {
      it.join("konto");
      it.addFilter("konto.id = buchung.konto");
      it.addFilter("kontoart = ?", Kontoart.ANLAGE.getKey());
    }

    for (Entry<Filter, Object> entry : filter.entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case DATUM_VON:
          it.addFilter("buchung.datum >= ? ", (Date) value);
          break;
        case DATUM_BIS:
          it.addFilter("buchung.datum <= ? ", (Date) value);
          break;
        case KONTO:
          Konto konto = (Konto) value;
          it.addFilter("buchung.konto = ? ", konto.getID());
          break;
        case BUCHUNGSART:
          Buchungsart buchungart = (Buchungsart) value;
          if (buchungart.getNummer().isEmpty())
          {
            it.addFilter("buchung.buchungsart is null ");
          }
          else
          {
            it.addFilter("buchung.buchungsart = ? ", buchungart.getID());
          }
          break;
        case PROJEKT:
          Projekt projekt = (Projekt) value;
          if (projekt.getID() == null)
          {
            it.addFilter("projekt is null");
          }
          else
          {
            it.addFilter("projekt = ?", projekt.getID());
          }
          break;
        case ENTHALTENER_TEXT:
          String text = (String) value;
          if (text != null && text.length() > 0)
          {
            Long id = 0L;
            try
            {
              id = Long.parseLong(text);
            }
            catch (Exception e)
            {

            }
            String ttext = text.toUpperCase();
            ttext = "%" + ttext + "%";
            it.addFilter(
                "(upper(buchung.name) like ? or upper(buchung.zweck) like ? "
                    + "or upper(buchung.kommentar) like ? or buchung.id = ?) ",
                ttext, ttext, ttext, id);
          }
          break;
        case BETRAG:
          String betrag = (String) value;
          try
          {
            Suchbetrag suchbetrag = new Suchbetrag(betrag);
            switch (suchbetrag.getSuchstrategie())
            {
              case GLEICH:
              {
                it.addFilter("buchung.betrag = ?", suchbetrag.getBetrag());
                break;
              }
              case GRÖSSER:
              {
                it.addFilter("buchung.betrag > ?", suchbetrag.getBetrag());
                break;
              }
              case GRÖSSERGLEICH:
              {
                it.addFilter("buchung.betrag >= ?", suchbetrag.getBetrag());
                break;
              }
              case BEREICH:
                it.addFilter("buchung.betrag >= ? AND buchung.betrag <= ?",
                    suchbetrag.getBetrag(), suchbetrag.getBetrag2());
                break;
              case KEINE:
                break;
              case KLEINER:
                it.addFilter("buchung.betrag < ?", suchbetrag.getBetrag());
                break;
              case KLEINERGLEICH:
                it.addFilter("buchung.betrag <= ?", suchbetrag.getBetrag());
                break;
              case BETRAG:
                it.addFilter("(buchung.betrag = ? OR buchung.betrag = ?)",
                    suchbetrag.getBetrag(), suchbetrag.getBetrag().negate());
                break;
              default:
                break;
            }
          }
          catch (Exception e)
          {
            // throw new RemoteException(e.getMessage());
          }
          break;
        case MITGLIED_ZUGEORDNET:
          MitgliedZugeordnetFilter filter = (MitgliedZugeordnetFilter) value;
          if (filter == MitgliedZugeordnetFilter.JA)
          {
            it.addFilter(Buchung.SOLLBUCHUNG + " is not null");
          }
          else if (filter == MitgliedZugeordnetFilter.NEIN)
          {
            it.addFilter(Buchung.SOLLBUCHUNG + " is null");
          }
          break;
        case MITGLIED_NAME:
          String mitglied = (String) value;
          String mitgliedsuche = "%" + mitglied.toLowerCase() + "%";
          it.join(Sollbuchung.TABLE_NAME);
          it.addFilter(Sollbuchung.TABLE_NAME_ID + " = " + Buchung.SOLLBUCHUNG);
          it.join("mitglied");
          it.addFilter("mitglied.id = " + Sollbuchung.T_MITGLIED);
          it.addFilter(
              "(lower(mitglied.name) like ? or lower(mitglied.vorname) like ?)",
              new Object[] { mitgliedsuche, mitgliedsuche });
          break;
        case SPLITBUCHUNG:
          SplitbuchungFilter split = (SplitbuchungFilter) value;
          switch (split)
          {
            case SPLIT:
              it.addFilter("(buchung.splittyp is null or buchung.splittyp = ?)",
                  SplitbuchungTyp.SPLIT);
              break;
            case HAUPT:
              it.addFilter("(buchung.splittyp is null or buchung.splittyp = ?)",
                  SplitbuchungTyp.HAUPT);
              break;
            default:
              break;
          }
          break;
        case UNGEPRUEFT:
          Boolean ungeprueft = (Boolean) value;
          if (ungeprueft != null && ungeprueft)
          {
            it.addFilter("(geprueft = 0 or geprueft is null)");
          }
          break;
        case STEUER:
          Steuer steuer = (Steuer) value;
          if (steuer.getID() == null)
          {
            it.addFilter("steuer is null");
          }
          else
          {
            it.addFilter("steuer = ? ", steuer.getID());
          }
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }

    // 20220823: sbuer: Neue Sortierfelder
    SortHashMap();
    String orderString = getOrder(ordername);
    // System.out.println("ordervalue : " + ordername + " ,orderString : " +
    // orderString);
    it.setOrder(orderString);

    this.ergebnis = it != null ? PseudoIterator.asList(it) : null;
    return ergebnis;
  }

  public Buchungsart getBuchungsart()
  {
    return (Buchungsart) filter.get(Filter.BUCHUNGSART);
  }

  public int getSize()
  {
    return ergebnis.size();
  }

}
