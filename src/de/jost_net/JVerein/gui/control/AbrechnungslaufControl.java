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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.AbrechnungsmodusFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.AbrechnungslaufMenu;
import de.jost_net.JVerein.gui.view.AbrechnungslaufView;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungslaufControl extends FilterControl
{

  private Abrechnungslauf abrl;

  private TablePart abrechnungslaufList;

  private LabelInput datum;

  private LabelInput abgeschlossen;

  private LabelInput modus;

  private LabelInput faelligkeit;

  private LabelInput astichtag;

  private LabelInput eintrittsdatum;

  private LabelInput austrittsdatum;

  private LabelInput zahlungsgrund;

  private LabelInput zusatzabrechnungen;

  private TextInput bemerkung;

  private LabelInput statistikbuchungen;

  private LabelInput statistiklastschriften;

  public AbrechnungslaufControl(AbstractView view) 
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Abrechnungslauf getAbrechnungslaeufe()
  {
    if (abrl != null)
    {
      return abrl;
    }
    abrl = (Abrechnungslauf) getCurrentObject();
    return abrl;
  }

  public LabelInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    datum = new LabelInput(
        new JVDateFormatTTMMJJJJ().format(getAbrechnungslaeufe().getDatum()));
    datum.setName("Datum");
    return datum;
  }

  public LabelInput getAbgeschlossen() throws RemoteException
  {
    if (abgeschlossen != null)
    {
      return abgeschlossen;
    }
    Boolean b = getAbrechnungslaeufe().getAbgeschlossen();
    abgeschlossen = new LabelInput(b ? "Ja" : "Nein");
    abgeschlossen.setName("Abgeschlossen");
    return abgeschlossen;
  }

  public LabelInput getAbrechnungsmodus() throws RemoteException
  {
    if (modus != null)
    {
      return modus;
    }
    String m = Abrechnungsmodi.get(getAbrechnungslaeufe().getModus());
    modus = new LabelInput(m);
    modus.setName("Abrechnungsmodus");
    return modus;
  }

  public LabelInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }
    faelligkeit = new LabelInput(new JVDateFormatTTMMJJJJ()
        .format(getAbrechnungslaeufe().getFaelligkeit()));
    faelligkeit.setName("Fälligkeit");
    return faelligkeit;
  }

  public LabelInput getAbrechnungStichtag() throws RemoteException
  {
    if (astichtag != null)
    {
      return astichtag;
    }
    astichtag = new LabelInput(new JVDateFormatTTMMJJJJ()
        .format(getAbrechnungslaeufe().getStichtag()));
    astichtag.setName("Stichtag");
    return astichtag;
  }

  public LabelInput getEintrittsdatum() throws RemoteException
  {
    if (eintrittsdatum != null)
    {
      return eintrittsdatum;
    }
    Date ed = getAbrechnungslaeufe().getEintrittsdatum();
    if(ed.equals(Einstellungen.NODATE))
      eintrittsdatum = new LabelInput(null);
    else
      eintrittsdatum = new LabelInput(new JVDateFormatTTMMJJJJ().format(ed));
    eintrittsdatum.setName("Eintrittsdatum");
    return eintrittsdatum;
  }

  public LabelInput getAustrittsdatum() throws RemoteException
  {
    if (austrittsdatum != null)
    {
      return austrittsdatum;
    }
    Date ed = getAbrechnungslaeufe().getAustrittsdatum();
    if(ed.equals(Einstellungen.NODATE))
      austrittsdatum = new LabelInput(null);
    else
      austrittsdatum = new LabelInput(new JVDateFormatTTMMJJJJ().format(ed));
    austrittsdatum.setName("Austrittsdatum");
    return austrittsdatum;
  }

  public LabelInput getZahlungsgrund() throws RemoteException
  {
    if (zahlungsgrund != null)
    {
      return zahlungsgrund;
    }
    zahlungsgrund = new LabelInput(getAbrechnungslaeufe().getZahlungsgrund());
    zahlungsgrund.setName("Zahlungsgrund");
    return zahlungsgrund;
  }

  public LabelInput getZusatzAbrechnungen() throws RemoteException
  {
    if (zusatzabrechnungen != null)
    {
      return zusatzabrechnungen;
    }
    String zs = "";
    if (getAbrechnungslaeufe().getZusatzbetraege())
    {
      zs += "Zusatzbeträge ";
    }
    if (getAbrechnungslaeufe().getKursteilnehmer())
    {
      zs += "Kursteilnehmer ";
    }
    zusatzabrechnungen = new LabelInput(zs);
    zusatzabrechnungen.setName("Weitere Abrechnungen");
    return zusatzabrechnungen;
  }

  public Input getBemerkung() throws RemoteException
  {
    if (bemerkung != null)
    {
      return bemerkung;
    }
    bemerkung = new TextInput(getAbrechnungslaeufe().getBemerkung(), 80);
    bemerkung.setName("Bemerkung");
    return bemerkung;
  }

  final class StatData
  {
    Double summe;

    Integer anzahl;
  }

  public LabelInput getStatistikBuchungen() throws RemoteException
  {
    // Summe und Anzahl der Buchungen. Es gibt einen weiterer Datensatz
    // bei dem der Name "JVerein" und der Zweck "Gegenbuchung" ist.
    // Es handelt sich um die Gegenbuchung mit umgekehrten Vorzeichen.

    if (statistikbuchungen != null)
    {
      return statistikbuchungen;
    }

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        StatData ret = new StatData();
        while (rs.next())
        {
          ret.summe = rs.getDouble(1);
          ret.anzahl = rs.getInt(2);
        }
        return ret;
      }
    };

    String sql = "SELECT SUM(betrag), COUNT(id) " + "FROM mitgliedskonto "
        + "WHERE abrechnungslauf=?";
    StatData data = (StatData) Einstellungen.getDBService().execute(sql,
        new Object[] { getAbrechnungslaeufe().getID() }, rs);

    CurrencyFormatter cf = new CurrencyFormatter("EUR",
        Einstellungen.DECIMALFORMAT);
    String s = String.format("Anzahl: %s; Summe: %s", data.anzahl.toString(),
        cf.format(data.summe));
    statistikbuchungen = new LabelInput(s);
    statistikbuchungen.setName("Sollbuchungen");

    return statistikbuchungen;
  }

  public LabelInput getStatistikLastschriften() throws RemoteException
  {
    // Summe und Anzahl der Lastschriften.

    if (statistiklastschriften != null)
    {
      return statistiklastschriften;
    }

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        StatData ret = new StatData();
        while (rs.next())
        {
          ret.summe = rs.getDouble(1);
          ret.anzahl = rs.getInt(2);
        }
        return ret;
      }
    };

    String sql = "SELECT SUM(betrag), COUNT(id) " + "FROM lastschrift "
        + "WHERE abrechnungslauf=?";
    StatData data = (StatData) Einstellungen.getDBService().execute(sql,
        new Object[] { getAbrechnungslaeufe().getID() }, rs);

    CurrencyFormatter cf = new CurrencyFormatter("EUR",
        Einstellungen.DECIMALFORMAT);
    String s = String.format("Anzahl: %s; Summe: %s", data.anzahl.toString(),
        cf.format(data.summe));
    statistiklastschriften = new LabelInput(s);
    statistiklastschriften.setName("Lastschriften");

    return statistiklastschriften;
  }

  public void handleStore()
  {
    // Es kann nur die Bemerkung verändert werden
    try
    {
      Abrechnungslauf al = getAbrechnungslaeufe();
      al.setBemerkung((String) getBemerkung().getValue());
      try
      {
        al.store();
        GUI.getStatusBar()
            .setSuccessText("Bemerkung zum Abrechnungslauf gespeichert");
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern des Abrechnungslaufs";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getAbrechnungslaeufeList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Abrechnungslauf> abrechnungslaeufe = service
        .createList(Abrechnungslauf.class);
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      abrechnungslaeufe.addFilter("datum >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      abrechnungslaeufe.addFilter("datum <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    abrechnungslaeufe.setOrder("ORDER BY datum DESC");

    if (abrechnungslaufList == null)
    {
      abrechnungslaufList = new TablePart(abrechnungslaeufe,
          new EditAction(AbrechnungslaufView.class));
      abrechnungslaufList.addColumn("Nr", "nr");
      abrechnungslaufList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Modus", "modus",
          new AbrechnungsmodusFormatter(), false, Column.ALIGN_LEFT);
      abrechnungslaufList.addColumn("Fälligkeit", "faelligkeit",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Stichtag", "stichtag",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Eintrittsdatum", "eingabedatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Austrittsdatum", "austrittsdatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Zahlungsgrund", "zahlungsgrund");
      if (Einstellungen.getEinstellung().getZusatzbetrag())
      {
        abrechnungslaufList.addColumn("Zusatzbeträge", "zusatzbetraege",
            new JaNeinFormatter());
      }
      if (Einstellungen.getEinstellung().getKursteilnehmer())
      {
        abrechnungslaufList.addColumn("Kursteilnehmer", "kursteilnehmer",
            new JaNeinFormatter());
      }
      abrechnungslaufList.setContextMenu(new AbrechnungslaufMenu());
      abrechnungslaufList.setRememberColWidths(true);
      abrechnungslaufList.setRememberOrder(true);
      abrechnungslaufList.addFeature(new FeatureSummary());
    }
    else
    {
      abrechnungslaufList.removeAll();
      while (abrechnungslaeufe.hasNext())
      {
        abrechnungslaufList.addItem(abrechnungslaeufe.next());
      }
      abrechnungslaufList.sort();
    }
    return abrechnungslaufList;
  }
  
  public void TabRefresh()
  {
    if (abrechnungslaufList == null)
    {
      return;
    }
    try
    {
      getAbrechnungslaeufeList();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

}
