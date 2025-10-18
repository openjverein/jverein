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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.keys.Ausrichtung;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularfeldControl extends FormularPartControl implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private TextAreaInput name;

  private IntegerInput seite;

  private DecimalInput x;

  private DecimalInput y;

  private SelectInput font;

  private IntegerInput fontsize;

  private Formularfeld formularfeld;

  private SelectInput ausrichtung;

  public FormularfeldControl(AbstractView view, Formular formular)
  {
    super(view, formular);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Formularfeld getFormularfeld()
  {
    if (formularfeld != null)
    {
      return formularfeld;
    }
    formularfeld = (Formularfeld) getCurrentObject();
    return formularfeld;
  }

  public Formular getFormular()
  {
    return formular;
  }

  public TextAreaInput getName()
      throws RemoteException, NoSuchFieldException, SecurityException
  {
    if (name != null)
    {
      return name;
    }

    name = new TextAreaInput(getFormularfeld().getName(), 1000);
    name.setHeight(200);
    return name;
  }

  public IntegerInput getSeite() throws RemoteException
  {
    if (seite != null)
    {
      return seite;
    }
    seite = new IntegerInput(getFormularfeld().getSeite());
    seite.setComment("Seite");
    return seite;
  }

  public DecimalInput getX() throws RemoteException
  {
    if (x != null)
    {
      return x;
    }
    x = new DecimalInput(getFormularfeld().getX(), Einstellungen.DECIMALFORMAT);
    x.setComment("Millimeter");
    return x;
  }

  public DecimalInput getY() throws RemoteException
  {
    if (y != null)
    {
      return y;
    }
    y = new DecimalInput(getFormularfeld().getY(), Einstellungen.DECIMALFORMAT);
    y.setComment("Millimeter");
    return y;
  }

  public SelectInput getFont() throws RemoteException
  {
    if (font != null)
    {
      return font;
    }
    ArrayList<String> fonts = new ArrayList<>();
    fonts.add("PTSans-Regular");
    fonts.add("PTSans-Bold");
    fonts.add("PTSans-Italic");
    fonts.add("PTSans-BoldItalic");
    fonts.add("FreeSans");
    fonts.add("FreeSans-Bold");
    fonts.add("FreeSans-BoldOblique");
    fonts.add("FreeSans-Oblique");
    fonts.add("Courier Prime");
    fonts.add("Courier Prime Bold");
    fonts.add("Courier Prime Bold Italic");
    fonts.add("Courier Prime Italic");
    fonts.add("LiberationSans-Bold");
    fonts.add("LiberationSans-BoldItalic");
    fonts.add("LiberationSans-Italic");
    fonts.add("LiberationSans-Regular");
    fonts.add("LiberationSerif-Bold");
    fonts.add("LiberationSerif-BoldItalic");
    fonts.add("LiberationSerif-Italic");
    fonts.add("LiberationSerif-Regular");
    font = new SelectInput(fonts, getFormularfeld().getFont());
    return font;
  }

  public IntegerInput getFontsize() throws RemoteException
  {
    if (fontsize != null)
    {
      return fontsize;
    }
    fontsize = new IntegerInput(getFormularfeld().getFontsize());
    return fontsize;
  }

  public SelectInput getAusrichtung() throws RemoteException
  {
    if (ausrichtung != null)
    {
      return ausrichtung;
    }
    ausrichtung = new SelectInput(Ausrichtung.values(),
        getFormularfeld().getAusrichtung());
    return ausrichtung;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Formularfeld f = getFormularfeld();
    try
    {
      f.setFormular(getFormular());
      f.setName((String) getName().getValue());
      f.setSeite((Integer) getSeite().getValue());
      f.setX((Double) getX().getValue());
      f.setY((Double) getY().getValue());
      f.setFont((String) getFont().getValue());
      f.setFontsize((Integer) getFontsize().getValue());
      f.setAusrichtung((Ausrichtung) getAusrichtung().getValue());
    }
    catch (RemoteException e)
    {
      throw new RemoteException(e.getMessage());
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
    return f;
  }

  /**
   * This method stores the project using the current values.
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern des Formularfeldes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Map<String, Object> getMap() throws RemoteException
  {
    Map<String, Object> map = new AllgemeineMap().getMap(null);
    if (formular.getArt() == FormularArt.SPENDENBESCHEINIGUNG
        || formular.getArt() == FormularArt.SAMMELSPENDENBESCHEINIGUNG)
    {
      map = SpendenbescheinigungMap.getDummyMap(map);
      map = MitgliedMap.getDummyMap(map);
      // Dieser Eintrag ist nur in Formularen möglich, daher wird er hier extra
      // hinzugefügt und ist nicht in der DummyMap.
      map.put(SpendenbescheinigungVar.UNTERSCHRIFT.getName(),
          "$" + SpendenbescheinigungVar.UNTERSCHRIFT.getName());
    }
    if (formular.getArt() == FormularArt.FREIESFORMULAR)
    {
      map = MitgliedMap.getDummyMap(map);
    }
    if (formular.getArt() == FormularArt.SEPA_PRENOTIFICATION)
    {
      map = LastschriftMap.getDummyMap(map);
    }
    if (formular.getArt() == FormularArt.RECHNUNG
        || formular.getArt() == FormularArt.MAHNUNG)
    {
      map = MitgliedMap.getDummyMap(map);
      map = RechnungMap.getDummyMap(map);
      // Dieser Eintrag ist nur in Formularen möglich, daher wird er hier extra
      // hinzugefügt und ist nicht in der DummyMap.
      map.put(RechnungVar.QRCODE_SUMME.getName(),
          "$" + RechnungVar.QRCODE_SUMME.getName());
    }
    return map;
  }
}
