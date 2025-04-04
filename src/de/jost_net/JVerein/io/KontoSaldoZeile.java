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

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;

/**
 * Hilfs-Objekt
 */
public class KontoSaldoZeile implements ISaldoZeile
{

  private Konto konto;

  private Double anfangsbestand = 0.0d;

  private Double einnahmen = 0.0d;

  private Double ausgaben = 0.0d;

  private Double umbuchungen = 0.0d;

  private Double endbestand = 0.0d;

  private String bemerkung = "";
  
  private Date von = null;
  
  private Date bis = null;

  private int status = UNDEFINED;

  public KontoSaldoZeile(int status, Konto konto, Double anfangsbestand,
      Double einnahmen,
      Double ausgaben, Double umbuchungen, Double endbestand)
  {
    this.status = status;
    this.konto = konto;
    this.anfangsbestand = anfangsbestand;
    this.einnahmen = einnahmen;
    this.ausgaben = ausgaben;
    this.umbuchungen = umbuchungen;
    this.endbestand = endbestand;
  }

  public KontoSaldoZeile(int status, Date von, Date bis, Konto konto)
      throws RemoteException
  {
    this.status = status;
    this.von = von;
    this.bis = bis;
    this.konto = konto;
    saldoZeile();
  }
  
  public KontoSaldoZeile(int status, Geschaeftsjahr gj, Konto konto)
      throws RemoteException
  {
    this.status = status;
    this.von = gj.getBeginnGeschaeftsjahr();
    this.bis = gj.getEndeGeschaeftsjahr();
    this.konto = konto;
    saldoZeile();
  }
  
  private void saldoZeile() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    Date vonRange = null;
    Date bisRange = null;
    Calendar cal = Calendar.getInstance();
    
    // Suchen ob Anfangsstand im Suchbereich enthalten ist
    DBIterator<Anfangsbestand> anf = service.createList(Anfangsbestand.class);
    anf.addFilter("konto = ? ", new Object[] { konto.getID() });
    anf.addFilter("datum >= ? AND datum <= ?", new Object[] { von, bis });
    anf.setOrder("ORDER BY day(datum)");
    ArrayList<Anfangsbestand> anf1 = null;
    if (anf != null)
    {
      @SuppressWarnings("unchecked")
      ArrayList<Anfangsbestand> anf01 = (ArrayList<Anfangsbestand>) PseudoIterator.asList(anf);
      anf1 = anf01;
    }
    
    // Anfangsstand ist für das von Datum vorhanden (Geschäftsjahresanfang)
    if (anf1 != null && !anf1.isEmpty() && anf1.get(0).getDatum().equals(von))
    {
      Anfangsbestand a = anf1.get(0);
      anfangsbestand = a.getBetrag();
      extract(service, von, bis);
      return;
    }
    
    // Suchen ob Anfangsstand vor dem Bereich
    anf = service.createList(Anfangsbestand.class);
    anf.addFilter("konto = ? ", new Object[] { konto.getID() });
    anf.addFilter("datum < ? ", new Object[] { von });
    anf.setOrder("ORDER BY day(datum)");
    ArrayList<Anfangsbestand> anf2 = null; 
    if (anf != null)
    {
      @SuppressWarnings("unchecked")
      ArrayList<Anfangsbestand> anf02 = (ArrayList<Anfangsbestand>) PseudoIterator.asList(anf);
      anf2 = anf02;
    }
       
    // Anfangsstand vor von Datum vorhanden
    if (anf2 != null && !anf2.isEmpty())
    {
      int size = anf2.size();
      // Endstand zum von Datum berechnen vom letzten Anfangsbestand
      vonRange = anf2.get(size-1).getDatum();
      cal.setTime(von);
      cal.add(Calendar.DAY_OF_MONTH, -1);
      bisRange = cal.getTime();
      anfangsbestand = anf2.get(size-1).getBetrag();
      extract(service, vonRange, bisRange);
      // Jetzt Anfangsstand zum von Datum setzen
      anfangsbestand = endbestand;
      bemerkung = "";
      extract(service, von, bis);
      return;
    }
    
