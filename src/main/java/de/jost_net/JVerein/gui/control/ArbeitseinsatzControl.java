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
import de.jost_net.JVerein.gui.menu.ArbeitseinsatzMenu;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ArbeitseinsatzControl extends FilterControl implements Savable
{
  private ArbeitseinsatzPart part = null;

  private Arbeitseinsatz aeins = null;

  private JVereinTablePart arbeitseinsatzList;

  public ArbeitseinsatzControl(AbstractView view)
  {
    super(view);
  }

  public Arbeitseinsatz getArbeitseinsatz()
  {
    if (aeins != null)
    {
      return aeins;
    }
    aeins = (Arbeitseinsatz) getCurrentObject();
    return aeins;
  }

  public ArbeitseinsatzPart getPart()
  {
    if (part != null)
    {
      return part;
    }
    part = new ArbeitseinsatzPart(getArbeitseinsatz(), true);
    return part;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Arbeitseinsatz ae = getArbeitseinsatz();
    if (ae.isNewObject())
    {
      if (getPart().getMitglied().getValue() != null)
      {
        Mitglied m = (Mitglied) getPart().getMitglied().getValue();
        ae.setMitglied(Integer.parseInt(m.getID()));
      }
      else
      {
        ae.setMitglied(null);
      }
    }
    ae.setDatum((Date) part.getDatum().getValue());
    ae.setStunden((Double) part.getStunden().getValue());
    ae.setBemerkung((String) part.getBemerkung().getValue());
    return ae;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Arbeitseinsatzes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (arbeitseinsatzList != null)
    {
      return arbeitseinsatzList;
    }

    DBIterator<Arbeitseinsatz> arbeitseinsaetze = getArbeitseinsaetzeIt();
    arbeitseinsatzList = new JVereinTablePart(arbeitseinsaetze, null);
    arbeitseinsatzList.setMulti(true);
    arbeitseinsatzList
        .setContextMenu(new ArbeitseinsatzMenu(arbeitseinsatzList));
    arbeitseinsatzList.addColumn("Nr", "id-int");
    arbeitseinsatzList.addColumn("Name", "mitglied");
    arbeitseinsatzList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    arbeitseinsatzList.addColumn("Stunden", "stunden",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    arbeitseinsatzList.addColumn("Bemerkung", "bemerkung");
    arbeitseinsatzList.setAction(
        new EditAction(ArbeitseinsatzDetailView.class, arbeitseinsatzList));
    VorZurueckControl.setObjektListe(null, null);
    return arbeitseinsatzList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (arbeitseinsatzList == null)
      {
        return;
      }
      arbeitseinsatzList.removeAll();
      DBIterator<Arbeitseinsatz> arbeitseinsaetze = getArbeitseinsaetzeIt();
      while (arbeitseinsaetze.hasNext())
      {
        arbeitseinsatzList.addItem(arbeitseinsaetze.next());
      }
      arbeitseinsatzList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Arbeitseinsatz> getArbeitseinsaetzeIt()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Arbeitseinsatz> arbeitseinsaetze = service
        .createList(Arbeitseinsatz.class);
    arbeitseinsaetze.join("mitglied");
    arbeitseinsaetze.addFilter("mitglied.id = arbeitseinsatz.mitglied");

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NAME:
          String suchName = "%" + value.toString().toLowerCase() + "%";
          arbeitseinsaetze.addFilter(
              "(lower(name) like ? " + "or lower(vorname) like ?)", suchName,
              suchName);
          break;
        case DATUM_VON:
          arbeitseinsaetze.addFilter("datum >= ?", value);
          break;
        case DATUM_BIS:
          arbeitseinsaetze.addFilter("datum <= ?", value);
          break;
        case BEMERKUNG:
          arbeitseinsaetze.addFilter("(lower(bemerkung) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    arbeitseinsaetze.setOrder("ORDER by datum desc");
    return arbeitseinsaetze;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ARBEITSEINSAETZE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ARBEITSEINSAETZE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ARBEITSEINSAETZE_DATEINAME, this);
  }

}
