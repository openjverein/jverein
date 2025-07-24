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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.control.SollbuchungControl.DIFFERENZ;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.util.ApplicationException;

public class SollbuchungQuery
{

  protected boolean umwandeln = false;

  protected Mitglied mitglied = null;

  protected FilterControl control = null;

  public SollbuchungQuery(FilterControl control, boolean umwandeln,
      Mitglied mitglied)
  {
    this.umwandeln = umwandeln;
    this.control = control;
    this.mitglied = mitglied;
  }

  @SuppressWarnings("unchecked")
  public GenericIterator<Sollbuchung> get()
      throws RemoteException, ApplicationException
  {
    Date d1 = null;
    java.sql.Date vd = null;
    java.sql.Date bd = null;
    if (control.isDatumvonAktiv() && control.getDatumvon() != null)
    {
      d1 = (Date) control.getDatumvon().getValue();
      if (d1 != null)
      {
        vd = new java.sql.Date(d1.getTime());
      }
    }
    if (control.isDatumbisAktiv() && control.getDatumbis() != null)
    {
      d1 = (Date) control.getDatumbis().getValue();
      if (d1 != null)
      {
        bd = new java.sql.Date(d1.getTime());
      }
    }

    DIFFERENZ diff = DIFFERENZ.EGAL;
    if (control.isDifferenzAktiv() && control.getDifferenz() != null)
    {
      diff = (DIFFERENZ) control.getDifferenz().getValue();
    }

    boolean kein_name = (!control.isSuchnameAktiv()
        || control.getSuchname().getValue() == null
        || ((String) control.getSuchname().getValue()).isEmpty())
        && (!control.isSuchtextAktiv()
            || control.getSuchtext().getValue() == null
            || ((String) control.getSuchtext().getValue()).isEmpty());
    // Zahler ist gesetzt in Sollbuchungen Liste View oder bei Sollbuchungen
    // Zuweisung Dialog
    boolean ein_zahler_name = control.isSuchnameAktiv()
        && control.getSuchname().getValue() != null
        && !((String) control.getSuchname().getValue()).isEmpty();
    // Mitglied ist gesetzt in Sollbuchungen Liste View
    boolean ein_mitglied_name = control.isSuchtextAktiv()
        && control.getSuchtext().getValue() != null
        && !((String) control.getSuchtext().getValue()).isEmpty();
    boolean keine_email = !control.isMailauswahlAktiv() || (Integer) control
        .getMailauswahl().getValue() == MailAuswertungInput.ALLE;
    boolean filter_email = control.isMailauswahlAktiv() && !((Integer) control
        .getMailauswahl().getValue() == MailAuswertungInput.ALLE);

    // Falls kein Name, kein Mailfilter und keine Differenz dann alles lesen
    if (kein_name && keine_email && diff == DIFFERENZ.EGAL)
    {
      DBIterator<Sollbuchung> sollbIt = Einstellungen.getDBService()
          .createList(Sollbuchung.class);
      if (mitglied != null)
      {
        sollbIt.addFilter(Sollbuchung.T_MITGLIED + " = ?",
            new Object[] { Long.valueOf(mitglied.getID()) });
      }
      if (vd != null)
      {
        sollbIt.addFilter(Sollbuchung.T_DATUM + " >= ? ", new Object[] { vd });
      }
      if (bd != null)
      {
        sollbIt.addFilter(Sollbuchung.T_DATUM + " <= ? ", new Object[] { bd });
      }
      if (control.isOhneAbbucherAktiv()
          && (Boolean) control.getOhneAbbucher().getValue())
      {
        sollbIt.addFilter(Sollbuchung.T_ZAHLUNGSWEG + " <> ?",
            Zahlungsweg.BASISLASTSCHRIFT);
      }
      sollbIt.setOrder("ORDER BY " + Sollbuchung.T_DATUM + " desc");
      return sollbIt;
    }

    // Falls ein Name aber keine Differenz und kein Mailfilter dann alles des
    // Mitglieds lesen
    if (!kein_name && !filter_email && diff == DIFFERENZ.EGAL)
    {
      DBIterator<Sollbuchung> sollbuchungen = Einstellungen.getDBService()
          .createList(Sollbuchung.class);
      if (mitglied != null)
      {
        sollbuchungen.addFilter(Sollbuchung.T_MITGLIED + " = ?",
            new Object[] { Long.valueOf(mitglied.getID()) });
      }

      // Schauen welche joins gebraucht werden
      if (ein_mitglied_name)
      {
        sollbuchungen.join("mitglied dasMitglied");
        sollbuchungen.addFilter("dasMitglied.id = " + Sollbuchung.T_MITGLIED);
      }
      if ((ein_zahler_name && ein_mitglied_name)
          || (ein_zahler_name && !umwandeln))
      {
        sollbuchungen.join("mitglied derZahler");
        sollbuchungen.addFilter("derZahler.id = " + Sollbuchung.T_ZAHLER);
      }

      // Nach Namen filtern
      if (ein_mitglied_name && ein_zahler_name)
      {
        // Beide Namen kommen nur im Sollbuchungen Liste View vor
        // Es wird nicht umgewandelt
        String mitgliedName = ((String) control.getSuchtext().getValue())
            .toLowerCase() + "%";
        String zahlerName = ((String) control.getSuchname().getValue())
            .toLowerCase() + "%";
        sollbuchungen.addFilter(
            "((lower(dasMitglied.name) like ?)"
                + " OR (lower(dasMitglied.vorname) like ?)) AND "
                + "((lower(derZahler.name) like ?)"
                + " OR (lower(derZahler.vorname) like ?))",
            new Object[] { mitgliedName, mitgliedName, zahlerName,
                zahlerName });
      }
      else if (ein_mitglied_name)
      {
        // Mitglied Name kommt nur im Sollbuchungen Liste View vor
        // Es wird nicht umgewandelt
        String mitgliedName = ((String) control.getSuchtext().getValue())
            .toLowerCase() + "%";
        sollbuchungen.addFilter(
            "((lower(dasMitglied.name) like ?)"
                + " OR (lower(dasMitglied.vorname) like ?))",
            new Object[] { mitgliedName, mitgliedName });
      }
      else if (ein_zahler_name && !umwandeln)
      {
        // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
        String zahlerName = ((String) control.getSuchname().getValue())
            .toLowerCase() + "%";
        sollbuchungen.addFilter(
            "((lower(derZahler.name) like ?)"
                + " OR (lower(derZahler.vorname) like ?))",
            new Object[] { zahlerName, zahlerName });
      }
      else if (ein_zahler_name && umwandeln)
      {
        // Der Name muss umgewandelt werden, es kann mehrere Matches geben
        String zahlerName = ((String) control.getSuchname().getValue())
            .toLowerCase();
        ArrayList<Long> namenids = getNamenIds(zahlerName);
        if (namenids != null)
        {
          sollbuchungen.addFilter("(" + Sollbuchung.T_ZAHLER + " in ("
              + StringUtils.join(namenids, ",") + ") OR "
              + Sollbuchung.T_MITGLIED + " in ("
              + StringUtils.join(namenids, ",") + "))");
        }
      }

      if (vd != null)
      {
        sollbuchungen.addFilter(Sollbuchung.T_DATUM + " >= ? ",
            new Object[] { vd });
      }
      if (bd != null)
      {
        sollbuchungen.addFilter(Sollbuchung.T_DATUM + " <= ? ",
            new Object[] { bd });
      }
      if (control.isOhneAbbucherAktiv()
          && (Boolean) control.getOhneAbbucher().getValue())
      {
        sollbuchungen.addFilter(Sollbuchung.T_ZAHLUNGSWEG + " <> ?",
            Zahlungsweg.BASISLASTSCHRIFT);
      }
      sollbuchungen.setOrder("ORDER BY " + Sollbuchung.T_DATUM + " desc");
      return sollbuchungen;
    }

    // Eine Differenz ist ausgewählt
    final DBService service = Einstellungen.getDBService();

    // Suche nach dem Zahler, der LEFT JOIN beim Mitglied behält auch
    // Sollbuchungen bei denen kein Zahler gesetzt ist. Er könnte gelöscht
    // worden sein, aber die Sollbuchung existiert noch und evtl.
    // musss eine Buchung zugeordnet werden
    StringBuilder sql = new StringBuilder(
        "SELECT " + Sollbuchung.TABLE_NAME_ID + ", " + Sollbuchung.T_BETRAG
            + ", SUM(buchung.betrag) FROM " + Sollbuchung.TABLE_NAME);
    if (ein_mitglied_name)
    {
      sql.append(" JOIN mitglied dasMitglied ON (" + Sollbuchung.T_MITGLIED
          + " = dasMitglied.id)");
    }
    if ((ein_zahler_name && ein_mitglied_name)
        || (ein_zahler_name && !umwandeln) || filter_email)
    {
      sql.append(" LEFT JOIN mitglied derZahler ON (" + Sollbuchung.T_ZAHLER
          + " = derZahler.id)");
    }
    sql.append(" LEFT JOIN buchung ON " + Sollbuchung.TABLE_NAME_ID + " = "
        + Buchung.T_SOLLBUCHUNG);

    StringBuilder where = new StringBuilder();
    ArrayList<Object> param = new ArrayList<>();
    if (mitglied != null)
    {
      where.append(where.length() == 0 ? "" : " AND ")
          .append(Sollbuchung.T_MITGLIED + " = ? ");
      param.add(Long.valueOf(mitglied.getID()));
    }
    if (ein_mitglied_name && ein_zahler_name)
    {
      // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
      String mitgliedName = ((String) control.getSuchtext().getValue())
          .toLowerCase() + "%";
      String zahlerName = ((String) control.getSuchname().getValue())
          .toLowerCase() + "%";
      where.append(where.length() == 0 ? "" : " AND ").append(
          "((LOWER(dasMitglied.name) LIKE ?) OR (LOWER(dasMitglied.vorname) LIKE ?))");
      where.append(where.length() == 0 ? "" : " AND ").append(
          "((LOWER(derZahler.name) LIKE ?) OR (LOWER(derZahler.vorname) LIKE ?))");
      param.add(mitgliedName);
      param.add(mitgliedName);
      param.add(zahlerName);
      param.add(zahlerName);
    }
    else if (ein_mitglied_name)
    {
      // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
      String mitgliedName = ((String) control.getSuchtext().getValue())
          .toLowerCase() + "%";
      where.append(where.length() == 0 ? "" : " AND ").append(
          "((LOWER(dasMitglied.name) LIKE ?) OR (LOWER(dasMitglied.vorname) LIKE ?))");
      param.add(mitgliedName);
      param.add(mitgliedName);
    }
    else if (ein_zahler_name && !umwandeln)
    {
      // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
      String zahlerName = ((String) control.getSuchname().getValue())
          .toLowerCase() + "%";
      where.append(where.length() == 0 ? "" : " AND ").append(
          "((LOWER(derZahler.name) LIKE ?) OR (LOWER(derZahler.vorname) LIKE ?))");
      param.add(zahlerName);
      param.add(zahlerName);
    }
    else if (ein_zahler_name && umwandeln)
    {
      // Der Name muss umgewandelt werden, es kann mehrere Matches geben
      ArrayList<Long> namenids = getNamenIds(
          (String) control.getSuchname().getValue());
      if (namenids != null)
      {
        where.append(where.length() == 0 ? "" : " AND ")
            .append("(" + Sollbuchung.T_ZAHLER + " in ("
                + StringUtils.join(namenids, ",") + ") OR "
                + Sollbuchung.T_MITGLIED + " in ("
                + StringUtils.join(namenids, ",") + "))");
      }
    }
    if (vd != null)
    {
      where.append(where.length() == 0 ? "" : " AND ")
          .append(Sollbuchung.T_DATUM + " >= ?");
      param.add(vd);
    }
    if (bd != null)
    {
      where.append(where.length() == 0 ? "" : " AND ")
          .append(Sollbuchung.T_DATUM + " <= ?");
      param.add(bd);
    }
    if (control.isOhneAbbucherAktiv()
        && (Boolean) control.getOhneAbbucher().getValue())
    {
      where.append(where.length() == 0 ? "" : " AND ")
          .append(Sollbuchung.T_ZAHLUNGSWEG + " <> ?");
      param.add(Zahlungsweg.BASISLASTSCHRIFT);
    }
    if (filter_email)
    {
      int mailauswahl = (Integer) control.getMailauswahl().getValue();
      if (mailauswahl == MailAuswertungInput.OHNE)
      {
        where.append(where.length() == 0 ? "" : " AND ")
            .append("(derZahler.email IS NULL OR LENGTH(derZahler.email) = 0)");
      }
      if (mailauswahl == MailAuswertungInput.MIT)
      {
        where.append(where.length() == 0 ? "" : " AND ").append(
            "(derZahler.email IS NOT NULL AND LENGTH(derZahler.email) > 0)");
      }
    }

    if (where.length() > 0)
    {
      sql.append(" WHERE ").append(where);
    }
    sql.append(
        " GROUP BY " + Sollbuchung.TABLE_NAME_ID + ", " + Sollbuchung.T_BETRAG);

    Double limit = Double.valueOf(0d);
    if (control.isDoubleAuswAktiv()
        && control.getDoubleAusw().getValue() != null)
    {
      // Es ist egal ob der Betrag positiv oder negativ eingetragen wurde
      limit = Math.abs((Double) control.getDoubleAusw().getValue());
    }
    if (DIFFERENZ.FEHLBETRAG == diff)
    {
      sql.append(
          " HAVING CAST(COALESCE(SUM(buchung.betrag),0) AS DECIMAL(10,2)) < "
              + Sollbuchung.T_BETRAG + " - " + limit.toString());
    }
    if (DIFFERENZ.UEBERZAHLUNG == diff)
    {
      sql.append(
          " HAVING CAST(COALESCE(SUM(buchung.betrag),0) AS DECIMAL(10,2)) > "
              + Sollbuchung.T_BETRAG + " + " + limit.toString());
    }

    List<Long> ids = (List<Long>) service.execute(sql.toString(),
        param.toArray(), new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            List<Long> list = new ArrayList<>();
            while (rs.next())
            {
              list.add(rs.getLong(1));
            }
            return list;
          }
        });

    if (ids.size() == 0)
    {
      return null;
    }
    DBIterator<Sollbuchung> sollbIt = Einstellungen.getDBService()
        .createList(Sollbuchung.class);
    sollbIt.addFilter("id in (" + StringUtils.join(ids, ",") + ")");
    sollbIt.setOrder("ORDER BY " + Sollbuchung.T_DATUM + " desc");
    return sollbIt;
  }

  private ArrayList<Long> getNamenIds(final String suchname)
      throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql = "SELECT  mitglied.id, mitglied.name, mitglied.vorname from mitglied";

    @SuppressWarnings("unchecked")
    ArrayList<Long> mitgliedids = (ArrayList<Long>) service.execute(sql,
        new Object[] {}, new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            ArrayList<Long> ergebnis = new ArrayList<>();

            // In case the text search input is used, we calculate
            // an "equality" score for each Mitglied entry.
            // Only the entries with
            // score == maxScore will be shown.
            Integer maxScore = 0;
            int count = 0;
            String name = reduceWord(suchname);
            Long mgid = null;
            String nachname = null;
            String vorname = null;
            while (rs.next())
            {
              count++;
              // Nur die ids der Mitglieder speichern
              mgid = rs.getLong(1);

              StringTokenizer tok = new StringTokenizer(name, " ,-");
              Integer score = 0;
              nachname = reduceWord(rs.getString(2));
              vorname = reduceWord(rs.getString(3));
              while (tok.hasMoreElements())
              {
                String nextToken = tok.nextToken();
                if (nextToken.length() > 2)
                {
                  score += scoreWord(nextToken, nachname);
                  score += scoreWord(nextToken, vorname);
                }
              }

              if (maxScore < score)
              {
                maxScore = score;
                // We found a Sollbuchung matching with a higher equality
                // score, so we drop all previous matches, because they were
                // less equal.
                ergebnis.clear();
              }
              else if (maxScore > score)
              {
                // This match is worse, so skip it.
                continue;
              }
              ergebnis.add(mgid);
            }
            if (ergebnis.size() != count)
            {
              return ergebnis;
            }
            else
            {
              // Kein Match
              return null;
            }
          }
        });
    return mitgliedids;
  }

  public Integer scoreWord(String word, String in)
  {
    Integer wordScore = 0;
    StringTokenizer tok = new StringTokenizer(in, " ,-");

    while (tok.hasMoreElements())
    {
      String nextToken = tok.nextToken();

      // Full match is twice worth
      if (nextToken.equals(word))
      {
        wordScore += 2;
      }
      else if (nextToken.contains(word))
      {
        wordScore += 1;
      }
    }

    return wordScore;
  }

  public String reduceWord(String word)
  {
    // We replace "ue" -> "u" and "ü" -> "u", because some bank institutions
    // remove the dots "ü" -> "u". So we get "u" == "ü" == "ue".
    return word.toLowerCase().replaceAll("ä", "a").replaceAll("ae", "a")
        .replaceAll("ö", "o").replaceAll("oe", "o").replaceAll("ü", "u")
        .replaceAll("ue", "u").replaceAll("ß", "s").replaceAll("ss", "s");
  }

}
