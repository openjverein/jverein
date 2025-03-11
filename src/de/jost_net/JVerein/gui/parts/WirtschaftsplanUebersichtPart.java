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
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;
import java.util.List;

public class WirtschaftsplanUebersichtPart implements Part
{
  private final WirtschaftsplanControl control;

  private DateInput bis;

  private DateInput von;

  private DecimalInput sollEinnahme;

  private DecimalInput sollAusgaben;

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
}
