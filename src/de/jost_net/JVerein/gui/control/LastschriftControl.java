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
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.EmailInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.PersonenartInput;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.logging.Logger;

public class LastschriftControl extends FilterControl
{

  private TextInput personenart;

  private TextInput mitgliedstyp;

  private TextInput geschlecht;

  private TextInput anrede;

  private TextInput titel;

  private TextInput name;

  private TextInput vorname;

  private TextInput strasse;

  private TextInput adressierungszusatz;

  private TextInput plz;

  private TextInput ort;

  private TextInput staat;

  private EmailInput email;

  private DecimalInput betrag;

  private Input vzweck;

  private DateInput mandatdatum;

  private BICInput bic;

  private IBANInput iban;

  private Lastschrift lastschrift;

  private JVereinTablePart lastschriftList;

  public LastschriftControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Part getLastschriftList() throws RemoteException
  {
    if (lastschriftList != null)
    {
      return lastschriftList;
    }
    lastschriftList = new JVereinTablePart(getLastschriften(), null);
    lastschriftList.addColumn("Nr", "id-int");
    lastschriftList.addColumn("Abrechnungslauf", "abrechnungslauf");
    lastschriftList.addColumn("Name", "name");
    lastschriftList.addColumn("Vorname", "vorname");
    lastschriftList.addColumn("Email", "email");
    lastschriftList.addColumn("Zweck", "verwendungszweck");
    lastschriftList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    lastschriftList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("IBAN", "iban", new IbanFormatter());
    lastschriftList.addColumn("Mandat", "mandatid");
    lastschriftList.addColumn("Mandatdatum", "mandatdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.setRememberColWidths(true);
    lastschriftList.setContextMenu(new LastschriftMenu(lastschriftList));
    lastschriftList.setRememberOrder(true);
    lastschriftList.addFeature(new FeatureSummary());
    lastschriftList.setMulti(true);
    lastschriftList.setAction(
        new EditAction(LastschriftDetailView.class, lastschriftList));
    VorZurueckControl.setObjektListe(null, null);
    return lastschriftList;
  }

  @Override
  public void TabRefresh()
  {
    if (lastschriftList == null)
    {
      return;
    }
    try
    {
      lastschriftList.removeAll();
      DBIterator<Lastschrift> lastschriften = getLastschriften();
      while (lastschriften.hasNext())
      {
        lastschriftList.addItem(lastschriften.next());
      }
      lastschriftList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Lastschrift> getLastschriften() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Lastschrift> lastschriften = service
        .createList(Lastschrift.class);
    lastschriften.join("abrechnungslauf");
    lastschriften.addFilter("abrechnungslauf.id = lastschrift.abrechnungslauf");
    if (isMitgliedArtAktiv() && getMitgliedArt().getValue() != null)
    {
      String tmpArt = (String) getMitgliedArt().getValue();
      if (tmpArt.equalsIgnoreCase("Kursteilnehmer"))
      {
        lastschriften.addFilter("(lastschrift.kursteilnehmer IS NOT NULL)");
      }
      else
      {
        lastschriften.join("mitglied");
        lastschriften.addFilter("mitglied.id = lastschrift.mitglied");
        if (tmpArt.equalsIgnoreCase("Mitglied"))
        {
          lastschriften
              .addFilter(Mitglied.MITGLIEDSTYP + " = " + Mitgliedstyp.MITGLIED);
        }
        else if (tmpArt.equalsIgnoreCase("Nicht-Mitglied"))
        {
          lastschriften
              .addFilter(Mitglied.MITGLIEDSTYP + " > " + Mitgliedstyp.MITGLIED);
        }
      }
    }
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        lastschriften.addFilter("(lower(lastschrift.name) like ?)",
            new Object[] { tmpSuchname.toLowerCase() + "%" });
      }
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        lastschriften.addFilter("(lower(verwendungszweck) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%" });
      }
    }
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      lastschriften.addFilter("faelligkeit >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      lastschriften.addFilter("faelligkeit <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    if (isIntegerAuswAktiv() && getIntegerAusw().getValue() != null)
    {
      lastschriften.addFilter("abrechnungslauf >= ?",
          new Object[] { (Integer) getIntegerAusw().getValue() });
    }

    lastschriften.setOrder("ORDER BY name");

    return lastschriften;
  }

  public Lastschrift getLastschrift()
  {
    if (lastschrift != null)
    {
      return lastschrift;
    }
    lastschrift = (Lastschrift) getCurrentObject();
    return lastschrift;
  }

  public TextInput getPersonenart() throws RemoteException
  {
    if (personenart != null)
    {
      return personenart;
    }
    String art = getLastschrift().getPersonenart();
    String text = "";
    if (art.equalsIgnoreCase("n"))
    {
      text = PersonenartInput.NATUERLICHE_PERSON;
    }
    else if (art.equalsIgnoreCase("j"))
    {
      text = PersonenartInput.JURISTISCHE_PERSON;
    }
    personenart = new TextInput(text, 60);
    personenart.setName("Personenart");
    personenart.setEnabled(false);
    return personenart;
  }

  public TextInput getMitgliedstyp() throws RemoteException
  {
    if (mitgliedstyp != null)
    {
      return mitgliedstyp;
    }
    String text = "";
    if (getLastschrift().getKursteilnehmer() != null)
      text = "Kursteilnehmer";
    else if (getLastschrift().getMitglied().getMitgliedstyp()
        .getJVereinid() == Mitgliedstyp.MITGLIED)
      text = "Mitglied";
    else
      text = "Nicht-Mitglied";
    mitgliedstyp = new TextInput(text, 15);
    mitgliedstyp.setName("Mitgliedstyp");
    mitgliedstyp.setEnabled(false);
    return mitgliedstyp;
  }

  public TextInput getAnrede() throws RemoteException
  {
    if (anrede != null)
    {
      return anrede;
    }
    anrede = new TextInput(getLastschrift().getAnrede(), 10);
    anrede.setName("Anrede");
    anrede.setEnabled(false);
    return anrede;
  }

  public TextInput getTitel() throws RemoteException
  {
    if (titel != null)
    {
      return titel;
    }
    titel = new TextInput(getLastschrift().getTitel(), 40);
    titel.setName("Titel");
    titel.setEnabled(false);
    return titel;
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getLastschrift().getName(), 40);
    name.setName("Name");
    name.setEnabled(false);
    return name;
  }

  public TextInput getVorname() throws RemoteException
  {
    if (vorname != null)
    {
      return vorname;
    }
    vorname = new TextInput(getLastschrift().getVorname(), 40);
    vorname.setName("Vorname");
    vorname.setEnabled(false);
    return vorname;
  }

  public Input getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }
    strasse = new TextInput(getLastschrift().getStrasse(), 40);
    strasse.setName("Straße");
    strasse.setEnabled(false);
    return strasse;
  }

  public TextInput getAdressierungszusatz() throws RemoteException
  {
    if (adressierungszusatz != null)
    {
      return adressierungszusatz;
    }
    adressierungszusatz = new TextInput(
        getLastschrift().getAdressierungszusatz(), 40);
    adressierungszusatz.setName("Adressierungszusatz");
    adressierungszusatz.setEnabled(false);
    return adressierungszusatz;
  }

  public Input getPLZ() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }
    plz = new TextInput(getLastschrift().getPlz(), 10);
    plz.setName("PLZ");
    plz.setEnabled(false);
    return plz;
  }

  public Input getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }
    ort = new TextInput(getLastschrift().getOrt(), 40);
    ort.setName("Ort");
    ort.setEnabled(false);
    return ort;
  }

  public TextInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }
    staat = new TextInput(getLastschrift().getStaat(), 50);
    staat.setName("Staat");
    staat.setEnabled(false);
    return staat;
  }

  public EmailInput getEmail() throws RemoteException
  {
    if (email != null)
    {
      return email;
    }
    email = new EmailInput(getLastschrift().getEmail());
    email.setEnabled(false);
    return email;
  }

  public Input getVZweck() throws RemoteException
  {
    if (vzweck != null)
    {
      return vzweck;
    }
    vzweck = new TextInput(getLastschrift().getVerwendungszweck(), 140);
    vzweck.setName("Verwendungszweck");
    vzweck.setEnabled(false);
    return vzweck;
  }

  public DateInput getMandatDatum() throws RemoteException
  {
    if (mandatdatum != null)
    {
      return mandatdatum;
    }

    Date d = getLastschrift().getMandatDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    this.mandatdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.mandatdatum.setName("Datum des Mandats");
    mandatdatum.setEnabled(false);
    return mandatdatum;
  }

  public BICInput getBIC() throws RemoteException
  {
    if (bic != null)
    {
      return bic;
    }
    bic = new BICInput(getLastschrift().getBIC());
    bic.setName("BIC");
    bic.setEnabled(false);
    return bic;
  }

  public IBANInput getIBAN() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(HBCIProperties.formatIban(getLastschrift().getIBAN()),
        getBIC());
    iban.setName("IBAN");
    iban.setEnabled(false);
    return iban;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getLastschrift().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setName("Betrag");
    betrag.setEnabled(false);
    return betrag;
  }

  public TextInput getGeschlecht() throws RemoteException
  {
    if (geschlecht != null)
    {
      return geschlecht;
    }
    String text = "Geschlecht nicht konfiguriert";
    if (getLastschrift().getGeschlecht() != null)
    {
      String g = getLastschrift().getGeschlecht();
      if (g.equals(GeschlechtInput.MAENNLICH))
      {
        text = "Männlich";
      }
      else if (g.equals(GeschlechtInput.WEIBLICH))
      {
        text = "Weiblich";
      }
      else if (g.equals(GeschlechtInput.OHNEANGABE))
      {
        text = "Ohne Angabe";
      }
    }
    geschlecht = new TextInput(text, 40);
    geschlecht.setName("Geschlecht");
    geschlecht.setEnabled(false);
    return geschlecht;
  }
}
