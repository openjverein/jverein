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

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.server.BuchungImpl;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungenSollbuchungZuordnungDialog extends AbstractDialog<Object>
{
  private static final String SETTINGS_PREFIX = "BUCHUNGSZUORDNUNG.";

  private static final String SETTINGS_IBAN = SETTINGS_PREFIX + "IBAN";

  private static final String SETTINGS_MITGLIEDSNUMMER = SETTINGS_PREFIX
      + "MITGLIEDSNUMMER";

  private static final String SETTINGS_VORNAME_NAME = SETTINGS_PREFIX
      + "VORNAME_NAME";

  private static final String SETTINGS_ZWECK = SETTINGS_PREFIX + "ZWECK";

  private static final int WINDOW_WIDTH = 620;

  private DateInput dateFrom = null;

  private DateInput dateUntil = null;

  private CheckboxInput useIban = null;

  private CheckboxInput useMitgliedId = null;

  private CheckboxInput useName = null;

  private CheckboxInput useZweck;

  private Settings settings = null;

  /**
   * ct.
   * 
   * @param bisdatum
   * @param vondatum
   * 
   * @throws RemoteException
   */
  public BuchungenSollbuchungZuordnungDialog(Date vondatum, Date bisdatum)
  {
    super(POSITION_CENTER);

    setTitle("Buchungen zu Sollbuchung zuordnen");
    setSize(WINDOW_WIDTH, SWT.DEFAULT);

    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);

    // Inputs
    dateFrom = createDateInput(vondatum, true);
    dateUntil = createDateInput(bisdatum, false);
    useIban = new CheckboxInput(settings.getBoolean(SETTINGS_IBAN, true));
    useMitgliedId = new CheckboxInput(
        settings.getBoolean(SETTINGS_MITGLIEDSNUMMER, false));
    useName = new CheckboxInput(
        settings.getBoolean(SETTINGS_VORNAME_NAME, false));
    useZweck = new CheckboxInput(settings.getBoolean(SETTINGS_ZWECK, false));
  }

  private DateInput createDateInput(Date date, boolean isStart)
  {
    DateInput returnValue = new DateInput(date, new JVDateFormatTTMMJJJJ());
    String typeOfInput = isStart ? "Be­ginn" : "Ende";
    returnValue.setTitle(typeOfInput + " des Suchbereichs");
    returnValue.setText("Bitte " + typeOfInput + " des Suchbereichs wählen");
    returnValue.setComment("*)");
    return returnValue;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText(
        "Bitte wählen Sie den Suchzeitraum und die gewünschte Zuordnungsart aus",
        true);

    group.addLabelPair("Startdatum", dateFrom);
    group.addLabelPair("Enddatum", dateUntil);
    group.addLabelPair("Nach eindeutiger IBAN", useIban);
    group.addLabelPair("Nach " + ((Boolean) Einstellungen
        .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER)
            ? "Ext. Mitgliedsnummer"
            : "Mitgliedsnummer"),
        useMitgliedId);
    group.addLabelPair("Nach eindeutigen Vorname und Nachname", useName);
    group.addLabelPair("Nach eindeutigem Verwendungszweck", useZweck);
    ButtonArea buttons = new ButtonArea();

    Button button = new Button("Zuordnungen suchen",
        context -> buchungenZuordnen(), null, true, "user-friends.png");
    buttons.addButton(button);
    buttons.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    group.addButtonArea(buttons);
    getShell()
        .setMinimumSize(getShell().computeSize(WINDOW_WIDTH, SWT.DEFAULT));
  }

  private enum ZuordnungsArt
  {
    IBAN("IBAN"),
    NAME("Name"),
    NAME_KONTOINHABER("Name Kontoinhaber"),
    NAME_ZWECK("Name im Verwendungszweck"),
    NAME_ZAHLER("Name des Zahlers"),
    NAME_KONTOINHABER_ZAHLER("Name Kontoinhaber des Zahlers"),
    ZWECK("Verwendungszweck"),
    ID("Id");

    private String art;

    ZuordnungsArt(String art)
    {
      this.art = art;
    }

    public String getText()
    {
      return art;
    }
  }

  /**
   * Ermittelt eine Liste von Buchungen mit Mitgliedskonten und gibt diese an
   * den BuchungenMitgliedskontenZuordnungVorschauDialog weiter
   * 
   */
  private void buchungenZuordnen()
  {
    final Date von = (Date) dateFrom.getValue();
    final Date bis = (Date) dateUntil.getValue();

    HashSet<ZuordnungsArt> arten = new HashSet<>();
    if ((boolean) this.useIban.getValue())
    {
      arten.add(ZuordnungsArt.IBAN);
    }
    if ((boolean) this.useMitgliedId.getValue())
    {
      arten.add(ZuordnungsArt.ID);
    }
    if ((boolean) this.useName.getValue())
    {
      arten.add(ZuordnungsArt.NAME);
      arten.add(ZuordnungsArt.NAME_KONTOINHABER);
      arten.add(ZuordnungsArt.NAME_ZWECK);
      arten.add(ZuordnungsArt.NAME_ZAHLER);
      arten.add(ZuordnungsArt.NAME_KONTOINHABER_ZAHLER);
    }
    if ((boolean) this.useZweck.getValue())
    {
      arten.add(ZuordnungsArt.ZWECK);
    }

    settings.setAttribute(SETTINGS_IBAN, (Boolean) useIban.getValue());
    settings.setAttribute(SETTINGS_MITGLIEDSNUMMER,
        (Boolean) useMitgliedId.getValue());
    settings.setAttribute(SETTINGS_VORNAME_NAME, (Boolean) useName.getValue());
    settings.setAttribute(SETTINGS_ZWECK, (Boolean) useZweck.getValue());

    BackgroundTask t = new BackgroundTask()
    {

      private boolean interrupted = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          boolean externeMitgliedsnummer = (Boolean) Einstellungen
              .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER);

          if (arten.size() == 0)
          {
            GUI.getStatusBar()
                .setErrorText("Es wurde keine Zuordnungsart angegeben.");
            return;
          }

          if (von == null || bis == null)
          {
            GUI.getStatusBar()
                .setErrorText("Bitte geben Sie ein Start- und Enddatum ein.");
            return;
          }

          if (bis.before(von))
          {
            GUI.getStatusBar()
                .setErrorText("Das Enddatum liegt vor dem Startdatum.");
            return;
          }

          // Map mit allen bereits zugeordneten Sollbuchungen. Wird erst
          // gefüllt, wenn die Zuordnung nach einer Art abgeschlossen ist.
          // <ZuordnungArt<BuchungId, SollbuchungId>>
          HashMap<String, HashMap<Integer, Integer>> zuordnungMap = new HashMap<>();

          for (ZuordnungsArt art : ZuordnungsArt.values())
          {
            if (!arten.contains(art))
            {
              continue;
            }
            monitor.setStatusText("Suche Zuordnungen nach " + art.getText());
            monitor.setPercentComplete(0);

            // Map mit den Zugeordneten Buchungen dieser Art. Wird am Ende in
            // zuordnungMap übernommen
            // <BuchungId, SollbuchungId>
            HashMap<Integer, Integer> artMap = new HashMap<>();

            ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
                "sollbuchung");
            it.addFilter("sollbuchung.datum BETWEEN ? and ?", von, bis);

            it.leftJoin("buchung", "buchung.sollbuchung = sollbuchung.id");

            it.addColumn(
                "sum(sollbuchung.betrag -COALESCE(buchung.betrag,0)) as fehlbetrag");
            it.addHaving("fehlbetrag != 0");
            it.addColumn("sollbuchung.zweck1 as sollbuchung_zweck");

            it.leftJoin("mitglied", "mitglied.id = sollbuchung.mitglied");
            it.addColumn("mitglied.id as mitglied_id");
            it.addColumn("mitglied.name as mitglied_name");
            it.addColumn("mitglied.vorname as mitglied_vorname");
            it.addColumn("mitglied.iban as mitglied_iban");
            it.addColumn("mitglied.kontoinhaber as mitglied_kontoinhaber");

            it.leftJoin("mitglied as zahler", "mitglied.altzahler = zahler.id");
            it.addColumn("zahler.name as zahler_name");
            it.addColumn("zahler.vorname as zahler_vorname");
            it.addColumn("zahler.kontoinhaber as zahler_kontoinhaber");

            if (externeMitgliedsnummer)
            {
              it.addColumn(
                  "mitglied.externemitgliedsnummer as mitglied_externe_id");
            }

            it.addColumn("sollbuchung.id as sollbuchung_id");

            it.addGroupBy("sollbuchung.id");

            // Alle nach anderer Art zugeordnete Sollbuchungen überspringen
            String zugeordneteSollbuchungIds = zuordnungMap.values().stream()
                .filter(m -> m.size() > 0)
                .map(e -> e.values().stream().filter(t -> t != null)
                    .map(s -> s.toString()).collect(Collectors.joining(",")))
                .collect(Collectors.joining(","));

            if (!zugeordneteSollbuchungIds.isBlank())
            {
              it.addFilter(
                  "sollbuchung.id NOT IN (" + zugeordneteSollbuchungIds + ")");
            }

            // Sortierung je nach Art. Muss auf jeden Fall nach Mitglied-Id
            // sortiert sein, sonst funktioniert die Zuordnung nicht richtig
            switch (art)
            {
              case NAME:
              case NAME_ZWECK:
                // Kurze Namen am Ende, sonst passen sie ggf. auf Teile von
                // langen Namen
                it.setOrder(
                    "Order by length(mitglied.name)+length(mitglied.vorname) DESC,"
                        + "mitglied.id, sollbuchung.datum");
                break;
              case NAME_ZAHLER:
                it.setOrder(
                    "Order by length(zahler.name)+length(zahler.vorname) DESC,"
                        + "mitglied.id, sollbuchung.datum");
                break;
              case ID:
                if (externeMitgliedsnummer)
                {
                  it.setOrder(
                      "Order by length(mitglied.externemitgliedsnummer) DESC,"
                          + "mitglied.id, sollbuchung.datum");
                }
                else
                {
                  it.setOrder(
                      "Order by length(mitglied.id) DESC,mitglied.id, sollbuchung.datum");
                }
                break;
              default:
                it.setOrder("Order by mitglied.id, sollbuchung.datum");
            }

            // Alle nach anderer Art zugeordnete Buchungen
            String zugeordneteBuchungIds = zuordnungMap.values().stream()
                .filter(m -> m.size() > 0)
                .map(e -> e.entrySet().stream()
                    .filter(t -> t.getValue() != null).map(m -> m.toString())
                    .collect(Collectors.joining(",")))
                .collect(Collectors.joining(","));

            // Diese Map enthält alle BuchungsIds die dem aktuellen Mitglied
            // zugeordnet wurden.
            HashSet<Integer> buchungenMitglied = new HashSet<>();
            int oldMitgliedId = 0;
            int count = 0;
            while (it.hasNext())
            {
              if (isInterrupted())
              {
                throw new OperationCanceledException();
              }
              monitor.setPercentComplete(count++ * 100 / it.size());
              PseudoDBObject o = it.next();

              if (o.getInteger("mitglied_id") != oldMitgliedId)
              {
                buchungenMitglied = new HashSet<>();
                oldMitgliedId = o.getInteger("mitglied_id");
              }

              DBIterator<BuchungImpl> buchungIt = Einstellungen.getDBService()
                  .createList(BuchungImpl.class);
              buchungIt.addFilter("datum BETWEEN ? and ?", von, bis);
              buchungIt.addFilter("(splittyp != ? or splittyp is null)",
                  SplitbuchungTyp.HAUPT);
              buchungIt.addFilter("(splittyp != ? or splittyp is null)",
                  SplitbuchungTyp.GEGEN);
              buchungIt.addFilter("sollbuchung is null");

              buchungIt.addFilter("CAST(betrag AS DECIMAL(10,2)) = ?",
                  Math.round(o.getDouble("fehlbetrag") * 100) / 100);

              // Buchungen, die diesem Mitglied schon zugeordnet wurden,
              // rausfiltern. (Es dürfen mehrere Sollbuchungen zu einer Buchung
              // passen, solange es das gleiche Mitglied ist)
              if (buchungenMitglied.size() > 0)
              {
                buchungIt.addFilter("id not IN (" + buchungenMitglied.stream()
                    .map(String::valueOf).collect(Collectors.joining(","))
                    + ")");
              }

              // Alle nach anderer Art zugeordnete Buchungen rausfiltern.
              if (!zugeordneteBuchungIds.isBlank())
              {
                it.addFilter(
                    "buchung.id NOT IN (" + zugeordneteBuchungIds + ")");
              }

              buchungIt.setOrder("Order by datum");
              buchungIt.setLimit(1);

              switch (art)
              {
                case IBAN:
                  buchungIt.addFilter("iban is not null");
                  buchungIt.addFilter("iban != ''");
                  buchungIt.addFilter("iban = ?",
                      o.getAttribute("mitglied_iban"));
                  break;
                case NAME:
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("mitglied_name"));
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("mitglied_vorname"));
                  break;
                case NAME_ZWECK:
                  buchungIt.addFilter("buchung.zweck LIKE CONCAT('%',?,'%')",
                      o.getAttribute("mitglied_name"));
                  buchungIt.addFilter("buchung.zweck LIKE CONCAT('%',?,'%')",
                      o.getAttribute("mitglied_vorname"));
                  break;
                case NAME_KONTOINHABER:
                  buchungIt.addFilter("? is not null",
                      o.getAttribute("mitglied_kontoinhaber"));
                  buchungIt.addFilter("? != ''",
                      o.getAttribute("mitglied_kontoinhaber"));
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("mitglied_kontoinhaber"));
                  break;
                case NAME_ZAHLER:
                  buchungIt.addFilter("? is not null",
                      o.getAttribute("zahler_name"));
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("zahler_name"));
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("zahler_vorname"));
                  break;
                case NAME_KONTOINHABER_ZAHLER:
                  buchungIt.addFilter("? is not null",
                      o.getAttribute("zahler_kontoinhaber"));
                  buchungIt.addFilter("? != ''",
                      o.getAttribute("zahler_kontoinhaber"));
                  buchungIt.addFilter("buchung.name LIKE CONCAT('%',?,'%')",
                      o.getAttribute("zahler_kontoinhaber"));
                  break;
                case ZWECK:
                  buchungIt.addFilter("buchung.zweck LIKE CONCAT('%',?,'%')",
                      o.getAttribute("sollbuchung_zweck"));
                  break;
                case ID:
                  buchungIt.addFilter("buchung.zweck LIKE ?",
                      o.getAttribute(
                          externeMitgliedsnummer ? "mitglied_externe_id"
                              : "mitglied_id"));
                  break;
              }

              if (buchungIt.hasNext())
              {
                BuchungImpl buchung = buchungIt.next();
                Integer buchungId = Integer.parseInt(buchung.getID());
                if (!artMap.containsKey(buchungId))
                {
                  artMap.put(buchungId, o.getInteger("sollbuchung_id"));
                  buchungenMitglied.add(buchungId);
                }
                else
                {
                  // Für diese Buchung passte schon eine andere Sollbuchung
                  // eines anderen Mitglieds, daher keine zuordnen. Es dürfen
                  // auf
                  // eine Buchung nur Sollbuchungen des gleichen Mitglieds
                  // passen, sonst ist es nicht eindeutig genug.
                  artMap.put(buchungId, null);
                }
              }
            }
            zuordnungMap.put(art.getText(), artMap);

            long anz = artMap.entrySet().stream()
                .filter(e -> e.getValue() != null).count();
            if (anz > 0)
            {
              monitor.setStatusText("           " + anz + " Zuordnungen nach "
                  + art.getText() + " gefunden.");
            }
          }
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);

          new BuchungenSollbuchungZuordnungVorschauDialog(zuordnungMap).open();
        }
        catch (Exception e)
        {
          DBTransaction.rollback();
          Logger.error("error while opening a dialog", e);
          throw new ApplicationException("Fehler beim Zuordnen", e);
        }
      }

      @Override
      public void interrupt()
      {
        interrupted = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return interrupted;
      }
    };

    Application.getController().start(t);

    close();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
}
