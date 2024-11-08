package de.jost_net.JVerein.util;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedfoto;
import de.jost_net.JVerein.server.AdresstypImpl;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class MitgliedDummy implements Mitglied {

    private final static String PERSONENART = "n";
    private final static String ANREDE = "Herrn";
    private final static String TITEL = "Dr. Dr.";
    private final static String NAME = "Wichtig";
    private final static String VORNAME = "Willi";
    private final static String STRASSE = "Bahnhofstr. 22";
    private final static String ADRESSZUSATZ = "Hinterhof bei Müller";
    private final static String PLZ = "12345";
    private final static String ORT = "Testenhausen";
    private final static String STAAT = "Deutschland";
    private final static String BIC = "XXXXXXXXXXX";
    private final static String IBAN = "DE89370400440532013000";
    private final static Integer ZAHLUNGSWEG = 1;
    private final static Date MANDATDATUM = new Date();
    private final static String MANDATID = "12345";
    private final static Date LETZTE_LASTSCHRIFT = new Date();
    private final static String EXTERNE_MITGLIEDSNUMMER = "123456";
    private final static String ID = "1";
    private final static Zahlungsrhythmus ZAHLUNGSRHYTHMUS = new Zahlungsrhythmus(12);
    private final static Zahlungstermin ZAHLUNGSTERMIN = Zahlungstermin.HALBJAEHRLICH4;
    private final static Integer MANDATVERSION = 1;
    private final static String MAIL = "willi.wichtig@jverein.de";
    private final static String GESCHLECHT = GeschlechtInput.MAENNLICH;
    private final static Date GEBURTSDATUM = toDate("02.03.1980");
    private final static String TELEFON_PRIVAT = "011/123456";
    private final static String TELEFON_DIENSTLICH = "011/123456789";
    private final static String HANDY = "0170/123456789";
    private final static Date EINTRITT = toDate("01.01.2010");
    private final static Beitragsgruppe BEITRAGSGRUPPE = beitragsgruppe();
    private final static double INDIVIDUELLER_BEITRAG = 123.45;
    private final static long ZAHLERID = 123456;
    private final static Date AUSTRITT = toDate("01.01.2025");
    private final static Date KUENDIGUNG = toDate("01.11.2024");
    private final static Date STERBETAG = toDate(("31.12.2024"));
    private final static String VERMERK1 = "Vermerk 1";
    private final static String VERMERK2 = "Vermerk 2";
    private final static Date EINGABEDATUM = toDate("01.02.2010");
    private final static Date LETTZTE_AENDERUNG = new Date();
    private final static boolean IS_ANGEMELDET = true;
    private final static Map<String, String> VARIABLEN = new HashMap<>();
    private final static Adresstyp ADRESSTYP = new Adresstyp() {
        @Override
        public String getBezeichnung() throws RemoteException {
            return "Adresstyp";
        }

        @Override
        public void setBezeichnung(String bezeichnung) throws RemoteException {

        }

        @Override
        public String getBezeichnungPlural() throws RemoteException {
            return "Adresstypen";
        }

        @Override
        public void setBezeichnungPlural(String bezeichnungplural) throws RemoteException {

        }

        @Override
        public int getJVereinid() throws RemoteException {
            return 1;
        }

        @Override
        public void setJVereinid(int jvereinid) throws RemoteException {

        }

        @Override
        public void load(String s) throws RemoteException {

        }

        @Override
        public Object getAttribute(String s) throws RemoteException {
            return null;
        }

        @Override
        public String getAttributeType(String s) throws RemoteException {
            return null;
        }

        @Override
        public String getPrimaryAttribute() throws RemoteException {
            return null;
        }

        @Override
        public DBIterator getList() throws RemoteException {
            return null;
        }

        @Override
        public boolean equals(GenericObject genericObject) throws RemoteException {
            return false;
        }

        @Override
        public void addDeleteListener(Listener listener) throws RemoteException {

        }

        @Override
        public void addStoreListener(Listener listener) throws RemoteException {

        }

        @Override
        public void removeDeleteListener(Listener listener) throws RemoteException {

        }

        @Override
        public void removeStoreListener(Listener listener) throws RemoteException {

        }

        @Override
        public String[] getAttributeNames() throws RemoteException {
            return new String[0];
        }

        @Override
        public String getID() throws RemoteException {
            return "1";
        }

        @Override
        public void store() throws RemoteException, ApplicationException {

        }

        @Override
        public void delete() throws RemoteException, ApplicationException {

        }

        @Override
        public void clear() throws RemoteException {

        }

        @Override
        public boolean isNewObject() throws RemoteException {
            return false;
        }

        @Override
        public void overwrite(DBObject dbObject) throws RemoteException {

        }

        @Override
        public void transactionBegin() throws RemoteException {

        }

        @Override
        public void transactionCommit() throws RemoteException {

        }

        @Override
        public void transactionRollback() throws RemoteException {

        }
    };

    @Override
    public String getPersonenart() throws RemoteException {
        return PERSONENART;
    }

    @Override
    public String getAnrede() throws RemoteException {
        return ANREDE;
    }

    @Override
    public String getTitel() throws RemoteException {
        return TITEL;
    }

    @Override
    public String getName() throws RemoteException {
        return NAME;
    }

    @Override
    public String getVorname() throws RemoteException {
        return VORNAME;
    }

    @Override
    public String getStrasse() throws RemoteException {
        return STRASSE;
    }

    @Override
    public String getAdressierungszusatz() throws RemoteException {
        return ADRESSZUSATZ;
    }

    @Override
    public String getPlz() throws RemoteException {
        return PLZ;
    }

    @Override
    public String getOrt() throws RemoteException {
        return ORT;
    }

    @Override
    public String getStaat() throws RemoteException {
        return STAAT;
    }

    @Override
    public String getBic() throws RemoteException {
        return BIC;
    }

    @Override
    public void setBic(String bic) throws RemoteException {

    }

    @Override
    public String getIban() throws RemoteException {
        return IBAN;
    }

    @Override
    public void setIban(String iban) throws RemoteException {

    }

    @Override
    public Integer getZahlungsweg() throws RemoteException {
        return ZAHLUNGSWEG;
    }

    @Override
    public Date getMandatDatum() throws RemoteException {
        return MANDATDATUM;
    }

    @Override
    public String getMandatID() throws RemoteException {
        return MANDATID;
    }

    @Override
    public Date getLetzteLastschrift() throws RemoteException {
        return LETZTE_LASTSCHRIFT;
    }

    @Override
    public void setExterneMitgliedsnummer(String extnr) throws RemoteException {

    }

    @Override
    public String getExterneMitgliedsnummer() throws RemoteException {
        return EXTERNE_MITGLIEDSNUMMER;
    }

    @Override
    public void setID(String id) throws RemoteException {

    }

    @Override
    public void setAdresstyp(Integer adresstyp) throws RemoteException {

    }

    @Override
    public Adresstyp getAdresstyp() throws RemoteException {
        return ADRESSTYP;
    }

    @Override
    public void setPersonenart(String personenart) throws RemoteException {

    }

    @Override
    public void setAnrede(String anrede) throws RemoteException {

    }

    @Override
    public void setTitel(String titel) throws RemoteException {

    }

    @Override
    public void setName(String name) throws RemoteException {

    }

    @Override
    public void setVorname(String vorname) throws RemoteException {

    }

    @Override
    public void setAdressierungszusatz(String adressierungszusatz) throws RemoteException {

    }

    @Override
    public void setStrasse(String strasse) throws RemoteException {

    }

    @Override
    public void setPlz(String plz) throws RemoteException {

    }

    @Override
    public void setOrt(String ort) throws RemoteException {

    }

    @Override
    public void setStaat(String staat) throws RemoteException {

    }

    @Override
    public void setZahlungsweg(Integer zahlungsweg) throws RemoteException {

    }

    @Override
    public Zahlungsrhythmus getZahlungsrhythmus() throws RemoteException {
        return ZAHLUNGSRHYTHMUS;
    }

    @Override
    public void setZahlungsrhythmus(Integer zahlungsrhythmus) throws RemoteException {

    }

    @Override
    public void setZahlungstermin(Integer zahlungstermin) throws RemoteException {

    }

    @Override
    public Zahlungstermin getZahlungstermin() throws RemoteException {
        return ZAHLUNGSTERMIN;
    }

    @Override
    public void setMandatDatum(Date mandatdatum) throws RemoteException {

    }

    @Override
    public Integer getMandatVersion() throws RemoteException {
        return MANDATVERSION;
    }

    @Override
    public void setMandatVersion(Integer mandatversion) throws RemoteException {

    }

    @Override
    public String getKtoiPersonenart() throws RemoteException {
        return PERSONENART;
    }

    @Override
    public void setKtoiPersonenart(String ktoipersonenart) throws RemoteException {

    }

    @Override
    public String getKtoiAnrede() throws RemoteException {
        return ANREDE;
    }

    @Override
    public void setKtoiAnrede(String ktoianrede) throws RemoteException {

    }

    @Override
    public String getKtoiTitel() throws RemoteException {
        return TITEL;
    }

    @Override
    public void setKtoiTitel(String ktoititel) throws RemoteException {

    }

    @Override
    public String getKtoiName() throws RemoteException {
        return NAME;
    }

    @Override
    public void setKtoiName(String ktoiname) throws RemoteException {

    }

    @Override
    public String getKtoiVorname() throws RemoteException {
        return VORNAME;
    }

    @Override
    public void setKtoiVorname(String ktoivorname) throws RemoteException {

    }

    @Override
    public String getKtoiStrasse() throws RemoteException {
        return STRASSE;
    }

    @Override
    public void setKtoiStrasse(String ktoiStrasse) throws RemoteException {

    }

    @Override
    public String getKtoiAdressierungszusatz() throws RemoteException {
        return ADRESSZUSATZ;
    }

    @Override
    public void setKtoiAdressierungszusatz(String ktoiAdressierungszusatz) throws RemoteException {

    }

    @Override
    public String getKtoiPlz() throws RemoteException {
        return PLZ;
    }

    @Override
    public void setKtoiPlz(String ktoiPlz) throws RemoteException {

    }

    @Override
    public String getKtoiOrt() throws RemoteException {
        return ORT;
    }

    @Override
    public void setKtoiOrt(String ktoiOrt) throws RemoteException {

    }

    @Override
    public String getKtoiStaat() throws RemoteException {
        return STAAT;
    }

    @Override
    public void setKtoiStaat(String ktoiStaat) throws RemoteException {

    }

    @Override
    public String getKtoiEmail() throws RemoteException {
        return MAIL;
    }

    @Override
    public void setKtoiEmail(String ktoiEmail) throws RemoteException {

    }

    @Override
    public String getKtoiGeschlecht() throws RemoteException {
        return GESCHLECHT;
    }

    @Override
    public void setKtoiGeschlecht(String ktoigeschlecht) throws RemoteException {

    }

    @Override
    public String getKontoinhaber(int art) throws RemoteException {
        return null;
    }

    @Override
    public Date getGeburtsdatum() throws RemoteException {
        return GEBURTSDATUM;
    }

    @Override
    public void setGeburtsdatum(Date geburtsdatum) throws RemoteException {

    }

    @Override
    public void setGeburtsdatum(String geburtsdatum) throws RemoteException {

    }

    @Override
    public Integer getAlter() throws RemoteException {
        return null;
    }

    @Override
    public String getGeschlecht() throws RemoteException {
        return GESCHLECHT;
    }

    @Override
    public void setGeschlecht(String geschlecht) throws RemoteException {

    }

    @Override
    public String getTelefonprivat() throws RemoteException {
        return TELEFON_PRIVAT;
    }

    @Override
    public void setTelefonprivat(String telefonprivat) throws RemoteException {

    }

    @Override
    public String getTelefondienstlich() throws RemoteException {
        return TELEFON_DIENSTLICH;
    }

    @Override
    public void setTelefondienstlich(String telefondienstlich) throws RemoteException {

    }

    @Override
    public String getHandy() throws RemoteException {
        return HANDY;
    }

    @Override
    public void setHandy(String handy) throws RemoteException {

    }

    @Override
    public String getEmail() throws RemoteException {
        return MAIL;
    }

    @Override
    public void setEmail(String email) throws RemoteException {

    }

    @Override
    public Date getEintritt() throws RemoteException {
        return EINTRITT;
    }

    @Override
    public void setEintritt(Date eintritt) throws RemoteException {

    }

    @Override
    public void setEintritt(String eintritt) throws RemoteException {

    }

    @Override
    public Beitragsgruppe getBeitragsgruppe() throws RemoteException {
        return BEITRAGSGRUPPE;
    }

    @Override
    public int getBeitragsgruppeId() throws RemoteException {
        return 0;
    }

    @Override
    public void setBeitragsgruppe(Integer beitragsgruppe) throws RemoteException {

    }

    @Override
    public Double getIndividuellerBeitrag() throws RemoteException {
        return INDIVIDUELLER_BEITRAG;
    }

    @Override
    public void setIndividuellerBeitrag(Double individuellerbeitrag) throws RemoteException {

    }

    @Override
    public Long getZahlerID() throws RemoteException {
        return ZAHLERID;
    }

    @Override
    public void setZahlerID(Long id) throws RemoteException {

    }

    @Override
    public Date getAustritt() throws RemoteException {
        return AUSTRITT;
    }

    @Override
    public void setAustritt(Date austritt) throws RemoteException {

    }

    @Override
    public void setAustritt(String austritt) throws RemoteException {

    }

    @Override
    public Date getKuendigung() throws RemoteException {
        return KUENDIGUNG;
    }

    @Override
    public void setKuendigung(Date kuendigung) throws RemoteException {

    }

    @Override
    public void setKuendigung(String kuendigung) throws RemoteException {

    }

    @Override
    public Date getSterbetag() throws RemoteException {
        return STERBETAG;
    }

    @Override
    public void setSterbetag(Date sterbetag) throws RemoteException {

    }

    @Override
    public void setSterbetag(String sterbetag) throws RemoteException {

    }

    @Override
    public String getVermerk1() throws RemoteException {
        return VERMERK1;
    }

    @Override
    public void setVermerk1(String vermerk1) throws RemoteException {

    }

    @Override
    public String getVermerk2() throws RemoteException {
        return VERMERK2;
    }

    @Override
    public void setVermerk2(String vermerk2) throws RemoteException {

    }

    @Override
    public void insert() throws RemoteException, ApplicationException {

    }

    @Override
    public void setEingabedatum() throws RemoteException {

    }

    @Override
    public Date getEingabedatum() throws RemoteException {
        return EINGABEDATUM;
    }

    @Override
    public void setLetzteAenderung() throws RemoteException {

    }

    @Override
    public Date getLetzteAenderung() throws RemoteException {
        return LETTZTE_AENDERUNG;
    }

    @Override
    public Mitgliedfoto getFoto() throws RemoteException {
        return null;
    }

    @Override
    public void setFoto(Mitgliedfoto foto) throws RemoteException {

    }

    @Override
    public boolean isAngemeldet(Date stichtag) throws RemoteException {
        return IS_ANGEMELDET;
    }

    @Override
    public void addVariable(String name, String wert) throws RemoteException {
        VARIABLEN.put(name, wert);
    }

    @Override
    public Map<String, String> getVariablen() throws RemoteException {
        return VARIABLEN;
    }

    @Override
    public void load(String s) throws RemoteException {

    }

    @Override
    public Object getAttribute(String s) throws RemoteException {
        return null;
    }

    @Override
    public String[] getAttributeNames() throws RemoteException {
        return new String[0];
    }

    @Override
    public String getID() throws RemoteException {
        return ID;
    }

    @Override
    public String getAttributeType(String s) throws RemoteException {
        return null;
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException {
        return null;
    }

    @Override
    public DBIterator getList() throws RemoteException {
        return null;
    }

    @Override
    public boolean equals(GenericObject genericObject) throws RemoteException {
        return genericObject instanceof MitgliedDummy;
    }

    @Override
    public void addDeleteListener(Listener listener) throws RemoteException {

    }

    @Override
    public void addStoreListener(Listener listener) throws RemoteException {

    }

    @Override
    public void removeDeleteListener(Listener listener) throws RemoteException {

    }

    @Override
    public void removeStoreListener(Listener listener) throws RemoteException {

    }

    @Override
    public void store() throws RemoteException, ApplicationException {

    }

    @Override
    public void delete() throws RemoteException, ApplicationException {

    }

    @Override
    public void clear() throws RemoteException {

    }

    @Override
    public boolean isNewObject() throws RemoteException {
        return false;
    }

    @Override
    public void overwrite(DBObject dbObject) throws RemoteException {

    }

    @Override
    public void transactionBegin() throws RemoteException {

    }

    @Override
    public void transactionCommit() throws RemoteException {

    }

    @Override
    public void transactionRollback() throws RemoteException {

    }

    private static Date toDate(String datum)
    {
        Date d = null;

        try
        {
            d = new JVDateFormatTTMMJJJJ().parse(datum);
        }
        catch (Exception e)
        {
            //
        }
        return d;
    }

    private static Beitragsgruppe beitragsgruppe() {
        try {
            DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
                    .createList(Beitragsgruppe.class);
            return (Beitragsgruppe) it.next();
        }
        catch (RemoteException rmi) {
            return null;
        }
    }
}
