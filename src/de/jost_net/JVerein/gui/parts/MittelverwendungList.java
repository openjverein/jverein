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
import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.MittelverwendungZeile;
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

    ResultSetExtractor rsbk = new ResultSetExtractor()
    {
      @Override
      public HashMap<Integer, String> extract(ResultSet rs) throws SQLException
      {
        HashMap<Integer, String> map = new HashMap<>();
        while (rs.next())
        {
          map.put(Integer.valueOf(rs.getInt(1)), rs.getString(3));
        }
        return map;
      }
    };

    // Ids der Buchunsklassen
    sql = "SELECT buchungsklasse.* FROM buchungsklasse" + " ORDER BY nummer";
    @SuppressWarnings("unchecked")
    HashMap<Integer, String> bkMap = (HashMap<Integer, String>) service
        .execute(sql, new Object[] {}, rsbk);

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

    bezeichnung = "Vorhandene Mittel zum Ende des letzten GJ";
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? ";
    Double pos1 = (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.GELD.getKey() }, rsd);
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung, pos1,
        null);

    bezeichnung = "Nicht der zeitnahen Mittelverwendung unterliegende Mittel zum Ende des letzten GJ";
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id "
        + " AND (konto.kontoart = ? OR konto.kontoart = ? OR konto.kontoart = ?)";
    Double pos2 = (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.RUECKLAGE.getKey(),
            Kontoart.VERMOEGEN.getKey(),
            Kontoart.SONSTIGE_RUECKLAGEN.getKey() },
        rsd);
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
        pos2);

    bezeichnung = "          Verwendungsüberhang/Rückstand Ende des letzten GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, pos1,
        -pos2);

    Double zufuehrung = 0d;
    Double verwendung = 0d;
    // Mittel Zufluss und Abfluss für alle Buchungsklassen
    for (Integer bkId : bkMap.keySet())
    {
      // Mittel Zufluss
      // Summe der Buchungen bei Einnahmen
      sql = getSummenBuchungSql();
      Double zuf = (Double) service.execute(sql, new Object[] { datumvon,
          datumbis, Kontoart.GELD.getKey(), bkId, ArtBuchungsart.EINNAHME },
          rsd);
      // Summe der positiven Buchungen bei Umbuchung
      sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0";
      Double um = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis,
              Kontoart.VERBINDLICHKEITEN.getKey(), Kontoart.ANLAGE.getKey(),
              bkId, ArtBuchungsart.UMBUCHUNG },
          rsd);
      zuf -= um;
      zufuehrung += zuf;

      // Mittel Abfluss
      // Summe der Buchungen bei Ausgaben
      sql = getSummenBuchungSql();
      Double verw = (Double) service.execute(sql, new Object[] { datumvon,
          datumbis, Kontoart.GELD.getKey(), bkId, ArtBuchungsart.AUSGABE },
          rsd);
      // Summe der negativen Buchungen bei Umbuchung
      sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0";
      Double um2 = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis,
              Kontoart.VERBINDLICHKEITEN.getKey(), Kontoart.ANLAGE.getKey(),
              bkId, ArtBuchungsart.UMBUCHUNG },
          rsd);
      verw -= um2;
      verwendung += verw;

      if (zuf != 0d || verw != 0d
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Mittel Zufluss aus " + bkMap.get(bkId);
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
            zuf, null);
        bezeichnung = "Verwendete Mittel aus " + bkMap.get(bkId);
        addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung,
            null, verw);
        bezeichnung = "          Überschuss/Verlust aus " + bkMap.get(bkId);
        addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, zuf,
            verw);
      }
    }

    // Summen über alle Sphären
    bezeichnung = "Mittel Zufluss aus allen Sphären";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zufuehrung, 0d);
    bezeichnung = "Verwendete Mittel aus allen Sphären";
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, 0d,
        verwendung);
    bezeichnung = "          Überschuss/Verlust aus allen Sphären";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung, verwendung);

    // Rücklagen nach § 62 Abs. 1 AO
    sql = getSummenRuecklagenSql();
    Double zuRuecklagen = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.RUECKLAGE.getKey(), ArtBuchungsart.EINNAHME }, rsd);

    sql = getSummenRuecklagenSql();
    Double entRuecklagen = (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.RUECKLAGE.getKey(),
            ArtBuchungsart.AUSGABE },
        rsd);

    if (zuRuecklagen != 0d || entRuecklagen != 0d
        || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
    {
      bezeichnung = "Zuführung zu Rücklagen nach § 62 Abs. 1 AO";
      addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
          zuRuecklagen);
      bezeichnung = "Entnahme aus Rücklagen nach § 62 Abs. 1 AO";
      addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
          entRuecklagen, null);
      bezeichnung = "          Summe der Buchungen zu Rücklagen nach § 62 Abs. 1 AO";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
          entRuecklagen, zuRuecklagen);
    }

    // Vermögen nach § 62 Abs. 3 und 4 AO
    sql = getSummenRuecklagenSql();
    Double zuVermoegen = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.VERMOEGEN.getKey(), ArtBuchungsart.EINNAHME }, rsd);

    sql = getSummenRuecklagenSql();
    Double entVermoegen = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.VERMOEGEN.getKey(), ArtBuchungsart.AUSGABE }, rsd);

    if (zuVermoegen != 0d || entVermoegen != 0d
        || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
    {
      bezeichnung = "Zuführung zum Vermögen nach § 62 Abs. 3 und 4 AO";
      addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
          zuVermoegen);
      bezeichnung = "Entnahme aus Vermögen nach § 62 Abs. 3 und 4 AO";
      addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
          entVermoegen, null);
      bezeichnung = "          Summe der Buchungen zum Vermögen nach § 62 Abs. 3 und 4 AO";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
          entVermoegen, zuVermoegen);
    }

    // Sonstige Rücklagen
    sql = getSummenRuecklagenSql();
    Double zuSonstig = (Double) service.execute(sql,
        new Object[] { datumvon, datumbis,
            Kontoart.SONSTIGE_RUECKLAGEN.getKey(), ArtBuchungsart.EINNAHME },
        rsd);

    sql = getSummenRuecklagenSql();
    Double entSonstig = (Double) service.execute(sql,
        new Object[] { datumvon, datumbis,
            Kontoart.SONSTIGE_RUECKLAGEN.getKey(), ArtBuchungsart.AUSGABE },
        rsd);

    if (zuSonstig != 0d || entSonstig != 0d
        || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
    {
      bezeichnung = "Zuführung zu sonstigen Rücklagen";
      addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
          zuSonstig);
      bezeichnung = "Entnahme aus sonstigen Rücklagen";
      addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
          entSonstig, null);
      bezeichnung = "          Summe der Buchungen aus sonstigen Rücklagen";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
          entSonstig, zuSonstig);
    }

    bezeichnung = "Vorhandene Mittel zum Ende des aktuellen GJ";
    Double einnahmen = pos1 + zufuehrung + verwendung;
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        einnahmen, null);
    bezeichnung = "Nicht der zeitnahen Mittelverwendung unterliegende Mittel zum Ende aktuellen GJ";
    Double ausgaben = pos2 + zuRuecklagen + entRuecklagen + zuVermoegen
        + entVermoegen + zuSonstig + entSonstig;
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
        ausgaben);
    bezeichnung = "          Verwendungsüberhang/Rückstand zum Ende des aktuellen GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        einnahmen, -ausgaben);

    // Leerzeile am Ende wegen Scrollbar
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED,
        null, null, null, null));
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

  private String getSummenBuchungSql() throws RemoteException
  {
    String sql = "";
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
          + " WHERE datum >= ? AND datum <= ?"
          + " AND buchung.konto = konto.id" + " AND konto.kontoart = ?"
          + " AND buchung.buchungsart = buchungsart.id"
          + " AND buchungsart.buchungsklasse = ? " + "AND buchungsart.art = ?";
    }
    else
    {
      sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
          + " WHERE datum >= ? AND datum <= ?"
          + " AND buchung.konto = konto.id" + " AND konto.kontoart = ?"
          + " AND buchung.buchungsart = buchungsart.id"
          + " AND buchung.buchungsklasse = ? " + "AND buchungsart.art = ?";
    }
    return sql;
  }

  private String getSummenUmbuchungSql() throws RemoteException
  {
    String sql = "";
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
          + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
          + " AND (konto.kontoart = ? OR konto.kontoart = ?)"
          + " AND buchung.buchungsart = buchungsart.id"
          + " AND buchungsart.buchungsklasse = ?" + " AND buchungsart.art = ?";
    }
    else
    {
      sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
          + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
          + " AND (konto.kontoart = ? OR konto.kontoart = ?)"
          + " AND buchung.buchungsart = buchungsart.id"
          + " AND buchung.buchungsklasse = ?" + " AND buchungsart.art = ?";
    }
    return sql;
  }

  private String getSummenRuecklagenSql()
  {
    return "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?  " + "AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
  }

  private void addZeile(ArrayList<MittelverwendungZeile> zeilen, int status,
      Integer position, String bezeichnung, Double einnahme, Double ausgabe)
      throws RemoteException
  {
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
