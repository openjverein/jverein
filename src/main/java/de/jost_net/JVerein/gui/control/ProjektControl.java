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
import de.jost_net.JVerein.gui.menu.ProjektMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.ProjektDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ProjektControl extends FilterControl implements Savable
{

  private JVereinTablePart projektList;

  private Input bezeichnung;

  private DateInput startDatum;

  private DateInput endeDatum;

  private Projekt projekt;

  public ProjektControl(AbstractView view)
  {
    super(view);
  }

  private Projekt getProjekt()
  {
    if (projekt != null)
    {
      return projekt;
    }
    projekt = (Projekt) getCurrentObject();
    return projekt;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getProjekt().getBezeichnung(), 50);
    bezeichnung.setName("Bezeichnung");
    bezeichnung.setMandatory(true);
    return bezeichnung;
  }

  public Input getStartDatum() throws RemoteException
  {
    if (startDatum != null)
    {
      return startDatum;
    }

    Date d = getProjekt().getStartDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    startDatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    startDatum.setName("Startdatum");
    return startDatum;
  }

  public Input getEndeDatum() throws RemoteException
  {
    if (endeDatum != null)
    {
      return endeDatum;
    }

    Date d = getProjekt().getEndeDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    endeDatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    endeDatum.setName("Endedatum");
    return endeDatum;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Projekt p = getProjekt();
    p.setBezeichnung((String) getBezeichnung().getValue());
    p.setStartDatum((Date) getStartDatum().getValue());
    p.setEndeDatum((Date) getEndeDatum().getValue());
    return p;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Projektes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (projektList != null)
    {
      return projektList;
    }
    projektList = new JVereinTablePart(getProjekte(), null);
    projektList.addColumn("Bezeichnung", "bezeichnung");
    projektList.addColumn("Startdatum", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    projektList.addColumn("Endedatum", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    projektList.setContextMenu(new ProjektMenu(projektList));
    projektList.setMulti(true);
    projektList.setAction(new EditAction(ProjektDetailView.class, projektList));
    VorZurueckControl.setObjektListe(null, null);
    return projektList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (projektList == null)
    {
      return;
    }
    projektList.removeAll();
    try
    {
      DBIterator<Projekt> projekte = getProjekte();
      while (projekte.hasNext())
      {
        projektList.addItem(projekte.next());
      }
      projektList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e1);
    }
  }

  private DBIterator<Projekt> getProjekte()
      throws RemoteException, ApplicationException
  {
    DBIterator<Projekt> projekte = Einstellungen.getDBService()
        .createList(Projekt.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case BEZEICHNUNG:
          projekte.addFilter("(lower(bezeichnung) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case DATUM_START_VON:
          projekte.addFilter("startdatum >= ?", value);
          break;
        case DATUM_START_BIS:
          projekte.addFilter("startdatum <= ?", value);
          break;
        case DATUM_ENDE_VON:
          projekte.addFilter("endedatum >= ?", value);
          break;
        case DATUM_ENDE_BIS:
          projekte.addFilter("endedatum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    projekte.setOrder("ORDER BY bezeichnung");
    return projekte;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTE_DATEINAME, this);
  }
}
