package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
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
}
