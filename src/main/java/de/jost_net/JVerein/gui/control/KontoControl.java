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
import java.util.Date;
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.menu.KontoMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.NaturalOrderColumn;
import de.jost_net.JVerein.gui.view.KontoDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KontoControl extends FilterControl
{

  private JVereinTablePart kontenList;

  public KontoControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (kontenList != null)
    {
      return kontenList;
    }

    kontenList = new JVereinTablePart(getKonten(), null);
    kontenList.addColumn(getGesperrtColumn(), false);
    kontenList.addColumn(new NaturalOrderColumn("nummer", "Nummer"));
    kontenList.addColumn("Bezeichnung", "bezeichnung");
    kontenList.addColumn("Kontoart", "kontoart", new Formatter()
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
          return Kontoart.getByKey((Integer) o).getText();
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
    kontenList.addColumn("Hibiscus-Konto", "hibiscusid", new Formatter()
    {

      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "nein";
        }
        if (o instanceof Integer)
        {
          Integer hibid = (Integer) o;
          if (hibid.intValue() >= 0)
          {
            return "ja";
          }
        }
        return "nein";
      }
    }, false, Column.ALIGN_LEFT);
    kontenList.addColumn("Eröffnungsdatum", "eroeffnung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Auflösungsdatum", "aufloesung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kontenList.addColumn("Gegenbuchung-Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    kontenList.setContextMenu(new KontoMenu(kontenList));
    kontenList.setMulti(true);
    kontenList.setAction(new EditAction(KontoDetailView.class, kontenList));
    VorZurueckControl.setObjektListe(null, null);
    return kontenList;
  }

  public void refreshTable() throws ApplicationException
  {
    TabRefresh();
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (kontenList == null)
    {
      return;
    }
    try
    {
      kontenList.removeAll();
      DBIterator<Konto> konten = getKonten();
      while (konten.hasNext())
      {
        kontenList.addItem(konten.next());
      }
      kontenList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Konto> getKonten()
      throws RemoteException, ApplicationException
  {
    DBIterator<Konto> konten = Einstellungen.getDBService()
        .createList(Konto.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case KONTOART:
          konten.addFilter("kontoart = ?", ((Kontoart) value).getKey());
          break;
        case NUR_AKTIVE_KONTEN:
          if ((boolean) value)
          {
            konten.addFilter("(aufloesung IS NULL OR aufloesung > ?)",
                new Date());
          }
          break;
        case BEZEICHNUNG:
          konten.addFilter("(lower(bezeichnung) like ?)",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        case NUMMER:
          konten.addFilter("(lower(nummer) like ?)",
              "%" + ((String) value).toLowerCase() + "%");
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    konten.setOrder("ORDER BY nummer");
    return konten;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.KONTEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.KONTEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.KONTEN_DATEINAME, this);
  }
}
