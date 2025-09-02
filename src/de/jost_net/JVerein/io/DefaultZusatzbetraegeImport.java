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
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class DefaultZusatzbetraegeImport implements Importer
{

  @Override
  public void doImport(Object context, IOFormat format, File file,
      String encoding, ProgressMonitor monitor) throws ApplicationException
  {
    monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
    ResultSet results = null;
    Statement stmt = null;
    Connection conn = null;
    try
    {
      Properties props = new java.util.Properties();
      props.put("separator", ";"); // separator is a bar
      props.put("suppressHeaders", "false"); // first line contains data
      props.put("charset", encoding);
      String path = file.getParent();
      String fil = file.getName();
      int pos = fil.lastIndexOf('.');
      props.put("fileExtension", fil.substring(pos));

      // load the driver into memory
      Class.forName("org.relique.jdbc.csv.CsvDriver");

      // create a connection. The first command line parameter is assumed to
      // be the directory in which the .csv files are held
      conn = DriverManager.getConnection("jdbc:relique:csv:" + path, props);

      // create a Statement object to execute the query with
      stmt = conn.createStatement();

      results = stmt
          .executeQuery("SELECT * FROM \"" + fil.substring(0, pos) + "\"");
      String columnMitgliedsnummer = "Mitglieds_Nr";
      String colExtMitgliedsnummer = "Ext_Mitglieds_Nr";
      String colNachname = "Nachname";
      String colVorname = "Vorname";
      boolean b_mitgliedsnummer = false;
      boolean b_extmitgliedsnummer = false;
      boolean b_nachname = false;
      boolean b_vorname = false;
      ResultSetMetaData meta = results.getMetaData();
      for (int i = 1; i < meta.getColumnCount(); i++)
      {
        String columnTitle = meta.getColumnName(i);
        // Breche ab, wenn UTF-8 BOM vorhanden->
        // http://de.wikipedia.org/wiki/UTF-8#Byte_Order_Mark
        if (i == 1 && encoding.equals("UTF-8") && columnTitle.length() > 1)
        {
          if (Character.getNumericValue(meta.getColumnName(1).charAt(0)) == -1)
          {
            throw new ApplicationException(
                "Eingelesene Datei beginnt mit UTF-8 BOM. Bitte entfernen. Abbruch!");
          }

        }
        if (columnTitle.equals(columnMitgliedsnummer))
        {
          b_mitgliedsnummer = true;
        }
        else if (columnTitle.equals(colExtMitgliedsnummer))
        {
          b_extmitgliedsnummer = true;
        }
        else if (columnTitle.equals(colNachname))
        {
          b_nachname = true;
        }
        else if (columnTitle.equals(colVorname))
        {
          b_vorname = true;
        }
      }
      if (b_mitgliedsnummer && b_extmitgliedsnummer)
      {
        throw new ApplicationException(
            "Spaltenüberschrift muss entweder nur Mitglieds_Nr"
                + " oder Ext_Mitglieds_Nr zur Zuordnung des Mitglieds enthalten. Es ist beides vorhanden. Abbruch!");
      }
      if ((b_mitgliedsnummer || b_extmitgliedsnummer)
          && (b_nachname || b_vorname))
      {
        throw new ApplicationException(
            "Spaltenüberschrift muss entweder Angaben zur Mitgliedsnummer"
                + " oder Nachname und Vorname zur Zuordnung des Mitglieds enthalten. Es ist beides vorhanden. Abbruch!");
      }
      if (!b_mitgliedsnummer && !b_extmitgliedsnummer
          && (!b_nachname || !b_vorname))
      {
        throw new ApplicationException(
            "Spaltenüberschrift muss entweder Mitglieds_Nr, Ext_Mitglieds_Nr"
                + " oder Nachname/Vorname zur Zuordnung des Mitglieds enhalten. Es ist keine Information vorhanden. Abbruch!");
      }

      monitor.setStatusText(
          "Überprüfung der Spaltenüberschriften erfolgreich abgeschlossen.");

      int anz = 0;
      String mitgliedIdString = "";
      int zeilen = (int) Files.lines(Paths.get(file.getPath())).count();
      try
      {
        DBTransaction.starten();
        while (results.next())
        {
          anz++;
          monitor.setPercentComplete(100 * anz / zeilen);

          DBIterator<Mitglied> list = Einstellungen.getDBService()
              .createList(Mitglied.class);
          if (b_mitgliedsnummer)
          {
            list.addFilter("id = ? ", results.getString(columnMitgliedsnummer));
          }
          if (b_extmitgliedsnummer)
          {
            list.addFilter("externemitgliedsnummer = ? ",
                results.getString(colExtMitgliedsnummer));
          }
          if (b_nachname)
          {
            list.addFilter("name = ? ", results.getString(colNachname));
          }
          if (b_vorname)
          {
            list.addFilter("vorname = ? ", results.getString(colVorname));
          }

          if (b_mitgliedsnummer)
            mitgliedIdString = columnMitgliedsnummer + "="
                + results.getString(columnMitgliedsnummer);
          else if (b_extmitgliedsnummer)
          {
            mitgliedIdString = colExtMitgliedsnummer + "="
                + results.getString(colExtMitgliedsnummer);
          }
          else
            mitgliedIdString = colNachname + "="
                + results.getString(colNachname) + ", " + colVorname + "="
                + results.getString(colVorname);

          if (list.size() == 0)
          {
            throw new ApplicationException(
                "Kein Mitglied in JVerein-Datenbank gefunden.");
          }
          else if (list.size() > 1)
          {
            DBTransaction.rollback();
            throw new ApplicationException("Mehr als ein Mitglied gefunden.");
          }

          Mitglied m = (Mitglied) list.next();
          Zusatzbetrag zus = (Zusatzbetrag) Einstellungen.getDBService()
              .createObject(Zusatzbetrag.class, null);
          zus.setMitglied(Integer.valueOf(m.getID()));
          double betrag = results.getDouble("Betrag");
          zus.setBetrag(betrag);
          String buchungstext = results.getString("Buchungstext");
          if (buchungstext.length() > 140)
          {
            throw new ApplicationException(
                "Buchungstextes zu lang (max 140 Zeichen).");
          }
          zus.setBuchungstext(buchungstext);
          try
          {
            Date d = de.jost_net.JVerein.util.Datum
                .toDate(results.getString("Fälligkeit"));
            zus.setFaelligkeit(d);
            zus.setStartdatum(d);
          }
          catch (ParseException e)
          {
            DBTransaction.rollback();
            throw new ApplicationException("Ungültiges Fälligkeitsdatum.");
          }

          int intervall = results.getInt("Intervall");
          if (intervall < 0)
          {
            throw new ApplicationException(
                "Ungültiges Intervall, Zahl muss größer oder gleich 0 sein.");
          }
          zus.setIntervall(intervall);
          try
          {
            String datum = results.getString("Endedatum");
            if (datum.length() > 0)
            {
              Date d = de.jost_net.JVerein.util.Datum
                  .toDate(results.getString("Endedatum"));
              zus.setEndedatum(d);
            }
          }
          catch (ParseException e)
          {
            throw new ApplicationException("Ungültiges Endedatum.");
          }
          catch (SQLException e)
          {
            //
          }
          Buchungsart ba = null;
          try
          {
            String buchungsart = results.getString("Buchungsart");
            DBIterator<Buchungsart> it = Einstellungen.getDBService()
                .createList(Buchungsart.class);
            it.addFilter("nummer = ?", buchungsart);
            if (it.size() == 0)
            {
              throw new ApplicationException(String.format(
                  "Buchungsart mit der Nummer %s nicht gefunden", buchungsart));
            }
            ba = it.next();
            zus.setBuchungsart(ba);
          }
          catch (SQLException e)
          {
            //
          }
          try
          {
            String buchungsklasse = results.getString("Buchungsklasse");
            DBIterator<Buchungsklasse> it = Einstellungen.getDBService()
                .createList(Buchungsklasse.class);
            it.addFilter("nummer = ?", buchungsklasse);
            if (it.size() == 0)
            {
              throw new ApplicationException(String.format(
                  "Buchungsklasse mit der Nummer %s nicht gefunden",
                  buchungsklasse));
            }
            zus.setBuchungsklasseId(Long.valueOf(buchungsklasse));
          }
          catch (SQLException e)
          {
            //
          }
          try
          {
            String steuer = results.getString("Steuer");
            // Bei 0 setzen wir keine Steuer
            if (!"0".equals(steuer))
            {
              DBIterator<Steuer> it = Einstellungen.getDBService()
                  .createList(Steuer.class);
              it.join("buchungsart");
              it.addFilter("buchungsart.id = steuer.buchungsart");
              if (ba == null)
              {
                throw new ApplicationException(
                    "Steuer nur möglich, wenn auch eine Buchungsart angegeben ist.");
              }
              it.addFilter("buchungsart.art = ?", ba.getArt());
              it.addFilter("satz = ?", steuer);
              if (it.size() == 0)
              {
                String steuerart = "";
                switch (ba.getArt())
                {
                  case ArtBuchungsart.AUSGABE:
                    steuerart = "Vorsteuer";
                    break;
                  case ArtBuchungsart.EINNAHME:
                    steuerart = "Umsatzsteuer";
                    break;
                  case ArtBuchungsart.UMBUCHUNG:
                    throw new ApplicationException(
                        "Steuer bei Umbuchungen ist bei Zusatzbeträgen nicht möglich");
                }
                throw new ApplicationException(String.format(
                    steuerart + " mit dem Satz %s%% nicht gefunden.", steuer));
              }
              zus.setSteuer(it.next());
            }
          }
          catch (SQLException e)
          {
            //
          }
          try
          {
            Integer zahlungsweg = results.getInt("Zahlungsweg");
            zus.setZahlungsweg(new Zahlungsweg(zahlungsweg));
          }
          catch (Exception e)
          {
            zus.setZahlungsweg(new Zahlungsweg(Zahlungsweg.STANDARD));
          }
          monitor.setStatusText(String.format(
              "Zusatzbeitrag für Mitglied %s erfolgreich importiert.",
              Adressaufbereitung.getNameVorname(zus.getMitglied())));
          zus.store();
        }
        // überprüfen und parsen der Daten beendet.
        monitor
            .setStatusText(String.format("%d Zusatzbeiträge importiert", anz));
        DBTransaction.commit();
      }
      catch (ApplicationException e)
      {
        DBTransaction.rollback();
        throw new ApplicationException(
            String.format("Importzeile %d (%s):", anz, mitgliedIdString)
                + e.getMessage());
      }
    }
    catch (IOException | SQLException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Fehler beim Importieren: " + e.getMessage());
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Treiber zum Parsen der CSV-Datei nicht gefunden.");
    }
    finally
    {
      try
      {
        if (results != null)
        {
          results.close();
        }
      }
      catch (SQLException e)
      {
        Logger.error("Fehler beim Schließen von ResultSet results", e);
      }
      try
      {
        if (stmt != null)
        {
          stmt.close();
        }
      }
      catch (SQLException e)
      {
        Logger.error("Fehler beim Schließen von Statement stmt", e);
      }
      try
      {
        if (conn != null)
          conn.close();
      }
      catch (SQLException e)
      {
        Logger.error("Fehler beim Schließen von Connection conn", e);
      }
    }
  }

  @Override
  public String getName()
  {
    return "Default-Zusatzbeträge";
  }

  public boolean hasFileDialog()
  {
    return true;
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Zusatzbetrag.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return DefaultZusatzbetraegeImport.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.csv" };
      }
    };
    return new IOFormat[] { f };
  }
}
