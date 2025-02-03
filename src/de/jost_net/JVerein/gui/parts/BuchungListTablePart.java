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
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.parts.table.Feature.Context;

public class BuchungListTablePart extends AutoUpdateTablePart
{

  private Double saldo = null;

  public BuchungListTablePart(Action action)
  {
    super(action);
  }

  public BuchungListTablePart(List<Buchung> list, Action action)
  {
    super(list, action);
  }
  
  /**
   * Belegt den Context mit dem anzuzeigenden Text.
   * Ersetzt getSummary() welches deprecated ist.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    if (this.hasEvent(FeatureSummary.class,e))
    {
      double sumBetrag = 0.0;
      String summary = "";
      try
      {
        @SuppressWarnings("rawtypes")
        List l = this.getItems();
        summary = new String(l.size() + " Datensätze");
        for (int i = 0; i < l.size(); i++)
        {
          Buchung b = (Buchung) l.get(i);
          sumBetrag += b.getBetrag();
        }
        summary += " / " + "Gesamtbetrag:" + " "
            + Einstellungen.DECIMALFORMAT.format(sumBetrag) + " "
            + Einstellungen.CURRENCY;
        if (saldo != null)
        {
        summary += " / " + "Kontosaldo:" + " "
            + Einstellungen.DECIMALFORMAT.format(saldo) + " "
            + Einstellungen.CURRENCY;
        }
      }
      catch (RemoteException re)
      {
        // nichts tun
      }
      ctx.addon.put(FeatureSummary.CTX_KEY_TEXT,summary);
    }
    return ctx;
  }

  public void updateSaldo(Konto konto) throws RemoteException
  {
    if (konto != null)
    {
      saldo = konto.getSaldo();
    }
    else
    {
      saldo = null;
    }
  }
}
