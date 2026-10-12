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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.VorlageMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.EinstellungenVorlageDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Vorlageart;
import de.jost_net.JVerein.rmi.Vorlage;
import de.jost_net.JVerein.server.VorlageImpl;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class VorlageListeControl extends FilterControl
{

  private JVereinTablePart namenList;

  public VorlageListeControl(AbstractView view)
  {
    super(view, VorlageControl.class);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (namenList != null)
    {
      return namenList;
    }
    namenList = new JVereinTablePart(getVorlagenList(), null);
    namenList.addColumn("Vorlage Art", "art");
    namenList.addColumn("Vorlagenmuster", Vorlage.MUSTER);
    namenList.setContextMenu(new VorlageMenu(namenList));
    namenList.setRememberState(true);
    namenList.removeFeature(FeatureSummary.class);
    namenList.setAction(
        new EditAction(EinstellungenVorlageDetailView.class, namenList));
    VorZurueckControl.setObjektListe(null, null);

    return namenList;
  }

  @Override
  protected void TabRefresh()
  {
    try
    {
      if (namenList == null)
      {
        return;
      }
      namenList.removeAll();
      for (Vorlage v : getVorlagenList())
      {
        namenList.addItem(v);
      }
      namenList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private List<Vorlage> getVorlagenList() throws RemoteException
  {
    Map<Filter, Object> filter = getFilter();
    String tmpSuchtext = "";
    if ((String) filter.get(Filter.ENTHALTENER_TEXT) != null)
    {
      tmpSuchtext = ((String) filter.get(Filter.ENTHALTENER_TEXT))
          .toLowerCase();
    }
    Vorlageart art = (Vorlageart) filter.get(Filter.VORLAGEART);

    // Vorhandene Vorlagen aus DB laden
    Map<String, DBObject> vorlagen = new HashMap<>();
    DBIterator<?> it = Einstellungen.getDBService().createList(Vorlage.class);
    while (it.hasNext())
    {
      Vorlage v = (Vorlage) it.next();
      vorlagen.put(v.getKey(), v);
    }

    // Alle möglichen Typen durchlaufen
    ArrayList<Vorlage> list = new ArrayList<>();
    for (VorlageTyp typ : VorlageTyp.values())
    {
      // TODO Nach Belegumstellung reaktivieren
      if (typ.equals(VorlageTyp.BUCHUNG_DOKUMENT_PFAD)
          || typ.equals(VorlageTyp.MITGLIED_DOKUMENT_PFAD))
      {
        continue;
      }
      VorlageImpl vorlage = (VorlageImpl) vorlagen.get(typ.getKey());
      // Wenn es nicht in der DB steht, neu erstellen
      if (vorlage == null)
      {
        vorlage = Einstellungen.getDBService().createObject(Vorlage.class,
            null);
        vorlage.setAttribute(Vorlage.KEY, typ.getKey());
        vorlage.setMuster(typ.getDefault());
        try
        {
          vorlage.store();
        }
        catch (ApplicationException e)
        {
          // kann nicht vorkommen, da es nur von insertCheck geworfen wird, und
          // da ist bei VorlageImpl nichts implementiert
          throw new RemoteException(e.getMessage());
        }
      }
      // ggf. Filtern
      if (art != null && art.getKey() != typ.getArtkey())
      {
        continue;
      }
      if (tmpSuchtext.length() == 0
          || vorlage.getMuster().toLowerCase().contains(tmpSuchtext)
          || vorlage.getKey().toLowerCase().contains(tmpSuchtext))
      {
        list.add(vorlage);
      }
    }
    return list;
  }

  @Override
  protected String getTableTitle()
  {
    return "";
  }

  @Override
  protected String getTableSubtitle()
  {
    return "";
  }

  @Override
  protected String getTableDateiname()
  {
    return "";
  }
}
