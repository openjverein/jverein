package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.server.Bug;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ForderungControl extends AbstractAbrechnungControl
{

  private Mitglied[] mitglieder;

  private CheckboxInput vorlageSpeichernInput;

  private boolean einstellungRechnungAnzeigen = false;

  private boolean einstellungBuchungsklasseInBuchung = false;

  private boolean einstellungSteuerInBuchung = false;

  private Zusatzbetrag zusatzb;

  private final ZusatzbetragPart part;

  public ForderungControl(Mitglied[] mitglieder) throws RemoteException
  {
    super();

    einstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    einstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    einstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    zusatzb = getZusatzbetrag();
    part = new ZusatzbetragPart(zusatzb, false);
    this.mitglieder = mitglieder;
  }

  public ZusatzbetragPart getPart()
  {
    return part;
  }

  public CheckboxInput getVorlageSpeichernInput()
  {
    if (vorlageSpeichernInput != null)
    {
      return vorlageSpeichernInput;
    }
    vorlageSpeichernInput = new CheckboxInput(false);
    return vorlageSpeichernInput;
  }

  public Button getVorlagenButton() throws RemoteException
  {
    Button b = new Button("Vorlagen",
        new ZusatzbetragVorlageAuswahlAction(part), null, false,
        "view-refresh.png");
    return b;
  }

  @Override
  public List<Bug> getBugs()
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    boolean global = true;
    try
    {
      // Prüfen ob das Verrechnungskonto gesetzt ist. Das wird auch beim
      // Abrechnungslauf am Anfang geholt.
      checkVerrechnungskonto(bugs);

      for (Mitglied m : mitglieder)
      {
        Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
        if ((weg == null && m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
            || (weg != null && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT))
        {
          if (global)
          {
            checkGlobal(bugs);
            checkFaelligkeit((Date) getFaelligkeit().getValue(), bugs);
            global = false;
          }
          checkMitgliedKontodaten(m, bugs);
          if (!(Boolean) getSEPACheck().getValue())
          {
            checkSEPA(m, bugs);
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

  private Zusatzbetrag getZusatzbetrag() throws RemoteException
  {
    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    zusatzb.setStartdatum(new Date());
    zusatzb.setBuchungstext(settings.getString("buchungstext", ""));
    zusatzb.setBetrag(settings.getDouble("betrag", 0.0));

    String buchungsart = settings.getString("buchungsart", "");
    if (buchungsart.length() > 0)
    {
      try
      {
        Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
            .createObject(Buchungsart.class, buchungsart);
        zusatzb.setBuchungsart(ba);
      }
      catch (ObjectNotFoundException e)
      {
        //
      }
    }

    if (einstellungBuchungsklasseInBuchung)
    {
      String buchungsklasse = settings.getString("buchungsklasse", "");
      if (buchungsklasse.length() > 0)
      {
        try
        {
          Buchungsklasse bk = (Buchungsklasse) Einstellungen.getDBService()
              .createObject(Buchungsklasse.class, buchungsklasse);
          zusatzb.setBuchungsklasseId(Long.valueOf(bk.getID()));
        }
        catch (ObjectNotFoundException e)
        {
          //
        }
      }
    }

    if (einstellungSteuerInBuchung)
    {
      String steuer = settings.getString("steuer", "");
      if (steuer.length() > 0)
      {
        try
        {
          Steuer st = (Steuer) Einstellungen.getDBService()
              .createObject(Steuer.class, steuer);
          zusatzb.setSteuer(st);
        }
        catch (ObjectNotFoundException e)
        {
          //
        }
      }
    }

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
    for (Mitglied mit : mitglieder)
    {
      Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
          .createObject(Zusatzbetrag.class, null);
      zb.setBetrag((Double) part.getBetrag().getValue());
      zb.setBuchungstext((String) part.getBuchungstext().getValue());
      zb.setFaelligkeit((Date) getFaelligkeit().getValue());
      zb.setIntervall(IntervallZusatzzahlung.KEIN);
      zb.setMitglied(Integer.parseInt(mit.getID()));
      zb.setStartdatum((Date) getFaelligkeit().getValue());
      zb.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
      zb.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
      if (part.isSteuerActive())
      {
        zb.setSteuer((Steuer) part.getSteuer().getValue());
      }
      zb.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zb.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      list.add(zb);
    }
    if ((Boolean) getVorlageSpeichernInput().getValue())
    {
      ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
          .getDBService().createObject(ZusatzbetragVorlage.class, null);
      zv.setIntervall(IntervallZusatzzahlung.KEIN);
      zv.setBuchungstext((String) part.getBuchungstext().getValue());
      zv.setBetrag((Double) part.getBetrag().getValue());
      zv.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
      zv.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
      if (part.isSteuerActive())
      {
        zv.setSteuer((Steuer) part.getSteuer().getValue());
      }
      zv.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zv.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      zv.store();
    }
    return list;
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
        return ("Bitte Zahlungsgrund eingeben");
      }
      if (part.getBetrag().getValue() == null
          || ((Double) part.getBetrag().getValue()) < 0.005d)
      {
        return ("Bitte positiven Betrag eingeben");
      }

      if (einstellungSteuerInBuchung)
      {
        Buchungsart buchungsart = (Buchungsart) part.getBuchungsart()
            .getValue();
        Steuer steuer = (Steuer) part.getSteuer().getValue();
        if (steuer != null && buchungsart != null)
        {
          if (buchungsart.getSpende() || buchungsart.getAbschreibung())
          {
            return ("Bei Spenden und Abschreibungen ist keine Steuer möglich.");
          }
          if (steuer.getBuchungsart().getArt() != buchungsart.getArt())
          {
            switch (buchungsart.getArt())
            {
              case ArtBuchungsart.AUSGABE:
                return ("Umsatzsteuer statt Vorsteuer gewählt!");
              case ArtBuchungsart.EINNAHME:
                return ("Vorsteuer statt Umsatzsteuer gewählt!");
              // Umbuchung ist bei Anlagebuchungen möglich,
              // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf)
              // möglich
              case ArtBuchungsart.UMBUCHUNG:
                break;
            }
          }
        }
      }

      if (einstellungRechnungAnzeigen && (boolean) getRechnung().getValue())
      {
        if (getRechnungFormular().getValue() == null)
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
  protected void saveSettings()
  {
    super.saveSettings();
    try
    {
      settings.setAttribute("buchungstext",
          (String) part.getBuchungstext().getValue());
      settings.setAttribute("betrag", (Double) part.getBetrag().getValue());
      Buchungsart tmpba = (Buchungsart) part.getBuchungsart().getValue();
      if (tmpba != null)
      {
        settings.setAttribute("buchungsart", tmpba.getID());
      }
      else
      {
        settings.setAttribute("buchungsart", "");
      }
      if (einstellungBuchungsklasseInBuchung)
      {
        Buchungsklasse tmpbk = (Buchungsklasse) part.getBuchungsklasse()
            .getValue();
        if (tmpbk != null)
        {
          settings.setAttribute("buchungsklasse", tmpbk.getID());
        }
        else
        {
          settings.setAttribute("buchungsklasse", "");
        }
      }
      if (einstellungSteuerInBuchung)
      {
        Steuer tmpst = (Steuer) part.getSteuer().getValue();
        if (tmpst != null)
        {
          settings.setAttribute("steuer", tmpst.getID());
        }
        else
        {
          settings.setAttribute("steuer", "");
        }
      }
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
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    return new AbrechnungSEPAParam(this, sepaVersion);
  }
}
