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

package de.jost_net.JVerein;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.jost_net.JVerein.io.MailSender.IMAPCopyData;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.Altermodel;
import de.jost_net.JVerein.keys.ArbeitsstundenModel;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Einstellung;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.server.EinstellungImpl;
import de.jost_net.JVerein.server.Util;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Diese Klasse speichert einige Einstellungen für dieses Plugin.
 * 
 * @author Heiner Jostkleigrewe
 */
public class Einstellungen
{

  private static JVereinDBService db;

  private static Settings settings = new Settings(Einstellungen.class);

  /**
   * Verschlüsselte Datei für besonders sensible Daten (Passwörter)
   */
  private static Wallet wallet = null;

  /**
   * Our decimal formatter.
   */
  public final static DecimalFormat DECIMALFORMAT = new DecimalFormat(
      "###,###.##");

  /**
   * Int formatter.
   */
  public final static DecimalFormat INTFORMAT = new DecimalFormat(
      "###,###,###");

  /**
   * Our currency name.
   */
  public final static String CURRENCY = "EUR";

  public static Date NODATE = new Date();

  public final static String ZUSATZFELD_PRE = "mitglied_zusatzfeld_";

  public final static String LESEFELD_PRE = "mitglied_lesefelder_";

  private static HashMap<String, String> cache = new HashMap<>();

  private static long loadtime;

  /**
   * Timeout nach dem die Einstellungen neu geladen werden in sekunden
   */
  private static long TIMEOUT = 60;

  static
  {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 1900);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    NODATE.setTime(cal.getTimeInMillis());
    DECIMALFORMAT.setMinimumFractionDigits(2);
    DECIMALFORMAT.setMaximumFractionDigits(2);
    try
    {
      loadEinstellungen();
      try
      {
        // Wir nehmen EinstellungImpl.class da es vorher darin war, so ist keine
        // Migration notwendig.
        wallet = new Wallet(EinstellungImpl.class);
      }
      catch (Exception e)
      {
        Logger.error("Erstellen des Wallet-Objekts fehlgeschlagen");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim laden der Einstellungen", e);
    }
  }

  /**
   * Settings in die lokale Settings Datei schreiben (nicht in der DB)
   * 
   * @param key
   *          der Settings-Key
   * @param value
   *          der zu speichernde Wert
   */
  public static void setSettingBoolean(String key, boolean value)
  {
    settings.setAttribute(key, value);
  }

  /**
   * Settings in die lokale Settings Datei schreiben (nicht in der DB)
   * 
   * @param key
   *          der Settings-Key
   * @param value
   *          der zu speichernde Wert
   */
  public static boolean getSettingBoolean(String key, boolean def)
  {
    return settings.getBoolean(key, def);
  }

  /**
   * List die Settings aus der lokalen Datei.
   * 
   * @param key
   *          der zu lesende Settings-Key
   * @param value
   */
  public static void setSettingInt(String key, int value)
  {
    settings.setAttribute(key, value);
  }

  /**
   * List die Settings aus der lokalen Datei.
   * 
   * @param key
   *          der zu lesende Settings-Key
   * @param value
   */
  public static int getSettingInt(String key, int def)
  {
    return settings.getInt(key, def);
  }

