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
package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.BuchungsartAnzeige;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.logging.Logger;

public class BuchungsartInput
{
  public enum buchungsarttyp
  {
    BUCHUNGSART,
    ANLAGENART,
    AFAART,
    STEUERART,
    ALLE
  }

  @SuppressWarnings("unchecked")
  public AbstractInput getBuchungsartInput(Buchungsart bart, buchungsarttyp art,
      int auswahl) throws RemoteException
  {
    AbstractInput buchungsart;
    switch (auswahl)
    {
      case AbstractInputAuswahl.ComboBox:
        DBIterator<Buchungsart> it = getIterator(art);
        List<Buchungsart> list = PseudoIterator.asList(it);
        // Es muss die contains Funktion des Iterators verwendet werden, sonst
        // funktioniert es nicht.
        if (bart != null && it.contains(bart) == null)
        {
          list.add(bart);
        }

        buchungsart = new SelectInput(list, bart);

        switch ((Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGSARTANZEIGE))
        {
          case BuchungsartAnzeige.NUMMER_BEZEICHNUNG:
            ((SelectInput) buchungsart).setAttribute("nrbezeichnung");
            break;
          case BuchungsartAnzeige.BEZEICHNUNG_NUMMER:
            ((SelectInput) buchungsart).setAttribute("bezeichnungnr");
            break;
          default:
            ((SelectInput) buchungsart).setAttribute("bezeichnung");
            break;
        }
        ((SelectInput) buchungsart).setPleaseChoose("Bitte auswählen");
        break;
      case AbstractInputAuswahl.SearchInput:
      default:
        buchungsart = new BuchungsartSearchInput(art);

        switch ((Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGSARTANZEIGE))
        {
          case BuchungsartAnzeige.NUMMER_BEZEICHNUNG:
            ((BuchungsartSearchInput) buchungsart)
                .setAttribute("nrbezeichnung");
            break;
          case BuchungsartAnzeige.BEZEICHNUNG_NUMMER:
            ((BuchungsartSearchInput) buchungsart)
                .setAttribute("bezeichnungnr");
            break;
          default:
            ((BuchungsartSearchInput) buchungsart).setAttribute("bezeichnung");
            break;
        }
        ((BuchungsartSearchInput) buchungsart)
            .setSearchString("Zum Suchen tippen");
    }
    buchungsart.setValue(bart);
    return buchungsart;
  }

  private DBIterator<Buchungsart> getIterator(buchungsarttyp art)
      throws RemoteException
  {
    int unterdrueckunglaenge = (Integer) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGLAENGE);

    DBIterator<Buchungsart> it = Einstellungen.getDBService()
        .createList(Buchungsart.class);

    Calendar cal = Calendar.getInstance();
    Date db = cal.getTime();
    cal.add(Calendar.MONTH, -unterdrueckunglaenge);
    Date dv = cal.getTime();

    if (unterdrueckunglaenge == 0)
    {
      it.addFilter("buchungsart.status != ?", StatusBuchungsart.INACTIVE);
    }
    switch (art)
    {
      case ANLAGENART:
        if (unterdrueckunglaenge > 0)
        {
          it.addFilter("(status = ? OR (status = ? AND id IN "
              + "(SELECT DISTINCT anlagenart FROM konto WHERE anlagenart IS NOT NULL AND "
              + "(aufloesung IS NULL OR (aufloesung >= ? AND aufloesung <= ?)))"
              + "))", StatusBuchungsart.ACTIVE, StatusBuchungsart.AUTO, dv, db);
        }

        it.addFilter("IFNULL(buchungsart.abschreibung, 0) IS FALSE");
        it.addFilter(
            "id NOT IN (SELECT DISTINCT buchungsart from steuer where buchungsart IS NOT NULL)");
        break;
      case AFAART:
        if (unterdrueckunglaenge > 0)
        {
          it.addFilter("(status = ? OR (status = ? AND id IN "
              + "(SELECT DISTINCT afaart FROM konto WHERE afaart IS NOT NULL AND"
              + " (aufloesung IS NULL OR (aufloesung >= ? AND aufloesung <= ?)))"
              + "))", StatusBuchungsart.ACTIVE, StatusBuchungsart.AUTO, dv, db);
        }

        it.addFilter("buchungsart.abschreibung IS TRUE");
        it.addFilter(
            "id NOT IN (SELECT DISTINCT buchungsart from steuer where buchungsart IS NOT NULL)");
        break;
      case BUCHUNGSART:
        if (unterdrueckunglaenge > 0)
        {
          it.addFilter("(status = ? OR (status = ? AND id IN "
              + "(SELECT DISTINCT buchungsart FROM buchung WHERE buchungsart IS NOT NULL AND"
              + " datum >= ? AND datum <= ?)))", StatusBuchungsart.ACTIVE,
              StatusBuchungsart.AUTO, dv, db);
        }
        it.addFilter(
            "id NOT IN (SELECT DISTINCT buchungsart from steuer where buchungsart IS NOT NULL)");
        break;
      case STEUERART:
        it.addFilter("id NOT IN (SELECT DISTINCT buchungsart from buchung"
            + " where (dependencyid IS NULL OR dependencyid != -1) AND buchungsart IS NOT NULL)");
        break;
      case ALLE:
        break;
    }

    if ((Integer) Einstellungen.getEinstellung(
        Property.BUCHUNGSARTSORT) == BuchungsartSort.NACH_NUMMER)
    {
      it.setOrder("ORDER BY nummer");
    }
    else
    {
      it.setOrder("ORDER BY bezeichnung");
    }
    return it;
  }

  private class BuchungsartSearchInput extends SearchInput
  {
    private buchungsarttyp art;

    public BuchungsartSearchInput(buchungsarttyp art)
    {
      this.art = art;
    }

    @Override
    public List<?> startSearch(String text)
    {
      try
      {
        DBIterator<Buchungsart> it = getIterator(art);

        if (text != null)
        {
          text = "%" + text.toUpperCase() + "%";
          it.addFilter(
              "(UPPER(buchungsart.bezeichnung) like ? or buchungsart.nummer like ?) ",
              text, text);
        }
        return it != null ? PseudoIterator.asList(it) : null;
      }
      catch (RemoteException e)
      {
        Logger.error("unable to load buchungsart list", e);
        return null;
      }
    }

  }
}
