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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Datum;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ProgressMonitor;

public class DbBereinigenControl extends AbstractControl
{

  private Settings settings = null;

  Date defaultDate = null;

  private double anzahl = 7.0d;

  // Rechnungen loeschen
  private CheckboxInput rLoeschenInput = null;
  private DateInput rDateInput = null;

  // Spendenbescheinigungen loeschen
  private CheckboxInput sLoeschenInput = null;
  private DateInput sDateInput = null;

  // Buchungen loeschen
  private CheckboxInput bLoeschenInput = null;
  private DateInput bDateInput = null;
  private CheckboxInput sollLoeschenInput = null;

  // Lastschriften loeschen
  private CheckboxInput lLoeschenInput = null;
  private DateInput lDateInput = null;

  // Abrechnungslauf loeschen
  private CheckboxInput aLoeschenInput = null;
  private DateInput aDateInput = null;

  // Jahresabschluss loeschen
  private CheckboxInput jLoeschenInput = null;
  private DateInput jDateInput = null;

  // Mails loeschen
  private CheckboxInput mLoeschenInput = null;
  private DateInput mDateInput = null;

  public DbBereinigenControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    cal.set(Calendar.YEAR, year-11);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    defaultDate = new Date(cal.getTimeInMillis());
  }


  public Button getStartLoeschenButton()
  {
    Button b = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          runDelete(
              (boolean) rLoeschenInput.getValue(), (Date) rDateInput.getValue(),
              (boolean) sLoeschenInput.getValue(), (Date) sDateInput.getValue(),
              (boolean) bLoeschenInput.getValue(), (Date) bDateInput.getValue(),
              (boolean) sollLoeschenInput.getValue(),
              (boolean) lLoeschenInput.getValue(), (Date) lDateInput.getValue(),
              (boolean) aLoeschenInput.getValue(), (Date) aDateInput.getValue(),
              (boolean) jLoeschenInput.getValue(), (Date) jDateInput.getValue(),
              (boolean) mLoeschenInput.getValue(), (Date) mDateInput.getValue());
        }
        catch (Exception e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    // button
    return b;
  }


  private void runDelete(
      final boolean rloeschen, final Date rdate,
      final boolean sloeschen, final Date sdate,
      final boolean bloeschen, final Date bdate, final boolean sollloeschen,
      final boolean lloeschen, final Date ldate,
      final boolean aloeschen, final Date adate,
      final boolean jloeschen, final Date jdate,
      final boolean mloeschen, final Date mdate) throws RemoteException
  {
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor)
      {
        try
        {
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);
          double progress = 1.0d;

          // Rechnungen löschen
          if ( rloeschen && rdate == null)
          {
            monitor.log("Rechnungen löschen: Kein gültiges Datum eingegeben");
          }
          else if ( rloeschen && rdate != null)
          {
            rechnungenLoeschen(monitor, rdate);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Spendenbescheinigungen löschen
          if ( sloeschen && sdate == null)
          {
            monitor.log("Spendenbescheinigungen löschen: Kein gültiges Datum eingegeben");
          }
          else if ( sloeschen && sdate != null)
          {
            spendenbescheinigungenLoeschen(monitor, sdate);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Buchungen löschen
          if ( bloeschen && bdate == null)
          {
            monitor.log("Buchungen löschen: Kein gültiges Datum eingegeben");
          }
          else if ( bloeschen && bdate != null)
          {
            buchungenLoeschen(monitor, bdate, sollloeschen);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Lastschriften löschen
          if ( lloeschen && ldate == null)
          {
            monitor.log("Lastschriften löschen: Kein gültiges Datum eingegeben");
          }
          else if ( lloeschen && ldate != null)
          {
            lastschriftenLoeschen(monitor, ldate);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Abrechnungslauf löschen
          if ( aloeschen && adate == null)
          {
            monitor.log("Abrechnungsläufe löschen: Kein gültiges Datum eingegeben");
          }
          else if ( aloeschen && adate != null)
          {
            abrechnungslaufLoeschen(monitor, adate);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Jahresabschluss löschen
          if ( jloeschen && jdate == null)
          {
            monitor.log("Jahresabschlüsse löschen: Kein gültiges Datum eingegeben");
          }
          else if ( jloeschen && jdate != null)
          {
            jahresabschlussLoeschen(monitor, jdate);
          }
          monitor.setPercentComplete((int) (progress / anzahl * 100d));
          progress++;

          // Mails löschen
          if ( mloeschen && mdate == null)
          {
            monitor.log("Mails löschen: Kein gültiges Datum eingegeben");
          }
          else if ( mloeschen && mdate != null)
          {
            mailsLoeschen(monitor, mdate);
          }

          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Bereinigung beendet");
          GUI.getStatusBar().setSuccessText("Bereinigung beendet");
        }
        catch (Exception re)
        {
          monitor.log(re.getMessage());
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

  // Rechnungen loeschen
  public CheckboxInput getRechnungenLoeschen()
  {
    if (rLoeschenInput != null)
    {
      return rLoeschenInput;
    }
    rLoeschenInput = new CheckboxInput(false);
    return rLoeschenInput;
  }

  public DateInput getDatumAuswahlRechnungen()
  {
    if (rDateInput != null)
    {
      return rDateInput;
    }
    rDateInput = new DateInput(defaultDate);
    return rDateInput;
  }

  // Spendenbescheinigungen loeschen
  public CheckboxInput getSpendenbescheinigungenLoeschen()
  {
    if (sLoeschenInput != null)
    {
      return sLoeschenInput;
    }
    sLoeschenInput = new CheckboxInput(false);
    return sLoeschenInput;
  }

  public DateInput getDatumAuswahlSpendenbescheinigungen()
  {
    if (sDateInput != null)
    {
      return sDateInput;
    }
    sDateInput = new DateInput(defaultDate);
    return sDateInput;
  }

  // Buchungen loeschen
  public CheckboxInput getBuchungenLoeschen()
  {
    if (bLoeschenInput != null)
    {
      return bLoeschenInput;
    }
    bLoeschenInput = new CheckboxInput(false);
    return bLoeschenInput;
  }

  public DateInput getDatumAuswahlBuchungen()
  {
    if (bDateInput != null)
    {
      return bDateInput;
    }
    bDateInput = new DateInput(defaultDate);
    return bDateInput;
  }

  public CheckboxInput getSollbuchungenLoeschen()
  {
    if (sollLoeschenInput != null)
    {
      return sollLoeschenInput;
    }
    sollLoeschenInput = new CheckboxInput(true);
    return sollLoeschenInput;
  }

  // Lastschriften loeschen
  public CheckboxInput getLastschriftenLoeschen()
  {
    if (lLoeschenInput != null)
    {
      return lLoeschenInput;
    }
    lLoeschenInput = new CheckboxInput(false);
    return lLoeschenInput;
  }

  public DateInput getDatumAuswahlLastschriften()
  {
    if (lDateInput != null)
    {
      return lDateInput;
    }
    lDateInput = new DateInput(defaultDate);
    return lDateInput;
  }

  // Abrechnungslauf loeschen
  public CheckboxInput getAbrechnungslaufLoeschen()
  {
    if (aLoeschenInput != null)
    {
      return aLoeschenInput;
    }
    aLoeschenInput = new CheckboxInput(false);
    return aLoeschenInput;
  }

  public DateInput getDatumAuswahlAbrechnungslauf()
  {
    if (aDateInput != null)
    {
      return aDateInput;
    }
    aDateInput = new DateInput(defaultDate);
    return aDateInput;
  }

  // Jahresabschluss loeschen
  public CheckboxInput getJahresabschlussLoeschen()
  {
    if (jLoeschenInput != null)
    {
      return jLoeschenInput;
    }
    jLoeschenInput = new CheckboxInput(false);
    return jLoeschenInput;
  }

  public DateInput getDatumAuswahlJahresabschluss()
  {
    if (jDateInput != null)
    {
      return jDateInput;
    }
    jDateInput = new DateInput(defaultDate);
    return jDateInput;
  }

  // Mails loeschen
  public CheckboxInput getMailsLoeschen()
  {
    if (mLoeschenInput != null)
    {
      return mLoeschenInput;
    }
    mLoeschenInput = new CheckboxInput(false);
    return mLoeschenInput;
  }

  public DateInput getDatumAuswahlMails()
  {
    if (mDateInput != null)
    {
      return mDateInput;
    }
    mDateInput = new DateInput(defaultDate);
    return mDateInput;
  }

  // Lösch Aktionen
  private void rechnungenLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Rechnung> it = Einstellungen.getDBService()
          .createList(Rechnung.class);
      it.addFilter("datum < ?", date);
      it.setOrder("order by datum"); 
      int count = 0;
      Rechnung rechnung = null;
      while (it.hasNext())
      {
        try
        {
          rechnung = it.next();
          rechnung.delete();
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen der Rechnungung mit Nr " + 
              rechnung.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Rechnung" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Rechnung im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Rechnungen.";
      monitor.setStatusText(fehler);
    }
  }

  private void spendenbescheinigungenLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Spendenbescheinigung> it = Einstellungen.getDBService()
          .createList(Spendenbescheinigung.class);
      it.addFilter("spendedatum < ?", date);
      it.setOrder("order by spendedatum"); 
      int count = 0;
      Spendenbescheinigung sp = null;
      while (it.hasNext())
      {
        try
        {
          sp = it.next();
          sp.delete();
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen der Spendenbescheinigung mit Nr " + 
              sp.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Spendenbescheinigung" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Spendenbescheinigung im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Spendenbescheinigungen.";
      monitor.setStatusText(fehler);
    }
  }

  private void buchungenLoeschen(ProgressMonitor monitor, final Date date, 
      final boolean sollloeschen)
  {
    try
    {
      // Check ob im Bereich Splittbuchungen mit Spendenbescheinigungen liegen
      final DBService service = Einstellungen.getDBService();
      String sql = "SELECT DISTINCT buchung.splitid from buchung "
          + "WHERE splitid IS NOT NULL and spendenbescheinigung IS NOT NULL "
          + "and datum < ? ";
      @SuppressWarnings("unchecked")
      ArrayList<Long> splitmitspende = (ArrayList<Long>) service.execute(sql,
          new Object[] { date }, new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs)
            throws RemoteException, SQLException
        {
          ArrayList<Long> list = new ArrayList<>();
          while (rs.next())
          {
            list.add(rs.getLong(1));
          }
          return list;
        }
      });

      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("datum < ?", date);
      it.setOrder("order by datum"); 
      int countb = 0;
      int counts = 0;
      Buchung b = null;
      while (it.hasNext())
      {
        try
        {
          b = it.next();
          if (splitmitspende.contains(b.getSplitId()))
          {
            String fehler = "Die Buchung mit der Nr " + b.getID() +
                " wurde nicht gelöscht. Sie ist Teil einer Splittbuchung "
                + "mit zugeordeneten Spendenbescheinigungen";
            monitor.setStatusText(fehler);
            continue;
          }
          try
          {
            b.delete();
          }
          catch (RemoteException e)
          {
            //Das kann passieren, wenn die Split Hauptbuchung gelöscht wurde
            //und jetzt die Splitbuchung gelöscht werden soll, diese
            //aber durch den Foreign Key bereits gelöscht ist
            //wenn es aber keine Splitbuchung ist, werfen wir die Exeption
            if(b.getSplitId() == null)
              throw e;
          }
          catch (Exception e)
          {
            String fehler = "Fehler beim Löschen der Buchung mit Nr " + 
                b.getID() + ", " + e.getMessage();
            monitor.setStatusText(fehler);
          }
          try
          {
            if (sollloeschen && (b.getSollbuchung() != null))
            {
              b.getSollbuchung().delete();
              counts++;
            }
          }
          catch (OperationCanceledException oce)
          {
            throw oce;
          }
          catch (ObjectNotFoundException e)
          {
            // Das kann passieren wenn der Sollbuchung mehrere Buchungen 
            // zugeordnet waren. Dann existiert die Sollbuchung nicht mehr  
            // bei den weiteren Buchungen da das Query vorher erfolgt ist
          }
          catch (Exception e)
          {
            String fehler = "Fehler beim Löschen der Sollbuchung mit Nr " + 
                b.getSollbuchung().getID() + ", " + e.getMessage();
            monitor.setStatusText(fehler);
          }
          countb++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen der Buchung mit Nr " + 
              b.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (counts > 0)
      {
        monitor.setStatusText(String.format(
            "%d Sollbuchung" + (counts != 1 ? "en" : "") + " gelöscht.", counts));
      }
      if (countb > 0)
      {
        monitor.setStatusText(String.format(
            "%d Buchung" + (countb != 1 ? "en" : "") + " gelöscht.", countb));
      }
      else
      {
        monitor.log("Keine Buchungen im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Buchungen.";
      monitor.setStatusText(fehler);
    }
  }

  private void lastschriftenLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Lastschrift> it = Einstellungen.getDBService()
          .createList(Lastschrift.class);
      it.join("abrechnungslauf");
      it.addFilter("abrechnungslauf.id = lastschrift.abrechnungslauf");
      it.addFilter("faelligkeit < ?", date);
      it.setOrder("order by faelligkeit"); 
      int count = 0;
      Lastschrift la = null;
      while (it.hasNext())
      {
        try
        {
          la = it.next();
          la.delete();
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen der Lastschrift mit Nr " + 
              la.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Lastschrift" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Lastschriften im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Lastschriften.";
      monitor.setStatusText(fehler);
    }
  }

  private void abrechnungslaufLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Abrechnungslauf> it = Einstellungen.getDBService()
          .createList(Abrechnungslauf.class);
      it.addFilter("faelligkeit < ?", date);
      it.setOrder("order by faelligkeit"); 
      int count = 0;
      Abrechnungslauf al = null;
      while (it.hasNext())
      {
        try
        {
          al = it.next();
          // Suche Buchung des Abrechnungslaufes
          final DBService service = Einstellungen.getDBService();
          String sql = "SELECT buchung.id from buchung "
              + "WHERE abrechnungslauf = ? ";      
          boolean buchungen = (boolean) service.execute(sql,
              new Object[] { al.getID() }, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              if (rs.next())
              {
                return true;
              }
              return false;
            }
          });
          if (buchungen)
          {
            String fehler = "Der Abrechnungslauf mit der Nr " + al.getID() +
                " wurde nicht gelöscht. Es existieren noch Buchungen" +
                " zu diesem Abrechnungslauf";
            monitor.setStatusText(fehler);
            continue;
          }

          // Suche Sollbuchung des Abrechnungslaufes
          final DBService service1 = Einstellungen.getDBService();
          String sql1 = "SELECT " + Sollbuchung.TABLE_NAME_ID + " from "
              + Sollbuchung.TABLE_NAME + " WHERE " + Sollbuchung.ABRECHNUNGSLAUF
              + " = ? ";
          boolean sollbuchungen = (boolean) service1.execute(sql1,
              new Object[] { al.getID() }, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              if (rs.next())
              {
                return true;
              }
              return false;
            }
          });
          if (sollbuchungen)
          {
            String fehler = "Der Abrechnungslauf mit der Nr " + al.getID() +
                " wurde nicht gelöscht. Es existieren noch Sollbuchungen" +
                " zu diesem Abrechnungslauf";
            monitor.setStatusText(fehler);
            continue;
          }

          // Suche Lastschriften des Abrechnungslaufes
          final DBService service2 = Einstellungen.getDBService();
          String sql2 = "SELECT lastschrift.id from lastschrift "
              + "WHERE abrechnungslauf = ? ";      
          boolean lastschriften = (boolean) service2.execute(sql2,
              new Object[] { al.getID() }, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              if (rs.next())
              {
                return true;
              }
              return false;
            }
          });
          if (lastschriften)
          {
            String fehler = "Der Abrechnungslauf mit der Nr " + al.getID() +
                " wurde nicht gelöscht. Es existieren noch Lastschriften" +
                " zu diesem Abrechnungslauf";
            monitor.setStatusText(fehler);
            continue;
          }

          al.delete();
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen des Abrechnungslaufs mit Nr " + 
              al.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Abrechnungsl" + (count != 1 ? "äufe" : "auf") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Abrechnungsläufe im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Abrechnugsläufen.";
      monitor.setStatusText(fehler);
    }
  }

  private void jahresabschlussLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      // Suche Datum der ältesten Buchung
      final DBService service = Einstellungen.getDBService();
      String sql = "SELECT buchung.datum from buchung "
          + "WHERE datum < ? "
          + "order by datum ";      
      Date buchungdate = (Date) service.execute(sql,
          new Object[] { date }, new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs)
            throws RemoteException, SQLException
        {
          if (rs.next())
          {
            return rs.getDate(1);
          }
          return null;
        }
      });

      DBIterator<Jahresabschluss> it = Einstellungen.getDBService()
          .createList(Jahresabschluss.class);
      it.addFilter("bis < ?", date);
      it.setOrder("order by bis");
      int count = 0;
      Jahresabschluss ja = null;
      while (it.hasNext())
      {
        try
        {
          ja = it.next();
          if (buchungdate != null)
          {
            Date bis = ja.getBis();
            if (!bis.before(buchungdate))
            {
              String fehler = "Der Jahresabschluss mit der Nr " + ja.getID() +
                  " wurde nicht gelöscht. Es existieren noch Buchungen" +
                  " in diesem oder vorangehenden Jahresabschlüssen";
              monitor.setStatusText(fehler);
              continue;
            }
          }
          ja.delete();
          DBIterator<Anfangsbestand> it2 = Einstellungen.getDBService()
              .createList(Anfangsbestand.class);
          it2.addFilter("datum = ?", new Object[] { Datum.addTage(ja.getBis(), 1) });
          while (it2.hasNext())
          {
            it2.next().delete();
          }
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen des Jahresabschluss mit Nr " + 
              ja.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Jahresabschl" + (count != 1 ? "üsse" : "uss") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Jahresabschlüsse im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Jahresabschlüssen.";
      monitor.setStatusText(fehler);
    }
  }

  private void mailsLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Mail> it = Einstellungen.getDBService()
          .createList(Mail.class);
      it.addFilter("versand < ?", date);
      it.setOrder("order by versand");
      int count = 0;
      Mail mail = null;
      while (it.hasNext())
      {
        try
        {
          mail = it.next();
          mail.delete();
          count++;
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          String fehler = "Fehler beim Löschen der Lastschrift mit Nr " + 
              mail.getID() + ", " + e.getMessage();
          monitor.setStatusText(fehler);
        }
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Mail" + (count != 1 ? "s" : "") + " gelöscht.", count));
      }
      else
      {
        monitor.log("Keine Mails im vorgegebenen Zeitraum vorhanden!");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Löschen von Mails.";
      monitor.setStatusText(fehler);
    }
  }

}
