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
import de.jost_net.JVerein.gui.control.MittelverwendungSaldoControl;
import de.jost_net.JVerein.gui.control.ProjektSaldoControl;
import de.jost_net.JVerein.gui.dialogs.BuchungDialog;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SaldoSteuerbuchungAction implements Action
{

  private AbstractSaldoControl control;

  public SaldoSteuerbuchungAction(AbstractSaldoControl control)
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

      Integer buchungsartId = o.getInteger(AbstractSaldoControl.BUCHUNGSART_ID);
      if (buchungsartId == null)
      {
        throw new ApplicationException("Keine Buchungsart ausgewählt");
      }

      it.join("steuer");
      if (steuerInBuchung)
      {
        it.addFilter("buchung.steuer = steuer.id");
      }
      else
      {
        it.join("buchungsart as buchungbuchungsart");
        it.addFilter("buchungbuchungsart.id = buchung.buchungsart");
        it.addFilter("buchungbuchungsart.steuer = steuer.id");

      }

      it.addFilter("steuer.buchungsart = ?", buchungsartId);
      filterText.add("Steuer-Buchungsart: "
          + o.getAttribute(AbstractSaldoControl.BUCHUNGSART));
      if (klasseInBuchung)
      {
        Object buchungsklasseId = o
            .getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE_ID);
        if (buchungsklasseId == null)
        {
          it.addFilter("steuer.buchungsklasse is null");
          filterText.add("Steuer ohne Buchungsklasse");
        }
        else
        {
          it.addFilter("steuer.buchungsklasse = ?", buchungsklasseId);
          filterText.add("Steuer - Buchungsklasse: "
              + o.getAttribute(AbstractSaldoControl.BUCHUNGSKLASSE));
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

      if (control instanceof MittelverwendungSaldoControl)
      {
        it.addFilter("konto.kontoart = ?", Kontoart.GELD.getKey());
        filterText.add("Kontoarten: " + Kontoart.GELD.getText());
      }
      else
      {
        it.addFilter("konto.kontoart < ? and konto.kontoart != ?",
            Kontoart.LIMIT.getKey(), Kontoart.ANLAGE.getKey());
        filterText.add("Kontoarten: " + Kontoart.GELD.getText() + ", "
            + Kontoart.SCHULDEN.getText());
      }

      BuchungDialog dialog = new BuchungDialog(BuchungDialog.POSITION_CENTER,
          PseudoIterator.asList(it), filterText);

      dialog.open();
    }
    catch (

    ApplicationException e)
    {
      throw e;
    }
    catch (

    Exception e)
    {
      Logger.error("Serverfehler", e);
      throw new ApplicationException("Serverfehler", e);
    }
  }

}
