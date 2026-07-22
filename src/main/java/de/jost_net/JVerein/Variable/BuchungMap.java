package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.StringTool;

public class BuchungMap extends AbstractMap
{
  public Map<String, Object> getMap(Buchung bu, Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }

    if (bu == null)
    {
      return getDummyMap(map);
    }
    for (BuchungVar var : BuchungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ABRECHNUNGSLAUF:
          value = bu.getAbrechnungslauf() != null
              ? Datum.formatDate(bu.getAbrechnungslauf().getDatum())
              : "";
          break;
        case ART:
          value = StringTool.toNotNullString(bu.getArt());
          break;
        case AUSZUGSNUMMER:
          value = bu.getAuszugsnummer();
          break;
        case BETRAG:
          value = bu.getBetrag() != null
              ? Einstellungen.DECIMALFORMAT.format(bu.getBetrag())
              : "";
          break;
        case BETRAGNETTO:
          if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
          {
            value = bu.getNetto() != null
                ? Einstellungen.DECIMALFORMAT.format(bu.getNetto())
                : "";
          }
          else
          {
            continue;
          }
          break;
        case BLATTNUMMER:
          value = bu.getBlattnummer();
          break;
        case BUCHUNGSARBEZEICHNUNG:
          value = bu.getBuchungsart() != null
              ? bu.getBuchungsart().getBezeichnung()
              : "";
          break;
        case BUCHUNGSARTNUMMER:
          value = bu.getBuchungsart() != null ? bu.getBuchungsart().getNummer()
              : "";
          break;
        case BUCHUNGSKLASSEBEZEICHNUNG:
          if ((Boolean) Einstellungen
              .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
          {
            value = bu.getBuchungsklasse() != null
                ? bu.getBuchungsklasse().getBezeichnung()
                : "";
          }
          else
          {
            value = bu.getBuchungsart() != null
                && bu.getBuchungsart().getBuchungsklasse() != null
                    ? bu.getBuchungsart().getBuchungsklasse().getBezeichnung()
                    : "";
          }
          break;
        case BUCHUNGSKLASSENUMMER:
          if ((Boolean) Einstellungen
              .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
          {
            value = bu.getBuchungsklasse() != null
                ? bu.getBuchungsklasse().getNummer()
                : "";
          }
          else
          {
            value = bu.getBuchungsart() != null
                && bu.getBuchungsart().getBuchungsklasse() != null
                    ? bu.getBuchungsart().getBuchungsklasse().getNummer()
                    : "";
          }
          break;
        case DATUM:
          value = Datum.formatDate(bu.getDatum());
          break;
        case IBAN:
          value = bu.getIban();
          break;
        case ID:
          value = bu.getID();
          break;
        case JAHRESABSCHLUSS:
          value = bu.getJahresabschluss() != null
              ? Datum.formatDate(bu.getJahresabschluss().getBis())
              : "";
          break;
        case KOMMENTAR:
          value = StringTool.toNotNullString(bu.getKommentar());
          break;
        case KONTONUMMER:
          value = bu.getKonto() != null ? bu.getKonto().getNummer() : "";
          break;
        case MITGLIEDSKONTO:
          value = bu.getSollbuchung() != null
              && bu.getSollbuchung().getMitglied() != null
                  ? Adressaufbereitung
                      .getNameVorname(bu.getSollbuchung().getMitglied())
                  : "";
          break;
        case NAME:
          value = bu.getName();
          break;
        case PROJEKTBEZEICHNUNG:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = bu.getProjekt() != null ? bu.getProjekt().getBezeichnung()
                : "";
          }
          else
          {
            continue;
          }
          break;
        case PROJEKTNUMMER:
          if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
          {
            value = bu.getProjekt() != null ? bu.getProjektID() : "";
          }
          else
          {
            continue;
          }
          break;
        case SPENDENBESCHEINIGUNG:
          if ((Boolean) Einstellungen
              .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
          {
            value = bu.getSpendenbescheinigung() != null
                ? bu.getSpendenbescheinigung().getID()
                : "";
          }
          else
          {
            continue;
          }
          break;
        case STEUER:
          if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
          {
            if ((Boolean) Einstellungen
                .getEinstellung(Property.STEUERINBUCHUNG))
            {
              value = bu.getSteuer() == null ? 0d : bu.getSteuer().getSatz();
            }
            else
            {
              value = bu.getBuchungsart() == null
                  || bu.getBuchungsart().getSteuer() == null ? 0d
                      : bu.getBuchungsart().getSteuer().getSatz();
            }
          }
          else
          {
            continue;
          }
          break;
        case ZWECK1:
          value = StringTool.toNotNullString(bu.getZweck());
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }

  private Map<String, Object> getDummyMap(Map<String, Object> map)
  {
    for (BuchungVar var : BuchungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ABRECHNUNGSLAUF:
          value = Datum.formatDate(new Date());
          break;
        case ART:
          value = "Überweisung";
          break;
        case AUSZUGSNUMMER:
          value = "3";
          break;
        case BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(10.20);
          break;
        case BETRAGNETTO:
          value = Einstellungen.DECIMALFORMAT.format(10.20);
          break;
        case BLATTNUMMER:
          value = "1";
          break;
        case BUCHUNGSARBEZEICHNUNG:
          value = "Mitgliedsbeiträge";
          break;
        case BUCHUNGSARTNUMMER:
          value = "1000";
          break;
        case BUCHUNGSKLASSEBEZEICHNUNG:
          value = "Ideeller Bereich";
          break;
        case BUCHUNGSKLASSENUMMER:
          value = "1";
          break;
        case DATUM:
          value = Datum.formatDate(new Date());
          break;
        case IBAN:
          value = "DE89 3704 0044 0532 0130 00";
          break;
        case ID:
          value = "1234";
          break;
        case JAHRESABSCHLUSS:
          value = Datum.formatDate(new Date());
          break;
        case KOMMENTAR:
          value = "Kommentar";
          break;
        case KONTONUMMER:
          value = "123";
          break;
        case MITGLIEDSKONTO:
          value = "Max Mustermann";
          break;
        case NAME:
          value = "Mustermann";
          break;
        case PROJEKTBEZEICHNUNG:
          value = "Projekt";
          break;
        case PROJEKTNUMMER:
          value = "4";
          break;
        case SPENDENBESCHEINIGUNG:
          value = "55";
          break;
        case STEUER:
          value = "";
          break;
        case ZWECK1:
          value = "Verwendungszweck";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
