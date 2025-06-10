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
package de.jost_net.JVerein.gui.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

public class SaldoControl extends AbstractControl
{
  protected DateLabel datumvon;

  protected DateLabel datumbis;
  
  protected DateInput suchdatumvon;

  protected DateInput suchdatumbis;
  
  protected TextInput geschaeftsjahr;
  
  protected Settings settings = null;


  public SaldoControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public DateLabel getDatumvon()
  {
    if (datumvon != null)
    {
      return datumvon;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("von", "01.01" + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    datumvon = new DateLabel(d);
    datumvon.disable();
    return datumvon;
  }

  public DateLabel getDatumbis()
  {
    if (datumbis != null)
    {
      return datumbis;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("bis", "31.12." + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    datumbis = new DateLabel(d);
    datumbis.disable();
    return datumbis;
  }
  
  public Input getGeschaeftsjahr()
  {
    if (geschaeftsjahr != null)
    {
      return geschaeftsjahr;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(getDatumvon().getDate());
    geschaeftsjahr = new TextInput(Integer.valueOf(((int) 
        cal.get(Calendar.YEAR))).toString());
    geschaeftsjahr.disable();
    return geschaeftsjahr;
  }
  
  public DateInput getSuchDatumvon()
  {
    if (suchdatumvon != null)
    {
      return suchdatumvon;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("von", "01.01" + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    suchdatumvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    return suchdatumvon;
  }

  public DateInput getSuchDatumbis()
  {
    if (suchdatumbis != null)
    {
      return suchdatumbis;
    }
    Calendar cal = Calendar.getInstance();
    Date d = new Date();
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString("bis", "31.12." + cal.get(Calendar.YEAR)));
    }
    catch (ParseException e)
    {
      //
    }
    suchdatumbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    return suchdatumbis;
  }
  
  public Part getSaldoList() throws ApplicationException
  {
    //to be implemented in derived class
    return new TablePart(new ArrayList<>(), null);
  }
  
  public Integer isGeschaeftsjahr()
  {
    try
    {
      Integer year;
      Calendar cal = Calendar.getInstance();
      Date von = getDatumvon().getDate();
      Date bis = getDatumbis().getDate();
      cal.setTime(von);
      year = cal.get(Calendar.YEAR);
      Date gjvon  = Datum.toDate((String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR) + year);
      if (!von.equals(gjvon))
      {
        return 0;
      }
      cal.add(Calendar.YEAR, 1);
      cal.add(Calendar.DAY_OF_MONTH, -1);
      if (bis.equals(cal.getTime()))
      {
        return year;
      }
    }
    catch (Exception e)
    {
      //
    }
    return 0;
  }
  
  public class DateLabel extends TextInput
  {
    private Date d;
    
    DateLabel(Date date)
    {
      super("");
      setDate(date);
    }
    
    public void setDate(Date date)
    {
      d = date;
      JVDateFormatTTMMJJJJ df = new JVDateFormatTTMMJJJJ();
      String dstring = d == null ? "" : df.format(d);
      super.setValue(dstring);
    }

    public Date getDate()
    {
      return d;
    }
  }
}
