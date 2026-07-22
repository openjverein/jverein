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
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.action.MitgliederImportAction;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.util.ApplicationException;

public abstract class AbstractMitgliedListeView extends AbstractView
{

  final MitgliedControl control = new MitgliedControl(this);

  @Override
  public void bind() throws Exception
  {
    control.setMitgliedAuswahl(getMitgliedAuswahl());

    GUI.getView().setTitle(getTitle());
    this.setCurrentObject(
        Einstellungen.getDBService().createObject(Mitglied.class, null));

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
    if (anzahlbeitragsgruppe == 0)
    {// TODO braucht man das hier wiklich?
      new LabelInput("Noch keine Beitragsgruppe erfaßt. Bitte unter "
          + "Administration|Beitragsgruppen erfassen.").paint(getParent());
    }

    getFilter();
    if (anzahlbeitragsgruppe > 0)
    {
      control.getTablePart(getDetailAction()).paint(getParent());
    }
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getHilfeButton());
    if (anzahlbeitragsgruppe > 0)
    {
      buttons.addButton("Import", new MitgliederImportAction(), null, false,
          "file-import.png");
      buttons.addButton(control.getExportButton());
      buttons.addButton("Neu", getDetailAction(), null, false,
          "document-new.png");
    }
    buttons.paint(this.getParent());

    GUI.getView().addPanelButton(control.exportButton(ExportArt.PDF));
    GUI.getView().addPanelButton(control.exportButton(ExportArt.CSV));
    GUI.getView().addPanelButton(control.getSpaltenPanelButton());
  }

  protected abstract MitgliedAuswahl getMitgliedAuswahl();

  protected abstract String getTitle();

  protected abstract void getFilter()
      throws RemoteException, ApplicationException;

  protected abstract Action getDetailAction();

  protected abstract Button getHilfeButton();
}
