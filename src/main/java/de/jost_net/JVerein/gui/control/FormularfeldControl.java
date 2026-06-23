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
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.gui.input.FontInput;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.Ausrichtung;
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
  }

  private Formularfeld getFormularfeld()
  {
    if (formularfeld != null)
    {
      return formularfeld;
    }
    formularfeld = (Formularfeld) getCurrentObject();
    return formularfeld;
  }

  private Formular getFormular()
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
    font = new FontInput(getFormularfeld().getFont());
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
    switch (formular.getArt())
    {
      case SPENDENBESCHEINIGUNG:
      case SAMMELSPENDENBESCHEINIGUNG:
      case SACHSPENDENBESCHEINIGUNG:
        map = SpendenbescheinigungMap.getDummyMap(map);
        map = MitgliedMap.getDummyMap(map);

        // Dieser Eintrag ist nur in Formularen möglich, daher wird er hier
        // extra hinzugefügt und ist nicht in der DummyMap.
        map.put(SpendenbescheinigungVar.UNTERSCHRIFT.getName(),
            "$" + SpendenbescheinigungVar.UNTERSCHRIFT.getName());
        break;
      case FREIESFORMULAR:
        map = MitgliedMap.getDummyMap(map);
        break;
      case SEPA_PRENOTIFICATION:
        map = MitgliedMap.getDummyMap(map);
        map = LastschriftMap.getDummyMap(map);
        break;
      case RECHNUNG:
      case MAHNUNG:
        map = MitgliedMap.getDummyMap(map);
        map = RechnungMap.getDummyMap(map);

        // Dieser Eintrag ist nur in Formularen möglich, daher wird er hier
        // extra hinzugefügt und ist nicht in der DummyMap.
        map.put(RechnungVar.QRCODE_SUMME.getName(),
            "$" + RechnungVar.QRCODE_SUMME.getName());
        break;
      case HINTERGRUND:
        break;
    }
    return map;
  }

  @Override
  protected JVereinTablePart getTablePart() throws RemoteException
  {
    // Es gitb keine FormularfeldListe View
    return null;
  }

  @Override
  protected String getTableTitle()
  {
    // Es gitb keine FormularfeldListe View
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    // Es gitb keine FormularfeldListe View
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    // Es gitb keine FormularfeldListe View
    return null;
  }
}