    // Anfangsstand ist im Bereich vorhanden und es gibt keinen Anfangsstand vorher
    // Dann muß das Konto im Bereich erzeugt worden sein oder es gibt keinen 
    // früheren Anfangsstand. Dann zurückrechnen
    if (anf1 != null && !anf1.isEmpty() && (anf2 == null || anf2.isEmpty()))
    {
      // Delta in die Zukunft berechnen
      cal.setTime(anf1.get(0).getDatum());
      cal.add(Calendar.DAY_OF_MONTH, -1);
      bisRange = cal.getTime();
      extract(service, von, bisRange);
      Anfangsbestand a = anf1.get(0);
      // Endstand zum von Datum berechnen
      anfangsbestand = a.getBetrag() - endbestand;
      bemerkung = "";
      extract(service, von, bis);
      return;
    }

    anfangsbestand = 0.0d;
    bemerkung += "kein Anfangsbestand vorhanden  ";
  }

  @Override
  public int getStatus()
  {
    return status;
  }

  @Override
  public String getMessage()
  {
    return "Summen werden nur für Konto Zeilen berechnet!";
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (arg0.equals("konto"))
    {
      return konto;
    }
    else if (arg0.equals("kontonummer"))
    {
      return konto.getNummer();
    }
    else if (arg0.equals("kontobezeichnung"))
    {
      return konto.getBezeichnung();
    }
    else if (arg0.equals("anfangsbestand"))
    {
      return anfangsbestand;
    }
    else if (arg0.equals("einnahmen"))
    {
      return einnahmen;
    }
    else if (arg0.equals("ausgaben"))
    {
      return ausgaben;
    }
    else if (arg0.equals("umbuchungen"))
    {
      return umbuchungen;
    }
    else if (arg0.equals("endbestand"))
    {
      return endbestand;
    }
    else if (arg0.equals("bemerkung"))
    {
      return bemerkung;
    }
    throw new RemoteException("Ungültige Spaltenbezeichung: " + arg0);
  }

  @Override
  public String[] getAttributeNames()
  {
    return new String[] { "kontonummer", "kontobezeichnung", "anfangsbestand",
        "einnahmen", "ausgaben", "umbuchungen", "endbestand", "bemerkung" };
  }

  @Override
  public String getID() throws RemoteException
  {
    return konto.getNummer();
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "kontonummer";
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof KontoSaldoZeile))
    {
      return false;
    }
    return this.getID().equals(arg0.getID());
  }
  
  private void extract(DBService service, Date von, Date bis) throws RemoteException
  {
    String sql = "select sum(betrag) from buchung, buchungsart "
        + "where datum >= ? and datum <= ? AND konto = ? "
        + "and buchung.buchungsart = buchungsart.id " + "and buchungsart.art=?";

    ResultSetExtractor rs = new ResultSetExtractor()
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
    einnahmen = (Double) service.execute(sql,
        new Object[] { von, bis, konto.getID(), 0 }, rs);
    ausgaben = (Double) service.execute(sql,
        new Object[] { von, bis , konto.getID(), 1 },  rs);
    umbuchungen = (Double) service.execute(sql,
        new Object[] { von, bis, konto.getID(), 2 }, rs);

    String sqlOhneBuchungsart = "select sum(betrag) from buchung "
        + "where datum >= ? and datum <= ? AND konto = ? "
        + "and buchung.buchungsart is null";

    Double ohneBuchungsart = (Double) service.execute(sqlOhneBuchungsart,
        new Object[] { von, bis, konto.getID() }, rs);

    if (ohneBuchungsart >= 0.01d || ohneBuchungsart <= -0.01d)
    {
      bemerkung += "Summe Buchungen ohne Buchungsart: "
          + new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
              .format(ohneBuchungsart)
          + "  ";
    }

    endbestand = anfangsbestand + einnahmen + ausgaben + umbuchungen
        + ohneBuchungsart;
  }
  
}

