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
import de.jost_net.JVerein.gui.parts.MittelverwendungList;
import de.jost_net.JVerein.io.MittelverwendungExportCSV;
import de.jost_net.JVerein.io.MittelverwendungExportPDF;
import de.jost_net.JVerein.io.MittelverwendungZeile;
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

public class MittelverwendungControl extends SaldoControl
{

  private MittelverwendungList[] saldoList = new MittelverwendungList[ANZAHL_TABS];

  final static String ExportPDF = "PDF";

  final static String ExportCSV = "CSV";

  public final static int FLOW_REPORT = 0;

  public final static int SALDO_REPORT = 1;

  public final static int ANZAHL_TABS = 2;

  private int selectedTab = 0;

  public MittelverwendungControl(AbstractView view)
  {
    super(view);
  }

  public Button getPDFExportButton()
  {
    Button b = new Button("PDF", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteExport(ExportPDF);
      }
    }, null, false, "file-pdf.png");
    // button
    return b;
  }

  public Button getCSVExportButton()
  {
    Button b = new Button("CSV", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteExport(ExportCSV);
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
    for (int i = 0; i < ANZAHL_TABS; i++)
    {
      if (i != selectedTab)
      {
        getSaldoList(i);
      }
    }
    return getSaldoList(selectedTab);
  }

  public Part getSaldoList(int tab) throws ApplicationException
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

      if (saldoList[tab] == null)
      {
        saldoList[tab] = new MittelverwendungList(null, datumvon.getDate(),
            datumbis.getDate(), tab);
      }
      else
      {
        settings.setAttribute("von",
            new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));

        saldoList[tab].setDatumvon(datumvon.getDate());
        saldoList[tab].setDatumbis(datumbis.getDate());
        ArrayList<MittelverwendungZeile> zeile = saldoList[tab].getInfo();
        saldoList[tab].removeAll();
        for (MittelverwendungZeile sz : zeile)
        {
          saldoList[tab].addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
    return saldoList[tab].getSaldoList();
  }

  private void starteExport(String type) throws ApplicationException
  {
    try
    {
      ArrayList<MittelverwendungZeile> zeilen = saldoList[selectedTab]
          .getInfo();

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
      fd.setFileName(new Dateiname("mittelverwendungsrechnung", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), type).get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      exportSaldo(zeilen, file, getDatumvon().getDate(),
          getDatumbis().getDate(), type, selectedTab);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Aufbau des Reports: %s", e.getMessage()));
    }
  }

  private void exportSaldo(final ArrayList<MittelverwendungZeile> zeile,
      final File file, final Date datumvon, final Date datumbis,
      final String type, final int tab)
  {
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          if (type.equals(ExportCSV))
            new MittelverwendungExportCSV(zeile, file, datumvon, datumbis, tab);
          else if (type.equals(ExportPDF))
            new MittelverwendungExportPDF(zeile, file, datumvon, datumbis, tab);
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

  public void setSelectedTab(int tab)
  {
    selectedTab = tab;
  }

}
