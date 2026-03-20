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
package de.jost_net.JVerein.gui.action;

import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.AnlagenlisteControl;
import de.jost_net.JVerein.gui.control.BuchungsklasseSaldoControl;
import de.jost_net.JVerein.gui.control.KontensaldoControl;
import de.jost_net.JVerein.gui.control.MittelverwendungSaldoControl;
import de.jost_net.JVerein.gui.control.ProjektSaldoControl;
import de.jost_net.JVerein.gui.control.UmsatzsteuerSaldoControl;
import de.jost_net.JVerein.gui.dialogs.BuchungDialog;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SaldoBuchungAction implements Action
{

  private AbstractSaldoControl control;

  public SaldoBuchungAction(AbstractSaldoControl control)
  {
    this.control = control;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      ArrayList<String> filterText = new ArrayList<>();
      filterText.add(new JVDateFormatTTMMJJJJ()
          .format(control.getDatumvon().getDate()) + " bis "
          + new JVDateFormatTTMMJJJJ().format(control.getDatumbis().getDate()));

      boolean klasseInBuchung = (boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
      boolean steuerInBuchung = (boolean) Einstellungen
          .getEinstellung(Property.STEUERINBUCHUNG);

      PseudoDBObject o = (PseudoDBObject) context;
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("buchung.datum between ? and ?",
          control.getDatumvon().getDate(), control.getDatumbis().getDate());

      if (control instanceof BuchungsklasseSaldoControl)
      {
        Integer buchungsartId = o
            .getInteger(AbstractSaldoControl.BUCHUNGSART_ID);
        if (buchungsartId == null)
        {
          throw new ApplicationException("Keine Buchungsart ausgewählt");
        }
        // -1 bedeutet ohne Buchungsart
        if (buchungsartId == -1)
        {
          it.addFilter("buchung.buchungsart is null");
          filterText.add("ohne Buchungsart");
        }
        else
        {
          it.addFilter("buchung.buchungsart = ?", buchungsartId);
          filterText.add("Buchungsart: "
              + o.getAttribute(AbstractSaldoControl.BUCHUNGSART));
          if (klasseInBuchung)
          {
            Object buchungsklasseId = o
                .getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_ID);
            if (buchungsklasseId == null)
            {
              it.addFilter("buchung.buchungsklasse is null");
              filterText.add("ohne Buchungsklasse");
            }
            else
            {
              it.addFilter("buchung.buchungsklasse = ?", buchungsklasseId);
              filterText.add("Buchungsklasse: "
                  + o.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE));
            }
          }
        }

        if (control instanceof ProjektSaldoControl)
        {
          it.addFilter("buchung.projekt = ?",
              o.getAttribute(AbstractSaldoControl.PROJEKT_ID));
          filterText
              .add("Projekt: " + o.getAttribute(AbstractSaldoControl.PROJEKT));
        }

        it.join("konto");
        it.addFilter("konto.id = buchung.konto");
        it.addFilter("konto.kontoart < ?", Kontoart.LIMIT.getKey());

        if (control instanceof MittelverwendungSaldoControl)
        {
          // Bei der Mittelverwendung verwenden wir nur Geldkonten und
          // zweckfremde Anlagen.

          switch (o.getInteger(MittelverwendungSaldoControl.ARTBUCHUNGSART))
          {
            // (Ausgaben Geldkonto+zweckfremde Anlagen) - (Umbuchungen
            // Schuldenen+gebundene Anlagen)>0

            // (Einnamen Geldkonto+zweckfremde Anlagen) - (Umbuchungen
            // Schuldenen+gebundene Anlagen)>0
            case ArtBuchungsart.AUSGABE:
            case ArtBuchungsart.EINNAHME:
              it.addFilter(
                  "(konto.kontoart = ? OR (konto.kontoart = ? and konto.zweck = ?))",
                  Kontoart.GELD.getKey(), Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey());
              filterText.add("Kontoarten: " + Kontoart.GELD.getText() + ", "
                  + Kontoart.ANLAGE.getText() + " (nur "
                  + Anlagenzweck.ZWECKFREMD_EINGESETZT.getText() + ")");
              break;
            case ArtBuchungsart.UMBUCHUNG:
              it.addFilter(
                  "(konto.kontoart = ? OR (konto.kontoart = ? and konto.zweck = ?))",
                  Kontoart.SCHULDEN.getKey(), Kontoart.ANLAGE.getKey(),
                  Anlagenzweck.NUTZUNGSGEBUNDEN.getKey());
              filterText.add("Kontoarten: " + Kontoart.SCHULDEN.getText() + ", "
                  + Kontoart.ANLAGE.getText() + " (nur "
                  + Anlagenzweck.NUTZUNGSGEBUNDEN.getText() + ")");
              filterText.add("ACHTUNG Beträge haben das falsche Vorzeichen!");
              break;
          }
        }
        else
        {
          filterText.add("Kontoarten: " + Kontoart.GELD.getText() + ", "
              + Kontoart.ANLAGE.getText() + ", " + Kontoart.SCHULDEN.getText());
        }

      }

      else if (control instanceof KontensaldoControl
          || control instanceof AnlagenlisteControl)
      {
        Object kontoId = o.getAttribute(AbstractSaldoControl.KONTO_ID);
        if (kontoId == null)
        {
          throw new ApplicationException("Kein Konto ausgewählt");
        }
        it.addFilter("buchung.konto = ?", kontoId);
        filterText.add("Konto: " + o.getAttribute(AbstractSaldoControl.KONTO));
      }

      else if (control instanceof UmsatzsteuerSaldoControl)
      {
        Integer steuerID = o.getInteger(UmsatzsteuerSaldoControl.STEUER_ID);
        if (steuerID == null)
        {
          throw new ApplicationException("Keine Steuer ausgewählt");
        }
        else if (steuerID == -1)
        {
          // Steuerfreie Buchungen nur Einnahmen
          it.addFilter("buchungsart.art = ?", ArtBuchungsart.EINNAHME);
          if (steuerInBuchung)
          {
            it.addFilter("buchung.steuer is NULL");
          }
          else
          {
            it.addFilter("buchungsart.steuer is NULL");
          }
          filterText.add("Steuerfreie Buchungen (Einnahmen)");
        }
        else
        {
          if (steuerInBuchung)
          {
            it.addFilter("buchung.steuer = ?", steuerID);
          }
          else
          {
            it.addFilter("buchungsart.steuer = ?", steuerID);
          }
          filterText.add(
              "Steuer: " + o.getAttribute(UmsatzsteuerSaldoControl.STEUER));
        }
      }
      else
      {
        throw new ApplicationException("Kann Buchungen nicht anzeigen");
      }

      BuchungDialog dialog = new BuchungDialog(BuchungDialog.POSITION_CENTER,
          PseudoIterator.asList(it), filterText);

      dialog.open();
    }
    catch (ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }

}
