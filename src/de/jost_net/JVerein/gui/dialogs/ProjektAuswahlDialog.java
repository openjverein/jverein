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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Projekt;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

/**
 * Ein Dialog, ueber den man ein Projekt auswählen kann.
 */
public class ProjektAuswahlDialog extends AbstractDialog<Projekt>
{

  private Projekt selected = null;

  private SelectInput projekte = null;

  private CheckboxInput ueberschreiben = null;

  private LabelInput status = null;

  private boolean abort = false;

  private boolean ueberschr;

  Buchung[] buchungen = null;

  public ProjektAuswahlDialog(int position, Buchung[] buchungen)
  {
    super(position);
    this.buchungen = buchungen;

    setTitle("Projekt auswählen");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Projekt", this.getProjekte());
    group.addLabelPair("Projekte überschreiben", getUeberschreiben());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        if (projekte.getValue() == null)
        {
          status.setValue("Bitte auswählen");
          status.setColor(Color.ERROR);
          return;
        }
        selected = (Projekt) projekte.getValue();
        ueberschr = (boolean) getUeberschreiben().getValue();
        close();
      }
    }, null, false, "ok.png");
    buttons.addButton("Entfernen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        selected = null;
        close();
      }
    }, null, false, "user-trash-full.png");
    buttons.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        abort = true;
        close();
      }
    }, null, false, "process-stop.png");
    getShell().addListener(SWT.Close, new Listener()
    {
      public void handleEvent(Event event)
      {
        abort = true;
      }
    });
    buttons.paint(parent);
  }

  @Override
  protected Projekt getData() throws Exception
  {
    return this.selected;
  }

  public boolean getAbort()
  {
    return abort;
  }

  public boolean getOverride()
  {
    return ueberschr;
  }

  private SelectInput getProjekte() throws RemoteException
  {
    if (this.projekte != null)
    {
      return this.projekte;
    }
    DBIterator<Projekt> pj = Einstellungen.getDBService()
        .createList(Projekt.class);
    String select = "((startdatum is null or startdatum <= ?) and (endedatum is null or endedatum >= ?)) ";

    if (buchungen != null)
    {
      /*
       * UND-Verknüpfung der Datumsbereiche, damit nur Projekte angezeigt
       * werden, die für die Auswahl gültig sind
       */
      for (Buchung buchung : buchungen)
      {
        pj.addFilter(select,
            new Object[] { buchung.getDatum(), buchung.getDatum() });
      }
    }

    pj.setOrder("ORDER BY bezeichnung");
    this.projekte = new SelectInput(
        pj != null ? PseudoIterator.asList(pj) : null, null);
    this.projekte.setValue(null);
    this.projekte.setPleaseChoose("Bitte Projekt auswählen");
    this.projekte.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        selected = (Projekt) projekte.getValue();
      }
    });
    return this.projekte;
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  private CheckboxInput getUeberschreiben()
  {
    if (ueberschreiben != null)
    {
      return ueberschreiben;
    }
    ueberschreiben = new CheckboxInput(false);
    return ueberschreiben;
  }
}
