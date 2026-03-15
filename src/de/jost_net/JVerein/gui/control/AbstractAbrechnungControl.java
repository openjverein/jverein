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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AbrechnungsParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.BugObjektEditAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.DisableTextAreaInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.JVereinDateInput;
import de.jost_net.JVerein.gui.menu.BugListMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.AbrechnungSEPA;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.io.Bankarbeitstage;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractAbrechnungControl
{
  protected final String KEINFEHLER = "Es wurden keine Probleme gefunden.";

  private static String CONFIRM_TITEL = "SEPA-Check temporär deaktivieren";

  private static String CONFIRM_TEXT = "Bei einer SEPA-Lastschrift muß ein gültiges SEPA-Mandat vorliegen.\n"
      + "Wenn das Mandat älter als 3 Jahre ist, müssen in den letzten 3 Jahren Lastschriften durchgeführt worden sein.\n"
      + "Wählen Sie \"Ja\" nur, wenn diese Bedingungen für alle Mitglieder erfüllt sind.";

  private static String CONFIRM_FEHLER = "Fehler beim Setzen der Checkbox";

  protected LabelInput status;

  private JVereinDateInput faelligkeit;

  private JVereinDateInput stichtag;

  private CheckboxInput sollbuchungenzusammenfassen;

  private CheckboxInput kompakteabbuchung;

  private CheckboxInput sepacheck;

  private CheckboxInput sepaprint;

  private SelectInput ausgabe;

  private CheckboxInput rechnung;

  private CheckboxInput rechnungsdokumentspeichern;

  private FormularInput rechnungsformular;

  private TextInput rechnungstext;

  private JVereinDateInput rechnungsdatum;

  private DisableTextAreaInput rechnungskommentar;

  protected JVereinTablePart bugsList;

  private Date sepagueltigkeit;

  private Beitragsmodel beitragsmodel;

  protected Settings settings = null;

  public AbstractAbrechnungControl() throws RemoteException
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -36);
    sepagueltigkeit = cal.getTime();
    beitragsmodel = Beitragsmodel.getByKey(
        (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL));
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

  public JVereinDateInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }
    Calendar cal = Calendar.getInstance();
    Bankarbeitstage bat = new Bankarbeitstage();
    cal = bat.getCalendar(cal,
        1 + (Integer) Einstellungen.getEinstellung(Property.SEPADATUMOFFSET));
    this.faelligkeit = new JVereinDateInput(cal.getTime(),
        new JVDateFormatTTMMJJJJ());
    this.faelligkeit.setTitle("Fälligkeit");
    this.faelligkeit.setText("Bitte Fälligkeitsdatum wählen");
    faelligkeit.addListener(event -> {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      if (faelligkeit.getValue() != null && stichtag != null
          && getStichtag().getValue() == null)
      {
        getStichtag().setValue(faelligkeit.getValue());
      }
    });
    faelligkeit.setMandatory(true);
    faelligkeit.setName("Fälligkeit");
    return faelligkeit;
  }

  public boolean isFaelligkeitActiv()
  {
    return faelligkeit != null;
  }

  public JVereinDateInput getStichtag()
  {
    if (stichtag != null)
    {
      return stichtag;
    }
    this.stichtag = new JVereinDateInput(null, new JVDateFormatTTMMJJJJ());
    this.stichtag.setTitle("Stichtag für die Abrechnung");
    this.stichtag.setText("Bitte Stichtag für die Abrechnung wählen");
    stichtag.setMandatory(true);
    stichtag.setName("Stichtag¹");
    return stichtag;
  }

  public boolean isStichtagActiv()
  {
    return stichtag != null;
  }

  public SelectInput getAbbuchungsausgabe()
  {
    if (ausgabe != null)
    {
      return ausgabe;
    }
    Abrechnungsausgabe aus = Abrechnungsausgabe.getByKey(settings
        .getInt("abrechnungsausgabe", Abrechnungsausgabe.SEPA_DATEI.getKey()));
    if (aus != Abrechnungsausgabe.SEPA_DATEI
        && aus != Abrechnungsausgabe.HIBISCUS
        && aus != Abrechnungsausgabe.KEINE_DATEI)
    {
      aus = Abrechnungsausgabe.HIBISCUS;
    }
    ausgabe = new SelectInput(Abrechnungsausgabe.values(), aus);
    ausgabe.setName("Abbuchungsausgabe");
    return ausgabe;
  }

  public boolean isAbbuchungsausgabeActiv()
  {
    return ausgabe != null;
  }

  public CheckboxInput getKompakteAbbuchung()
  {
    if (kompakteabbuchung != null)
    {
      return kompakteabbuchung;
    }
    kompakteabbuchung = new CheckboxInput(
        settings.getBoolean("kompakteabbuchung", false));
    kompakteabbuchung.addListener(new KompaktListener());
    return kompakteabbuchung;
  }

  public boolean isKompakteAbbuchungActiv()
  {
    return kompakteabbuchung != null;
  }

  public CheckboxInput getSollbuchungenZusammenfassen()
  {
    if (sollbuchungenzusammenfassen != null)
    {
      return sollbuchungenzusammenfassen;
    }
    sollbuchungenzusammenfassen = new CheckboxInput(
        settings.getBoolean("sollbuchungenzusammenfassen", false));
    sollbuchungenzusammenfassen.addListener(new ZusammenfassenListener());
    return sollbuchungenzusammenfassen;
  }

  public boolean isSollbuchungenZusammenfassenActiv()
  {
    return sollbuchungenzusammenfassen != null;
  }

  public CheckboxInput getRechnung()
  {
    if (rechnung != null)
    {
      return rechnung;
    }
    rechnung = new CheckboxInput(settings.getBoolean("rechnung", false));
    rechnung.addListener(new RechnungListener());
    return rechnung;
  }

  public boolean isRechnungActiv()
  {
    return rechnung != null;
  }

  public CheckboxInput getRechnungsdokumentSpeichern()
  {
    if (rechnungsdokumentspeichern != null)
    {
      return rechnungsdokumentspeichern;
    }
    rechnungsdokumentspeichern = new CheckboxInput(
        settings.getBoolean("rechnungsdokumentspeichern", false));
    rechnungsdokumentspeichern
        .setEnabled(settings.getBoolean("rechnung", false));
    return rechnungsdokumentspeichern;
  }

  public boolean isRechnungsdokumentSpeichernActiv()
  {
    return rechnungsdokumentspeichern != null;
  }

  public FormularInput getRechnungsformular() throws RemoteException
  {
    if (rechnungsformular != null)
    {
      return rechnungsformular;
    }
    rechnungsformular = new FormularInput(FormularArt.RECHNUNG,
        settings.getString("rechnungsformular", ""));
    updateRechnungsformular();
    rechnungsformular.setName("Rechnungsformular");
    return rechnungsformular;
  }

  public boolean isRechnungsformularActiv()
  {
    return rechnungsformular != null;
  }

  public TextInput getRechnungstext(String hint)
  {
    if (rechnungstext != null)
    {
      return rechnungstext;
    }
    rechnungstext = new TextInput(
        settings.getString("rechnungstext", "RE$rechnung_nummer"));
    rechnungstext.setEnabled((boolean) rechnung.getValue());
    rechnungstext.setHint(hint);
    rechnungstext.setName("Rechnungstext");
    return rechnungstext;
  }

  public boolean isRechnungstextActiv()
  {
    return rechnungstext != null;
  }

  public JVereinDateInput getRechnungsdatum()
  {
    if (rechnungsdatum != null)
    {
      return rechnungsdatum;
    }
    rechnungsdatum = new JVereinDateInput(new Date());
    rechnungsdatum.setMandatory((boolean) rechnung.getValue());
    rechnungsdatum.setEnabled((boolean) rechnung.getValue());
    rechnungsdatum.setName("Rechnungsdatum");
    return rechnungsdatum;
  }

  public boolean isRechnungsdatumActiv()
  {
    return rechnungsdatum != null;
  }

  public DisableTextAreaInput getRechnungskommentar() throws RemoteException
  {
    if (rechnungskommentar != null)
    {
      return rechnungskommentar;
    }

    rechnungskommentar = new DisableTextAreaInput(
        settings.getString("rechnungskommentar", ""), 1024);
    rechnungskommentar.setHeight(50);
    rechnungskommentar.setEnabled((Boolean) rechnung.getValue());
    rechnungskommentar.setName("Rechnungskommentar");
    return rechnungskommentar;
  }

  public boolean isRechnungskommentarActiv()
  {
    return rechnungskommentar != null;
  }

  public CheckboxInput getSEPAPrint()
  {
    if (sepaprint != null)
    {
      return sepaprint;
    }
    sepaprint = new CheckboxInput(settings.getBoolean("sepaprint", false));
    return sepaprint;
  }

  public boolean isSEPAPrintActiv()
  {
    return sepaprint != null;
  }

  public CheckboxInput getSEPACheck()
  {
    if (sepacheck != null)
    {
      return sepacheck;
    }
    sepacheck = new CheckboxInput(false);
    sepacheck.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        // Bei temporär deaktivieren den User fragen
        if ((Boolean) sepacheck.getValue())
        {
          if (!confirmDialog(CONFIRM_TITEL, CONFIRM_TEXT))
          {
            sepacheck.setValue(false);
          }
        }
      }
    });
    return sepacheck;
  }

  public boolean isSEPACheckActiv()
  {
    return sepacheck != null;
  }

  public static boolean confirmDialog(String title, String text)
  {
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle(title);
    d.setText(text);
    try
    {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return false;
      }
    }
    catch (Exception e)
    {
      Logger.error(CONFIRM_FEHLER, e);
      return false;
    }
    return true;
  }

  public class RechnungListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      if (rechnungsformular != null)
      {
        updateRechnungsformular();
      }
      if (rechnungstext != null)
      {
        rechnungstext.setEnabled((boolean) rechnung.getValue());
      }
      if (rechnungsdatum != null)
      {
        rechnungsdatum.setMandatory((boolean) rechnung.getValue());
        rechnungsdatum.setEnabled((boolean) rechnung.getValue());
      }
      if (rechnungskommentar != null)
      {
        rechnungskommentar.setEnabled((boolean) rechnung.getValue());
      }
      if (rechnungsdokumentspeichern != null)
      {
        rechnungsdokumentspeichern.setEnabled((boolean) rechnung.getValue());
      }
    }
  }

  private void updateRechnungsformular()
  {
    // Die Reihenfolge von mandatory und enabled ist abhängig von
    // enable/disable. Sonst klappt das mit der gelben Farbe nicht
    if ((boolean) rechnung.getValue())
    {
      rechnungsformular.setEnabled(true);
      rechnungsformular.setMandatory(true);
    }
    else
    {
      rechnungsformular.setMandatory(false);
      rechnungsformular.setEnabled(false);
    }
  }

  public class ZusammenfassenListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      if ((boolean) sollbuchungenzusammenfassen.getValue())
      {
        kompakteabbuchung.setValue(true);
      }
    }
  }

  public class KompaktListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      if (!(boolean) kompakteabbuchung.getValue())
      {
        sollbuchungenzusammenfassen.setValue(false);
      }
    }
  }

  // ======== Buttons ========

  public Button getHelpButton(String link)
  {
    Button b = new Button("Hilfe", new DokumentationAction(), link, false,
        "question-circle.png");
    return b;
  }

  public Button getZahlungsgrundVariablenButton() throws RemoteException
  {
    Button b = new Button("Zahlungsgrund Variablen",
        new ZahlungsgrundVariableDialogAction(), null, false, "bookmark.png");
    return b;
  }

  public Button getRechnungstextVariablenButton() throws RemoteException
  {
    Button b = new Button("Rechnungstext Variablen",
        new RechnungVariableDialogAction(), null, false, "bookmark.png");
    return b;
  }

  public Button getPruefenButton()
  {
    Button b = new Button("Auf Probleme prüfen", context -> {
      try
      {
        status.setValue("");
        String error = checkInput();
        if (error != null)
        {
          bugsList.removeAll();
          bugsList.addItem(new Bug(null, error, Bug.ERROR));
          return;
        }
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

  public Button getStartButton(AbstractDialog<Boolean> dialog)
  {
    Button button = new Button("Starten", new Action()
    {
      @SuppressWarnings("unchecked")
      @Override
      public void handleAction(Object context)
      {
        try
        {
          status.setValue("");
          String error = checkInput();
          if (error != null)
          {
            bugsList.removeAll();
            bugsList.addItem(new Bug(null, error, Bug.ERROR));
            status.setValue("Es Existieren Warnungen/Fehler, bitte beheben!");
            status.setColor(Color.ERROR);
            return;
          }
          refreshBugsList();

          // Prüfen ob Error oder Warning vorliegen
          for (Bug bug : (List<Bug>) bugsList.getItems())
          {
            if (bug.getKlassifikation() != Bug.HINT)
            {
              status.setValue("Es Existieren Warnungen/Fehler, bitte beheben!");
              status.setColor(Color.ERROR);
              return;
            }
          }
          saveSettings();
          new AbrechnungSEPA(getAbrechnungSEPAParam());
          dialog.close();
        }
        catch (ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (RemoteException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  public Button getAbbrechenButton(AbstractDialog<Boolean> dialog)
      throws RemoteException
  {
    Button b = new Button("Abbrechen", context -> {
      dialog.close();
    }, null, false, "process-stop.png");
    return b;
  }

  public JVereinTablePart getBugsList()
  {
    if (bugsList != null)
    {
      return bugsList;
    }
    bugsList = new JVereinTablePart(getInitialBugs(),
        new BugObjektEditAction());
    bugsList.addColumn("Name", "name");
    bugsList.addColumn("Meldung", "meldung");
    bugsList.addColumn("Klassifikation", "klassifikationText");
    bugsList.setContextMenu(new BugListMenu());
    bugsList.setRememberColWidths(true);
    bugsList.setRememberOrder(true);
    return bugsList;
  }

  protected void refreshBugsList() throws RemoteException
  {
    bugsList.removeAll();
    for (Bug bug : getBugs())
    {
      bugsList.addItem(bug);
    }
    bugsList.sort();
  }

  protected List<Bug> getInitialBugs()
  {
    String error = checkInput();
    if (error != null)
    {
      ArrayList<Bug> bugs = new ArrayList<>();
      bugs.add(new Bug(null, error, Bug.ERROR));
      return bugs;
    }
    else
    {
      return getBugs();
    }
  }

  /**
   * Prüfung des Verrechnungskontos.
   * 
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkVerrechnungskonto(ArrayList<Bug> bugs) throws RemoteException
  {
    if (Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID) == null)
    {
      bugs.add(new Bug(null,
          "Verrechnungskonto nicht gesetzt. Unter Administration->Einstellungen->Abrechnung erfassen.",
          Bug.ERROR));
    }
    else
    {
      try
      {
        Konto k = Einstellungen.getDBService().createObject(Konto.class,
            Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID)
                .toString());
        if (k == null)
        {
          bugs.add(new Bug(null,
              "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.",
              Bug.ERROR));
        }
      }
      catch (ObjectNotFoundException ex)
      {
        bugs.add(new Bug(null,
            "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.",
            Bug.ERROR));
      }
    }
  }

  /**
   * Prüfung der Vereinsdaten.
   * 
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkGlobal(ArrayList<Bug> bugs) throws RemoteException
  {
    if (Einstellungen.getEinstellung(Property.NAME) == null
        || ((String) Einstellungen.getEinstellung(Property.NAME)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Name des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.ERROR));
    }

    if (Einstellungen.getEinstellung(Property.IBAN) == null
        || ((String) Einstellungen.getEinstellung(Property.IBAN)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Die IBAN des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.ERROR));
    }
    else
    {
      try
      {
        new IBAN((String) Einstellungen.getEinstellung(Property.IBAN));
      }
      catch (SEPAException e)
      {
        bugs.add(new Bug(null,
            "Ungültige IBAN des Vereins. Unter "
                + "Administration->Einstellungen->Allgemein erfassen.",
            Bug.ERROR));
      }
    }

    if (Einstellungen.getEinstellung(Property.BIC) == null
        || ((String) Einstellungen.getEinstellung(Property.BIC)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Die BIC des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.HINT));
    }
    else
    {
      try
      {
        new BIC((String) Einstellungen.getEinstellung(Property.BIC));
      }
      catch (SEPAException e)
      {
        bugs.add(new Bug(null,
            "Ungültige BIC des Vereins. Unter "
                + "Administration->Einstellungen->Allgemein erfassen.",
            Bug.ERROR));
      }
    }
  }

  /**
   * Prüfung der Gläubiger Id.
   * 
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkGlaeubigerId(ArrayList<Bug> bugs) throws RemoteException
  {
    if (Einstellungen.getEinstellung(Property.GLAEUBIGERID) == null
        || ((String) Einstellungen.getEinstellung(Property.GLAEUBIGERID))
            .length() == 0)
    {
      bugs.add(new Bug(null,
          "Gläubiger-ID fehlt. Gfls. unter https://extranet.bundesbank.de/scp/ beantragen\n"
              + " und unter Administration->Einstellungen->Allgemein eintragen.\n"
              + "Zu Testzwecken kann DE98ZZZ09999999999 eingesetzt werden.",
          Bug.ERROR));
    }
  }

  /**
   * Prüft den Fälligkeitstermin.
   * 
   * @param faelligkeit
   *          Fälligkeit der Forderungen
   * @param bugs
   *          Die Bugliste
   */
  public void checkFaelligkeit(Date faelligkeit, ArrayList<Bug> bugs)
  {
    if (faelligkeit != null && faelligkeit.before(new Date()))
    {
      bugs.add(new Bug(null,
          "Fälligkeit muss bei Lastschriften in der Zukunft liegen!",
          Bug.ERROR));
    }
  }

  /**
   * Prüft ob die nötigen Eingabeparameter des Mitglieds gesetzt sind.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param bugs
   *          Die Bugliste
   * @return Sagt, ob ein Fehler erkannt wurde
   * @throws RemoteException
   */
  public boolean checkMitgliedBeitraege(Mitglied m, ArrayList<Bug> bugs)
      throws RemoteException
  {
    boolean result = true;

    if (m.getZahler().getZahlungsweg() == null)
    {
      bugs.add(
          new Bug(m.getZahler(), "Zahlungsweg ist nicht gesetzt!", Bug.ERROR));
      result = false;
    }

    switch (beitragsmodel)
    {
      case MONATLICH12631:
        if (m.getZahlungsrhythmus() == null)
        {
          bugs.add(new Bug(m, "Zahlungsrythmus ist nicht gesetzt!", Bug.ERROR));
          result = false;
        }
      case GLEICHERTERMINFUERALLE:
        Double betrag = m.getBeitragsgruppe().getBetrag();
        if (betrag == null)
        {
          bugs.add(new Bug(m, "Betrag in Beitragsgruppe ist nicht gesetzt!",
              Bug.ERROR));
          result = false;
        }
        break;
      case FLEXIBEL:
        if (m.getBeitragsgruppe().getBetragMonatlich() == null
            || m.getBeitragsgruppe().getBetragVierteljaehrlich() == null
            || m.getBeitragsgruppe().getBetragHalbjaehrlich() == null
            || m.getBeitragsgruppe().getBetragJaehrlich() == null)
        {
          bugs.add(new Bug(m, "Beträge in Beitragsgruppe sind nicht gesetzt!",
              Bug.ERROR));
          result = false;
        }
        if (m.getZahlungstermin() == null)
        {
          bugs.add(new Bug(m, "Zahlungstermin ist nicht gesetzt!", Bug.ERROR));
          result = false;
        }
        break;
    }
    return result;
  }

  /**
   * Prüft für eine Lastschrift auf gültige Kontodaten des Mitglieds.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkMitgliedKontodaten(Mitglied m, ArrayList<Bug> bugs)
      throws RemoteException
  {
    if (m.getMandatDatum().equals(Einstellungen.NODATE))
    {
      bugs.add(new Bug(m, "Für die Basislastschrift fehlt das Mandatsdatum!",
          Bug.ERROR));
    }
    else if (m.getMandatDatum().after(new Date()))
    {
      bugs.add(new Bug(m, "Das Mandatsdatum liegt in der Zukunft!", Bug.ERROR));
    }

    if (m.getIban() == null || m.getIban().isEmpty())
    {
      bugs.add(
          new Bug(m, "Für die Basislastschrift fehlt die IBAN!", Bug.ERROR));
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
    }

    if (m.getBic() == null || m.getBic().isEmpty())
    {
      bugs.add(new Bug(m, "Für die Basislastschrift fehlt die BIC!", Bug.HINT));
    }
    else
    {
      try
      {
        new BIC(m.getBic());
      }
      catch (Exception e)
      {
        bugs.add(new Bug(m, "Ungültige BIC " + m.getBic(), Bug.ERROR));
      }
    }
  }

  /**
   * Prüft das SEPA Mandat des Mitglieds auf Gültigkeit.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkSEPA(Mitglied m, ArrayList<Bug> bugs) throws RemoteException
  {
    if (!m.getMandatDatum().equals(Einstellungen.NODATE))
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

  protected void saveSettings()
  {
    try
    {
      if (ausgabe != null)
      {
        Abrechnungsausgabe aa = (Abrechnungsausgabe) ausgabe.getValue();
        settings.setAttribute("abrechnungsausgabe", aa.getKey());
      }
      if (kompakteabbuchung != null)
      {
        settings.setAttribute("kompakteabbuchung",
            (Boolean) kompakteabbuchung.getValue());
      }
      if (sollbuchungenzusammenfassen != null)
      {
        settings.setAttribute("sollbuchungenzusammenfassen",
            (Boolean) sollbuchungenzusammenfassen.getValue());
      }
      if (sepaprint != null)
      {
        settings.setAttribute("sepaprint", (Boolean) sepaprint.getValue());
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        if (rechnung != null)
        {
          settings.setAttribute("rechnung", (Boolean) rechnung.getValue());
        }
        if (rechnungsdokumentspeichern != null)
        {
          settings.setAttribute("rechnungsdokumentspeichern",
              (Boolean) rechnungsdokumentspeichern.getValue());
        }
        if (rechnungstext != null)
        {
          settings.setAttribute("rechnungstext",
              (String) rechnungstext.getValue());
        }
        if (rechnungsformular != null)
        {
          settings.setAttribute("rechnungsformular",
              rechnungsformular.getValue() == null ? null
                  : ((Formular) rechnungsformular.getValue()).getID());
        }
        if (rechnungskommentar != null)
        {
          settings.setAttribute("rechnungskommentar",
              (String) rechnungskommentar.getValue());
        }
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler", re);
    }
  }

  private class ZahlungsgrundVariableDialogAction implements Action
  {

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        Map<String, Object> zmap = new AllgemeineMap().getMap(null);
        zmap = new AbrechnungsParameterMap().getMap(getAbrechnungSEPAParam(),
            zmap);
        zmap = MitgliedMap.getDummyMap(zmap);
        new InsertVariableDialogAction(zmap).handleAction(null);
      }
      catch (RemoteException re)
      {
        //
      }
    }
  }

  private class RechnungVariableDialogAction implements Action
  {

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      try
      {
        Map<String, Object> rmap = new AllgemeineMap().getMap(null);
        rmap = new AbrechnungsParameterMap().getMap(getAbrechnungSEPAParam(),
            rmap);
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

  protected AbrechnungSEPAParam getAbrechnungSEPAParam()
      throws ApplicationException
  {
    SepaVersion sepaVersion = null;
    try
    {
      sepaVersion = SepaVersion
          .byURN((String) Einstellungen.getEinstellung(Property.SEPAVERSION));
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
    final AbrechnungSEPAParam abupar;
    try
    {
      abupar = getSEPAParam(sepaVersion);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
    return abupar;
  }

  protected abstract List<Bug> getBugs();

  protected abstract AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException;

  protected abstract String checkInput();
}
