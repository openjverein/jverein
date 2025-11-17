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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.util.SimpleVerticalContainer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, ueber den man ein eines Nicht-Mitglieds erzeugen kann.
 */
public class AbweichenderZahlerNeuDialog extends AbstractDialog<Boolean>
{

  private String status = null;

  private Boolean abort = true;

  private MitgliedControl control;

  private Mitglied mitglied;

  public AbweichenderZahlerNeuDialog(int position, Mitglied m)
  {
    super(position);
    mitglied = m;
    setTitle("Abweichender Zahler");
    setSize(1000, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    control = new MitgliedControl(null);
    control.setMitglied(mitglied);
    zeicheStammdaten(parent);
    zeichneZahlung(parent);
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Speichern", context -> {
      try
      {
        control.handleStore();
      }
      catch (ApplicationException e)
      {
        Logger.error("Fehler", e);
        status = e.getMessage();
      }
      abort = false;
      close();
    }, null, true, "document-save.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return this.abort;
  }

  public String getStatus()
  {
    return this.status;
  }

  /**
   * Zeichnet GUI-Felder für Stammdaten.
   * 
   * @param parentComposite
   *          Composite auf dem gezeichnet wird.
   * @throws RemoteException
   */
  private void zeicheStammdaten(Composite parentComposite)
      throws RemoteException
  {
    LabelGroup container = new LabelGroup(parentComposite, "Stammdaten");
    SimpleVerticalContainer cols = new SimpleVerticalContainer(
        container.getComposite(), true, 3);

    cols.addInput(control.getMitgliedstyp());

    cols.addInput(control.getAnrede());
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("n"))
    {
      cols.addInput(control.getTitel());
    }
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("j"))
    {
      control.getName(true).setName("Name Zeile 1");
      control.getVorname().setName("Name Zeile 2");
      control.getVorname().setMandatory(false);
    }
    cols.addInput(control.getName(true));
    cols.addInput(control.getVorname());
    cols.addInput(control.getAdressierungszusatz());

    cols.addInput(control.getStrasse());
    cols.addInput(control.getPlz());
    cols.addInput(control.getOrt());
    if ((Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN))
    {
      cols.addInput(control.getStaat());
    }
    if (control.getMitglied().getPersonenart().equalsIgnoreCase("n"))
    {
      cols.addInput(control.getGeburtsdatum());
      cols.addInput(control.getGeschlecht());
    }
    else
    {
      cols.addInput(control.getLeitwegID());
    }

    if ((Boolean) Einstellungen.getEinstellung(Property.KOMMUNIKATIONSDATEN))
    {
      cols.addInput(control.getTelefonprivat());
      cols.addInput(control.getHandy());
      cols.addInput(control.getTelefondienstlich());
      cols.addInput(control.getEmail());
    }
    cols.arrangeVertically();
  }

  private void zeichneZahlung(Composite parentComposite) throws RemoteException
  {
    LabelGroup zahlungsweg = new LabelGroup(parentComposite, "Zahlungsweg");
    zahlungsweg.getComposite().setLayout(new GridLayout(1, false));

    SimpleVerticalContainer cols1 = new SimpleVerticalContainer(
        zahlungsweg.getComposite(), false, 1);

    cols1.addInput(control.getZahlungsweg());
    cols1.arrangeVertically();

    LabelGroup bankverbindung = new LabelGroup(parentComposite,
        "Bankverbindung");
    bankverbindung.getComposite().setLayout(new GridLayout(1, false));
    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton(control.getKontoDatenLoeschenButton());
    buttons2.paint(bankverbindung.getComposite());

    SimpleVerticalContainer cols2 = new SimpleVerticalContainer(
        bankverbindung.getComposite(), false, 2);

    cols2.addInput(control.getMandatID());
    cols2.addInput(control.getMandatDatum());
    cols2.addInput(control.getMandatVersion());
    cols2.addInput(control.getLetzteLastschrift());
    cols2.addInput(control.getIban());
    cols2.addInput(control.getBic());
    cols2.addInput(control.getKontoinhaber());
    cols2.arrangeVertically();
  }
}
