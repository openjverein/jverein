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
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.parts.AbstractSaldoList;
import de.jost_net.JVerein.gui.parts.BuchungsklasseSaldoList;
import de.jost_net.JVerein.gui.parts.MittelverwendungFlowSaldoList;
import de.jost_net.JVerein.gui.view.BuchungsklasseSaldoView;
import de.jost_net.JVerein.gui.view.MittelverwendungSaldoView;
import de.jost_net.JVerein.io.BuchungsklasseSaldoZeile;
import de.jost_net.JVerein.io.BuchungsklassesaldoCSV;
import de.jost_net.JVerein.io.BuchungsklassesaldoPDF;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungsklasseSaldoControl extends SaldoControl
{

  private AbstractSaldoList saldoList;

  private boolean summe = true;

  final static String AuswertungPDF = "PDF";

  final static String AuswertungCSV = "CSV";

  public BuchungsklasseSaldoControl(AbstractView view)
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
        starteAuswertung(AuswertungPDF);
      }
    }, null, false, "file-pdf.png");
    // button
    return b;
  }

  public Button getStartAuswertungCSVButton()
  {
    Button b = new Button("CSV", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteAuswertung(AuswertungCSV);
      }
    }, null, false, "xsd.png");
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

      if (saldoList == null)
      {
        if (view instanceof BuchungsklasseSaldoView)
        {
          saldoList = new BuchungsklasseSaldoList(null, datumvon.getDate(),
              datumbis.getDate());
        }
        else if (view instanceof MittelverwendungSaldoView)
        {
          saldoList = new MittelverwendungFlowSaldoList(null, datumvon.getDate(),
              datumbis.getDate());
        }
      }
      else
      {
        settings.setAttribute("von",
            new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));

        saldoList.setDatumvon(datumvon.getDate());
        saldoList.setDatumbis(datumbis.getDate());
        ArrayList<BuchungsklasseSaldoZeile> zeile = saldoList.getInfo();
        saldoList.removeAll();
        for (BuchungsklasseSaldoZeile sz : zeile)
        {
          saldoList.addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
    return saldoList.getSaldoList();
  }

  // THL pdf cvs umschalter
  private void starteAuswertung(String type) throws ApplicationException
  {
    try
    {
      String title = "-";
      if (view instanceof BuchungsklasseSaldoView)
      {
        title = "Buchungsklassen-Saldo";
        summe = true;
      }
      else if (view instanceof MittelverwendungSaldoView)
      {
        title = "Mittelverwendung-Saldo";
        summe = false;
      }
      ArrayList<BuchungsklasseSaldoZeile> zeile = saldoList.getInfo();

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");
      //
      Settings settings = new Settings(this.getClass());
      //
      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname(title, "",
          Einstellungen.getEinstellung().getDateinamenmuster(), type).get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      auswertungSaldo(zeile, file, getDatumvon().getDate(),
          getDatumbis().getDate(), type, title);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Aufbau des Reports: %s", e.getMessage()));
    }
  }

  private void auswertungSaldo(final ArrayList<BuchungsklasseSaldoZeile> zeile,
      final File file, final Date datumvon, final Date datumbis,
      final String type, String title)
  {
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          if (type.equals(AuswertungCSV))
          {
            new BuchungsklassesaldoCSV(zeile, file, datumvon, datumbis, summe);
          }
          else if (type.equals(AuswertungPDF))
          {
            new BuchungsklassesaldoPDF(zeile, file, datumvon, datumbis, title,
                summe);
          }
          GUI.getCurrentView().reload();
        }
        catch (ApplicationException ae)
        {
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
