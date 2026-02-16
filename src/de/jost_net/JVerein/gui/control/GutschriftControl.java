package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.dialogs.GutschriftDialog;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.GrayableTextAreaInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.io.Gutschrift;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GutschriftControl
{
  private TablePart bugsList;

  private boolean isMitglied;

  private IGutschriftProvider[] providerArray;

  private LabelInput status;

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

  private Boolean einstellungRechnungAnzeigen;

  private Boolean einstellungSpeicherungAnzeigen;

  private Boolean einstellungBuchungsklasseInBuchung;

  private Boolean einstellungSteuerInBuchung;

  private Settings settings = null;

  final AbrechnungSEPAControl scontrol = new AbrechnungSEPAControl(null);

  public GutschriftControl(IGutschriftProvider[] providerArray)
      throws RemoteException
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.isMitglied = providerArray[0] instanceof Mitglied;
    this.providerArray = providerArray;

    einstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    einstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    einstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    einstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
  }

  public IGutschriftProvider[] getProviderArray()
  {
    return providerArray;
  }

  public GutschriftParam getParams()
  {
    return params;
  }

  public LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
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
    datumInput = new DateInput(d, new JVDateFormatTTMMJJJJ());
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
        .setName(" *Sonst ganzen Betrag erstatten und Fehlbeträge verrechnen.");
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
    return rechnungsDatumInput;
  }

  public Button getHelpButton()
  {
    Button b = new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.GUTSCHRIFT, false, "question-circle.png");
    return b;
  }

  public Button getVZweckVariablenButton() throws RemoteException
  {
    Map<String, Object> map = GutschriftMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);
    map = MitgliedMap.getDummyMap(map);
    Button b = new Button("Verwendungszweck Variablen anzeigen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    return b;
  }

  public Button getRZweckVariablenButton() throws RemoteException
  {
    Map<String, Object> rmap = new AllgemeineMap().getMap(null);
    rmap = GutschriftMap.getDummyMap(rmap);
    rmap = MitgliedMap.getDummyMap(rmap);
    rmap = RechnungMap.getDummyMap(rmap);
    Button b = new Button("Rechnungstext Variablen anzeigen",
        new InsertVariableDialogAction(rmap), null, false, "bookmark.png");
    return b;
  }

  public Button getPruefenButton()
  {
    Button b = new Button("Auf Probleme prüfen", context -> {
      if (!checkInput())
      {
        return;
      }
      status.setValue("");
      storeValues();
      try
      {
        refreshBugsList();
      }
      catch (RemoteException e)
      {
        status.setValue("Interner Fehler beim Update der Fehlerliste");
        status.setColor(Color.ERROR);
        Logger.error("Fehler", e);
      }
    }, null, false, "bug.png");
    return b;
  }

  public Button getErstellenButton(GutschriftDialog dialog)
  {
    Button b = new Button("Gutschriften erstellen", context -> {
      try
      {
        if (!checkInput())
        {
          return;
        }
        storeValues();
        new Gutschrift(this);
        dialog.close();
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
      catch (Exception e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
        Logger.error("Fehler", e);
      }
    }, null, false, "ok.png");
    return b;
  }

  public Button getAbbrechenButton(GutschriftDialog dialog)
  {
    Button b = new Button("Abbrechen", context -> {
      dialog.close();
    }, null, false, "process-stop.png");
    return b;
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
    if (einstellungRechnungAnzeigen)
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
      settings.setAttribute("verwendungszweck", params.getVerwendungszweck());

      // Fixen Betrag erstatten
      settings.setAttribute("fixerBetragAbrechnen",
          (boolean) fixerBetragAbrechnenInput.getValue());

      if ((Double) fixerBetragInput.getValue() != null)
      {
        settings.setAttribute("fixerBetrag",
            (Double) fixerBetragInput.getValue());
      }
      else
      {
        settings.setAttribute("fixerBetrag", "");
      }
      if ((Buchungsart) buchungsartInput.getValue() != null)
      {
        settings.setAttribute("buchungsart",
            ((Buchungsart) buchungsartInput.getValue()).getID());
      }
      else
      {
        settings.setAttribute("buchungsart", "");
      }
      if (einstellungBuchungsklasseInBuchung)
      {
        if ((Buchungsklasse) buchungsklasseInput.getValue() != null)
        {
          settings.setAttribute("buchungsklasse",
              ((Buchungsklasse) buchungsklasseInput.getValue()).getID());
        }
        else
        {
          settings.setAttribute("buchungsklasse", "");
        }
      }
      if (einstellungSteuerInBuchung)
      {
        if ((Steuer) steuerInput.getValue() != null)
        {
          settings.setAttribute("steuer",
              ((Steuer) steuerInput.getValue()).getID());
        }
        else
        {
          settings.setAttribute("steuer", "");
        }
      }

      if (einstellungRechnungAnzeigen)
      {
        settings.setAttribute("rechnungErzeugen", params.isRechnungErzeugen());
        if (einstellungSpeicherungAnzeigen)
        {
          settings.setAttribute("rechnungsDokumentSpeichern",
              params.isRechnungsDokumentSpeichern());
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

  public boolean checkInput()
  {
    try
    {
      if (getZweckInput().getValue() == null
          || ((String) getZweckInput().getValue()).isEmpty())
      {
        status.setValue("Bitte Verwendungszweck eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (getDatumInput().getValue() == null)
      {
        status.setValue("Bitte Ausführungsdatum auswählen");
        status.setColor(Color.ERROR);
        return false;
      }
      if (einstellungRechnungAnzeigen
          && (boolean) getRechnungErzeugenInput().getValue())
      {
        if (getFormularInput().getValue() == null)
        {
          status.setValue("Bitte Erstattungsformular auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
        if (getRechnungsDatumInput().getValue() == null)
        {
          status.setValue("Bitte Rechnungsdatum auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
      }
      if ((boolean) getFixerBetragAbrechnenInput().getValue())
      {
        if (getFixerBetragInput().getValue() == null
            || ((Double) getFixerBetragInput().getValue()) < 0.005d)
        {
          status.setValue("Bitte positiven Erstattungsbetrag eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (getBuchungsartInput().getValue() == null)
        {
          status.setValue("Bitte Buchungsart eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (einstellungBuchungsklasseInBuchung
            && getBuchungsklasseInput().getValue() == null)
        {
          status.setValue("Bitte Buchungsklasse eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (einstellungSteuerInBuchung)
        {
          Buchungsart buchungsart = (Buchungsart) getBuchungsartInput()
              .getValue();
          Steuer steuer = (Steuer) getSteuerInput().getValue();
          if (steuer != null && buchungsart != null)
          {
            if (buchungsart.getSpende() || buchungsart.getAbschreibung())
            {
              status.setValue(
                  "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
              status.setColor(Color.ERROR);
              return false;
            }
            if (steuer.getBuchungsart().getArt() != buchungsart.getArt())
            {
              switch (buchungsart.getArt())
              {
                case ArtBuchungsart.AUSGABE:
                  status.setValue("Umsatzsteuer statt Vorsteuer gewählt!");
                  status.setColor(Color.ERROR);
                  return false;
                case ArtBuchungsart.EINNAHME:
                  status.setValue("Vorsteuer statt Umsatzsteuer gewählt!");
                  status.setColor(Color.ERROR);
                  return false;
                // Umbuchung ist bei Anlagebuchungen möglich,
                // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf)
                // möglich
                case ArtBuchungsart.UMBUCHUNG:
                  break;
              }
            }
          }
        }
      }
    }
    catch (RemoteException re)
    {
      status.setValue("Fehler beim Auswerten der Eingabe!");
      status.setColor(Color.ERROR);
      return false;
    }
    return true;
  }

  public Part getBugsList() throws RemoteException
  {
    if (bugsList != null)
    {
      return bugsList;
    }
    bugsList = new TablePart(getBugs(), context -> {
      Bug bug = (Bug) context;
      Object object = bug.getObject();
      if (object instanceof Mitglied)
      {
        GUI.startView(MitgliedDetailView.class, object);
      }
      if (object instanceof Lastschrift)
      {
        GUI.startView(LastschriftDetailView.class, object);
      }
      if (object instanceof Rechnung)
      {
        GUI.startView(RechnungDetailView.class, object);
      }
      if (object instanceof Sollbuchung)
      {
        GUI.startView(SollbuchungDetailView.class, object);
      }
      if (object instanceof Buchung)
      {
        GUI.startView(BuchungDetailView.class, object);
      }
    });
    bugsList.addColumn("Typ", "objektName");
    bugsList.addColumn("ID", "objektId");
    bugsList.addColumn("Zahler", "zahlerName");
    bugsList.addColumn("Meldung", "meldung");
    bugsList.addColumn("Klassifikation", "klassifikationText");
    bugsList.setRememberColWidths(true);
    bugsList.setRememberOrder(true);
    return bugsList;
  }

  public void refreshBugsList() throws RemoteException
  {
    bugsList.removeAll();
    for (Bug bug : getBugs())
    {
      bugsList.addItem(bug);
    }
    bugsList.sort();
  }

  private List<Bug> getBugs() throws RemoteException
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    if (getParams() != null)
    {
      for (IGutschriftProvider provider : getProviderArray())
      {
        doChecks(provider, getParams(), bugs);
      }

      if (bugs.isEmpty())
      {
        bugs.add(new Bug(null, "Es wurden keine Probleme gefunden.", Bug.HINT));
      }
    }
    return bugs;
  }

  public String doChecks(IGutschriftProvider provider, GutschriftParam params,
      ArrayList<Bug> bugs) throws RemoteException
  {
    String meldung;

    if (!(provider instanceof Lastschrift)
        && provider.getGutschriftZahler() == null)
    {
      meldung = "Kein Zahler konfiguriert!";
      if (bugs != null)
      {
        bugs.add(new Bug(provider, meldung, Bug.WARNING));
      }
      else
      {
        return meldung;
      }
    }

    // Bei Lastschrift ohne Zahler erstatten wir auf das gleiche Konto
    // wie bei der Lastschrift
    if (provider.getGutschriftZahler() != null)
    {
      String iban = provider.getGutschriftZahler().getIban();
      if (iban == null || iban.isEmpty())
      {
        meldung = "Bei dem Mitglied ist keine IBAN gesetzt!";
        if (bugs != null)
        {
          bugs.add(
              new Bug(provider.getGutschriftZahler(), meldung, Bug.WARNING));
        }
        else
        {
          return meldung;
        }
      }
    }

    // Keine Gutschrift bei Erstattungen
    if (provider.getBetrag() < -0.005d)
    {
      meldung = "Der Betrag ist negativ!";
      if (bugs != null)
      {
        bugs.add(new Bug(provider, meldung, Bug.WARNING));
      }
      else
      {
        return meldung;
      }
    }

    // Keine Gutschrift bei negativer Einzahlung
    if (provider.getIstSumme() < -0.005d)
    {
      meldung = "Der Zahlungseingang ist negativ, dadurch kann nichts erstattet werden!";
      if (bugs != null)
      {
        bugs.add(new Bug(provider, meldung, Bug.WARNING));
      }
      else
      {
        return meldung;
      }
    }

    if (provider instanceof Sollbuchung)
    {
      meldung = checkSollbuchung((Sollbuchung) provider, bugs);
      if (meldung != null)
      {
        return meldung;
      }
    }

    List<Sollbuchung> sollbList = null;
    if (provider instanceof Rechnung)
    {
      sollbList = ((Rechnung) provider).getSollbuchungList();
      if (sollbList == null || sollbList.isEmpty())
      {
        meldung = "Die Rechnung hat keine Sollbuchungen!";
        if (bugs != null)
        {
          bugs.add(new Bug(provider, meldung, Bug.WARNING));
        }
        else
        {
          return meldung;
        }
      }
      if (sollbList != null)
      {
        for (Sollbuchung sollb : sollbList)
        {
          meldung = checkSollbuchung((Sollbuchung) sollb, bugs);
          if (meldung != null)
          {
            return meldung;
          }
        }
      }
    }

    if (params.isFixerBetragAbrechnen())
    {
      // Beträge bestimmen
      double tmp = provider.getBetrag() - provider.getIstSumme();
      double offenbetrag = tmp > 0.005d ? tmp : 0;
      tmp = params.getFixerBetrag() - offenbetrag;
      double ueberweisungsbetrag = tmp > 0.005d ? tmp : 0;
      tmp = params.getFixerBetrag() - ueberweisungsbetrag;
      double ausgleichsbetrag = tmp > 0.005d ? tmp : 0;

      Sollbuchung sollbFix = null;
      if (provider instanceof Rechnung)
      {
        // Fixer Betrag bei Gesamtrechnung wird nicht unterstützt
        // Bei welcher Sollbuchung soll man da die Erstattung ausgleichen?
        if (sollbList != null && sollbList.size() > 1)
        {
          meldung = "Fixer Betrag bei Gesamtrechnungen wird nicht unterstützt!";
          if (bugs != null)
          {
            bugs.add(new Bug(provider, meldung, Bug.WARNING));
          }
          else
          {
            return meldung;
          }
        }

        if (sollbList != null && sollbList.size() == 1)
        {
          sollbFix = ((Rechnung) provider).getSollbuchungList().get(0);
        }
      }

      if (provider instanceof Sollbuchung)
      {
        sollbFix = (Sollbuchung) provider;
      }

      if (sollbFix != null
          && !checkVorhandenePosten(sollbFix, params, ausgleichsbetrag))
      {
        meldung = "Der Betrag der Sollbuchungspositionen mit der gewählten Buchungsart, Buchungsklasse und Steuer ist nicht ausreichend!";
        if (bugs != null)
        {
          bugs.add(new Bug(provider, meldung, Bug.WARNING));
        }
        else
        {
          return meldung;
        }
      }

      if (ausgleichsbetrag > 0)
      {
        meldung = "Der Erstattungsbetrag wird mit offenen Forderungen verrechnet!";
        if (bugs != null)
        {
          bugs.add(new Bug(provider, meldung, Bug.WARNING));
        }
      }
    }
    return null;
  }

  private String checkSollbuchung(Sollbuchung sollb, ArrayList<Bug> bugs)
      throws RemoteException
  {
    String meldung;
    List<SollbuchungPosition> posList = sollb.getSollbuchungPositionList();
    if (posList == null || posList.isEmpty())
    {
      meldung = "Die Sollbuchung hat keine Sollbuchungspositionen!";
      if (bugs != null)
      {
        bugs.add(new Bug(sollb, meldung, Bug.WARNING));
      }
      else
      {
        return meldung;
      }
    }
    else
    {
      for (SollbuchungPosition pos : posList)
      {
        if (pos.getBuchungsart() == null)
        {
          meldung = "Es haben nicht alle Sollbuchungspositionen eine Buchungsart!";
          if (bugs != null)
          {
            bugs.add(new Bug(sollb, meldung, Bug.WARNING));
          }
          else
          {
            return meldung;
          }
          break;
        }
      }
    }
    List<Buchung> buList = sollb.getBuchungList();
    if (buList != null && !buList.isEmpty())
    {
      for (Buchung bu : buList)
      {
        if (bu.getBuchungsart() == null)
        {
          meldung = "Die zugeordnete Buchung hat keine Buchungsart!";
          if (bugs != null)
          {
            bugs.add(new Bug(bu, meldung, Bug.WARNING));
          }
          else
          {
            return meldung;
          }
        }
      }
    }
    return null;
  }

  private boolean checkVorhandenePosten(Sollbuchung sollb,
      GutschriftParam params, double ausgleichsbetrag) throws RemoteException
  {
    boolean buchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    double summe = 0;
    for (SollbuchungPosition pos : sollb.getSollbuchungPositionList())
    {
      if (pos.getBuchungsart() == null
          || (buchungsklasseInBuchung && pos.getBuchungsklasse() == null))
      {
        continue;
      }
      String posSteuer = pos.getSteuer() != null ? pos.getSteuer().getID()
          : "0";
      String paramsSteuer = params.getSteuer() != null
          ? params.getSteuer().getID()
          : "0";
      if (!pos.getBuchungsart().getID().equals(params.getBuchungsart().getID())
          || (buchungsklasseInBuchung && !pos.getBuchungsklasse().getID()
              .equals(params.getBuchungsklasse().getID()))
          || (steuerInBuchung && !posSteuer.equals(paramsSteuer)))
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
    return true;
  }
}