  /**
   * Alle Einstellungen (key, Type, Default)
   */
  public enum Property
  {
    // Allgemein
    NAME("name", String.class,
        "Vereinsname fehlt! Unter Administration | Einstellungen erfassen."),
    STRASSE("strasse", String.class, ""),
    PLZ("plz", String.class, ""),
    ORT("ort", String.class, ""),
    STAAT("staat", String.class, "DE"),
    BIC("bic", String.class, ""),
    IBAN("iban", String.class, ""),
    GLAEUBIGERID("glaeubigerid", String.class, ""),
    USTID("ustid", String.class, ""),
    GEBURTSDATUMPFLICHT("geburtsdatumpflicht", Boolean.class, "1"),
    NICHTMITGLIEDGEBURTSDATUMPFLICHT("nichtmitgliedgeburtsdatumpflicht",
        Boolean.class, "0"),
    EINTRITTSDATUMPFLICHT("eintrittsdatumpflicht", Boolean.class, "1"),
    KURSTEILNEHMERGEBPFLICHT("kursteilnehmergebpflicht", Boolean.class, "0"),
    KURSTEILNEHMERGESPFLICHT("kursteilnehmergespflicht", Boolean.class, "0"),
    NICHTMITGLIEDPFLICHTEIGENSCHAFTEN("nichtmitgliedpflichteigenschaften",
        Boolean.class, "1"),
    JURISTISCHEPERSONPFLICHTEIGENSCHAFTEN(
        "juristischepersonpflichteigenschaften", Boolean.class, "1"),

    // Anzeige
    STERBEDATUM("sterbedatum", Boolean.class, "0"),
    KOMMUNIKATIONSDATEN("kommunikationsdaten", Boolean.class, "1"),
    SEKUNDAEREBEITRAGSGRUPPEN("sekundaerebeitragsgruppen", Boolean.class, "0"),
    ZUSATZBETRAG("zusatzabbuchung", Boolean.class, "0"),
    VERMERKE("vermerke", Boolean.class, "1"),
    WIEDERVORLAGE("wiedervorlage", Boolean.class, "0"),
    KURSTEILNEHMER("kursteilnehmer", Boolean.class, "0"),
    LEHRGAENGE("lehrgaenge", Boolean.class, "0"),
    JURISTISCHEPERSONEN("juristischepersonen", Boolean.class, "0"),
    MITGLIEDFOTO("mitgliedfoto", Boolean.class, "0"),
    FAMILIENBEITRAG("familienbeitrag", Boolean.class, "0"),
    ANLAGENKONTEN("anlagenkonten", Boolean.class, "0"),
    RUECKLAGENKONTEN("ruecklagenkonten", Boolean.class, "0"),
    VERBINDLICHKEITEN_FORDERUNGEN("verbindlichkeitenforderungen", Boolean.class,
        "0"),
    MITTELVERWENDUNG("mittelverwendung", Boolean.class, "0"),
    PROJEKTEANZEIGEN("projekteanzeigen", Boolean.class, "0"),
    SPENDENBESCHEINIGUNGENANZEIGEN("spendenbescheinigungenanzeigen",
        Boolean.class, "0"),
    RECHNUNGENANZEIGEN("rechnungenanzeigen", Boolean.class, "0"),
    // Anzeige 2.Spalte
    USELESEFELDER("uselesefelder", Boolean.class, "0"),
    USEZUSATZFELDER("usezusatzfelder", Boolean.class, "0"),
    ZUSATZADRESSEN("zusatzadressen", Boolean.class, "0"),
    AUSLANDSADRESSEN("auslandsadressen", Boolean.class, "0"),
    ARBEITSEINSATZ("arbeitseinsatz", Boolean.class, "0"),
    DOKUMENTENSPEICHERUNG("dokumentenspeicherung", Boolean.class, "0"),
    INDIVIDUELLEBEITRAEGE("individuellebeitraege", Boolean.class, "0"),
    EXTERNEMITGLIEDSNUMMER("externemitgliedsnummer", Boolean.class, "0"),
    MITGLIEDSNUMMERANZEIGEN("nummeranzeigen", Boolean.class, "0"),
    SUMMENANLAGENKONTO("summenanlagenkonto", Boolean.class, "0"),
    ALTERSMODEL("altermodel", Integer.class,
        ((Integer) Altermodel.AKTUELLES_DATUM).toString()),
    BUCHUNGBUCHUNGSARTAUSWAHL("buchungbuchungsartauswahl", Integer.class,
        ((Integer) AbstractInputAuswahl.SearchInput).toString()),
    BUCHUNGSARTSORT("buchungsartsort", Integer.class, "1"),
    MITGLIEDAUSWAHL("mitgliedauswahl", Integer.class,
        ((Integer) AbstractInputAuswahl.SearchInput).toString()),
    AFAINJAHRESABSCHLUSS("afainjahresabschluss", Boolean.class, "1"),
    UNTERDRUECKUNGOHNEBUCHUNG("unterdrueckungohnebuchung", Boolean.class, "1"),
    UNTERDRUECKUNGKONTEN("unterdrueckungkonten", Integer.class, "2"),
    UNTERDRUECKUNGLAENGE("unterdrueckunglaenge", Integer.class, "0"),

