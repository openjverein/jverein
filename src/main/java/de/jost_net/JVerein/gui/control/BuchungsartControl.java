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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.BuchungsartMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.NaturalOrderColumn;
import de.jost_net.JVerein.gui.view.BuchungsartDetailView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsartControl extends FilterControl
{
  private JVereinTablePart buchungsartList;

  public BuchungsartControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (buchungsartList != null)
    {
      return buchungsartList;
    }
    buchungsartList = new JVereinTablePart(getBuchungsarten(), null);
    buchungsartList.addColumn(getGesperrtColumn(), false);
    buchungsartList.addColumn(new NaturalOrderColumn("nummer", "Nummer"));
    buchungsartList.addColumn("Bezeichnung", "bezeichnung");
    buchungsartList.addColumn("Art", "art", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        if (o instanceof Integer)
        {
          return ArtBuchungsart.get((Integer) o);
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
    buchungsartList.addColumn("Buchungsklasse", "buchungsklasse",
        new BuchungsklasseFormatter());
    buchungsartList.addColumn("Spende", "spende", new JaNeinFormatter());
    buchungsartList.addColumn("Abschreibung", "abschreibung",
        new JaNeinFormatter(), false, Column.ALIGN_RIGHT);
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      buchungsartList.addColumn("Steuer", "steuer", o -> {
        if (o == null)
        {
          return "";
        }
        try
        {
          return ((Steuer) o).getName();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return "";
      }, false, Column.ALIGN_RIGHT);
    }
    buchungsartList.addColumn("Status", "status", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        if (o instanceof Integer)
        {
          return StatusBuchungsart.get((Integer) o);
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
    buchungsartList.addColumn("Suchtext", "suchbegriff");
    buchungsartList.setContextMenu(new BuchungsartMenu(buchungsartList));
    buchungsartList.setMulti(true);
    buchungsartList.setRememberState(true);
    buchungsartList.setAction(
        new EditAction(BuchungsartDetailView.class, buchungsartList));
    VorZurueckControl.setObjektListe(null, null);
    return buchungsartList;
  }

  private DBIterator<Buchungsart> getBuchungsarten()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Buchungsart> buchungsarten = service
        .createList(Buchungsart.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case OHNE_DEAKTIVIERT:
          if ((Boolean) value)
          {
            buchungsarten.addFilter("status != ?", StatusBuchungsart.INACTIVE);
          }
          break;
        case NUMMER:
          buchungsarten.addFilter("nummer like ?",
              "%" + value.toString().toUpperCase() + "%");
          break;
        case BEZEICHNUNG:
          buchungsarten.addFilter("UPPER(bezeichnung) like ?",
              "%" + value.toString().toUpperCase() + "%");
          break;
        case BUCHUNGSARTART:
          buchungsarten.addFilter("art = ?", ((ArtBuchungsart) value).getKey());
          break;
        case BUCHUNGSKLASSE:
          buchungsarten.addFilter("buchungsklasse = ?",
              ((Buchungsklasse) value).getID());
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    buchungsarten.setOrder("ORDER BY nummer");
    return buchungsarten;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (buchungsartList != null)
      {
        buchungsartList.removeAll();
        DBIterator<Buchungsart> buchungsarten = getBuchungsarten();
        while (buchungsarten.hasNext())
        {
          buchungsartList.addItem(buchungsarten.next());
        }
        buchungsartList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_DATEINAME, this);
  }
}
