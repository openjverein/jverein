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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.WirtschaftsplanDetailControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanUebersichtPart implements Part
{
  private final WirtschaftsplanDetailControl control;

  private TextInput bezeichnung;

  private DateInput bis;

  private DateInput von;

  private SelectInput projekt;

  private BigDecimalInput sollEinnahme;

  private BigDecimalInput sollAusgaben;

  private BigDecimalInput sollRuecklagenGebildet;

  private BigDecimalInput sollRuecklagenAufgeloest;

  public WirtschaftsplanUebersichtPart(WirtschaftsplanDetailControl control)
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

    ColumnLayout baseData = new ColumnLayout(uebersicht.getComposite(), 4);

    SimpleContainer bezeichnungContainer = new SimpleContainer(
        baseData.getComposite());
    bezeichnung = new TextInput(control.getWirtschaftsplan().getBezeichung(),
        200);
    bezeichnungContainer.addLabelPair("Bezeichnung", bezeichnung);

    SimpleContainer vonContainer = new SimpleContainer(baseData.getComposite());
    von = new DateInput(control.getWirtschaftsplan().getDatumVon(),
        new JVDateFormatTTMMJJJJ());
    vonContainer.addLabelPair("Von", von);

    SimpleContainer bisContainer = new SimpleContainer(baseData.getComposite());
    bis = new DateInput(control.getWirtschaftsplan().getDatumBis(),
        new JVDateFormatTTMMJJJJ());
    bisContainer.addLabelPair("Bis", bis);

    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      SimpleContainer projektContainer = new SimpleContainer(
          baseData.getComposite());
      projekt = control.getProjekt();
      projektContainer.addLabelPair("Projekt", projekt);
    }

    ColumnLayout finanzData = new ColumnLayout(uebersicht.getComposite(), 6);

    SimpleContainer einnahmen = new SimpleContainer(finanzData.getComposite());
    sollEinnahme = new BigDecimalInput(
        (BigDecimal) control.getWirtschaftsplan().getAttribute("planEinnahme"),
        Einstellungen.BIGDECIMALFORMAT);
    sollEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Soll", sollEinnahme);
    BigDecimalInput istEinnahme = new BigDecimalInput(
        (BigDecimal) control.getWirtschaftsplan().getAttribute("istEinnahme"),
        Einstellungen.BIGDECIMALFORMAT);
    istEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Ist", istEinnahme);

    if (verbindlichkeiten)
    {
      SimpleContainer forderungen = new SimpleContainer(
          finanzData.getComposite());
      BigDecimalInput istForderungen = new BigDecimalInput((BigDecimal) control
          .getWirtschaftsplan().getAttribute("istForderungen"),
          Einstellungen.BIGDECIMALFORMAT);
      istForderungen.disable();
      forderungen.addLabelPair("Forderungen Ist", istForderungen);
      BigDecimalInput istPositiv = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan().getAttribute("istPlus"),
          Einstellungen.BIGDECIMALFORMAT);
      istPositiv.disable();
      forderungen.addLabelPair("Einnahmen inkl. Ford. Ist", istPositiv);
    }

    SimpleContainer ausgaben = new SimpleContainer(finanzData.getComposite());
    sollAusgaben = new BigDecimalInput(
        (BigDecimal) control.getWirtschaftsplan().getAttribute("planAusgabe"),
        Einstellungen.BIGDECIMALFORMAT);
    sollAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Soll", sollAusgaben);
    BigDecimalInput istAusgaben = new BigDecimalInput(
        (BigDecimal) control.getWirtschaftsplan().getAttribute("istAusgabe"),
        Einstellungen.BIGDECIMALFORMAT);
    istAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Ist", istAusgaben);

    if (verbindlichkeiten)
    {
      SimpleContainer verbindlichkeitenContainer = new SimpleContainer(
          finanzData.getComposite());
      BigDecimalInput istVerbindlichkeiten = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan()
              .getAttribute("istVerbindlichkeiten"),
          Einstellungen.BIGDECIMALFORMAT);
      istVerbindlichkeiten.disable();
      verbindlichkeitenContainer.addLabelPair("Verbindlichkeiten Ist",
          istVerbindlichkeiten);
      BigDecimalInput istNegativ = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan().getAttribute("istMinus"),
          Einstellungen.BIGDECIMALFORMAT);
      istNegativ.disable();
      verbindlichkeitenContainer.addLabelPair("Ausgaben inkl. Verb. Ist",
          istNegativ);
    }

    if (ruecklagen)
    {
      SimpleContainer ruecklagenGebildetContainer = new SimpleContainer(
          finanzData.getComposite());
      sollRuecklagenGebildet = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan()
              .getAttribute("planRuecklagenGebildet"),
          Einstellungen.BIGDECIMALFORMAT);
      sollRuecklagenGebildet.disable();
      ruecklagenGebildetContainer.addLabelPair("Rücklagen gebildet Soll",
          sollRuecklagenGebildet);
      BigDecimalInput istRuecklagenGebildet = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan()
              .getAttribute("istRücklagenGebildet"),
          Einstellungen.BIGDECIMALFORMAT);
      istRuecklagenGebildet.disable();
      ruecklagenGebildetContainer.addLabelPair("Rücklagen gebildet Ist",
          istRuecklagenGebildet);

      SimpleContainer ruecklagenAufgeloestContainer = new SimpleContainer(
          finanzData.getComposite());
      sollRuecklagenAufgeloest = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan()
              .getAttribute("planRuecklagenAufgeloest"),
          Einstellungen.BIGDECIMALFORMAT);
      sollRuecklagenAufgeloest.disable();
      ruecklagenAufgeloestContainer.addLabelPair("Rücklagen aufgelöst Soll",
          sollRuecklagenAufgeloest);
      BigDecimalInput istRuecklagenAufgeloest = new BigDecimalInput(
          (BigDecimal) control.getWirtschaftsplan()
              .getAttribute("istRücklagenAufgelöst"),
          Einstellungen.BIGDECIMALFORMAT);
      istRuecklagenAufgeloest.disable();
      ruecklagenAufgeloestContainer.addLabelPair("Rücklagen aufgelöst Ist",
          istRuecklagenAufgeloest);
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
    BigDecimal sollEinnahmen = BigDecimal.ZERO;
    for (WirtschaftsplanNode node : einnahmen)
    {
      sollEinnahmen = sollEinnahmen.add(node.getSoll());
    }

    BigDecimal sollAusgaben = BigDecimal.ZERO;

    for (WirtschaftsplanNode node : ausgaben)
    {
      sollAusgaben = sollAusgaben.add(node.getSoll());
    }

    this.sollEinnahme.setValue(sollEinnahmen);
    this.sollAusgaben.setValue(sollAusgaben);

    if (sollRuecklagenGebildet != null && sollRuecklagenAufgeloest != null)
    {
      List<WirtschaftsplanNode> ruecklagenArten;
      List<WirtschaftsplanNode> ruecklagenItems = new ArrayList<>();

      try
      {
        ruecklagenArten = (List<WirtschaftsplanNode>) control.getRuecklagen()
            .getItems();
        for (WirtschaftsplanNode node : ruecklagenArten)
        {
          GenericIterator<WirtschaftsplanNode> it = node.getChildren();
          while (it.hasNext())
          {
            ruecklagenItems.add(it.next());
          }
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(
            "Fehler beim Aktualisieren der Übersicht!");
      }

      BigDecimal sollRuecklagenGebildet = BigDecimal.ZERO;
      for (WirtschaftsplanNode node : ruecklagenItems)
      {
        sollRuecklagenGebildet = sollRuecklagenGebildet
            .add(node.getSollEinnahmen());
      }

      BigDecimal sollRuecklagenAufgeloest = BigDecimal.ZERO;

      for (WirtschaftsplanNode node : ruecklagenItems)
      {
        sollRuecklagenAufgeloest = sollRuecklagenAufgeloest
            .add(node.getSollAusgaben());
      }

      this.sollRuecklagenGebildet.setValue(sollRuecklagenGebildet);
      this.sollRuecklagenAufgeloest.setValue(sollRuecklagenAufgeloest);
    }
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

  public SelectInput getProjekt()
  {
    return projekt;
  }
}
