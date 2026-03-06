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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.Bug;
import de.jost_net.OBanToo.SEPA.BIC;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbstractAbrechnungControl
{
  protected final String KEINFEHLER = "Es wurden keine Probleme gefunden.";

  protected LabelInput status = null;

  protected JVereinTablePart bugsList;

  private Date sepagueltigkeit;

  private Boolean individuelleBetraege;

  private Beitragsmodel beitragsmodel;

  public AbstractAbrechnungControl() throws RemoteException
  {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -36);
    sepagueltigkeit = cal.getTime();

    individuelleBetraege = (Boolean) Einstellungen
        .getEinstellung(Property.INDIVIDUELLEBEITRAEGE);
    beitragsmodel = Beitragsmodel.getByKey(
        (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL));
  }

  public LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  public Button getPruefenButton()
  {
    Button b = new Button("Auf Probleme prüfen", context -> {
      try
      {
        status.setValue("");
        String error = checkInput();
        if (error != null)
        {
          bugsList.removeAll();
          bugsList.addItem(new Bug(null, error, Bug.ERROR));
          return;
        }
        refreshBugsList();
      }
      catch (RemoteException e)
      {
        status.setValue("Interner Fehler beim Update der Fehlerliste");
        status.setColor(Color.ERROR);
        Logger.error("Fehler", e);
      }
    }, null, false, "bug.png");
    return b;
  }

  public Button getStartButton(AbstractDialog<Boolean> dialog)
  {
    Button button = new Button("Starten", new Action()
    {
      @SuppressWarnings("unchecked")
      @Override
      public void handleAction(Object context)
      {
        try
        {
          status.setValue("");
          String error = checkInput();
          if (error != null)
          {
            bugsList.removeAll();
            bugsList.addItem(new Bug(null, error, Bug.ERROR));
            status.setValue("Es Existieren Warnungen/Fehler, bitte beheben!");
            status.setColor(Color.ERROR);
            return;
          }
          refreshBugsList();

          // Prüfen ob Error oder Warning vorliegen
          for (Bug bug : (List<Bug>) bugsList.getItems())
          {
            if (bug.getKlassifikation() != Bug.HINT)
            {
              status.setValue("Es Existieren Warnungen/Fehler, bitte beheben!");
              status.setColor(Color.ERROR);
              return;
            }
          }
          handleStart();
          dialog.close();
        }
        catch (ApplicationException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (RemoteException e)
        {
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  // Checks müssen in den abgeleiteten Klassen implementiert werden
  protected String checkInput()
  {
    return null;
  }

  // Die Ausgabe muss in den abgeleiteten Klassen implementiert werden
  protected void handleStart() throws RemoteException, ApplicationException
  {
    //
  }

  public Button getAbbrechenButton(AbstractDialog<Boolean> dialog)
      throws RemoteException
  {
    Button b = new Button("Abbrechen", context -> {
      dialog.close();
    }, null, false, "process-stop.png");
    return b;
  }

  public Part getBugsList()
  {
    if (bugsList != null)
    {
      return bugsList;
    }
    bugsList = new JVereinTablePart(getBugs(), context -> {
      Bug bug = (Bug) context;
      Object object = bug.getObject();
      if (object instanceof Mitglied)
      {
        Mitglied m = (Mitglied) object;
        try
        {
          if (m.getMitgliedstyp() == null
              || m.getMitgliedstyp().getID().equals(Mitgliedstyp.MITGLIED))
          {
            GUI.startView(new MitgliedDetailView(), m);
          }
          else
          {
            GUI.startView(new NichtMitgliedDetailView(), m);
          }
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(
              "Fehler beim Anzeigen eines Mitgliedes", e);
        }
      }
    });
    bugsList.addColumn("Name", "name");
    bugsList.addColumn("Meldung", "meldung");
    bugsList.addColumn("Klassifikation", "klassifikationText");
    bugsList.setRememberColWidths(true);
    bugsList.setRememberOrder(true);
    return bugsList;
  }

  public void refreshBugsList() throws RemoteException
  {
    bugsList.removeAll();
    for (Bug bug : getBugs())
    {
      bugsList.addItem(bug);
    }
    bugsList.sort();
  }

  // die Liste muss in den abgeleiteten Klassen implementiert werden
  public List<Bug> getBugs()
  {
    return new ArrayList<Bug>();
  }

  /**
   * Prüfung der Vereinsdaten und des Verrechnungskontos.
   * 
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkGlobal(ArrayList<Bug> bugs) throws RemoteException
  {
    if (Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID) == null)
    {
      bugs.add(new Bug(null,
          "Verrechnungskonto nicht gesetzt. Unter Administration->Einstellungen->Abrechnung erfassen.",
          Bug.ERROR));
    }
    else
    {
      try
      {
        Konto k = Einstellungen.getDBService().createObject(Konto.class,
            Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID)
                .toString());
        if (k == null)
        {
          bugs.add(new Bug(null,
              "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.",
              Bug.ERROR));
        }
      }
      catch (ObjectNotFoundException ex)
      {
        bugs.add(new Bug(null,
            "Verrechnungskonto nicht gefunden. Unter Administration->Einstellungen->Abrechnung erfassen.",
            Bug.ERROR));
      }
    }

    if (Einstellungen.getEinstellung(Property.NAME) == null
        || ((String) Einstellungen.getEinstellung(Property.NAME)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Name des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.ERROR));
    }

    if (Einstellungen.getEinstellung(Property.IBAN) == null
        || ((String) Einstellungen.getEinstellung(Property.IBAN)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Die IBAN des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.ERROR));
    }
    else
    {
      try
      {
        new IBAN((String) Einstellungen.getEinstellung(Property.IBAN));
      }
      catch (SEPAException e)
      {
        bugs.add(new Bug(null,
            "Ungültige IBAN des Vereins. Unter "
                + "Administration->Einstellungen->Allgemein erfassen.",
            Bug.ERROR));
      }
    }

    if (Einstellungen.getEinstellung(Property.BIC) == null
        || ((String) Einstellungen.getEinstellung(Property.BIC)).isEmpty())
    {
      bugs.add(new Bug(null,
          "Die BIC des Vereins fehlt. Unter "
              + "Administration->Einstellungen->Allgemein erfassen.",
          Bug.HINT));
    }
    else
    {
      try
      {
        new BIC((String) Einstellungen.getEinstellung(Property.BIC));
      }
      catch (SEPAException e)
      {
        bugs.add(new Bug(null,
            "Ungültige BIC des Vereins. Unter "
                + "Administration->Einstellungen->Allgemein erfassen.",
            Bug.ERROR));
      }
    }

    if (Einstellungen.getEinstellung(Property.GLAEUBIGERID) == null
        || ((String) Einstellungen.getEinstellung(Property.GLAEUBIGERID))
            .length() == 0)
    {
      bugs.add(new Bug(null,
          "Gläubiger-ID fehlt. Gfls. unter https://extranet.bundesbank.de/scp/ beantragen\n"
              + " und unter Administration->Einstellungen->Allgemein eintragen.\n"
              + "Zu Testzwecken kann DE98ZZZ09999999999 eingesetzt werden.",
          Bug.ERROR));
    }
  }

  /**
   * Prüft den Fälligkeitstermin.
   * 
   * @param faelligkeit
   *          Fälligkeit der Forderungen
   * @param bugs
   *          Die Bugliste
   */
  public void checkFaelligkeit(Date faelligkeit, ArrayList<Bug> bugs)
  {
    if (faelligkeit.before(new Date()))
    {
      bugs.add(new Bug(null,
          "Fälligkeit muss bei Lastschriften in der Zukunft liegen!",
          Bug.ERROR));
    }
  }

  /**
   * Prüft ob die Beiträge konfiguriert sind. Falls Beiträge abgebucht werden
   * sollen wird optional das SEPA Mandat überprüft.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param sepacheck
   *          Bestimmt ob der SEPA Mandat überprüft werden soll
   * @param bugs
   *          Die Bugliste
   * @return Sagt, ob eine Lastschrift durchgeführt wird
   * @throws RemoteException
   */
  public boolean checkMitgliedBeitraege(Mitglied m, boolean sepacheck,
      ArrayList<Bug> bugs) throws RemoteException
  {
    boolean zahlunswegLastschrift = m.getZahler()
        .getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT;
    boolean isLastschrift = false;
    if (individuelleBetraege && m.getIndividuellerBeitrag() != null)
    {
      if (zahlunswegLastschrift && m.getIndividuellerBeitrag() > 0)
      {
        if (sepacheck)
        {
          checkSEPA(m.getZahler(), bugs);
        }
        isLastschrift = true;
      }
    }
    else
    {
      switch (beitragsmodel)
      {
        case GLEICHERTERMINFUERALLE:
        case MONATLICH12631:
          Double betrag = m.getBeitragsgruppe().getBetrag();
          if (betrag == null)
          {
            bugs.add(new Bug(m, "Betrag in Beitragsgruppe ist nicht gesetzt!",
                Bug.ERROR));
          }
          else if (zahlunswegLastschrift && betrag > 0)
          {
            if (sepacheck)
            {
              checkSEPA(m.getZahler(), bugs);
            }
            isLastschrift = true;
          }
          break;
        case FLEXIBEL:
          if (m.getBeitragsgruppe().getBetragMonatlich() == null
              || m.getBeitragsgruppe().getBetragVierteljaehrlich() == null
              || m.getBeitragsgruppe().getBetragHalbjaehrlich() == null
              || m.getBeitragsgruppe().getBetragJaehrlich() == null)
          {
            bugs.add(new Bug(m, "Beträge in Beitragsgruppe sind nicht gesetzt!",
                Bug.ERROR));
          }
          else if (zahlunswegLastschrift
              && (m.getBeitragsgruppe().getBetragMonatlich() > 0
                  || m.getBeitragsgruppe().getBetragVierteljaehrlich() > 0
                  || m.getBeitragsgruppe().getBetragHalbjaehrlich() > 0
                  || m.getBeitragsgruppe().getBetragJaehrlich() > 0))
          {
            if (sepacheck)
            {
              checkSEPA(m.getZahler(), bugs);
            }
            isLastschrift = true;
          }
          break;
      }
    }
    return isLastschrift;
  }

  /**
   * Prüft für eine Lastschrift auf gültige Kontodaten des Mitglieds.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkMitgliedKontodaten(Mitglied m, ArrayList<Bug> bugs)
      throws RemoteException
  {
    if (m.getMandatDatum().equals(Einstellungen.NODATE))
    {
      bugs.add(new Bug(m, "Für die Basislastschrift fehlt das Mandatsdatum!",
          Bug.ERROR));
    }
    else if (m.getMandatDatum().after(new Date()))
    {
      bugs.add(new Bug(m, "Das Mandatsdatum liegt in der Zukunft!", Bug.ERROR));
    }

    if (m.getIban() == null || m.getIban().isEmpty())
    {
      bugs.add(
          new Bug(m, "Für die Basislastschrift fehlt die IBAN!", Bug.ERROR));
    }
    else
    {
      try
      {
        new IBAN(m.getIban());
      }
      catch (SEPAException e)
      {
        bugs.add(new Bug(m, "Ungültige IBAN " + m.getIban(), Bug.ERROR));
      }
    }

    if (m.getBic() == null || m.getBic().isEmpty())
    {
      bugs.add(new Bug(m, "Für die Basislastschrift fehlt die BIC!", Bug.HINT));
    }
    else
    {
      try
      {
        new BIC(m.getBic());
      }
      catch (Exception e)
      {
        bugs.add(new Bug(m, "Ungültige BIC " + m.getBic(), Bug.ERROR));
      }
    }
  }

  /**
   * Prüft das SEPA Mandat des Mitglieds auf Gültigkeit.
   * 
   * @param m
   *          Das Mitglied dessen Daten geprüft werden
   * @param bugs
   *          Die Bugliste
   * @throws RemoteException
   */
  public void checkSEPA(Mitglied m, ArrayList<Bug> bugs) throws RemoteException
  {
    if (!m.getMandatDatum().equals(Einstellungen.NODATE))
    {
      if (m.getLetzteLastschrift() == null
          && m.getMandatDatum().before(sepagueltigkeit))
      {
        bugs.add(new Bug(m,
            "Das Mandat ist älter als 36 Monate und es existiert noch keine Lastschrift in JVerein.\n"
                + "Neues Mandat anfordern und eingeben oder den SEPA-Check temporär deaktivieren.",
            Bug.ERROR));
      }

      if (m.getLetzteLastschrift() != null
          && m.getLetzteLastschrift().before(sepagueltigkeit)
          && m.getMandatDatum().before(sepagueltigkeit))
      {
        bugs.add(new Bug(m,
            "Letzte Lastschrift und das Mandat sind älter als 36 Monate.\nNeues Mandat anfordern und eingeben.",
            Bug.ERROR));
      }
    }
  }
}
