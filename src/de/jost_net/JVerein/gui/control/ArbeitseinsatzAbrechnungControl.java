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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.ArbeitseinsatzUeberpruefungInput;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzUeberpruefungList;
import de.jost_net.JVerein.gui.parts.JVereinTablePart.ExportArt;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.io.ArbeitseinsatzZeile;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ArbeitseinsatzAbrechnungControl extends AbstractAbrechnungControl
{
  private ArbeitseinsatzUeberpruefungList arbeitseinsatzueberpruefungList;

  private SelectInput suchjahr = null;

  private ArbeitseinsatzUeberpruefungInput auswertungschluessel = null;

  private TextInput buchungstext;

  private SelectInput zahlungsweg;

  private CheckboxInput mitgliedZahltSelbst;

  public ArbeitseinsatzAbrechnungControl() throws RemoteException
  {
    super();
  }

  public ArbeitseinsatzUeberpruefungInput getAuswertungSchluessel()
      throws RemoteException
  {
    if (auswertungschluessel != null)
    {
      return auswertungschluessel;
    }
    auswertungschluessel = new ArbeitseinsatzUeberpruefungInput(1);
    return auswertungschluessel;
  }

  public SelectInput getSuchJahr() throws RemoteException
  {
    if (suchjahr != null)
    {
      return suchjahr;
    }
    DBIterator<Arbeitseinsatz> list = Einstellungen.getDBService()
        .createList(Arbeitseinsatz.class);
    list.setOrder("ORDER BY datum");
    Arbeitseinsatz ae = null;
    Calendar von = Calendar.getInstance();
    if (list.hasNext())
    {
      ae = list.next();
      von.setTime(ae.getDatum());
    }
    Calendar bis = Calendar.getInstance();
    ArrayList<Integer> jahre = new ArrayList<>();

    for (int i = von.get(Calendar.YEAR); i <= bis.get(Calendar.YEAR); i++)
    {
      jahre.add(i);
    }

    suchjahr = new SelectInput(jahre, settings.getInt("jahr", jahre.get(0)));
    suchjahr.setPreselected(settings.getInt("jahr", bis.get(Calendar.YEAR)));
    suchjahr.addListener(new FilterListener());
    return suchjahr;
  }

  public TextInput getBuchungstext() throws RemoteException
  {
    if (buchungstext != null)
    {
      return buchungstext;
    }
    buchungstext = new TextInput(settings.getString("buchungstext", ""), 140);
    buchungstext.setMandatory(true);
    return buchungstext;
  }

  public SelectInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }
    Zahlungsweg weg = new Zahlungsweg(Zahlungsweg.STANDARD);
    String wegsetting = settings.getString("zahlungsweg", "");
    if (wegsetting.length() > 0)
    {
      try
      {
        weg = new Zahlungsweg(Integer.valueOf(wegsetting));
      }
      catch (Exception e)
      {
        //
      }
    }
    zahlungsweg = new SelectInput(Zahlungsweg.getArray(), weg);
    zahlungsweg.setPleaseChoose("Standard");
    return zahlungsweg;
  }

  public CheckboxInput getMitgliedzahltSelbst() throws RemoteException
  {
    if (mitgliedZahltSelbst != null)
    {
      return mitgliedZahltSelbst;
    }
    mitgliedZahltSelbst = new CheckboxInput(
        settings.getBoolean("mitgliedzahltselbst", false));
    mitgliedZahltSelbst
        .setName(" *Falls ein abweichender Zahler konfiguriert ist.");
    return mitgliedZahltSelbst;
  }

  public Button getVariablenButton() throws RemoteException
  {
    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = MitgliedMap.getDummyMap(map);
    Button b = new Button("Buchungstext Variablen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");
    return b;
  }

  public Button exportButton(ExportArt art) throws ApplicationException
  {
    return new Button(art.equals(ExportArt.PDF) ? "PDF" : "CSV", context -> {
      if (arbeitseinsatzueberpruefungList
          .getArbeitseinsatzUeberpruefungList() == null)
      {
        throw new ApplicationException(
            "Der Export kann nicht durchgeführt werden, Tabelle ist nicht geladen.");
      }
      try
      {
        arbeitseinsatzueberpruefungList.getArbeitseinsatzUeberpruefungList()
            .export(
                VorlageUtil.getName(
                    VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_TITEL, this),
                VorlageUtil.getName(
                    VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL, this),
                VorlageUtil.getName(
                    VorlageTyp.AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME, this),
                art);
      }
      catch (OperationCanceledException ex)
      {
        // Ignorieren und Dialog nicht schließen
        return;
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
    }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  private void starteArbeitseinsatzGenerierung()
      throws RemoteException, ApplicationException
  {
    final GenericIterator<ArbeitseinsatzZeile> it = getIterator();
    String text = (String) getBuchungstext().getValue();
    Date faelligkeit = (Date) getFaelligkeit().getValue();
    Zahlungsweg zahlungsweg = (Zahlungsweg) getZahlungsweg().getValue();
    Boolean mitgliedzahltselbst = (Boolean) getMitgliedzahltSelbst().getValue();

    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          DBTransaction.starten();
          monitor.setStatusText("Starte die Generierung der Zusatzbeträge");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);

          int i = 0;
          while (it.hasNext())
          {
            if (isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
              monitor.setStatusText("Generierung abgebrochen.");
              monitor.setPercentComplete(100);
              throw new OperationCanceledException();
            }

            ArbeitseinsatzZeile z = (ArbeitseinsatzZeile) it.next();
            Mitglied m = (Mitglied) Einstellungen.getDBService().createObject(
                Mitglied.class, (String) z.getAttribute("mitgliedid"));
            Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
                .createObject(Zusatzbetrag.class, null);
            Double betrag = (Double) z.getAttribute("gesamtbetrag");
            betrag = betrag * -1;
            zb.setBetrag(betrag);
            String vzweck = text;
            boolean ohneLesefelder = !vzweck
                .contains(Einstellungen.LESEFELD_PRE);
            Map<String, Object> map = new AllgemeineMap().getMap(null);
            map = new MitgliedMap().getMap(m, map, ohneLesefelder);
            try
            {
              vzweck = VelocityTool.eval(map, vzweck);
            }
            catch (IOException e)
            {
              Logger.error("Fehler bei der Aufbereitung der Variablen", e);
            }
            zb.setBuchungstext(vzweck);
            zb.setFaelligkeit(faelligkeit);
            zb.setStartdatum(faelligkeit);
            zb.setIntervall(IntervallZusatzzahlung.KEIN);
            zb.setMitglied(
                Integer.valueOf((String) z.getAttribute("mitgliedid")));
            zb.setZahlungsweg(zahlungsweg);
            zb.setMitgliedzahltSelbst(mitgliedzahltselbst);
            Beitragsgruppe b = m.getBeitragsgruppe();
            zb.setBuchungsart(b.getBuchungsart());
            if ((Boolean) Einstellungen
                .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
            {
              zb.setBuchungsklasseId(b.getBuchungsklasseId());
            }
            zb.setSteuer(b.getSteuer());
            zb.store();
            i++;
            monitor.setPercentComplete(100 * i / it.size());
            monitor.setStatusText("Zusatzbetrag generiert für " + m.getName()
                + ", " + m.getVorname() + " über "
                + Einstellungen.DECIMALFORMAT.format(betrag) + "€");
          }
          monitor.setPercentComplete(100);
          DBTransaction.commit();
        }
        catch (Exception e)
        {
          DBTransaction.rollback();
          Logger.error("Fehler beim Zusatzbeträge erstellen", e);
          GUI.getStatusBar().setErrorText(
              "Fehler beim Zusatzbeträge erstellen: " + e.getMessage());
          throw new ApplicationException("Fehler beim Zusatzbeträge erstellen",
              e);
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

  private GenericIterator<ArbeitseinsatzZeile> getIterator()
      throws RemoteException
  {
    ArrayList<ArbeitseinsatzZeile> zeile = arbeitseinsatzueberpruefungList
        .getInfo();
    @SuppressWarnings("unchecked")
    GenericIterator<ArbeitseinsatzZeile> gi = PseudoIterator
        .fromArray(zeile.toArray(new GenericObject[zeile.size()]));
    return gi;
  }

  public Part getArbeitseinsatzUeberpruefungList() throws ApplicationException
  {
    try
    {
      settings.setAttribute("jahr", (Integer) getSuchJahr().getValue());
      settings.setAttribute("schluessel",
          (Integer) getAuswertungSchluessel().getValue());

      if (arbeitseinsatzueberpruefungList == null)
      {
        arbeitseinsatzueberpruefungList = new ArbeitseinsatzUeberpruefungList(
            null, (Integer) getSuchJahr().getValue(),
            (Integer) getAuswertungSchluessel().getValue());
      }
      else
      {
        arbeitseinsatzueberpruefungList
            .setJahr((Integer) getSuchJahr().getValue());
        arbeitseinsatzueberpruefungList
            .setSchluessel((Integer) getAuswertungSchluessel().getValue());
        ArrayList<ArbeitseinsatzZeile> zeile = arbeitseinsatzueberpruefungList
            .getInfo();
        arbeitseinsatzueberpruefungList.removeAll();
        for (ArbeitseinsatzZeile az : zeile)
        {
          arbeitseinsatzueberpruefungList.addItem(az);
        }
        arbeitseinsatzueberpruefungList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler aufgetreten", e);
    }
    return arbeitseinsatzueberpruefungList.getArbeitseinsatzUeberpruefungList();
  }

  private void refreshList()
  {
    try
    {
      getArbeitseinsatzUeberpruefungList();
    }
    catch (ApplicationException e1)
    {
      //
    }
  }

  private class FilterListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      refreshList();
    }
  }

  @Override
  protected void saveSettings()
  {
    super.saveSettings();
    try
    {
      settings.setAttribute("buchungstext",
          (String) getBuchungstext().getValue());
      Zahlungsweg weg = (Zahlungsweg) getZahlungsweg().getValue();
      if (weg != null)
      {
        settings.setAttribute("zahlungsweg", weg.getKey());
      }
      else
      {
        settings.setAttribute("zahlungsweg", "");
      }
      Boolean tmp = (Boolean) getMitgliedzahltSelbst().getValue();
      if (tmp != null)
      {
        settings.setAttribute("mitgliedzahltselbst", tmp);
      }
      else
      {
        settings.setAttribute("mitgliedzahltselbst", "false");
      }
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler", re);
    }
  }

  @Override
  protected List<Bug> getBugs()
  {
    ArrayList<Bug> bugs = new ArrayList<>();
    boolean global = true;

    try
    {
      ArrayList<ArbeitseinsatzZeile> zeilen = arbeitseinsatzueberpruefungList
          .getInfo();
      for (ArbeitseinsatzZeile zeile : zeilen)
      {
        Mitglied m = (Mitglied) Einstellungen.getDBService().createObject(
            Mitglied.class, (String) zeile.getAttribute("mitgliedid"));
        Mitglied zahler = m.getZahler();
        if ((Boolean) getMitgliedzahltSelbst().getValue())
        {
          zahler = m;
        }
        Zahlungsweg weg = (Zahlungsweg) getZahlungsweg().getValue();
        if ((weg == null
            && zahler.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT)
            || (weg != null && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT))
        {
          if (global)
          {
            checkGlobal(bugs);
            checkGlaeubigerId(bugs);
            checkFaelligkeit((Date) getFaelligkeit().getValue(), bugs);
            global = false;
          }
          checkMitgliedKontodaten(zahler, bugs);
          if (!(Boolean) getSEPACheck().getValue())
          {
            checkSEPA(zahler, bugs);
          }
        }
      }

      if (bugs.isEmpty())
      {
        bugs.add(new Bug(null, KEINFEHLER, Bug.HINT));
      }
    }
    catch (Exception ex)
    {
      bugs.add(new Bug(null, ex.getMessage(), Bug.ERROR));
    }

    return bugs;
  }

  @Override
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    // wird hier nicht verwendet
    return null;
  }

  @Override
  protected String checkInput()
  {
    try
    {
      int i = (Integer) auswertungschluessel.getValue();
      if (i != ArbeitseinsatzUeberpruefungInput.MINDERLEISTUNG)
      {
        return ("Auswertung wird nur bei Minderleistung durchgeführt");
      }
      if (getFaelligkeit().getValue() == null)
      {
        return ("Bitte Fälligkeit eingeben");
      }
      Zahlungsweg weg = (Zahlungsweg) getZahlungsweg().getValue();
      if (getFaelligkeit().getValue() != null && weg != null
          && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT)
      {
        if (((Date) getFaelligkeit().getValue()).before(new Date()))
        {
          return ("Fälligkeit muss bei Lastschriften in der Zukunft liegen!");
        }
      }

      if (getBuchungstext().getValue() == null
          || ((String) getBuchungstext().getValue()).isEmpty())
      {
        return ("Bitte Buchungstext eingeben");
      }
    }
    catch (RemoteException re)
    {
      return ("Fehler beim Auswerten der Eingabe!");
    }
    return null;
  }

  @Override
  protected void doStart() throws ApplicationException, RemoteException
  {
    starteArbeitseinsatzGenerierung();
  }
}
