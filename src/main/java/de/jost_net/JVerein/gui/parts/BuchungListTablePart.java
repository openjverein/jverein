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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.parts.table.Feature.Context;

public class BuchungListTablePart extends AutoUpdateTablePart
{

  private BigDecimal saldo = null;

  private Context ctx;

  public BuchungListTablePart(Action action)
  {
    super(action);
  }

  public BuchungListTablePart(List<Buchung> list, Action action)
  {
    super(list, action);

    // ChangeListener für die Summe der ausgewählten Buchungen anhängen.
    addSelectionListener(e -> featureEvent(
        de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null));
  }

  /**
   * Belegt den Context mit dem anzuzeigenden Text. Ersetzt getSummary() welches
   * deprecated ist.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    ctx = super.createFeatureEventContext(e, data);
    if (this.hasEvent(FeatureSummary.class, e))
    {
      BigDecimal sumBetrag = BigDecimal.ZERO;
      BigDecimal sumNetto = BigDecimal.ZERO;
      String summary = (String) ctx.addon.get(FeatureSummary.CTX_KEY_TEXT);
      try
      {
        @SuppressWarnings("rawtypes")
        List l = this.getItems();
        for (int i = 0; i < l.size(); i++)
        {
          Buchung b = (Buchung) l.get(i);
          sumBetrag = sumBetrag.add(b.getBetrag());
          sumNetto = sumNetto.add(b.getNetto());
        }
        summary += ", Gesamtbetrag: "
            + Einstellungen.BIGDECIMALFORMAT.format(sumBetrag) + " "
            + Einstellungen.CURRENCY;
        if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
        {
          summary += " , Gesamtnetto: "
              + Einstellungen.BIGDECIMALFORMAT.format(sumNetto) + " "
              + Einstellungen.CURRENCY;
        }
        if (saldo != null)
        {
          summary += " , Kontosaldo: "
              + Einstellungen.BIGDECIMALFORMAT.format(saldo) + " "
              + Einstellungen.CURRENCY;
        }
        Object o = getSelection();
        if (o != null && o instanceof Buchung[])
        {
          BigDecimal summe = BigDecimal.ZERO;
          BigDecimal netto = BigDecimal.ZERO;
          for (Buchung b : (Buchung[]) o)
          {
            summe = summe.add(b.getBetrag());
            netto = netto.add(b.getNetto());
          }
          summary += " ==> Summe Auswahl: "
              + Einstellungen.BIGDECIMALFORMAT.format(summe) + " "
              + Einstellungen.CURRENCY;
          if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
          {
            summary += " , Auswahl Netto: "
                + Einstellungen.BIGDECIMALFORMAT.format(netto) + " "
                + Einstellungen.CURRENCY;
          }
        }
      }
      catch (RemoteException re)
      {
        // nichts tun
      }
      ctx.addon.put(FeatureSummary.CTX_KEY_TEXT, summary);
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
