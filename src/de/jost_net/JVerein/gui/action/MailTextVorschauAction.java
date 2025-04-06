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
package de.jost_net.JVerein.gui.action;

import java.util.Map;

import de.jost_net.JVerein.gui.control.IMailControl;
import de.jost_net.JVerein.gui.dialogs.MailTextVorschauDialog;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MailTextVorschauAction implements Action
{
  Map<String, Object> map;

  boolean mitMitglied;

  public MailTextVorschauAction(Map<String, Object> map, boolean mitMitglied)
  {
    super();
    this.map = map;
    this.mitMitglied = mitMitglied;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof IMailControl)
    {
      new MailTextVorschauDialog((IMailControl) context, map,
          MailTextVorschauDialog.POSITION_CENTER, mitMitglied);
    }
    else
    {
      String name = "";
      if (context != null && context.getClass() != null)
      {
        name = context.getClass().getCanonicalName();
      }
      Logger.error("ShowVariablesDiaglog: Ung�ltige Klasse: " + name);
    }
  }
}
