package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

public class GutschriftControl extends AbstractControl
{
  private boolean isMitglied;

  private SelectInput ausgabeInput;

  private DateInput datumInput;

  private TextInput zweckInput;

  private CheckboxInput fixerBetragAbrechnenInput;

  private DecimalInput fixerBetragInput;

  private AbstractInput buchungsartInput;

  private SelectInput buchungsklasseInput;

  private SelectInput steuerInput;

  private Settings settings = null;

  public GutschriftControl(AbstractView view, boolean isMitglied)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.isMitglied = isMitglied;
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
    datumInput = new DateInput(new Date());
    datumInput.setMandatory(true);
    return datumInput;
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

  public void saveSettings(GutschriftParam params)
  {
    try
    {
      settings.setAttribute("ausgabe", params.getAusgabe().getKey());
      settings.setAttribute("verwendungszweck", params.getVerwendungszweck());
      if (!isMitglied)
      {
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
            settings.setAttribute("buchungsart",
                params.getBuchungsart().getID());
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
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Speichern der Settings", ex);
    }
  }
}