    // Abrechnung
    BEITRAGSMODEL("beitragsmodel", Integer.class,
        ((Integer) Beitragsmodel.GLEICHERTERMINFUERALLE.getKey()).toString()),
    ZAHLUNGSRHYTMUS("zahlungsrhytmus", Integer.class, "12"),
    ZAHLUNGSWEG("zahlungsweg", Integer.class, "1"),
    SEPADATUMOFFSET("sepadatumoffset", Integer.class, "0"),
    DEFAULTLAND("defaultland", String.class, "DE"),
    SEPAVERSION("sepaversion", String.class, "pain.008.001.01.xsd"),
    CT1SEPAVERSION("ct1sepaversion", String.class, "pain.001.001.01.xsd"),
    VERRECHNUNGSKONTOID("verrechnungskonto", Integer.class, ""),
    ARBEITSSTUNDENMODEL("arbeitsmodel", Integer.class,
        ((Integer) ArbeitsstundenModel.STANDARD).toString()),
    BEITRAGALTERSSTUFEN("beitragaltersstufen", String.class, "0-99"),
    ABRLABSCHLIESSEN("abrlabschliessen", Boolean.class, "0"),
    SEPAMANDATIDSOURCE("mandatid_source", Integer.class,
        ((Integer) SepaMandatIdSource.DBID).toString()),
    ZUSATZBETRAGAUSGETRETENE("zusatzbetragausgetretene", Boolean.class, "0"),
    KEINEISTBUCHUNGBEILASTSCHRIFT("keineistbuchungbeilastschrift",
        Boolean.class, "0"),

    // Dateinamen
    DATEINAMENMUSTER("dateinamenmuster", String.class, "a$s$-d$-z$"),
    VORLAGENCSVVERZEICHNIS("vorlagencsvverzeichnis", String.class, ""),

    // Spendenbescheinigung
    FINANZAMT("finanzamt", String.class, ""),
    STEUERNUMMER("steuernummer", String.class, ""),
    BESCHEIDDATUM("bescheiddatum", Date.class, ""),
    VORLAEUFIG("vorlaeufig", Boolean.class, "0"),
    VERANLAGUNGVON("veranlagungvon", Date.class, ""),
    VERANLAGUNGBIS("veranlagungbis", Date.class, ""),
    BEGUENSTIGTERZWECK("beguenstigterzweck", String.class, ""),
    MITGLIEDSBETRAEGE("mitgliedsbeitraege", Boolean.class, "0"),
    SPENDENBESCHEINIGUNGMINBETRAG("spendenbescheinigungminbetrag", Double.class,
        "0.01"),
    SPENDENBESCHEINIGUNGVERZEICHNIS("spendenbescheinigungverzeichnis",
        String.class, ""),
    SPENDENBESCHEINIGUNGPRINTBUCHUNGSART("spendenbescheinigungprintbuchungsart",
        Boolean.class, "0"),
    UNTERSCHRIFTDRUCKEN("unterschriftdrucken", Boolean.class, "0"),
    UNTERSCHRIFT("unterschrift", String.class, ""),

