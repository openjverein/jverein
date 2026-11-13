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
package de.jost_net.JVerein.util;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;

import de.speedbanking.bankdata.BankData;
import de.speedbanking.bankdata.BankDataLookup;
import de.speedbanking.bic.Bic;
import de.speedbanking.bic.InvalidBicException;
import de.speedbanking.checkdigit.de.CheckDigitResult;
import de.speedbanking.checkdigit.de.GermanAccountCheckDigit;
import de.speedbanking.iban.Iban;
import de.speedbanking.iban.IbanConfig;
import de.speedbanking.iban.IbanValidationError;
import de.speedbanking.iban.InvalidIbanException;
import de.speedbanking.util.Country;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * IBAN-/BIC-Prüfung und Bankstammdaten-Lookup auf Basis von "iban-commons",
 * "iban-commons-de-checkdigit" und "iban-commons-bankdata" - Ersatz für
 * OBanToos {@code IBAN}, {@code BIC} und {@code Banken}.
 * <p>
 * Die drei iban-commons-Module decken zusammen ab, was OBanToo bisher geleistet
 * hat: strukturelle IBAN-Prüfung und -Erzeugung ({@code iban-commons}), die
 * deutsche Kontonummer-Prüfziffer je BLZ ({@code iban-commons-de-checkdigit})
 * und der BLZ-/BIC-Bankstammdaten-Lookup, multi-country statt nur für
 * Deutschland wie bei OBanToo ({@code iban-commons-bankdata}).
 *
 * @since 4.3.0
 */
public final class IbanUtil
{
  static
  {
    // Einmalig vor der ersten Nutzung von iban-commons: Leerzeichen in der
    // Eingabe tolerieren und, soweit iban-commons das für das jeweilige Land
    // unterstützt, dessen eigene IBAN-interne Prüfziffer (NCD) prüfen bzw.
    // berechnen.
    IbanConfig.configure(IbanConfig.builder().allowSpace(true).validateNcd(true)
        .calculateNcd(true).build());
  }

  private IbanUtil()
  {
    throw new UnsupportedOperationException(String.format(
        "Utility class %s cannot be instantiated", getClass().getSimpleName()));
  }

  /**
   * Prüft die IBAN auf Gültigkeit und liefert sie normalisiert zurück.
   * <p>
   * Geprüft werden die IBAN-Struktur (Länge, Länderkennzeichen, Prüfsumme nach
   * ISO 7064 MOD 97-10) sowie, bei einer deutschen IBAN, zusätzlich die
   * Bankleitzahl-abhängige Kontonummer-Prüfziffer (siehe
   * {@link #checkDeutschePruefziffer(String, String, String)}). Die Eingabe
   * darf beliebigen Leerraum (Leerzeichen, Tabs, Zeilenumbrüche) enthalten,
   * z.B. aus einer gruppierten Anzeige ("DE89 3704 0044 0532 0130 00") oder
   * copy&amp;paste - dieser wird vor der Prüfung vollständig entfernt.
   *
   * @param iban die IBAN, ggf. mit Leerraum.
   * @return die normalisierte IBAN (ohne Leerraum, Länderkennzeichen groß).
   * @throws ApplicationException wenn die IBAN nicht korrekt ist; die Meldung
   *         ist bereits für die Anzeige im GUI aufbereitet.
   */
  public static String checkIban(String iban) throws ApplicationException
  {
    String bereinigt = StringUtils.deleteWhitespace(StringUtils.trimToEmpty(iban));
    try
    {
      Iban i = parse(bereinigt);
      if (Country.DE.getCode().equals(i.getCountryCode()))
      {
        checkDeutschePruefziffer(i.getBankCode(), i.getAccountNumber(), bereinigt);
      }
      return i.toString();
    }
    catch (InvalidIbanException ex)
    {
      throw new ApplicationException(meldung(bereinigt, ex.getReason()));
    }
  }

  /**
   * Prüft die BIC auf syntaktische Gültigkeit (8 oder 11 Zeichen, gültiges
   * Länderkennzeichen). Ob tatsächlich eine Bank mit dieser BIC existiert,
   * wird nicht geprüft - dafür siehe {@link #getBankname(String)}.
   *
   * @param bic die BIC.
   * @throws ApplicationException wenn die BIC nicht korrekt ist.
   */
  public static void checkBic(String bic) throws ApplicationException
  {
    try
    {
      Bic.validate(StringUtils.trimToEmpty(bic));
    }
    catch (InvalidBicException ex)
    {
      throw new ApplicationException("Ungültige BIC \"" + bic + "\"");
    }
  }

  /**
   * Ermittelt die BIC zu einer (bereits geprüften) IBAN über die
   * Bankstammdaten von {@code iban-commons-bankdata}. Anders als bei OBanToo
   * ist das nicht auf deutsche IBANs beschränkt, sondern funktioniert für
   * alle von {@code iban-commons-bankdata} unterstützten Länder.
   *
   * @param iban die IBAN.
   * @return die BIC oder NULL, wenn sie nicht ermittelbar ist (z.B. weil das
   *         Land nicht unterstützt wird oder die Bankleitzahl unbekannt ist).
   */
  public static String getBicFuerIban(String iban)
  {
    if (StringUtils.trimToNull(iban) == null)
    {
      return null;
    }
    return BankDataLookup.byIban(iban).map(BankData::getBic).map(Object::toString)
        .orElse(null);
  }

