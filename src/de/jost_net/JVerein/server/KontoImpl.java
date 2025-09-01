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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KontoImpl extends AbstractJVereinDBObject implements Konto
{

  private static final long serialVersionUID = 1L;

  public KontoImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "konto";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "id";
  }

  @Override
  protected void deleteCheck()
  {
    //
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      plausi();
      DBIterator<Konto> it = Einstellungen.getDBService()
          .createList(Konto.class);
      it.addFilter("nummer = ?", new Object[] { getNummer() });
      if (it.size() > 0)
      {
        throw new ApplicationException("Konto existiert bereits");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("insert check of konto failed", e);
      throw new ApplicationException(
          "Konto kann nicht gespeichert werden. Siehe system log");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    plausi();
  }

  private void plausi() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().length() == 0)
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben");
      }
      if (getBezeichnung().length() > 255)
      {
        throw new ApplicationException(
            "Maximale Länge der Bezeichnung: 255 Zeichen");
      }
      if (getNummer() == null || getNummer().length() == 0)
      {
        throw new ApplicationException("Bitte Nummer eingeben");
      }
      if (getKontoArt() == Kontoart.ANLAGE)
      {
        if (getBetrag() != null && getBetrag() < 0.0)
        {
          throw new ApplicationException(
              "Wert der Anlage darf nicht negativ sein");
        }
        if (getAnlagenart() == null)
        {
          throw new ApplicationException("Bitte Anlagen Buchungsart eingeben");
        }
        if (getAfaart() == null)
        {
          throw new ApplicationException("Bitte AfA Buchungsart eingeben");
        }
        if (getBuchungsklasse() == null)
        {
          throw new ApplicationException(
              "Bitte Anlagen Buchungsklasse eingeben");
        }
        if (getAnlagenzweck() == null)
        {
          throw new ApplicationException("Bitte Anlagen Zweck auswählen");
        }
        if (getAfaMode() == null)
        {
          throw new ApplicationException("Bitte Afa Mode eingeben");
        }
        if (getAfaMode() != null && getAfaMode() == AfaMode.ANGEPASST)
        {
          if (getAfaStart() == null)
          {
            throw new ApplicationException("Bitte Afa Erstes Jahr eingeben");
          }
          if (getAfaDauer() == null)
          {
            throw new ApplicationException("Bitte Afa Folgejahre eingeben");
          }
        }
      }
    }
    catch (RemoteException e)
    {
      Logger.error("insert check of konto failed", e);
      throw new ApplicationException(
          "Konto kann nicht gespeichert werden. Siehe system log");
    }
  }

  /**
   * 
   * @param konto
   *          id des Kontos
   * @param datum
   * @return dibt den Kontostand am anfang des angegebenen Tages zurück, also
   *         ohne die Buchungen des angegebenen Tages
   * @throws RemoteException
   */
  public static Double getSaldo(Integer konto, Date datum)
      throws RemoteException
  {
    DBService service = Einstellungen.getDBService();

    // Suchen ob Anfangsstand vor dem Bereich.
    DBIterator<Anfangsbestand> it = service.createList(Anfangsbestand.class);
    it.addFilter("konto = ? ", konto);
    it.addFilter("datum <= ? ", datum);
    it.setOrder("ORDER BY datum DESC");
    it.setLimit(1);

    // Anfangsstand vor/an von Datum vorhanden.
    if (it.hasNext())
    {
      Anfangsbestand a = it.next();

      // Anfangsbestand = vorhandener Anfangsbestand + Umsätze bis "bis"
      ExtendedDBIterator<PseudoDBObject> summeIt = new ExtendedDBIterator<>(
          "buchung");
      summeIt.addColumn("sum(betrag) as summe");
      summeIt.addFilter("datum >= ?", a.getDatum());
      summeIt.addFilter("datum < ?", datum);
      summeIt.addFilter("konto = ?", konto);

      Double summe = 0d;
      if (summeIt.hasNext())
      {
        PseudoDBObject o = summeIt.next();
        if (o.getAttribute("summe") != null)
        {
          summe = o.getDouble("summe");
        }
      }

      return a.getBetrag() + summe;
    }

    // Suchen ob Anfangsstand im Suchbereich enthalten ist.
    it = service.createList(Anfangsbestand.class);
    it.addFilter("konto = ? ", konto);
    it.addFilter("datum >= ?", datum);
    it.setOrder("ORDER BY datum");
    it.setLimit(1);

    // Anfangsstand ist vorhanden und es gibt keinen Anfangsstand vorher
    // Dann muß das Konto im Bereich erzeugt worden sein oder es gibt keinen
    // früheren Anfangsstand. Dann zurückrechnen
    if (it.hasNext())
    {
      Anfangsbestand a = it.next();

      // Anfangsbestand = vorhandener Anfangsbestand - Umsätze davor.
      ExtendedDBIterator<PseudoDBObject> summeIt = new ExtendedDBIterator<>(
          "buchung");
      summeIt.addColumn("sum(betrag) as summe");
      summeIt.addFilter("datum >= ?", datum);
      summeIt.addFilter("datum <= ?", DateUtils.addDays(a.getDatum(), -1));
      summeIt.addFilter("konto = ?", konto);

      Double summe = 0d;
      PseudoDBObject o = summeIt.next();
      if (o.getAttribute("summe") != null)
      {
        summe = o.getDouble("summe");
      }
      return a.getBetrag() - summe;
    }
    return null;
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    return null;
  }

  @Override
  public String getNummer() throws RemoteException
  {
    return (String) getAttribute("nummer");
  }

  @Override
  public void setNummer(String nummer) throws RemoteException
  {
    setAttribute("nummer", nummer);
  }

  @Override
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  @Override
  public Date getEroeffnung() throws RemoteException
  {
    return (Date) getAttribute("eroeffnung");
  }

  @Override
  public void setEroeffnung(Date eroeffnungdatum) throws RemoteException
  {
    setAttribute("eroeffnung", eroeffnungdatum);
  }

  @Override
  public Date getAufloesung() throws RemoteException
  {
    return (Date) getAttribute("aufloesung");
  }

  @Override
  public void setAufloesung(Date aufloesungsdatum) throws RemoteException
  {
    setAttribute("aufloesung", aufloesungsdatum);
  }

  @Override
  public Integer getHibiscusId() throws RemoteException
  {
    return (Integer) getAttribute("hibiscusid");
  }

  @Override
  public void setHibiscusId(Integer id) throws RemoteException
  {
    setAttribute("hibiscusid", id);
  }

  @Override
  public DBIterator<Konto> getKontenEinesJahres(Geschaeftsjahr gj)
      throws RemoteException
  {
    DBIterator<Konto> konten = Einstellungen.getDBService()
        .createList(Konto.class);
    konten.addFilter("(eroeffnung is null or eroeffnung <= ?)",
        new Object[] { gj.getEndeGeschaeftsjahr() });
    konten.addFilter("(aufloesung is null or aufloesung >= ? )",
        new Object[] { gj.getBeginnGeschaeftsjahr() });
    konten.setOrder("order by bezeichnung");
    return konten;
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    super.delete();
    Cache.get(Konto.class, false).remove(this); // Aus Cache loeschen
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    Cache.get(Konto.class, false).put(this); // Cache aktualisieren
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsart");
  }

  @Override
  public void setBuchungsartId(Long buchungsartId) throws RemoteException
  {
    setAttribute("buchungsart", buchungsartId);
  }

  @Override
  public Kontoart getKontoArt() throws RemoteException
  {
    Integer tmp = (Integer) super.getAttribute("kontoart");
    if (tmp == null)
    {
      return Kontoart.GELD;
    }
    else
    {
      return Kontoart.getByKey((int) super.getAttribute("kontoart"));
    }
  }

  @Override
  public void setKontoArt(Kontoart kontoart) throws RemoteException
  {
    setAttribute("kontoart", kontoart.getKey());
  }

  @Override
  public Buchungsart getAnlagenart() throws RemoteException
  {
    Long l = (Long) super.getAttribute("anlagenart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public Long getAnlagenartId() throws RemoteException
  {
    return (Long) super.getAttribute("anlagenart");
  }

  @Override
  public void setAnlagenartId(Long anlagenartId) throws RemoteException
  {
    setAttribute("anlagenart", anlagenartId);
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object l = (Object) super.getAttribute("anlagenklasse");
    if (l == null)
    {
      return null; // Keine Buchungsklasse zugeordnet
    }

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public Long getBuchungsklasseId() throws RemoteException
  {
    return (Long) super.getAttribute("anlagenklasse");
  }

  @Override
  public void setBuchungsklasseId(Long anlagenklasseId) throws RemoteException
  {
    setAttribute("anlagenklasse", anlagenklasseId);
  }

  @Override
  public Buchungsart getAfaart() throws RemoteException
  {
    Long l = (Long) super.getAttribute("afaart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public Long getAfaartId() throws RemoteException
  {
    return (Long) super.getAttribute("afaart");
  }

  @Override
  public void setAfaartId(Long afaartId) throws RemoteException
  {
    setAttribute("afaart", afaartId);
  }

  @Override
  public Integer getNutzungsdauer() throws RemoteException
  {
    return (Integer) getAttribute("nutzungsdauer");
  }

  @Override
  public void setNutzungsdauer(Integer nutzungsdauer) throws RemoteException
  {
    setAttribute("nutzungsdauer", nutzungsdauer);
  }

  @Override
  public Double getBetrag() throws RemoteException
  {
    return (Double) getAttribute("betrag");
  }

  @Override
  public void setBetrag(Double d) throws RemoteException
  {
    setAttribute("betrag", d);
  }

  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar", kommentar);
  }

  @Override
  public Date getAnschaffung() throws RemoteException
  {
    return (Date) getAttribute("anschaffung");
  }

  @Override
  public void setAnschaffung(Date anschaffung) throws RemoteException
  {
    setAttribute("anschaffung", anschaffung);
  }

  @Override
  public Double getAfaStart() throws RemoteException
  {
    return (Double) getAttribute("afastart");
  }

  @Override
  public void setAfaStart(Double afastart) throws RemoteException
  {
    setAttribute("afastart", afastart);
  }

  @Override
  public Double getAfaDauer() throws RemoteException
  {
    return (Double) getAttribute("afadauer");
  }

  @Override
  public void setAfaDauer(Double afadauer) throws RemoteException
  {
    setAttribute("afadauer", afadauer);
  }

  @Override
  public Double getAfaRestwert() throws RemoteException
  {
    return (Double) getAttribute("afarestwert");
  }

  @Override
  public void setAfaRestwert(Double afarestwert) throws RemoteException
  {
    setAttribute("afarestwert", afarestwert);
  }

  @Override
  public Integer getAfaMode() throws RemoteException
  {
    return (Integer) getAttribute("afamode");
  }

  @Override
  public void setAfaMode(Integer afamode) throws RemoteException
  {
    setAttribute("afamode", afamode);
  }

  @Override
  public Double getSaldo() throws RemoteException
  {
    ResultSetExtractor rsd = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        if (!rs.next())
        {
          return Double.valueOf(0);
        }
        return Double.valueOf(rs.getDouble(1));
      }
    };
    Double saldo = 0.0;
    Date datum = null;
    // Suchen ob Anfangsstand im Suchbereich enthalten ist
    DBService service = Einstellungen.getDBService();
    DBIterator<Anfangsbestand> anf = service.createList(Anfangsbestand.class);
    anf.addFilter("konto = ? ", new Object[] { getID() });
    anf.setOrder("ORDER BY datum desc");
    if (anf != null && anf.hasNext())
    {
      Anfangsbestand anfang = anf.next();
      saldo = anfang.getBetrag();
      datum = anfang.getDatum();
    }
    if (datum != null)
    {
      String sql = "SELECT sum(buchung.betrag) FROM buchung"
          + " WHERE buchung.konto = ?" + " AND buchung.datum >= ?";
      saldo += (Double) service.execute(sql, new Object[] { getID(), datum },
          rsd);
    }
    else
    {
      String sql = "SELECT sum(buchung.betrag) FROM buchung"
          + " WHERE buchung.konto = ?";
      saldo += (Double) service.execute(sql, new Object[] { getID() }, rsd);
    }
    return saldo;
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("buchungsart".equals(fieldName))
      return getBuchungsart();
    return super.getAttribute(fieldName);
  }

  @Override
  public Anlagenzweck getAnlagenzweck() throws RemoteException
  {
    Integer tmp = (Integer) super.getAttribute("zweck");
    if (tmp == null)
    {
      return Anlagenzweck.NUTZUNGSGEBUNDEN;
    }
    else
    {
      return Anlagenzweck.getByKey((int) super.getAttribute("zweck"));
    }
  }

  @Override
  public void setAnlagenzweck(Anlagenzweck zweck) throws RemoteException
  {
    setAttribute("zweck", zweck.getKey());
  }

  @Override
  public String getObjektName()
  {
    return "Konto";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Konten";
  }
}
