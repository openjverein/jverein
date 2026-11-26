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

import java.io.File;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.ZahlerMap;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.BelegTyp;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegRechnung;
import de.jost_net.JVerein.rmi.Beleggruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungImpl extends AbstractBelegDBObjectImpl
    implements Rechnung, UnreadCounter
{

  private static final long serialVersionUID = -286067581211521888L;

  private BigDecimal ist;

  public RechnungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    if (!forcedDelete)
    {
      try
      {
        if (getVersanddatum() != null)
        {
          throw new ApplicationException(
              "Rechnung kann nicht gelöscht werden. Sie wurde bereits versendet!");
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        String msg = "Rechnung kann nicht gelöscht werden. Siehe system log";
        throw new ApplicationException(msg);
      }
    }
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public void setMitglied(Integer mitglied) throws RemoteException
  {
    setAttribute("mitglied", mitglied);
  }

  @Override
  public BigDecimal getBetrag() throws RemoteException
  {
    return (BigDecimal) getAttribute("betrag");
  }

  @Override
  public void setBetrag(BigDecimal betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
  }

  @Override
  protected String getTableName()
  {
    return "rechnung";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void setFormular(Formular formular) throws RemoteException
  {
    setAttribute("formular", Long.valueOf(formular.getID()));
  }

  @Override
  public Formular getFormular() throws RemoteException
  {
    return (Formular) getAttribute("formular");
  }

  @Override
  public void setDatum(Date date) throws RemoteException
  {
    setAttribute("datum", date);
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  @Override
  public void setAnrede(String anrede) throws RemoteException
  {
    setAttribute("anrede", anrede);
  }

  @Override
  public String getAnrede() throws RemoteException
  {
    return (String) getAttribute("anrede");
  }

  @Override
  public String getTitel() throws RemoteException
  {
    return (String) getAttribute("titel");
  }

  @Override
  public void setTitel(String titel) throws RemoteException
  {
    setAttribute("titel", titel);
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public String getVorname() throws RemoteException
  {
    return (String) getAttribute("vorname");
  }

  @Override
  public void setVorname(String vorname) throws RemoteException
  {
    setAttribute("vorname", vorname);
  }

  @Override
  public String getStrasse() throws RemoteException
  {
    return (String) getAttribute("strasse");
  }

  @Override
  public void setStrasse(String strasse) throws RemoteException
  {
    setAttribute("strasse", strasse);
  }

  @Override
  public String getAdressierungszusatz() throws RemoteException
  {
    return (String) getAttribute("adressierungszusatz");
  }

  @Override
  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException
  {
    setAttribute("adressierungszusatz", adressierungszusatz);
  }

  @Override
  public String getPlz() throws RemoteException
  {
    return (String) getAttribute("plz");
  }

  @Override
  public void setPlz(String plz) throws RemoteException
  {
    setAttribute("plz", plz);
  }

  @Override
  public String getOrt() throws RemoteException
  {
    return (String) getAttribute("ort");
  }

  @Override
  public void setOrt(String ort) throws RemoteException
  {
    setAttribute("ort", ort);
  }

  @Override
  public String getStaat() throws RemoteException
  {
    return Staat.getStaat(getStaatCode());
  }

  @Override
  public String getStaatCode() throws RemoteException
  {
    String code = (String) getAttribute("staat");
    return Staat.getStaatCode(code);
  }

  @Override
  public void setStaat(String staat) throws RemoteException
  {
    setAttribute("staat", staat);
  }

  @Override
  public String getGeschlecht() throws RemoteException
  {
    return (String) getAttribute("geschlecht");
  }

  @Override
  public void setGeschlecht(String geschlecht) throws RemoteException
  {
    setAttribute("geschlecht", geschlecht);
  }

  @Override
  public BigDecimal getIstSumme() throws RemoteException
  {
    if (ist != null)
    {
      return ist;
    }
    ist = BigDecimal.ZERO;
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>("buchung");
    it.addColumn("sum(buchung.betrag) as summe");
    it.join(Sollbuchung.TABLE_NAME,
        Sollbuchung.TABLE_NAME_ID + " = " + Buchung.SOLLBUCHUNG);
    it.addFilter(Sollbuchung.T_RECHNUNG + " = ?", getID());
    if (it.hasNext())
    {
      PseudoDBObject o = it.next();
      if (o != null && o.getAttribute("summe") != null)
      {
        ist = o.getBigDecimal("summe");
      }
    }
    return ist;
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    switch (fieldName)
    {
      case "ist":
        return getIstSumme();
      case "differenz":
        if (getIstSumme() != null)
        {
          return getBetrag().subtract(getIstSumme());
        }
        else
        {
          return getBetrag();
        }
      default:
        return super.getAttribute(fieldName);
    }
  }

  @Override
  public String getPersonenart() throws RemoteException
  {
    return (String) getAttribute("personenart");
  }

  @Override
  public void setPersonenart(String personenart) throws RemoteException
  {
    setAttribute("personenart", personenart);
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("formular".equals(field))
    {
      return Formular.class;
    }
    if ("mitglied".equals(field))
    {
      return Mitglied.class;
    }
    if ("zahler".equals(field))
    {
      return Mitglied.class;
    }
    return null;
  }

  @Override
  public ArrayList<Sollbuchung> getSollbuchungList() throws RemoteException
  {
    ArrayList<Sollbuchung> sbs = new ArrayList<>();
    DBIterator<Sollbuchung> sollbIt = Einstellungen.getDBService()
        .createList(Sollbuchung.class);
    sollbIt.addFilter(Sollbuchung.RECHNUNG + " = ?", getID());
    while (sollbIt.hasNext())
    {
      sbs.add((Sollbuchung) sollbIt.next());
    }
    return sbs;
  }

  @Override
  public ArrayList<SollbuchungPosition> getSollbuchungPositionList()
      throws RemoteException
  {
    ArrayList<SollbuchungPosition> sps = new ArrayList<>();
    DBIterator<SollbuchungPosition> it = Einstellungen.getDBService()
        .createList(SollbuchungPosition.class);
    it.join(Sollbuchung.TABLE_NAME);
    it.addFilter(
        Sollbuchung.TABLE_NAME_ID + " = sollbuchungposition.sollbuchung");
    it.addFilter(Sollbuchung.T_RECHNUNG + " = ?", getID());
    it.setOrder("ORDER BY datum");
    while (it.hasNext())
    {
      sps.add((SollbuchungPosition) it.next());
    }
    return sps;
  }

  @Override
  public void fill(Sollbuchung sollb)
      throws RemoteException, ApplicationException
  {
    Mitglied mitglied = sollb.getZahler();
    if (mitglied == null)
    {
      throw new ApplicationException("Sollbuchung enthält keinen Zahler.");
    }
    // Es wir die ID des Mitglieds eingetragen, aber die Daten des Zahlers
    setMitglied(Integer.parseInt(sollb.getMitgliedId()));
    setPersonenart(mitglied.getPersonenart());
    setAnrede(mitglied.getAnrede());
    setTitel(mitglied.getTitel());
    setName(mitglied.getName());
    setVorname(mitglied.getVorname());
    setStrasse(mitglied.getStrasse());
    setAdressierungszusatz(mitglied.getAdressierungszusatz());
    setPlz(mitglied.getPlz());
    setOrt(mitglied.getOrt());
    setStaat(mitglied.getStaat());
    setGeschlecht(mitglied.getGeschlecht());

    if (!mitglied.getMandatDatum().equals(Einstellungen.NODATE))
    {
      setMandatDatum(mitglied.getMandatDatum());
    }
    setLeitwegID(mitglied.getLeitwegID());
    setMandatID(mitglied.getMandatID());
    setBIC(mitglied.getBic());
    setIBAN(mitglied.getIban());
    setZahlungsweg(sollb.getZahlungsweg());
    setBetrag(sollb.getBetrag());
    setKommentar("");
    setZahler(mitglied);
  }

  @Override
  public String getMandatID() throws RemoteException
  {
    return (String) getAttribute("mandatid");
  }

  @Override
  public void setMandatID(String id) throws RemoteException
  {
    setAttribute("mandatid", id);
  }

  @Override
  public Date getMandatDatum() throws RemoteException
  {
    return (Date) getAttribute("mandatdatum");
  }

  @Override
  public void setMandatDatum(Date datum) throws RemoteException
  {
    setAttribute("mandatdatum", datum);
  }

  @Override
  public String getBIC() throws RemoteException
  {
    return (String) getAttribute("bic");
  }

  @Override
  public void setBIC(String bic) throws RemoteException
  {
    setAttribute("bic", bic);
  }

  @Override
  public String getIBAN() throws RemoteException
  {
    return (String) getAttribute("iban");
  }

  @Override
  public void setIBAN(String iban) throws RemoteException
  {
    setAttribute("iban", iban);
  }

  @Override
  public Zahlungsweg getZahlungsweg() throws RemoteException
  {
    if (getAttribute("zahlungsweg") == null)
    {
      return null;
    }
    return new Zahlungsweg((Integer) getAttribute("zahlungsweg"));
  }

  @Override
  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException
  {
    setAttribute("zahlungsweg", zahlungsweg);
  }

  @Override
  public String getLeitwegID() throws RemoteException
  {
    return (String) getAttribute("leitwegid");
  }

  @Override
  public void setLeitwegID(String leitwegid) throws RemoteException
  {
    setAttribute("leitwegid", leitwegid);
  }

  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    setAttribute("kommentar", kommentar);
  }

  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) getAttribute("kommentar");
  }

  @Override
  public void setVersanddatum(Date datum) throws RemoteException
  {
    setAttribute("versanddatum", datum);
  }

  @Override
  public Date getVersanddatum() throws RemoteException
  {
    return (Date) getAttribute("versanddatum");
  }

  @Override
  public void setZahler(Mitglied zahler) throws RemoteException
  {
    setAttribute("zahler", zahler);
  }

  @Override
  public Mitglied getZahler() throws RemoteException
  {
    Object o = getAttribute("zahler");
    if (o == null)
    {
      return getMitglied();
    }
    return (Mitglied) o;
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "kommentar":
      case "titel":
      case "vorname":
      case "adressierungszusatz":
      case "strasse":
      case "plz":
      case "ort":
      case "iban":
      case "bic":
      case "leitwegid":
      case "mandatid":
      case "staat":
      case "anrede":
      case "rechnungstext":
        return "";
      default:
        return null;
    }
  }

  @Override
  public String getObjektName()
  {
    return "Rechnung";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Rechnungen";
  }

  @Override
  public int getUeberfaellig() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        getTableName());

    it.join(
        "(SELECT " + Sollbuchung.TABLE_NAME
            + ".rechnung AS re,sum(buchung.betrag) AS betrag FROM "
            + Sollbuchung.TABLE_NAME + " LEFT JOIN buchung ON "
            + Buchung.T_SOLLBUCHUNG + "=" + Sollbuchung.TABLE_NAME_ID
            + " GROUP BY " + Sollbuchung.TABLE_NAME + ".rechnung) AS ist",
        "ist.re = rechnung.id");
    it.addFilter(
        "abs(COALESCE(ist.betrag,0) - " + getTableName() + ".betrag) >= 0.01");
    it.addFilter(getTableName() + ".datum <= ?", new Date());
    it.addColumn("count(*) as sum");
    return it.next().getInteger("sum");
  }

  @Override
  public String getMenueID()
  {
    return "OpenJVerein.Mitglieder.Rechnungen";
  }

  // Für Gutschrift Support

  @Override
  public Mitglied getGutschriftZahler() throws RemoteException
  {
    return getZahler();
  }

  @Override
  public List<Buchung> getBuchungList() throws RemoteException
  {
    ArrayList<Buchung> buchungen = new ArrayList<>();
    ArrayList<Sollbuchung> sollbuchungen = getSollbuchungList();
    for (Sollbuchung sollb : sollbuchungen)
    {
      buchungen.addAll(sollb.getBuchungList());
    }
    return buchungen;
  }

  @Override
  public void setRechnungstext(String value) throws RemoteException
  {
    setAttribute("rechnungstext", value);
  }

  @Override
  public String getRechnungstext() throws RemoteException
  {
    return (String) getAttribute("rechnungstext");
  }

  @Override
  public void setErstattungsbetrag(BigDecimal betrag) throws RemoteException
  {
    setAttribute("erstattungsbetrag", betrag);
  }

  @Override
  public BigDecimal getErstattungsbetrag() throws RemoteException
  {
    return (BigDecimal) getAttribute("erstattungsbetrag");
  }

  @Override
  public Long getReferenzrechnungID() throws RemoteException
  {
    return (Long) getAttribute("refrechnung");
  }

  @Override
  public void setReferenzrechnungID(Long referenz) throws RemoteException
  {
    setAttribute("refrechnung", referenz);
  }

  @Override
  public String getEmail() throws RemoteException
  {
    // Wegen Interface IAdresse
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
  public void store() throws RemoteException, ApplicationException
  {
    if (isNewObject())
    {
      try
      {
        transactionBegin();

        // Speichern, damit es schon eine ID gibt
        super.store();

        // Rechnungsnummer erstellen
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map = new RechnungMap().getMap(this, map);
        if (getMitglied() != null)
        {
          map = new MitgliedMap().getMap(getMitglied(), map);
        }
        Integer nr = (Integer) Einstellungen
            .getEinstellung(Property.RECHNUNG_ZAHLER);
        map.put(RechnungVar.RECHNUNG_ZAEHLER.getName(),
            StringTool.lpad(nr.toString(),
                (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE),
                "0"));

        String nummer = VelocityTool.eval(map,
            (String) Einstellungen.getEinstellung(Property.RECHNUNGSNUMMER));

        if (nummer.length() > 50)
        {
          throw new ApplicationException(
              "Rechnungsnummer zu lang, maximal 50 Zeichen erlaubt!");
        }
        setNummer(nummer);

        // Prüfen, ob es schon eine Rechnung mit dieser Nummer gibt
        DBIterator<?> it = getList();
        it.addFilter("nummer = ?", nummer);
        if (it.hasNext())
        {
          throw new ApplicationException(
              "Rechnung mit dieser Nummer existiert bereits. Rechnungsnummer in Einstellungen korrigieren!");
        }

        super.store();

        Einstellungen.setEinstellung(Property.RECHNUNG_ZAHLER, nr + 1);

        transactionCommit();
      }
      catch (Exception e)
      {
        transactionRollback();
        throw e;
      }
    }
    else
    {
      super.store();
    }
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    DBIterator<Beleg> it = getBelegList();
    while (it.hasNext())
    {
      it.next().deleteForced();
    }
    super.delete();
  }

  public Beleg erzeugeBeleg(BelegTyp typ, Formular formular)
      throws RemoteException, ApplicationException
  {
    File file = null;
    Beleg doc = null;
    try
    {
      transactionBegin();

      Map<String, Object> map = new AllgemeineMap().getMap(null);
      map = new RechnungMap().getMap(this, map);
      if (getMitglied() != null)
      {
        map = new MitgliedMap().getMap(getMitglied(), map);
      }
      if (getZahler() != null)
      {
        map = new ZahlerMap().getMap(getZahler(), map);
      }

      // PDF Beleg erzeugen
      doc = Einstellungen.getDBService().createObject(Beleg.class, null);
      doc.setDatum(typ == BelegTyp.RECHNUNG ? getDatum() : new Date());
      doc.setBelegArt(getBelegArt());
      doc.setBelegTyp(typ);
      Beleggruppe gruppe = null;
      Integer gruppeid = (Integer) Einstellungen
          .getEinstellung(Property.BELEGGRUPPE_RECHNUNG);
      if (gruppeid != null)
      {
        gruppe = Einstellungen.getDBService().createObject(Beleggruppe.class,
            gruppeid.toString());
      }
      doc.setBelegGruppe(gruppe);

      String dateiname = null;
      switch (typ)
      {
        case RECHNUNG:
          dateiname = VorlageUtil
              .getName(VorlageTyp.RECHNUNG_MITGLIED_DATEINAME, this);
          formular = getFormular();
          break;
        case MAHNUNG:
          dateiname = VorlageUtil.getName(VorlageTyp.MAHNUNG_MITGLIED_DATEINAME,
              this);
          break;
        default:
          throw new ApplicationException(
              "Belegtyp für Rechnungen nicht erlaubt.");
      }
      if (formular == null)
      {
        throw new ApplicationException("Kein Formular angegeben.");
      }

      File tempDir = new File(System.getProperty("java.io.tmpdir"));
      file = new File(tempDir, dateiname + ".pdf");
      FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
          false);
      aufbereitung.writeForm(formular, map);
      formular.store();
      formular.setZaehlerToFormlink(getFormular().getZaehler());
      aufbereitung.closeFormular();
      aufbereitung.addZUGFeRD(this, false);

      doc.setBemerkung(dateiname);
      doc.setFile(file);
      doc.store();

      // Beleg zur Rechnung hinzufügen
      addBeleg(doc);
      // Wenn ein Mitglied gesetzt ist dann auch diesem zuweisen
      if (getMitglied() != null)
      {
        getMitglied().addBeleg(doc);
      }
      // Wenn ein anderer Zahler als das Mitglied gesetzt ist dann auch diesem
      // zuweisen
      if (getZahler() != null && (getMitglied() == null
          || !BeanUtil.equals(getZahler(), getMitglied())))
      {
        getZahler().addBeleg(doc);
      }

      transactionCommit();
    }
    catch (Exception e)
    {
      transactionRollback();
      throw new ApplicationException(e.getMessage());
    }
    finally
    {
      if (file != null)
      {
        file.delete();
      }
    }
    return doc;
  }

  @Override
  public BelegArt getBelegArt() throws RemoteException
  {
    return BelegArt.getByKey(
        (Integer) Einstellungen.getEinstellung(Property.BELEGART_RECHNUNG));
  }

  @Override
  protected Class<? extends AbstractBelegReferenz> getBelegReferenzClass()
  {
    return BelegRechnung.class;
  }

  @Override
  protected String getBelegReferenzTableName() throws RemoteException
  {
    return "belegrechnung";
  }

  @Override
  public void setBelegVersand(BelegTyp typ)
      throws RemoteException, ApplicationException
  {
    DBIterator<Beleg> it = getBelegList();
    if (typ == BelegTyp.RECHNUNG)
    {
      it.setOrder("ORDER BY id");
    }
    else
    {
      it.setOrder("ORDER BY id DESC");
    }
    it.setLimit(1);
    Beleg beleg = it.hasNext() ? it.next() : null;
    if (beleg != null && typ == beleg.getBelegTyp())
    {
      beleg.setVersand(new Timestamp(new Date().getTime()));
      beleg.updateForced();
    }
  }
}
