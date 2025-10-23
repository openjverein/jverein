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
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Vorlage;
import de.jost_net.JVerein.server.VorlageImpl;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class VorlageControl extends FilterControl implements Savable
{

  private JVereinTablePart namenList;

  private Vorlage vorlage;

  private Input muster;

  private Input vorschau;

  public VorlageControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Vorlage getVorlage() throws RemoteException, ApplicationException
  {
    if (vorlage != null)
    {
      return vorlage;
    }
    vorlage = (Vorlage) getCurrentObject();
    if (vorlage == null)
    {
      throw new ApplicationException("Keine Vorlage ausgewählt");
    }
    return vorlage;
  }

  public Input getMuster() throws RemoteException, ApplicationException
  {
    if (muster != null)
    {
      return muster;
    }
    muster = new TextInput(getVorlage().getMuster(), 250);
    return muster;
  }

  public Input getVorschau() throws RemoteException, ApplicationException
  {
    if (vorschau != null)
    {
      return vorschau;
    }
    vorschau = new TextInput(
        VorlageUtil.getDummyName(VorlageTyp.getByKey(getVorlage().getKey()),
            getVorlage().getMuster()),
        1000);
    vorschau.disable();
    return vorschau;
  }

  public void updateVorschau() throws RemoteException, ApplicationException
  {
    getVorschau().setValue(
        VorlageUtil.getDummyName(VorlageTyp.getByKey(getVorlage().getKey()),
            getMuster().getValue().toString()));
  }

  /**
   * This method sets the attributes of the Vorlage.
   * 
   * @return
   * @throws ApplicationException
   * 
   * @throws RemoteException
   */
  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Vorlage bv = getVorlage();
    bv.setMuster((String) getMuster().getValue());
    return bv;
  }

  /**
   * This method stores the Vorlage using the current values.
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
      String fehler = "Fehler bei speichern des Dateinamen";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getDateinamenList() throws RemoteException
  {
    if (namenList != null)
    {
      return namenList;
    }
    namenList = new JVereinTablePart(getVorlagenList(), null);
    namenList.addColumn("Vorlage Art", "art");
    namenList.addColumn("Vorlagenmuster", Vorlage.MUSTER);
    namenList.setContextMenu(new VorlageMenu(namenList));
    namenList.setRememberColWidths(true);
    namenList.setRememberOrder(true);
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
    String tmpSuchtext = ((String) getSuchtext().getValue()).toLowerCase();

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
      VorlageImpl vorlage = (VorlageImpl) vorlagen.get(typ.getKey());
      // Wenn es nicht in der DB steht, neu erstellen
      if (vorlage == null)
      {
        vorlage = Einstellungen.getDBService().createObject(Vorlage.class,
            null);
        vorlage.setAttribute(Vorlage.KEY, typ.getKey());
        vorlage.setMuster(typ.getDefault());
      }
      // ggf. Filtern
      if (tmpSuchtext.length() == 0
          || vorlage.getMuster().toLowerCase().contains(tmpSuchtext)
          || vorlage.getKey().toLowerCase().contains(tmpSuchtext))
      {
        list.add(vorlage);
      }
    }
    return list;
  }
}
