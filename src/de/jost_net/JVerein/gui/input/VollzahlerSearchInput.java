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
package de.jost_net.JVerein.gui.input;

import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.logging.Logger;

public class VollzahlerSearchInput extends SearchInput
{
  
  Mitglied mitglied = null;
  
  public VollzahlerSearchInput(Mitglied mitglied)
  {
    super();
    this.mitglied = mitglied;
  }
  
  @Override
  @SuppressWarnings("rawtypes")
  public List startSearch(String text)
  {
    try
    {
      StringBuffer cond = new StringBuffer();
      // Beitragsgruppen ermitteln, die Zahler für andere Mitglieder sind
      DBIterator<Beitragsgruppe> bg = Einstellungen.getDBService()
          .createList(Beitragsgruppe.class);
      bg.addFilter("beitragsart != ?", ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey());
      while (bg.hasNext())
      {
        if (cond.length() > 0)
        {
          cond.append(" OR ");
        }
        Beitragsgruppe beitragsgruppe = bg.next();
        cond.append("beitragsgruppe = ");
        cond.append(beitragsgruppe.getID());
      }
      DBIterator<Mitglied> zhl = Einstellungen.getDBService()
          .createList(Mitglied.class);
      zhl.addFilter("(" + cond.toString() + ")");
      if(mitglied.getID() != null)
        zhl.addFilter("id != ?",mitglied.getID());
      MitgliedUtils.setNurAktive(zhl);
      MitgliedUtils.setMitglied(zhl);
      if (text != null)
      {
        text = "%" + text.toUpperCase() + "%";
        zhl.addFilter("(UPPER(name) like ? or UPPER(vorname) like ?)",
            new Object[] { text, text });
      }
      zhl.setOrder("ORDER BY name, vorname");

      return zhl != null ? PseudoIterator.asList(zhl) : null;

    }
    catch (Exception e)
    {
      Logger.error("Unable to load mitglied list", e);
      return null;
    }
  }

}
