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
package de.jost_net.JVerein.io;

import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;

public abstract class AuswertungMitgliedAbstractPDF
    extends AuswertungMitgliedAbstract
{

  protected MitgliedAuswahl mitgliedauswahl = MitgliedAuswahl.MITGLIEDER;

  @Override
  public String getDateiname(Object object)
  {
    if (((MitgliedControl) object).getMitgliedAuswahl()
        .equals(MitgliedAuswahl.MITGLIEDER))
    {
      return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_MITGLIED_DATEINAME,
          object, getName()) + ".pdf";
    }
    else
    {
      mitgliedauswahl = MitgliedAuswahl.NICHTMITGLIEDER;
      return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_NICHT_MITGLIED_DATEINAME,
          object, getName()) + ".pdf";
    }
  }

}
