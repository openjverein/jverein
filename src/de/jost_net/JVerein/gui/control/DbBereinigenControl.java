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
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mail;
import de.willuhn.datasource.rmi.DBIterator;
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
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;

public class DbBereinigenControl extends AbstractControl
{

  private Settings settings = null;
  
  private double anzahl = 3.0d;
  
  // Buchungen loeschen
  private CheckboxInput bLoeschenInput = null;
  private DateInput bDateInput = null;
  private CheckboxInput sLoeschenInput = null;

  //Lastschriften loeschen
  private CheckboxInput lLoeschenInput = null;
  private DateInput lDateInput = null;
  
  //Mails loeschen
  private CheckboxInput mLoeschenInput = null;
  private DateInput mDateInput = null;

  public DbBereinigenControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
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
          runDelete((boolean) bLoeschenInput.getValue(), (Date) bDateInput.getValue(),
              (boolean) sLoeschenInput.getValue(),
              (boolean) lLoeschenInput.getValue(), (Date) lDateInput.getValue(),
              (boolean) mLoeschenInput.getValue(), (Date) mDateInput.getValue());
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    // button
    return b;
  }

  
  private void runDelete(final boolean bloeschen, final Date bdate,
      final boolean sloeschen,
      final boolean lloeschen, final Date ldate,
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
          
          // Buchungen löschen
          if ( bloeschen && bdate == null)
          {
            monitor.log("Buchungen löschen: Kein gültiges Datum eingegeben");
          }
          else if ( bloeschen && bdate != null)
          {
            buchungenLoeschen(monitor, bdate, sloeschen);
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
          Logger.error("", re);
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
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    cal.set(Calendar.YEAR, year-11);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    bDateInput = new DateInput(new Date(cal.getTimeInMillis()));
    return bDateInput;
  }

  public CheckboxInput getSollbuchungenLoeschen()
  {
    if (sLoeschenInput != null)
    {
      return sLoeschenInput;
    }
    sLoeschenInput = new CheckboxInput(true);
    return sLoeschenInput;
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
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    cal.set(Calendar.YEAR, year-11);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    lDateInput = new DateInput(new Date(cal.getTimeInMillis()));
    return lDateInput;
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
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    cal.set(Calendar.YEAR, year-11);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    mDateInput = new DateInput(new Date(cal.getTimeInMillis()));
    return mDateInput;
  }
  
  // Lösch Aktionen
  private void buchungenLoeschen(ProgressMonitor monitor, final Date date, final boolean sloeschen)
  {
    try
    {
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("datum < ?", date);
      int count = 0;
      Buchung b = null;
      while (it.hasNext())
      {
        b = it.next();
        try
        {
          if (sloeschen && (b.getMitgliedskonto() != null))
          {
            b.getMitgliedskonto().delete();
          }
        }
        catch (Exception e)
        {
          // Das kann passieren wenn der Sollbuchung mehrere Buchungen 
          // zugeordnet waren. Dann existiert die Sollbuchung nicht mehr  
          // bei den weiteren Buchungen da das Query vorher erfolgt ist
        }
        b.delete();
        count++;
      }
      if (count > 0)
      {
        monitor.setStatusText(String.format(
            "%d Buchung" + (count != 1 ? "en" : "") + " gelöscht.", count));
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
      Logger.error(fehler, e);
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
      int count = 0;
      while (it.hasNext())
      {
        it.next().delete();;
        count++;
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
      Logger.error(fehler, e);
    }
  }
  
  private void mailsLoeschen(ProgressMonitor monitor, final Date date)
  {
    try
    {
      DBIterator<Mail> it = Einstellungen.getDBService()
          .createList(Mail.class);
      it.addFilter("versand < ?", date);
      int count = 0;
      while (it.hasNext())
      {
        it.next().delete();;
        count++;
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
      Logger.error(fehler, e);
    }
  }

}
