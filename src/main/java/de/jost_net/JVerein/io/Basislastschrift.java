/**********************************************************************
 * Copyright (c) by Markus Spann
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.willuhn.util.ApplicationException;

/**
 * Eine SEPA-Basislastschrift - Ersatz für OBanToos
 * {@code Basislastschrift.Basislastschrift}.
 * <p>
 * Die eigentliche pain.008-XML-Erzeugung läuft in JVerein bereits über
 * HBCI4Java ({@code SEPAGeneratorFactory}), nicht über diese Klasse - sie
 * dient nur noch als Datenhalter für die Lastschrift-Übersicht (PDF-Druck).
 *
 * @since 4.3.0
 */
public class Basislastschrift
{
  private String messageId;

  private String bic;

  private String iban;

  private String name;

  private String glaeubigerId;

  private boolean komprimiert;

  private final List<Zahler> zahler = new ArrayList<>();

  private final Date creationDateTime = new Date();

  public String getMessageID()
  {
    return messageId;
  }

  public void setMessageID(String messageId)
  {
    this.messageId = messageId;
  }

  public String getBIC()
  {
    return bic;
  }

  public void setBIC(String bic)
  {
    this.bic = bic;
  }

  public String getIBAN()
  {
    return iban;
  }

  public void setIBAN(String iban)
  {
    this.iban = iban;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getGlaeubigerID()
  {
    return glaeubigerId;
  }

  public void setGlaeubigerID(String glaeubigerId)
  {
    this.glaeubigerId = glaeubigerId;
  }

  public void setKomprimiert(boolean komprimiert)
  {
    this.komprimiert = komprimiert;
  }

  public boolean isKomprimiert()
  {
    return komprimiert;
  }

  // Ein Zusammenfassen mehrerer Buchungen zu einer Mandats-ID (bei
  // komprimiert=true) findet hier bewusst nicht mehr statt - das übernimmt
  // der Aufrufer bereits vorher, siehe Zahler.add().
  public void add(Zahler zahler) throws ApplicationException
  {
    if (zahler.getBetrag().compareTo(BigDecimal.ZERO) < 0)
    {
      throw new ApplicationException("Ungültiger Betrag: " + zahler.getBetrag());
    }
    this.zahler.add(zahler);
  }

  public List<Zahler> getZahler()
  {
    return zahler;
  }

  public BigDecimal getKontrollsumme() throws ApplicationException
  {
    BigDecimal summe = BigDecimal.ZERO;
    for (Zahler z : zahler)
    {
      summe = summe.add(z.getBetrag());
    }
    return summe;
  }

  public int getAnzahlBuchungen()
  {
    return zahler.size();
  }

  // Zeitpunkt der Konstruktion dieses Objekts, nicht der eigentlichen
  // SEPA-Datei-Erzeugung - siehe Klassen-Javadoc.
  public Date getCreationDateTime()
  {
    return creationDateTime;
  }
}
