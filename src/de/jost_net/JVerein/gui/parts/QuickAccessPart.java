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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList; 
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.control.SaldoControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class QuickAccessPart implements Part
{
  private SaldoControl control;
  
  private int anzahlButtons = 10;
  
  private boolean additionalButtons;
  
  private Integer jahr;
  
  private Integer startjahr;
  
  private ArrayList<Button> buttons = new ArrayList<Button>();

  public QuickAccessPart(SaldoControl control, boolean additionalButtons)
  {
    this.control = control;
    this.additionalButtons = additionalButtons;
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.YEAR, 1-anzahlButtons);
    jahr = calendar.get(Calendar.YEAR);
    startjahr = jahr;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup quickGroup = new LabelGroup(parent, "Schnellzugriff");
    ButtonArea quickBtns = new ButtonArea();
    
    Button home = new Button("*", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        jahr = startjahr;
        updateButtons();
        control.getDatumvon().setValue(genYearStartDate(jahr+anzahlButtons-1));
        control.getDatumbis().setValue(genYearEndDate(jahr+anzahlButtons-1));
        control.getSaldoList();
      }
    });
    quickBtns.addButton(home);
    
    Button zurueck = new Button("<", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        jahr = jahr - 5;
        updateButtons();
      }
    });
    quickBtns.addButton(zurueck);
    
    Button vor = new Button(">", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        jahr = jahr + 5;
        updateButtons();
      }
    });
    quickBtns.addButton(vor);
    
    
    Integer j = 0;
    for (Integer i = 0; i < anzahlButtons; i++)
    {
      j = i + jahr;
      buttons.add(new Button(j.toString(), new QuickAccessAction(control,i)));
    }
    for (Button bu : buttons)
    {
      quickBtns.addButton(bu);
    }
    
    if (additionalButtons)
    {
      quickBtns.addButton("Letzte 30 Tage",
          new QuickAccessAction(control, deltaDaysFromNow(-30), new Date()));
      quickBtns.addButton("Letzte 60 Tage",
          new QuickAccessAction(control, deltaDaysFromNow(-60), new Date()));
      quickBtns.addButton("Letzte 90 Tage",
          new QuickAccessAction(control, deltaDaysFromNow(-90), new Date()));
    }
    quickGroup.addPart(quickBtns);
  }
  
  private void updateButtons()
  {
    Integer j = 0;
    for (int i = 0; i < anzahlButtons; i++)
    {
      j = i + jahr;
      buttons.get(i).setText(j.toString());
    }
  }

  private class QuickAccessAction implements Action
  {

    private SaldoControl control;

    private Integer offset;
    
    private Date von;

    private Date bis;

    QuickAccessAction(SaldoControl control, Integer offset)
    {
      this.control = control;
      this.offset = offset;
    }
    

    QuickAccessAction(SaldoControl control, Date von, Date bis)
    {
      this.control = control;
      this.von = von;
      this.bis = bis;
      this.offset = null;
    }

    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      if (offset == null)
      {
        control.getDatumvon().setValue(von);
        control.getDatumbis().setValue(bis);
      }
      else
      {
        control.getDatumvon().setValue(genYearStartDate(jahr+offset));
        control.getDatumbis().setValue(genYearEndDate(jahr+offset));
      }

      control.getSaldoList();
    }
  }

  public Date deltaDaysFromNow(Integer delta)
  {
    Date now = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(now);

    // add 5 days to calendar instance
    calendar.add(Calendar.DAY_OF_MONTH, delta);

    // get the date instance
    return calendar.getTime();
  }

  public Date genYearStartDate(Integer year)
  {
    Calendar calendarStart = Calendar.getInstance();
    calendarStart.set(Calendar.YEAR, year);
    calendarStart.set(Calendar.MONTH, 0);
    calendarStart.set(Calendar.DAY_OF_MONTH, 1);
    return calendarStart.getTime();
  }

  public Date genYearEndDate(Integer year)
  {
    Calendar calendarStart = Calendar.getInstance();
    calendarStart.set(Calendar.YEAR, year);
    calendarStart.set(Calendar.MONTH, 11);
    calendarStart.set(Calendar.DAY_OF_MONTH, 31);
    return calendarStart.getTime();
  }

}
