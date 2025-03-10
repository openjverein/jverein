/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanMenu;
import de.jost_net.JVerein.gui.parts.WirtschaftsplanUebersichtPart;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class WirtschaftsplanView extends AbstractView
{
  private final Calendar calendar = Calendar.getInstance();

  private enum RANGE
  {
    MONAT, TAG
  }

  @Override
  public void bind() throws Exception
  {
    if (!(this.getCurrentObject() instanceof Wirtschaftsplan))
    {
      throw new ApplicationException(
          "Fehler beim Anzeigen des Wirtschaftsplans!");
    }

    GUI.getView().setTitle("Wirtschaftsplanung");

    final WirtschaftsplanControl control = new WirtschaftsplanControl(
        this);

    WirtschaftsplanUebersichtPart uebersicht = new WirtschaftsplanUebersichtPart(
        control);
    uebersicht.paint(this.getParent());
    control.setUebersicht(uebersicht);

    SimpleContainer group = new SimpleContainer(this.getParent(), true, 2);

    LabelGroup einnahmen = new LabelGroup(group.getComposite(), "Einnahmen", true);
    TreePart treeEinnahmen = control.getEinnahmen();
    treeEinnahmen.setContextMenu(new WirtschaftsplanMenu(WirtschaftsplanImpl.EINNAHME, control));
    einnahmen.addPart(treeEinnahmen);
    LabelGroup ausgaben = new LabelGroup(group.getComposite(), "Ausgaben", true);
    TreePart treeAusgaben = control.getAusgaben();
    treeAusgaben.setContextMenu(new WirtschaftsplanMenu(WirtschaftsplanImpl.AUSGABE, control));
    ausgaben.addPart(treeAusgaben);

    ButtonArea buttons = new ButtonArea();

    Button zurueck = new Button("", context -> loadNextPlan(false, control), null, false, "go-previous.png");
    buttons.addButton(zurueck);

    Button vor = new Button("", context -> loadNextPlan(true, control), null, false, "go-next.png");
    buttons.addButton(vor);

    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.WIRTSCHAFTSPLANUNG, false, "question-circle.png");
    buttons.addButton("CSV", context -> control.starteAuswertung(
        WirtschaftsplanControl.AUSWERTUNG_CSV), null, false, "xsd.png");
    buttons.addButton("PDF", context -> control.starteAuswertung(
        WirtschaftsplanControl.AUSWERTUNG_PDF), null, false, "file-pdf.png");
    buttons.addButton("Speichern", context -> control.handleStore(), null,
        false, "document-save.png");
    buttons.paint(this.getParent());
  }

  /**
   * Lädt den nächsten Wirtschaftsplan, wenn man einen der Pfeil-Buttons drückt
   * @param next true, falls der nächste Plan geladen wird und false, falls der vorherige Plan geladen wird.
   */
  private void loadNextPlan(boolean next, WirtschaftsplanControl control) throws ApplicationException
  {
    Date vonDate = (Date) control.getUebersicht().getVon().getValue();
    Date bisDate = (Date) control.getUebersicht().getBis().getValue();

    try
    {
      DBIterator<Wirtschaftsplan> iterator = Einstellungen.getDBService().createList(Wirtschaftsplan.class);

      if (next)
      {
        calendar.setTime(bisDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        iterator.addFilter("datum_von = ?", calendar.getTime());
      }
      else
      {
        calendar.setTime(vonDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        iterator.addFilter("datum_bis = ?", calendar.getTime());
      }

      if (iterator.hasNext())
      {
        Wirtschaftsplan plan = iterator.next();
        GUI.startView(WirtschaftsplanView.class, plan);
        if (iterator.hasNext())
        {
          GUI.getStatusBar().setSuccessText(
                  "Mehr als einen Plan für diesen Zeitraum gefunden. Es wird ein zufälliger Plan für diesen Zeitraum geladen!");
        }
      }
      else
      {
        if (getRangeTyp(vonDate, bisDate, control) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(vonDate.toInstant(), bisDate.toInstant());
          delta++;
          delta = next ? delta : -delta;
          calendar.setTime(vonDate);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          vonDate = calendar.getTime();
          calendar.setTime(bisDate);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
        }
        else
        {
          LocalDate lvon = vonDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
          LocalDate lbis = bisDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
          int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
          delta++;
          calendar.setTime(vonDate);
          calendar.add(Calendar.MONTH, next ? delta : -delta);
          vonDate = calendar.getTime();
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        bisDate = calendar.getTime();

        Wirtschaftsplan plan = Einstellungen.getDBService().createObject(Wirtschaftsplan.class, null);
        plan.setDatumVon(vonDate);
        plan.setDatumBis(bisDate);
        GUI.startView(WirtschaftsplanView.class, plan);
        GUI.getStatusBar().setErrorText(
                "Kein Plan für den Zeitraum gefunden. Neuer Plan wurde erstellt!");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Laden des Wirtschaftsplans", e);
    }
  }

  private RANGE getRangeTyp(Date von, Date bis, WirtschaftsplanControl control) throws ApplicationException
  {
    control.checkDate();
    calendar.setTime(von);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    calendar.setTime(bis);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    return RANGE.MONAT;
  }
}
