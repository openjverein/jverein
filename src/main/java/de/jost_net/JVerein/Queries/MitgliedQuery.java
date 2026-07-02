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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Differenz;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Geschlecht;
import de.jost_net.JVerein.keys.MailAuswahl;
import de.jost_net.JVerein.keys.MitgliedStatus;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Settings;

public class MitgliedQuery
{

  private FilterControl control;

  private boolean and = false;

  private String sql = "";

  String zusatzfeld = null;

  String zusatzfelder = null;

  String sort = "";

  public MitgliedQuery(FilterControl control)
  {
    this.control = control;
  }

  public enum MitgliedAuswahl
  {
    ALLE,
    MITGLIEDER,
    NICHTMITGLIEDER
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Mitglied> get(MitgliedAuswahl mitgliedAuswahl, String sort)
      throws RemoteException, ApplicationException
  {

    this.sort = sort;
    zusatzfeld = control.getAdditionalparamprefix1();
    zusatzfelder = control.getAdditionalparamprefix2();

    ArrayList<Object> bedingungen = new ArrayList<>();

    sql = "select distinct mitglied.*, ucase(name), ucase(vorname) ";
    sql += "from mitglied ";

    Map<Filter, Object> filter = control.getFilter();

    Settings settings = control.getSettings();
    char synonym = 'a';
    if (settings.getInt(zusatzfelder + "selected", 0) > 0)
    {
      for (int i = 1; i <= settings.getInt(zusatzfelder + "counter", 0); i++)
      {
        int definition = settings.getInt(zusatzfeld + i + ".definition", -1);
        switch (settings.getInt(zusatzfeld + i + ".datentyp", -1))
        {
          case Datentyp.ZEICHENFOLGE:
          {
            String val = settings.getString(zusatzfeld + i + ".value", null)
                .replace('*', '%');
            String cond = settings.getString(zusatzfeld + i + ".cond", null);
            if (val != null && val.length() > 0)
            {
              sql += "join zusatzfelder " + synonym + " on " + synonym
                  + ".mitglied = mitglied.id  and lower(" + synonym + ".FELD) "
                  + cond + " lower( ? ) and " + synonym
                  + ".felddefinition = ? ";
              synonym++;
              bedingungen.add(val);
              bedingungen.add(definition);
            }
            break;
          }
          case Datentyp.DATUM:
          {
            String val = settings.getString(zusatzfeld + i + ".value", null);
            String cond = settings.getString(zusatzfeld + i + ".cond", null);
            if (val != null)
            {
              try
              {
                Date datum = new JVDateFormatTTMMJJJJ().parse(val);
                sql += "join zusatzfelder " + synonym + " on " + synonym
                    + ".mitglied = mitglied.id  and " + synonym + ".FELDDATUM "
                    + cond + " ? and " + synonym + ".felddefinition = ? ";
                bedingungen.add(datum);
                bedingungen.add(definition);
                synonym++;
              }
              catch (ParseException e)
              {
                //
              }
            }
            break;
          }
          case Datentyp.GANZZAHL:
          {
            Integer val = null;
            String tmp = settings.getString(zusatzfeld + i + ".value", "");
            if (tmp != null && !tmp.isEmpty())
            {
              val = Integer.parseInt(tmp);
            }
            String cond = settings.getString(zusatzfeld + i + ".cond", null);
            if (val != null)
            {
              sql += "join zusatzfelder " + synonym + " on " + synonym
                  + ".mitglied = mitglied.id  and " + synonym + ".FELDGANZZAHL "
                  + cond + " ? and " + synonym + ".felddefinition = ? ";
              bedingungen.add(val);
              bedingungen.add(definition);
              synonym++;
            }
            break;
          }
          case Datentyp.JANEIN:
          {
            boolean val = settings.getBoolean(zusatzfeld + i + ".value", false);
            if (val)
            {
              sql += "join zusatzfelder " + synonym + " on " + synonym
                  + ".mitglied = mitglied.id  and " + synonym
                  + ".FELDJANEIN = true and " + synonym
                  + ".felddefinition = ? ";
              bedingungen.add(definition);
              synonym++;
            }
            break;
          }
          case Datentyp.WAEHRUNG:
          {
            String val = settings.getString(zusatzfeld + i + ".value", null);
            String cond = settings.getString(zusatzfeld + i + ".cond", null);
            if (val != null)
            {
              try
              {
                Number n = Einstellungen.DECIMALFORMAT.parse(val);
                sql += "join zusatzfelder " + synonym + " on " + synonym
                    + ".mitglied = mitglied.id  and " + synonym
                    + ".FELDWAEHRUNG " + cond + " ? and " + synonym
                    + ".felddefinition = ? ";
                bedingungen.add(n);
                bedingungen.add(definition);
              }
              catch (ParseException e)
              {
                //
              }
              synonym++;
            }
            break;
          }
        }
      }
    }

    Beitragsgruppe bg = (Beitragsgruppe) filter.get(Filter.BEITRAGSGRUPPE);
    if (bg != null)
    {
      sql += " left join sekundaerebeitragsgruppe on sekundaerebeitragsgruppe.mitglied = mitglied.id ";
      addCondition(
          "(mitglied.beitragsgruppe = ? OR sekundaerebeitragsgruppe.beitragsgruppe = ?)");
      bedingungen.add(bg.getID());
      bedingungen.add(bg.getID());
    }
    // Wenn der MitgliedsTyp Filter vorhanden ist, diesen verwenden, sonst den
    // übergebenen Wert.
    if (filter.get(Filter.MITGLIEDSTYP) != null)
    {
      addCondition(Mitglied.MITGLIEDSTYP + " = "
          + ((Mitgliedstyp) filter.get(Filter.MITGLIEDSTYP)).getID());
    }
    else
    {
      switch (mitgliedAuswahl)
      {
        case MITGLIEDER:
          addCondition(Mitglied.MITGLIEDSTYP + " = " + Mitgliedstyp.MITGLIED);
          break;
        case NICHTMITGLIEDER:
          addCondition(Mitglied.MITGLIEDSTYP + " != " + Mitgliedstyp.MITGLIED);
          break;
        case ALLE:
          break;
      }
    }

    for (Entry<Filter, Object> entry : filter.entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case MITGLIEDSCHAFT_STATUS:
          Date stichtag = (Date) filter.get(Filter.STICHTAG);
          if (stichtag == null)
          {
            stichtag = new Date();
          }
          switch ((MitgliedStatus) value)
          {
            case ANGEMELDET:
              addCondition("(eintritt is null or eintritt <= ?)");
              bedingungen.add(stichtag);
              addCondition("(austritt is null or austritt > ?)");
              bedingungen.add(stichtag);
              break;
            case ABGEMELDET:
              addCondition(
                  "((austritt is not null and austritt <= ?) or (eintritt is not null and eintritt > ?))");
              bedingungen.add(stichtag);
              bedingungen.add(stichtag);
              break;
          }
          break;
        case MAIL:
          switch ((MailAuswahl) value)
          {
            case OHNE:
              addCondition("(email is null or length(email) = 0)");
              break;
            case MIT:
              addCondition("(email is  not null and length(email) > 0)");
              break;
          }
          break;
        case NAME:
          String tmpSuchname = ((String) value).toLowerCase() + "%";
          addCondition("(lower(name) like ? or lower(vorname) like ?) ");
          bedingungen.add(tmpSuchname);
          bedingungen.add(tmpSuchname);
          break;
        case GEBURTSDATUM_VON:
          addCondition("geburtsdatum >= ?");
          bedingungen.add(value);
          break;
        case GEBURTSDATUM_BIS:
          addCondition("geburtsdatum <= ?");
          bedingungen.add(value);
          break;
        case STERBEDATUM_VON:
          addCondition("sterbetag >= ?");
          bedingungen.add(value);
          break;
        case STERBEDATUM_BIS:
          addCondition("sterbetag <= ?");
          bedingungen.add(value);
          break;
        case GESCHLECHT:
          addCondition("geschlecht = ?");
          bedingungen.add(((Geschlecht) value).getStringKey());
          break;
        case EINTRITT_VON:
          addCondition("eintritt >= ?");
          bedingungen.add(value);
          break;
        case EINTRITT_BIS:
          addCondition("eintritt <= ?");
          bedingungen.add(value);
          break;
        case AUSTRITT_VON:
          addCondition("austritt >= ?");
          bedingungen.add(value);
          break;
        case AUSTRITT_BIS:
          addCondition("austritt <= ?");
          bedingungen.add(value);
          break;
        case EXTERNEMITGLIEDSNUMMER:
          addCondition("externemitgliedsnummer = ?");
          bedingungen.add(value);
          break;
        case MITGLIEDSNUMMER:
          addCondition("mitglied.id = ?");
          bedingungen.add(value);
          break;
        case DIFFERENZ:
          Double limit = (Double) filter.get(Filter.DIFFERENZ_LIMIT);

          if (limit == null)
          {
            limit = 0.005d;
          }
          // Es ist egal ob der Betrag positiv oder negativ eingetragen wurde
          limit = Math.abs(limit);

          ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
              Sollbuchung.TABLE_NAME);
          it.addColumn(Sollbuchung.T_MITGLIED + " as mid");
          it.addColumn("sum(cast(COALESCE(buchung.ist,0) - COALESCE("
              + Sollbuchung.T_BETRAG + ",0) AS DECIMAL(10,2))) as dif");
          it.leftJoin(
              "(SELECT sum(COALESCE((betrag),0)) AS ist,"
                  + Buchung.T_SOLLBUCHUNG + " FROM buchung GROUP BY "
                  + Buchung.T_SOLLBUCHUNG + ") AS buchung",
              Buchung.T_SOLLBUCHUNG + " = " + Sollbuchung.TABLE_NAME_ID);

          Date von = (Date) filter.get(Filter.DATUM_VON);
          if (von != null)
          {
            it.addFilter(Sollbuchung.T_DATUM + " >= ?", von);
          }
          Date bis = (Date) filter.get(Filter.DATUM_BIS);
          if (bis != null)
          {
            it.addFilter(Sollbuchung.T_DATUM + " <= ?", bis);
          }
          if (value == Differenz.FEHLBETRAG)
          {
            it.addHaving("dif < ?", -limit);
          }
          else
          {
            it.addHaving("dif > ?", limit);
          }
          it.addGroupBy(Sollbuchung.T_MITGLIED);
          ArrayList<String> diffIds = new ArrayList<>();
          while (it.hasNext())
          {
            diffIds.add(it.next().getAttribute("mid").toString());
          }

          if (diffIds.size() == 0)
          {
            return new ArrayList<Mitglied>();
          }
          addCondition("mitglied.id in (" + String.join(",", diffIds) + ")");
          break;
        case BEITRAGSGRUPPE:
        case MITGLIEDSTYP:
        case STICHTAG:
        case DIFFERENZ_LIMIT:
        case DATUM_BIS:
        case DATUM_VON:
        case EIGENSCHAFTEN:
        case ZUSATZFELD:
          // werden oben abgefragt
          break;
        case JAHR:
        case UEBERSCHRIFT:
          // Nur für Auswertung
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }

