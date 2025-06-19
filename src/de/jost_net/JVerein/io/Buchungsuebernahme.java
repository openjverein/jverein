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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.BuchungUebernahmeProtokollDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Misc;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

public class Buchungsuebernahme
{
  private ArrayList<Buchung> buchungen;

  private Buchung fehlerbuchung = null;

  private Exception exception = null;

  private List<Buchungsart> buchungsartList;

  private final static transient Map<String, Pattern> patternCache = new HashMap<String, Pattern>();

  public Buchungsuebernahme()
  {
    uebernahme();
  }

  @SuppressWarnings("unchecked")
  private void uebernahme()
  {
    try
    {
      Logger.info("Buchungsübernahme zu JVerein gestartet");

      // BuchungsartList für automatische Buchungszuordnung bestimmen.
      DBIterator<Buchungsart> buchungsartIt = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      buchungsartIt.addFilter("suchbegriff != '' and suchbegriff is not null");
      buchungsartList = PseudoIterator.asList(buchungsartIt);

      // Protokollliste initialisieren
      buchungen = new ArrayList<>();
      // Über alle Hibiscus-Konten (aus JVerein-Sicht) iterieren
      DBIterator<Konto> hibkto = Einstellungen.getDBService()
          .createList(Konto.class);
      hibkto.addFilter("hibiscusid > 0");
      while (hibkto.hasNext())
      {
        Konto kto = (Konto) hibkto.next();
        leseHibiscus(kto);
      }
      Logger.info("Buchungsübernahme zu JVerein abgeschlossen");
    }
    catch (Exception e)
    {
      Logger.error("Buchungsübernahme zu JVerein fehlerhaft", e);
    }
    try
    {
      BuchungUebernahmeProtokollDialog bup = new BuchungUebernahmeProtokollDialog(
          buchungen, fehlerbuchung, exception);
      bup.open();
    }
    catch (OperationCanceledException oce)
    {
      // Keine Ausgabe einer Fehlermeldung
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }

  }

  private void leseHibiscus(Konto kto) throws Exception
  {
    Integer hibid = kto.getHibiscusId();
    Integer jvid = Integer.valueOf(kto.getID());
    DBService service = Einstellungen.getDBService();
    String sql = "select max(umsatzid) from buchung where konto = "
        + jvid.toString();

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        if (!rs.next())
        {
          return Integer.valueOf(0);
        }
        return Integer.valueOf(rs.getInt(1));
      }
    };
    Integer maximum = (Integer) service.execute(sql, new Object[] {}, rs);

    DBIterator<Jahresabschluss> itjahresabschl = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    itjahresabschl.setOrder("order by bis desc");
    Jahresabschluss ja = null;
    if (itjahresabschl.hasNext())
    {
      ja = (Jahresabschluss) itjahresabschl.next();
    }

    DBService hibservice = (DBService) Application.getServiceFactory()
        .lookup(HBCI.class, "database");
    DBIterator<Umsatz> hibbuchungen = hibservice.createList(Umsatz.class);
    if (maximum.intValue() > 0)
    {
      hibbuchungen.addFilter("id > ?", maximum);
    }
    hibbuchungen.addFilter("konto_id = ?", hibid);
    if (ja != null)
    {
      Logger.info("datum=" + ja.getBis());
      hibbuchungen.addFilter("datum > ?", ja.getBis());
    }
    hibbuchungen.setOrder("ORDER BY id");
    while (hibbuchungen.hasNext())
    {
      Umsatz u = (Umsatz) hibbuchungen.next();
      importiereUmsatz(u, kto);
    }
  }

  private void importiereUmsatz(Umsatz u, Konto kto) throws Exception
  {
    if ((u.getFlags() & Umsatz.FLAG_NOTBOOKED) == 0)
    {
      Buchung b = null;
      try
      {
        b = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
            null);
        b.setUmsatzid(Integer.valueOf(u.getID()));
        b.setKonto(kto);
        b.setName(u.getGegenkontoName());
        b.setIban(u.getGegenkontoNummer());
        b.setBetrag(u.getBetrag());
        b.setZweck(u.getZweck());
        String[] moreLines = u.getWeitereVerwendungszwecke();
        String zweck = u.getZweck();
        String line2 = u.getZweck2();
        if (line2 != null && line2.trim().length() > 0)
        {
          zweck += "\r\n" + line2.trim();
        }
        if (moreLines != null && moreLines.length > 0)
        {
          for (String s : moreLines)
          {
            if (s == null || s.trim().length() == 0)
              continue;
            zweck += "\r\n" + s.trim();
          }
        }
        // Beautify zweck
        if (Einstellungen.getEinstellung().getAutomatischeBuchungskorrekturHibiscus())
        {
          zweck = Misc.getBuchungsZweckKorrektur(zweck, true);
        }
        if (zweck != null && zweck.length() > 500)
        {
          zweck = zweck.substring(0, 500);
        }
        b.setZweck(zweck);
        
        // Buchungsart automatisch zuordnen
        String suchZweck = u.getGegenkontoNummer() + " " + u.getGegenkontoName()
            + " " + zweck;
        for (Buchungsart ba : buchungsartList)
        {
          if (match(ba.getSuchbegriff(), suchZweck, ba.getRegexp()))
          {
            b.setBuchungsartId(Long.parseLong(ba.getID()));
            if (Einstellungen.getEinstellung().getSteuerInBuchung())
            {
              b.setSteuer(ba.getSteuer());
            }
          }
          break;
        }
        
        b.setDatum(u.getDatum());
        b.setArt(u.getArt());
        b.setKommentar(u.getKommentar());
        b.store();
        buchungen.add(b);
      }
      catch (Exception e)
      {
        this.fehlerbuchung = b;
        this.exception = e;
        throw e;
      }
    }
  }

  /**
   * 
   * @param suchtext
   *          der Text oder eine Kommegetrennte Liste nach der gesucht werden
   *          soll
   * @param zweck
   *          der Verwendungszweck etc. der Buchung
   * @param isRegexp
   *          ist der Suchtext ein regulärer Ausdruck?
   * @return true wenn der zweck zum suchtext passt
   */
  private boolean match(String suchtext, String zweck, boolean isRegexp)
  {
    if (!isRegexp)
    {
      final List<String> result = new ArrayList<String>();
      for (String s : suchtext.toLowerCase().split("(?<!\\\\),"))
      {
        s = StringUtils.trimToNull(s);
        if (s == null)
          continue;

        // Escaping-Zeichen entfernen, falls vorhanden
        s = s.replace("\\", "");
        result.add(s);
      }

      zweck = zweck.toLowerCase();
      for (String test : result)
      {
        if (test.isEmpty())
        {
          continue;
        }
        if (zweck.indexOf(test) != -1)
        {
          return true;
        }
      }
    }
    else
    {
      Pattern pattern = patternCache.get(suchtext);
      try
      {
        if (pattern == null)
        {
          pattern = Pattern.compile(suchtext, Pattern.CASE_INSENSITIVE);
          patternCache.put(suchtext, pattern);
        }

        return pattern.matcher(zweck).matches();
      }
      catch (Exception e)
      {
        Logger.error("invalid regex pattern: " + e.getMessage(), e);
        return false;
      }
    }
    return false;
  }

}
