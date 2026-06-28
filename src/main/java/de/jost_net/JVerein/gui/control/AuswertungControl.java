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
import java.io.FilenameFilter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.dialogs.ExporterExportDialog;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.view.AuswertungVorlagenCsvView;
import de.jost_net.JVerein.io.ExportLayoutParam;
import de.jost_net.JVerein.io.Exporter;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.IOFormat;
import de.jost_net.JVerein.io.AuswertungMitgliedAdressbuchCSV;
import de.jost_net.JVerein.io.AuswertungMitgliedAdresslistePDF;
import de.jost_net.JVerein.io.AuswertungMitgliedCSV;
import de.jost_net.JVerein.io.AuswertungMitgliedPDF;
import de.jost_net.JVerein.io.AuswertungKursteilnehmerPDF;
import de.jost_net.JVerein.io.AuswertungMitgliederStatistikPDF;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungControl extends FilterControl
{

  // Mitglied und Nicht-Mitglied Auswertung
  private SelectInput ausgabe;

  private TextInput auswertungUeberschrift = null;

  private SelectInput sortierung;

  private FileInput vorlagedateicsv;

  // Jahrgangsstatistik und Jubiläen
  private SelectInput jubeljahr;

  private int jjahr = 0;

  // Allgemein
  private MitgliedAuswahl mitgliedAuswahl = MitgliedAuswahl.MITGLIEDER;

  private boolean open = true;

  public AuswertungControl(AbstractView view)
  {
    super(view);
  }

  // --------- Inputs ---------

  public SelectInput getAusgabe() throws RemoteException
  {
    if (ausgabe != null)
    {
      return ausgabe;
    }

    // Hilfsklasse FilenameFilter *.csv
    FilenameFilter csvFilter = new FilenameFilter()
    {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.toLowerCase().endsWith(".csv");
      }
    };

    // Suche alle *.csv Dateien im vorlagencsvverzeichnis
    String vorlagencsvverzeichnis = "";
    String[] vorlagencsvList = {};
    vorlagencsvverzeichnis = (String) Einstellungen
        .getEinstellung(Property.VORLAGENCSVVERZEICHNIS);
    if (vorlagencsvverzeichnis.length() > 0)
    {
      File verzeichnis = new File(vorlagencsvverzeichnis);
      if (verzeichnis.isDirectory())
      {
        vorlagencsvList = verzeichnis.list(csvFilter);
      }
    }

    // erzeuge Auswertungsobjekte
    List<Object> objectList = new ArrayList<>();
    objectList.add(new AuswertungMitgliedPDF());
    objectList.add(new AuswertungMitgliedAdresslistePDF());
    objectList.add(new AuswertungMitgliedCSV());
    objectList.add(new AuswertungMitgliedAdressbuchCSV());

    for (String vorlagecsv : vorlagencsvList)
    {
      objectList.add(new AuswertungMitgliedCSV(
          vorlagencsvverzeichnis + File.separator + vorlagecsv));
    }

    ausgabe = new SelectInput(objectList.toArray(), null);
    ausgabe.setName("Ausgabe");
    return ausgabe;
  }

  public TextInput getAuswertungUeberschrift()
  {
    if (auswertungUeberschrift != null)
    {
      return auswertungUeberschrift;
    }
    auswertungUeberschrift = new TextInput(
        settings.getString("auswertung.ueberschrift", ""));
    auswertungUeberschrift.setName("Überschrift");
    return auswertungUeberschrift;
  }

  public SelectInput getSortierung()
  {
    if (sortierung != null)
    {
      return sortierung;
    }
    String[] sort = { "Name, Vorname", "Eintrittsdatum", "Geburtsdatum",
        "Geburtstagsliste" };
    sortierung = new SelectInput(sort, "Name, Vorname");
    sortierung.setName("Sortierung");
    return sortierung;
  }

  public boolean isSortierungAktiv()
  {
    return sortierung != null;
  }

  public Input getVorlagedateicsv()
  {
    if (vorlagedateicsv != null)
    {
      return vorlagedateicsv;
    }
    String lastValue = settings.getString("auswertung.vorlagedateicsv", "");
    String[] extensions = { "*.csv" };
    vorlagedateicsv = new FileInput(lastValue, false, extensions);
    vorlagedateicsv.setName("Vorlagedatei CSV");
    vorlagedateicsv.setEnabled(false); // default is PDF
    return vorlagedateicsv;
  }

  public SelectInput getJubeljahr()
  {
    if (jubeljahr != null)
    {
      return jubeljahr;
    }
    Calendar cal = Calendar.getInstance();
    jjahr = cal.get(Calendar.YEAR);
    cal.add(Calendar.YEAR, -2);
    Integer[] jubeljahre = new Integer[5];
    for (int i = 0; i < 5; i++)
    {
      jubeljahre[i] = cal.get(Calendar.YEAR);
      cal.add(Calendar.YEAR, 1);
    }
    jubeljahr = new SelectInput(jubeljahre, jubeljahre[2]);
    jubeljahr.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        jjahr = (Integer) jubeljahr.getValue();
      }
    });
    return jubeljahr;
  }

  public int getJJahr()
  {
    return jjahr;
  }

  // --------- Buttons ---------

  public Button getStartAuswertungButton()
  {
    Button b = new Button("Starten", context -> {
      try
      {
        final Exporter exporter = (Exporter) getAusgabe().getValue();
        String sort = null;
        if (isSortierungAktiv() && getSortierung().getValue() != null)
        {
          sort = (String) getSortierung().getValue();
        }
        ArrayList<Mitglied> list = new MitgliedQuery(this)
            .get(MitgliedAuswahl.MITGLIEDER, sort);

        Object[] objects = new Object[] { list.toArray(new Mitglied[0]),
            (String) getAuswertungUeberschrift().getValue(),
            getFilterText(false) };
        starteAuswertung(exporter, objects, view.getClass(), this);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    return b;
  }

  public Button getStartAdressAuswertungButton()
  {
    Button b = new Button("Starten", context -> {
      try
      {
        final Exporter exporter = (Exporter) getAusgabe().getValue();
        String sort = null;
        if (isSortierungAktiv() && getSortierung().getValue() != null)
        {
          sort = (String) getSortierung().getValue();
        }
        ArrayList<Mitglied> list = new MitgliedQuery(this)
            .get(MitgliedAuswahl.NICHTMITGLIEDER, sort);
        Object[] objects = new Object[] { list.toArray(new Mitglied[0]),
            (String) getAuswertungUeberschrift().getValue(),
            getFilterText(false) };
        starteAuswertung(exporter, objects, view.getClass(), this);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    return b;
  }

  public Button getVorlagenCsvEditButton()
  {
    Button b = new Button("CSV Vorlagen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        GUI.startView(AuswertungVorlagenCsvView.class.getName(), null);
      }
    }, null, false, "xsd.png");
    return b;
  }

  public Button getStartStatistikButton()
  {
    Button b = new Button("Starten", context -> {
      try
      {
        starteAuswertung(new AuswertungMitgliederStatistikPDF(),
            new Object[] { getFilter(), "" }, view.getClass(), this);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    return b;
  }

  public Button getStartAuswertungKursteilnehmerButton()
  {
    Button b = new Button("Starten", context -> {
      try
      {
        starteAuswertung(new AuswertungKursteilnehmerPDF(),
            new Object[] { getFilter(), "" }, view.getClass(), this);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    return b;
  }

  // --------- Auswertung ---------

  /**
   * Startet die Ausgabe des Auswerters
   * 
   * @param exporter
   *          Der Exporter
   * @param objects
   *          objects[0] ist das Mitglied[], objects[1] ist der Subtitel,
   *          objects[2] ist der Filter
   * @param type
   *          Die View Klasse
   * @param dateinameObject
   *          Das Objekt für die Evaluierung von Dateiname, Titel und Subtitel
   * @throws ApplicationException
   * @throws RemoteException
   */
  private void starteAuswertung(Exporter exporter, Object[] objects,
      Class<?> type, Object dateinameObject)
      throws ApplicationException, RemoteException
  {
    saveFilterSettings();
    final IOFormat format = exporter.getIOFormats(type)[0];

    String[] se = format.getFileExtensions();
    String ext = se == null ? "" : se[0];
    ext = ext.replaceAll("\\*.", ""); // "*." entfernen
    String prefix = exporter.getName().replaceAll(" ", "-") + ".";

    String subtitle = "";
    if (((String) objects[1]).length() > 0)
    {
      subtitle = (String) objects[1];
    }

    try
    {
      if (ext.equalsIgnoreCase("pdf"))
      {
        if (!new ExporterExportDialog(exporter, objects, format, prefix,
            ExportArt.PDF, exporter.getTitle(dateinameObject), subtitle,
            exporter.getDateiname(dateinameObject), open).open())
        {
          throw new OperationCanceledException();
        }
      }
      else
      {
        FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
        fd.setText(
            "Bitte geben Sie eine Datei ein, in die die Daten exportiert werden sollen.");
        fd.setOverwrite(true);
        fd.setFileName(exporter.getDateiname(dateinameObject));
        fd.setFilterExtensions(new String[] { "*" + ext });
        String path = settings.getString("lastdir",
            System.getProperty("user.home"));
        if (path != null && path.length() > 0)
        {
          fd.setFilterPath(path);
        }
        final String s = fd.open();

        if (s == null || s.length() == 0)
        {
          throw new OperationCanceledException("Abgebrochen");
        }

        final File file = new File(s);

        // Wir merken uns noch das Verzeichnis vom letzten mal
        settings.setAttribute("lastdir", file.getParent());
        ExportLayoutParam param = new ExportLayoutParam();
        param.setTitle(exporter.getTitle(dateinameObject));
        param.setSubtitle(subtitle);

        BackgroundTask t = new BackgroundTask()
        {
          @Override
          public void run(ProgressMonitor monitor) throws ApplicationException
          {
            try
            {
              exporter.doExport(objects, format, file, param, monitor);
              monitor.setPercentComplete(100);
              monitor.setStatus(ProgressMonitor.STATUS_DONE);
              GUI.getStatusBar().setSuccessText(
                  String.format("Daten exportiert nach %s", file.getParent()));
              monitor.setStatusText(
                  String.format("Daten exportiert nach %s", file.getParent()));

              if (open)
              {
                FileViewer.show(file);
              }
            }
            catch (ApplicationException ae)
            {
              GUI.getStatusBar().setErrorText(ae.getMessage());
              throw ae;
            }
            catch (OperationCanceledException oce)
            {
              throw oce;
            }
            catch (Exception e)
            {
              String s = file.getParent();
              Logger.error("error while writing objects to " + s, e);
              ApplicationException ae = new ApplicationException(
                  String.format("Fehler beim Exportieren der Daten in %s", s),
                  e);
              GUI.getStatusBar().setErrorText(ae.getMessage());
              throw ae;
            }
          }

          @Override
          public void interrupt()
          {
            //
          }

          @Override
          public boolean isInterrupted()
          {
            return false;
          }
        };
        Application.getController().start(t);
      }
    }
    catch (OperationCanceledException | ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      String text = "Fehler beim Erstellen des Reports.";
      Logger.error(text, e);
      throw new ApplicationException(text);
    }
  }

  @Override
  public void saveFilterSettings() throws RemoteException
  {

    if (auswertungUeberschrift != null)
    {
      String tmp = (String) getAuswertungUeberschrift().getValue();
      if (tmp != null)
      {
        settings.setAttribute("auswertung.ueberschrift", tmp);
      }
      else
      {
        settings.setAttribute("auswertung.ueberschrift", "");
      }
    }

    if (vorlagedateicsv != null)
    {
      String tmp = (String) getVorlagedateicsv().getValue();
      if (tmp != null)
      {
        settings.setAttribute("auswertung.vorlagedateicsv", tmp);
      }
      else
      {
        settings.setAttribute("auswertung.vorlagedateicsv", "");
      }
    }
    super.saveFilterSettings();
  }

  public void setMitgliedAuswahl(MitgliedAuswahl mitgliedAuswahl)
  {
    this.mitgliedAuswahl = mitgliedAuswahl;
  }

  public MitgliedAuswahl getMitgliedAuswahl()
  {
    return mitgliedAuswahl;
  }

  @Override
  protected String getTableTitle()
  {
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    return null;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Nichts zu tun
  }

  @Override
  protected IJVereinPart getTablePart()
      throws RemoteException, ApplicationException
  {
    return null;
  }
}
