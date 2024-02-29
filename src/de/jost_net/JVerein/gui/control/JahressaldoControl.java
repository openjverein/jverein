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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.parts.JahressaldoList;
import de.jost_net.JVerein.io.JahressaldoPDF;
import de.jost_net.JVerein.io.SaldoZeile;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class JahressaldoControl extends SaldoControl
{

  private JahressaldoList saldoList;

  public JahressaldoControl(AbstractView view)
  {
    super(view);
  }

  public Button getStartAuswertungButton()
  {
    Button b = new Button("PDF", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteAuswertung();
      }
    }, null, true, "file-pdf.png"); // "true" defines this button as the default
    // button
    return b;
  }

  public void handleStore()
  {
    //
  }

  public Part getSaldoList() throws ApplicationException
  {
    try
    {
      if (getDatumvon().getDate() != null)
      {
        settings.setAttribute("von",
            new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));
        settings.setAttribute("bis",
            new JVDateFormatTTMMJJJJ().format(getDatumbis().getDate()));
      }
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(getDatumvon().getDate());
      int jahr = cal.get(Calendar.YEAR);

      if (saldoList == null)
      {
        saldoList = new JahressaldoList(null, new Geschaeftsjahr(jahr));
      }
      else
      {
        saldoList.setGeschaeftsjahr(new Geschaeftsjahr(jahr));
        ArrayList<SaldoZeile> zeile = saldoList.getInfo();
        saldoList.removeAll();
        for (SaldoZeile sz : zeile)
        {
          saldoList.addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
    catch (ParseException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
    return saldoList.getSaldoList();
  }

  private void starteAuswertung() throws ApplicationException
  {
    try
    {
      ArrayList<SaldoZeile> zeile = saldoList.getInfo();

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei w�hlen.");
      //
      Settings settings = new Settings(this.getClass());
      //
      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("jahressaldo", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), "PDF").get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
      Calendar cal = Calendar.getInstance();
      cal.setTime(getDatumvon().getDate());
      int jahr = cal.get(Calendar.YEAR);
      Geschaeftsjahr gj = new Geschaeftsjahr(jahr);

      auswertungSaldoPDF(zeile, file, gj);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler beim Aufbau des Reports: " + e.getMessage());
    }
    catch (ParseException e)
    {
      throw new ApplicationException(
          "Fehler beim Aufbau des Reports: " + e.getMessage());
    }
  }

  private void auswertungSaldoPDF(final ArrayList<SaldoZeile> zeile,
      final File file, final Geschaeftsjahr gj)
  {
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          new JahressaldoPDF(zeile, file, gj);
          GUI.getCurrentView().reload();
        }
        catch (ApplicationException ae)
        {
          Logger.error("Fehler", ae);
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
