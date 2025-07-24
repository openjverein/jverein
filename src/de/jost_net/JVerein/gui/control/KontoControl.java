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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.IntegerNullInput;
import de.jost_net.JVerein.gui.input.KontoInput;
import de.jost_net.JVerein.gui.menu.KontoMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.KontoDetailView;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
//import de.jost_net.JVerein.keys.ArtBuchungsart;

public class KontoControl extends FilterControl implements Savable
{

  private JVereinTablePart kontenList;

  private TextInput nummer;

  private TextInput bezeichnung;

  private DateInput eroeffnung;

  private DateInput aufloesung;

  private SelectInput hibiscusid;

  private Konto konto;

  private SelectInput buchungsart;

  private SelectInput kontoart;

  private int unterdrueckunglaenge = 0;

  private AbstractInput anlagenart;

  private SelectInput buchungsklasse;

  private AbstractInput afaart;

  private DecimalInput betrag;

  private IntegerNullInput nutzungsdauer;

  private TextAreaInput kommentar;

  private DateInput anschaffung;

  private DecimalInput afastart;

  private DecimalInput afadauer;

  private DecimalInput afarestwert;

  private SelectInput afamode;

  private SelectInput anlagenzweck;

  Button autobutton;

  Button afabutton;

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
    nummer.setMandatory(true);
    return nummer;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getKonto().getBezeichnung(), 255);
    bezeichnung.setMandatory(true);
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

  public DateInput getAnschaffung() throws RemoteException
  {
    if (anschaffung != null)
    {
      return anschaffung;
    }
    anschaffung = new DateInput(getKonto().getAnschaffung(),
        new JVDateFormatTTMMJJJJ());
    if (((Kontoart) getKontoArt().getValue()) != Kontoart.ANLAGE)
    {
      anschaffung.setValue(null);
      anschaffung.disable();
    }
    return anschaffung;
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

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Konto k = getKonto();
    k.setNummer((String) getNummer().getValue());
    k.setBezeichnung((String) getBezeichnung().getValue());
    k.setEroeffnung((Date) getEroeffnung().getValue());
    k.setAufloesung((Date) getAufloesung().getValue());
    k.setBuchungsartId(getSelectedBuchungsArtId());
    k.setKommentar((String) getKommentar().getValue());
    k.setKontoArt((Kontoart) getKontoArt().getValue());
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
    k.setAnlagenartId(getSelectedAnlagenartId());
    k.setBuchungsklasseId(getSelectedBuchungsklasseId());
    k.setAfaartId(getSelectedAfaartId());
    k.setBetrag((Double) getBetrag().getValue());
    k.setNutzungsdauer((Integer) getNutzungsdauer().getValue());
    k.setAnschaffung((Date) getAnschaffung().getValue());
    k.setAfaStart((Double) getAfaStart().getValue());
    k.setAfaDauer((Double) getAfaDauer().getValue());
    k.setAfaRestwert((Double) getAfaRestwert().getValue());
    if (getAfaMode().getValue() == null)
      k.setAfaMode(null);
    else
    {
      k.setAfaMode(
          Integer.valueOf(((AfaMode) getAfaMode().getValue()).getKey()));
    }
    if (anlagenzweck != null)
    {
      k.setAnlagenzweck((Anlagenzweck) getAnlagenzweck().getValue());
    }
    return k;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore();

      DBService service = Einstellungen.getDBService();
      String sql = "SELECT DISTINCT konto.id from konto "
          + "WHERE (kontoart = ?) ";
      boolean exist = (boolean) service.execute(sql,
          new Object[] { Kontoart.ANLAGE.getKey() }, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              if (rs.next())
              {
                return true;
              }
              return false;
            }
          });
      if (!exist && getKonto().getKontoArt() == Kontoart.ANLAGE)
      {
        SimpleDialog d = new SimpleDialog(SimpleDialog.POSITION_CENTER);
        d.setTitle("Erstes Anlagenkonto");
        d.setText(
            "Beim ersten Anlagenkonto bitte JVerein neu starten um die Änderungen anzuwenden");
        try
        {
          d.open();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
        }
      }
      Konto k = getKonto();
      k.store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Kontos";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getKontenList() throws RemoteException
  {
    kontenList = new JVereinTablePart(getKonten(), null);
    kontenList.addColumn("Nummer", "nummer");
    kontenList.addColumn("Bezeichnung", "bezeichnung");
    kontenList.addColumn("Kontoart", "kontoart", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        if (o instanceof Integer)
        {
          return Kontoart.getByKey((Integer) o).getText();
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
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
    kontenList.addColumn("Eröffnungsdatum", "eroeffnung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Auflösungsdatum", "aufloesung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Gegenbuchung-Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    kontenList.setRememberColWidths(true);
    kontenList.setContextMenu(new KontoMenu(kontenList));
    kontenList.setRememberOrder(true);
    kontenList.addFeature(new FeatureSummary());
    kontenList.setAction(new EditAction(KontoDetailView.class, kontenList));
    VorZurueckControl.setObjektListe(null, null);
    return kontenList;
  }

  public void refreshTable()
  {
    TabRefresh();
  }

  protected void TabRefresh()
  {
    if (kontenList == null)
    {
      return;
    }
    kontenList.removeAll();
    try
    {
      kontenList.removeAll();
      DBIterator<Konto> konten = getKonten();
      while (konten.hasNext())
      {
        kontenList.addItem(konten.next());
      }
      kontenList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Konto> getKonten() throws RemoteException
  {
    DBIterator<Konto> konten = Einstellungen.getDBService()
        .createList(Konto.class);

    if (isSuchKontoartAktiv() && getSuchKontoart().getValue() != null)
    {
      konten.addFilter("kontoart = ?",
          new Object[] { ((Kontoart) getSuchKontoart().getValue()).getKey() });
    }
    if (isSuchStatusAktiv() && getSuchStatus(null).getValue().toString()
        .equalsIgnoreCase("Nur aktive Konten"))
      konten.addFilter("(aufloesung IS NULL OR aufloesung > ?)",
          new Object[] { new Date() });
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        konten.addFilter("(lower(bezeichnung) like ?)",
            new Object[] { "%" + tmpSuchname.toLowerCase() + "%" });
      }
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        konten.addFilter("(lower(nummer) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%" });
      }
    }
    konten.setOrder("ORDER BY nummer");
    return konten;
  }

  public Input getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    ArrayList<Buchungsart> liste = new ArrayList<>();
    unterdrueckunglaenge = (Integer) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGLAENGE);
    final DBService service = Einstellungen.getDBService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Buchungsart> list = new ArrayList<Buchungsart>();
        while (rs.next())
        {
          list.add((Buchungsart) service.createObject(Buchungsart.class,
              rs.getString(1)));
        }
        return list;
      }
    };
    if (unterdrueckunglaenge > 0)
    {
      Calendar cal = Calendar.getInstance();
      Date db = cal.getTime();
      cal.add(Calendar.MONTH, -unterdrueckunglaenge);
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
      sql += "AND ba.id IS NOT NULL AND ba.art = ? ";
      sql += "AND ((ba.id = bu.buchungsart ";
      sql += "AND bu.datum >= ? AND bu.datum <= ? ";
      sql += "AND ba.status = ?) OR ba.status = ?) ";
      if ((Integer) Einstellungen.getEinstellung(
          Property.BUCHUNGSARTSORT) == BuchungsartSort.NACH_NUMMER)
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
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service
            .execute(sql, new Object[] { ArtBuchungsart.UMBUCHUNG, dv, db,
                StatusBuchungsart.AUTO, StatusBuchungsart.ACTIVE }, rs);
        addToList(liste, ergebnis);
      }
      else
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service
            .execute(sql,
                new Object[] { konto.getBuchungsartId(),
                    ArtBuchungsart.UMBUCHUNG, dv, db, StatusBuchungsart.AUTO,
                    StatusBuchungsart.ACTIVE },
                rs);
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
      sql += "AND ba.art = ? AND ba.status != ? ";
      if ((Integer) Einstellungen.getEinstellung(
          Property.BUCHUNGSARTSORT) == BuchungsartSort.NACH_NUMMER)
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
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service
            .execute(sql, new Object[] { ArtBuchungsart.UMBUCHUNG,
                StatusBuchungsart.INACTIVE }, rs);
        addToList(liste, ergebnis);
      }
      else
      {
        @SuppressWarnings("unchecked")
        ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service
            .execute(sql, new Object[] { konto.getBuchungsartId(),
                ArtBuchungsart.UMBUCHUNG, StatusBuchungsart.INACTIVE }, rs);
        addToList(liste, ergebnis);
      }
    }

    Buchungsart b = konto.getBuchungsart();
    if (liste != null && b != null && !liste.contains(b))
      liste.add(b);
    buchungsart = new SelectInput(liste, b);
    buchungsart.setPleaseChoose("Bitte auswählen");

    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
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

  private void addToList(ArrayList<Buchungsart> liste,
      ArrayList<Buchungsart> ergebnis)
  {
    int size = ergebnis.size();
    for (int i = 0; i < size; i++)
    {
      liste.add(ergebnis.get(i));
    }
  }

  public SelectInput getKontoArt() throws RemoteException
  {
    if (kontoart != null)
    {
      return kontoart;
    }
    Kontoart art = Kontoart.GELD;
    if (!getKonto().isNewObject())
    {
      art = getKonto().getKontoArt();
    }
    ArrayList<Kontoart> values = new ArrayList<Kontoart>(
        Arrays.asList(Kontoart.values()));
    values.remove(Kontoart.LIMIT);
    values.remove(Kontoart.LIMIT_RUECKLAGE);
    kontoart = new SelectInput(values, art);
    kontoart.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        refreshGui();
      }
    });
    return kontoart;
  }

  public Input getAnlagenart() throws RemoteException
  {
    if (anlagenart != null)
    {
      return anlagenart;
    }
    Version version = Application.getManifest().getVersion();
    if (version.compareTo(new Version("2.10.5")) < 0)
    {
      anlagenart = new BuchungsartInput().getBuchungsartInput(anlagenart,
          getKonto().getAnlagenart(), buchungsarttyp.ANLAGENART,
          AbstractInputAuswahl.ComboBox);
    }
    else
    {
      anlagenart = new BuchungsartInput().getBuchungsartInput(anlagenart,
          getKonto().getAnlagenart(), buchungsarttyp.ANLAGENART,
          (Integer) Einstellungen
              .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    }
    anlagenart.addListener(new AnlagenartListener());
    if (getKontoArt().getValue() == Kontoart.ANLAGE)
    {
      anlagenart.setMandatory(true);
    }
    else
    {
      anlagenart.setMandatory(false);
      anlagenart.setValue(null);
      anlagenart.disable();
    }
    return anlagenart;
  }

  private Long getSelectedAnlagenartId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getAnlagenart().getValue();
      if (null == buchungsArt)
        return null;
      Long id = Long.valueOf(buchungsArt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Anlagensart kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    list.setOrder(getBuchungartSortOrder());
    buchungsklasse = new SelectInput(
        list != null ? PseudoIterator.asList(list) : null,
        getKonto().getBuchungsklasse());
    buchungsklasse.setAttribute(getBuchungartAttribute());
    buchungsklasse.setPleaseChoose("Bitte auswählen");
    if (getKontoArt().getValue() == Kontoart.ANLAGE)
    {
      buchungsklasse.setMandatory(true);
    }
    else
    {
      buchungsklasse.setMandatory(false);
    }
    return buchungsklasse;
  }

  private Long getSelectedBuchungsklasseId() throws ApplicationException
  {
    try
    {
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse()
          .getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public Input getAfaart() throws RemoteException
  {
    if (afaart != null)
    {
      return afaart;
    }
    Version version = Application.getManifest().getVersion();
    if (version.compareTo(new Version("2.10.5")) < 0)
    {
      afaart = new BuchungsartInput().getBuchungsartInput(afaart,
          getKonto().getAfaart(), buchungsarttyp.AFAART,
          AbstractInputAuswahl.ComboBox);
    }
    else
    {
      afaart = new BuchungsartInput().getBuchungsartInput(afaart,
          getKonto().getAfaart(), buchungsarttyp.AFAART, (Integer) Einstellungen
              .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    }
    afaart.addListener(new AnlagenartListener());
    if (getKontoArt().getValue() == Kontoart.ANLAGE)
    {
      afaart.setMandatory(true);
    }
    else
    {
      afaart.setMandatory(false);
      afaart.setValue(null);
      afaart.disable();
    }
    return afaart;
  }

  private Long getSelectedAfaartId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getAfaart().getValue();
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

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getKonto().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.addListener(new Listener()
    {
      public void handleEvent(Event e)
      {
        try
        {
          if (getBetrag().getValue() != null)
            getAutobutton().setEnabled(false);
          else
            getAutobutton().setEnabled(true);
        }
        catch (RemoteException re)
        {
          Logger.error("Fehler beim Konto auto button Listener", re);
        }
      }
    });
    if (getKontoArt().getValue() != Kontoart.ANLAGE)
    {
      betrag.setValue(null);
      betrag.disable();
    }
    return betrag;
  }

  public IntegerNullInput getNutzungsdauer() throws RemoteException
  {
    if (nutzungsdauer != null)
    {
      return nutzungsdauer;
    }
    if (getKonto().getNutzungsdauer() != null)
    {
      nutzungsdauer = new IntegerNullInput(getKonto().getNutzungsdauer());
    }
    else
    {
      nutzungsdauer = new IntegerNullInput();
    }
    if (getKontoArt().getValue() != Kontoart.ANLAGE)
    {
      nutzungsdauer.setValue(null);
      nutzungsdauer.disable();
    }
    return nutzungsdauer;
  }

  public Input getKommentar() throws RemoteException
  {
    if (kommentar != null && !kommentar.getControl().isDisposed())
    {
      return kommentar;
    }
    kommentar = new TextAreaInput(getKonto().getKommentar(), 1024);
    kommentar.setHeight(90);
    return kommentar;
  }

  public DecimalInput getAfaStart() throws RemoteException
  {
    if (afastart != null)
    {
      return afastart;
    }
    afastart = new DecimalInput(getKonto().getAfaStart(),
        Einstellungen.DECIMALFORMAT);
    if (getKontoArt().getValue() != Kontoart.ANLAGE
        || getAfaMode().getValue() == null
        || ((AfaMode) getAfaMode().getValue()).getKey() != AfaMode.ANGEPASST)
    {
      afastart.setMandatory(false);
      afastart.setValue(null);
      afastart.disable();
    }
    else
    {
      afastart.setMandatory(true);
    }
    return afastart;
  }

  public DecimalInput getAfaDauer() throws RemoteException
  {
    if (afadauer != null)
    {
      return afadauer;
    }
    afadauer = new DecimalInput(getKonto().getAfaDauer(),
        Einstellungen.DECIMALFORMAT);
    if (getKontoArt().getValue() != Kontoart.ANLAGE
        || getAfaMode().getValue() == null
        || ((AfaMode) getAfaMode().getValue()).getKey() != AfaMode.ANGEPASST)
    {
      afadauer.setMandatory(false);
      afadauer.setValue(null);
      afadauer.disable();
    }
    else
    {
      afadauer.setMandatory(true);
    }
    return afadauer;
  }

  public DecimalInput getAfaRestwert() throws RemoteException
  {
    if (afarestwert != null)
    {
      return afarestwert;
    }
    afarestwert = new DecimalInput(getKonto().getAfaRestwert(),
        Einstellungen.DECIMALFORMAT);
    if (getKontoArt().getValue() != Kontoart.ANLAGE)
    {
      afarestwert.setValue(null);
      afarestwert.disable();
    }
    return afarestwert;
  }

  public SelectInput getAfaMode() throws RemoteException
  {
    if (afamode != null)
    {
      return afamode;
    }
    if (getKonto().getAfaMode() == null)
      afamode = new SelectInput(AfaMode.getArray(), null);
    else
      afamode = new SelectInput(AfaMode.getArray(),
          new AfaMode(getKonto().getAfaMode()));
    afamode.setPleaseChoose("Bitte auswählen");
    afamode.addListener(new Listener()
    {
      public void handleEvent(Event e)
      {
        try
        {
          if (getAfaMode().getValue() != null
              && ((AfaMode) getAfaMode().getValue())
                  .getKey() == AfaMode.ANGEPASST)
          {
            getAfaStart().enable();
            getAfaStart().setMandatory(true);
            getAfaDauer().enable();
            getAfaDauer().setMandatory(true);
            getAfabutton().setEnabled(true);
          }
          else
          {
            getAfaStart().setMandatory(false);
            getAfaStart().setValue(null);
            getAfaStart().disable();
            getAfaDauer().setMandatory(false);
            getAfaDauer().setValue(null);
            getAfaDauer().disable();
            getAfabutton().setEnabled(false);
          }
        }
        catch (RemoteException re)
        {
          Logger.error("Fehler beim Konto autoAfA button Listener", re);
        }
      }
    });
    if (getKontoArt().getValue() == Kontoart.ANLAGE)
    {
      afamode.setMandatory(true);
    }
    else
    {
      afamode.setMandatory(false);
      afamode.setValue(null);
      afamode.disable();
    }
    return afamode;
  }

  public SelectInput getAnlagenzweck() throws RemoteException
  {
    if (anlagenzweck != null)
    {
      return anlagenzweck;
    }
    Anlagenzweck zweck = getKonto().getAnlagenzweck();
    ArrayList<Anlagenzweck> values = new ArrayList<Anlagenzweck>(
        Arrays.asList(Anlagenzweck.values()));
    anlagenzweck = new SelectInput(values, zweck);
    if (getKontoArt().getValue() != Kontoart.ANLAGE)
    {
      anlagenzweck.setValue(Anlagenzweck.NUTZUNGSGEBUNDEN);
      anlagenzweck.disable();
    }
    anlagenzweck.setPleaseChoose("Bitte wählen");
    return anlagenzweck;
  }

  public String getBuchungartSortOrder()
  {
    try
    {
      switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
      {
        case BuchungsartSort.NACH_NUMMER:
          return "ORDER BY nummer";
        default:
          return "ORDER BY bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }

    return "ORDER BY bezeichnung";
  }

  public String getBuchungartAttribute()
  {
    try
    {
      switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
      {
        case BuchungsartSort.NACH_NUMMER:
          return "nrbezeichnung";
        case BuchungsartSort.NACH_BEZEICHNUNG_NR:
          return "bezeichnungnr";
        default:
          return "bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }

    return "bezeichnung";
  }

  public class AnlagenartListener implements Listener
  {

    AnlagenartListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      try
      {
        Buchungsart ba = (Buchungsart) getAnlagenart().getValue();
        if (ba != null)
        {
          if (getBuchungsklasse().getValue() == null)
            getBuchungsklasse().setValue(ba.getBuchungsklasse());
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
      }
    }
  }

  public void refreshGui()
  {
    try
    {
      if (getKontoArt().getValue() == Kontoart.ANLAGE)
      {
        getBuchungsklasse().setMandatory(true);
        getAnlagenart().enable();
        getAnlagenart().setMandatory(true);
        getAfaart().enable();
        getAfaart().setMandatory(true);
        getBetrag().enable();
        getNutzungsdauer().enable();
        getAnschaffung().enable();
        getAfaRestwert().enable();
        getAfaRestwert().setValue(
            (Boolean) Einstellungen.getEinstellung(Property.AFARESTWERT));
        if (getBetrag().getValue() == null)
          getAutobutton().setEnabled(true);
        getAfabutton().setEnabled(false);
        getAfaMode().enable();
        getAfaMode().setValue(new AfaMode(AfaMode.AUTO));
        getAfaMode().setMandatory(true);
        getAfaMode().setEnabled(true);
        if (anlagenzweck != null)
        {
          anlagenzweck.enable();
          anlagenzweck.setValue(getKonto().getAnlagenzweck());
        }
      }
      else
      {
        getBuchungsklasse().setMandatory(false);
        getAnlagenart().setMandatory(false);
        getAnlagenart().setValue(null);
        getAnlagenart().disable();
        getAfaart().setMandatory(false);
        getAfaart().setValue(null);
        getAfaart().disable();
        getBetrag().setValue(null);
        getBetrag().disable();
        getNutzungsdauer().setValue(null);
        getNutzungsdauer().disable();
        getAnschaffung().setValue(null);
        getAnschaffung().disable();
        getAfaStart().setMandatory(false);
        getAfaStart().setValue(null);
        getAfaStart().disable();
        getAfaDauer().setMandatory(false);
        getAfaDauer().setValue(null);
        getAfaDauer().disable();
        getAfaRestwert().setValue(null);
        getAfaRestwert().disable();
        getAutobutton().setEnabled(false);
        getAfabutton().setEnabled(false);
        getAfaMode().setMandatory(false);
        getAfaMode().setValue(null);
        getAfaMode().disable();
        if (anlagenzweck != null)
        {
          anlagenzweck.setValue(Anlagenzweck.NUTZUNGSGEBUNDEN);
          anlagenzweck.disable();
        }
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  public Button getAutobutton() throws RemoteException
  {
    if (autobutton != null)
      return autobutton;

    autobutton = new Button("Auto Anlagenwert", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        handleAuto();
      }
    }, null, true, "view-refresh.png");

    if (getBetrag().getValue() != null)
      autobutton.setEnabled(false);
    if (getKontoArt().getValue() != Kontoart.ANLAGE)
    {
      autobutton.setEnabled(false);
    }

    return autobutton;
  }

  public Button getAfabutton() throws RemoteException
  {
    if (afabutton != null)
      return afabutton;

    afabutton = new Button("Auto AfA", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        handleAfa();
      }
    }, null, true, "view-refresh.png");

    if (getKontoArt().getValue() != Kontoart.ANLAGE
        || getAfaMode().getValue() == null
        || ((AfaMode) getAfaMode().getValue()).getKey() != AfaMode.ANGEPASST)
    {
      afabutton.setEnabled(false);
    }

    return afabutton;
  }

  private void handleAuto()
  {
    Double betrag = 0d;
    DBService service;
    try
    {
      service = Einstellungen.getDBService();
      DBIterator<Buchung> buchungenIt = service.createList(Buchung.class);
      buchungenIt.join("buchungsart");
      buchungenIt.addFilter("buchungsart.id = buchung.buchungsart");
      buchungenIt.addFilter("konto = ?", new Object[] { konto.getID() });
      buchungenIt.addFilter("buchungsart.abschreibung = FALSE");
      buchungenIt.addFilter("datum <= ?",
          new Object[] { new java.sql.Date(new Date().getTime()) });
      buchungenIt.setOrder("order by datum");
      Date d = new Date();
      Buchung b;
      while (buchungenIt.hasNext())
      {
        b = (Buchung) buchungenIt.next();
        // TODO Bei der Anlage müssen wir immer Netto Beträge verwenden?
        betrag += b.getBetrag();
        d = b.getDatum();
      }
      getBetrag().setValue(betrag);
      if (getAnschaffung().getValue() == null)
        getAnschaffung().setValue(d);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim automatischen Bestimmen des Anlagenwerts");
    }
  }

  private void handleAfa()
  {
    try
    {
      Double betrag = (Double) getBetrag().getValue();
      if (betrag == null)
      {
        GUI.getStatusBar().setErrorText("Anlagenwert fehlt, bitte eingeben!");
        return;
      }
      Date anschaffung = (Date) getAnschaffung().getValue();
      if (anschaffung == null)
      {
        GUI.getStatusBar()
            .setErrorText("Anschaffungsdatum fehlt, bitte eingeben!");
        return;
      }
      Integer nutzungsdauer = (Integer) getNutzungsdauer().getValue();
      if (nutzungsdauer == null)
      {
        GUI.getStatusBar().setErrorText("Nutzungsdauer fehlt, bitte eingeben!");
        return;
      }
      Double restwert = (Double) getAfaRestwert().getValue();
      if (restwert == null)
      {
        GUI.getStatusBar().setErrorText("Restwert fehlt, bitte eingeben!");
        return;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(anschaffung);
      Integer monatAnschaffung = calendar.get(Calendar.MONTH);
      Integer year = calendar.get(Calendar.YEAR);
      Date startGJ = Datum.toDate(
          (String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR)
              + year);
      calendar.setTime(startGJ);
      Integer monatStartGJ = calendar.get(Calendar.MONTH);
      Integer monate = 12;
      if (monatAnschaffung < monatStartGJ)
        monate = monatStartGJ - monatAnschaffung;
      else if (monatAnschaffung > monatStartGJ)
        monate = monatStartGJ - monatAnschaffung + 12;
      if (nutzungsdauer == 0)
      {
        getAfaStart().setValue(betrag);
        getAfaDauer().setValue(0);
      }
      else if (nutzungsdauer == 1)
      {
        Double start = ((betrag - restwert) * monate) / 12;
        getAfaStart().setValue(start);
        getAfaDauer().setValue(betrag - start - restwert);
      }
      else
      {
        if (monate == 12)
          getAfaStart().setValue(((betrag - restwert)) / (nutzungsdauer));
        else
          getAfaStart()
              .setValue(((betrag - restwert) * monate) / (nutzungsdauer * 12));
        getAfaDauer().setValue((betrag - restwert) / nutzungsdauer);
      }
    }
    catch (Exception e)
    {
      GUI.getStatusBar().setErrorText("Fehler bei der AfA Berechnung");
    }
  }

}
