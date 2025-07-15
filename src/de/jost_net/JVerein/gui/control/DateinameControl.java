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
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.DateinameMenu;
import de.jost_net.JVerein.gui.view.DateinameDetailView;
import de.jost_net.JVerein.keys.DateinameTyp;
import de.jost_net.JVerein.rmi.DateinamenVorlage;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.util.Dateiname;
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

public class DateinameControl extends AbstractControl implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart namenList;

  private DateinamenVorlage dateiname;

  private Input name;

  private Input vorschau;

  public DateinameControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public DateinamenVorlage getDateiname() throws RemoteException
  {
    if (dateiname != null)
    {
      return dateiname;
    }
    dateiname = (DateinamenVorlage) getCurrentObject();
    if (dateiname == null)
    {
      dateiname = (DateinamenVorlage) Einstellungen.getDBService()
          .createObject(DateinamenVorlage.class, null);
    }
    return dateiname;
  }

  public Input getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getDateiname().getDateiname(), 250);
    return name;
  }

  public Input getVorschau() throws RemoteException
  {
    if (vorschau != null)
    {
      return vorschau;
    }
    vorschau = new TextInput(generiereVorschau(getDateiname().getDateiname()),
        250);
    vorschau.disable();
    return vorschau;
  }

  public String generiereVorschau(String dateiname)
  {
    return Dateiname.translate(getDummyMap(), dateiname);
  }

  public void updateVorschau() throws RemoteException
  {
    getVorschau().setValue(generiereVorschau(getName().getValue().toString()));
  }

  public Map<String, Object> getDummyMap()
  {
    Map<String, Object> map = null;
    try
    {
      map = new AllgemeineMap().getMap(null);
      DateinameTyp typ = DateinameTyp
          .getByKey(Integer.valueOf(getDateiname().getID()));
      switch (typ)
      {
        case SPENDENBESCHEINIGUNG:
          map = SpendenbescheinigungMap.getDummyMap(map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case RECHNUNG_MITGLIED:
        case MAHNUNG_MITGLIED:
          map = RechnungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case KONTOAUSZUG_MITGLIED:
        case PRENOTIFICATION_MITGLIED:
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIES_FORMULAR:
          map.put("formular_name", "Freies Formular");
          break;
        case FREIES_FORMULAR_MITGLIED:
          map = MitgliedMap.getDummyMap(map);
          map.put("formular_name", "Freies Formular");
          break;
        case RECHNUNG:
        case MAHNUNG:
        case KONTOAUSZUG:
        case CT1_AUSGABE:
        case PRENOTIFICATION:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        default:
          Logger.error("Dateiname Typ nicht implementiert: " + typ.toString());
          break;
      }
    }
    catch (RemoteException e)
    {
      //
    }

    return map;
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
    DateinamenVorlage bv = getDateiname();
    bv.setDateiname((String) getName().getValue());
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
    DBIterator<DateinamenVorlage> namen = service
        .createList(DateinamenVorlage.class);
    namen.setOrder("ORDER BY dateiname");

    if (namenList == null)
    {
      namenList = new TablePart(namen,
          new EditAction(DateinameDetailView.class));
      namenList.addColumn("Dateityp", "id-int");
      namenList.addColumn("Dateiname", "dateiname");
      namenList.setContextMenu(new DateinameMenu());
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
