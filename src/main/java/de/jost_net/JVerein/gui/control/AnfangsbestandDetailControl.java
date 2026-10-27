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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnfangsbestandDetailControl extends VorZurueckControl
    implements Savable
{

  private TextInput konto;

  private DateInput datum;

  private BigDecimalInput betrag;

  private Anfangsbestand anfangsbestand;

  private boolean editable = false;

  public AnfangsbestandDetailControl(AbstractView view)
  {
    super(view);
  }

  private Anfangsbestand getAnfangsbestand()
  {
    if (anfangsbestand != null)
    {
      return anfangsbestand;
    }
    anfangsbestand = (Anfangsbestand) getCurrentObject();
    return anfangsbestand;
  }

  public boolean isAnfangsbestandEditable() throws RemoteException
  {
    if (getAnfangsbestand().getJahresabschluss() != null)
    {
      GUI.getStatusBar()
          .setErrorText("Anfangsbestand ist bereits abgeschlossen.");
      return editable = false;
    }
    return editable = true;
  }

  public TextInput getKonto() throws RemoteException
  {
    if (konto != null)
    {
      return konto;
    }
    konto = new TextInput(getAnfangsbestand().getKonto().getNummer(), 35);
    konto.setEnabled(false);
    return konto;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    datum = new DateInput(getAnfangsbestand().getDatum());
    datum.focus();
    if (!getAnfangsbestand().isNewObject())
    {
      datum.setEnabled(false);
    }
    datum.setMandatory(true);
    return datum;
  }

  public BigDecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new BigDecimalInput(getAnfangsbestand().getBetrag(),
        Einstellungen.BIGDECIMALFORMAT);
    betrag.setMandatory(true);
    betrag.setEnabled(editable);
    return betrag;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Anfangsbestand a = getAnfangsbestand();
    // Konto ist schon gesetzt und ist nicht editierbar
    a.setDatum((Date) getDatum().getValue());
    a.setBetrag((BigDecimal) getBetrag().getValue());
    return a;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
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
      String fehler = "Fehler bei speichern des Anfangsbestandes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
