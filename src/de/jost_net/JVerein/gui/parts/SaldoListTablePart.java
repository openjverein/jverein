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
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.AnlagenlisteControl;
import de.jost_net.JVerein.gui.control.UmsatzsteuerSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
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
        if (o != null && o instanceof PseudoDBObject[])
        {
          Double anfangsbestand = null;
          Double einnahmen = null;
          Double ausgaben = null;
          Double umbuchungen = null;
          Double endbestand = null;
          Double steuer = null;
          Double steuerSumme = null;

          Double anschaffung = null;
          Double startwert = null;
          Double zugang = null;
          Double afa = null;
          Double abgang = null;
          Double endwert = null;

          Integer anzahl = null;

          PseudoDBObject[] zeilen = (PseudoDBObject[]) o;

          // Werte berechnen
          // Felder ohne Wert (null) werden nicht ausgegeben
          for (int i = 0; i < zeilen.length; i++)
          {
            PseudoDBObject zeile = (PseudoDBObject) zeilen[i];
            try
            {
              if (zeile.getInteger(
                  AbstractSaldoControl.ART) != AbstractSaldoControl.ART_DETAIL)
              {
                throw new ApplicationException(
                    "Summe kann nur für Detail Zeilen berechnet werden");
              }
            }
            catch (NullPointerException ex)
            {
              throw new ApplicationException(
                  "Summe kann nur für Detail Zeilen berechnet werden");
            }
            try
            {
              anfangsbestand = (anfangsbestand == null ? 0d : anfangsbestand)
                  + zeile.getDouble(AbstractSaldoControl.ANFANGSBESTAND);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              einnahmen = (einnahmen == null ? 0d : einnahmen)
                  + zeile.getDouble(AbstractSaldoControl.EINNAHMEN);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              ausgaben = (ausgaben == null ? 0d : ausgaben)
                  + zeile.getDouble(AbstractSaldoControl.AUSGABEN);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              umbuchungen = (umbuchungen == null ? 0d : umbuchungen)
                  + zeile.getDouble(AbstractSaldoControl.UMBUCHUNGEN);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              endbestand = (endbestand == null ? 0d : endbestand)
                  + zeile.getDouble(AbstractSaldoControl.ENDBESTAND);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              steuer = (steuer == null ? 0d : steuer)
                  + zeile.getDouble(UmsatzsteuerSaldoControl.STEUERBETRAG);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              steuerSumme = (steuerSumme == null ? 0d : steuerSumme)
                  + zeile.getDouble(UmsatzsteuerSaldoControl.SUMME);
            }
            catch (NullPointerException ignore)
            {
            }

            try
            {
              anschaffung = (anschaffung == null ? 0d : anschaffung)
                  + zeile.getDouble(AnlagenlisteControl.BETRAG);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              startwert = (startwert == null ? 0d : startwert)
                  + zeile.getDouble(AnlagenlisteControl.STARTWERT);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              zugang = (zugang == null ? 0d : zugang)
                  + zeile.getDouble(AnlagenlisteControl.ZUGANG);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              afa = (afa == null ? 0d : afa)
                  + zeile.getDouble(AnlagenlisteControl.ABSCHREIBUNG);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              abgang = (abgang == null ? 0d : abgang)
                  + zeile.getDouble(AnlagenlisteControl.ABGANG);
            }
            catch (NullPointerException ignore)
            {
            }
            try
            {
              endwert = (endwert == null ? 0d : endwert)
                  + zeile.getDouble(AnlagenlisteControl.ENDWERT);
            }
            catch (NullPointerException ignore)
            {
            }

            try
            {
              anzahl = (anzahl == null ? 0 : anzahl)
                  + zeile.getInteger(AbstractSaldoControl.ANZAHL);
            }
            catch (NullPointerException ignore)
            {
            }
          }

          // String aufbauen
          // Wenn für eine Spalte aller Felder null sind geben wir die Spalte
          // nicht aus.
          summary.append("Summe Auswahl:");
          if (anfangsbestand != null)
          {
            summary.append(" Anfangsbestand: "
                + Einstellungen.DECIMALFORMAT.format(anfangsbestand) + " "
                + Einstellungen.CURRENCY + ",");
          }
          if (einnahmen != null)
          {
            summary.append(
                " Einnahmen: " + Einstellungen.DECIMALFORMAT.format(einnahmen)
                    + " " + Einstellungen.CURRENCY + ",");
          }
          if (ausgaben != null)
          {
            summary.append(
                " Ausgaben: " + Einstellungen.DECIMALFORMAT.format(ausgaben)
                    + " " + Einstellungen.CURRENCY + ",");
          }
          if (umbuchungen != null)
          {
            summary.append(" Umbuchungen: "
                + Einstellungen.DECIMALFORMAT.format(umbuchungen) + " "
                + Einstellungen.CURRENCY + ",");
          }
          if (endbestand != null)
          {
            summary.append(
                " Endbestand: " + Einstellungen.DECIMALFORMAT.format(endbestand)
                    + " " + Einstellungen.CURRENCY);
          }
          if (steuerSumme != null)
          {
            summary.append(" Bemessungsgrundlage: "
                + Einstellungen.DECIMALFORMAT.format(steuerSumme) + " "
                + Einstellungen.CURRENCY);
          }
          if (steuer != null)
          {
            summary
                .append(" Steuer: " + Einstellungen.DECIMALFORMAT.format(steuer)
                    + " " + Einstellungen.CURRENCY);
          }

          if (anschaffung != null)
          {
            summary.append(" Anschaffung: "
                + Einstellungen.DECIMALFORMAT.format(anschaffung) + " "
                + Einstellungen.CURRENCY);
          }
          if (startwert != null)
          {
            summary.append(
                " Startwert: " + Einstellungen.DECIMALFORMAT.format(startwert)
                    + " " + Einstellungen.CURRENCY);
          }
          if (zugang != null)
          {
            summary
                .append(" Zugang: " + Einstellungen.DECIMALFORMAT.format(zugang)
                    + " " + Einstellungen.CURRENCY);
          }
          if (afa != null)
          {
            summary.append(
                " Abschreibung: " + Einstellungen.DECIMALFORMAT.format(afa)
                    + " " + Einstellungen.CURRENCY);
          }
          if (abgang != null)
          {
            summary
                .append(" Abgang: " + Einstellungen.DECIMALFORMAT.format(abgang)
                    + " " + Einstellungen.CURRENCY);
          }
          if (endwert != null)
          {
            summary.append(
                " Endwert: " + Einstellungen.DECIMALFORMAT.format(endwert) + " "
                    + Einstellungen.CURRENCY);
          }

          if (anzahl != null)
          {
            summary.append(" Anzahl: " + anzahl);
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
