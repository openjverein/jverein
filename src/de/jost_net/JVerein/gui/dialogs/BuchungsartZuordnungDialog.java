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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.DBIterator;
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

  private AbstractInput buchungsartInput = null;

  private SelectInput buchungsklasseInput = null;

  private SelectInput steuerInput = null;

  private CheckboxInput ueberschreiben = null;

  private LabelInput status = null;

  private Buchungsart buchungsart = null;

  private Buchungsklasse buchungsklasse = null;

  private Steuer steuer = null;

  private boolean ueberschr;

  private boolean löschen = false;

  private boolean abort = true;

  private boolean klasseInBuchung;

  private boolean steuerInBuchung;

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

    klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Buchungsart", getBuchungsartAuswahl());
    if (klasseInBuchung)
    {
      group.addLabelPair("Buchungsklasse", getBuchungsklasseAuswahl());
    }
    if (steuerInBuchung)
    {
      group.addLabelPair("Steuer", getSteuerAuswahl());
    }
    group.addLabelPair("Überschreiben", getUeberschreiben());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        boolean buchungsartNull = true;
        if (buchungsartInput.getValue() != null)
        {
          buchungsartNull = false;
        }
        boolean buchungsklasseNull = true;
        if (klasseInBuchung && buchungsklasseInput.getValue() != null)
        {
          buchungsklasseNull = false;
        }
        boolean steuerNull = true;
        if (steuerInBuchung && steuerInput.getValue() != null)
        {
          steuerNull = false;
        }
        if (!klasseInBuchung && !steuerInBuchung)
        {
          if (buchungsartNull)
          {
            status.setValue("Bitte Buchungsart auswählen");
            status.setColor(Color.ERROR);
            return;
          }
        }
        else
        {
          if (buchungsartNull && buchungsklasseNull && steuerNull)
          {
            status.setValue("Bitte mindestens einen Eintrag auswählen");
            status.setColor(Color.ERROR);
            return;
          }
        }

        buchungsart = (Buchungsart) buchungsartInput.getValue();
        if (klasseInBuchung)
        {
          buchungsklasse = (Buchungsklasse) buchungsklasseInput.getValue();
        }
        if (steuerInBuchung)
        {
          steuer = (Steuer) steuerInput.getValue();
        }
        ueberschr = (Boolean) getUeberschreiben().getValue();
        löschen = false;
        abort = false;
        close();
      }
    }, null, true, "ok.png");
    buttons.addButton("Alle Entfernen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        löschen = true;
        abort = false;
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

  public Steuer getSteuer()
  {
    return steuer;
  }

  public boolean getOverride()
  {
    return ueberschr;
  }

  public boolean getAbort()
  {
    return abort;
  }

  public boolean getDelete()
  {
    return löschen;
  }

  private Input getBuchungsartAuswahl() throws RemoteException
  {
    if (buchungsartInput != null)
    {
      return buchungsartInput;
    }
    buchungsartInput = new BuchungsartInput().getBuchungsartInput(
        buchungsartInput, null, buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    if (buchungsartInput instanceof SelectInput)
    {
      if (klasseInBuchung || steuerInBuchung)
      {
        ((SelectInput) buchungsartInput)
            .setPleaseChoose("Buchungsart nicht ändern");
      }
      else
      {
        ((SelectInput) buchungsartInput).setPleaseChoose("Bitte auswählen");
      }
    }
    buchungsartInput.addListener(event -> {
      try
      {
        status.setValue("");
        if (buchungsartInput.getValue() != null)
        {
          if (buchungsklasseInput != null
              && buchungsklasseInput.getValue() == null)
            buchungsklasseInput
                .setValue(((Buchungsart) buchungsartInput.getValue())
                    .getBuchungsklasse());
          if (steuerInput != null && steuerInput.getValue() == null)
            steuerInput.setValue(
                ((Buchungsart) buchungsartInput.getValue()).getSteuer());
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    });
    return buchungsartInput;
  }

  private SelectInput getBuchungsklasseAuswahl() throws RemoteException
  {
    if (buchungsklasseInput != null)
    {
      return buchungsklasseInput;
    }
    buchungsklasseInput = new BuchungsklasseInput()
        .getBuchungsklasseInput(buchungsklasseInput, null);
    if (buchungsklasseInput instanceof SelectInput)
    {
      ((SelectInput) buchungsklasseInput)
          .setPleaseChoose("Buchungsklasse nicht ändern");
    }
    buchungsklasseInput.addListener(event -> {
      status.setValue("");
    });
    return buchungsklasseInput;
  }

  private SelectInput getSteuerAuswahl() throws RemoteException
  {
    if (steuerInput != null)
    {
      return steuerInput;
    }
    List<Steuer> liste = new ArrayList<>();
    Steuer löschen = (Steuer) Einstellungen.getDBService()
        .createObject(Steuer.class, null);
    löschen.setName("Steuer löschen");
    liste.add(löschen);
    DBIterator<Steuer> it = Einstellungen.getDBService()
        .createList(Steuer.class);
    it.addFilter("aktiv = true");
    while (it.hasNext())
    {
      liste.add(it.next());
    }
    steuerInput = new SelectInput(liste, null);
    steuerInput.setAttribute("name");
    steuerInput.setPleaseChoose("Steuer nicht ändern");
    steuerInput.addListener(event -> {
      status.setValue("");
    });
    return steuerInput;
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
