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
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.BuchungsartMenu;
import de.jost_net.JVerein.gui.view.BuchungDetailView;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.keys.SteuersatzBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.Dateiname;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungsartControl extends FilterControl
{
  private TablePart buchungsartList;

  private IntegerInput nummer;

  private Input bezeichnung;

  private SelectInput art;

  private SelectInput buchungsklasse;

  private CheckboxInput spende;

  private CheckboxInput abschreibung;

  private SelectInput steuersatz;

  private SelectInput steuer_buchungsart;

  private Buchungsart buchungsart;

  private SelectInput status;

  private TextInput suchbegriff;

  private CheckboxInput regexp;


  public BuchungsartControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  private Buchungsart getBuchungsart()
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = (Buchungsart) getCurrentObject();
    return buchungsart;
  }

  public IntegerInput getNummer(boolean withFocus) throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }
    nummer = new IntegerInput(getBuchungsart().getNummer());
    if (withFocus)
    {
      nummer.focus();
    }
    return nummer;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getBuchungsart().getBezeichnung(), 80);
    return bezeichnung;
  }

  public SelectInput getArt() throws RemoteException
  {
    if (art != null)
    {
      return art;
    }
    art = new SelectInput(ArtBuchungsart.getArray(),
        new ArtBuchungsart(getBuchungsart().getArt()));
    return art;
  }
  
  public SelectInput getStatus() throws RemoteException
  {
    if (status != null)
    {
      return status;
    }
    status = new SelectInput(StatusBuchungsart.getArray(),
        new StatusBuchungsart(getBuchungsart().getStatus()));
    return status;
  }

  public TextInput getSuchbegriff() throws RemoteException
  {
    if (suchbegriff != null)
    {
      return suchbegriff;
    }
    suchbegriff = new TextInput(getBuchungsart().getSuchbegriff(), 150);
    return suchbegriff;
  }

  public CheckboxInput getRegexp() throws RemoteException
  {
    if (regexp != null)
    {
      return regexp;
    }
    regexp = new CheckboxInput(getBuchungsart().getRegexp());
    return regexp;
  }

  public CheckboxInput getSpende() throws RemoteException
  {
    if (spende != null)
    {
      return spende;
    }
    spende = new CheckboxInput(getBuchungsart().getSpende());
    spende.addListener(new Listener()
    {
      // Listener enabled / disabled Steuer Felder falls eine Spende ausgewählt wurde
      // (Steuer und Spende schließen sich aus)
      @Override
      public void handleEvent(Event event)
      {
        // Disable steuersatz and buchungsart for type spende
        if ((Boolean) spende.getValue()) 
        {
          steuersatz.setPleaseChoose("Kein Steuersatz für Spenden");
          steuersatz.setList(null);
          steuersatz.setValue(null);
          steuersatz.disable();
          steuer_buchungsart.setPleaseChoose("Keine Buchungsart für Spenden");
          steuer_buchungsart.setList(null);
          steuer_buchungsart.setValue(null);
          steuer_buchungsart.disable();
        }
        else 
        {
          // Rebuild selectinput values if buchungsart is NOT spende
          steuersatz.setPleaseChoose(null);
          steuersatz.setList(SteuersatzBuchungsart.getArray());
          steuersatz.setValue(null);
          steuersatz.enable();
          steuer_buchungsart.setPleaseChoose("Bitte Steuersatz wählen");
          steuer_buchungsart.setList(null);
          steuer_buchungsart.setValue(null);
          steuer_buchungsart.disable();
        }
      }
    });
    return spende;
  }
  
  public CheckboxInput getAbschreibung() throws RemoteException
  {
    if (abschreibung != null)
    {
      return abschreibung;
    }
    abschreibung = new CheckboxInput(getBuchungsart().getAbschreibung());
    return abschreibung;
  }

  public SelectInput getSteuersatz() throws RemoteException
  {
    if (steuersatz != null)
    {
      return steuersatz;
    }
    steuersatz = new SelectInput(SteuersatzBuchungsart.getArray(), 
        new SteuersatzBuchungsart(getBuchungsart().getSteuersatz()));
    // Disable steuersatz for type spende
    if (getBuchungsart().getSpende()) 
    {
      steuersatz.setPleaseChoose("Kein Steuersatz für Spenden");
      steuersatz.setValue(null);
      steuersatz.setList(null);
      steuersatz.disable();
    }
    steuersatz.addListener(new Listener()
    {
      // Listener enabled / disabled Feld Buchungsart für Steuer falls Steuer = 0 ist
      @Override
      public void handleEvent(Event event)
      {
        SteuersatzBuchungsart steuersatzItem = (SteuersatzBuchungsart) steuersatz.getValue();
        Double steuersatzValue = (steuersatzItem == null) ? Double.valueOf(0) : (Double) steuersatzItem.getSteuersatz();
        if (steuersatzValue == null || steuersatzValue == 0)
        {
          // disable und auf 0 setzen
          steuer_buchungsart.setPleaseChoose("Bitte Steuersatz wählen");
          steuer_buchungsart.setValue(null);
          steuer_buchungsart.setList(null);
          steuer_buchungsart.disable();
        }
        else 
        {
          try
          {
            DBIterator<Buchungsart> it = getFilteredBuchungsart();
            @SuppressWarnings("unchecked")
            List<Buchungsart> buchungsartenListe = it != null ? PseudoIterator.asList(it) : null;
            steuer_buchungsart.setPleaseChoose("Bitte auswählen");
            steuer_buchungsart.setAttribute(getBuchungartAttribute());
            steuer_buchungsart.setList(buchungsartenListe);
            steuer_buchungsart.enable();
          }
          catch (RemoteException e)
          {
            Logger.error(e.getMessage());
          }
        }
      }
    });
    return steuersatz;
  }

  public SelectInput getSteuerBuchungsart() throws RemoteException
  {
    if (steuer_buchungsart != null)
    {
      return steuer_buchungsart;
    }
    
    Boolean isSpende = getBuchungsart().getSpende();
    Boolean hasSteuersatz = ((getSteuersatz().getValue() != null) && (getSteuersatz().getValue().toString().length() > 0)) ? true : false;
    
    DBIterator<Buchungsart> it = (!isSpende && hasSteuersatz) ? getFilteredBuchungsart() : null;
    steuer_buchungsart = new SelectInput(it != null ? PseudoIterator.asList(it) : null, null);
    if (it != null)
    {
      @SuppressWarnings("unchecked")
      List<Buchungsart> buchungsartenListe = it != null ? PseudoIterator.asList(it) : null;
      steuer_buchungsart.setAttribute(getBuchungartAttribute());
      steuer_buchungsart.setPleaseChoose("Bitte auswählen");
      steuer_buchungsart.setPreselected(getBuchungsart().getSteuerBuchungsart());
      steuer_buchungsart.setList(buchungsartenListe);
      steuer_buchungsart.setMandatory(true);
    }
    else
    {
      String pleaseChoose = null;
      if (isSpende)
      {
        pleaseChoose = "Keine Buchungsart für Spenden";
      }
      else if (!hasSteuersatz)
      {
        pleaseChoose = "Bitte Steuersatz wählen";
      }
      steuer_buchungsart.setPleaseChoose(pleaseChoose);
      steuer_buchungsart.setValue(null);
      steuer_buchungsart.setList(null);
      steuer_buchungsart.setMandatory(false);
      steuer_buchungsart.disable();
    }
    
    return steuer_buchungsart;
  }
  
  public String getBuchungartAttribute()
  {
    try
    {
      switch (Einstellungen.getEinstellung().getBuchungsartSort())
      {
        case BuchungsartSort.NACH_NUMMER:
          return "nrbezeichnung";
        case BuchungsartSort.NACH_BEZEICHNUNG_NR:
          return "bezeichnungnr";
        default:
          return "bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    
    return "bezeichnung";
  }

  public String getBuchungartSortOrder()
  {
    try
    {
      switch (Einstellungen.getEinstellung().getBuchungsartSort())
      {
        case BuchungsartSort.NACH_NUMMER:
          return "ORDER BY nummer";
        default:
          return "ORDER BY bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    
    return "ORDER BY bezeichnung";
  }
  
  public DBIterator<Buchungsart> getFilteredBuchungsart() throws RemoteException
  {
    DBIterator<Buchungsart> it = Einstellungen.getDBService()
        .createList(Buchungsart.class);
    it.setOrder("ORDER BY nummer");
    // Do not allow to select oneself
    if (getBuchungsart().getID() != null) it.addFilter("id != " + getBuchungsart().getID());
    it.addFilter("(spende = false OR spende IS NULL)");
    it.addFilter("(steuersatz = 0 OR steuersatz IS NULL)");
    it.addFilter("buchungsart.status != ?", StatusBuchungsart.INACTIVE);

    return it;
  }
  
  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    list.setOrder(getBuchungartSortOrder());
    buchungsklasse = new SelectInput(list != null ? PseudoIterator.asList(list) : null,
        getBuchungsart().getBuchungsklasse());
    buchungsklasse.setValue(getBuchungsart().getBuchungsklasse());
    buchungsklasse.setAttribute(getBuchungartAttribute());
    buchungsklasse.setPleaseChoose("Bitte auswählen");
    return buchungsklasse;
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    try
    {
      Buchungsart b = getBuchungsart();
      try
      {
        b.setNummer(((Integer) getNummer(false).getValue()).intValue());
      }
      catch (NullPointerException e)
      {
        GUI.getStatusBar().setErrorText("Nummer fehlt");
        return;
      }
      b.setBezeichnung((String) getBezeichnung().getValue());
      ArtBuchungsart ba = (ArtBuchungsart) getArt().getValue();
      b.setArt(ba.getKey());
      if (buchungsklasse != null)
      {
        GenericObject o = (GenericObject) getBuchungsklasse().getValue();
        if (o != null)
        {
          b.setBuchungsklasseId(Long.valueOf(o.getID()));
        }
        else
        {
          b.setBuchungsklasseId(null);
        }
      }
      else
      {
        b.setBuchungsklasseId(null);
      }
      b.setSpende((Boolean) spende.getValue());
      b.setAbschreibung((Boolean) abschreibung.getValue());
      double steuersatzValue = (SteuersatzBuchungsart) steuersatz.getValue() == null ? 0 : ((SteuersatzBuchungsart) steuersatz.getValue()).getSteuersatz();
      b.setSteuersatz(steuersatzValue);
      if (steuer_buchungsart.getValue() instanceof Buchungsart) 
      {
        b.setSteuerBuchungsart(Long.parseLong(((Buchungsart) steuer_buchungsart.getValue()).getID()));
      }
      else
      {
        b.setSteuerBuchungsart(null);
      }
      StatusBuchungsart st = (StatusBuchungsart) getStatus().getValue();
      b.setStatus(st.getKey());
      b.setSuchbegriff((String) getSuchbegriff().getValue());
      b.setRegexp((Boolean) getRegexp().getValue());
      if ((Boolean) getRegexp().getValue())
      {
        try
        {
          Pattern.compile((String) getSuchbegriff().getValue());
        }
        catch (PatternSyntaxException pse)
        {
          throw new ApplicationException(
              "Regulärer Ausdruck ungültig: " + pse.getDescription());
        }
      }
      try
      {
        b.store();
        GUI.getStatusBar().setSuccessText("Buchungsart gespeichert");
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Buchungsart";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getBuchungsartList() throws RemoteException
  {
    if (buchungsartList == null)
    {
      buchungsartList = new TablePart(getBuchungsarten(),
          new EditAction(BuchungDetailView.class));
      buchungsartList.addColumn("Nummer", "nummer");
      buchungsartList.addColumn("Bezeichnung", "bezeichnung");
      buchungsartList.addColumn("Art", "art", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          if (o == null)
          {
            return "";
          }
          if (o instanceof Integer)
          {
            return ArtBuchungsart.get((Integer) o);
          }
          return "ungültig";
        }
      }, false, Column.ALIGN_LEFT);
      buchungsartList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
      buchungsartList.addColumn("Spende", "spende", new JaNeinFormatter());
      buchungsartList.addColumn("Abschreibung", "abschreibung",
          new JaNeinFormatter(), false, Column.ALIGN_RIGHT);
      buchungsartList.addColumn("Steuersatz", "steuersatz", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          if (o == null)
          {
            return "";
          }
          if (o instanceof Double)
          {
            return SteuersatzBuchungsart.get((Double) o);
          }
          return "ungültig";
        }
      }, false, Column.ALIGN_RIGHT);
      buchungsartList.addColumn("Steuer Buchungsart", "steuerbuchungsart",
          new BuchungsartFormatter());
      buchungsartList.addColumn("Status", "status", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          if (o == null)
          {
            return "";
          }
          if (o instanceof Integer)
          {
            return StatusBuchungsart.get((Integer) o);
          }
          return "ungültig";
        }
      }, false, Column.ALIGN_LEFT);
      buchungsartList.addColumn("Suchtext", "suchbegriff");
      buchungsartList.setContextMenu(new BuchungsartMenu());
      buchungsartList.setMulti(true);
      buchungsartList.setRememberColWidths(true);
      buchungsartList.setRememberOrder(true);
      buchungsartList.setRememberState(true);
      buchungsartList.addFeature(new FeatureSummary());
    }
    else
    {
      buchungsartList.removeAll();
      DBIterator<Buchungsart> buchungsarten = getBuchungsarten();
      while (buchungsarten.hasNext())
      {
        buchungsartList.addItem(buchungsarten.next());
      }
      buchungsartList.sort();
    }
    return buchungsartList;
  }

  private DBIterator<Buchungsart> getBuchungsarten() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Buchungsart> buchungsarten = service
        .createList(Buchungsart.class);

    if (isSuchStatusAktiv() && 
        getSuchStatus(null).getValue().toString()
            .equalsIgnoreCase("Ohne Deaktiviert"))
      buchungsarten.addFilter("status != ?", new Object[] { StatusBuchungsart.INACTIVE });
    if (isSuchnameAktiv() && !getSuchname().getValue().equals(""))
    {
      String text = "%" + ((String) getSuchname().getValue()).toUpperCase()
          + "%";
      buchungsarten.addFilter("nummer like ?", new Object[] { text });
    }
    if (isSuchtextAktiv() && !getSuchtext().getValue().equals(""))
    {
      String text = "%" + ((String) getSuchtext().getValue()).toUpperCase()
          + "%";
      buchungsarten.addFilter("UPPER(bezeichnung) like ?",
          new Object[] { text });
    }
    if (isSuchBuchungsartArtAktiv() && getSuchBuchungsartArt().getValue() != null)
    {
      ArtBuchungsart art = (ArtBuchungsart) getSuchBuchungsartArt().getValue();
      buchungsarten.addFilter("art = ?", new Object[] { art.getKey() });
    }
    if (isSuchBuchungsklasseAktiv() && getSuchBuchungsklasse().getValue() != null)
    {
      Buchungsklasse tmp = (Buchungsklasse) getSuchBuchungsklasse().getValue();
      buchungsarten.addFilter("buchungsklasse = ?", new Object[] { tmp.getID() });
    }
    buchungsarten.setOrder("ORDER BY nummer");
    return buchungsarten;
  }

  public void TabRefresh()
  {
    try
    {
      getBuchungsartList();
    }
    catch (RemoteException e)
    {
      //
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
              "Fehler beim Start der PDF-Ausgabe der Buchungsarten");
        }
      }
    }, null, false, "file-pdf.png");
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
    fd.setFileName(new Dateiname("buchungsarten", "",
        Einstellungen.getEinstellung().getDateinamenmuster(), "pdf").get());
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
    final DBIterator<Buchungsart> it = getBuchungsarten();
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          FileOutputStream fos = new FileOutputStream(file);
          Reporter reporter = new Reporter(fos, "Buchungsarten", "", it.size());
          reporter.addHeaderColumn("Nummer", Element.ALIGN_LEFT, 20,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Bezeichnung", Element.ALIGN_LEFT, 80,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Art", Element.ALIGN_LEFT, 25,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Buchungsklasse", Element.ALIGN_LEFT, 80,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Spende", Element.ALIGN_CENTER, 20,
              BaseColor.LIGHT_GRAY);          
          reporter.addHeaderColumn("Steuersatz", Element.ALIGN_CENTER, 25,
              BaseColor.LIGHT_GRAY);                        
          reporter.addHeaderColumn("Steuer Buchungsart", Element.ALIGN_CENTER, 30,
              BaseColor.LIGHT_GRAY);
          reporter.createHeader();
          while (it.hasNext())
          {
            Buchungsart b = it.next();
            reporter.addColumn(b.getNummer() + "", Element.ALIGN_RIGHT);
            reporter.addColumn(b.getBezeichnung(), Element.ALIGN_LEFT);
            reporter.addColumn(ArtBuchungsart.get(b.getArt()), Element.ALIGN_LEFT);
            if (b.getBuchungsklasse() != null)
            {
              reporter.addColumn(b.getBuchungsklasse().getBezeichnung(),
                  Element.ALIGN_LEFT);
            }
            else
            {
              reporter.addColumn("", Element.ALIGN_LEFT);
            }
            reporter.addColumn(b.getSpende());
            reporter.addColumn(SteuersatzBuchungsart.get(b.getSteuersatz()), Element.ALIGN_RIGHT);
            if (b.getSteuerBuchungsart() != null) {
              reporter.addColumn(b.getSteuerBuchungsart().getNummer() + "", Element.ALIGN_RIGHT);
            }
            else {
              reporter.addColumn("", Element.ALIGN_LEFT);
            }
          }
          reporter.closeTable();
          reporter.close();
          fos.close();
          GUI.getStatusBar().setSuccessText("Auswertung gestartet");
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
