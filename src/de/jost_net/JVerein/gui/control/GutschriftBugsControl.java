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
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Settings;

public class GutschriftBugsControl extends AbstractControl
{

  private Settings settings = null;

  private TablePart bugsList;

  private GutschriftControl gcontrol;

  public GutschriftBugsControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    gcontrol = (GutschriftControl) this.view.getCurrentObject();
  }

  public Part getBugsList() throws RemoteException
  {
    bugsList = new TablePart(getBugs(), context -> {
      Bug bug = (Bug) context;
      Object object = bug.getProvider();
      if (object instanceof Mitglied)
      {
        GUI.startView(MitgliedDetailView.class, object);
      }
      if (object instanceof Lastschrift)
      {
        GUI.startView(LastschriftDetailView.class, object);
      }
      if (object instanceof Rechnung)
      {
        GUI.startView(RechnungDetailView.class, object);
      }
      if (object instanceof Sollbuchung)
      {
        GUI.startView(SollbuchungDetailView.class, object);
      }
    });
    bugsList.addColumn("Typ", "objektName");
    bugsList.addColumn("ID", "objektId");
    bugsList.addColumn("Zahler", "zahlerName");
    bugsList.addColumn("Meldung", "meldung");
    bugsList.addColumn("Klassifikation", "klassifikationText");
    bugsList.setRememberColWidths(true);
    bugsList.setRememberOrder(true);
    return bugsList;
  }

  private List<Bug> getBugs() throws RemoteException
  {
    ArrayList<Bug> bugs = new ArrayList<>();

    for (IGutschriftProvider provider : gcontrol.getProviderArray())
    {
      doChecks(provider, gcontrol.getParams(), bugs);
    }

    if (bugs.isEmpty())
    {
      bugs.add(new Bug(Bug.HINT, null, "Es wurden keine Probleme gefunden!"));
    }
    return bugs;
  }

  public static String doChecks(IGutschriftProvider provider,
      GutschriftParam params, ArrayList<Bug> bugs) throws RemoteException
  {
    String meldung;

    if (!(provider instanceof Lastschrift)
        && provider.getGutschriftZahler() == null)
    {
      meldung = "Kein Zahler konfiguriert!";
      if (bugs != null)
      {
        bugs.add(new Bug(Bug.WARNING, provider, meldung));
      }
      else
      {
        return meldung;
      }
    }

    // Bei Lastschrift ohne Zahler erstatten wir auf das gleiche Konto
    // wie bei der Lastschrift
    if (provider.getGutschriftZahler() != null)
    {
      String iban = provider.getGutschriftZahler().getIban();
      if (iban == null || iban.isEmpty())
      {
        meldung = "Bei dem Mitglied ist keine IBAN gesetzt!";
        if (bugs != null)
        {
          bugs.add(
              new Bug(Bug.WARNING, provider.getGutschriftZahler(), meldung));
        }
        else
        {
          return meldung;
        }
      }
    }

    // Keine Gutschrift bei Erstattungen
    if (provider.getBetrag() < -0.005d)
    {
      meldung = "Der Betrag ist negativ!";
      if (bugs != null)
      {
        bugs.add(new Bug(Bug.WARNING, provider, meldung));
      }
      else
      {
        return meldung;
      }
    }

    // Keine Gutschrift bei negativer Einzahlung
    if (provider.getIstSumme() < -0.005d)
    {
      meldung = "Der Zahlungseingang ist negativ, dadurch kann nichts erstattet werden!";
      if (bugs != null)
      {
        bugs.add(new Bug(Bug.WARNING, provider, meldung));
      }
      else
      {
        return meldung;
      }
    }

    if (provider instanceof Sollbuchung)
    {
      meldung = checkSollbuchung((Sollbuchung) provider, bugs);
      if (meldung != null)
      {
        return meldung;
      }
    }

    List<Sollbuchung> sollbList = null;
    if (provider instanceof Rechnung)
    {
      sollbList = ((Rechnung) provider).getSollbuchungList();
      if (sollbList == null || sollbList.isEmpty())
      {
        meldung = "Die Rechnung hat keine Sollbuchungen!";
        if (bugs != null)
        {
          bugs.add(new Bug(Bug.WARNING, provider, meldung));
        }
        else
        {
          return meldung;
        }
      }
      if (sollbList != null)
      {
        for (Sollbuchung sollb : sollbList)
        {
          meldung = checkSollbuchung((Sollbuchung) sollb, bugs);
          if (meldung != null)
          {
            return meldung;
          }
        }
      }
    }

    if (params.isFixerBetragAbrechnen())
    {
      // Beträge bestimmen
      double tmp = provider.getBetrag() - provider.getIstSumme();
      double offenbetrag = tmp > 0.005d ? tmp : 0;
      tmp = params.getFixerBetrag() - offenbetrag;
      double ueberweisungsbetrag = tmp > 0.005d ? tmp : 0;
      tmp = params.getFixerBetrag() - ueberweisungsbetrag;
      double ausgleichsbetrag = tmp > 0.005d ? tmp : 0;

      Sollbuchung sollbFix = null;
      if (provider instanceof Rechnung)
      {
        // Fixer Betrag bei Gesamtrechnung wird nicht unterstützt
        // Bei welcher Sollbuchung soll man da die Erstattung ausgleichen?
        if (sollbList != null && sollbList.size() > 1)
        {
          meldung = "Fixer Betrag bei Gesamtrechnungen wird nicht unterstützt!";
          if (bugs != null)
          {
            bugs.add(new Bug(Bug.WARNING, provider, meldung));
          }
          else
          {
            return meldung;
          }
        }

        if (sollbList != null && sollbList.size() == 1)
        {
          sollbFix = ((Rechnung) provider).getSollbuchungList().get(0);
        }
      }

      if (provider instanceof Sollbuchung)
      {
        sollbFix = (Sollbuchung) provider;
      }

      if (sollbFix != null
          && !checkVorhandenePosten(sollbFix, params, ausgleichsbetrag))
      {
        meldung = "Der Betrag der passenden Sollbuchungspositionen ist nicht ausreichend!";
        if (bugs != null)
        {
          bugs.add(new Bug(Bug.WARNING, provider, meldung));
        }
        else
        {
          return meldung;
        }
      }

      if (ausgleichsbetrag > 0)
      {
        meldung = "Der Erstattungsbetrag wird mit offenen Forderungen verrechnet!";
        if (bugs != null)
        {
          bugs.add(new Bug(Bug.HINT, provider, meldung));
        }
      }
    }
    return null;
  }

  private static String checkSollbuchung(Sollbuchung sollb, ArrayList<Bug> bugs)
      throws RemoteException
  {
    String meldung;
    List<SollbuchungPosition> posList = sollb.getSollbuchungPositionList();
    if (posList == null || posList.isEmpty())
    {
      meldung = "Die Sollbuchung hat keine Sollbuchungspositionen!";
      if (bugs != null)
      {
        bugs.add(new Bug(Bug.WARNING, sollb, meldung));
      }
      else
      {
        return meldung;
      }
    }
    else
    {
      boolean bug = false;
      for (SollbuchungPosition pos : posList)
      {
        if (pos.getBuchungsart() == null)
        {
          bug = true;
          break;
        }
      }
      if (bug)
      {
        meldung = "Es haben nicht alle Sollbuchungspositionen eine Buchungsart!";
        if (bugs != null)
        {
          bugs.add(new Bug(Bug.WARNING, sollb, meldung));
        }
        else
        {
          return meldung;
        }
      }
    }
    return null;
  }

  private static boolean checkVorhandenePosten(Sollbuchung sollb,
      GutschriftParam params, double ausgleichsbetrag) throws RemoteException
  {
    boolean buchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    double summe = 0;
    for (SollbuchungPosition pos : sollb.getSollbuchungPositionList())
    {
      if (pos.getBuchungsart() == null
          || (buchungsklasseInBuchung && pos.getBuchungsklasse() == null))
      {
        continue;
      }
      String posSteuer = pos.getSteuer() != null ? pos.getSteuer().getID()
          : "0";
      String paramsSteuer = params.getSteuer() != null
          ? params.getSteuer().getID()
          : "0";
      if (!pos.getBuchungsart().getID().equals(params.getBuchungsart().getID())
          || (buchungsklasseInBuchung && !pos.getBuchungsklasse().getID()
              .equals(params.getBuchungsklasse().getID()))
          || (steuerInBuchung && !posSteuer.equals(paramsSteuer)))
      {
        continue;
      }
      summe += pos.getBetrag();
    }
    if (summe - params.getFixerBetrag() < -0.005d)
    {
      // Es gibt nicht genügend Betrag für die Erstattung
      return false;
    }
    if (ausgleichsbetrag > 0)
    {
      // Der Position kann nicht mehr zugewiesen werden als noch frei ist
      double zugewiesen = 0;
      for (Buchung bu : sollb.getBuchungList())
      {
        if (bu.getBuchungsart() == null
            || (buchungsklasseInBuchung && bu.getBuchungsklasse() == null))
        {
          continue;
        }
        String buSteuer = bu.getSteuer() != null ? bu.getSteuer().getID() : "0";
        String paramsSteuer = params.getSteuer() != null
            ? params.getSteuer().getID()
            : "0";
        if (!bu.getBuchungsart().getID().equals(params.getBuchungsart().getID())
            || (buchungsklasseInBuchung && !bu.getBuchungsklasse().getID()
                .equals(params.getBuchungsklasse().getID()))
            || (steuerInBuchung && !buSteuer.equals(paramsSteuer)))
        {
          continue;
        }
        zugewiesen += bu.getBetrag();
      }
      if (summe - zugewiesen - ausgleichsbetrag < -0.005d)
      {
        // Es gibt nicht genügend unausgeglichene Beträge für den Ausgleich
        return false;
      }
    }
    return true;
  }
}
