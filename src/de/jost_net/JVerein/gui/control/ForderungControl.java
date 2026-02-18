package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.JVerein.gui.dialogs.ForderungDialog;
import de.jost_net.JVerein.gui.input.AbbuchungsmodusInput.AbbuchungsmodusObject;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ForderungControl
{
  private final String KEINFEHLER = "Es wurden keine Probleme gefunden.";

  private LabelInput status = null;

  private Mitglied[] mitglieder;

  private TablePart bugsList;

  private CheckboxInput vorlageSpeichernInput;

  private boolean einstellungRechnungAnzeigen = false;

  private boolean einstellungBuchungsklasseInBuchung = false;

  private boolean einstellungSteuerInBuchung = false;

  final AbrechnungSEPAControl sepaControl = new AbrechnungSEPAControl(null);

  private Settings settings = null;

  private Zusatzbetrag zusatzb;

  private ZusatzbetragPart part;

  private Date sepagueltigkeit;

  public ForderungControl(Mitglied[] mitglieder) throws RemoteException
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -36);
    sepagueltigkeit = cal.getTime();

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

  public AbrechnungSEPAControl getSepaControl()
  {
    return sepaControl;
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

  public CheckboxInput getVorlageSpeichernInput()
  {
    if (vorlageSpeichernInput != null)
    {
      return vorlageSpeichernInput;
    }
    vorlageSpeichernInput = new CheckboxInput(false);
    return vorlageSpeichernInput;
  }

  public Button getHelpButton()
  {
    Button b = new Button("Hilfe", new DokumentationAction(),
        DokumentationUtil.FORDERUNG, false, "question-circle.png");
    return b;
  }

  public Button getZahlungsgrundVariablenButton() throws RemoteException
  {
    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = MitgliedMap.getDummyMap(map);
    Button b = new Button("Zahlungsgrund Variablen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    return b;
  }

  public Button getRechnungstextVariablenButton() throws RemoteException
  {
    Button b = new Button("Rechnungstext Variablen",
        new RechnungVariableDialogAction(part), null, false, "bookmark.png");
    return b;
  }

  public Button getVorlagenButton() throws RemoteException
  {
    Button b = new Button("Vorlagen",
        new ZusatzbetragVorlageAuswahlAction(part), null, false,
        "view-refresh.png");
    return b;
  }

  public Button getPruefenButton()
  {
    Button b = new Button("Auf Probleme prüfen", context -> {
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

  public Button getErstellenButton(ForderungDialog dialog)
  {
    Button b = new Button("Erstellen", context -> {
      try
      {
        if (!checkInput(part))
        {
          return;
        }
        saveSettings(part);

        sepaControl.getZahlungsgrund()
            .setValue((String) part.getBuchungstext().getValue());
        sepaControl.getAbbuchungsmodus()
            .setValue(new AbbuchungsmodusObject(Abrechnungsmodi.FORDERUNG));
        sepaControl.startZusatzbetragAbrechnung(getZusatzbetraegeList(part));
        dialog.close();
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
      catch (RemoteException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
        Logger.error("Fehler", e);
      }
    }, null, true, "ok.png");
    return b;
  }

  public Button getAbbrechenButton(ForderungDialog dialog)
      throws RemoteException
  {
    Button b = new Button("Abbrechen", context -> {
      dialog.close();
    }, null, false, "process-stop.png");
    return b;
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
        Mitglied m = (Mitglied) object;
        try
        {
          if (m.getMitgliedstyp() == null
              || m.getMitgliedstyp().getID().equals(Mitgliedstyp.MITGLIED))
          {
            GUI.startView(new MitgliedDetailView(), m);
          }
          else
          {
            GUI.startView(new NichtMitgliedDetailView(), m);
          }
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(
              "Fehler beim Anzeigen eines Mitgliedes", e);
        }
      }
    });
    bugsList.addColumn("Name", "name");
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

    for (Mitglied m : mitglieder)
    {
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if ((weg == null && m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
          || (weg != null && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT))
      {
        if (m.getMandatDatum().equals(Einstellungen.NODATE))
        {
          bugs.add(new Bug(m,
              "Für die Basislastschrift fehlt das Mandatsdatum!", Bug.ERROR));
        }
        else if (m.getMandatDatum().after(new Date()))
        {
          bugs.add(
              new Bug(m, "Das Mandatsdatum liegt in der Zukunft!", Bug.ERROR));
        }

        if (m.getIban() == null || m.getIban().isEmpty())
        {
          bugs.add(new Bug(m, "Für die Basislastschrift fehlt die IBAN!",
              Bug.ERROR));
        }
        else
        {
          try
          {
            new IBAN(m.getIban());
          }
          catch (SEPAException e)
          {
            bugs.add(new Bug(m, "Ungültige IBAN " + m.getIban(), Bug.ERROR));
          }
          try
          {
            new BIC(m.getBic());
          }
          catch (Exception e)
          {
            bugs.add(new Bug(m, "Ungültige BIC " + m.getBic(), Bug.ERROR));
          }
        }

        if (!(Boolean) sepaControl.getSEPACheck().getValue()
            && !m.getMandatDatum().equals(Einstellungen.NODATE))
        {
          if (m.getLetzteLastschrift() == null
              && m.getMandatDatum().before(sepagueltigkeit))
          {
            bugs.add(new Bug(m,
                "Das Mandat ist älter als 36 Monate und es existiert noch keine Lastschrift in JVerein.\n"
                    + "Neues Mandat anfordern und eingeben oder den SEPA-Check temporär deaktivieren.",
                Bug.ERROR));
          }

          if (m.getLetzteLastschrift() != null
              && m.getLetzteLastschrift().before(sepagueltigkeit)
              && m.getMandatDatum().before(sepagueltigkeit))
          {
            bugs.add(new Bug(m,
                "Letzte Lastschrift und das Mandat sind älter als 36 Monate.\nNeues Mandat anfordern und eingeben.",
                Bug.ERROR));
          }
        }
      }
    }

    if (bugs.isEmpty())
    {
      bugs.add(new Bug(null, KEINFEHLER, Bug.HINT));
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

  private List<Zusatzbetrag> getZusatzbetraegeList(ZusatzbetragPart part)
      throws RemoteException, ApplicationException
  {
    List<Zusatzbetrag> list = new ArrayList<>();
    for (Mitglied mit : mitglieder)
    {
      Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
          .createObject(Zusatzbetrag.class, null);
      zb.setBetrag((Double) part.getBetrag().getValue());
      zb.setBuchungstext((String) part.getBuchungstext().getValue());
      zb.setFaelligkeit((Date) sepaControl.getFaelligkeit().getValue());
      zb.setIntervall(IntervallZusatzzahlung.KEIN);
      zb.setMitglied(Integer.parseInt(mit.getID()));
      zb.setStartdatum((Date) sepaControl.getFaelligkeit().getValue());
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

  private boolean checkInput(ZusatzbetragPart part)
  {
    try
    {
      if (sepaControl.getFaelligkeit().getValue() == null)
      {
        status.setValue("Bitte Fälligkeit eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (part.getBuchungstext().getValue() == null
          || ((String) part.getBuchungstext().getValue()).isEmpty())
      {
        status.setValue("Bitte Zahlungsgrund eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (part.getBetrag().getValue() == null
          || ((Double) part.getBetrag().getValue()) < 0.005d)
      {
        status.setValue("Bitte positiven Betrag eingeben");
        status.setColor(Color.ERROR);
        return false;
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

      if (einstellungRechnungAnzeigen
          && (boolean) sepaControl.getRechnung().getValue())
      {
        if (sepaControl.getRechnungFormular().getValue() == null)
        {
          status.setValue("Bitte Rechnungsformular auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
        if (sepaControl.getRechnungsdatum().getValue() == null)
        {
          status.setValue("Bitte Rechnungsdatum auswählen");
          status.setColor(Color.ERROR);
          return false;
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

  private void saveSettings(ZusatzbetragPart part)
  {
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

  private class RechnungVariableDialogAction implements Action
  {
    private ZusatzbetragPart part;

    public RechnungVariableDialogAction(ZusatzbetragPart part)
    {
      this.part = part;
    }

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        sepaControl.getZahlungsgrund()
            .setValue((String) part.getBuchungstext().getValue());

        Map<String, Object> rmap = new AllgemeineMap().getMap(null);
        rmap = new AbrechnungsParameterMap().getMap(
            new AbrechnungSEPAParam(sepaControl, null, null, null), rmap);
        rmap = MitgliedMap.getDummyMap(rmap);
        rmap = RechnungMap.getDummyMap(rmap);
        new InsertVariableDialogAction(rmap).handleAction(null);
      }
      catch (RemoteException re)
      {
        //
      }
    }
  }
}
