package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.BugObjektEditAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.dialogs.GutschriftDialog;
import de.jost_net.JVerein.gui.dialogs.JVereinYesNoDialog;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.gui.menu.BugListMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.io.Gutschrift;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GutschriftControl extends AbstractAbrechnungControl
{
  private final Double LIMIT = 0.005;

  private boolean isMitglied;

  private IGutschriftProvider[] providerArray;

  private TextInput zweckInput;

  private CheckboxInput fixerBetragAbrechnenInput;

  private DecimalInput fixerBetragInput;

  private AbstractInput buchungsartInput;

  private SelectInput buchungsklasseInput;

  private SelectInput steuerInput;

  public GutschriftControl(IGutschriftProvider[] providerArray)
      throws RemoteException
  {
    this.isMitglied = providerArray[0] instanceof Mitglied;
    this.providerArray = providerArray;
  }

  public IGutschriftProvider[] getProviderArray()
  {
    return providerArray;
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

  public boolean isBuchungsklasseInputActiv()
  {
    return buchungsklasseInput != null;
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

  public boolean isSteuerInputActiv()
  {
    return steuerInput != null;
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

  public Button getVZweckVariablenButton() throws RemoteException
  {
    Map<String, Object> map = GutschriftMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);
    map = MitgliedMap.getDummyMap(map);
    Button b = new Button("Verwendungszweck Variablen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    return b;
  }

  public Button getRZweckVariablenButton() throws RemoteException
  {
    Map<String, Object> rmap = new AllgemeineMap().getMap(null);
    rmap = GutschriftMap.getDummyMap(rmap);
    rmap = MitgliedMap.getDummyMap(rmap);
    rmap = RechnungMap.getDummyMap(rmap);
    Button b = new Button("Rechnungstext Variablen",
        new InsertVariableDialogAction(rmap), null, false, "bookmark.png");
    return b;
  }

  public Button getErstellenButton(GutschriftDialog dialog)
  {
    Button b = new Button("Erstellen", context -> {
      try
      {
        status.setValue("");
        String error = checkInput();
        if (error != null)
        {
          bugsList.removeAll();
          bugsList.addItem(new Bug(null, error, Bug.ERROR));
          status.setValue("Es existieren Warnungen/Fehler, bitte beheben!");
          status.setColor(Color.ERROR);
          return;
        }
        refreshBugsList();

        // Prüfen ob Error oder Warning vorliegen. Bei Error nicht weiter
        // machen.
        @SuppressWarnings("unchecked")
        List<Bug> bugs = bugsList.getItems();
        for (Bug bug : bugs)
        {
          if (bug.getKlassifikation() == Bug.ERROR)
          {
            status.setValue("Es existieren Fehler, bitte beheben!");
            status.setColor(Color.ERROR);
            return;
          }
        }

        boolean warning = false;
        for (Bug bug : bugs)
        {
          if (bug.getKlassifikation() == Bug.WARNING)
          {
            warning = true;
            break;
          }
        }
        if (warning)
        {
          JVereinYesNoDialog d = new JVereinYesNoDialog(
              JVereinYesNoDialog.POSITION_CENTER);
          d.setTitle("Warnungen");
          d.setText(
              "Es existieren Warnungen.\nWenn fortgefahren wird, werden betroffene Einträge übersprungen.\nFortfahren?");
          Boolean choice = (Boolean) d.open();
          if (!choice.booleanValue())
          {
            return;
          }
        }
        saveSettings();
        new Gutschrift(this, getAbrechnungSEPAParam());
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
    }, null, true, "ok.png");
    return b;
  }

  @Override
  protected void saveSettings()
  {
    super.saveSettings();
    try
    {
      settings.setAttribute("verwendungszweck", (String) zweckInput.getValue());

      if (!isMitglied)
      {
        // Fixen Betrag erstatten
        settings.setAttribute("fixerBetragAbrechnen",
            (boolean) fixerBetragAbrechnenInput.getValue());
      }

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
      if (buchungsklasseInput != null)
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
      if (steuerInput != null)
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
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Speichern der Eingaben", ex);
    }
  }

  @Override
  protected String checkInput()
  {
    try
    {
      if (getZweckInput().getValue() == null
          || ((String) getZweckInput().getValue()).isEmpty())
      {
        return ("Bitte Verwendungszweck eingeben");
      }
      if (getFaelligkeit().getValue() == null)
      {
        return ("Bitte Ausführungsdatum auswählen");
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN)
          && (boolean) getRechnung().getValue())
      {
        if (getRechnungsformular().getValue() == null)
        {
          return ("Bitte Erstattungsformular auswählen");
        }
        if (getRechnungsdatum().getValue() == null)
        {
          return ("Bitte Rechnungsdatum auswählen");
        }
      }
      if ((boolean) getFixerBetragAbrechnenInput().getValue())
      {
        if (getFixerBetragInput().getValue() == null
            || ((Double) getFixerBetragInput().getValue()) < LIMIT)
        {
          return ("Bitte positiven Erstattungsbetrag eingeben");
        }
        if (getBuchungsartInput().getValue() == null)
        {
          return ("Bitte Buchungsart eingeben");
        }
        if (buchungsklasseInput != null
            && getBuchungsklasseInput().getValue() == null)
        {
          return ("Bitte Buchungsklasse eingeben");
        }
        if (steuerInput != null)
        {
          Buchungsart buchungsart = (Buchungsart) getBuchungsartInput()
              .getValue();
          Steuer steuer = (Steuer) getSteuerInput().getValue();
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
      }
    }
    catch (RemoteException re)
    {
      return ("Fehler beim Auswerten der Eingabe!");
    }
    return null;
  }

  @Override
  public JVereinTablePart getBugsList()
  {
    if (bugsList != null)
    {
      return bugsList;
    }
    bugsList = new JVereinTablePart(getInitialBugs(),
        new BugObjektEditAction());
    bugsList.addColumn("Typ", "objektName");
    bugsList.addColumn("ID", "objektId");
    bugsList.addColumn("Zahler", "zahlerName");
    bugsList.addColumn("Meldung", "meldung");
    bugsList.addColumn("Klassifikation", "klassifikationText");
    bugsList.setContextMenu(new BugListMenu());
    bugsList.setRememberColWidths(true);
    bugsList.setRememberOrder(true);
    return bugsList;
  }

  @Override
  protected List<Bug> getBugs()
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    try
    {
      checkVerrechnungskonto(bugs);
      checkGlobal(bugs);
      for (IGutschriftProvider provider : getProviderArray())
      {
        doChecks(provider, bugs);
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

  public String doChecks(IGutschriftProvider provider, ArrayList<Bug> bugs)
      throws RemoteException
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

    if (provider instanceof Lastschrift
        && provider.getGutschriftZahler() == null
        && ((Lastschrift) provider).getKursteilnehmer() == null)
    {
      meldung = "Es ist weder ein Mitglied noch ein Kursteilnehmer konfiguriert!";
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
      else
      {
        try
        {
          new IBAN(iban);
        }
        catch (SEPAException e)
        {
          meldung = "Ungültige IBAN des Mitglieds!";
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
      String bic = provider.getGutschriftZahler().getBic();
      if (bic == null || bic.isEmpty())
      {
        meldung = "Bei dem Mitglied ist keine BIC gesetzt.";
        if (bugs != null)
        {
          bugs.add(new Bug(provider.getGutschriftZahler(), meldung, Bug.HINT));
        }
      }
      else
      {
        try
        {
          new BIC(bic);
        }
        catch (SEPAException e)
        {
          meldung = "Ungültige BIC des Mitglieds!";
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
    }

    // Keine Gutschrift bei Erstattungen
    if (provider.getBetrag() < -LIMIT)
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
    if (provider.getIstSumme() < -LIMIT)
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

    boolean fixbetragabrechnen = (Boolean) fixerBetragAbrechnenInput.getValue();
    Double fixerbetrag = (Double) fixerBetragInput.getValue();
    if (provider instanceof Rechnung && fixbetragabrechnen && sollbList != null
        && sollbList.size() > 1
        && provider.getBetrag() - provider.getIstSumme() > LIMIT)
    {
      // Bei Gesamtrechnung erlauben wir nicht, dass eine beteiligte Sollbuchung
      // überzahlt ist falls es Ausgleichsbuchungen gibt, also nicht die ganze
      // Rechnung überzahlt ist. Das ist im Code nicht implementiert.
      for (Sollbuchung sollbuchung : sollbList)
      {
        if (sollbuchung.getIstSumme() - sollbuchung.getBetrag() > LIMIT)
        {
          meldung = "Die Gesamtrechnung enthält eine überzahlte Sollbuchung!";
          if (bugs != null)
          {
            bugs.add(new Bug(sollbuchung, meldung, Bug.WARNING));
          }
          else
          {
            return meldung;
          }
        }
      }
    }

    if (fixbetragabrechnen)
    {
      // Beträge bestimmen
      double tmp = provider.getBetrag() - provider.getIstSumme();
      double offenbetrag = tmp > LIMIT ? tmp : 0;
      tmp = fixerbetrag - offenbetrag;
      double ueberweisungsbetrag = tmp > LIMIT ? tmp : 0;
      tmp = fixerbetrag - ueberweisungsbetrag;
      double ausgleichsbetrag = tmp > LIMIT ? tmp : 0;

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
          && !checkVorhandenePosten(sollbFix, ausgleichsbetrag))
      {
        meldung = "Der Betrag der Sollbuchungspositionen mit der gewählten Buchungsart, \n"
            + "Buchungsklasse und Steuer ist nicht ausreichend!";
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
          bugs.add(new Bug(provider, meldung, Bug.HINT));
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
      double ausgleichsbetrag) throws RemoteException
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
      String posKlasse = pos.getBuchungsklasse() != null
          ? pos.getBuchungsklasse().getID()
          : "0";
      String paramsSteuer = steuerInput != null
          && steuerInput.getValue() != null
              ? ((Steuer) steuerInput.getValue()).getID()
              : "0";
      String paramKlasse = buchungsklasseInput != null
          && buchungsklasseInput.getValue() != null
              ? ((Buchungsklasse) buchungsklasseInput.getValue()).getID()
              : "0";
      if (!pos.getBuchungsart().getID()
          .equals(((Buchungsart) buchungsartInput.getValue()).getID())
          || (buchungsklasseInBuchung && !posKlasse.equals(paramKlasse))
          || (steuerInBuchung && !posSteuer.equals(paramsSteuer)))
      {
        continue;
      }
      summe += pos.getBetrag();
    }
    if (summe - (Double) fixerBetragInput.getValue() < -LIMIT)
    {
      // Es gibt nicht genügend Betrag für die Erstattung
      return false;
    }
    return true;
  }

  @Override
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    return new AbrechnungSEPAParam(this, sepaVersion);
  }

  @Override
  public void doStart() throws ApplicationException
  {
    // wird hier nicht benutzt
  }
}
