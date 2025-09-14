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
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.gui.view.ZusatzbetragDetailView;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ZusatzbetragControl extends VorZurueckControl implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private Zusatzbetrag zuab;

  private ZusatzbetragPart part;

  private SelectInput vorlage = null;

  private SelectInput ausfuehrungSuch = null;

  private AutoUpdateTablePart zusatzbetraegeList;

  public static final String NEIN = "nein";

  public static final String MITDATUM = "ja, mit Datum";

  public static final String OHNEDATUM = "ja, ohne Datum";

  public ZusatzbetragControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Zusatzbetrag getZusatzbetrag()
  {
    if (zuab != null)
    {
      return zuab;
    }
    zuab = (Zusatzbetrag) getCurrentObject();
    return zuab;
  }

  public ZusatzbetragPart getZusatzbetragPart()
  {
    if (part != null)
    {
      return part;
    }
    part = new ZusatzbetragPart(getZusatzbetrag(), true);
    return part;
  }

  public SelectInput getVorlage()
  {
    if (vorlage != null)
    {
      return vorlage;
    }
    vorlage = new SelectInput(new Object[] { NEIN, MITDATUM, OHNEDATUM }, NEIN);
    return vorlage;
  }

  public SelectInput getAusfuehrungSuch() throws RemoteException
  {
    if (ausfuehrungSuch != null)
    {
      return ausfuehrungSuch;
    }

    final Vector<String> werte = new Vector<>();
    werte.addElement("Alle");
    werte.addElement("Aktive");
    werte.addElement("Noch nicht ausgeführt");

    String sql = "select ausfuehrung from zusatzabbuchung where ausfuehrung is not null "
        + "group by ausfuehrung order by ausfuehrung desc";
    DBService service = Einstellungen.getDBService();

    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        while (rs.next())
        {
          werte.addElement(new JVDateFormatTTMMJJJJ().format(rs.getDate(1)));
        }
        return null;
      }
    };
    service.execute(sql, new Object[] {}, rs);

    ausfuehrungSuch = new SelectInput(werte, werte.elementAt(1));
    ausfuehrungSuch.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          getZusatzbetraegeList();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    return ausfuehrungSuch;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Zusatzbetrag z = getZusatzbetrag();
    z.setFaelligkeit((Date) getZusatzbetragPart().getFaelligkeit().getValue());
    z.setStartdatum(
        (Date) getZusatzbetragPart().getStartdatum(false).getValue());
    IntervallZusatzzahlung iz = (IntervallZusatzzahlung) getZusatzbetragPart()
        .getIntervall().getValue();
    z.setIntervall(iz.getKey());
    z.setEndedatum((Date) getZusatzbetragPart().getEndedatum().getValue());
    z.setBuchungstext(
        (String) getZusatzbetragPart().getBuchungstext().getValue());
    z.setBuchungsart(
        (Buchungsart) getZusatzbetragPart().getBuchungsart().getValue());
    z.setBuchungsklasseId(getZusatzbetragPart().getSelectedBuchungsKlasseId());
    z.setBetrag((Double) getZusatzbetragPart().getBetrag().getValue());
    z.setZahlungsweg(
        (Zahlungsweg) getZusatzbetragPart().getZahlungsweg().getValue());
    if (getZusatzbetragPart().isSteuerActive())
    {
      z.setSteuer((Steuer) getZusatzbetragPart().getSteuer().getValue());
    }
    if (z.isNewObject())
    {
      if (getZusatzbetragPart().getMitglied().getValue() != null)
      {
        Mitglied m = (Mitglied) getZusatzbetragPart().getMitglied().getValue();
        z.setMitglied(Integer.parseInt(m.getID()));
      }
      else
      {
        z.setMitglied(null);
      }
    }
    return z;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Zusatzbetrag z = (Zusatzbetrag) prepareStore();
      z.store();
      if (getVorlage().getValue().equals(MITDATUM)
          || getVorlage().getValue().equals(OHNEDATUM))
      {
        ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
            .getDBService().createObject(ZusatzbetragVorlage.class, null);
        zv.setIntervall(z.getIntervall());
        zv.setBuchungstext(z.getBuchungstext());
        zv.setBetrag(z.getBetrag());
        if (getVorlage().getValue().equals(MITDATUM))
        {
          zv.setEndedatum(z.getEndedatum());
          zv.setFaelligkeit(z.getFaelligkeit());
          zv.setStartdatum(z.getStartdatum());
        }
        zv.setBuchungsart(z.getBuchungsart());
        zv.setBuchungsklasseId(z.getBuchungsklasseId());
        zv.setZahlungsweg(z.getZahlungsweg());
        if (getZusatzbetragPart().isSteuerActive())
        {
          zv.setSteuer(z.getSteuer());
        }
        zv.store();
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Zusatzbetrages";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getZusatzbetraegeList() throws RemoteException
  {
    DBIterator<Zusatzbetrag> zusatzbetraege = getIterator();

    if (zusatzbetraegeList == null)
    {
      zusatzbetraegeList = new AutoUpdateTablePart(zusatzbetraege, null);
      zusatzbetraegeList.addColumn("Nr", "id-int");
      zusatzbetraegeList.addColumn("Name", "mitglied");
      zusatzbetraegeList.addColumn("Erste Fälligkeit", "startdatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Nächste Fälligkeit", "faelligkeit",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Letzte abgerechnete Fälligkeit",
          "ausfuehrung", new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Intervall", "intervalltext");
      zusatzbetraegeList.addColumn("Nicht mehr ausführen ab", "endedatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetraegeList.addColumn("Buchungstext", "buchungstext");
      zusatzbetraegeList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      zusatzbetraegeList.addColumn("Zahlungsweg", "zahlungsweg", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          return new Zahlungsweg((Integer) o).getText();
        }
      });
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        zusatzbetraegeList.addColumn("Buchungsklasse", "buchungsklasse",
            new BuchungsklasseFormatter());
      }
      zusatzbetraegeList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        zusatzbetraegeList.addColumn("Steuer", "steuer", o -> {
          if (o == null)
          {
            return "";
          }
          try
          {
            return ((Steuer) o).getName();
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
          return "";
        }, false, Column.ALIGN_RIGHT);
      }
      zusatzbetraegeList
          .setContextMenu(new ZusatzbetraegeMenu(zusatzbetraegeList));
      zusatzbetraegeList.setRememberColWidths(true);
      zusatzbetraegeList.setRememberOrder(true);
      zusatzbetraegeList.addFeature(new FeatureSummary());
      zusatzbetraegeList.setMulti(true);
      zusatzbetraegeList.setAction(
          new EditAction(ZusatzbetragDetailView.class, zusatzbetraegeList));
      VorZurueckControl.setObjektListe(null, null);
    }
    else
    {
      zusatzbetraegeList.removeAll();
      while (zusatzbetraege.hasNext())
      {
        zusatzbetraegeList.addItem(zusatzbetraege.next());
      }
      zusatzbetraegeList.sort();
    }
    if (this.ausfuehrungSuch.getText().equals("Aktive"))
    {
      nichtAktiveEliminieren(zusatzbetraegeList);
    }
    return zusatzbetraegeList;
  }

  private DBIterator<Zusatzbetrag> getIterator() throws RemoteException
  {
    DBIterator<Zusatzbetrag> zusatzbetraege = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    zusatzbetraege.join("mitglied");
    zusatzbetraege.addFilter("zusatzabbuchung.mitglied = mitglied.id");
    if (this.ausfuehrungSuch.getText().equals("Alle"))
    {
      // nichts tun
    }
    else if (this.ausfuehrungSuch.getText().equals("Aktive"))
    {
      // zunächst nichts tun
    }
    else if (this.ausfuehrungSuch.getText().equals("Noch nicht ausgeführt"))
    {
      zusatzbetraege.addFilter("ausfuehrung is null");
    }
    else
    {
      try
      {
        Date d = new JVDateFormatTTMMJJJJ()
            .parse(this.ausfuehrungSuch.getText());
        java.sql.Date sqd = new java.sql.Date(d.getTime());
        zusatzbetraege.addFilter("ausfuehrung = ?", new Object[] { sqd });
      }
      catch (ParseException e)
      {
        Logger.error("Fehler", e);
      }
    }
    zusatzbetraege
        .setOrder("ORDER BY ausfuehrung DESC, faelligkeit DESC, name");
    return zusatzbetraege;
  }

  private void nichtAktiveEliminieren(TablePart table) throws RemoteException
  {
    List<?> li = table.getItems();
    Iterator<?> it = li.iterator();
    while (it.hasNext())
    {
      Zusatzbetrag z = (Zusatzbetrag) it.next();
      if (!z.isOffen(new Date()))
      {
        table.removeItem(z);
      }
    }
  }

  public Button getPDFAusgabeButton()
  {
    Button b = new Button("PDF", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          starteAuswertung();
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler beim Start der PDF-Ausgabe der Zusatzbeträge");
        }
      }
    }, null, true, "file-pdf.png");
    return b;
  }

  private void starteAuswertung() throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(
        VorlageUtil.getName(VorlageTyp.ZUSATZBETRAEGE_DATEINAME, this)
            + ".pdf");
    fd.setFilterExtensions(new String[] { "*.pdf" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".pdf"))
    {
      s = s + ".pdf";
    }
    final File file = new File(s);
    final DBIterator<Zusatzbetrag> it = getIterator();
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          FileOutputStream fos = new FileOutputStream(file);
          Reporter reporter = new Reporter(fos, "Zusatzbeträge", "", it.size());
          reporter.addHeaderColumn("Mitglied", Element.ALIGN_LEFT, 60,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Startdatum", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Nächste Fälligkeit", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Letzte abgerechnete Fälligkeit",
              Element.ALIGN_LEFT, 30, BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Intervall", Element.ALIGN_LEFT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Nicht mehr ausführen ab",
              Element.ALIGN_LEFT, 30, BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Buchungstext", Element.ALIGN_LEFT, 50,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.createHeader();
          while (it.hasNext())
          {
            Zusatzbetrag z = (Zusatzbetrag) it.next();
            if ((Boolean) Einstellungen
                .getEinstellung(Property.MITGLIEDSNUMMERANZEIGEN))
            {
              reporter.addColumn(
                  Adressaufbereitung.getIdNameVorname(z.getMitglied()),
                  Element.ALIGN_LEFT);
            }
            else
            {
              reporter.addColumn(
                  Adressaufbereitung.getNameVorname(z.getMitglied()),
                  Element.ALIGN_LEFT);
            }
            reporter.addColumn(z.getStartdatum(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getFaelligkeit(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getAusfuehrung(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getIntervallText(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getEndedatum(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getBuchungstext(), Element.ALIGN_LEFT);
            reporter.addColumn(z.getBetrag());
          }
          reporter.closeTable();
          reporter.close();
          fos.close();
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
        }
        FileViewer.show(file);
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
