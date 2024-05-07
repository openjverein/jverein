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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * Dialog zur Zuordnung einer Buchungsart.
 */
public class BuchungenDeleteDialog extends AbstractDialog<Date>
{

  private DateInput dateInput = null;

  private CheckboxInput sollbuchungenloeschen = null;
  
  private Date date = null;
  
  private Boolean loeschen = true;
  
  private String text = "Diese Aktion löscht alle Buchungen vor dem "
      + "selektierten Datum."
      + "\nSollbuchungen können mit gelöscht werden damit "
      + "\nkeine offenen Buchungen im Mitgliedskonto bleiben."
      + "\nDie Buchungen können nicht wieder hergestellt werden!"
      + "\nBitte Aufbewahrungsfristen beachten!";

  /**
   * @param position
   */
  public BuchungenDeleteDialog(int position)
  {
    super(position);
    setTitle("Alte Buchungen löschen");
    setPanelText("Alte Buchungen löschen?");
    setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(text,true);
    
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Buchungen löschen älter als", getDatumAuswahl());
    group.addLabelPair("Zugeordnete Sollbuchungen löschen", getSollbuchungenLoeschen());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Ok", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        date = (Date) getDatumAuswahl().getValue();
        loeschen = (Boolean) getSollbuchungenLoeschen().getValue();
        close();
      }
    }, null, true, "ok.png");

    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        date = null;
        close();
      }
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Date getData() throws Exception
  {
    return date;
  }
  
  public Boolean getLoeschen()
  {
    return loeschen;
  }
  
  private DateInput getDatumAuswahl()
  {
    if (dateInput != null)
    {
      return dateInput;
    }
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2000);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    dateInput = new DateInput(new Date(cal.getTimeInMillis()));
    return dateInput;
  }

  private CheckboxInput getSollbuchungenLoeschen()
  {
    if (sollbuchungenloeschen != null)
    {
      return sollbuchungenloeschen;
    }
    sollbuchungenloeschen = new CheckboxInput(true);
    return sollbuchungenloeschen;
  }
}
