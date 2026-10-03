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
package de.jost_net.JVerein.gui.control.listener;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.util.IbanUtil;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Sucht das Geldinstitut zur eingegebenen IBAN und zeigt es als Kommentar
 * hinter dem Feld an. -Prüft die IBAN -Ermittelt die BIC
 */

public class IBANListener implements Listener
{
  private TextInput iban;

  private TextInput bic;

  public IBANListener(IBANInput iban, TextInput bic)
  {
    this.iban = iban;
    this.bic = bic;
  }

  @Override
  public void handleEvent(Event event)
  {
    if (event == null)
    {
      return;
    }
    if (event.type != SWT.FocusOut)
    {
      return;
    }
    // Wurde eine alte Bankverbindung mit BLZ und Kontonummer eingegeben?
    checkAlteBankverbindung();

    String ib = (String) iban.getValue();
    if (ib == null)
    {
      return;
    }
    String ib2 = ib.trim().replace(" ", "");
    iban.setValue(new IBANFormatter().format(ib2));
    if (ib2.length() == 0)
    {
      iban.setComment("");
      bic.setValue("");
      bic.setComment("");
    }
    if (ib2.length() > 4)
    {
      try
      {
        String ibanGeprueft = IbanUtil.checkIban(ib2);
        String bicErmittelt = IbanUtil.getBicFuerIban(ibanGeprueft);
        String bankname = IbanUtil.getBankname(bicErmittelt);
        if (bankname != null)
        {
          iban.setComment(bankname);
          bic.setValue(bicErmittelt);
          bic.setComment(bankname);
        }
        return;
      }
      catch (ApplicationException e)
      {
        iban.setComment(e.getMessage());
        return;
      }
    }
    String bankname = IbanUtil.getBankname((String) iban.getValue());
    iban.setComment(bankname != null ? bankname : "");
  }

  private void checkAlteBankverbindung()
  {
    String ib = (String) iban.getValue();
    if (ib.length() < 10)
    {
      return; // Wert zu kurz
    }
    for (int i = 0; i > 8; i++)
    {
      if (ib.charAt(i) < '0' || ib.charAt(i) > '9')
      {
        return;
      }
    }
    if (ib.charAt(8) != ' ')
    {
      return;
    }
    String blz = ib.substring(0, 8);
    String konto = ib.substring(9, ib.length());
    try
    {
      IbanUtil.IbanUndBic ibankonv = IbanUtil.vonBlzUndKonto(blz, konto,
          (String) Einstellungen.getEinstellung(Property.DEFAULTLAND));
      iban.setValue(ibankonv.getIban());
      bic.setValue(ibankonv.getBic());
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    catch (ApplicationException e)
    {
      Logger.error("Fehler", e);
    }
  }
}