    // Buchführung
    BEGINNGESCHAEFTSJAHR("beginngeschaeftsjahr", String.class, "01.01."),
    AFARESTWERT("afarestwert", Double.class, "1"),
    AUTOBUCHUNGUEBERNAHME("autobuchunguebernahme", Boolean.class, "1"),
    AUTOMATISCHEBUCHUNGSKORREKTURHIBISCUS("autobuchungskorrekturhibiscus",
        Boolean.class, "1"),

    KONTONUMMERINBUCHUNGSLISTE("kontonummer_in_buchungsliste", Boolean.class,
        "0"),
    OPTIERT("optiert", Boolean.class, "0"),
    OPTIERTPFLICHT("optiertpflicht", Boolean.class, "0"),
    STEUERINBUCHUNG("steuerinbuchung", Boolean.class, "0"),
    BUCHUNGSKLASSEINBUCHUNG("bkinbuchung", Boolean.class, "0"),
    SPLITPOSITIONZWECK("splitpositionzweck", Boolean.class, "0"),
    GEPRUEFTSYNCHRONISIEREN("geprueftsynchronisieren", Boolean.class, "0"),

    // Rechnung
    RECHNUNGTEXTABBUCHUNG("rechnungtextabbuchung", String.class,
        "Der Betrag wird vom Konto ${IBAN}, (BIC ${BIC}) abgebucht."),
    RECHNUNGTEXTUEBERWEISUNG("rechnungtextueberweisung", String.class,
        "Bitte überweisen Sie den Betrag auf das angegebene Konto."),
    RECHNUNGTEXTBAR("rechnungtextbar", String.class,
        "Bitte zahlen Sie den Betrag auf das angegebene Konto ein."),
    ZAEHLERLAENGE("zaehlerlaenge", Integer.class, "5"),
    QRCODESIZEINMM("qrcodesizemm", Integer.class, "20"),
    QRCODETEXT("qrcodetext", String.class, "Mitgliedsbeitrag"),
    QRCODEFESTERTEXT("qrcodeptext", Boolean.class, "1"),
    QRCODESNGLLINE("qrcodesngl", Boolean.class, "1"),
    QRCODEDATUM("qrcodepdate", Boolean.class, "0"),
    QRCODERENU("qrcodeprenum", Boolean.class, "0"),
    QRCODEMEMBER("qrcodepmnum", Boolean.class, "0"),
    QRCODEINFOM("qrcodeinfom", String.class, "Vielen Dank!"),
    QRCODEKUERZEN("qrcodekuerzen", Boolean.class, "0"),
    QRCODEINTRO("qrcodeintro", String.class,
        "Bequem bezahlen mit Girocode. Einfach mit der Banking-App auf dem Handy abscannen."),

    // Mail
    SMTPSERVER("smtp_server", String.class, ""),
    SMTPPORT("smtp_port", String.class, ""),
    SMTPAUTHUSER("smtp_auth_user", String.class, ""),
    SMTPFROMADDRESS("smtp_from_address", String.class, ""),
    SMTPFROMANZEIGENAME("smtp_from_anzeigename", String.class, ""),
    SMTPSSL("smtp_ssl", Boolean.class, "0"),
    SMTPSTARTTLS("smtp_starttls", Boolean.class, "0"),
    MAILVERZOEGERUNG("mailverzoegerung", Integer.class, "5"),
    MAILALWAYSCC("mail_always_cc", String.class, ""),
    MAILALWAYSBCC("mail_always_bcc", String.class, ""),
    COPYTOIMAPFOLDER("copy_to_imap_folder", Boolean.class, "0"),
    IMAPHOST("imap_host", String.class, ""),
    IMAPPORT("imap_port", String.class, ""),
    IMAPAUTHUSER("imap_auth_user", String.class, ""),
    IMAPSSL("imap_ssl", Boolean.class, "0"),
    IMAPSTARTTLS("imap_starttls", Boolean.class, "0"),
    IMAPSENTFOLDER("imap_sent_folder", String.class, "Sent"),
    MAILSIGNATUR("mailsignatur", String.class, ""),
    ANHANGSPEICHERN("anhangspeichern", Boolean.class, "1"),

