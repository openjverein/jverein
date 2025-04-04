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
import de.jost_net.JVerein.io.ISaldoZeile;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.parts.table.Feature.Event;

public class SaldoListTablePart extends TablePart
{

  private Context ctx;

  public SaldoListTablePart(Action action)
  {
    super(action);
  }

  public SaldoListTablePart(ArrayList<?> list, Action action)
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
      StringBuilder summary = new StringBuilder();
      String summaryString = "";
      try
      {
        Object o = getSelection();
        if (o != null && o instanceof ISaldoZeile[])
        {
          boolean hasAnfangsbestand = false;
          boolean hasEinnahmen = false;
          boolean hasAusgaben = false;
          boolean hasUmbuchungen = false;
          boolean hasEndbestand = false;
          Double anfangsbestand = null;
          Double einnahmen = null;
          Double ausgaben = null;
          Double umbuchungen = null;
          Double endbestand = null;
          GenericObject[] zeilen = (GenericObject[]) o;

          // Wir schauen welche Attribute enthalten sind
          if (zeilen.length > 0)
          {
            String[] attributeNames = zeilen[0].getAttributeNames();
            for (String name : attributeNames)
            {
              if (name.equalsIgnoreCase("anfangsbestand"))
              {
                hasAnfangsbestand = true;
              }
              if (name.equalsIgnoreCase("einnahmen"))
              {
                hasEinnahmen = true;
              }
              if (name.equalsIgnoreCase("ausgaben"))
              {
                hasAusgaben = true;
              }
              if (name.equalsIgnoreCase("umbuchungen"))
              {
                hasUmbuchungen = true;
              }
              if (name.equalsIgnoreCase("endbestand"))
              {
                hasEndbestand = true;
              }
            }
          }

          // Werte berechnen
          // Felder ohne Wert (null) werden nicht ausgegeben
          for (int i = 0; i < zeilen.length; i++)
          {
            ISaldoZeile zeile = (ISaldoZeile) zeilen[i];
            if (zeile.getStatus() != ISaldoZeile.DETAIL)
            {
              throw new ApplicationException(zeile.getMessage());
            }
            try
            {
              if (hasAnfangsbestand)
              {
                if (anfangsbestand == null)
                {
                  anfangsbestand = (Double) zeilen[i]
                      .getAttribute("anfangsbestand");
                }
                else
                {
                  anfangsbestand = anfangsbestand
                      + (Double) zeilen[i].getAttribute("anfangsbestand");
                }
              }
            }
            catch (NullPointerException ex)
            {
              // Das Feld hat keinen Eintrag, also ignorieren
            }
            try
            {
              if (hasEinnahmen)
              {
                if (einnahmen == null)
                {
                  einnahmen = (Double) zeilen[i].getAttribute("einnahmen");
                }
                else
                {
                  einnahmen = einnahmen
                      + (Double) zeilen[i].getAttribute("einnahmen");
                }
              }
            }
            catch (NullPointerException ex)
            {
              // Das Feld hat keinen Eintrag, also ignorieren
            }
            try
            {
              if (hasAusgaben)
              {
                if (ausgaben == null)
                {
                  ausgaben = (Double) zeilen[i].getAttribute("ausgaben");
                }
                else
                {
                  ausgaben = ausgaben
                      + (Double) zeilen[i].getAttribute("ausgaben");
                }
              }
            }
            catch (NullPointerException ex)
            {
              // Das Feld hat keinen Eintrag, also ignorieren
            }
            try
            {
              if (hasUmbuchungen)
              {
                if (umbuchungen == null)
                {
                  umbuchungen = (Double) zeilen[i].getAttribute("umbuchungen");
                }
                else
                {
                  umbuchungen = umbuchungen
                      + (Double) zeilen[i].getAttribute("umbuchungen");
                }
              }
            }
            catch (NullPointerException ex)
            {
              // Das Feld hat keinen Eintrag, also ignorieren
            }
            try
            {
              if (hasEndbestand)
              {
                if (endbestand == null)
                {
                  endbestand = (Double) zeilen[i].getAttribute("endbestand");
                }
                else
                {
                  endbestand = endbestand
                      + (Double) zeilen[i].getAttribute("endbestand");
                }
              }
            }
            catch (NullPointerException ex)
            {
              // Das Feld hat keinen Eintrag, also ignorieren
            }
          }

          // String aufbauen
          // Wenn für eine Spalte aller Felder null sind geben wir die Spalte
          // nicht aus.
          // Das ist wegen der Mittelverwendungsaldo Liste, die hat keine
          // Umbuchungen, aber wegen der gleichen Saldozeile wie das
          // Buchungsklassensaldo kommt die Spalte in den Attribut Namen vor.
          // Aber auch sonst gibt es Spalten bei denen in einigen Zeilen
          // keine Werte in der Spalte stehen.
          summary.append("Summe Auswahl:");
          if (hasAnfangsbestand && anfangsbestand != null)
          {
            summary.append(" Anfangsbestand: "
                + Einstellungen.DECIMALFORMAT.format(anfangsbestand) + " "
                + Einstellungen.CURRENCY + ",");
          }
          if (hasEinnahmen && einnahmen != null)
          {
            summary.append(
                " Einnahmen: " + Einstellungen.DECIMALFORMAT.format(einnahmen)
                    + " " + Einstellungen.CURRENCY + ",");
          }
          if (hasAusgaben && ausgaben != null)
          {
            summary.append(
                " Ausgaben: " + Einstellungen.DECIMALFORMAT.format(ausgaben)
                    + " " + Einstellungen.CURRENCY + ",");
          }
          if (hasUmbuchungen && umbuchungen != null)
          {
            summary.append(" Umbuchungen: "
                + Einstellungen.DECIMALFORMAT.format(umbuchungen) + " "
                + Einstellungen.CURRENCY + ",");
          }
          if (hasEndbestand && endbestand != null)
          {
            summary.append(
                " Endbestand: " + Einstellungen.DECIMALFORMAT.format(endbestand)
                    + " " + Einstellungen.CURRENCY);
          }
          summaryString = summary.toString();
          if (summaryString.endsWith(","))
          {
            summaryString = summaryString.substring(0,
                summaryString.length() - 1);
          }
        }
      }
      catch (RemoteException re)
      {
        // nichts tun
      }
      catch (ApplicationException ae)
      {
        summaryString = ae.getMessage();
      }
      ctx.addon.put(FeatureSummary.CTX_KEY_TEXT, summaryString);
    }
    return ctx;
  }
}
