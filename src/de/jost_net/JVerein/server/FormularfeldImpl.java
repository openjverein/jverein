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
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;

import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.keys.Ausrichtung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularfeldImpl extends AbstractJVereinDBObject
    implements Formularfeld
{

  private static final long serialVersionUID = -5754574029501014426L;

  public FormularfeldImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "formularfeld";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "name";
  }

  @Override
  protected void deleteCheck()
  {
    //
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getName() == null || getName().length() == 0)
      {
        throw new ApplicationException("Bitte Inhalt eingeben");
      }
      for (String zeile : getName().split("\n"))
      {
        if (zeile.contains(RechnungVar.QRCODE_SUMME.getName())
            && !zeile.replace("$", "")
                .equalsIgnoreCase(RechnungVar.QRCODE_SUMME.getName()))
        {
          throw new ApplicationException(RechnungVar.QRCODE_SUMME.getName()
              + " Muss alleine in der Zeile Stehen!");
        }
        if (zeile.contains(SpendenbescheinigungVar.UNTERSCHRIFT.getName())
            && !zeile.replace("$", "").equalsIgnoreCase(
                SpendenbescheinigungVar.UNTERSCHRIFT.getName()))
        {
          throw new ApplicationException(
              SpendenbescheinigungVar.UNTERSCHRIFT.getName()
                  + " Muss alleine in der Zeile Stehen!");
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Formularfeld kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("formular".equals(field))
    {
      return Formular.class;
    }
    return null;
  }

  @Override
  public Formular getFormular() throws RemoteException
  {
    return (Formular) getAttribute("formular");
  }

  @Override
  public void setFormular(Formular formular) throws RemoteException
  {
    setAttribute("formular", formular);
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public int getSeite() throws RemoteException
  {
    Integer ret = (Integer) getAttribute("seite");
    if (ret == null)
    {
      ret = Integer.valueOf(1);
    }
    return ret;
  }

  @Override
  public void setSeite(int seite) throws RemoteException
  {
    setAttribute("seite", seite);
  }

  @Override
  public Double getX() throws RemoteException
  {
    Double ret = (Double) getAttribute("x");
    if (ret == null)
    {
      ret = Double.valueOf(0);
    }
    return ret;
  }

  @Override
  public void setX(Double x) throws RemoteException
  {
    setAttribute("x", x);
  }

  @Override
  public Double getY() throws RemoteException
  {
    Double ret = (Double) getAttribute("y");
    if (ret == null)
    {
      ret = Double.valueOf(0);
    }

    return ret;
  }

  @Override
  public void setY(Double y) throws RemoteException
  {
    setAttribute("y", y);
  }

  @Override
  public String getFont() throws RemoteException
  {
    return (String) getAttribute("font");
  }

  @Override
  public void setFont(String font) throws RemoteException
  {
    setAttribute("font", font);
  }

  @Override
  public Integer getFontsize() throws RemoteException
  {
    Integer ret = (Integer) getAttribute("fontsize");
    if (ret == null)
    {
      ret = Integer.valueOf(10);
    }
    return ret;
  }

  @Override
  public void setFontsize(Integer fontsize) throws RemoteException
  {
    setAttribute("fontsize", fontsize);
  }

  @Override
  public Integer getFontstyle() throws RemoteException
  {
    Integer ret = (Integer) getAttribute("fontstyle");
    if (ret == null)
    {
      ret = Integer.valueOf(SWT.NORMAL);
    }
    return ret;
  }

  @Override
  public void setFontstyle(Integer fontstyle) throws RemoteException
  {
    setAttribute("fontstyle", fontstyle);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("ausrichtung".equals(fieldName))
    {
      return this.getAusrichtung();
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public String getObjektName()
  {
    return "Formularfeld";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Formularfelder";
  }

  @Override
  public Ausrichtung getAusrichtung() throws RemoteException
  {
    Object o = super.getAttribute("ausrichtung");
    if (o == null)
    {
      return Ausrichtung.LINKS;
    }
    return Ausrichtung.getByKey((int) o);
  }

  @Override
  public void setAusrichtung(Ausrichtung ausrichtung) throws RemoteException
  {
    setAttribute("ausrichtung", ausrichtung.getKey());
  }
}
