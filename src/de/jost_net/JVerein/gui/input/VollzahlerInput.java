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

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.MitgliedUtils;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;

public class VollzahlerInput
{

  public AbstractInput getMitgliedInput(AbstractInput mitgliedInput,
      Mitglied mitglied, int auswahl) throws RemoteException
  {
    Version version = Application.getManifest().getVersion();
    if (version.compareTo(new Version("2.10.5")) < 0)
    {
      auswahl = AbstractInputAuswahl.ComboBox;
    }
    String suche = "";
    if (mitglied.getVollZahlerID() != null)
    {
      suche = mitglied.getVollZahlerID().toString();
    }
    Mitglied zahlmitglied = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, suche);
    switch (auswahl)
    {
      case AbstractInputAuswahl.ComboBox:
        StringBuffer cond = new StringBuffer();
        // Beitragsgruppen ermitteln, die Zahler für andere Mitglieder sind
        DBIterator<Beitragsgruppe> bg = Einstellungen.getDBService()
            .createList(Beitragsgruppe.class);
        bg.addFilter("beitragsart != ?",
            ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey());
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
        if (mitglied.getID() != null)
          zhl.addFilter("id != ?", mitglied.getID());
        MitgliedUtils.setMitglied(zhl);
        zhl.setOrder("ORDER BY name, vorname");
        mitgliedInput = new SelectNoScrollInput(
            zhl != null ? PseudoIterator.asList(zhl) : null, zahlmitglied);
        ((SelectNoScrollInput) mitgliedInput).setAttribute("namevorname");
        ((SelectNoScrollInput) mitgliedInput)
            .setPleaseChoose("Bitte auswählen");
        break;
      case AbstractInputAuswahl.SearchInput:
      default:
        mitgliedInput = new VollzahlerSearchInput(mitglied);
        ((VollzahlerSearchInput) mitgliedInput)
            .setSearchString("Zum Suchen tippen");
        mitgliedInput.setValue(zahlmitglied);
    }
    return mitgliedInput;
  }

}
