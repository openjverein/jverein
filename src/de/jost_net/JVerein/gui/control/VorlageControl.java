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

import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.VorlageMenu;
import de.jost_net.JVerein.gui.view.EinstellungenVorlageDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Vorlage;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
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

  private Vorlage vorlageObjekt;

  private Input name;

  private Input vorschau;

  public VorlageControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Vorlage getVorlageObjekt() throws RemoteException
  {
    if (vorlageObjekt != null)
    {
      return vorlageObjekt;
    }
    vorlageObjekt = (Vorlage) getCurrentObject();
    if (vorlageObjekt == null)
    {
      vorlageObjekt = (Vorlage) Einstellungen.getDBService()
          .createObject(Vorlage.class, null);
    }
    return vorlageObjekt;
  }

  public Input getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getVorlageObjekt().getText(), 250);
    return name;
  }

  public Input getVorschau() throws RemoteException
  {
    if (vorschau != null)
    {
      return vorschau;
    }
    vorschau = new TextInput(generiereVorschau(getVorlageObjekt().getText()),
        250);
    vorschau.disable();
    return vorschau;
  }

  public String generiereVorschau(String dateiname)
  {
    return translate(getDummyMap(), dateiname);
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
      VorlageTyp typ = VorlageTyp
          .getByKey(Integer.valueOf(getVorlageObjekt().getID()));
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
    Vorlage bv = getVorlageObjekt();
    bv.setText((String) getName().getValue());
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
    namen.setOrder("ORDER BY " + Vorlage.TEXT);

    if (namenList == null)
    {
      namenList = new TablePart(namen,
          new EditAction(EinstellungenVorlageDetailView.class));
      namenList.addColumn("Vorlage Art", "id-int");
      namenList.addColumn("Vorlage", Vorlage.TEXT);
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

  public static String getVorlage(VorlageTyp typ)
  {
    return getVorlage(typ, null, null);
  }

  public static String getVorlage(VorlageTyp typ, Object obj)
  {
    return getVorlage(typ, obj, null);
  }

  public static String getVorlage(VorlageTyp typ, Object obj,
      Mitglied mitglied)
  {
    Map<String, Object> map = null;
    String dateiname = "";
    try
    {
      map = new AllgemeineMap().getMap(null);
      dateiname = ((Vorlage) Einstellungen.getDBService()
          .createObject(Vorlage.class, String.valueOf(typ.getKey())))
              .getText();
      switch (typ)
      {
        case SPENDENBESCHEINIGUNG:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case RECHNUNG_MITGLIED:
        case MAHNUNG_MITGLIED:
          // Ein Dokument pro Mitglied
          map = new RechnungMap().getMap((Rechnung) obj, map);
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case KONTOAUSZUG_MITGLIED:
        case PRENOTIFICATION_MITGLIED:
          map = new MitgliedMap().getMap(mitglied, map);
          break;
        case FREIES_FORMULAR:
          map.put("formular_name", (String) obj);
          break;
        case FREIES_FORMULAR_MITGLIED:
          map = new MitgliedMap().getMap(mitglied, map);
          map.put("formular_name", (String) obj);
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
          return "";
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return translate(map, dateiname);
  }

  public static String translate(Map<String, Object> map, String inString)
  {
    Velocity.init();
    VelocityContext context = new VelocityContext();
    context.put("dateformat", new JVDateFormatTTMMJJJJ());
    context.put("decimalformat", Einstellungen.DECIMALFORMAT);
    VarTools.add(context, map);
    StringWriter wdateiname = new StringWriter();
    String in = inString.replaceAll("-\\$", " \\$");
    Velocity.evaluate(context, wdateiname, "LOG", in);
    String str = wdateiname.toString();
    str = str.replaceAll(" ", "-");
    return str;
  }
}
