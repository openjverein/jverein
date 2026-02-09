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
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.gui.control.IMailControl;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.util.EvalMail;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Ein Dialog zur Vorschau einer Mail.
 */
public class MailTextVorschauDialog extends AbstractDialog<Object>
{

  private final IMailControl control;

  private Map<String, Object> map;

  private boolean mitMitglied;

  private AbstractInput mitglied;

  private TextInput betreff;

  private TextAreaInput text;

  private EvalMail em;

  private String betreffString;

  private String textString;

  private MitgliedListener listener = null;

  private final Settings settings;

  private Map<Mitglied, Object> objectslist = null;

  public MailTextVorschauDialog(IMailControl control, Map<String, Object> map,
      int position, boolean mitMitglied)
  {
    super(position);
    settings = new Settings(this.getClass());
    this.control = control;
    this.map = map;
    this.mitMitglied = mitMitglied;
    setTitle("Mail-Text-Vorschau");
    setSize(settings.getInt("width", 550), settings.getInt("height", 450));

    try
    {
      this.open();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
  }

  @Override
  protected void onEscape()
  {
    close();
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent, true, 2);
    em = new EvalMail(map);

    betreffString = control.getBetreffString();
    textString = control.getTxtString();
    objectslist = control.getDruckMailList();
    List<Mitglied> empfaengerlist = control.getEmpfaengerList();

    if (mitMitglied && empfaengerlist == null && objectslist == null)
    {
      mitglied = new MitgliedInput().getMitgliedInput(mitglied, null,
          (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
      mitglied.addListener(new MitgliedListener());
      if (mitglied instanceof SelectInput)
      {
        ((SelectInput) mitglied).setPleaseChoose("Optional auswählen");
        ((SelectInput) mitglied).setPreselected(null);
      }
      container.addLabelPair("Mitglied", mitglied);
    }
    else if (mitMitglied)
    {
      List<Mitglied> empfaenger = null;
      if (empfaengerlist != null)
      {
        empfaenger = empfaengerlist;
      }
      else if (objectslist != null)
      {
        empfaenger = new ArrayList<>(objectslist.keySet());
      }
      mitglied = new SelectInput(empfaenger, null);
      listener = new MitgliedListener();
      mitglied.addListener(listener);
      // Bei keinem oder nur einem Eintrag kann man nichts auswählen
      if (empfaenger == null || empfaenger.size() < 2)
      {
        mitglied.disable();
      }
      container.addLabelPair("Empfänger", mitglied);
    }

    betreff = new TextInput(em.evalBetreff(betreffString));
    betreff.setEnabled(false);
    container.addLabelPair("Betreff", betreff);
    text = new TextAreaInput(em.evalText(textString));
    text.setEnabled(false);
    container.addLabelPair("Text", text);

    ButtonArea b = new ButtonArea();
    b.addButton("Schließen", context -> close(), null, false,
        "process-stop.png");
    b.paint(parent);

    if (listener != null)
    {
      listener.handleEvent(null);
    }
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

  public class MitgliedListener implements Listener
  {

    MitgliedListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      Mitglied m = (Mitglied) mitglied.getValue();
      try
      {
        // Mitglied (m) NULL ist, dann wird die Dummy geliefert
        map = new MitgliedMap().getMap(m, map);
        // Falls es Anhänge gibt, dann auch deren Maps erzeugen, damit in der
        // Mail-Text-Vorschau die Variablen-Werte aus den Attachements verwendet
        // werden und nicht deren Dummy Maps
        if (objectslist != null)
        {
          if (objectslist.get(m) instanceof Rechnung)
          {
            map = new RechnungMap().getMap((Rechnung) objectslist.get(m), map);
          }
          else if (objectslist.get(m) instanceof Lastschrift)
          {
            map = new LastschriftMap().getMap((Lastschrift) objectslist.get(m),
                map);
          }
          else if (objectslist.get(m) instanceof Spendenbescheinigung)
          {
            map = new SpendenbescheinigungMap()
                .getMap((Spendenbescheinigung) objectslist.get(m), map);
          }
        }
        em = new EvalMail(map);
        betreff.setValue(em.evalBetreff(betreffString));
        text.setValue(em.evalText(textString));
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    }
  }
}
