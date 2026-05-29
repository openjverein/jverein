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
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.EmailInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.PersonenartInput;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.gui.view.PreNotificationMailView;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.MitgliedsArt;
import de.jost_net.JVerein.keys.SuchVersand;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LastschriftControl extends FilterControl implements Savable
{

  private TextInput personenart;

  private TextInput mitglied;

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

  private BetragSummaryTablePart lastschriftList;

  private DateInput versanddatum;

  public LastschriftControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (lastschriftList != null)
    {
      return lastschriftList;
    }
    lastschriftList = new BetragSummaryTablePart(getLastschriften(), null);
    lastschriftList.addColumn("Nr", "id-int");
    lastschriftList.addColumn("Versanddatum", "versanddatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("Abrechnungslauf", "abrechnungslauf");
    lastschriftList.addColumn("Name", "name");
    lastschriftList.addColumn("Vorname", "vorname");
    lastschriftList.addColumn("Email", "email");
    lastschriftList.addColumn("Zweck", "verwendungszweck");
    lastschriftList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    lastschriftList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("IBAN", "iban", new IBANFormatter());
    lastschriftList.addColumn("Mandat", "mandatid");
    lastschriftList.addColumn("Mandatdatum", "mandatdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.setContextMenu(new LastschriftMenu(lastschriftList));
    lastschriftList.setMulti(true);
    lastschriftList.setAction(
        new EditAction(LastschriftDetailView.class, lastschriftList));
    VorZurueckControl.setObjektListe(null, null);
    return lastschriftList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
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

  private DBIterator<Lastschrift> getLastschriften()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Lastschrift> lastschriften = service
        .createList(Lastschrift.class);
    lastschriften.join("abrechnungslauf");
    lastschriften.addFilter("abrechnungslauf.id = lastschrift.abrechnungslauf");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case MITGLIEDART:
          if (((MitgliedsArt) value).equals(MitgliedsArt.KURSTEILNEHMER))
          {
            lastschriften.addFilter("(lastschrift.kursteilnehmer IS NOT NULL)");
          }
          else
          {
            lastschriften.join("mitglied");
            lastschriften.addFilter("mitglied.id = lastschrift.mitglied");
            if (((MitgliedsArt) value).equals(MitgliedsArt.MITGLIED))
            {
              lastschriften.addFilter(
                  Mitglied.MITGLIEDSTYP + " = " + Mitgliedstyp.MITGLIED);
            }
            else if (((MitgliedsArt) value).equals(MitgliedsArt.NICHT_MITGLIED))
            {
              lastschriften.addFilter(
                  Mitglied.MITGLIEDSTYP + " > " + Mitgliedstyp.MITGLIED);
            }
          }
          break;
        case NAME:
          lastschriften.addFilter("(lower(lastschrift.name) like ?)",
              value.toString() + "%");
          break;
        case ZWECK:
          lastschriften.addFilter("(lower(verwendungszweck) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_FAELLIGKEI_VON:
          lastschriften.addFilter("faelligkeit >= ?", value);
          break;
        case DATUM_FAELLIGKEI_BIS:
          lastschriften.addFilter("faelligkeit <= ?", value);
          break;
        case ABRECHNUNGSLAUF_AB:
          lastschriften.addFilter("abrechnungslauf >= ?", value);
          break;
        case VERSAND:
          switch ((SuchVersand) value)
          {
            case VERSAND:
              lastschriften.addFilter("versanddatum IS NOT NULL");
              break;
            case NICHT_VERSAND:
              lastschriften.addFilter("versanddatum IS NULL");
              break;
          }
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }

    lastschriften.setOrder("ORDER BY name");

    return lastschriften;
  }

  private Lastschrift getLastschrift()
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
    else if (getLastschrift().getMitglied() != null)
    {
      if (getLastschrift().getMitglied().getMitgliedstyp().getID()
          .equals(Mitgliedstyp.MITGLIED))
      {
        text = "Mitglied";
      }
      else
      {
        text = "Nicht-Mitglied";
      }
    }
    mitgliedstyp = new TextInput(text, 15);
    mitgliedstyp.setName("Mitgliedstyp");
    mitgliedstyp.setEnabled(false);
    return mitgliedstyp;
  }

  public TextInput getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    String text = "";
    if (getLastschrift().getMitglied() != null)
    {
      text = Adressaufbereitung.getNameVorname(getLastschrift().getMitglied());
    }
    mitglied = new TextInput(text);
    mitglied.setName("Mitglied");
    mitglied.setEnabled(false);
    return mitglied;
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
    bic = new BICInput(getLastschrift().getBic());
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
    iban = new IBANInput(new IBANFormatter().format(getLastschrift().getIban()),
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

  public DateInput getVersanddatum() throws RemoteException
  {
    if (versanddatum != null)
    {
      return versanddatum;
    }
    versanddatum = new DateInput(getLastschrift().getVersanddatum());
    return versanddatum;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Lastschrift la = getLastschrift();
    la.setVersanddatum((Date) getVersanddatum().getValue());
    return la;
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

  public ButtonRtoL getDruckUndMailButton()
  {
    ButtonRtoL b = new ButtonRtoL("Druck und Mail", c -> {
      Lastschrift la = getLastschrift();
      GUI.startView(PreNotificationMailView.class, new Lastschrift[] { la });
    }, getLastschrift(), false, "document-print.png");
    return b;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LASTSCHRIFTEN_DATEINAME, this);
  }
}
