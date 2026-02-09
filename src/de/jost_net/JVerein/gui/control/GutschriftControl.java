package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.GrayableTextAreaInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
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

  private boolean isNewDialog;

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

  private GrayableTextAreaInput kommentarInput;

  private CheckboxInput rechnungErzeugenInput;

  private CheckboxInput rechnungsDokumentSpeichernInput;

  private FormularInput formularInput;

  private TextInput rechnungsTextInput;

  private DateInput rechnungsDatumInput;

  private Boolean EinstellungRechnungAnzeigen;

  private Boolean EinstellungSpeicherungAnzeigen;

  private Boolean EinstellungBuchungsklasseInBuchung;

  private Boolean EinstellungSteuerInBuchung;

  private Settings settings = null;

  final AbrechnungSEPAControl scontrol = new AbrechnungSEPAControl(null);

  public GutschriftControl(IGutschriftProvider[] providerArray,
      boolean isMitglied, boolean isNewDialog) throws RemoteException
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.isMitglied = isMitglied;
    this.providerArray = providerArray;
    this.isNewDialog = isNewDialog;

    EinstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    EinstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    EinstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    EinstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
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
    Date d = new Date();
    if (!isNewDialog)
    {
      d = getDatum("datum");
    }
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
    fixerBetragAbrechnenInput
        .setName(" *Sonst bereits bezahlten Betrag überweisen.");
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

  public GrayableTextAreaInput getRechnungKommentarInput()
      throws RemoteException
  {
    if (kommentarInput != null)
    {
      return kommentarInput;
    }

    kommentarInput = new GrayableTextAreaInput(
        settings.getString("kommentar", ""), 1024);
    kommentarInput.setHeight(50);
    kommentarInput.setEnabled((Boolean) rechnungErzeugenInput.getValue());
    return kommentarInput;
  }

  public CheckboxInput getRechnungErzeugenInput()
  {
    if (rechnungErzeugenInput != null)
    {
      return rechnungErzeugenInput;
    }
    rechnungErzeugenInput = scontrol.getRechnung();
    rechnungErzeugenInput
        .setValue(settings.getBoolean("rechnungErzeugen", false));
    rechnungErzeugenInput.addListener(e -> {
      kommentarInput.setEnabled((Boolean) rechnungErzeugenInput.getValue());
    });
    return rechnungErzeugenInput;
  }

  public CheckboxInput getRechnungsDokumentSpeichernInput()
  {
    if (rechnungsDokumentSpeichernInput != null)
    {
      return rechnungsDokumentSpeichernInput;
    }
    rechnungsDokumentSpeichernInput = scontrol.getRechnungsdokumentSpeichern();
    rechnungsDokumentSpeichernInput
        .setValue(settings.getBoolean("rechnungsDokumentSpeichern", false));
    return rechnungsDokumentSpeichernInput;
  }

  public FormularInput getFormularInput() throws RemoteException
  {
    if (formularInput != null)
    {
      return formularInput;
    }
    formularInput = scontrol.getRechnungFormular();
    Formular f = null;
    try
    {
      String id = settings.getString("formular", "");
      if (id != null && !id.isEmpty())
      {
        f = (Formular) Einstellungen.getDBService().createObject(Formular.class,
            id);
      }
    }
    catch (Exception ex)
    {
      // Nicht gefunden, dann null
    }
    formularInput.setValue(f);
    return formularInput;
  }

  public TextInput getRechnungsTextInput()
  {
    if (rechnungsTextInput != null)
    {
      return rechnungsTextInput;
    }
    rechnungsTextInput = scontrol.getRechnungstext();
    rechnungsTextInput.setValue(settings.getString("rechnungstext", ""));
    return rechnungsTextInput;
  }

  public DateInput getRechnungsDatumInput()
  {
    if (rechnungsDatumInput != null)
    {
      return rechnungsDatumInput;
    }
    rechnungsDatumInput = scontrol.getRechnungsdatum();
    if (!isNewDialog)
    {
      rechnungsDatumInput.setValue(getDatum("rechnungsdatum"));
    }
    return rechnungsDatumInput;
  }

  public void storeValues()
  {
    params = new GutschriftParam();
    params.setAusgabe((UeberweisungAusgabe) ausgabeInput.getValue());
    params.setDatum((Date) datumInput.getValue());
    params.setVerwendungszweck((String) zweckInput.getValue());

    // Fixer Betrag
    boolean fixerBetragAbrechnen = (boolean) fixerBetragAbrechnenInput
        .getValue();
    if (fixerBetragAbrechnen)
    {
      params.setFixerBetragAbrechnen(fixerBetragAbrechnen);
      params.setFixerBetrag((Double) fixerBetragInput.getValue());
      params.setBuchungsart((Buchungsart) buchungsartInput.getValue());
      if (buchungsklasseInput != null)
      {
        params
            .setBuchungsklasse((Buchungsklasse) buchungsklasseInput.getValue());
      }
      if (steuerInput != null)
      {
        params.setSteuer((Steuer) steuerInput.getValue());
      }
    }

    // Rechnung
    if (EinstellungRechnungAnzeigen)
    {
      params.setRechnungErzeugen((boolean) rechnungErzeugenInput.getValue());
      params.setFormular((Formular) formularInput.getValue());
      if (rechnungsDokumentSpeichernInput != null)
      {
        params.setRechnungsDokumentSpeichern(
            (boolean) rechnungsDokumentSpeichernInput.getValue());
      }
      params.setRechnungsText((String) rechnungsTextInput.getValue());
      params.setRechnungsDatum((Date) rechnungsDatumInput.getValue());
      params.setRechnungsKommentar((String) kommentarInput.getValue());
    }
    saveSettings();
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
      settings.setAttribute("verwendungszweck", params.getVerwendungszweck());

      // Fixen Betrag erstatten
      settings.setAttribute("fixerBetragAbrechnen",
          (boolean) fixerBetragAbrechnenInput.getValue());

      if (params.getFixerBetrag() != null)
      {
        settings.setAttribute("fixerBetrag",
            (Double) fixerBetragInput.getValue());
      }
      else
      {
        settings.setAttribute("fixerBetrag", "");
      }
      if (params.getBuchungsart() != null)
      {
        settings.setAttribute("buchungsart",
            ((Buchungsart) buchungsartInput.getValue()).getID());
      }
      else
      {
        settings.setAttribute("buchungsart", "");
      }
      if (EinstellungBuchungsklasseInBuchung)
      {
        if (params.getBuchungsklasse() != null)
        {
          settings.setAttribute("buchungsklasse",
              ((Buchungsklasse) buchungsklasseInput.getValue()).getID());
        }
        else
        {
          settings.setAttribute("buchungsklasse", "");
        }
      }
      if (EinstellungSteuerInBuchung)
      {
        if (params.getSteuer() != null)
        {
          settings.setAttribute("steuer",
              ((Steuer) steuerInput.getValue()).getID());
        }
        else
        {
          settings.setAttribute("steuer", "");
        }
      }

      if (EinstellungRechnungAnzeigen)
      {
        settings.setAttribute("rechnungErzeugen", params.isRechnungErzeugen());
        if (EinstellungSpeicherungAnzeigen)
        {
          settings.setAttribute("rechnungsDokumentSpeichern",
              params.isRechnungsDokumentSpeichern());
        }
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
        settings.setAttribute("formular", params.getFormular().getID());
        settings.setAttribute("rechnungstext", params.getRechnungsText());
        settings.setAttribute("kommentar", params.getRechnungsKommentar());
      }
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Speichern der Settings", ex);
    }
  }
}
