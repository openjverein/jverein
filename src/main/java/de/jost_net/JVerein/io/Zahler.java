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
import java.text.MessageFormat;
import java.util.Date;

import de.jost_net.JVerein.keys.MandatSequence;
import de.jost_net.JVerein.util.IbanUtil;
import de.jost_net.JVerein.util.SepaZeichensatz;
import de.willuhn.util.ApplicationException;

/**
 * Ein Zahlungspflichtiger einer SEPA-Basislastschrift - Ersatz für OBanToos
 * {@code Basislastschrift.Zahler}.
 *
 * @since 4.3.0
 */
public class Zahler
{
  private static final int MANDATID_MAX_LAENGE = 35;

  private static final int NAME_MAX_LAENGE = 70;

  private static final int VERWENDUNGSZWECK_MAX_LAENGE = 140;

  private static final BigDecimal NULL = new BigDecimal("0.00");

  private String mandatid;

  private Date mandatdatum;

  private String bic;

  private String name;

  private String iban;

  private String verwendungszweck;

  private String verwendungszweckOrig;

  private BigDecimal betrag;

  private MandatSequence mandatsequence;

  private Date faelligkeit;

  // Zählt, wie oft add() den Verwendungszweck schon zusammengefasst hat -
  // beim ersten Mal muss noch der eigene Betrag angehängt werden.
  private int verwendungszwecke = 0;

  public String getMandatid() throws ApplicationException
  {
    checkMandatid(mandatid);
    return mandatid;
  }

  public void setMandatid(String mandatid) throws ApplicationException
  {
    checkMandatid(mandatid);
    this.mandatid = mandatid;
  }

  private void checkMandatid(String mandatid) throws ApplicationException
  {
    if (mandatid == null || mandatid.isEmpty()
        || mandatid.length() > MANDATID_MAX_LAENGE)
    {
      throw new ApplicationException("Ungültige Mandats-ID: " + mandatid);
    }
  }

  public Date getMandatdatum() throws ApplicationException
  {
    checkMandatdatum(mandatdatum);
    return mandatdatum;
  }

  public void setMandatdatum(Date mandatdatum) throws ApplicationException
  {
    checkMandatdatum(mandatdatum);
    this.mandatdatum = mandatdatum;
  }

  private void checkMandatdatum(Date mandatdatum) throws ApplicationException
  {
    if (mandatdatum == null || mandatdatum.after(new Date()))
    {
      throw new ApplicationException("Ungültiges Mandatsdatum: " + mandatdatum);
    }
  }

  public String getBic() throws ApplicationException
  {
    IbanUtil.checkBic(bic);
    return bic;
  }

  public void setBic(String bic) throws ApplicationException
  {
    IbanUtil.checkBic(bic);
    this.bic = bic;
  }

  public String getName() throws ApplicationException
  {
    checkName(name);
    return name;
  }

  public void setName(String name) throws ApplicationException
  {
    String konvertiert = SepaZeichensatz.konvertieren(name);
    checkName(konvertiert);
    this.name = konvertiert;
  }

  private void checkName(String name) throws ApplicationException
  {
    if (name == null || name.isEmpty() || name.length() > NAME_MAX_LAENGE)
    {
      throw new ApplicationException("Ungültiger Name: " + name);
    }
  }

  public String getIban() throws ApplicationException
  {
    return IbanUtil.checkIban(iban);
  }

  public void setIban(String iban) throws ApplicationException
  {
    this.iban = IbanUtil.checkIban(iban);
  }

  public String getVerwendungszweck() throws ApplicationException
  {
    checkVerwendungszweck(verwendungszweck);
    return verwendungszweck;
  }

  /**
   * Liefert den zuletzt per {@link #setVerwendungszweck(String)} übergebenen,
   * noch nicht in den SEPA-Zeichensatz transliterierten Verwendungszweck.
   */
  public String getVerwendungszweckOrig() throws ApplicationException
  {
    checkVerwendungszweck(verwendungszweckOrig);
    return verwendungszweckOrig;
  }

