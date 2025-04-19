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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList; 
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.SaldoControl;
import de.jost_net.JVerein.util.Datum;
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
  
  private Calendar calendar = Calendar.getInstance();;
  
  private ArrayList<Button> buttons = new ArrayList<Button>();

  public QuickAccessPart(SaldoControl control, boolean additionalButtons)
  {
    this.control = control;
    this.additionalButtons = additionalButtons;
    Calendar calendar = Calendar.getInstance();
    jahr = calendar.get(Calendar.YEAR);
    try
    {
      if (genYearStartDate(jahr).after(calendar.getTime()))
      {
        calendar.add(Calendar.YEAR, -1);
      }
    }
    catch (Exception e)
    {
      //
    }
    finally
    {
      calendar.add(Calendar.YEAR, 1-anzahlButtons);
      jahr = calendar.get(Calendar.YEAR);
      startjahr = jahr;
    }
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup quickGroup = new LabelGroup(parent, "Schnellzugriff");
    ButtonArea quickBtns = new ButtonArea();
    
    Button home = new Button("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          jahr = startjahr;
          Integer geschaeftsjahr = jahr+anzahlButtons-1;
          updateButtons();
          control.getDatumvon().setDate(genYearStartDate(geschaeftsjahr));
          control.getDatumbis().setDate(genYearEndDate(geschaeftsjahr));
          control.getGeschaeftsjahr().setValue(geschaeftsjahr.toString());
          if (control instanceof AbstractSaldoControl)
          {
            ((AbstractSaldoControl) control).reloadList();
          }
          else
          {
            control.getSaldoList();
          }
        }
        catch (RemoteException e)
        {
          throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
        }
        catch (ParseException e)
        {
          throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
        }
      }
    }, null, false, "edit-undo.png");
    quickBtns.addButton(home);
    
    Button zurueck = new Button("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        jahr = jahr - 5;
        updateButtons();
      }
    }, null, false, "go-previous.png");
    quickBtns.addButton(zurueck);
    
    Button vor = new Button("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        jahr = jahr + 5;
        updateButtons();
      }
    }, null, false, "go-next.png");
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
      try
      {
        if (offset == null)
        {
          control.getDatumvon().setDate(von);
          control.getDatumbis().setDate(bis);
          Integer year = control.isGeschaeftsjahr();
          if (year != 0)
          {
            control.getGeschaeftsjahr().setValue(year.toString());
          }
          else
          {
            control.getGeschaeftsjahr().setValue("");
          }
        }
        else
        {
          Integer geschaeftsjahr = jahr+offset;
          control.getDatumvon().setDate(genYearStartDate(geschaeftsjahr));
          control.getDatumbis().setDate(genYearEndDate(geschaeftsjahr));
          control.getGeschaeftsjahr().setValue(geschaeftsjahr.toString());
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
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
      }
      catch (ParseException e)
      {
        throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
      }
    }
  }

  private Date deltaDaysFromNow(Integer delta)
  {
    Date now = new Date();
    calendar.setTime(now);

    // add delta days to calendar instance
    calendar.add(Calendar.DAY_OF_MONTH, delta);

    // get the date instance
    return calendar.getTime();
  }

  private Date genYearStartDate(Integer year) throws ParseException, RemoteException
  {
    return Datum.toDate(Einstellungen.getEinstellung()
        .getBeginnGeschaeftsjahr() + year);
  }

  private Date genYearEndDate(Integer year) throws ParseException, RemoteException
  {
    calendar.setTime(genYearStartDate(year));
    calendar.add(Calendar.YEAR, 1);
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    return calendar.getTime();
  }

}
