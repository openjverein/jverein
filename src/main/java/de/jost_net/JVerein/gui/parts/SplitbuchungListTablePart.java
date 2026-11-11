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
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.parts.table.Feature.Context;

public class SplitbuchungListTablePart extends JVereinTablePart
{

  public SplitbuchungListTablePart(List<Buchung> list, Action action)
  {
    super(list, action);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    if (this.hasEvent(FeatureSummary.class, e))
    {
      String summary = (String) ctx.addon.get(FeatureSummary.CTX_KEY_TEXT);

      try
      {
        BigDecimal summe = BigDecimal.ZERO;
        for (Buchung b : (List<Buchung>) this.getItems())
        {
          if (!b.isToDelete() && (b.getSplitTyp() == null
              || b.getSplitTyp() != SplitbuchungTyp.HAUPT))
          {
            summe = summe.add(b.getBetrag());
          }
        }
        summary += " / Differenz:" + " "
            + Einstellungen.BIGDECIMALFORMAT.format(summe) + " "
            + Einstellungen.CURRENCY;
        ctx.addon.put(FeatureSummary.CTX_KEY_TEXT, summary);
      }
      catch (RemoteException re)
      {
        // nichts tun
      }
    }
    return ctx;
  }

}
