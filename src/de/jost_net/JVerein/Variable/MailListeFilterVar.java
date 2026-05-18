/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.Variable;

public enum MailListeFilterVar
{
  MAIL_EMPFAENGER("filter_mail_empfaenger"),
  BETREFF("filter_betreff"),
  DATUM_BEARBEITUNG_VON_F("filter_bearbeitung_von_f"),
  DATUM_BEARBEITUNG_BIS_F("filter_bearbeitung_bis_f"),
  DATUM_VERSAND_VON_F("filter_versand_von_f"),
  DATUM_VERSAND_BIS_F("filter_versand_bis_f");

  private String name;

  MailListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
