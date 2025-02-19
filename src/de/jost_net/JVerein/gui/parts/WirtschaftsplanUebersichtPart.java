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
package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.view.WirtschaftsplanView;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WirtschaftsplanUebersichtPart implements Part
{
  private final WirtschaftsplanControl control;

  private DateInput bis;

  private DateInput von;

  private DecimalInput sollEinnahme;

  private DecimalInput sollAusgaben;

  private final Calendar calendar = Calendar.getInstance();

  private enum RANGE
  {
    MONAT, TAG
  }

  public WirtschaftsplanUebersichtPart(WirtschaftsplanControl control)
  {
    this.control = control;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup uebersicht = new LabelGroup(parent, "Übersicht");

    ColumnLayout columns = new ColumnLayout(uebersicht.getComposite(), 2);

    SimpleContainer einnahmen = new SimpleContainer(columns.getComposite());

    von = new DateInput(
        control.getWirtschaftsplan().getDatumVon(),
        new JVDateFormatTTMMJJJJ());
    einnahmen.addLabelPair("Von", von);
    sollEinnahme = new DecimalInput(
        (Double) control.getWirtschaftsplan().getAttribute("planEinnahme"),
        Einstellungen.DECIMALFORMAT);
    sollEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Soll", sollEinnahme);
    DecimalInput istEinnahme = new DecimalInput(
        (Double) control.getWirtschaftsplan().getAttribute("istEinnahme"),
        Einstellungen.DECIMALFORMAT);
    istEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Ist", istEinnahme);

    SimpleContainer ausgaben = new SimpleContainer(columns.getComposite());

    bis = new DateInput(
        control.getWirtschaftsplan().getDatumBis(),
        new JVDateFormatTTMMJJJJ());
    ausgaben.addLabelPair("Bis", bis);
    sollAusgaben = new DecimalInput(
        (Double) control.getWirtschaftsplan().getAttribute("planAusgabe"),
        Einstellungen.DECIMALFORMAT);
    sollAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Soll", sollAusgaben);
    DecimalInput istAusgaben = new DecimalInput(
        (Double) control.getWirtschaftsplan().getAttribute("istAusgabe"),
        Einstellungen.DECIMALFORMAT);
    istAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Ist", istAusgaben);

    ButtonArea buttonArea = new ButtonArea();

    Button zurueck = new Button("", context -> loadNextPlan(false), null, false, "go-previous.png");
    buttonArea.addButton(zurueck);

    Button vor = new Button("", context -> loadNextPlan(true), null, false, "go-next.png");
    buttonArea.addButton(vor);

    uebersicht.addButtonArea(buttonArea);
  }

  @SuppressWarnings("unchecked")
  public void updateSoll() throws ApplicationException
  {
    if (sollEinnahme == null || sollAusgaben == null)
    {
      return;
    }

    List<WirtschaftsplanNode> einnahmen;
    List<WirtschaftsplanNode> ausgaben;

    try
    {
      einnahmen = (List<WirtschaftsplanNode>) control.getEinnahmen()
          .getItems();
      ausgaben = (List<WirtschaftsplanNode>) control.getAusgaben()
          .getItems();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler beim aktualisieren der Übersicht!");
    }

    double sollEinnahmen = einnahmen.stream()
        .mapToDouble(WirtschaftsplanNode::getSoll).sum();

    double sollAusgaben = ausgaben.stream()
        .mapToDouble(WirtschaftsplanNode::getSoll).sum();

    this.sollEinnahme.setValue(sollEinnahmen);
    this.sollAusgaben.setValue(sollAusgaben);
  }

  public DateInput getBis()
  {
    return bis;
  }

  public DateInput getVon()
  {
    return von;
  }

  private RANGE getRangeTyp(Date von, Date bis) throws ApplicationException
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

  /**
   * Lädt den nächsten Wirtschaftsplan, wenn man einen der Pfeil-Buttons drückt
   * @param next true, falls der nächste Plan geladen wird und false, falls der vorherige Plan geladen wird.
   */
  private void loadNextPlan(boolean next) throws ApplicationException
  {
    Date vonDate = (Date) von.getValue();
    Date bisDate = (Date) bis.getValue();
    if (getRangeTyp(vonDate, bisDate) == RANGE.TAG)
    {
      int delta = (int) ChronoUnit.DAYS.between(vonDate.toInstant(), bisDate.toInstant());
      delta++;
      delta = next ? delta : -delta;
      calendar.setTime(vonDate);
      calendar.add(Calendar.DAY_OF_MONTH, delta);
      vonDate = calendar.getTime();
      calendar.setTime(bisDate);
      calendar.add(Calendar.DAY_OF_MONTH, delta);
      bisDate = calendar.getTime();
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
      bisDate = calendar.getTime();
    }

    try
    {
      DBIterator<Wirtschaftsplan> iterator = Einstellungen.getDBService().createList(Wirtschaftsplan.class);
      iterator.addFilter("datum_von = ?", vonDate);
      iterator.addFilter("datum_bis = ?", bisDate);

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
}
