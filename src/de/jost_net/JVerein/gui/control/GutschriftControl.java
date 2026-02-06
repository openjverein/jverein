package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

public class GutschriftControl
{
  private boolean isMitglied;

  private IGutschriftProvider[] providerArray;

  private GutschriftParam params;

  private SelectInput ausgabeInput;

  private DateInput datumInput;

  private TextInput zweckInput;

  private CheckboxInput fixerBetragAbrechnenInput;

  private DecimalInput fixerBetragInput;

  private AbstractInput buchungsartInput;

  private SelectInput buchungsklasseInput;

  private SelectInput steuerInput;

  private Settings settings = null;

  public GutschriftControl(IGutschriftProvider[] providerArray,
      boolean isMitglied)
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.isMitglied = isMitglied;
    this.providerArray = providerArray;
  }

  public IGutschriftProvider[] getProviderArray()
  {
    return providerArray;
  }

  public void setParams(GutschriftParam params)
  {
    this.params = params;
  }

  public GutschriftParam getParams()
  {
    return params;
  }

  // Ausgabe
  public SelectInput getAusgabeInput()
  {
    if (ausgabeInput != null)
    {
      return ausgabeInput;
    }
    UeberweisungAusgabe aus = UeberweisungAusgabe.getByKey(
        settings.getInt("ausgabe", UeberweisungAusgabe.HIBISCUS.getKey()));
    if (aus != UeberweisungAusgabe.SEPA_DATEI
        && aus != UeberweisungAusgabe.HIBISCUS)
    {
      aus = UeberweisungAusgabe.HIBISCUS;
    }
    ausgabeInput = new SelectInput(UeberweisungAusgabe.values(), aus);
    ausgabeInput.setMandatory(true);
    return ausgabeInput;
  }

  // Ausführungsdatum
  public DateInput getDatumInput()
  {
    if (datumInput != null)
    {
      return datumInput;
    }
    Date d = getDatum("datum");
    datumInput = new DateInput(d, new JVDateFormatTTMMJJJJ());
    datumInput.setMandatory(true);
    return datumInput;
  }

  public Date getDatum(String datum)
  {
    String tmp = settings.getString(datum, null);
    if (tmp != null)
    {
      try
      {
        return new JVDateFormatTTMMJJJJ().parse(tmp);
      }
      catch (ParseException e)
      {
        //
      }
    }
    return new Date();
  }

  // Verwendungszweck
  public TextInput getZweckInput()
  {
    if (zweckInput != null)
    {
      return zweckInput;
    }
    zweckInput = new TextInput(settings.getString("verwendungszweck", ""));
    zweckInput.setMandatory(true);
    return zweckInput;
  }

  // Fixen Betrag erstatten
  public CheckboxInput getFixerBetragAbrechnenInput()
  {
    if (fixerBetragAbrechnenInput != null)
    {
      return fixerBetragAbrechnenInput;
    }
    if (isMitglied)
    {
      fixerBetragAbrechnenInput = new CheckboxInput(true);
      fixerBetragAbrechnenInput.addListener(e -> {
        fixerBetragAbrechnenInput.setValue(true);
      });
    }
    else
    {
      fixerBetragAbrechnenInput = new CheckboxInput(
          settings.getBoolean("fixerBetragAbrechnen", false));
      fixerBetragAbrechnenInput.addListener(e -> {
        updateFixerBetragInput();
        updateBuchungsartInput();
        updateBuchungsklasseInput();
        updateSteuerInput();
      });
    }
    fixerBetragAbrechnenInput.setName(
        " *Sonst ganzen Betrag erstatten und bereits bezahlten Betrag überweisen.");
    return fixerBetragAbrechnenInput;
  }

  // Erstattungsbetrag
  public DecimalInput getFixerBetragInput()
  {
    if (fixerBetragInput != null)
    {
      return fixerBetragInput;
    }
    String tmp = settings.getString("fixerBetrag", "");
    if (tmp != null && !tmp.isEmpty())
    {
      fixerBetragInput = new DecimalInput(Double.parseDouble(tmp),
          Einstellungen.DECIMALFORMAT);
    }
    else
    {
      fixerBetragInput = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    updateFixerBetragInput();
    return fixerBetragInput;
  }

  public AbstractInput getBuchungsartInput() throws RemoteException
  {
    if (buchungsartInput != null)
    {
      return buchungsartInput;
    }
    Buchungsart ba = null;
    String buchungsart = settings.getString("buchungsart", "");
    if (buchungsart.length() > 0)
    {
      try
      {
        ba = (Buchungsart) Einstellungen.getDBService()
            .createObject(Buchungsart.class, buchungsart);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    buchungsartInput = new BuchungsartInput().getBuchungsartInput(
        buchungsartInput, ba, buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsartInput.addListener(e -> {
      try
      {
        if (buchungsklasseInput != null && buchungsartInput.getValue() != null)
        {
          buchungsklasseInput.setValue(
              ((Buchungsart) buchungsartInput.getValue()).getBuchungsklasse());
        }
        if (steuerInput != null && buchungsartInput.getValue() != null)
        {
          steuerInput.setValue(
              ((Buchungsart) buchungsartInput.getValue()).getSteuer());
        }
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    });
    updateBuchungsartInput();
    return buchungsartInput;
  }

  public SelectInput getBuchungsklasseInput() throws RemoteException
  {
    if (buchungsklasseInput != null)
    {
      return buchungsklasseInput;
    }
    Buchungsklasse bk = null;
    String buchungskl = settings.getString("buchungsklasse", "");
    if (buchungskl.length() > 0)
    {
      try
      {
        bk = (Buchungsklasse) Einstellungen.getDBService()
            .createObject(Buchungsklasse.class, buchungskl);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    buchungsklasseInput = new BuchungsklasseInput()
        .getBuchungsklasseInput(buchungsklasseInput, bk);
    updateBuchungsklasseInput();
    return buchungsklasseInput;
  }

  public SelectInput getSteuerInput() throws RemoteException
  {
    if (steuerInput != null)
    {
      return steuerInput;
    }
    Steuer st = null;
    String steuer = settings.getString("steuer", "");
    if (steuer.length() > 0)
    {
      try
      {
        st = (Steuer) Einstellungen.getDBService().createObject(Steuer.class,
            steuer);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    steuerInput = new SteuerInput(st);
    steuerInput.setPleaseChoose("Keine Steuer");
    updateSteuerInput();
    return steuerInput;
  }

  private void updateFixerBetragInput()
  {
    fixerBetragInput
        .setMandatory((boolean) fixerBetragAbrechnenInput.getValue());
    fixerBetragInput.setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
  }

  private void updateBuchungsartInput()
  {
    // Die Reihenfolge von mandatory und enabled ist abhängig von
    // enable/disable. Sonst klappt das mit der gelben Farbe nicht
    if ((boolean) fixerBetragAbrechnenInput.getValue())
    {
      buchungsartInput.setEnabled(true);
      buchungsartInput.setMandatory(true);
    }
    else
    {
      buchungsartInput.setMandatory(false);
      buchungsartInput.setEnabled(false);
    }
  }

  private void updateBuchungsklasseInput()
  {
    if (buchungsklasseInput != null)
    {
      // Die Reihenfolge von mandatory und enabled ist abhängig von
      // enable/disable. Sonst klappt das mit der gelben Farbe nicht
      if ((boolean) fixerBetragAbrechnenInput.getValue())
      {
        buchungsklasseInput.setEnabled(true);
        buchungsklasseInput.setMandatory(true);
      }
      else
      {
        buchungsklasseInput.setMandatory(false);
        buchungsklasseInput.setEnabled(false);
      }
    }
  }

  private void updateSteuerInput()
  {
    if (steuerInput != null)
    {
      steuerInput.setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
    }
  }

  public void saveSettings()
  {
    try
    {
      settings.setAttribute("ausgabe", params.getAusgabe().getKey());
      Date tmp = (Date) params.getDatum();
      if (tmp != null)
      {
        settings.setAttribute("datum", new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute("datum", "");
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        tmp = (Date) params.getRechnungsDatum();
        if (tmp != null)
        {
          settings.setAttribute("rechnungsdatum",
              new JVDateFormatTTMMJJJJ().format(tmp));
        }
        else
        {
          settings.setAttribute("rechnungsdatum", "");
        }
      }
      settings.setAttribute("verwendungszweck", params.getVerwendungszweck());
      settings.setAttribute("fixerBetragAbrechnen",
          params.isFixerBetragAbrechnen());
      if (params.isFixerBetragAbrechnen())
      {
        if (params.getFixerBetrag() != null)
        {
          settings.setAttribute("fixerBetrag", params.getFixerBetrag());
        }
        else
        {
          settings.setAttribute("fixerBetrag", "");
        }
        if (params.getBuchungsart() != null)
        {
          settings.setAttribute("buchungsart", params.getBuchungsart().getID());
        }
        else
        {
          settings.setAttribute("buchungsart", "");
        }
        if (params.getBuchungsklasse() != null)
        {
          settings.setAttribute("buchungsklasse",
              params.getBuchungsklasse().getID());
        }
        else
        {
          settings.setAttribute("buchungsklasse", "");
        }
        if (params.getSteuer() != null)
        {
          settings.setAttribute("steuer", params.getSteuer().getID());
        }
        else
        {
          settings.setAttribute("steuer", "");
        }
      }
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Speichern der Settings", ex);
    }
  }

  public boolean checkVorhandenePosten(Sollbuchung sollb,
      double ausgleichsbetrag) throws RemoteException
  {
    boolean buchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    double summe = 0;
    for (SollbuchungPosition pos : sollb.getSollbuchungPositionList())
    {
      if (!pos.getBuchungsart().getID().equals(params.getBuchungsart().getID())
          || (buchungsklasseInBuchung && !pos.getBuchungsklasse().getID()
              .equals(params.getBuchungsklasse().getID()))
          || (steuerInBuchung
              && !pos.getSteuer().getID().equals(params.getSteuer().getID())))
      {
        continue;
      }
      summe += pos.getBetrag();
    }
    if (summe - params.getFixerBetrag() < -0.005d)
    {
      // Es gibt nicht genügend Betrag für die Erstattung
      return false;
    }
    if (ausgleichsbetrag > 0)
    {
      // Der Position kann nicht mehr zugewiesen werden als noch frei ist
      double zugewiesen = 0;
      for (Buchung bu : sollb.getBuchungList())
      {
        if (!bu.getBuchungsart().getID().equals(params.getBuchungsart().getID())
            || (buchungsklasseInBuchung && !bu.getBuchungsklasse().getID()
                .equals(params.getBuchungsklasse().getID()))
            || (steuerInBuchung
                && !bu.getSteuer().getID().equals(params.getSteuer().getID())))
        {
          continue;
        }
        zugewiesen += bu.getBetrag();
      }
      if (summe - zugewiesen - ausgleichsbetrag < -0.005d)
      {
        // Es gibt nicht genügend unausgeglichene Beträge für den Ausgleich
        return false;
      }
    }
    return true;
  }
}
