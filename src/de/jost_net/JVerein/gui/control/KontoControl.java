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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.KontoAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.input.KontoInput;
import de.jost_net.JVerein.gui.menu.KontoMenu;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
//import de.jost_net.JVerein.keys.ArtBuchungsart;

public class KontoControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart kontenList;

  private TextInput nummer;

  private TextInput bezeichnung;

  private DateInput eroeffnung;

  private DateInput aufloesung;

  private SelectInput hibiscusid;

  private Konto konto;
  
  private SelectInput buchungsart;
  
  private int unterdrueckunglaenge = 0;

  public KontoControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  private Konto getKonto()
  {
    if (konto != null)
    {
      return konto;
    }
    konto = (Konto) getCurrentObject();
    return konto;
  }

  public TextInput getNummer() throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }
    nummer = new TextInput(getKonto().getNummer(), 35);
    return nummer;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getKonto().getBezeichnung(), 255);
    return bezeichnung;
  }

  public DateInput getEroeffnung() throws RemoteException
  {
    if (eroeffnung != null)
    {
      return eroeffnung;
    }
    eroeffnung = new DateInput(getKonto().getEroeffnung(),
        new JVDateFormatTTMMJJJJ());
    return eroeffnung;
  }

  public DateInput getAufloesung() throws RemoteException
  {
    if (aufloesung != null)
    {
      return aufloesung;
    }
    aufloesung = new DateInput(getKonto().getAufloesung(),
        new JVDateFormatTTMMJJJJ());
    return aufloesung;
  }

  public SelectInput getHibiscusId() throws RemoteException
  {
    if (hibiscusid != null)
    {
      return hibiscusid;
    }
    de.willuhn.jameica.hbci.rmi.Konto preselected = null;
    String hibid = "-1";
    try
    {
      hibid = getKonto().getHibiscusId().toString();
      if (!hibid.equals("-1"))
      {
        try
        {
          preselected = (de.willuhn.jameica.hbci.rmi.Konto) Settings
              .getDBService()
              .createObject(de.willuhn.jameica.hbci.rmi.Konto.class, hibid);
        }
        catch (ObjectNotFoundException e)
        {
          //
        }
      }
    }
    catch (NullPointerException e)
    {
      // nichts zu tun.
    }
    this.hibiscusid = new KontoInput(preselected);
    return hibiscusid;
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    try
    {
      Konto k = getKonto();
      k.setNummer((String) getNummer().getValue());
      k.setBezeichnung((String) getBezeichnung().getValue());
      k.setEroeffnung((Date) getEroeffnung().getValue());
      k.setAufloesung((Date) getAufloesung().getValue());
      k.setBuchungsart(getSelectedBuchungsArtId());
      if (getHibiscusId().getValue() == null)
      {
        k.setHibiscusId(-1);
      }
      else
      {
        de.willuhn.jameica.hbci.rmi.Konto hkto = (de.willuhn.jameica.hbci.rmi.Konto) getHibiscusId()
            .getValue();
        k.setHibiscusId(Integer.parseInt(hkto.getID()));
      }
      k.store();
      GUI.getStatusBar().setSuccessText("Konto gespeichert");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Kontos";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    catch (ApplicationException e)
    {
      String fehler = "Fehler bei speichern des Kontos";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getKontenList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Konto> konten = service.createList(Konto.class);
    konten.setOrder("ORDER BY nummer");

    kontenList = new TablePart(konten, new KontoAction());
    kontenList.addColumn("Nummer", "nummer");
    kontenList.addColumn("Bezeichnung", "bezeichnung");
    kontenList.addColumn("Hibiscus-Konto", "hibiscusid", new Formatter()
    {

      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "nein";
        }
        if (o instanceof Integer)
        {
          Integer hibid = (Integer) o;
          if (hibid.intValue() >= 0)
          {
            return "ja";
          }
        }
        return "nein";
      }
    }, false, Column.ALIGN_LEFT);
    kontenList.addColumn("Konto-Eröffnung", "eroeffnung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Konto-Auflösung", "aufloesung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Gegenbuchung-Buchungsart", "buchungsart", 
        new BuchungsartFormatter());
    kontenList.setRememberColWidths(true);
    kontenList.setContextMenu(new KontoMenu());
    kontenList.setRememberOrder(true);
    kontenList.addFeature(new FeatureSummary());
    return kontenList;
  }

  public void refreshTable() throws RemoteException
  {
    kontenList.removeAll();
    DBIterator<Konto> konten = Einstellungen.getDBService()
        .createList(Konto.class);
    konten.setOrder("ORDER BY nummer");
    while (konten.hasNext())
    {
      kontenList.addItem(konten.next());
    }
  }

  public Input getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    ArrayList<Buchungsart> liste = new ArrayList<>();
    unterdrueckunglaenge = Einstellungen.getEinstellung().getUnterdrueckungLaenge();
    final DBService service = Einstellungen.getDBService();
    
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Buchungsart> list = new ArrayList<Buchungsart>();
        while (rs.next())
        {
          list.add(
            (Buchungsart) service.createObject(Buchungsart.class, rs.getString(1)));
        }
        return list;
      }
    };
    if (unterdrueckunglaenge > 0)
    {
      Calendar cal = Calendar.getInstance();
      Date db = cal.getTime();
      cal.add(Calendar.MONTH, - unterdrueckunglaenge);
      Date dv = cal.getTime();
      String sql = "SELECT DISTINCT ba.* FROM buchungsart ba ";
      sql += "LEFT JOIN konto k ON k.buchungsart = ba.id, buchung bu ";
      if (konto.getBuchungsart() == null)
      {
        sql += "WHERE (k.buchungsart IS NULL) ";
      }
      else
      {
        sql += "WHERE (k.buchungsart IS NULL OR k.buchungsart = ?) ";
      }
      sql += "AND ba.id IS NOT NULL AND ba.id = bu.buchungsart ";
      sql += "AND bu.datum >= ? AND bu.datum <= ? ";
      sql += "AND ba.art = ? ";
      if (Einstellungen.getEinstellung()
          .getBuchungsartSort() == BuchungsartSort.NACH_NUMMER)
      {
        sql += "ORDER BY nummer";
      }
      else
      {
        sql += "ORDER BY bezeichnung";
      }

      if (konto.getBuchungsart() == null)
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service.execute(sql,
            new Object[] { dv, db, ArtBuchungsart.UMBUCHUNG }, rs);    
        addToList(liste, ergebnis);
      }
      else
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service.execute(sql,
            new Object[] { konto.getBuchungsartId(), dv, db, ArtBuchungsart.UMBUCHUNG }, rs);
        addToList(liste, ergebnis);
      }
    }
    else
    {
      String sql = "SELECT DISTINCT ba.* FROM buchungsart ba ";
      sql += "LEFT JOIN konto k ON k.buchungsart = ba.id ";
      if (konto.getBuchungsart() == null)
      {
        sql += "WHERE (k.buchungsart IS NULL) ";
      }
      else
      {
        sql += "WHERE (k.buchungsart IS NULL OR k.buchungsart = ?) ";
      }
      sql += "AND ba.art = ? ";
      if (Einstellungen.getEinstellung()
          .getBuchungsartSort() == BuchungsartSort.NACH_NUMMER)
      {
        sql += "ORDER BY nummer";
      }
      else
      {
        sql += "ORDER BY bezeichnung";
      }

      if (konto.getBuchungsart() == null)
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service.execute(sql,
            new Object[] { ArtBuchungsart.UMBUCHUNG }, rs);    
        addToList(liste, ergebnis);
      }
      else
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service.execute(sql,
            new Object[] { konto.getBuchungsartId(), ArtBuchungsart.UMBUCHUNG }, rs);
        addToList(liste, ergebnis);
      }
    }
    
    Buchungsart b = konto.getBuchungsart();
    buchungsart = new SelectInput(liste, b);
    buchungsart.setPleaseChoose("Bitte wählen...");

    switch (Einstellungen.getEinstellung().getBuchungsartSort())
    {
      case BuchungsartSort.NACH_NUMMER:
        buchungsart.setAttribute("nrbezeichnung");
        break;
      case BuchungsartSort.NACH_BEZEICHNUNG_NR:
        buchungsart.setAttribute("bezeichnungnr");
        break;
      default:
        buchungsart.setAttribute("bezeichnung");
        break;
    }
    
    return buchungsart;
  }
  
  private Long getSelectedBuchungsArtId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getBuchungsart().getValue();
      if (null == buchungsArt)
        return null;
      Long id = Long.valueOf(buchungsArt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsart kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }
  
  private void addToList(ArrayList<Buchungsart> liste, ArrayList<Buchungsart> ergebnis)
  {
    int size = ergebnis.size();
    for (int i = 0; i < size; i++)
    {
      liste.add(ergebnis.get(i));
    }
  }
  
}
