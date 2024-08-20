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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;

public class LastschriftControl extends FilterControl
{


  private TablePart lastschriftList;

  public LastschriftControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }
 
  public Part getLastschriftList() throws RemoteException
  {
    if (lastschriftList != null)
    {
      return lastschriftList;
    }
    lastschriftList = new TablePart(getLastschriften(), null);
    lastschriftList.addColumn("Nr", "id");
    lastschriftList.addColumn("Abrechnungslauf", "abrechnungslauf");
    lastschriftList.addColumn("Name", "name");
    lastschriftList.addColumn("Vorname", "vorname");
    lastschriftList.addColumn("Zweck", "verwendungszweck");
    lastschriftList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    lastschriftList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("IBAN", "iban");
    lastschriftList.addColumn("Mandat", "mandatid");
    lastschriftList.addColumn("Mandatdatum", "mandatdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.setRememberColWidths(true);
    lastschriftList.setContextMenu(new LastschriftMenu());
    lastschriftList.setRememberOrder(true);
    lastschriftList.addFeature(new FeatureSummary());
    lastschriftList.setMulti(true);
    return lastschriftList;
  }

  public void TabRefresh()
  {
    if (lastschriftList == null)
    {
      return;
    }
    try
    {
      lastschriftList.removeAll();
      DBIterator<Lastschrift> lastschriften = getLastschriften();
      while (lastschriften.hasNext())
      {
        lastschriftList.addItem(lastschriften.next());
      }
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }
  
  private  DBIterator<Lastschrift> getLastschriften() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Lastschrift> lastschriften = service
        .createList(Lastschrift.class);
    lastschriften.join("abrechnungslauf");
    lastschriften.addFilter("abrechnungslauf.id = lastschrift.abrechnungslauf");
    if (isMitgliedArtAktiv() && getMitgliedArt().getValue() != null)
    {
      String tmpArt = (String) getMitgliedArt().getValue();
      if (tmpArt.equalsIgnoreCase("Kursteilnehmer"))
      {
        lastschriften.addFilter("(lastschrift.kursteilnehmer IS NOT NULL)");
      }
      else
      {
        lastschriften.join("mitglied");
        lastschriften.addFilter("mitglied.id = lastschrift.mitglied");
        if (tmpArt.equalsIgnoreCase("Mitglied"))
        {
          lastschriften.addFilter("(adresstyp = 1)");
        }
        else if (tmpArt.equalsIgnoreCase("Nicht-Mitglied"))
        {
          lastschriften.addFilter("(adresstyp > 1)");
        }
      }
    }
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        lastschriften.addFilter("(lower(lastschrift.name) like ?)",
            new Object[] { tmpSuchname.toLowerCase() + "%"});
      }
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        lastschriften.addFilter("(lower(verwendungszweck) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%"});
      }
    }
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      lastschriften.addFilter("faelligkeit >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      lastschriften.addFilter("faelligkeit <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    if (isIntegerAuswAktiv() && getIntegerAusw().getValue() != null)
    {
      lastschriften.addFilter("abrechnungslauf >= ?",
          new Object[] { (Integer) getIntegerAusw().getValue() });
    }
    
    lastschriften.setOrder("ORDER BY name");

    return lastschriften;
  }

}