    // Statistik
    ALTERSGRUPPEN("altersgruppen", String.class,
        "1-5,6-10,11-17,18-25,26-50,50-100"),
    JUBILARSTARTALTER("jubilarstartalter", Integer.class, "0"),
    JUBILAEEN("jubilaeen", String.class, "10,25,40,50"),
    ALTERSJUBILAEEN("altersjubilaeen", String.class,
        "50,60,65,70,75,80,85,90,95,100");

    private final String key;

    private final Class<?> type;

    private final String defaultValue;

    Property(String key, Class<?> type, String defaultValue)
    {
      this.key = key;
      this.type = type;
      this.defaultValue = defaultValue;
    }

    public String getKey()
    {
      return key;
    }

    public Class<?> getType()
    {
      return type;
    }

    public String getDefault()
    {
      return defaultValue;
    }

    public static Property getByKey(String key)
    {
      for (Property prop : Property.values())
      {
        if (prop.getKey() == key)
        {
          return prop;
        }
      }
      return null;
    }

    @Override
    public String toString()
    {
      return getKey();
    }
  }

  /**
   * Holt eine Einstellung
   * 
   * @param prop
   *          die Einstellung
   * @return Eigenschaft vom typ String, Integer, Double, Boolean oder Date (Wie
   *         in der Einstellung-Property definiert)
   * @throws RemoteException
   */
  public static Object getEinstellung(Property prop) throws RemoteException
  {
    // Nach dem Timeout einstellungen neu laden. So werden auch Änderungen von
    // Außerhalb gelesen.
    if (System.currentTimeMillis() > loadtime + TIMEOUT * 1000)
    {
      loadEinstellungen();
    }
    String value = cache.get(prop.getKey());
    if (value == null)
    {
      value = prop.getDefault();
    }
    if (value == null)
    {
      return null;
    }
    switch (prop.getType().getSimpleName())
    {
      case "Integer":
        if (value.isBlank())
        {
          return null;
        }
        return Integer.valueOf(value);
      case "Double":
        if (value.isBlank())
        {
          return null;
        }
        return Double.valueOf(value);
      case "String":
        return value;
      case "Boolean":
        return Util.getBoolean(value);
      case "Date":
        if (value.isBlank())
        {
          return null;
        }
        try
        {
          return new JVDateFormatTTMMJJJJ().parse(value);
        }
        catch (ParseException e)
        {
          Logger.error("Kann Datum nicht parsen", e);
          return null;
        }
      default:
        throw new RemoteException(
            "Kann Einstellung nicht lesen, Type nicht implementiert: "
                + prop.getType().getSimpleName());
    }
  }

  /**
   * Setzt die Einstellung und speichert sie direkt in der DB. Falls meherer
   * Einstellungen gespeicher werden, sollte eine Transaction verwendet werden.
   * 
   * @param prop
   *          die Einstellung
   * @param value
   *          der neue Wert
   * @throws ApplicationException
   * @throws RemoteException
   */
  public static void setEinstellung(Property prop, Object value)
      throws ApplicationException, RemoteException
  {
    if (value != null && !value.getClass().isAssignableFrom(prop.getType()))
    {
      String text = "Einstellung hat den falschen Type. Eigenschaft: "
          + prop.getKey() + " Type:" + value.getClass() + " Statt Type:"
          + prop.getType();
      Logger.error(text);
      throw new ApplicationException(text);
    }

    DBIterator<Einstellung> einstellungIt = getDBService()
        .createList(Einstellung.class);
    einstellungIt.addFilter("name = ?", prop.getKey());
    Einstellung e;
    if (einstellungIt.hasNext())
    {
      e = einstellungIt.next();
    }
    else
    {
      // Neue Einstellung erstellen
      e = (Einstellung) getDBService().createObject(Einstellung.class, null);
      e.setKey(prop.getKey());
    }
    if (value instanceof Date)
    {
      value = new JVDateFormatTTMMJJJJ().format((Date) value);
    }
    e.setValue(value);
    e.store();

    // Cache aktualisieren
    cache.put(prop.getKey(), value == null ? null : value.toString());
  }

