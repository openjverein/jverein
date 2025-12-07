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
import de.jost_net.JVerein.gui.menu.MitgliedstypMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.MitgliedstypDetailView;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedstypControl extends VorZurueckControl implements Savable
{
  private de.willuhn.jameica.system.Settings settings;

  private JVereinTablePart mitgliedstypList;

  private Input bezeichnung;

  private Input bezeichnungplural;

  private Mitgliedstyp mitgliedstyp;

  private boolean editable = false;

  public MitgliedstypControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Mitgliedstyp getMitgliedstyp()
  {
    if (mitgliedstyp != null)
    {
      return mitgliedstyp;
    }
    mitgliedstyp = (Mitgliedstyp) getCurrentObject();
    return mitgliedstyp;
  }

  public boolean isMitgliedstypEditable() throws RemoteException
  {
    if (getMitgliedstyp().getJVereinid() > 0)
    {
      GUI.getStatusBar().setErrorText(
          "Dieser Mitgliedstyp ist reserviert und darf durch den Benutzer nicht verändert werden.");
      return editable = false;
    }
    return editable = true;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getMitgliedstyp().getBezeichnung(), 30);
    bezeichnung.setMandatory(true);
    bezeichnung.setEnabled(editable);
    return bezeichnung;
  }

  public Input getBezeichnungPlural() throws RemoteException
  {
    if (bezeichnungplural != null)
    {
      return bezeichnungplural;
    }
    bezeichnungplural = new TextInput(getMitgliedstyp().getBezeichnungPlural(),
        30);
    bezeichnungplural.setMandatory(true);
    bezeichnungplural.setEnabled(editable);
    return bezeichnungplural;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Mitgliedstyp mt = getMitgliedstyp();
    mt.setBezeichnung((String) getBezeichnung().getValue());
    mt.setBezeichnungPlural((String) getBezeichnungPlural().getValue());
    return mt;
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
      String fehler = "Fehler bei speichern des Mitgliedstyp";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getMitgliedstypList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Mitgliedstyp> mtIt = service.createList(Mitgliedstyp.class);
    mtIt.setOrder("ORDER BY " + Mitgliedstyp.BEZEICHNUNG);

    mitgliedstypList = new JVereinTablePart(mtIt, null);
    mitgliedstypList.addColumn("Bezeichnung", Mitgliedstyp.BEZEICHNUNG);
    mitgliedstypList.addColumn("Bezeichnung Plural",
        Mitgliedstyp.BEZEICHNUNG_PLURAL);
    mitgliedstypList.addColumn("ID", "id");
    mitgliedstypList.setContextMenu(new MitgliedstypMenu(mitgliedstypList));
    mitgliedstypList.setRememberColWidths(true);
    mitgliedstypList.setRememberOrder(true);
    mitgliedstypList.addFeature(new FeatureSummary());
    mitgliedstypList.setMulti(true);
    mitgliedstypList.setAction(
        new EditAction(MitgliedstypDetailView.class, mitgliedstypList));
    VorZurueckControl.setObjektListe(null, null);
    return mitgliedstypList;
  }
}
