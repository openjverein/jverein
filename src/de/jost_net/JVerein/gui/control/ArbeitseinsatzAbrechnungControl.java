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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.ArbeitseinsatzUeberpruefungInput;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart.ExportArt;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ArbeitseinsatzAbrechnungControl extends AbstractAbrechnungControl
{
  public static final String MITGLIED = "mitglied";

  public static final String MITGLIED_ID = "id";

  public static final String SOLLSTUNDEN = "sollstunden";

  public static final String ISTSTUNDEN = "iststunden";

  public static final String DIFFERENZ = "differenz";

  public static final String STUNDENSATZ = "stundensatz";

  public static final String GESAMTBETRAG = "gesamtbetrag";

  private JVereinTablePart arbeitseinsatzueberpruefungList;

  private SelectInput suchjahr = null;

  private ArbeitseinsatzUeberpruefungInput auswertungschluessel = null;

  private Zusatzbetrag zusatzb;

  private final ZusatzbetragPart part;

  public ArbeitseinsatzAbrechnungControl() throws RemoteException
  {
    super();

    zusatzb = getZusatzbetrag();
    part = new ZusatzbetragPart(zusatzb, false);
  }

  public ZusatzbetragPart getPart()
  {
    return part;
  }

  public ArbeitseinsatzUeberpruefungInput getAuswertungSchluessel()
      throws RemoteException
  {
    if (auswertungschluessel != null)
    {
      return auswertungschluessel;
    }
    auswertungschluessel = new ArbeitseinsatzUeberpruefungInput(
        ArbeitseinsatzUeberpruefungInput.MINDERLEISTUNG);
    auswertungschluessel.addListener(new FilterListener());
    return auswertungschluessel;
  }

  public SelectInput getSuchJahr() throws RemoteException
  {
    if (suchjahr != null)
    {
      return suchjahr;
    }
    DBIterator<Arbeitseinsatz> list = Einstellungen.getDBService()
        .createList(Arbeitseinsatz.class);
    list.setOrder("ORDER BY datum");
    Arbeitseinsatz ae = null;
    Calendar von = Calendar.getInstance();
    if (list.hasNext())
    {
      ae = list.next();
      von.setTime(ae.getDatum());
    }
    Calendar bis = Calendar.getInstance();
    ArrayList<Integer> jahre = new ArrayList<>();

    for (int i = von.get(Calendar.YEAR); i <= bis.get(Calendar.YEAR); i++)
    {
      jahre.add(i);
    }
    suchjahr = new SelectInput(jahre, settings.getInt("jahr", jahre.get(0)));
    suchjahr.setPreselected(settings.getInt("jahr", bis.get(Calendar.YEAR)));
    suchjahr.addListener(new FilterListener());
    return suchjahr;
  }

  public Button exportButton(ExportArt art) throws ApplicationException
  {
    return new Button(art.equals(ExportArt.PDF) ? "PDF" : "CSV", context -> {
      if (arbeitseinsatzueberpruefungList == null)
      {
        throw new ApplicationException(
            "Der Export kann nicht durchgeführt werden, Tabelle ist nicht geladen.");
      }
      try
      {
        arbeitseinsatzueberpruefungList.export(
            VorlageUtil.getName(VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_TITEL,
                this),
            VorlageUtil.getName(VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL,
                this),
            VorlageUtil.getName(
                VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME, this),
            art);
      }
      catch (OperationCanceledException ex)
      {
        // Ignorieren und Dialog nicht schließen
        return;
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
    }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  private Zusatzbetrag getZusatzbetrag() throws RemoteException
  {
    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    zusatzb.setStartdatum(new Date());
    zusatzb.setBuchungstext(settings.getString("buchungstext", ""));
    zusatzb.setBetrag(0.0);
    String zahlungsweg = settings.getString("zahlungsweg", "");
    if (zahlungsweg.length() > 0)
    {
      try
      {
        Zahlungsweg weg = new Zahlungsweg(Integer.valueOf(zahlungsweg));
        zusatzb.setZahlungsweg(weg);
      }
      catch (Exception e)
      {
        //
      }
    }
    zusatzb.setMitgliedzahltSelbst(
        settings.getBoolean("mitgliedzahltselbst", false));
    return zusatzb;
  }

  public List<Zusatzbetrag> getZusatzbetraegeList()
      throws RemoteException, ApplicationException
  {
    List<Zusatzbetrag> list = new ArrayList<>();
    for (PseudoDBObject o : getList())
    {
      Mitglied m = (Mitglied) o.getAttribute(MITGLIED);
      Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
          .createObject(Zusatzbetrag.class, null);
      zb.setBetrag(o.getDouble(GESAMTBETRAG) * -1);
      zb.setBuchungstext((String) part.getBuchungstext().getValue());
      zb.setFaelligkeit((Date) getFaelligkeit().getValue());
      zb.setIntervall(IntervallZusatzzahlung.KEIN);
      zb.setMitglied(Integer.parseInt(m.getID()));
      zb.setStartdatum((Date) getFaelligkeit().getValue());
      zb.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zb.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      Beitragsgruppe b = m.getBeitragsgruppe();
      zb.setBuchungsart(b.getBuchungsart());
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        zb.setBuchungsklasseId(b.getBuchungsklasseId());
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        zb.setSteuer(b.getSteuer());
      }
      list.add(zb);
    }
    return list;
  }

  public JVereinTablePart getArbeitseinsatzUeberpruefungList()
      throws ApplicationException
  {
    try
    {
      if (arbeitseinsatzueberpruefungList == null)
      {
        arbeitseinsatzueberpruefungList = new JVereinTablePart(getList(), null);
        arbeitseinsatzueberpruefungList.addColumn("Name", MITGLIED);
        arbeitseinsatzueberpruefungList.addColumn("Sollstunden", SOLLSTUNDEN,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        arbeitseinsatzueberpruefungList.addColumn("Iststunden", ISTSTUNDEN,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        arbeitseinsatzueberpruefungList.addColumn("Differenz", DIFFERENZ,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        arbeitseinsatzueberpruefungList.addColumn("Stundensatz", STUNDENSATZ,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        arbeitseinsatzueberpruefungList.addColumn("Gesamtbetrag", GESAMTBETRAG,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        arbeitseinsatzueberpruefungList.setRememberColWidths(true);
      }
      else
      {
        saveSettings();
        arbeitseinsatzueberpruefungList.removeAll();
        for (PseudoDBObject o : getList())
        {
          arbeitseinsatzueberpruefungList.addItem(o);
        }
        arbeitseinsatzueberpruefungList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler aufgetreten", e);
    }
    return arbeitseinsatzueberpruefungList;
  }

  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();
    while (it.hasNext())
    {
      PseudoDBObject o = it.next();
      Double soll = o.getAttribute(SOLLSTUNDEN) == null ? 0d
          : o.getDouble(SOLLSTUNDEN);
      Double ist = o.getAttribute(ISTSTUNDEN) == null ? 0d
          : o.getDouble(ISTSTUNDEN);
      Double satz = o.getAttribute(STUNDENSATZ) == null ? null
          : o.getDouble(STUNDENSATZ);
      Mitglied mitglied = (Mitglied) Einstellungen.getDBService()
          .createObject(Mitglied.class, o.getAttribute(MITGLIED_ID).toString());
      o.setAttribute(DIFFERENZ, ist - soll);
      o.setAttribute(GESAMTBETRAG, satz == null ? null : (ist - soll) * satz);
      o.setAttribute(MITGLIED, mitglied);
      zeilen.add(o);
    }
    return zeilen;
  }

  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    int year = (Integer) getSuchJahr().getValue();
    int schluessel;
    if (auswertungschluessel != null)
    {
      schluessel = (Integer) getAuswertungSchluessel().getValue();
    }
    else
    {
      schluessel = ArbeitseinsatzUeberpruefungInput.MINDERLEISTUNG;
    }

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "mitglied");
    it.addColumn("mitglied.id as " + MITGLIED_ID);
    it.addColumn("arbeitseinsatzstunden as " + SOLLSTUNDEN);
    it.addColumn("beitragsgruppe.arbeitseinsatzbetrag as " + STUNDENSATZ);
    it.addColumn("sum(stunden) as " + ISTSTUNDEN);
    it.leftJoin("beitragsgruppe",
        "mitglied.beitragsgruppe = beitragsgruppe.id");
    it.leftJoin("arbeitseinsatz",
        "mitglied.id = arbeitseinsatz.mitglied and year(arbeitseinsatz.datum) = ? ",
        year);
    it.addFilter(
        "(mitglied.eintritt is null or year(mitglied.eintritt) <= ?) and "
            + "(mitglied.austritt is null or year(mitglied.austritt) >= ?) ",
        year, year);
    if (schluessel != ArbeitseinsatzUeberpruefungInput.MEHRLEISTUNG
        && schluessel != ArbeitseinsatzUeberpruefungInput.ALLE)
    {
      it.addFilter("beitragsgruppe.arbeitseinsatzstunden is not null and "
          + "beitragsgruppe.arbeitseinsatzstunden > 0");
    }
    it.addGroupBy("mitglied.id, year(arbeitseinsatz.datum)");
    if (schluessel == ArbeitseinsatzUeberpruefungInput.MINDERLEISTUNG)
    {
      it.addHaving((ISTSTUNDEN + " < " + SOLLSTUNDEN + " or " + ISTSTUNDEN
          + " is null"));
    }
    if (schluessel == ArbeitseinsatzUeberpruefungInput.PASSENDELEISTUNG)
    {
      it.addHaving(ISTSTUNDEN + " = " + SOLLSTUNDEN);
    }
    if (schluessel == ArbeitseinsatzUeberpruefungInput.MEHRLEISTUNG)
    {
      it.addHaving(ISTSTUNDEN + " > " + SOLLSTUNDEN);
    }
    if (schluessel == ArbeitseinsatzUeberpruefungInput.ALLE)
    {
      it.addHaving(ISTSTUNDEN + " > 0 or " + SOLLSTUNDEN + " > 0");
    }
    it.setOrder("Order by mitglied.name, mitglied.vorname, mitglied.id ");
    return it;
  }

  private void refreshList()
  {
    try
    {
      getArbeitseinsatzUeberpruefungList();
    }
    catch (ApplicationException e1)
    {
      //
    }
  }

  private class FilterListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      refreshList();
    }
  }

  @Override
  protected void saveSettings()
  {
    super.saveSettings();
    try
    {
      settings.setAttribute("jahr", (Integer) getSuchJahr().getValue());
      settings.setAttribute("buchungstext",
          (String) part.getBuchungstext().getValue());
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if (weg != null)
      {
        settings.setAttribute("zahlungsweg", weg.getKey());
      }
      else
      {
        settings.setAttribute("zahlungsweg", "");
      }
      Boolean tmp = (Boolean) part.getMitgliedzahltSelbst().getValue();
      if (tmp != null)
      {
        settings.setAttribute("mitgliedzahltselbst", tmp);
      }
      else
      {
        settings.setAttribute("mitgliedzahltselbst", "false");
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler", re);
    }
  }

  @Override
  protected List<Bug> getBugs()
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    boolean global = true;

    try
    {
      // Prüfen ob das Verrechnungskonto gesetzt ist. Das wird auch beim
      // Abrechnungslauf am Anfang geholt.
      checkVerrechnungskonto(bugs);

      for (PseudoDBObject o : getList())
      {
        Mitglied m = (Mitglied) o.getAttribute(MITGLIED);
        Mitglied zahler = m.getZahler();
        if ((Boolean) part.getMitgliedzahltSelbst().getValue())
        {
          zahler = m;
        }
        Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
        if ((weg == null
            && zahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
            || (weg != null && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT))
        {
          if (global)
          {
            checkGlobal(bugs);
            checkGlaeubigerId(bugs);
            checkFaelligkeit((Date) getFaelligkeit().getValue(), bugs);
            global = false;
          }
          checkMitgliedKontodaten(zahler, bugs);
          if (!(Boolean) getSEPACheck().getValue())
          {
            checkSEPA(zahler, bugs);
          }
        }
      }

      if (bugs.isEmpty())
      {
        bugs.add(new Bug(null, KEINFEHLER, Bug.HINT));
      }
    }
    catch (Exception ex)
    {
      bugs.add(new Bug(null, ex.getMessage(), Bug.ERROR));
    }

    return bugs;
  }

  @Override
  protected String checkInput()
  {
    try
    {
      if (getFaelligkeit().getValue() == null)
      {
        return ("Bitte Fälligkeit eingeben");
      }
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if (getFaelligkeit().getValue() != null && weg != null
          && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT)
      {
        if (((Date) getFaelligkeit().getValue()).before(new Date()))
        {
          return ("Fälligkeit muss bei Lastschriften in der Zukunft liegen!");
        }
      }

      if (part.getBuchungstext().getValue() == null
          || ((String) part.getBuchungstext().getValue()).isEmpty())
      {
        return ("Bitte Buchungstext eingeben");
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN)
          && (boolean) getRechnung().getValue())
      {
        if (getRechnungsformular().getValue() == null)
        {
          return ("Bitte Rechnungsformular auswählen");
        }
        if (getRechnungsdatum().getValue() == null)
        {
          return ("Bitte Rechnungsdatum auswählen");
        }
      }
    }
    catch (RemoteException re)
    {
      return ("Fehler beim Auswerten der Eingabe!");
    }
    return null;
  }

  @Override
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    return new AbrechnungSEPAParam(this, sepaVersion);
  }
}
