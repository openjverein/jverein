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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.control.SollbuchungPositionControl;
import de.jost_net.JVerein.gui.input.MitgliedSearchInput;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * Dialog zur Bearbeitung einer Splitbuchung.
 */
public class SollbuchungNeuDialog extends AbstractDialog<Boolean>
{

  private SollbuchungControl sollbControl;

  private SollbuchungPositionControl sollbPosControl;

  private Sollbuchung sollbuchung;

  private SollbuchungPosition sollbuchungPosition;

  private Boolean edit = false;

  public SollbuchungNeuDialog(Sollbuchung sollbuchung)
  {
    super(AbstractDialog.POSITION_CENTER);
    setTitle("Neue Sollbuchung");
    this.sollbuchung = sollbuchung;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    sollbuchungPosition = (SollbuchungPosition) Einstellungen.getDBService()
        .createObject(SollbuchungPosition.class, null);

    sollbControl = new SollbuchungControl(null, sollbuchung);
    sollbPosControl = new SollbuchungPositionControl(null, sollbuchungPosition);

    LabelGroup group = new LabelGroup(parent, null);
    ColumnLayout cols = new ColumnLayout(group.getComposite(), 2);
    // Sollbuchung
    SimpleContainer left = new SimpleContainer(cols.getComposite());
    left.addHeadline("Sollbuchung");
    left.addLabelPair("Mitglied", sollbControl.getMitglied());
    left.addLabelPair("Zahler", sollbControl.getZahler());
    left.addLabelPair("Datum", sollbControl.getDatum());
    left.addLabelPair("Verwendungszweck", sollbControl.getZweck1());
    left.addLabelPair("Zahlungsweg", sollbControl.getZahlungsweg());

    // Sollbuchungsposition, Datum und Zweck wird von Sollbuchung genommen
    SimpleContainer right = new SimpleContainer(cols.getComposite());
    right.addHeadline("Sollbuchungsposition");
    right.addLabelPair("Betrag", sollbPosControl.getBetrag());
    DateInput date = sollbPosControl.getDatum();
    date.setMandatory(false);
    right.addLabelPair("Datum", date);
    Input zweck = sollbPosControl.getZweck();
    zweck.setMandatory(false);
    right.addLabelPair("Zweck", zweck);
    right.addLabelPair("Buchungsart", sollbPosControl.getBuchungsart());
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      right.addLabelPair("Buchungsklasse", sollbPosControl.getBuchungsklasse());
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.MITGLIEDSKONTO_UEBERSICHT, false,
        "question-circle.png");

    // Speichern und zurück zum View
    buttons.addButton("Speichern", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (!handleStore())
        {
          return;
        }
        edit = false;
        close();
      }
    }, null, true, "document-save.png");

    // Speichern und Sollbuchung anzeigen
    buttons.addButton("Speichern und Anzeigen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        if (!handleStore())
        {
          return;
        }
        edit = true;
        close();
      }
    }, null, false, "document-save.png");

    // Speichern und neue Sollbuchung erzeugen
    // Es wird nur angeboten wenn es nicht im Mitgliedskonto Tab eines Mitglieds
    // aufgerufen wurde
    if (sollbControl.getMitglied().getValue() == null)
    {
      buttons.addButton("Speichern und Neu", new Action()
      {

        @Override
        public void handleAction(Object context)
        {
          if (!handleStore())
          {
            return;
          }
          try
          {
            Sollbuchung sollb = (Sollbuchung) Einstellungen.getDBService()
                .createObject(Sollbuchung.class, null);
            sollbControl.setSollbuchung(sollb);
            Input mitgliedInput = sollbControl.getMitglied();
            if (mitgliedInput instanceof SelectInput)
            {
              ((SelectInput) mitgliedInput).setPreselected(null);
            }
            else if (mitgliedInput instanceof MitgliedSearchInput)
            {
              ((MitgliedSearchInput) mitgliedInput)
                  .setValue("Zum Suchen tippen");
            }
            Input zahlerInput = sollbControl.getZahler();
            if (zahlerInput instanceof SelectInput)
            {
              ((SelectInput) zahlerInput).setPreselected(null);
            }
            else if (zahlerInput instanceof MitgliedSearchInput)
            {
              ((MitgliedSearchInput) zahlerInput).setValue("Zum Suchen tippen");
            }
          }
          catch (Exception e)
          {
            String fehler = "Fehler beim erzeugen der Sollbuchung";
            GUI.getStatusBar().setErrorText(fehler);
          }
        }
      }, null, false, "go-next.png");
    }

    // Aktion abbrechen
    buttons.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        edit = false;
        close();
      }
    }, null, false, "process-stop.png");
    buttons.paint(parent);

    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));

  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Boolean getData() throws Exception
  {
    return edit;
  }

  private boolean handleStore()
  {
    try
    {
      DBTransaction.starten();
      sollbControl.getBetrag().setValue(sollbPosControl.getBetrag().getValue());
      if (sollbPosControl.getDatum().getValue() == null)
      {
        sollbPosControl.getDatum().setValue(sollbControl.getDatum().getValue());
      }
      String zweck = (String) sollbPosControl.getZweck().getValue();
      if (zweck == null || zweck.isEmpty())
      {
        sollbPosControl.getZweck()
            .setValue(sollbControl.getZweck1().getValue());
      }
      if (!sollbControl.handleStore())
      {
        DBTransaction.rollback();
        return false;
      }
      sollbuchungPosition.setSollbuchung(sollbuchung.getID());
      if (!sollbPosControl.handleStore())
      {
        DBTransaction.rollback();
        return false;
      }
      DBTransaction.commit();
      return true;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim speichern der Sollbuchung";
      GUI.getStatusBar().setErrorText(fehler);
      DBTransaction.rollback();
      return false;
    }
  }

}
