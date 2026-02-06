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

import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.RechnungDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
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

    IGutschriftProvider[] providerArray = gcontrol.getProviderArray();

    for (IGutschriftProvider provider : providerArray)
    {
      if (!(provider instanceof Lastschrift)
          && provider.getGutschriftZahler() == null)
      {
        bugs.add(new Bug(Bug.WARNING, provider, "Kein Zahler konfiguriert!"));
      }

      if (provider.getGutschriftZahler() != null)
      {
        String iban = provider.getGutschriftZahler().getIban();
        if (iban == null || iban.isEmpty())
        {
          bugs.add(new Bug(Bug.WARNING, provider.getGutschriftZahler(),
              "Bei dem Mitglied ist keine IBAN gesetzt!"));
        }
      }

      if (provider.getBetrag() < -0.005d)
      {
        bugs.add(new Bug(Bug.WARNING, provider, "Der Betrag ist negativ!"));
      }

      if (provider.getIstSumme() < -0.005d)
      {
        bugs.add(new Bug(Bug.WARNING, provider,
            "Der Zahlungseingang ist negativ, dadurch kann nichts erstattet werden!"));
      }

      GutschriftParam params = gcontrol.getParams();
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
          List<Sollbuchung> list = ((Rechnung) provider).getSollbuchungList();
          if (list == null || list.size() == 0)
          {
            bugs.add(new Bug(Bug.WARNING, provider,
                "Die Rechnung hat keine Sollbuchungen!"));
          }

          if (list != null && list.size() > 1)
          {
            bugs.add(new Bug(Bug.WARNING, provider,
                "Fixer Betrag bei Gesamtrechnungen wird nicht unterstützt!"));
          }

          if (list != null && list.size() == 1)
          {
            sollbFix = ((Rechnung) provider).getSollbuchungList().get(0);
          }
        }

        if (provider instanceof Sollbuchung)
        {
          sollbFix = (Sollbuchung) provider;
        }

        if (sollbFix != null
            && !gcontrol.checkVorhandenePosten(sollbFix, ausgleichsbetrag))
        {
          bugs.add(new Bug(Bug.WARNING, provider,
              "Der Betrag der Sollbuchungspositionen ist nicht ausreichend!"));
        }

        if (ausgleichsbetrag > 0)
        {
          bugs.add(new Bug(Bug.HINT, provider,
              "Der Erstattungsbetrag wird mit offenen Forderungen verrechnet!"));
        }
      }
    }
    return bugs;
  }
}
