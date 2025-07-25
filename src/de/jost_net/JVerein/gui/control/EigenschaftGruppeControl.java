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
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.menu.EigenschaftGruppeMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.EigenschaftGruppeDetailView;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class EigenschaftGruppeControl extends VorZurueckControl
    implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private JVereinTablePart eigenschaftgruppeList;

  private Input bezeichnung;

  private CheckboxInput pflicht;

  private CheckboxInput max1;

  private EigenschaftGruppe eigenschaftgruppe;

  public EigenschaftGruppeControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  private EigenschaftGruppe getEigenschaftGruppe()
  {
    if (eigenschaftgruppe != null)
    {
      return eigenschaftgruppe;
    }
    eigenschaftgruppe = (EigenschaftGruppe) getCurrentObject();
    return eigenschaftgruppe;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getEigenschaftGruppe().getBezeichnung(), 30);
    return bezeichnung;
  }

  public CheckboxInput getPflicht() throws RemoteException
  {
    if (pflicht != null)
    {
      return pflicht;
    }
    pflicht = new CheckboxInput(getEigenschaftGruppe().getPflicht());
    return pflicht;
  }

  public CheckboxInput getMax1() throws RemoteException
  {
    if (max1 != null)
    {
      return max1;
    }
    max1 = new CheckboxInput(getEigenschaftGruppe().getMax1());
    return max1;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    EigenschaftGruppe eg = getEigenschaftGruppe();
    eg.setBezeichnung((String) getBezeichnung().getValue());
    eg.setPflicht((Boolean) getPflicht().getValue());
    eg.setMax1((Boolean) getMax1().getValue());
    return eg;
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
      String fehler = "Fehler bei speichern der Eigenschaft Gruppe";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getEigenschaftGruppeList() throws RemoteException
  {
    if (eigenschaftgruppeList != null)
    {
      return eigenschaftgruppeList;
    }

    DBService service = Einstellungen.getDBService();
    DBIterator<EigenschaftGruppe> eigenschaftgruppe = service
        .createList(EigenschaftGruppe.class);
    eigenschaftgruppe.setOrder("ORDER BY bezeichnung");

    eigenschaftgruppeList = new JVereinTablePart(eigenschaftgruppe, null);
    eigenschaftgruppeList.addColumn("Bezeichnung", "bezeichnung");
    eigenschaftgruppeList.addColumn("Pflicht", "pflicht",
        new JaNeinFormatter());
    eigenschaftgruppeList.addColumn("Max. 1 Eigenschaft", "max1",
        new JaNeinFormatter());
    eigenschaftgruppeList
        .setContextMenu(new EigenschaftGruppeMenu(eigenschaftgruppeList));
    eigenschaftgruppeList.setRememberColWidths(true);
    eigenschaftgruppeList.setRememberOrder(true);
    eigenschaftgruppeList.addFeature(new FeatureSummary());
    eigenschaftgruppeList.setAction(new EditAction(
        EigenschaftGruppeDetailView.class, eigenschaftgruppeList));
    VorZurueckControl.setObjektListe(null, null);
    return eigenschaftgruppeList;
  }
}
