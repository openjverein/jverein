/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class MittelverwendungList extends TablePart
{

  private TablePart saldoList;

  private Date datumvon = null;

  private Date datumbis = null;

  public MittelverwendungList(Action action, Date datumvon, Date datumbis)
  {
    super(action);
    this.datumvon = datumvon;
    this.datumbis = datumbis;
  }

  public Part getSaldoList() throws ApplicationException
  {
    ArrayList<MittelverwendungZeile> zeilen = null;
    try
    {
      zeilen = getInfo();

      if (saldoList == null)
      {
        saldoList = new TablePart(zeilen, null)
        {
          @Override
          protected void orderBy(int index)
          {
            return;
          }
        };
        saldoList.addColumn("Nr", "position");
        saldoList.addColumn("Mittel", "bezeichnung");
        saldoList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Summe", "summe",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_LEFT);
        saldoList.setRememberColWidths(true);
        saldoList.removeFeature(FeatureSummary.class);
      }
      else
      {
        saldoList.removeAll();
        for (MittelverwendungZeile sz : zeilen)
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

  public ArrayList<MittelverwendungZeile> getInfo() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql;
    ArrayList<MittelverwendungZeile> zeilen = new ArrayList<>();
    String bezeichnung = "";
    Integer pos = 1;

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

    // Schritt 1: Berechnung des Verwendungsrückstand(+)/-überhang(-)
    // am Ende des letzten GJ
    // Vorhandene Geldmittel zum Ende des letzten GJ sind zu verwenden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? ";
    Double vorhandeneMittel = (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.GELD.getKey() }, rsd);

    // Vorhandene zweckfremde Anlagen sind zu verwenden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND konto.zweck = ?";
    vorhandeneMittel += (Double) service.execute(sql, new Object[] { datumvon,
        Kontoart.ANLAGE.getKey(), Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
        rsd);

    // Nicht der zeitnahen Mittelverwendung unterliegende Mittel (Rücklagen)
    // zum Ende des letzten GJ können abgezogen werden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id" + " AND konto.kontoart >= ?"
        + " AND konto.kontoart <= ?";
    vorhandeneMittel -= (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
            Kontoart.RUECKLAGE_SONSTIG.getKey() },
        rsd);

    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) am Ende des letzten GJ";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        vorhandeneMittel, null);

    // Schritt 2: Mittel Zufluss
    // Summe aller Zuflüsse bei Geldkonten und Anlagen (=Sachspenden)
    sql = getSummenKontenSql();
    Double zufuehrung = (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.GELD.getKey(),
            Kontoart.ANLAGE.getKey(), ArtBuchungsart.EINNAHME },
        rsd);
    // Summe Zuflüsse durch Umbuchung
    // Auszahlung aus Verbindlichkeiten z.B. Darlehen,
    // Rückbuchung von zweckgebundenen Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0";
    zufuehrung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.VERBINDLICHKEITEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Insgesamt im GJ zugeflossene Mittel";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zufuehrung, null);
    bezeichnung = "          Zu verwendende Mittel im GJ und nächstem GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung, vorhandeneMittel);

    // Schritt 3: Mittel Abfluss
    // Summe aller Abflüsse bei Geldkonten
    sql = getSummenKontoSql();
    Double verwendung = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.GELD.getKey(), ArtBuchungsart.AUSGABE }, rsd);
    // Summe aller Abflüsse bei nicht nutzungsgebundenen Anlagen
    sql = getSummenKontoZweckSql();
    verwendung += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
            ArtBuchungsart.AUSGABE },
        rsd);
    // Summe der Abflüsse bei Umbuchung
    // Tilgung Verbindlichkeiten z.B. Darlehen,
    // Erwerb zweckgebundener Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0";
    verwendung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.VERBINDLICHKEITEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Im GJ verwendete Mittel";
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
        verwendung);

    // Rücklagen
    Double summeZuRuecklagen = 0.0;
    Double summeEntRuecklagen = 0.0;
    sql = getSummenRuecklagenSql();
    for (int i = Kontoart.RUECKLAGE_ZWECK_GEBUNDEN
        .getKey(); i <= Kontoart.RUECKLAGE_SONSTIG.getKey(); i++)
    {
      Double zuRuecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, i, ArtBuchungsart.EINNAHME }, rsd);
      summeZuRuecklagen += zuRuecklagen;
      if (Math.abs(zuRuecklagen) > 0.005
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Zuführung " + Kontoart.getByKey(i).getText();
        addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung,
            null, -zuRuecklagen);
      }
      Double entRuecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, i, ArtBuchungsart.AUSGABE }, rsd);
      summeEntRuecklagen += entRuecklagen;
      if (Math.abs(entRuecklagen) > 0.005
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Entnahme " + Kontoart.getByKey(i).getText();
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
            -entRuecklagen, null);
      }
    }

    bezeichnung = "          Verwendungsrückstand(+)/-überhang(-) zum Ende des GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung + vorhandeneMittel + verwendung - summeZuRuecklagen,
        -summeEntRuecklagen);

    // Leerzeile am Ende wegen Scrollbar
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, null,
        null, null, null));
    return zeilen;
  }

  public void setDatumvon(Date datumvon)
  {
    this.datumvon = datumvon;
  }

  public void setDatumbis(Date datumbis)
  {
    this.datumbis = datumbis;
  }

  @Override
  public void removeAll()
  {
    saldoList.removeAll();
  }

  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }

  private String getSummenKontoSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenKontenSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND (konto.kontoart = ? OR konto.kontoart = ?)"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenKontoZweckSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ? AND konto.zweck = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenUmbuchungSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?))"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenRuecklagenSql()
  {
    return "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
  }

  private void addZeile(ArrayList<MittelverwendungZeile> zeilen, int status,
      Integer position, String bezeichnung, Double einnahme, Double ausgabe)
      throws RemoteException
  {
    if (einnahme != null && einnahme == -0.0)
    {
      einnahme = 0.0;
    }
    if (ausgabe != null && ausgabe == -0.0)
    {
      ausgabe = 0.0;
    }
    switch (status)
    {
      case MittelverwendungZeile.EINNAHME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            einnahme, null));
        break;
      case MittelverwendungZeile.AUSGABE:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            ausgabe, null));
        break;
      case MittelverwendungZeile.SUMME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            null, einnahme + ausgabe));
        break;
    }
  }

}