  /**
   * Ermittelt den Namen der Bank zu einer BIC oder (deutschen) BLZ über die
   * Bankstammdaten von {@code iban-commons-bankdata}.
   *
   * @param bicOderBlz die BIC oder eine (rein numerische) deutsche BLZ.
   * @return der Name der Bank oder NULL, wenn nicht ermittelbar.
   */
  public static String getBankname(String bicOderBlz)
  {
    String wert = StringUtils.trimToNull(bicOderBlz);
    if (wert == null)
    {
      return null;
    }

    // Unterscheidung über die Zeichen, nicht die Länge: eine deutsche BLZ ist
    // immer rein numerisch, eine BIC enthält immer Buchstaben (Bankcode und
    // Länderkennzeichen) - auch eine 8-stellige BIC ohne Filialkennzeichen
    // waere sonst von einer 8-stelligen BLZ nicht zu unterscheiden.
    Optional<BankData> bankData = StringUtils.isNumeric(wert)
        ? BankDataLookup.byBankCode(Country.DE.getCode(), wert)
        : BankDataLookup.byBic(wert);

    return bankData.map(BankData::getBankName).orElse(null);
  }

  /**
   * Zerlegt eine IBAN in ihre Bestandteile (Länderkennzeichen, Bankleitzahl,
   * Kontonummer usw.), ohne die deutsche Kontonummer-Prüfziffer zu prüfen -
   * dafür siehe {@link #checkIban(String)}.
   * <p>
   * Alle anderen Stellen in JVerein, die eine {@link Iban} benötigen, sollen
   * diese Methode statt {@link Iban#of(CharSequence)} direkt verwenden, damit
   * der statische Initialisierer dieser Klasse (siehe Klassen-Javadoc)
   * garantiert vor jeder IBAN-Verwendung gelaufen ist.
   *
   * @param iban die IBAN.
   * @return die zerlegte IBAN.
   * @throws InvalidIbanException wenn die IBAN strukturell nicht korrekt ist.
   */
  public static Iban parse(String iban) throws InvalidIbanException
  {
    return Iban.of(iban);
  }

  /**
   * Prüft die deutsche Kontonummer-Prüfziffer per
   * {@link GermanAccountCheckDigit}, sofern für die BLZ ein Prüfzifferverfahren
   * bekannt ist. Ist das Verfahren unbekannt oder in iban-commons-de-checkdigit
   * nicht implementiert, wird das toleriert.
   *
   * @param blz die BLZ.
   * @param konto die Kontonummer.
   * @param iban die zugehörige IBAN, nur für die Fehlermeldung.
   * @throws ApplicationException wenn die Prüfziffer nachweislich falsch ist.
   */
  private static void checkDeutschePruefziffer(String blz, String konto, String iban)
      throws ApplicationException
  {
    BankInfo info = HBCIUtils.getBankInfo(blz);
    String methode = info != null
        ? StringUtils.trimToNull(info.getChecksumMethod())
        : null;
    if (methode == null)
    {
      return;
    }

    CheckDigitResult result;
    try
    {
      result = GermanAccountCheckDigit.verify(methode, blz, konto);
    }
    catch (IllegalArgumentException e)
    {
      Logger.warn("Prüfzifferverfahren " + methode
          + " nicht implementiert, wird toleriert");
      return;
    }

    if (result.isChecked() && !result.isValid())
    {
      throw new ApplicationException(
          "IBAN \"" + iban + "\": Prüfziffer der Kontonummer falsch");
    }
  }

  /**
   * Übersetzt einen Validierungsfehler von iban-commons in eine für die
   * JVerein-GUI passende deutsche Fehlermeldung.
   *
   * @param wert die geprüfte IBAN (oder BLZ+Konto), nur für die Fehlermeldung.
   * @param reason der Fehlergrund.
   * @return die deutsche Fehlermeldung.
   */
  private static String meldung(String wert, IbanValidationError reason)
  {
    return switch (reason)
    {
      case EMPTY -> "Bitte geben Sie eine IBAN ein";
      case INCORRECT_LENGTH, INCORRECT_LENGTH_COUNTRY ->
          "IBAN \"" + wert + "\": Länge ungültig";
      case ILLEGAL_CHARACTERS ->
          "IBAN \"" + wert + "\": enthält ungültige Zeichen";
      case INVALID_COUNTRY -> "IBAN \"" + wert + "\": Land unbekannt";
      case INVALID_CHECK_DIGITS ->
          "IBAN \"" + wert + "\": Prüfziffer ungültig";
      case INVALID_STRUCTURE ->
          "IBAN \"" + wert + "\": Aufbau für das Land ungültig";
      case INVALID_BBAN ->
          "IBAN \"" + wert + "\": Bankverbindung ungültig aufgebaut";
      case INVALID_BANK_CODE ->
          "IBAN \"" + wert + "\": Bankleitzahl ungültig aufgebaut";
      case INVALID_BRANCH_CODE ->
          "IBAN \"" + wert + "\": Filialnummer ungültig aufgebaut";
      case INVALID_ACCOUNT_NUMBER ->
          "IBAN \"" + wert + "\": Kontonummer ungültig aufgebaut";
      case INVALID_CHECKSUM ->
          "IBAN \"" + wert + "\": Prüfziffer der Kontonummer falsch";
      default -> "IBAN \"" + wert + "\": IBAN-Regel unbekannt";
    };
  }
}