  public void setVerwendungszweck(String verwendungszweck)
      throws ApplicationException
  {
    String konvertiert = SepaZeichensatz.konvertieren(verwendungszweck);
    verwendungszwecke = 1;
    checkVerwendungszweck(konvertiert);
    this.verwendungszweck = konvertiert;
    this.verwendungszweckOrig = verwendungszweck;
  }

  private void checkVerwendungszweck(String verwendungszweck)
      throws ApplicationException
  {
    if (verwendungszweck == null || verwendungszweck.isEmpty()
        || verwendungszweck.length() > VERWENDUNGSZWECK_MAX_LAENGE)
    {
      throw new ApplicationException(
          "Ungültiger Verwendungszweck: " + verwendungszweck);
    }
  }

  public BigDecimal getBetrag() throws ApplicationException
  {
    checkBetrag(betrag);
    return betrag;
  }

  // Negative Beträge (Erstattungen) sind bewusst zugelassen, nur exakt 0
  // nicht.
  public void setBetrag(BigDecimal betrag) throws ApplicationException
  {
    checkBetrag(betrag);
    this.betrag = betrag;
  }

  private void checkBetrag(BigDecimal betrag) throws ApplicationException
  {
    if (betrag == null || betrag.compareTo(NULL) == 0)
    {
      throw new ApplicationException("Ungültiger Betrag: " + betrag);
    }
  }

  public void setMandatsequence(MandatSequence sequence)
  {
    this.mandatsequence = sequence;
  }

  public MandatSequence getMandatsequence() throws ApplicationException
  {
    if (mandatsequence == null)
    {
      throw new ApplicationException("Mandatssequenz ist nicht gesetzt");
    }
    return mandatsequence;
  }

  public Date getFaelligkeit() throws ApplicationException
  {
    if (faelligkeit == null)
    {
      throw new ApplicationException("Fälligkeit ist nicht gesetzt");
    }
    return faelligkeit;
  }

  public void setFaelligkeit(Date faelligkeit)
  {
    this.faelligkeit = faelligkeit;
  }

  /**
   * Fasst eine weitere Buchung in diesen Zahler zusammen (kompakte Abbuchung):
   * Betrag wird addiert, Verwendungszweck um den des übergebenen Zahlers samt
   * dessen Betrag ergänzt. Wird der Verwendungszweck dabei länger als
   * {@value #VERWENDUNGSZWECK_MAX_LAENGE} Zeichen, wird er gekürzt und mit
   * "..." abgeschlossen; weitere add()-Aufrufe ändern ihn dann nicht mehr
   * (der Betrag wird trotzdem weiter addiert). Eine negative Gesamtsumme ist
   * nicht erlaubt.
   */
  public void add(Zahler zahler) throws ApplicationException
  {
    if (verwendungszweck == null)
    {
      verwendungszweck = "";
      verwendungszweckOrig = "";
      verwendungszwecke = 1;
    }

    if (verwendungszwecke == 1)
    {
      verwendungszweck += " " + getBetrag();
    }
    verwendungszwecke++;

    betrag = betrag.add(zahler.getBetrag());
    if (betrag.compareTo(NULL) < 0)
    {
      throw new ApplicationException("Ungültiger Betrag: " + betrag);
    }

    if (verwendungszweck.length() == VERWENDUNGSZWECK_MAX_LAENGE
        && verwendungszweck.endsWith("..."))
    {
      return;
    }

    String zusammengefasst = verwendungszweck + ", "
        + zahler.getVerwendungszweck() + " " + zahler.getBetrag();
    if (zusammengefasst.length() > VERWENDUNGSZWECK_MAX_LAENGE)
    {
      zusammengefasst = zusammengefasst.substring(0,
          VERWENDUNGSZWECK_MAX_LAENGE - 3) + "...";
    }
    verwendungszweck = zusammengefasst;
  }

  @Override
  public String toString()
  {
    try
    {
      return MessageFormat.format(
          "Zahler: Name={0}, IBAN={1}, BIC={2}, Verwendungszweck={3}, "
              + "Betrag={4}, Mandatdatum={5}, Mandatreferenz={6}",
          getName(), getIban(), getBic(), getVerwendungszweck(), getBetrag(),
          getMandatdatum(), getMandatid());
    }
    catch (ApplicationException e)
    {
      return e.getMessage();
    }
  }
}
