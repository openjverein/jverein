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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.action.BuchungNeuAction;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.SplitbuchungNeuAction;
import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.BuchungsControl.Kontenfilter;
import de.jost_net.JVerein.gui.parts.BuchungPart;
import de.jost_net.JVerein.gui.parts.ButtonArea;
import de.jost_net.JVerein.gui.parts.Button;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungDetailView extends AbstractDetailView
{
  private BuchungsControl control;

  @Override
  public void bind() throws Exception
  {
    Kontenfilter art = Kontenfilter.GELDKONTO;
    if (this.getCurrentObject() != null
        && this.getCurrentObject() instanceof Buchung)
    {
      Buchung bu = (Buchung) this.getCurrentObject();
      if (bu.getKonto() != null
          && bu.getKonto().getKontoArt() == Kontoart.ANLAGE)
        art = Kontenfilter.ANLAGEKONTO;
    }
    control = new BuchungsControl(this, art);

    final boolean editable = control.isBuchungEditable();
    final boolean speicherung = control.getBuchung().getSpeicherung();

    BuchungPart part = new BuchungPart(control, this, !editable);
    part.paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BUCHUNGEN, false, "question-circle.png");
    buttons.addButton(control.getZurueckButton());
    buttons.addButton(control.getInfoButton());
    buttons.addButton(control.getVorButton());

    Button saveButton = new Button("Speichern", context -> {
      try
      {
        control.buchungSpeichern();

        // Bei Splitbuchungen nach dem Speichern zurück zu Splitübersicht
        if (!speicherung)
        {
          GUI.startPreviousView();
        }
      }
      catch (Exception e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }, null, true, "document-save.png");
    saveButton.setEnabled(editable);
    buttons.addButton(saveButton);

    Button saveNextButton = new Button("Speichern und neu", context -> {
      try
      {
        control.buchungSpeichern();

        // Bei Splitbuchungen neue Splitbuchung
        if (!speicherung)
        {
          if (Math.abs(SplitbuchungsContainer.getSumme(SplitbuchungTyp.HAUPT)
              .doubleValue()
              - SplitbuchungsContainer.getSumme(SplitbuchungTyp.SPLIT)
                  .doubleValue()) >= .01d)
            new SplitbuchungNeuAction().handleAction(context);
          else
            GUI.startPreviousView();
        }
        else
        {
          new BuchungNeuAction(control).handleAction(null);
        }
        GUI.getStatusBar().setSuccessText("Buchung übernommen");
      }
      catch (Exception e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }, null, false, "go-next.png");
    saveNextButton.setEnabled(editable);
    if (control.getBuchung().isNewObject())
    {
      buttons.addButton(saveNextButton);
    }

    buttons.paint(getParent());
  }

  @Override
  protected Savable getControl()
  {
    return control;
  }

  @Override
  public void unbind() throws OperationCanceledException, ApplicationException
  {
    // Bei Splitbuchunge Funktioniert die Änderungsüberwachung nicht, da nicht
    // direkt gespeichert wird.
    try
    {
      if (control.getBuchung().getSpeicherung())
      {
        super.unbind();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler bei Unbind", e);
      throw new ApplicationException("Fehler, siehe Systemlog");
    }
  }
}
