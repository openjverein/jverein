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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.VorlageMenu;
import de.jost_net.JVerein.gui.view.EinstellungenVorlageDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Vorlage;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class VorlageControl extends AbstractControl implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart namenList;

  private Vorlage vorlage;

  private Input muster;

  private Input vorschau;

  public VorlageControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Vorlage getVorlage() throws RemoteException
  {
    if (vorlage != null)
    {
      return vorlage;
    }
    vorlage = (Vorlage) getCurrentObject();
    if (vorlage == null)
    {
      vorlage = (Vorlage) Einstellungen.getDBService()
          .createObject(Vorlage.class, null);
    }
    return vorlage;
  }

  public Input getMuster() throws RemoteException
  {
    if (muster != null)
    {
      return muster;
    }
    muster = new TextInput(getVorlage().getMuster(), 250);
    return muster;
  }

  public Input getVorschau() throws RemoteException
  {
    if (vorschau != null)
    {
      return vorschau;
    }
    vorschau = new TextInput(
        VorlageUtil.getDummyName(VorlageTyp.getByKey(getVorlage().getKey()),
            getVorlage().getMuster()),
        250);
    vorschau.disable();
    return vorschau;
  }

  public void updateVorschau() throws RemoteException
  {
    getVorschau()
        .setValue(VorlageUtil.getDummyName(
            VorlageTyp.getByKey(getVorlage().getKey()),
            getMuster().getValue().toString()));
  }

  /**
   * This method sets the attributes of the DateinamenVorlage.
   * 
   * @return
   * 
   * @throws nRemoteException
   */
  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Vorlage bv = getVorlage();
    bv.setMuster((String) getMuster().getValue());
    return bv;
  }

  /**
   * This method stores the DateinamenVorlage using the current values.
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
    DBService service = Einstellungen.getDBService();
    DBIterator<Vorlage> namen = service
        .createList(Vorlage.class);
    namen.setOrder("ORDER BY " + Vorlage.MUSTER);

    if (namenList == null)
    {
      namenList = new TablePart(namen,
          new EditAction(EinstellungenVorlageDetailView.class));
      namenList.addColumn("Vorlage Art", "art");
      namenList.addColumn("Vorlagenmuster", Vorlage.MUSTER);
      namenList.setContextMenu(new VorlageMenu());
      namenList.setRememberColWidths(true);
      namenList.setRememberOrder(true);
      namenList.setRememberState(true);
      namenList.removeFeature(FeatureSummary.class);
    }
    else
    {
      namenList.removeAll();
      while (namen.hasNext())
      {
        namenList.addItem(namen.next());
      }
      namenList.sort();
    }
    return namenList;
  }


}
