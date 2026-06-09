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
import de.jost_net.JVerein.gui.menu.EigenschaftMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.EigenschaftDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class EigenschaftControl extends VorZurueckControl implements Savable
{
  private JVereinTablePart eigenschaftList;

  private Input bezeichnung;

  private SelectInput eigenschaftgruppe;

  private Eigenschaft eigenschaft;

  private TextInput name;

  public EigenschaftControl(AbstractView view)
  {
    super(view);
  }

  private Eigenschaft getEigenschaft() throws RemoteException
  {
    if (eigenschaft != null)
    {
      return eigenschaft;
    }
    eigenschaft = (Eigenschaft) getCurrentObject();
    if (eigenschaft == null)
    {
      eigenschaft = (Eigenschaft) Einstellungen.getDBService()
          .createObject(Eigenschaft.class, null);
    }
    return eigenschaft;
  }

  public Input getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getEigenschaft().getName(), 30);
    name.setMandatory(true);
    return name;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getEigenschaft().getBezeichnung(), 30);
    bezeichnung.setMandatory(true);
    return bezeichnung;
  }

  public Input getEigenschaftGruppe() throws RemoteException
  {
    if (eigenschaftgruppe != null)
    {
      return eigenschaftgruppe;
    }
    DBIterator<EigenschaftGruppe> list = Einstellungen.getDBService()
        .createList(EigenschaftGruppe.class);
    list.setOrder("ORDER BY bezeichnung");
    eigenschaftgruppe = new SelectInput(
        list != null ? PseudoIterator.asList(list) : null,
        getEigenschaft().getEigenschaftGruppe());
    eigenschaftgruppe.setValue(getEigenschaft().getEigenschaftGruppe());
    eigenschaftgruppe.setAttribute("bezeichnung");
    eigenschaftgruppe.setPleaseChoose("Bitte auswählen");
    eigenschaftgruppe.setMandatory(true);
    return eigenschaftgruppe;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Eigenschaft ei = getEigenschaft();

    GenericObject o = (GenericObject) getEigenschaftGruppe().getValue();
    if (o != null)
    {
      ei.setEigenschaftGruppe(Long.valueOf(o.getID()));
    }
    else
    {
      ei.setEigenschaftGruppe(null);
    }
    ei.setBezeichnung((String) getBezeichnung().getValue());
    ei.setName((String) getName().getValue());
    return ei;
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
      String fehler = "Fehler bei speichern der Eigenschaft";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (eigenschaftList != null)
    {
      return eigenschaftList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Eigenschaft> eigenschaften = service
        .createList(Eigenschaft.class);
    eigenschaften.setOrder("ORDER BY bezeichnung");

    eigenschaftList = new JVereinTablePart(eigenschaften, null);
    eigenschaftList.addColumn("Name", "name");
    eigenschaftList.addColumn("Bezeichnung", "bezeichnung");
    eigenschaftList.addColumn("Gruppe", "eigenschaftgruppe");
    eigenschaftList.setContextMenu(new EigenschaftMenu(eigenschaftList));
    eigenschaftList.setRememberState(true);
    eigenschaftList.setMulti(true);
    eigenschaftList.setAction(
        new EditAction(EigenschaftDetailView.class, eigenschaftList));
    VorZurueckControl.setObjektListe(null, null);
    return eigenschaftList;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.EIGENSCHAFTEN_DATEINAME);
  }
}
