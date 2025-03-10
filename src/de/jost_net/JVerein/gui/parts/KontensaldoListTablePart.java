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
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.SaldoZeile;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.parts.table.Feature.Event;

public class KontensaldoListTablePart extends TablePart
{

  private Context ctx;

  public KontensaldoListTablePart(Action action)
  {
    super(action);
  }

  public KontensaldoListTablePart(ArrayList<SaldoZeile> list, Action action)
  {
    super(list, action);

    // ChangeListener für die Summe der ausgewählten Konten
    addSelectionListener(e -> {
      createFeatureEventContext(Event.REFRESH, ctx);
      Feature feature = this.getFeature(FeatureSummary.class);
      if (feature != null)
      {
        feature.handleEvent(Event.REFRESH, ctx);
      }
    });
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
      String summary = "";
      try
      {
        Object o = getSelection();
        if (o != null && o instanceof SaldoZeile[])
        {
          Double anfangsbestand = Double.valueOf(0d);
          Double einnahmen = Double.valueOf(0d);
          Double ausgaben = Double.valueOf(0d);
          Double umbuchungen = Double.valueOf(0d);
          Double endbestand = Double.valueOf(0d);
          SaldoZeile[] zeilen = (SaldoZeile[]) o;
          for (int i = 0; i < zeilen.length; i++)
          {
            anfangsbestand = anfangsbestand
                + (Double) zeilen[i].getAttribute("anfangsbestand");
            einnahmen = einnahmen
                + (Double) zeilen[i].getAttribute("einnahmen");
            ausgaben = ausgaben + (Double) zeilen[i].getAttribute("ausgaben");
            umbuchungen = umbuchungen
                + (Double) zeilen[i].getAttribute("umbuchungen");
            endbestand = endbestand
                + (Double) zeilen[i].getAttribute("endbestand");
          }
          summary += "Summe Auswahl: Anfangsbestand: "
              + Einstellungen.DECIMALFORMAT.format(anfangsbestand) + " "
              + Einstellungen.CURRENCY + ", Einnahmen: "
              + Einstellungen.DECIMALFORMAT.format(einnahmen) + " "
              + Einstellungen.CURRENCY + ", Ausgaben: "
              + Einstellungen.DECIMALFORMAT.format(ausgaben) + " "
              + Einstellungen.CURRENCY + ", Umbuchungen: "
              + Einstellungen.DECIMALFORMAT.format(umbuchungen) + " "
              + Einstellungen.CURRENCY + ", Endbestand: "
              + Einstellungen.DECIMALFORMAT.format(endbestand) + " "
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
