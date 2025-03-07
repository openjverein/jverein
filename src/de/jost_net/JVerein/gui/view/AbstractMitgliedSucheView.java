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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.MitgliederImportAction;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.FilterControl.Mitgliedstypen;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;

public abstract class AbstractMitgliedSucheView extends AbstractView
{

  private TablePart p;

  final MitgliedControl control = new MitgliedControl(this);

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(getTitle());
    this.setCurrentObject(
        Einstellungen.getDBService().createObject(Mitglied.class, null)); // leeres
                                                                          // Object
                                                                          // erzeugen

    DBService service = Einstellungen.getDBService();
    String sql = "select count(*) from beitragsgruppe";
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        rs.next();
        return Long.valueOf(rs.getLong(1));
      }
    };
    Long anzahlbeitragsgruppe = (Long) service.execute(sql, new Object[] {},
        rs);
    if (anzahlbeitragsgruppe.longValue() == 0)
    {
      new LabelInput("Noch keine Beitragsgruppe erfaßt. Bitte unter "
          + "Administration|Beitragsgruppen erfassen.").paint(getParent());
    }
    rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        rs.next();
        return Long.valueOf(rs.getLong(1));
      }
    };

    getFilter();

    Long anzahl = (Long) service.execute(sql, new Object[] {}, rs);
    if (anzahl.longValue() > 0)
    {
      Mitgliedstyp mt = (Mitgliedstyp) control.getSuchMitgliedstyp(Mitgliedstypen.MITGLIED).getValue();
      if (mt != null)
      {
      Logger.debug(mt.getID() + ": " + mt.getBezeichnung());
      p = control.getMitgliedTable(Integer.parseInt(mt.getID()),
          getDetailAction());
      }
      else
      {
        p = control.getMitgliedTable(0, getDetailAction());
      }
      p.paint(getParent());
    }
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getHilfeButton());
    if (anzahlbeitragsgruppe > 0)
    {
      buttons.addButton("Import", new MitgliederImportAction(), null, false,
          "file-import.png");
      buttons.addButton("Neu", getDetailAction(), null, false, "document-new.png");
    }
    buttons.paint(this.getParent());
  }

  @Override
  public void unbind()
  {
    if (p != null)
      p.removeAll();
  }



  public abstract String getTitle();

  public abstract void getFilter() throws RemoteException;

  public abstract Action getDetailAction();

  public abstract Button getHilfeButton();
}