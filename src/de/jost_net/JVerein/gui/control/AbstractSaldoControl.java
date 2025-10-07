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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public abstract class AbstractSaldoControl extends VorZurueckControl
{
  /**
   * Die Art des Eintrags: Header, Detail, Footer (siehe Konstanten)
   */
  public static final String ART = "art";

  /**
   * Summe der Einnahmen.
   */
  public static final String EINNAHMEN = "einnahmen";

  /**
   * Summe der Ausgaben.
   */
  public static final String AUSGABEN = "ausgaben";

  /**
   * Summe der Umbuchungen.
   */
  public static final String UMBUCHUNGEN = "umbuchungen";

  /**
   * Die Buchungsklasse aus der DB
   */
  public static final String BUCHUNGSKLASSE = "buchungsklasse";

  /**
   * Die Buchungsart aus der DB.
   */
  public static final String BUCHUNGSART = "buchungsart";

  /**
   * Anfangsbestand der Konten.
   */
  public static final String ANFANGSBESTAND = "anfangsbestand";

  /**
   * Endbestand der Konten.
   */
  public static final String ENDBESTAND = "endbestand";

  /**
   * Anzahl Einträge
   */
  public static final String ANZAHL = "anzahl";

  /**
   * Die Gruppe die im Saldo an erster Stelle stehen soll (Buchungsklasse,
   * Projekt...)
   */
  public static final String GRUPPE = "gruppe";

  private DateLabel datumvon;

  private DateLabel datumbis;

  private DateInput suchdatumvon;

  protected DateInput suchdatumbis;

  protected TextInput geschaeftsjahr;

  protected Settings settings = null;

  public static final int ART_HEADER = 1;

  public static final int ART_DETAIL = 2;

  public static final int ART_SALDOFOOTER = 3;

  public static final int ART_SALDOGEWINNVERLUST = 4;

  public static final int ART_GESAMTSALDOFOOTER = 5;

  public static final int ART_GESAMTGEWINNVERLUST = 6;

  public static final int ART_NICHTZUGEORDNETEBUCHUNGEN = 7;

  public static final int ART_LEERZEILE = 8;

  final static String AuswertungPDF = ".pdf";

  final static String AuswertungCSV = ".csv";

  public AbstractSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * Erstellt den TablePart mit allen Spalten, die Liste wird aus getList()
   * geholt.
   * 
   * @return der TablePart
   * @throws ApplicationException
   */
  public abstract TablePart getSaldoList() throws ApplicationException;

  /**
   * Liefert die Liste
   * 
   * @return die Liste der Einträge
   * @throws RemoteException
   */
  public abstract ArrayList<PseudoDBObject> getList() throws RemoteException;

  /**
   * Lädt die Liste neu
   * 
   * @throws ApplicationException
   * 
   * @throws RemoteException
   */
  public void reloadList() throws ApplicationException
  {
    try
    {
      // Daten in Einstellungen Speichern
      if (getDatumvon().getDate() != null)
      {
        settings.setAttribute("von",
            new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));
        settings.setAttribute("bis",
            new JVDateFormatTTMMJJJJ().format(getDatumbis().getDate()));
      }

      ArrayList<PseudoDBObject> zeile = getList();
      getSaldoList().removeAll();
      for (PseudoDBObject sz : zeile)
      {
        getSaldoList().addItem(sz);
      }
    }
    catch (RemoteException re)
    {
      throw new ApplicationException("Fehler bei neu laden der Liste.");
    }
  }

  /**
   * Holt den Titel für die Auswertungen
   * 
   * @return
   */
  protected abstract String getAuswertungTitle();

  /**
   * 
   * Holt den Titel für die Auswertungen
   * 
   * @return
   */
  protected abstract String getAuswertungSubtitle();

  /**
   * Holt den Dateinamen für die Auswertungen
   * 
   * @return
   */
  protected abstract String getDateiname();

  /**
   * Git ein Object, dass das Interface ISaldoExport implementiert zurück.
   * 
   * @param type
   *          der type der Auswertung
   * @return
   * @throws ApplicationException
   */
  protected abstract ISaldoExport getAuswertung(String type)
      throws ApplicationException;

  /**
   * Erstellt die Auswertung
   * 
   * @param type
   *          Der Typ der Auswertung (AuswertungPDF, AuswertungCSV)
   * @throws ApplicationException
   */
  private void starteAuswertung(String type) throws ApplicationException
  {
    try
    {
      ArrayList<PseudoDBObject> zeile = getList();

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");

      Settings settings = new Settings(this.getClass());

      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(getDateiname() + type);

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      BackgroundTask t = new BackgroundTask()
      {
        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          ISaldoExport export = getAuswertung(type);
          export.export(zeile, file, getAuswertungTitle(),
              getAuswertungSubtitle());
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
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Aufbau des Reports: %s", e.getMessage()));
    }
  }

  public Button getStartAuswertungPDFButton()
  {
    return new Button("PDF", context -> starteAuswertung(AuswertungPDF), null,
        false, "file-pdf.png");
  }

  public Button getStartAuswertungCSVButton()
  {
    return new Button("CSV", context -> starteAuswertung(AuswertungCSV), null,
        false, "xsd.png");
  }

  public DateLabel getDatumvon()
  {
    if (datumvon != null)
    {
      return datumvon;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("von", "01.01" + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    datumvon = new DateLabel(d);
    datumvon.disable();
    return datumvon;
  }

  public DateLabel getDatumbis()
  {
    if (datumbis != null)
    {
      return datumbis;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("bis", "31.12." + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    datumbis = new DateLabel(d);
    datumbis.disable();
    return datumbis;
  }

  public TextInput getGeschaeftsjahr()
  {
    if (geschaeftsjahr != null)
    {
      return geschaeftsjahr;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(getDatumvon().getDate());
    geschaeftsjahr = new TextInput(
        Integer.valueOf((cal.get(Calendar.YEAR))).toString());
    geschaeftsjahr.disable();
    return geschaeftsjahr;
  }

  public DateInput getSuchDatumvon()
  {
    if (suchdatumvon != null)
    {
      return suchdatumvon;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("von", "01.01" + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    suchdatumvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    return suchdatumvon;
  }

  public DateInput getSuchDatumbis()
  {
    if (suchdatumbis != null)
    {
      return suchdatumbis;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("bis", "31.12." + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    suchdatumbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    return suchdatumbis;
  }

  /**
   * Holt das Geschäftsjahr des Datumbereichs
   * 
   * @return Das Geschäftsjahr, wenn Start und Ende mit der Auswahl
   *         übereinstimmen, sonst 0.
   */
  public Integer isGeschaeftsjahr()
  {
    try
    {
      Integer year;
      Calendar cal = Calendar.getInstance();
      Date von = getDatumvon().getDate();
      Date bis = getDatumbis().getDate();
      cal.setTime(von);
      year = cal.get(Calendar.YEAR);
      Date gjvon = Datum.toDate(
          (String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR)
              + year);
      if (!von.equals(gjvon))
      {
        return 0;
      }
      cal.add(Calendar.YEAR, 1);
      cal.add(Calendar.DAY_OF_MONTH, -1);
      if (bis.equals(cal.getTime()))
      {
        return year;
      }
    }
    catch (Exception e)
    {
      //
    }
    return 0;
  }

  /**
   * Textinput mit formatiertem Datum.
   */
  public class DateLabel extends TextInput
  {
    private Date d;

    DateLabel(Date date)
    {
      super("");
      setDate(date);
    }

    public void setDate(Date date)
    {
      d = date;
      JVDateFormatTTMMJJJJ df = new JVDateFormatTTMMJJJJ();
      String dstring = d == null ? "" : df.format(d);
      super.setValue(dstring);
    }

    public Date getDate()
    {
      return d;
    }
  }
}