  /**
   * Die Mailsignatur holen
   * 
   * @param separator
   *          wenn true wird der Separator (-- \n) automatisch eingefügt wenn
   *          noch nicht vorhanden
   * @return die Signatur
   * @throws RemoteException
   */
  public static String getMailSignatur(Boolean separator) throws RemoteException
  {
    String signatur = (String) getEinstellung(Property.MAILSIGNATUR);
    if (signatur == null || signatur.trim().length() == 0)
    {
      return "";
    }
    // Siehe RFC 3676, 4.3. Usenet Signature Convention
    if (separator && !signatur.startsWith("-- \n"))
    {
      return "\n-- \n" + signatur;
    }
    return signatur;
  }

  /**
   * Lädt die Einstellungen aus der DB und speichert sie in der Cache-Map
   */
  public static void loadEinstellungen() throws RemoteException
  {
    DBIterator<DBObject> einstellungen = getDBService()
        .createList(Einstellung.class);
    while (einstellungen.hasNext())
    {
      Einstellung e = (Einstellung) einstellungen.next();
      if (cache == null)
      {
        cache = new HashMap<>();
      }
      cache.put(e.getKey(), e.getValue());
    }
    loadtime = System.currentTimeMillis();
  }

  /**
   * Holt den Hibiscus DBService um auf die Hibiscus Konten etc. zugreifen zu
   * können
   * 
   * @return
   * @throws RemoteException
   */
  public static HBCIDBService getHibiscusDBService() throws RemoteException
  {
    return de.willuhn.jameica.hbci.Settings.getDBService();
  }

  /**
   * SMTP Passwort aus dem Wallet lesen.
   * 
   * @return
   * @throws RemoteException
   */
  public static String getSmtpAuthPwd() throws RemoteException
  {
    try
    {
      Serializable pwd = wallet.get("smtp_auth_pwd");
      return (String) pwd;
    }
    catch (Exception e)
    {
      String text = "Fehler beim Auslesen des SMTP-Passworts aus dem Wallet";
      Logger.error(text, e);
      throw new RemoteException(text, e);
    }
  }

  /**
   * SMTP Passwort ins Wallet schreiben.
   * 
   * @param pwd
   * @throws RemoteException
   */
  public static void setSmtpAuthPwd(String pwd) throws RemoteException
  {
    try
    {
      wallet.set("smtp_auth_pwd", pwd);
    }
    catch (Exception e)
    {
      String text = "Fehler beim Schreiben des SMTP-Passworts in das Wallet";
      Logger.error(text, e);
      throw new RemoteException(text, e);
    }
  }

  /**
   * IMAP Passwort aus dem Wallet lesen.
   * 
   * @return
   * @throws RemoteException
   */
  public static String getImapAuthPwd() throws RemoteException
  {
    try
    {
      Serializable pwd = wallet.get("imap_auth_pwd");
      return (String) pwd;
    }
    catch (Exception e)
    {
      String text = "Fehler beim Auslesen des IMAP-Passworts aus dem Wallet";
      Logger.error(text, e);
      throw new RemoteException(text, e);
    }
  }

  /**
   * IMAP Passwort ins Wallet schreiben.
   * 
   * @param pwd
   * @throws RemoteException
   */
  public static void setImapAuthPwd(String pwd) throws RemoteException
  {
    try
    {
      wallet.set("imap_auth_pwd", pwd);
    }
    catch (Exception e)
    {
      String text = "Fehler beim Schreiben des IMAP-Passworts in das Wallet";
      Logger.error(text, e);
      throw new RemoteException(text, e);
    }
  }

