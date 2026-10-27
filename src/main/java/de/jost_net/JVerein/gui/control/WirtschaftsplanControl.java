/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.WirtschaftsplanListMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.WirtschaftsplanDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanControl extends FilterControl
{

  private JVereinTablePart wirtschaftsplaene;

  public WirtschaftsplanControl(AbstractView view) throws RemoteException
  {
    super(view);
  }

  /**
   * Liefert die Liste der Wirtschaftsplaene.
   *
   * @return die Liste der Wirtschaftsplaene.
   * @throws RemoteException
   *           wenn ein Fehler beim Zugriff auf die Datenbank auftritt.
   */
  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (wirtschaftsplaene != null)
    {
      return wirtschaftsplaene;
    }

    DBService service = Einstellungen.getDBService();

    wirtschaftsplaene = new JVereinTablePart(
        service.createList(Wirtschaftsplan.class), null);

    CurrencyFormatter formatter = new CurrencyFormatter("",
        Einstellungen.BIGDECIMALFORMAT);
    DateFormatter dateFormatter = new DateFormatter(new JVDateFormatTTMMJJJJ());

    wirtschaftsplaene.addColumn("ID", "id");
    wirtschaftsplaene.addColumn("Bezeichnung", "bezeichnung");
    wirtschaftsplaene.addColumn("Von", "datum_von", dateFormatter);
    wirtschaftsplaene.addColumn("Bis", "datum_bis", dateFormatter);
    wirtschaftsplaene.addColumn("Einnahmen Soll", "planEinnahme", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Soll", "planAusgabe", formatter);
    wirtschaftsplaene.addColumn("Saldo Soll", "planSaldo", formatter);
    wirtschaftsplaene.addColumn("Einnahmen Ist", "istPlus", formatter);
    wirtschaftsplaene.addColumn("Ausgaben Ist", "istMinus", formatter);
    wirtschaftsplaene.addColumn("Saldo Ist", "istSaldo", formatter);
    wirtschaftsplaene.addColumn("Saldo Differenz", "differenz", formatter);

    wirtschaftsplaene
        .setContextMenu(new WirtschaftsplanListMenu(wirtschaftsplaene));
    wirtschaftsplaene.setAction(
        new EditAction(WirtschaftsplanDetailView.class, wirtschaftsplaene));
    VorZurueckControl.setObjektListe(null, null);
    wirtschaftsplaene.setMulti(true);
    return wirtschaftsplaene;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    // Es gibt keinen Refresh
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.WIRTSCHAFTSPLAENE_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.WIRTSCHAFTSPLAENE_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.WIRTSCHAFTSPLAENE_DATEINAME);
  }

}
