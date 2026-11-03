/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzAuswertungPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.io.AbrechnungSEPA;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.keys.ArbeitseinsatzAuswertung;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Jahr;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ArbeitseinsatzAbrechnungControl extends AbstractAbrechnungControl
{
  public static final String MITGLIED = "mitglied";

  public static final String GESAMTBETRAG = "gesamtbetrag";

  public static final int ZUSATZBETRAEGE = 1;

  public static final int ABRECHNUNG = 2;

  private JVereinTablePart arbeitseinsatzueberpruefungList;

  private ArbeitseinsatzAuswertungPart auswertungPart;

  private SelectInput suchjahr = null;

  private Zusatzbetrag zusatzb;

  private final ZusatzbetragPart part;

  private int typ = 1;

  public ArbeitseinsatzAbrechnungControl(int typ) throws RemoteException
  {
    super();
    this.typ = typ;
    zusatzb = getZusatzbetrag();
    part = new ZusatzbetragPart(zusatzb, false);
  }

  public ZusatzbetragPart getPart()
  {
    return part;
  }

  public SelectInput getSuchJahr() throws RemoteException
  {
    if (suchjahr != null)
    {
      return suchjahr;
    }
    ArrayList<Integer> jahre = new ArrayList<>();
    for (Jahr jahr : Jahr.getList())
    {
      jahre.add(jahr.getKey());
    }
    suchjahr = new SelectInput(jahre, settings.getInt("jahr", jahre.get(0)));
    suchjahr.setPreselected(settings.getInt("jahr", jahre.get(0)));
    suchjahr.addListener(new FilterListener());
    return suchjahr;
  }

  private Zusatzbetrag getZusatzbetrag() throws RemoteException
  {
    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    zusatzb.setStartdatum(new Date());
    zusatzb.setBuchungstext(settings.getString("buchungstext", ""));
    zusatzb.setBetrag(BigDecimal.ZERO);
    String zahlungsweg = settings.getString("zahlungsweg", "");
    if (zahlungsweg.length() > 0)
    {
      try
      {
        Zahlungsweg weg = new Zahlungsweg(Integer.valueOf(zahlungsweg));
        zusatzb.setZahlungsweg(weg);
      }
      catch (Exception e)
      {
        //
      }
    }
    zusatzb.setMitgliedzahltSelbst(
        settings.getBoolean("mitgliedzahltselbst", false));
    return zusatzb;
  }

  public List<Zusatzbetrag> getZusatzbetraegeList()
      throws RemoteException, ApplicationException
  {
    List<Zusatzbetrag> list = new ArrayList<>();
    for (PseudoDBObject o : auswertungPart.getList())
    {
      Mitglied m = (Mitglied) o.getAttribute(MITGLIED);
      Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
          .createObject(Zusatzbetrag.class, null);
      zb.setBetrag(o.getBigDecimal(GESAMTBETRAG).negate());
      zb.setBuchungstext((String) part.getBuchungstext().getValue());
      zb.setFaelligkeit((Date) getFaelligkeit().getValue());
      zb.setIntervall(IntervallZusatzzahlung.KEIN);
      zb.setMitglied(Integer.parseInt(m.getID()));
      zb.setStartdatum((Date) getFaelligkeit().getValue());
      zb.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
      zb.setMitgliedzahltSelbst(
          (Boolean) part.getMitgliedzahltSelbst().getValue());
      Beitragsgruppe b = m.getBeitragsgruppe();
      zb.setBuchungsart(b.getBuchungsart());
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        zb.setBuchungsklasseId(b.getBuchungsklasseId());
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        zb.setSteuer(b.getSteuer());
      }
      list.add(zb);
    }
    return list;
  }

  public JVereinTablePart getArbeitseinsatzUeberpruefungList()
      throws RemoteException
  {
    if (arbeitseinsatzueberpruefungList != null)
    {
      return arbeitseinsatzueberpruefungList;
    }
    else
    {
      auswertungPart = new ArbeitseinsatzAuswertungPart(null,
          ArbeitseinsatzAuswertung.MINDERLEISTUNG,
          (Integer) getSuchJahr().getValue());
      arbeitseinsatzueberpruefungList = auswertungPart
          .getArbeitseinsatzUeberpruefungList();
      return arbeitseinsatzueberpruefungList;
    }
  }

  private void refreshList()
  {
    try
    {
      if (arbeitseinsatzueberpruefungList != null)
      {
        saveSettings();
        auswertungPart.refreshList(ArbeitseinsatzAuswertung.MINDERLEISTUNG,
            (Integer) getSuchJahr().getValue());
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Tabellenaufbau", e);
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
      settings.setAttribute("jahr", (Integer) getSuchJahr().getValue());
      settings.setAttribute("buchungstext",
          (String) part.getBuchungstext().getValue());
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if (weg != null)
      {
        settings.setAttribute("zahlungsweg", weg.getKey());
      }
      else
      {
        settings.setAttribute("zahlungsweg", "");
      }
      Boolean tmp = (Boolean) part.getMitgliedzahltSelbst().getValue();
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
      // Prüfen ob das Verrechnungskonto gesetzt ist. Das wird auch beim
      // Abrechnungslauf am Anfang geholt.
      // Es ist nur nötig wenn abgerechnet wird, nicht wenn nur Zusatzbeträge
      // erzeugt werden
      if (isKompakteAbbuchungActiv())
      {
        checkVerrechnungskonto(bugs);
      }

      for (PseudoDBObject o : auswertungPart.getList())
      {
        Mitglied m = (Mitglied) o.getAttribute(MITGLIED);
        Mitglied zahler = m.getZahler();
        if ((Boolean) part.getMitgliedzahltSelbst().getValue())
        {
          zahler = m;
        }
        Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
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
  protected String checkInput()
  {
    try
    {
      if (getFaelligkeit().getValue() == null)
      {
        return ("Bitte Fälligkeit eingeben");
      }
      Zahlungsweg weg = (Zahlungsweg) part.getZahlungsweg().getValue();
      if (getFaelligkeit().getValue() != null && weg != null
          && weg.getKey() == Zahlungsweg.BASISLASTSCHRIFT)
      {
        if (((Date) getFaelligkeit().getValue()).before(new Date()))
        {
          return ("Fälligkeit muss bei Lastschriften in der Zukunft liegen!");
        }
      }

      if (part.getBuchungstext().getValue() == null
          || ((String) part.getBuchungstext().getValue()).isEmpty())
      {
        return ("Bitte Zahlungsgrund eingeben");
      }
      if (isRechnungActiv() && (boolean) getRechnung().getValue())
      {
        if (getRechnungsformular().getValue() == null)
        {
          return ("Bitte Rechnungsformular auswählen");
        }
        if (getRechnungsdatum().getValue() == null)
        {
          return ("Bitte Rechnungsdatum auswählen");
        }
      }
    }
    catch (RemoteException re)
    {
      return ("Fehler beim Auswerten der Eingabe!");
    }
    return null;
  }

  @Override
  protected AbrechnungSEPAParam getSEPAParam(SepaVersion sepaVersion)
      throws RemoteException, ApplicationException
  {
    return new AbrechnungSEPAParam(this, sepaVersion);
  }

  @Override
  protected void startAbrechnung() throws ApplicationException, RemoteException
  {
    switch (typ)
    {
      case ABRECHNUNG:
        new AbrechnungSEPA(getAbrechnungSEPAParam());
        break;
      case ZUSATZBETRAEGE:
        starteZusatzberaegeGenerierung();
        break;
    }
  }

  private void starteZusatzberaegeGenerierung()
      throws RemoteException, ApplicationException
  {
    BackgroundTask t = new BackgroundTask()
    {
      List<Zusatzbetrag> list = getZusatzbetraegeList();

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          DBTransaction.starten();
          for (Zusatzbetrag zb : list)
          {
            zb.store();
          }
          DBTransaction.commit();
          GUI.getStatusBar()
              .setSuccessText("Zusatzbeträge erfolgreich generiert");
        }
        catch (Exception e)
        {
          DBTransaction.rollback();
          Logger.error("Fehler beim Zusatzbeträge generieren", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
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
