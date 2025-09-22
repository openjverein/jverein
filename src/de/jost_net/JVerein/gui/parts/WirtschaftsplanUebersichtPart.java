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

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanUebersichtPart implements Part
{
  private final WirtschaftsplanControl control;

  private TextInput bezeichnung;

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
    boolean ruecklagen = ((Boolean) Einstellungen
        .getEinstellung(Einstellungen.Property.RUECKLAGENKONTEN));
    boolean verbindlichkeiten = ((Boolean) Einstellungen
        .getEinstellung(Einstellungen.Property.VERBINDLICHKEITEN_FORDERUNGEN));

    LabelGroup uebersicht = new LabelGroup(parent, "Übersicht");

    ColumnLayout baseData = new ColumnLayout(uebersicht.getComposite(), 3);

    SimpleContainer bezeichnungContainer = new SimpleContainer(
        baseData.getComposite());
    bezeichnung = new TextInput(control.getWirtschaftsplan().getBezeichung());
    bezeichnungContainer.addLabelPair("Bezeichnung", bezeichnung);

    SimpleContainer vonContainer = new SimpleContainer(baseData.getComposite());
    von = new DateInput(control.getWirtschaftsplan().getDatumVon(),
        new JVDateFormatTTMMJJJJ());
    vonContainer.addLabelPair("Von", von);

    SimpleContainer bisContainer = new SimpleContainer(baseData.getComposite());
    bis = new DateInput(control.getWirtschaftsplan().getDatumBis(),
        new JVDateFormatTTMMJJJJ());
    bisContainer.addLabelPair("Bis", bis);

    ColumnLayout finanzData = new ColumnLayout(uebersicht.getComposite(),
        verbindlichkeiten ? 4 : 2); // Falls keine Verbindlichkeiten, entfallen
                                    // die Spalten

    SimpleContainer einnahmen = new SimpleContainer(finanzData.getComposite());
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
    if (ruecklagen)
    {
      DecimalInput istRuecklagenGebildet = new DecimalInput(
          (Double) control.getWirtschaftsplan()
              .getAttribute("istRücklagenGebildet"),
          Einstellungen.DECIMALFORMAT);
      istRuecklagenGebildet.disable();
      einnahmen.addLabelPair("Rücklagen gebildet Ist", istRuecklagenGebildet);
    }

    if (verbindlichkeiten)
    {
      SimpleContainer forderungen = new SimpleContainer(
          finanzData.getComposite());
      DecimalInput istForderungen = new DecimalInput(
          (Double) control.getWirtschaftsplan().getAttribute("istForderungen"),
          Einstellungen.DECIMALFORMAT);
      istForderungen.disable();
      forderungen.addLabelPair("Forderungen Ist", istForderungen);
      DecimalInput istPositiv = new DecimalInput(
          (Double) control.getWirtschaftsplan().getAttribute("istPlus"),
          Einstellungen.DECIMALFORMAT);
      istPositiv.disable();
      forderungen.addLabelPair("Einnahmen inkl. Forderungen Ist", istPositiv);
    }

    SimpleContainer ausgaben = new SimpleContainer(finanzData.getComposite());
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
    if (ruecklagen)
    {
      DecimalInput istRuecklagenAufgeloest = new DecimalInput(
          (Double) control.getWirtschaftsplan()
              .getAttribute("istRücklagenAufgelöst"),
          Einstellungen.DECIMALFORMAT);
      istRuecklagenAufgeloest.disable();
      ausgaben.addLabelPair("Rücklagen aufgelöst Ist", istRuecklagenAufgeloest);
    }

    if (verbindlichkeiten)
    {
      SimpleContainer verbindlichkeitenContainer = new SimpleContainer(
          finanzData.getComposite());
      DecimalInput istVerbindlichkeiten = new DecimalInput(
          (Double) control.getWirtschaftsplan()
              .getAttribute("istVerbindlichkeiten"),
          Einstellungen.DECIMALFORMAT);
      istVerbindlichkeiten.disable();
      verbindlichkeitenContainer.addLabelPair("Verbindlichkeiten Ist",
          istVerbindlichkeiten);
      DecimalInput istNegativ = new DecimalInput(
          (Double) control.getWirtschaftsplan().getAttribute("istMinus"),
          Einstellungen.DECIMALFORMAT);
      istNegativ.disable();
      verbindlichkeitenContainer
          .addLabelPair("Ausgaben inkl. Verbindlichkeiten Ist", istNegativ);
    }
  }

  /**
   * Aktualisiert die Soll-Werte der Einnahmen und Ausgaben. Diese Methode wird
   * aufgerufen, wenn sich die Einträge in der Übersicht ändern, z.B. durch das
   * Hinzufügen oder Entfernen von Einträgen.
   * 
   * @throws ApplicationException
   *           wenn ein Fehler auftritt.
   */
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
      einnahmen = (List<WirtschaftsplanNode>) control.getEinnahmen().getItems();
      ausgaben = (List<WirtschaftsplanNode>) control.getAusgaben().getItems();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler beim Aktualisieren der Übersicht!");
    }

    double sollEinnahmen = einnahmen.stream()
        .mapToDouble(WirtschaftsplanNode::getSoll).sum();

    double sollAusgaben = ausgaben.stream()
        .mapToDouble(WirtschaftsplanNode::getSoll).sum();

    this.sollEinnahme.setValue(sollEinnahmen);
    this.sollAusgaben.setValue(sollAusgaben);
  }

  public TextInput getBezeichnung()
  {
    return bezeichnung;
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
