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
import de.jost_net.JVerein.server.IBetrag;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;

public class BetragSummaryTablePart extends AutoUpdateTablePart
{

  public BetragSummaryTablePart(Action action)
  {
    super(action);

    // ChangeListener für die Summe der ausgewählten Buchungen anhängen.
    addSelectionListener(e -> featureEvent(
        de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null));
  }

  @SuppressWarnings("rawtypes")
  public BetragSummaryTablePart(GenericIterator it, Action action)
  {
    super(it, action);

    // ChangeListener für die Summe der ausgewählten Buchungen anhängen.
    addSelectionListener(e -> featureEvent(
        de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null));
  }

  public BetragSummaryTablePart(List<?> list, Action action)
  {
    super(list, action);

    // ChangeListener für die Summe der ausgewählten Buchungen anhängen.
    addSelectionListener(e -> featureEvent(
        de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    if (this.hasEvent(FeatureSummary.class, e))
    {
      double sumBetrag = 0d;
      double sumAuswahl = 0d;
      String summary = (String) ctx.addon.get(FeatureSummary.CTX_KEY_TEXT);
      try
      {
        for (IBetrag b : (List<IBetrag>) this.getItems())
        {
          if (b.getBetrag() != null)
          {
            sumBetrag += b.getBetrag();
          }
        }
        summary += " / " + "Gesamtbetrag:" + " "
            + Einstellungen.DECIMALFORMAT.format(sumBetrag) + " "
            + Einstellungen.CURRENCY;

        Object o = this.getSelection();
        if (o != null && o instanceof IBetrag[])
        {
          for (IBetrag i : (IBetrag[]) o)
          {
            sumAuswahl += i.getBetrag();
          }
          summary += " / Summe Auswahl:" + " "
              + Einstellungen.DECIMALFORMAT.format(sumAuswahl) + " "
              + Einstellungen.CURRENCY;
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

}
