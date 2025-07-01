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

import java.util.Date;

import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.OBanToo.SEPA.Basislastschrift.Zahler;

public class JVereinZahler extends Zahler
{

  private String personId;

  private JVereinZahlerTyp personTyp;

  private Zahlungsweg zahlungsweg;

  private Long buchungsart;

  private Long buchungsklasse;

  private Date datum;

  private Mitglied mitglied;

  private Steuer steuer;

  public JVereinZahler()
  {
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public JVereinZahlerTyp getPersonTyp()
  {
    return personTyp;
  }

  public void setPersonTyp(JVereinZahlerTyp personTyp)
  {
    this.personTyp = personTyp;
  }

  public Zahlungsweg getZahlungsweg()
  {
    return zahlungsweg;
  }

  public void setZahlungsweg(Zahlungsweg zahlungsweg)
  {
    this.zahlungsweg = zahlungsweg;
  }

  public Long getBuchungsartId()
  {
    return buchungsart;
  }

  public void setBuchungsartId(Long buchungsart)
  {
    this.buchungsart = buchungsart;
  }

  public Long getBuchungsklasseId()
  {
    return buchungsklasse;
  }

  public void setBuchungsklasseId(Long buchungsklasse)
  {
    this.buchungsklasse = buchungsklasse;
  }

  public Date getDatum()
  {
    return datum;
  }

  public void setDatum(Date datum)
  {
    this.datum = datum;
  }

  public Mitglied getMitglied()
  {
    return mitglied;
  }

  public void setMitglied(Mitglied mitglied)
  {
    this.mitglied = mitglied;
  }

  public Steuer getSteuer()
  {
    return steuer;
  }

  public void setSteuer(Steuer steuer)
  {
    this.steuer = steuer;
  }
}
