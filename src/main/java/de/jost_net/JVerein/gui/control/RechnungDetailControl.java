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
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.PersonenartInput;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.SollbuchungPositionListPart;
import de.jost_net.JVerein.gui.view.MahnungMailView;
import de.jost_net.JVerein.gui.view.RechnungMailView;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class RechnungDetailControl extends VorZurueckControl implements Savable
{

  private Rechnung rechnung;

  private DateInput rechnungsDatum;

  private TextInput mitglied;

  private BigDecimalInput betrag;

  private JVereinTablePart buchungList;

  private TextInput anrede;

  private TextInput titel;

  private TextInput name;

  private TextInput vorname;

  private TextInput strasse;

  private TextInput adressierungszusatz;

  private TextInput ort;

  private TextInput plz;

  private TextInput staat;

  private GeschlechtInput geschlecht;

  private FormularInput rechnungFormular;

  private TextInput nummer;

  private IBANInput iban;

  private BICInput bic;

  private TextInput mandatid;

  private DateInput mandatdatum;

  private TextInput personenart;

  private TextInput zahlungsweg;

  private TextInput leitwegID;

  private TextAreaInput kommentar;

  private DateInput versanddatum;

  private TextInput zahler;

  private TextInput rechnungstext;

  private BigDecimalInput erstattungsbetrag;

  public enum TYP
  {
    RECHNUNG,
    MAHNUNG
  }

  public RechnungDetailControl(AbstractView view)
  {
    super(view);
  }

  private Rechnung getRechnung()
  {
    if (rechnung != null)
    {
      return rechnung;
    }
    rechnung = (Rechnung) getCurrentObject();
    return rechnung;
  }

  public DateInput getRechnungsdatum() throws RemoteException
  {
    if (rechnungsDatum != null)
    {
      return rechnungsDatum;
    }

    Date d = getRechnung().getDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    rechnungsDatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    rechnungsDatum.setName("Rechnungsdatum");
    rechnungsDatum.disable();
    return rechnungsDatum;
  }

  public TextInput getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    mitglied = new TextInput(getRechnung().getMitglied().getName() + ", "
        + getRechnung().getMitglied().getVorname());
    mitglied.setName("Mitglied");
    mitglied.disable();
    return mitglied;
  }

  public FormularInput getRechnungFormular() throws RemoteException
  {
    if (rechnungFormular != null)
    {
      return rechnungFormular;
    }

    rechnungFormular = new FormularInput(FormularArt.RECHNUNG,
        getRechnung().getFormular().getID());
    rechnungFormular.setName("Formular");
    return rechnungFormular;
  }

  public TextInput getNummer() throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }

    nummer = new TextInput(getRechnung().getNummer());
    nummer.setName("Rechnungsnummer");
    nummer.disable();

    return nummer;
  }

  public BigDecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }

    betrag = new BigDecimalInput(getRechnung().getBetrag(),
        Einstellungen.BIGDECIMALFORMAT);
    betrag.setName("Betrag");
    betrag.disable();
    return betrag;
  }

  public TextInput getAnrede() throws RemoteException
  {
    if (anrede != null)
    {
      return anrede;
    }

    anrede = new TextInput(getRechnung().getAnrede());
    anrede.setName("Anrede");
    anrede.disable();
    return anrede;
  }

  public TextInput getTitel() throws RemoteException
  {
    if (titel != null)
    {
      return titel;
    }

    titel = new TextInput(getRechnung().getTitel());
    titel.setName("Titel");
    titel.disable();
    return titel;
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }

    name = new TextInput(getRechnung().getName());
    name.setName("Name");
    name.disable();
    return name;
  }

  public TextInput getVorname() throws RemoteException
  {
    if (vorname != null)
    {
      return vorname;
    }

    vorname = new TextInput(getRechnung().getVorname());
    vorname.setName("Vorname");
    vorname.disable();
    return vorname;
  }

  public TextInput getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }

    strasse = new TextInput(getRechnung().getStrasse());
    strasse.setName("Strasse");
    strasse.disable();
    return strasse;
  }

  public TextInput getAdressierungszusatz() throws RemoteException
  {
    if (adressierungszusatz != null)
    {
      return adressierungszusatz;
    }

    adressierungszusatz = new TextInput(getRechnung().getAdressierungszusatz());
    adressierungszusatz.setName("Adressierungszusatz");
    adressierungszusatz.disable();
    return adressierungszusatz;
  }

  public TextInput getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }

    ort = new TextInput(getRechnung().getOrt());
    ort.setName("Ort");
    ort.disable();
    return ort;
  }

  public TextInput getPlz() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }

    plz = new TextInput(getRechnung().getPlz());
    plz.setName("Plz");
    plz.disable();
    return plz;
  }

  public TextInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }

    staat = new TextInput(getRechnung().getStaat());
    staat.setName("Staat");
    staat.disable();
    return staat;
  }

  public GeschlechtInput getGeschlecht() throws RemoteException
  {
    if (geschlecht != null)
    {
      return geschlecht;
    }

    geschlecht = new GeschlechtInput(getRechnung().getGeschlecht());
    geschlecht.setName("Geschlecht");
    geschlecht.disable();
    return geschlecht;
  }

  public TextInput getPersonenart() throws RemoteException
  {
    if (personenart != null)
    {
      return personenart;
    }

    personenart = new TextInput(
        getRechnung().getPersonenart().equalsIgnoreCase("n")
            ? PersonenartInput.NATUERLICHE_PERSON
            : PersonenartInput.JURISTISCHE_PERSON);
    personenart.setName("Personenart");
    personenart.disable();
    return personenart;
  }

  public DateInput getMandatdatum() throws RemoteException
  {
    if (mandatdatum != null)
    {
      return mandatdatum;
    }

    Date d = getRechnung().getMandatDatum();

    mandatdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    mandatdatum.setName("Mandatdatum");
    mandatdatum.disable();
    return mandatdatum;
  }

  public TextInput getMandatid() throws RemoteException
  {
    if (mandatid != null)
    {
      return mandatid;
    }

    mandatid = new TextInput(getRechnung().getMandatID());
    mandatid.setName("Mandatid");
    mandatid.disable();
    return mandatid;
  }

  public BICInput getBic() throws RemoteException
  {
    if (bic != null)
    {
      return bic;
    }

    bic = new BICInput(getRechnung().getBIC());
    bic.setName("BIC");
    bic.disable();
    return bic;
  }

  public IBANInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }

    iban = new IBANInput(new IBANFormatter().format(getRechnung().getIBAN()),
        getBic());
    iban.setName("IBAN");
    iban.disable();
    return iban;
  }

  public TextInput getLeitwegID() throws RemoteException
  {
    if (leitwegID != null)
    {
      return leitwegID;
    }
    leitwegID = new TextInput(getRechnung().getLeitwegID());
    leitwegID.setName("LeitwegID");
    leitwegID.disable();
    return leitwegID;
  }

  public DateInput getVersanddatum() throws RemoteException
  {
    if (versanddatum != null)
    {
      return versanddatum;
    }
    versanddatum = new DateInput(getRechnung().getVersanddatum());
    versanddatum.setName("Versand Datum");
    versanddatum.disable();
    return versanddatum;
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    buchungList = new SollbuchungPositionListPart(
        getRechnung().getSollbuchungPositionList(), null);
    return buchungList;
  }

  public TextInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }

    zahlungsweg = new TextInput(getRechnung().getZahlungsweg().getText());
    zahlungsweg.setName("Zahlungsweg");
    zahlungsweg.disable();
    return zahlungsweg;
  }

  public TextAreaInput getKommentar() throws RemoteException
  {
    if (kommentar != null)
    {
      return kommentar;
    }

    kommentar = new TextAreaInput(getRechnung().getKommentar(), 1024);
    kommentar.setName("Kommentar");
    kommentar.setHeight(50);
    return kommentar;
  }

  public TextInput getZahler() throws RemoteException
  {
    if (zahler != null)
    {
      return zahler;
    }

    zahler = new TextInput(getRechnung().getZahler().getName() + ", "
        + getRechnung().getZahler().getVorname());
    zahler.setName("Zahler");
    zahler.disable();
    return zahler;
  }

  public TextInput getRechnungstext() throws RemoteException
  {
    if (rechnungstext != null)
    {
      return rechnungstext;
    }
    rechnungstext = new TextInput(getRechnung().getRechnungstext());
    rechnungstext.setName("Rechnungstext");
    rechnungstext.disable();
    return rechnungstext;
  }

  public BigDecimalInput getErstattungsbetrag() throws RemoteException
  {
    if (erstattungsbetrag != null)
    {
      return erstattungsbetrag;
    }

    erstattungsbetrag = new BigDecimalInput(
        getRechnung().getErstattungsbetrag(), Einstellungen.BIGDECIMALFORMAT);
    erstattungsbetrag.setName("Erstattungsbetrag");
    erstattungsbetrag.disable();
    return erstattungsbetrag;
  }

  public ButtonRtoL getRechnungDruckUndMailButton()
  {
    return new ButtonRtoL("Druck und Mail",
        context -> GUI.startView(RechnungMailView.class,
            new Rechnung[] { getRechnung() }),
        getRechnung(), false, "document-print.png");
  }

  public ButtonRtoL getMahnungDruckUndMailButton()
  {
    return new ButtonRtoL("Mahnung Druck und Mail",
        context -> GUI.startView(MahnungMailView.class,
            new Rechnung[] { getRechnung() }),
        getRechnung(), false, "document-print.png");
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Rechnung re = getRechnung();
    re.setFormular((Formular) getRechnungFormular().getValue());
    re.setKommentar((String) getKommentar().getValue());
    re.setVersanddatum((Date) getVersanddatum().getValue());
    return re;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Rechnung";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
