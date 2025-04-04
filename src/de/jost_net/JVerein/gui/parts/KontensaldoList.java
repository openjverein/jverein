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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.ISaldoZeile;
import de.jost_net.JVerein.io.KontoSaldoZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class KontensaldoList extends TablePart implements Part
{

  private SaldoListTablePart saldoList;
  
  private Date von = null;
  
  private Date bis = null;

  public KontensaldoList(Action action, Geschaeftsjahr gj)
  {
    super(action);
    this.von = gj.getBeginnGeschaeftsjahr();
    this.bis = gj.getEndeGeschaeftsjahr();
  }
  
  public KontensaldoList(Action action, Date von, Date bis)
  {
    super(action);
    this.von = von;
    this.bis = bis;
  }

  public Part getSaldoList() throws ApplicationException
  {
    ArrayList<KontoSaldoZeile> zeile = null;
    try
    {
      zeile = getInfo(Einstellungen.getEinstellung().getSummenAnlagenkonto());

      if (saldoList == null)
      {
        saldoList = new SaldoListTablePart(zeile, null)
        {
          @Override
          protected void orderBy(int index)
          {
            return;
          }
        };
        saldoList.addColumn("Kontonummer", "kontonummer", null, false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Bezeichnung", "kontobezeichnung");
        saldoList.addColumn("Anfangsbestand", "anfangsbestand",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Einnahmen", "einnahmen",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Ausgaben", "ausgaben",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Umbuchungen", "umbuchungen",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Endbestand", "endbestand",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Bemerkung", "bemerkung");
        saldoList.setRememberColWidths(true);
        saldoList.setMulti(true);
        saldoList.addFeature(new FeatureSummary());
      }
      else
      {
        saldoList.removeAll();
        for (KontoSaldoZeile sz : zeile)
        {
          saldoList.addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten" + e.getMessage());
    }
    return saldoList;
  }

  public ArrayList<KontoSaldoZeile> getInfo(boolean summensaldo) throws RemoteException
  {
    ArrayList<KontoSaldoZeile> zeile = new ArrayList<>();
    Konto k = (Konto) Einstellungen.getDBService().createObject(Konto.class,
        null);
    DBIterator<Konto> konten = k.getKontenVonBis(von, bis);
    konten.addFilter("kontoart < ?", Kontoart.LIMIT.getKey());
    double anfangsbestand = 0;
    double einnahmen = 0;
    double ausgaben = 0;
    double umbuchungen = 0;
    double endbestand = 0;
    double jahressaldo = 0;
    double sanfangsbestand = 0;
    double seinnahmen = 0;
    double sausgaben = 0;
    double sumbuchungen = 0;
    double sendbestand = 0;
    Konto konto = null;
    
    if (von != null)
    {
      KontoSaldoZeile sz = null;
      while (konten.hasNext())
      {
        konto = konten.next();
        sz = new KontoSaldoZeile(ISaldoZeile.DETAIL, von, bis, konto);
        if (summensaldo && konto.getKontoArt() == Kontoart.ANLAGE)
        {
          sanfangsbestand += (Double) sz.getAttribute("anfangsbestand");
          seinnahmen += (Double) sz.getAttribute("einnahmen");
          sausgaben += (Double) sz.getAttribute("ausgaben");
          sumbuchungen += (Double) sz.getAttribute("umbuchungen");
          sendbestand += (Double) sz.getAttribute("endbestand");
          jahressaldo += (Double) sz.getAttribute("endbestand")
              - (Double) sz.getAttribute("anfangsbestand");
        }
        else
        {
          anfangsbestand += (Double) sz.getAttribute("anfangsbestand");
          einnahmen += (Double) sz.getAttribute("einnahmen");
          ausgaben += (Double) sz.getAttribute("ausgaben");
          umbuchungen += (Double) sz.getAttribute("umbuchungen");
          endbestand += (Double) sz.getAttribute("endbestand");
          jahressaldo += (Double) sz.getAttribute("endbestand")
              - (Double) sz.getAttribute("anfangsbestand");
          zeile.add(sz);
        }
      }
    }
    if (summensaldo)
    {
      k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
      k.setNummer("");
      k.setBezeichnung("Summe Anlagenkonten");
      zeile.add(new KontoSaldoZeile(ISaldoZeile.DETAIL, k, sanfangsbestand,
          seinnahmen, sausgaben,
          sumbuchungen, sendbestand));
    }
    k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
    k.setNummer("");
    k.setBezeichnung("Summe aller Konten");
    zeile.add(new KontoSaldoZeile(ISaldoZeile.GESAMTSALDOFOOTER, k,
        anfangsbestand + sanfangsbestand, einnahmen + seinnahmen,
        ausgaben + sausgaben, umbuchungen + sumbuchungen, endbestand + sendbestand));
    k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
    k.setNummer("");
    k.setBezeichnung("Überschuss/Verlust(-)");
    zeile.add(new KontoSaldoZeile(ISaldoZeile.GESAMTGEWINNVERLUST, k, null,
        null, null, null, jahressaldo));
    
    // Konten ohne Berücksichtigung im Saldo
    k = (Konto) Einstellungen.getDBService().createObject(Konto.class,
        null);
    konten = k.getKontenVonBis(von, bis);
    konten.addFilter("kontoart > ?", Kontoart.LIMIT.getKey());
    if (von != null && konten.hasNext())
    {
      KontoSaldoZeile sz = null;
      // Leerzeile als Trenner
      k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
      k.setNummer("");
      k.setBezeichnung("");
      zeile.add(new KontoSaldoZeile(ISaldoZeile.UNDEFINED, k, null, null, null,
          null, null));
      // Überschrift
      k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
      k.setNummer("");
      k.setBezeichnung("Konten ohne Berücksichtigung im Saldo:");
      zeile.add(new KontoSaldoZeile(ISaldoZeile.UNDEFINED, k, null, null, null,
          null, null));
      // Jetzt die Konten
      while (konten.hasNext())
      {
        konto = konten.next();
        sz = new KontoSaldoZeile(ISaldoZeile.DETAIL, von, bis, konto);
        zeile.add(sz);
      }
    }

    // Leerzeile am Ende wegen Scrollbar
    k = (Konto) Einstellungen.getDBService().createObject(Konto.class, null);
    k.setNummer("");
    k.setBezeichnung("");
    zeile.add(new KontoSaldoZeile(ISaldoZeile.UNDEFINED, k, null, null, null,
        null, null));
    return zeile;
  }

  public void setGeschaeftsjahr(Geschaeftsjahr gj)
  {
    this.von = gj.getBeginnGeschaeftsjahr();
    this.bis = gj.getEndeGeschaeftsjahr();
  }
  
  public void setVonBis(Date von, Date bis)
  {
    this.von = von;
    this.bis = bis;
  }

  @Override
  public void removeAll()
  {
    saldoList.removeAll();
  }

  public void addItem(KontoSaldoZeile sz) throws RemoteException
  {
    saldoList.addItem(sz);
  }

  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }

}
