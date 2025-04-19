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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.SaldoControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

public class VonBisPart implements Part
{
  private SaldoControl control;
  
  private boolean suchen = false;
  
  private Calendar calendar = Calendar.getInstance();;
  
  private enum RANGE
  {
    MONAT, TAG
  }
  
  public VonBisPart(SaldoControl control, boolean suchen)
  {
    this.control = control;
    this.suchen = suchen;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    if (!suchen)
    {
      LabelGroup group = new LabelGroup(parent, "Aktuell angezeigter Zeitraum");
      SimpleContainer left = new SimpleContainer(group.getComposite(), true, 4);
      left.addLabelPair("Von", control.getDatumvon());
      left.addLabelPair("Bis", control.getDatumbis());
    }
    else
    {
      LabelGroup group = new LabelGroup(parent,
          "Aktuell angezeigter Zeitraum und Suchen");
      SimpleContainer left = new SimpleContainer(group.getComposite());
      left.addLabelPair("Von", control.getDatumvon());
      left.addLabelPair("Bis", control.getDatumbis());
      left.addLabelPair("Geschäftsjahr", control.getGeschaeftsjahr());
      SimpleContainer right = new SimpleContainer(group.getComposite());
      right.addLabelPair("Von", control.getSuchDatumvon());
      right.addLabelPair("Bis", control.getSuchDatumbis());
      ButtonArea buttons = new ButtonArea();

      Button zurueck = new Button("", new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Date von = (Date) control.getSuchDatumvon().getValue();
          Date bis = (Date) control.getSuchDatumbis().getValue();
          if (getRangeTyp(von, bis) == RANGE.TAG)
          {
            int delta = (int) ChronoUnit.DAYS.between(von.toInstant(), bis.toInstant());
            delta++;
            calendar.setTime(von);
            calendar.add(Calendar.DAY_OF_MONTH, -delta);
            control.getSuchDatumvon().setValue(calendar.getTime());
            calendar.setTime(bis);
            calendar.add(Calendar.DAY_OF_MONTH, -delta);
            control.getSuchDatumbis().setValue(calendar.getTime());
          }
          else
          {
            LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
            delta++;
            calendar.setTime(von);
            calendar.add(Calendar.MONTH, -delta);
            control.getSuchDatumvon().setValue(calendar.getTime());
            calendar.add(Calendar.MONTH, delta);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            control.getSuchDatumbis().setValue(calendar.getTime());
          }
          refresh();
        }
      }, null, false, "go-previous.png");
      buttons.addButton(zurueck);

      Button vor = new Button("", new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Date von = (Date) control.getSuchDatumvon().getValue();
          Date bis = (Date) control.getSuchDatumbis().getValue();
          if (getRangeTyp(von, bis) == RANGE.TAG)
          {
            int delta = (int) ChronoUnit.DAYS.between(von.toInstant(), bis.toInstant());
            delta++;
            calendar.setTime(von);
            calendar.add(Calendar.DAY_OF_MONTH, delta);
            control.getSuchDatumvon().setValue(calendar.getTime());
            calendar.setTime(bis);
            calendar.add(Calendar.DAY_OF_MONTH, delta);
            control.getSuchDatumbis().setValue(calendar.getTime());
          }
          else
          {
            LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
            delta++;
            calendar.setTime(von);
            calendar.add(Calendar.MONTH, delta);
            control.getSuchDatumvon().setValue(calendar.getTime());
            calendar.add(Calendar.MONTH, delta);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            control.getSuchDatumbis().setValue(calendar.getTime());
          }
          refresh();
        }
      }, null, false, "go-next.png");
      buttons.addButton(vor);

      Button reset = new Button("Reset", new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          control.getSuchDatumvon().setValue(control.getDatumvon().getValue());
          control.getSuchDatumbis().setValue(control.getDatumbis().getValue());
        }
      }, null, false, "eraser.png");
      buttons.addButton(reset);

      Button suchen = new Button("Suchen", new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          refresh();
        }
      }, null, true, "search.png");
      buttons.addButton(suchen);
      right.addButtonArea(buttons);
    }
  }
  
  private void refresh() throws ApplicationException
  {
    checkDate();
    control.getDatumvon().setDate((Date) control.getSuchDatumvon().getValue());
    control.getDatumbis().setDate((Date) control.getSuchDatumbis().getValue());
    Integer year = control.isGeschaeftsjahr();
    if (year != 0)
    {
      control.getGeschaeftsjahr().setValue(year.toString());
    }
    else
    {
      control.getGeschaeftsjahr().setValue("");
    }
    if (control instanceof AbstractSaldoControl)
    {
      ((AbstractSaldoControl) control).reloadList();
    }
    else
    {
      control.getSaldoList();
    }
  }

  private void checkDate() throws ApplicationException
  {
    Date von = (Date) control.getSuchDatumvon().getValue();
    Date bis = (Date) control.getSuchDatumbis().getValue();
    if (von == null)
    {
      throw new ApplicationException("Bitte Von Datum eingeben!");
    }
    if (bis == null)
    {
      throw new ApplicationException("Bitte Bis Datum eingeben!");
    }
    if (von.after(bis))
    {
      throw new ApplicationException("Von Datum ist nach Bis Datum!");
    }
  }
  
  private RANGE getRangeTyp(Date von, Date bis) throws ApplicationException
  {
    checkDate();
    calendar.setTime(von);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    calendar.setTime(bis);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    return RANGE.MONAT;
  }
  
}
