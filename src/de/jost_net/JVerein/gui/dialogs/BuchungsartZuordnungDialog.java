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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;

/**
 * Dialog zur Zuordnung einer Buchungsart.
 */
public class BuchungsartZuordnungDialog extends AbstractDialog<Buchungsart>
{

  private AbstractInput buchungsarten = null;

  private SelectInput buchungsklassen = null;

  private CheckboxInput ueberschreiben = null;

  private LabelInput status = null;

  private Buchungsart buchungsart = null;

  private Buchungsklasse buchungsklasse = null;

  private boolean ueberschr;

  private boolean abort = true;

  /**
   * @param position
   */
  public BuchungsartZuordnungDialog(int position)
  {
    super(position);
    setTitle("Zuordnung Buchungsart");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Buchungsart", getBuchungsartAuswahl());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      group.addLabelPair("Buchungsklasse", getBuchungsklasseAuswahl());
    }
    group.addLabelPair("Buchungsarten überschreiben", getUeberschreiben());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        if (buchungsarten.getValue() == null)
        {
          status.setValue("Bitte Buchungsart auswählen");
          status.setColor(Color.ERROR);
          return;
        }
        if (buchungsarten.getValue() instanceof Buchungsart)
        {
          buchungsart = (Buchungsart) buchungsarten.getValue();
        }
        try
        {
          if ((Boolean) Einstellungen
              .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
          {
            if (buchungsklassen.getValue() == null)
            {
              status.setValue("Bitte Buchungsklasse auswählen");
              status.setColor(Color.ERROR);
              return;
            }
            if (buchungsklassen.getValue() instanceof Buchungsklasse)
            {
              buchungsklasse = (Buchungsklasse) buchungsklassen.getValue();
            }
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        ueberschr = (Boolean) getUeberschreiben().getValue();
        abort = false;
        close();
      }
    }, null, true, "ok.png");
    buttons.addButton("Entfernen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        buchungsart = null;
        buchungsklasse = null;
        abort = false;
        close();
      }
    }, null, false, "user-trash-full.png");
    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
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
  public Buchungsart getData() throws Exception
  {
    return buchungsart;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungsart;
  }

  public Buchungsklasse getBuchungsklasse()
  {
    return buchungsklasse;
  }

  public boolean getOverride()
  {
    return ueberschr;
  }

  public boolean getAbort()
  {
    return abort;
  }

  private Input getBuchungsartAuswahl() throws RemoteException
  {
    if (buchungsarten != null)
    {
      return buchungsarten;
    }
    buchungsarten = new BuchungsartInput().getBuchungsartInput(buchungsarten,
        null, buchungsarttyp.BUCHUNGSART, (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsarten.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          status.setValue("");
          if (buchungsklassen != null && buchungsklassen.getValue() == null)
            buchungsklassen.setValue(
                ((Buchungsart) buchungsarten.getValue()).getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    return buchungsarten;
  }

  private SelectInput getBuchungsklasseAuswahl() throws RemoteException
  {
    if (buchungsklassen != null)
    {
      return buchungsklassen;
    }
    buchungsklassen = new BuchungsklasseInput()
        .getBuchungsklasseInput(buchungsklassen, null);
    buchungsklassen.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        status.setValue("");
      }
    });
    return buchungsklassen;
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