    Logger.debug(sql);

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Long> list = new ArrayList<>();
        while (rs.next())
        {
          list.add(rs.getLong(1));
        }
        return list;
      }
    };

    ArrayList<Long> mitgliederIds = (ArrayList<Long>) Einstellungen
        .getDBService().execute(sql, bedingungen.toArray(), rs);

    EigenschaftenAuswahlParameter param = (EigenschaftenAuswahlParameter) filter
        .get(Filter.EIGENSCHAFTEN);
    if (param != null && param.getEigenschaften().size() > 0)
    {
      ArrayList<Long> suchIds = new ArrayList<>();
      HashMap<Long, String> suchauswahl = new HashMap<>();
      for (Entry<Eigenschaft, String> entry : param.getEigenschaften()
          .entrySet())
      {
        Long id = Long.valueOf(entry.getKey().getID());
        suchIds.add(id);
        suchauswahl.put(id, entry.getValue());
      }

      // Eigenschaften lesen
      String sql = "SELECT eigenschaften.* from eigenschaften ";
      List<Long[]> mitgliedEigenschaften = (List<Long[]>) Einstellungen
          .getDBService().execute(sql, new Object[] {}, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              List<Long[]> list = new ArrayList<>();
              while (rs.next())
              {
                list.add(new Long[] { rs.getLong(2), rs.getLong(3) }); // Mitglied.Id,
                                                                       // Eigenschaft.Id
              }
              return list;
            }
          });
      mitgliederIds = getFilteredIds(mitgliederIds, suchIds, suchauswahl,
          mitgliedEigenschaften, param.getVerknuepfung());
    }
    return getMitglieder(mitgliederIds);
  }

  private ArrayList<Mitglied> getMitglieder(ArrayList<Long> ids)
      throws RemoteException
  {
    if (ids.size() == 0)
      return new ArrayList<Mitglied>();

    DBIterator<Mitglied> list = Einstellungen.getDBService()
        .createList(Mitglied.class);
    list.addFilter("id in (" + StringUtils.join(ids, ",") + ")");
    if (sort != null && !sort.isEmpty())
    {
      if (sort.equals("Name, Vorname"))
      {
        list.setOrder("ORDER BY ucase(name), ucase(vorname)");
      }
      else if (sort.equals("Eintrittsdatum"))
      {
        list.setOrder(" ORDER BY eintritt");
      }
      else if (sort.equals("Geburtsdatum"))
      {
        list.setOrder("ORDER BY geburtsdatum");
      }
      else if (sort.equals("Geburtstagsliste"))
      {
        list.setOrder("ORDER BY month(geburtsdatum), day(geburtsdatum)");
      }
    }
    else
    {
      list.setOrder("ORDER BY name, vorname");
    }
    @SuppressWarnings("unchecked")
    ArrayList<Mitglied> mitglieder = list != null
        ? (ArrayList<Mitglied>) PseudoIterator.asList(list)
        : null;
    return mitglieder;
  }

  private void addCondition(String condition)
  {
    if (and)
    {
      sql += " AND ";
    }
    else
    {
      sql += "where ";
    }
    and = true;
    sql += condition;
  }

  /**
   * Die Methode evaluiert die Eigenschaften der Mitglieder.
   * 
   * @param mitgliederIds
   *          Liste der Mitglieder IDs
   * @param suchIds
   *          Liste der auszuwertenden Eigenschaften IDs
   * @param suchauswahl
   *          Map mit dem Vorzeichen der Eigenschaft PLUS/MINUS
   * @param mitgliedEigenschaften
   *          Liste der Mitglied/Eigenschaften, Index 0 ist Mitglied, Index 1
   *          ist Eigenschaft
   * @param verknuepfung
   *          Verknüpfung der Eigenschaften UND/ODER
   * @return Gefilterte Mitglieder IDs
   */
  public static ArrayList<Long> getFilteredIds(ArrayList<Long> mitgliederIds,
      ArrayList<Long> suchIds, HashMap<Long, String> suchauswahl,
      List<Long[]> mitgliedEigenschaften, String verknuepfung)
  {
    ArrayList<Long> mitgliederIdsFiltered = new ArrayList<>();
    for (Long mitglied : mitgliederIds)
    {
      ArrayList<Long> mitgliedeigenschaftenIds = new ArrayList<>();
      for (Long[] value : mitgliedEigenschaften)
      {
        if (value[0].equals(mitglied))
          mitgliedeigenschaftenIds.add(value[1]);
      }

      boolean ok = false;
      for (Long suchId : suchIds)
      {
        if (verknuepfung.equals(EigenschaftenAuswahlParameter.UND))
        {
          ok = true;
          if (suchauswahl.get(suchId).equals(EigenschaftenNode.PLUS))
          {
            if (!mitgliedeigenschaftenIds.contains(suchId))
            {
              ok = false;
              break;
            }
          }
          else // EigenschaftenNode2.MINUS
          {
            if (mitgliedeigenschaftenIds.contains(suchId))
            {
              ok = false;
              break;
            }
          }
        }
        else // Oder
        {
          if (suchauswahl.get(suchId).equals(EigenschaftenNode.PLUS))
          {
            if (mitgliedeigenschaftenIds.contains(suchId))
            {
              ok = true;
              break;
            }
          }
          else // EigenschaftenNode2.MINUS
          {
            if (!mitgliedeigenschaftenIds.contains(suchId))
            {
              ok = true;
              break;
            }
          }
        }
      }
      if (ok)
        mitgliederIdsFiltered.add(mitglied);
    }
    return mitgliederIdsFiltered;
  }
}
