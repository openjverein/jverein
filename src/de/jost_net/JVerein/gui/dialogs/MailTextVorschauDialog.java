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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.control.IMailControl;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.util.EvalMail;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
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

  private final de.willuhn.jameica.system.Settings settings;

  public MailTextVorschauDialog(IMailControl control,
      Map<String, Object> map, int position, boolean mitMitglied)
  {
    super(position);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
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

    if (mitMitglied && control.getEmpfaengerList() == null)
    {
      mitglied = new MitgliedInput().getMitgliedInput(mitglied, null,
          Einstellungen.getEinstellung().getMitgliedAuswahl());
      mitglied.addListener(new MitgliedListener());
      if (mitglied instanceof SelectInput)
      {
        ((SelectInput) mitglied).setPleaseChoose("Optional auswählen");
        ((SelectInput) mitglied).setPreselected(null);
      }
      container.addLabelPair("Mitglied", mitglied);
    }
    else if (mitMitglied && control.getEmpfaengerList() != null)
    {
      List<Mitglied> empfaenger = control.getEmpfaengerList();
      mitglied = new SelectInput(empfaenger, null);
      listener = new MitgliedListener();
      mitglied.addListener(listener);
      if (empfaenger.isEmpty() || empfaenger.size() == 1)
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