  /**
   * Small helper function to get the database service.
   * 
   * @return db service.
   * @throws RemoteException
   */
  public static JVereinDBService getDBService() throws RemoteException
  {
    if (db != null)
      return db;

    try
    {
      // We have to ask Jameica's ServiceFactory.
      // If we are running in Client/Server mode and we are the
      // client, the factory returns the remote dbService from the
      // Jameica server.
      // The name and class of the service is defined in plugin.xml
      db = (JVereinDBService) Application.getServiceFactory()
          .lookup(JVereinPlugin.class, "database");
      return db;
    }
    catch (Exception e)
    {
      throw new RemoteException("error while getting database service", e);
    }
  }

  /**
   * Prueft die Gueltigkeit der BLZ/Kontonummer-Kombi anhand von Pruefziffern.
   * 
   * @param blz
   * @param kontonummer
   * @return true, wenn die Kombi ok ist.
   */
  public final static boolean checkAccountCRC(String blz, String kontonummer)
  {
    QueryMessage q = new QueryMessage(blz + ":" + kontonummer);
    Application.getMessagingFactory()
        .getMessagingQueue("hibiscus.query.accountcrc").sendSyncMessage(q);
    Object data = q.getData();

    // Wenn wir keine oder eine ungueltige Antwort erhalten haben,
    // ist Hibiscus vermutlich nicht installiert. In dem Fall
    // lassen wir die Konto/BLZ-Kombination mangels besserer
    // Informationen zu
    return (data == null || !(data instanceof Boolean)) ? true
        : ((Boolean) data).booleanValue();
  }

  /**
   * Liefert den Namen der Bank zu einer BLZ.
   * 
   * @param blz
   *          BLZ.
   * @return Name der Bank oder Leerstring.
   */
  public final static String getNameForBLZ(String blz)
  {
    QueryMessage q = new QueryMessage(blz);
    Application.getMessagingFactory()
        .getMessagingQueue("hibiscus.query.bankname").sendSyncMessage(q);
    Object data = q.getData();

    // wenn wir nicht zurueckerhalten haben oder die Nachricht
    // noch unveraendert die BLZ enthaelt, liefern wir einen
    // Leerstring zurueck
    return (data == null || data.equals(blz)) ? "" : data.toString();
  }

  /**
   * Prueft, ob die MD5-Checksumme der Datenbank geprueft werden soll.
   * 
   * @return true, wenn die Checksumme geprueft werden soll.
   */
  public static boolean getCheckDatabase()
  {
    return settings.getBoolean("checkdatabase", true);
  }

  /**
   * Prüft, ob es der Erste Start von JVerein ist
   * 
   * @return
   */
  public static boolean isFirstStart()
  {
    boolean beigen = false;
    boolean bbeitragsgruppe = false;
    try
    {
      DBIterator<Einstellung> st = getDBService().createList(Einstellung.class);
      if (st.size() > 0)
      {
        beigen = true;
      }
      DBIterator<Beitragsgruppe> bg = getDBService()
          .createList(Beitragsgruppe.class);
      if (bg.size() > 0)
      {
        bbeitragsgruppe = true;
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return !beigen || !bbeitragsgruppe;
  }

  /**
   * Get the IMAP folder copy data from Einstellungen
   * 
   * @return IMAP copy data
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static IMAPCopyData getImapCopyData()
      throws RemoteException, ApplicationException
  {
    IMAPCopyData imapCopyData = new IMAPCopyData(
        (Boolean) getEinstellung(Property.COPYTOIMAPFOLDER),
        (String) getEinstellung(Property.IMAPAUTHUSER), getSmtpAuthPwd(),
        (String) getEinstellung(Property.IMAPHOST),
        (String) getEinstellung(Property.IMAPPORT),
        (Boolean) getEinstellung(Property.IMAPSSL),
        (Boolean) getEinstellung(Property.IMAPSTARTTLS),
        (String) getEinstellung(Property.IMAPSENTFOLDER));
    return imapCopyData;
  }
}
