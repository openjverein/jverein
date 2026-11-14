/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.BuchungMap;
import de.jost_net.JVerein.gui.dialogs.BuchungReportExportDialog;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.io.BuchungReportAusgabe;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungReportExportDialogControl
{
  private Settings settings = null;

  private ArrayList<Buchung> buchungen;

  private FormularInput formularInput;

  private DateInput dateInput;

  private LabelInput statusLabel;

  private CheckboxInput alsBuDokumentInput;

  private Button uebernehmenButton;

  private BuchungReportExportDialog dialog;

  private BuchungsControl control;

  public BuchungReportExportDialogControl(BuchungReportExportDialog dialog,
      Buchung[] buchungen, BuchungsControl control)
  {
    this.buchungen = new ArrayList<Buchung>(Arrays.asList(buchungen));
    this.dialog = dialog;
    this.control = control;
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public FormularInput getFormular() throws RemoteException
  {
    if (formularInput != null)
    {
      return formularInput;
    }
    formularInput = new FormularInput(FormularArt.BUCHUNGSREPORT,
        settings.getString("formular.key", ""));
    formularInput.setName("Formular");
    return formularInput;
  }

  public CheckboxInput getBuchungsdokumentSpeichern()
  {
    if (alsBuDokumentInput != null)
    {
      return alsBuDokumentInput;
    }
    alsBuDokumentInput = new CheckboxInput(
        settings.getBoolean("buchungsdokumentspeichern", false));
    alsBuDokumentInput.addListener(event -> {
      if ((Boolean) getBuchungsdokumentSpeichern().getValue())
      {
        dateInput.enable();
      }
      else
      {
        dateInput.disable();
      }
    });
    return alsBuDokumentInput;
  }

  public Input getDatum() throws RemoteException
  {
    if (dateInput != null)
    {
      return dateInput;
    }
    dateInput = new DateInput(new Date());
    dateInput.setName("Datum");
    if (!(Boolean) getBuchungsdokumentSpeichern().getValue())
    {
      dateInput.disable();
    }
    return dateInput;
  }

  public LabelInput getStatus()
  {
    if (statusLabel != null)
    {
      return statusLabel;
    }
    statusLabel = new LabelInput("");
    statusLabel.setName("");
    return statusLabel;
  }

  public Button getUebernehmenButton() throws RemoteException
  {
    if (uebernehmenButton != null)
    {
      return uebernehmenButton;
    }
    uebernehmenButton = new Button("Übernehmen", context -> {
      try
      {
        if (getFormular().getValue() == null)
        {
          statusLabel.setValue("Bitte Formular auswählen");
          statusLabel.setColor(Color.ERROR);
          return;
        }

        saveSettings();
        if ((Boolean) getBuchungsdokumentSpeichern().getValue()
            && (Boolean) Einstellungen
                .getEinstellung(Property.DOKUMENTENSPEICHERUNG))
        {
          storeBuchungsdokumente();
        }
        else
        {
          new BuchungReportAusgabe((Formular) getFormular().getValue())
              .aufbereiten(buchungen, Ausgabeart.PDF_EINZELN, null, null, false,
                  false, false);
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler bei der Buchungreport Ausgabe.", e);
        throw new ApplicationException(e.getMessage());
      }
      dialog.close();
    }, null, true, "ok.png");
    return uebernehmenButton;
  }

  private void storeBuchungsdokumente()
  {
    if (buchungen.size() == 1)
    {
      try
      {
        storeBuchungsDokument((Formular) getFormular().getValue(),
            buchungen.get(0), (Date) getDatum().getValue());
        GUI.getStatusBar().setSuccessText("Buchungsdokument generiert.");
        if (control != null)
        {
          control.refreshBuchungsList();
        }
      }
      catch (RemoteException e)
      {
        GUI.getStatusBar()
            .setErrorText("Fehler bei der Buchungsdokument Generierung.");
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }
    else
    {
      BackgroundTask t = new BackgroundTask()
      {
        private boolean interrupted;

        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          int count = 0;
          int skip = 0;
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setStatusText("Generiere " + buchungen.size() + " Dokumente");

          for (Buchung bu : buchungen)
          {
            if (isInterrupted())
            {
              throw new OperationCanceledException();
            }

            String id = "";
            try
            {
              id = bu.getID();
              storeBuchungsDokument((Formular) getFormular().getValue(), bu,
                  (Date) getDatum().getValue());
              count++;
            }
            catch (ApplicationException ex)
            {
              skip++;
              monitor.setStatusText("Buchung " + id + ": " + ex.getMessage());
            }
            catch (RemoteException e)
            {
              skip++;
              Logger.error(e.getMessage(), e);
              monitor.setStatusText("Buchung " + id + ": " + e.getMessage());
            }

            monitor.setPercentComplete(100 * (count + skip) / buchungen.size());
          }

          String text = String.format(
              "%d Dokument" + (count != 1 ? "e" : "") + " erzeugt.", count);
          monitor.setStatusText(text);
          monitor.setStatusText(String.format(
              "%d Buchung" + (skip != 1 ? "en" : "") + " übersprungen.", skip));
          GUI.getStatusBar().setSuccessText(text);

          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText("Dokumentgenerierung abgeschlossen");
          GUI.getDisplay().syncExec(() -> {
            try
            {
              if (control != null)
              {
                control.refreshBuchungsList();
              }
            }
            catch (ApplicationException e)
            {
              Logger.error("Fehler beim neuladen der Liste.", e);
            }
          });
        }

        @Override
        public void interrupt()
        {
          interrupted = true;
        }

        @Override
        public boolean isInterrupted()
        {
          return interrupted;
        }

      };
      Application.getController().start(t);
    }
  }

  private void storeBuchungsDokument(Formular formular, Buchung buchung,
      Date datum) throws ApplicationException
  {
    if (buchung != null)
    {
      File file = null;
      try
      {
        // Belegnummernummer erstellen
        String zaehler = Einstellungen
            .getEinstellung(Property.BELEG_ZAEHLER_BUCHUNG).toString();
        String nummer = (String) Einstellungen
            .getEinstellung(Property.BELEGNUMMER_BUCHUNG);
        Map<String, Object> map = new AllgemeineMap().getMap(null);
        map.put("beleg_zaehler", zaehler);
        map.put("beleg_nummer", VelocityTool.eval(map, nummer));
        map = new BuchungMap().getMap(buchung, map);

        // PDF erstellen
        String dateiname = VorlageUtil.getName(
            VorlageTyp.BUCHUNGSREPORT_DATEINAME, buchung,
            formular.getBezeichnung()) + ".pdf";
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        file = new File(tempDir, dateiname);
        FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
            false);
        aufbereitung.writeForm(formular, map);
        // ggf. hochgezählten Zähler speichern
        formular.store();
        aufbereitung.closeFormular();

        Beleg doc = Einstellungen.getDBService().createObject(Beleg.class,
            null);
        doc.setBemerkung(file.getName());
        doc.setDatum(datum);
        doc.setBelegArt(BelegArt.BUCHUNG);
        doc.setFile(file);
        doc.store();

        buchung.addBeleg(doc);
      }
      catch (IOException | DocumentException e)
      {
        String text = "Fehler beim Speichern des Buchungsreports als Buchungsdokument.";
        Logger.error(text, e);
        throw new ApplicationException(text);
      }
      finally
      {
        if (file != null)
        {
          file.delete();
        }
      }
    }
  }

  private void saveSettings()
  {
    {
      Formular f;
      try
      {
        if (getFormular() != null)
        {
          f = (Formular) getFormular().getValue();
          settings.setAttribute("formular.key", f == null ? "" : f.getID());
        }
        if (alsBuDokumentInput != null)
        {
          settings.setAttribute("buchungsdokumentspeichern",
              (Boolean) alsBuDokumentInput.getValue());
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Speichern der Settings.", e);
      }
    }
  }
}
