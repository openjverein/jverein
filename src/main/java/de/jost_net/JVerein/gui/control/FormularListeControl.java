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
import de.jost_net.JVerein.gui.formatter.FormularLinkFormatter;
import de.jost_net.JVerein.gui.formatter.FormularartFormatter;
import de.jost_net.JVerein.gui.menu.FormularMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.FormularDetailView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.Column;

public class FormularListeControl extends AbstractJVereinControl
{
  private JVereinTablePart formularList;

  public FormularListeControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (formularList != null)
    {
      return formularList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Formular> formulare = service.createList(Formular.class);
    formulare.setOrder("ORDER BY art, bezeichnung");

    formularList = new JVereinTablePart(formulare, null);
    formularList.addColumn("Bezeichnung", "bezeichnung");
    formularList.addColumn("Art", "art", new FormularartFormatter(), false,
        Column.ALIGN_LEFT);
    formularList.addColumn("Fortlaufende Nr.", "zaehler");
    formularList.addColumn("Verknüpft mit", "formlink",
        new FormularLinkFormatter());
    formularList.setContextMenu(new FormularMenu(this, formularList));
    formularList.setMulti(true);
    formularList
        .setAction(new EditAction(FormularDetailView.class, formularList));
    VorZurueckControl.setObjektListe(null, null);
    return formularList;
  }

  public void refreshFormularTable() throws RemoteException
  {
    if (formularList != null)
    {
      formularList.removeAll();
      DBIterator<Formular> formulare = Einstellungen.getDBService()
          .createList(Formular.class);
      formulare.setOrder("ORDER BY art, bezeichnung");
      while (formulare.hasNext())
      {
        formularList.addItem(formulare.next());
      }
      formularList.sort();
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARE_TITEL);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARE_SUBTITEL);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.FORMULARE_DATEINAME);
  }
}
