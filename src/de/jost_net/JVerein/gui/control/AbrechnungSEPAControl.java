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

import java.io.File;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.input.AbbuchungsmodusInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.io.AbrechnungSEPA;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.io.Bankarbeitstage;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AbrechnungSEPAControl extends AbstractControl
{

  private static String CONFIRM_TITEL = "SEPA-Check temporär deaktivieren";

  private static String CONFIRM_TEXT = "Bei einer SEPA-Lastschrift muß ein gültiges SEPA-Mandat vorliegen.\n"
      + "Wenn das Mandat älter als 3 Jahre ist, müssen in den letzten 3 Jahren Lastschriften durchgeführt worden sein.\n"
      + "Wählen Sie \"Ja\" nur, wenn diese Bedingungen für alle Mitglieder erfüllt sind.";

  private static String CONFIRM_FEHLER = "Fehler beim Setzen der Checkbox";

  private AbbuchungsmodusInput modus;

  private DateInput stichtag = null;

  private SelectInput abrechnungsmonat;

  private DateInput faelligkeit = null;

  private DateInput vondatum = null;

  private DateInput bisdatum = null;

  private TextInput zahlungsgrund;

  private CheckboxInput zusatzbetrag;

  private CheckboxInput kursteilnehmer;

  private CheckboxInput kompakteabbuchung;

  private CheckboxInput sepaprint;

  private CheckboxInput sepacheck;

  private SelectInput ausgabe;

  private Settings settings = null;

  private CheckboxInput sollbuchungenzusammenfassen;

  private CheckboxInput rechnung;

  private FormularInput rechnungsformular;

  private TextInput rechnungstext;

  private DateInput rechnungsdatum;

  private DateInput voneingabedatum;

  public AbrechnungSEPAControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public SelectInput getAbrechnungsmonat()
  {
    if (abrechnungsmonat != null)
    {
      return abrechnungsmonat;
    }
    abrechnungsmonat = new SelectInput(Monat.values(), Monat.JANUAR);
    return abrechnungsmonat;
  }

  public AbbuchungsmodusInput getAbbuchungsmodus() throws RemoteException
  {
    if (modus != null)
    {
      return modus;
    }
    Integer mod = settings.getInt("modus", Abrechnungsmodi.KEINBEITRAG);

    modus = new AbbuchungsmodusInput(mod);
    modus.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        Integer m = ((Integer) modus.getValue());
        if (m.intValue() != Abrechnungsmodi.EINGETRETENEMITGLIEDER)
        {
          vondatum.setValue(null);
          vondatum.setEnabled(false);
          voneingabedatum.setValue(null);
          voneingabedatum.setEnabled(false);
        }
        else
        {
          vondatum.setEnabled(true);
          vondatum.setValue(new Date());
          voneingabedatum.setValue(new Date());
          voneingabedatum.setEnabled(true);
        }
        if (m.intValue() != Abrechnungsmodi.ABGEMELDETEMITGLIEDER)
        {
          bisdatum.setValue(null);
          bisdatum.setEnabled(false);
        }
        else
        {
          bisdatum.setEnabled(true);
          bisdatum.setValue(new Date());
        }
      }
    });
    return modus;
  }

  public DateInput getStichtag()
  {
    if (stichtag != null)
    {
      return stichtag;
    }
    this.stichtag = new DateInput(null, new JVDateFormatTTMMJJJJ());
    this.stichtag.setTitle("Stichtag für die Abrechnung");
    this.stichtag.setText("Bitte Stichtag für die Abrechnung wählen");
    return stichtag;
  }

  public DateInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }
    Calendar cal = Calendar.getInstance();
    Bankarbeitstage bat = new Bankarbeitstage();
    cal = bat.getCalendar(cal,
        1 + Einstellungen.getEinstellung().getSEPADatumOffset());
    this.faelligkeit = new DateInput(cal.getTime(),
        new JVDateFormatTTMMJJJJ());
    this.faelligkeit.setTitle("Fälligkeit");
    this.faelligkeit.setText(
        "Bitte Fälligkeitsdatum wählen");
    faelligkeit.addListener(event -> {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      if (faelligkeit.getValue() != null && getStichtag() != null
          && getStichtag().getValue() == null)
      {
        getStichtag().setValue(faelligkeit.getValue());
      }
    });
    return faelligkeit;
  }

  public DateInput getVondatum()
  {
    if (vondatum != null)
    {
      return vondatum;
    }
    this.vondatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    this.vondatum.setTitle("Anfangsdatum Abrechnung");
    this.vondatum.setText("Bitte Anfangsdatum der Abrechnung wählen");
    this.vondatum.setEnabled(
        (Integer) modus.getValue() == Abrechnungsmodi.EINGETRETENEMITGLIEDER);
    return vondatum;
  }

  public DateInput getVonEingabedatum()
  {
    if (voneingabedatum != null)
    {
      return voneingabedatum;
    }
    this.voneingabedatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    this.voneingabedatum.setEnabled(
        (Integer) modus.getValue() == Abrechnungsmodi.EINGETRETENEMITGLIEDER);
    return voneingabedatum;
  }

  public DateInput getBisdatum()
  {
    if (bisdatum != null)
    {
      return bisdatum;
    }
    this.bisdatum = new DateInput(null, new JVDateFormatTTMMJJJJ());
    this.bisdatum.setTitle("Enddatum Abrechnung");
    this.bisdatum
        .setText("Bitte maximales Austrittsdatum für die Abrechnung wählen");
    this.bisdatum.setEnabled(
        (Integer) modus.getValue() == Abrechnungsmodi.ABGEMELDETEMITGLIEDER);
    return bisdatum;
  }

  public TextInput getZahlungsgrund()
  {
    if (zahlungsgrund != null)
    {
      return zahlungsgrund;
    }
    String zgrund = settings.getString("zahlungsgrund", "bitte eingeben");

    zahlungsgrund = new TextInput(zgrund, 140);
    return zahlungsgrund;
  }

  public CheckboxInput getZusatzbetrag()
  {
    if (zusatzbetrag != null)
    {
      return zusatzbetrag;
    }
    zusatzbetrag = new CheckboxInput(
        settings.getBoolean("zusatzbetraege", false));
    return zusatzbetrag;
  }

  public CheckboxInput getKursteilnehmer()
  {
    if (kursteilnehmer != null)
    {
      return kursteilnehmer;
    }
    kursteilnehmer = new CheckboxInput(
        settings.getBoolean("kursteilnehmer", false));
    return kursteilnehmer;
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


  public CheckboxInput getRechnung()
  {
    if (rechnung != null)
    {
      return rechnung;
    }
    rechnung = new CheckboxInput(
        settings.getBoolean("rechnung", false));
    rechnung.addListener(new RechnungListener());
    return rechnung;
  }
  
  public FormularInput getRechnungFormular() throws RemoteException
  {
    if (rechnungsformular != null)
    {
      return rechnungsformular;
    }
    rechnungsformular = new FormularInput(
        FormularArt.RECHNUNG, settings.getString("rechnungsformular", ""));
    rechnungsformular.setEnabled(settings.getBoolean("rechnung", false));
    return rechnungsformular;
  }

  public TextInput getRechnungstext()
  {
    if (rechnungstext != null)
    {
      return rechnungstext;
    }
    rechnungstext = new TextInput(
        settings.getString("rechnungstext", "RE$rechnung_nummer"));
    rechnungstext.setEnabled(settings.getBoolean("rechnung", false));
    return rechnungstext;
  }
  
  public DateInput getRechnungsdatum()
  {
    if (rechnungsdatum != null)
    {
      return rechnungsdatum;
    }
    rechnungsdatum = new DateInput(new Date());
    rechnungsdatum.setEnabled(settings.getBoolean("rechnung", false));
    return rechnungsdatum;
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
    return ausgabe;
  }

  public Button getStartButton()
  {
    Button button = new Button("Starten", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        try
        {
          doAbrechnung();
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

  private void doAbrechnung() throws ApplicationException, RemoteException
  {
    settings.setAttribute("modus",
        (Integer) modus.getValue());
    settings.setAttribute("zahlungsgrund", (String) zahlungsgrund.getValue());
    if (zusatzbetrag != null)
    {
      settings.setAttribute("zusatzbetraege",
          (Boolean) zusatzbetrag.getValue());
    }
    if (kursteilnehmer != null)
    {
      settings.setAttribute("kursteilnehmer",
          (Boolean) kursteilnehmer.getValue());
    }
    settings.setAttribute("kompakteabbuchung",
        (Boolean) kompakteabbuchung.getValue());
    settings.setAttribute("sollbuchungenzusammenfassen",
        (Boolean) sollbuchungenzusammenfassen.getValue());
    settings.setAttribute("rechnung",
        (Boolean) rechnung.getValue());
    settings.setAttribute("rechnungstext",
        (String) rechnungstext.getValue());
    settings.setAttribute("rechnungsformular",
        rechnungsformular.getValue() == null ? null
            : ((Formular) rechnungsformular.getValue()).getID());
    settings.setAttribute("sepaprint", (Boolean) sepaprint.getValue());
    Abrechnungsausgabe aa = (Abrechnungsausgabe) this.getAbbuchungsausgabe().getValue();
    settings.setAttribute("abrechnungsausgabe", aa.getKey());
    Integer modus = null;
    try
    {
      modus = (Integer) getAbbuchungsmodus().getValue();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Interner Fehler - kann Abrechnungsmodus nicht auslesen");
    }
    if (faelligkeit.getValue() == null)
    {
      throw new ApplicationException("Fälligkeitsdatum fehlt");
    }
    Date vondatum = null;
    if (stichtag.getValue() == null)
    {
      throw new ApplicationException("Stichtag fehlt");
    }
    if (modus != Abrechnungsmodi.KEINBEITRAG)
    {
      vondatum = (Date) getVondatum().getValue();
      if (modus == Abrechnungsmodi.EINGETRETENEMITGLIEDER && vondatum == null
          && getVonEingabedatum().getValue() == null)
      {
        throw new ApplicationException("von-Datum fehlt");
      }
      Date bisdatum = (Date) getBisdatum().getValue();
      if (modus == Abrechnungsmodi.ABGEMELDETEMITGLIEDER && bisdatum == null)
      {
        throw new ApplicationException("bis-Datum fehlt");
      }
    }
    File sepafilercur = null;
    SepaVersion sepaVersion = null;
    if (aa == Abrechnungsausgabe.SEPA_DATEI)
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("SEPA-Ausgabedatei wählen.");
      String path = settings.getString("lastdir.sepa",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("abbuchungRCUR", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), "XML").get());
      String file = fd.open();
      if (file == null || file.length() == 0)
      {
        throw new ApplicationException("Keine Datei ausgewählt!");
      }
      sepafilercur = new File(file);
      // Wir merken uns noch das Verzeichnis fürs nächste mal
      settings.setAttribute("lastdir.sepa", sepafilercur.getParent());
      try
      {
        sepaVersion = Einstellungen.getEinstellung().getSepaVersion();
      }
      catch (Exception e)
      {
        throw new ApplicationException(e);
      }
    }

    // PDF-Datei für Basislastschrift2PDF
    String pdffileRCUR = null;
    final Boolean pdfprintb = (Boolean) sepaprint.getValue();
    if (pdfprintb)
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("PDF-Ausgabedatei wählen");

      String path = settings.getString("lastdir.pdf",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("abbuchungRCUR", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), "PDF").get());
      pdffileRCUR = fd.open();
      File file = new File(pdffileRCUR);
      // Wir merken uns noch das Verzeichnis fürs nächste mal
      settings.setAttribute("lastdir.pdf", file.getParent());
    }

    {
      final AbrechnungSEPAParam abupar;
      try
      {
        abupar = new AbrechnungSEPAParam(this, sepafilercur, sepaVersion, pdffileRCUR);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      BackgroundTask t = new BackgroundTask()
      {
        private boolean interrupt = false;

        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          try
          {

            DBTransaction.starten();            
            new AbrechnungSEPA(abupar, monitor, this);
            DBTransaction.commit();

            monitor.setPercentComplete(100);
            monitor.setStatus(ProgressMonitor.STATUS_DONE);
            GUI.getStatusBar().setSuccessText("Abrechnung durchgeführt" + abupar.getText());

          }
          catch (ApplicationException ae)
          {
            DBTransaction.rollback();
            GUI.getStatusBar().setErrorText(ae.getMessage());
            throw ae;
          }
          catch (Exception e)
          {
            DBTransaction.rollback();
            ApplicationException ae;
            if (abupar.abbuchungsausgabe == Abrechnungsausgabe.SEPA_DATEI)
            {
              Logger.error(String.format("error while creating %s", abupar.sepafileRCUR.getAbsolutePath()), e);
              ae = new ApplicationException(String.format("Fehler beim Erstellen der Abbuchungsdatei: %s", abupar.sepafileRCUR.getAbsolutePath()), e);
            } 
            else if (abupar.abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
            {
              Logger.error("error while creating debit in Hibiscus", e);
              ae = new ApplicationException("Fehler beim Erstellen der Hibiscus-Lastschrift", e);
            } 
            else
            {
              Logger.error("error during operation", e);
              ae = new ApplicationException("Fehler beim Abrechnungslauf", e);
            }
            GUI.getStatusBar().setErrorText(ae.getMessage());
            throw ae;
          }
        }

        @Override
        public void interrupt()
        {
          interrupt = true;
        }

        @Override
        public boolean isInterrupted()
        {
          return interrupt;
        }
      };
      Application.getController().start(t);
    }
  }

  public class RechnungListener implements Listener
  {

    RechnungListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      rechnungsformular.setEnabled((boolean) rechnung.getValue());
      rechnungstext.setEnabled((boolean) rechnung.getValue());
      rechnungsdatum.setEnabled((boolean) rechnung.getValue());
    }
  }

  public class ZusammenfassenListener implements Listener
  {

    ZusammenfassenListener()
    {
    }

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

    KompaktListener()
    {
    }

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
}
