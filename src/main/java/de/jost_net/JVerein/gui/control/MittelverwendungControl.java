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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.MittelverwendungDialog;
import de.jost_net.JVerein.gui.parts.MittelverwendungFlowList;
import de.jost_net.JVerein.gui.parts.MittelverwendungSaldoList;
import de.jost_net.JVerein.io.MittelverwendungExportCSV;
import de.jost_net.JVerein.io.MittelverwendungExportPDF;
import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MittelverwendungControl extends SaldoControl
{

  private MittelverwendungFlowList zuflussList;

  private MittelverwendungSaldoList saldoList;

  final static String ExportPDF = "PDF";

  final static String ExportCSV = "CSV";

  public final static int FLOW_REPORT = 0;

  public final static int SALDO_REPORT = 1;

  private int selectedTab = FLOW_REPORT;

  private Button configButton;

  private Jahresabschluss[] jahresabschluesse = null;

  private Date editDatumvon = null;

  private String jaId = null;

  private String vorJaId = null;

  public MittelverwendungControl(AbstractView view) throws RemoteException
  {
    super(view);
    updateJahreasabschlüsse();
  }

  private void updateJahreasabschlüsse() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Jahresabschluss> abschluesse = service
        .createList(Jahresabschluss.class);
    abschluesse.setOrder("ORDER BY von desc");
    jahresabschluesse = new Jahresabschluss[abschluesse.size()];
    int i = 0;
    while (abschluesse.hasNext())
    {
      jahresabschluesse[i] = abschluesse.next();
      i++;
    }
  }

  public Button getConfigButton() throws RemoteException
  {
    configButton = new Button("Startwerte setzen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          Double rueckstand = null;
          Double weitergabe = null;
          Jahresabschluss ja = (Jahresabschluss) Einstellungen.getDBService()
              .createObject(Jahresabschluss.class, jaId);
          if (vorJaId != null)
          {
            Jahresabschluss vorJa = (Jahresabschluss) Einstellungen
                .getDBService().createObject(Jahresabschluss.class, vorJaId);
            if (vorJa.getVerwendungsrueckstand() != null
                && vorJa.getZwanghafteWeitergabe() != null)
            {
              MittelverwendungFlowList list = new MittelverwendungFlowList(
                  ja.getVon(), ja.getBis());
              list.getInfo();
              rueckstand = list.getRueckstandVorjahrNeu();
              weitergabe = list.getZwanghafteWeitergabeNeu();
            }
          }
          MittelverwendungDialog dialog = new MittelverwendungDialog(rueckstand,
              weitergabe, ja.getName());
          if (!dialog.open())
          {
            return;
          }
          if (ja.isNewObject())
          {
            ja.setVon(editDatumvon);
            Calendar cal = Calendar.getInstance();
            cal.setTime(editDatumvon);
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            ja.setBis(cal.getTime());
            ja.setDatum(new Date());
            ja.setName(dialog.getName());
          }
          ja.setVerwendungsrueckstand(dialog.getVerwendungsrueckstand());
          ja.setZwanghafteWeitergabe(dialog.getZwanghafteWeitergabe());
          ja.store();
          getSaldoList();
          updateJahreasabschlüsse();
          updateConfigButton();
        }
        catch (OperationCanceledException ignore)
        {
          throw new OperationCanceledException();
        }
        catch (Exception e)
        {
          throw new ApplicationException(e);
        }
      }
    }, null, false, "text-x-generic.png");
    updateConfigButton();
    return configButton;
  }

  private void updateConfigButton() throws RemoteException
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime((Date) datumvon.getDate());
    cal.add(Calendar.YEAR, -1);
    Date abschlussvon = cal.getTime();

    // Es gibt noch keinen Jahresabschluss, dann wird er erzeugt
    // Oder es ist der Mittelverwendungsreport des ersten Jahressabschlusses
    // Dann muss einer vorher erzeugt werden
    if (jahresabschluesse.length == 0
        || jahresabschluesse[jahresabschluesse.length - 1].getVon()
            .equals(datumvon.getDate()))
    {
      configButton.setEnabled(true);
      editDatumvon = abschlussvon;
      jaId = null;
      vorJaId = null;
      return;
    }

    // Der aktuelle Mittelverwendungsreport ist mehr als 1 Jahr nach dem letzten
    // Jahresabschluss oder vor dem ersten Jahresabschluss
    if (abschlussvon.after(jahresabschluesse[0].getVon()) || datumvon.getDate()
        .before(jahresabschluesse[jahresabschluesse.length - 1].getVon()))
    {
      configButton.setEnabled(false);
      return;
    }

    for (int i = 0; i < jahresabschluesse.length; i++)
    {
      if (jahresabschluesse[i].getVon().equals(abschlussvon))
      {
        if (jahresabschluesse[i].getVerwendungsrueckstand() == null
            || jahresabschluesse[i].getZwanghafteWeitergabe() == null)
        {
          configButton.setEnabled(true);
          editDatumvon = abschlussvon;
          jaId = jahresabschluesse[i].getID();
          if (i == jahresabschluesse.length - 1)
          {
            vorJaId = null;
          }
          else
          {
            vorJaId = jahresabschluesse[i + 1].getID();
          }
          return;
        }
      }
    }
    configButton.setEnabled(false);
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
    return b;
  }

  public void handleStore()
  {
    //
  }

  public Part getSaldoList() throws ApplicationException
  {
    if (selectedTab == FLOW_REPORT)
    {
      getSaldoTable();
      try
      {
        updateConfigButton();
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      return getFlowTable();
    }
    else
    {
      getFlowTable();
      try
      {
        updateConfigButton();
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      return getSaldoTable();
    }
  }

  public Part getSaldoTable() throws ApplicationException
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
      saldoList = new MittelverwendungSaldoList(datumvon.getDate(),
          datumbis.getDate());
    }
    else
    {
      saldoList.setDatumvon(datumvon.getDate());
      saldoList.setDatumbis(datumbis.getDate());
    }
    return saldoList.getSaldoList();
  }

  public Part getFlowTable() throws ApplicationException
  {
    if (getDatumvon().getDate() != null)
    {
      settings.setAttribute("von",
          new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));
      settings.setAttribute("bis",
          new JVDateFormatTTMMJJJJ().format(getDatumbis().getDate()));
    }

    if (zuflussList == null)
    {
      zuflussList = new MittelverwendungFlowList(datumvon.getDate(),
          datumbis.getDate());
    }
    else
    {
      zuflussList.setDatumvon(datumvon.getDate());
      zuflussList.setDatumbis(datumbis.getDate());
    }
    return zuflussList.getFlowList();
  }

  private void starteExport(String type) throws ApplicationException
  {
    try
    {
      ArrayList<MittelverwendungZeile> zeilen;
      if (selectedTab == FLOW_REPORT)
      {
        zeilen = zuflussList.getInfo();
      }
      else
      {
        zeilen = saldoList.getInfo();
      }

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
