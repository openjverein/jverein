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
import de.jost_net.JVerein.gui.action.FormularfeldAction;
import de.jost_net.JVerein.gui.menu.FormularfeldMenu;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;

public abstract class FormularPartControl extends VorZurueckControl
{
  protected TablePart formularfelderList;

  protected Formular formular;
 

  public FormularPartControl(AbstractView view, Formular formular)
  {
    super(view);
    this.formular = formular;
  }

  public Part getFormularfeldList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Formularfeld> formularfelder = service
        .createList(Formularfeld.class);
    formularfelder.addFilter("formular = ?", new Object[] { formular.getID() });
    formularfelder.setOrder("ORDER BY seite, x, y");

    formularfelderList = new TablePart(formularfelder,
        new FormularfeldAction());
    formularfelderList.addColumn("Name", "name");
    formularfelderList.addColumn("Seite", "seite");
    formularfelderList.addColumn("Von links", "x");
    formularfelderList.addColumn("Von unten", "y");
    formularfelderList.addColumn("Schriftart", "font");
    formularfelderList.addColumn("Schriftgröße", "fontsize");

    formularfelderList.setRememberColWidths(true);
    formularfelderList.setContextMenu(new FormularfeldMenu());
    formularfelderList.setRememberOrder(true);
    formularfelderList.removeFeature(FeatureSummary.class);
    return formularfelderList;
  }

  public void refreshTable() throws RemoteException
  {
    formularfelderList.removeAll();
    DBIterator<Formularfeld> formularfelder = Einstellungen.getDBService()
        .createList(Formularfeld.class);
    formularfelder.addFilter("formular = ?", new Object[] { formular.getID() });
    formularfelder.setOrder("ORDER BY x, y");
    while (formularfelder.hasNext())
    {
      formularfelderList.addItem(formularfelder.next());
    }
    formularfelderList.sort();
  }

}
