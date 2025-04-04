package de.jost_net.JVerein.io;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.EmailValidator;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MitgliederImport implements Importer
{

  @Override
  public void doImport(Object context, IOFormat format, File file,
      String encoding, ProgressMonitor monitor) throws Exception
  {
    ResultSet results;
    try
    {

      Properties props = new java.util.Properties();
      props.put("separator", ";");
      props.put("suppressHeaders", "false");
      props.put("charset", encoding);
      String path = file.getParent();
      String fil = file.getName();
      int pos = fil.lastIndexOf('.');
      props.put("fileExtension", fil.substring(pos));

      Class.forName("org.relique.jdbc.csv.CsvDriver");
      Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + path,
          props);
      Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      results = stmt.executeQuery("SELECT * FROM " + fil.substring(0, pos));

      /* Zusatzfelder ermitteln und in Liste ablegen */
      DBIterator<Felddefinition> felddefinitionIt = Einstellungen.getDBService()
          .createList(Felddefinition.class);
      LinkedList<Felddefinition> zusfeldList = new LinkedList<>();
      for (int i = 0; i < felddefinitionIt.size(); i++)
      {
        try
        {
          Felddefinition f = (Felddefinition) felddefinitionIt.next();
          results.findColumn("zusatzfeld_" + f.getName());
          zusfeldList.add(f);
        }
        catch (SQLException e)
        {
          // Feld nicht vorhanden
        }
      }

      /* Eigenschaften ermitteln und in Liste ablegen */
      DBIterator<Eigenschaft> eigenschaftenIt = Einstellungen.getDBService()
          .createList(Eigenschaft.class);
      LinkedList<Eigenschaft> eigenschaftList = new LinkedList<>();
      for (int i = 0; i < eigenschaftenIt.size(); i++)
      {
        try
        {
          Eigenschaft e = (Eigenschaft) eigenschaftenIt.next();
          results.findColumn("eigenschaft_" + e.getBezeichnung());
          eigenschaftList.add(e);
        }
        catch (SQLException e)
        {
          // Eigenschaft nicht vorhanden
        }
      }

      /* Sekundaere Beitragsgruppen ermitteln und in Liste ablegen */
      DBIterator<Beitragsgruppe> beitragsgruppenIt = Einstellungen
          .getDBService().createList(Beitragsgruppe.class);
      LinkedList<Beitragsgruppe> sekundaerList = new LinkedList<>();
      for (int i = 0; i < beitragsgruppenIt.size(); i++)
      {
        try
        {
          Beitragsgruppe bg = (Beitragsgruppe) beitragsgruppenIt.next();
          results.findColumn("sekundaer_" + bg.getBezeichnung());
          sekundaerList.add(bg);
        }
        catch (SQLException e)
        {
          // Eigenschaft nicht vorhanden
        }
      }

      DBTransaction.starten();
      int anz = 0;
      while (results.next())
      {
        anz++;
        monitor.setPercentComplete(anz * 10);

        Mitglied m = (Mitglied) Einstellungen.getDBService()
            .createObject(Mitglied.class, null);

        try
        {
          String mitgliedstyp = results.getString("adresstyp");
          if (mitgliedstyp != null && mitgliedstyp.length() != 0)
          {
            try
            {
              Mitgliedstyp mt = (Mitgliedstyp) Einstellungen.getDBService()
                  .createObject(Mitgliedstyp.class, mitgliedstyp);
              m.setMitgliedstyp(Integer.valueOf(mt.getID()));
            }
            catch (ObjectNotFoundException e)
            {
              throw new ApplicationException(
                  "Adresstyp nicht vorhanden: " + mitgliedstyp);
            }
          }
          else
            m.setMitgliedstyp(Mitgliedstyp.MITGLIED);
        }
        catch (SQLException e)
        {
          // Wenn Adresstyp nicht vorhanden speichern wir es als Mitglied
          m.setMitgliedstyp(Mitgliedstyp.MITGLIED);
        }

        try
        {
          String personenart = results.getString("personenart");
          if (personenart != null && personenart.length() != 0)
          {
            m.setPersonenart(personenart);
          }
          else
            m.setPersonenart("N");
        }
        catch (SQLException e)
        {
          // Wenn Personenart nicht vorhanden speichern wir es als natürliche
          // Person
          m.setPersonenart("N");
        }

        try
        {
          String adressierungszusatz = results.getString("adressierungszusatz");
          m.setAdressierungszusatz(adressierungszusatz);
        }
        catch (SQLException e)
        {
          // Optionaler parameter, ignorieren wir
        }

        if (m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
        {
          try
          {
            String eintritt = results.getString("eintritt");
            if (eintritt != null && eintritt.length() != 0)
            {
              try
              {
                m.setEintritt(Datum.toDate(eintritt));
              }
              catch (ParseException e)
              {
                throw new ApplicationException("Zeile " + anz
                    + ": Ungültiges Datumsformat für eintritt: " + eintritt);
              }
            }
            else
              throw new ApplicationException(
                  "Zeile " + anz + ": Mitglied muss ein Eintrittsdatum haben!");

          }
          catch (SQLException e)
          {
            throw new ApplicationException(
                "Mitglied muss ein Eintrittsdatum haben!");
          }
        }

        try
        {
          String austritt = results.getString("austritt");
          if (austritt != null && austritt.length() != 0)
          {
            try
            {
              if (m.getEintritt() == null
                  || Datum.toDate(austritt).before(m.getEintritt()))
                throw new ApplicationException("Zeile " + anz
                    + ": Austritt kann nicht vor Eintritt liegen");
              m.setAustritt(austritt);
            }
            catch (ParseException e)
            {
              throw new ApplicationException("Zeile " + anz
                  + ": Ungültiges Datumsformat für austritt: " + austritt);
            }
          }
        }
        catch (SQLException e)
        {
          // Optionaler parameter, ignorieren wir
        }

        try
        {
          String anrede = results.getString("anrede");
          m.setAnrede(anrede);
        }
        catch (SQLException e)
        {
          // Optionaler parameter, ignorieren wir
        }

        try
        {
          // Beitragsgruppe nur bei Mitgliedern möglich
          if (m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            String beitragsgruppe = results.getString("beitragsgruppe");
            DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
                .createList(Beitragsgruppe.class);
            it.addFilter("bezeichnung = ?", beitragsgruppe);
            it.addFilter("sekundaer = ?", false);
            if (!it.hasNext())
              throw new ApplicationException("Zeile " + anz
                  + ": Beitragsgruppe nicht gefunden: " + beitragsgruppe);
            Beitragsgruppe bg = (Beitragsgruppe) it.next();
            if (it.hasNext())
              throw new ApplicationException("Beitragsgruppe mit dem Namen "
                  + beitragsgruppe + " ist mehrfach vorhanden");
            m.setBeitragsgruppe(bg);
            if (bg.getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
            {
              m.setVollZahlerID(null);
            }
          }
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Beitragsgruppe fehlt");
        }

        try
        {
          if (Einstellungen.getEinstellung().getIndividuelleBeitraege())
          {
            String individuellerBeitrag = results
                .getString("individuellerbeitrag");
            if (individuellerBeitrag != null)
            {
              m.setIndividuellerBeitrag(Double.valueOf(individuellerBeitrag));
            }
            else
            {
              m.setIndividuellerBeitrag(null);
            }
          }
        }
        catch (SQLException e)
        {
          // Optionaler parameter, ignorieren wir
        }

        try
        {
          String zahlungsweg = results.getString("zahlungsweg");
          if (zahlungsweg != null && zahlungsweg.length() != 0)
          {
            if (Zahlungsweg.get(Integer.parseInt(zahlungsweg)) == null)
              throw new ApplicationException(
                  "Zeile " + anz + ": Zahlungsweg ungültig: " + zahlungsweg);
            if (Integer.parseInt(zahlungsweg) == 4 && m.getBeitragsgruppe()
                .getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
              throw new ApplicationException(
                  "Zeile " + anz + ": Zahlungsweg VOLLZAHLER(" + 4
                      + ") nur für Familienangehörige");
            m.setZahlungsweg(Integer.parseInt(zahlungsweg));
          }
          else
          {
            m.setZahlungsweg(Einstellungen.getEinstellung().getZahlungsweg());
          }
        }
        catch (SQLException e)
        {
          // Wenn nicht vorhanden Standartwert aus Einstellungen nehmen
          m.setZahlungsweg(Einstellungen.getEinstellung().getZahlungsweg());
        }

        try
        {
          if (Einstellungen.getEinstellung()
              .getBeitragsmodel() == Beitragsmodel.MONATLICH12631)
          {
            String zahlungsrhythmus = results.getString("zahlungsrhythmus");
            if (zahlungsrhythmus != null && zahlungsrhythmus.length() != 0)
            {
              try
              {
                if (Zahlungsrhythmus
                    .get(Integer.parseInt(zahlungsrhythmus)) == null)
                  throw new ApplicationException("Zeile " + anz
                      + ": Ungültiger Zahlungsrythmus: " + zahlungsrhythmus);
                m.setZahlungsrhythmus(Integer.parseInt(zahlungsrhythmus));
              }
              catch (NumberFormatException e)
              {
                // Eventuell ist es der Text statt der Nummer
                boolean found = false;
                for (Zahlungsrhythmus z : Zahlungsrhythmus.getArray())
                {
                  if (z.getText().toLowerCase()
                      .equals(zahlungsrhythmus.toLowerCase()))
                  {
                    m.setZahlungsrhythmus(z.getKey());
                    found = true;
                  }
                  if (!found)
                    throw new ApplicationException("Zeile " + anz
                        + ": Ungültiger Zahlungsrythmus: " + zahlungsrhythmus);
                }
              }
            }
            else
            {
              m.setZahlungsrhythmus(Zahlungsrhythmus.MONATLICH);
            }
          }
          else
            m.setZahlungsrhythmus(Zahlungsrhythmus.MONATLICH);
        }
        catch (SQLException e)
        {
          // wenn nicht vorhanden Monatlich als default nehmen
          m.setZahlungsrhythmus(Zahlungsrhythmus.MONATLICH);
        }

        try
        {
          if (Einstellungen.getEinstellung()
              .getBeitragsmodel() == Beitragsmodel.FLEXIBEL)
          {
            String zahlungstermin = results.getString("zahlungstermin");
            if (zahlungstermin != null && zahlungstermin.length() != 0)
            {
              if (Zahlungstermin
                  .getByKey(Integer.parseInt(zahlungstermin)) == null)
                throw new ApplicationException("Zeile " + anz
                    + ": Ungültiger Zahlungstermin: " + zahlungstermin);
              m.setZahlungstermin(Integer.parseInt(zahlungstermin));
            }
            else
            {
              m.setZahlungstermin(Zahlungstermin.MONATLICH.getKey());
            }
          }
        }
        catch (SQLException e)
        {
          // wenn nicht vorhanden Monatlich als default nehmen
          m.setZahlungstermin(Zahlungstermin.MONATLICH.getKey());
        }

        try
        {
          String mandatdatum = results.getString("mandatdatum");
          if (mandatdatum != null && mandatdatum.length() != 0)
          {
            m.setMandatDatum(Datum.toDate(mandatdatum));
          }
          else if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            throw new ApplicationException(
                "Zeile " + anz + ": Mandatdatum fehlt");
          }
        }
        catch (SQLException e)
        {
          // Nur bei Zahlungsweg Lastschrift pflicht
          if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            throw new ApplicationException("Mandatdatum fehlt");
          }
        }

        try
        {
          String mandatversion = results.getString("mandatversion");
          if (mandatversion != null && mandatversion.length() != 0)
          {
            m.setMandatVersion(Integer.parseInt(mandatversion));
          }
          else if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            m.setMandatVersion(0);
          }
        }
        catch (SQLException e)
        {
          // Nur bei Zahlungsweg Lastschrift nötig. 0 als default nehmen
          if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            m.setMandatVersion(0);
          }
        }

        try
        {
          String iban = results.getString("iban");
          if (iban != null && iban.length() != 0)
          {
            try
            {
              IBAN i = new IBAN(iban.toUpperCase());
              m.setIban(i.getIBAN());
            }
            catch (SEPAException e)
            {
              if (e.getFehler() == SEPAException.Fehler.UNGUELTIGES_LAND)
                throw new ApplicationException("Zeile " + anz
                    + ": IBAN Ungültiges Land: " + e.getMessage());
              else
                throw new ApplicationException(e.getMessage());
            }
          }
          else if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            throw new ApplicationException("Zeile " + anz + ": IBAN fehlt");
          }
        }
        catch (SQLException e)
        {
          // Nur bei Zahlungsweg Lastschrift nötig
          if (m.getZahlungsweg() == Zahlungsweg.BASISLASTSCHRIFT
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
          {
            throw new ApplicationException("IBAN fehlt");
          }
        }

        try
        {
          String bic = results.getString("bic");
          if (bic != null && bic.length() != 0)
          {
            m.setBic(bic);
          }
          else
          {
            if (m.getBic() == "" && m.getIban() != null && m.getIban().length() > 0)
            {
              IBAN i = new IBAN(m.getIban());
              m.setBic(i.getBIC());
            }
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
          if (m.getBic() == "" && m.getIban() != null && m.getIban().length() != 0)
          {
            IBAN i = new IBAN(m.getIban());
            m.setBic(i.getBIC());
          }
        }

        try
        {
          String email = results.getString("email");
          if (email != null && email.length() != 0)
          {
            if (!EmailValidator.isValid(email))
              throw new ApplicationException(
                  "Zeile " + anz + ": Ungültige Email: " + email);
            m.setEmail(email);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        if (Einstellungen.getEinstellung().getExterneMitgliedsnummer())
        {
          try
          {
            String externemitgliedsnummer = results
                .getString("externemitgliedsnummer");
            if (externemitgliedsnummer != null
                && externemitgliedsnummer.length() != 0)
            {
              m.setExterneMitgliedsnummer(externemitgliedsnummer);
            }
            else
              throw new ApplicationException(
                  "Zeile " + anz + ": Externe Mitgliedsnummer fehlt");
          }
          catch (SQLException e)
          {
            throw new ApplicationException("Externe Mitgliedsnummer fehlt");
          }
        }
        else
        {
          m.setExterneMitgliedsnummer(null);
        }

        try
        {
          String geburtsdatum = results.getString("geburtsdatum");
          if (geburtsdatum != null && geburtsdatum.length() != 0)
          {
            if (Datum.toDate(geburtsdatum).after(new Date()))
              throw new ApplicationException(
                  "Zeile " + anz + ": Geburtsdatum liegt in der Zukunft");
            m.setGeburtsdatum(Datum.toDate(geburtsdatum));
          }
          else if (Einstellungen.getEinstellung().getGeburtsdatumPflicht()
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
            throw new ApplicationException(
                "Zeile " + anz + ": Geburtsdatum fehlt");
        }
        catch (SQLException e)
        {
          if (Einstellungen.getEinstellung().getGeburtsdatumPflicht()
              && m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
            throw new ApplicationException("Geburtsdatum fehlt");
        }

        try
        {
          String geschlecht = results.getString("geschlecht");
          if (geschlecht != null && geschlecht.length() != 0)
          {
            if (!geschlecht.toLowerCase().equals("m")
                && !geschlecht.toLowerCase().equals("w")
                && !geschlecht.toLowerCase().equals("o"))
              throw new ApplicationException(
                  "Zeile " + anz + ": Ungültiges Geschlecht: " + geschlecht);
            m.setGeschlecht(geschlecht.toLowerCase());
          }
          else
            m.setGeschlecht("o");
        }
        catch (SQLException e)
        {
          m.setGeschlecht("o");
        }

        try
        {
          String ktoiadressierungszusatz = results
              .getString("ktoiadressierungszusatz");
          if (ktoiadressierungszusatz != null
              && ktoiadressierungszusatz.length() != 0)
          {
            m.setKtoiAdressierungszusatz(ktoiadressierungszusatz);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoianrede = results.getString("ktoianrede");
          if (ktoianrede != null && ktoianrede.length() != 0)
          {
            m.setKtoiAnrede(ktoianrede);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoiemail = results.getString("ktoiemail");
          if (ktoiemail != null && ktoiemail.length() != 0)
          {
            if (!EmailValidator.isValid(ktoiemail))
              throw new ApplicationException(
                  "Zeile " + anz + ": Ungültige Email: " + ktoiemail);
            m.setKtoiEmail(ktoiemail);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoiname = results.getString("ktoiname");
          if (ktoiname != null && ktoiname.length() != 0)
          {
            m.setKtoiName(ktoiname);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoiort = results.getString("ktoiort");
          if (ktoiort != null && ktoiort.length() != 0)
          {
            m.setKtoiOrt(ktoiort);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoipersonenart = results.getString("ktoipersonenart");
          if (ktoipersonenart != null && ktoipersonenart.length() != 0)
          {
            m.setKtoiPersonenart(ktoipersonenart.substring(0, 1));
          }
          else
          {
            if (m.getPersonenart() == null)
              m.setPersonenart("N");
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoiplz = results.getString("ktoiplz");
          if (ktoiplz != null && ktoiplz.length() != 0)
          {
            m.setKtoiPlz(ktoiplz);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoistaat = results.getString("ktoistaat");
          if (ktoistaat != null && ktoistaat.length() != 0)
          {
            if(Staat.getByText(ktoistaat.toUpperCase()) != null)
            {
              m.setKtoiStaat(Staat.getByText(ktoistaat.toUpperCase()).getKey());
            }
            else if(Staat.getByKey(ktoistaat.toUpperCase()) != null)
            {
              m.setKtoiStaat(ktoistaat.toUpperCase());
            }
            else
            {
              throw new ApplicationException("Zeile " + anz + ": Kontoinhaber Staat nicht erkannt: " + ktoistaat);
            }
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoistrasse = results.getString("ktoistrasse");
          if (ktoistrasse != null && ktoistrasse.length() != 0)
          {
            m.setKtoiStrasse(ktoistrasse);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoititel = results.getString("ktoititel");
          if (ktoititel != null && ktoititel.length() != 0)
          {
            m.setKtoiTitel(ktoititel);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoivorname = results.getString("ktoivorname");
          if (ktoivorname != null && ktoivorname.length() != 0)
          {
            m.setKtoiVorname(ktoivorname);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String ktoigeschlecht = results.getString("ktoigeschlecht");
          if (ktoigeschlecht != null && ktoigeschlecht.length() != 0)
          {
            if (!ktoigeschlecht.toLowerCase().equals("m")
                && !ktoigeschlecht.toLowerCase().equals("w")
                && !ktoigeschlecht.toLowerCase().equals("o"))
              throw new ApplicationException("Zeile " + anz
                  + ": Ungültiges Geschlecht: " + ktoigeschlecht);
            m.setKtoiGeschlecht(ktoigeschlecht);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String kuendigung = results.getString("kuendigung");
          if (kuendigung != null && kuendigung.length() != 0)
          {
            m.setKuendigung(kuendigung);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String sterbetag = results.getString("sterbetag");
          if (sterbetag != null && sterbetag.length() != 0)
          {
            m.setSterbetag(sterbetag);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String name = results.getString("name");
          if (name != null && name.length() != 0)
          {
            m.setName(name);
          }
          else
            throw new ApplicationException("Zeile " + anz + ": Name fehlt");
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Name fehlt");
        }

        try
        {
          String ort = results.getString("ort");
          m.setOrt(ort);
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
          m.setOrt("");
        }

        try
        {
          String plz = results.getString("plz");
          m.setPlz(plz);
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
          m.setPlz("");
        }

        try
        {
          String staat = results.getString("staat");
          if (staat != null && staat.length() != 0)
          {
            if(Staat.getByKey(staat.toUpperCase()) != null)
            {
              m.setStaat(staat.toUpperCase());
            }
            else if(Staat.getByText(staat.toUpperCase()) != null)
            {
              m.setStaat(Staat.getByText(staat.toUpperCase()).getKey());
            }
            else
            {
              throw new ApplicationException("Zeile " + anz + ": Staat nicht erkannt: " + staat);
            }
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String strasse = results.getString("strasse");
          m.setStrasse(strasse);
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
          m.setStrasse("");
        }

        try
        {
          String telefondienstlich = results.getString("telefondienstlich");
          if (telefondienstlich != null && telefondienstlich.length() != 0)
          {
            m.setTelefondienstlich(telefondienstlich);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String telefonprivat = results.getString("telefonprivat");
          if (telefonprivat != null && telefonprivat.length() != 0)
          {
            m.setTelefonprivat(telefonprivat);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String handy = results.getString("handy");
          if (handy != null && handy.length() != 0)
          {
            m.setHandy(handy);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String titel = results.getString("titel");
          if (titel != null && titel.length() != 0)
          {
            m.setTitel(titel);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String vermerk1 = results.getString("vermerk1");
          if (vermerk1 != null && vermerk1.length() != 0)
          {
            m.setVermerk1(vermerk1);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String vermerk2 = results.getString("vermerk2");
          if (vermerk2 != null && vermerk2.length() != 0)
          {
            m.setVermerk2(vermerk2);
          }
        }
        catch (SQLException e)
        {
          // Optionaler Parameter
        }

        try
        {
          String vorname = results.getString("vorname");
          if (vorname != null && vorname.length() != 0)
          {
            m.setVorname(vorname);
          }
          else
            throw new ApplicationException("Zeile " + anz + ": Vorname fehlt");
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Vorname fehlt");
        }

        m.setEingabedatum();
        m.setLetzteAenderung();
        m.store();

        for (Felddefinition f : zusfeldList)
        {
          String inhalt = results.getString("zusatzfeld_" + f.getName());
          if (inhalt.length() != 0)
          {
            Zusatzfelder zusatzfeld = (Zusatzfelder) Einstellungen
                .getDBService().createObject(Zusatzfelder.class, null);
            zusatzfeld.setMitglied(Integer.parseInt(m.getID()));
            zusatzfeld.setFelddefinition(Integer.parseInt(f.getID()));
            switch (f.getDatentyp())
            {
              case Datentyp.DATUM:
                try
                {
                  zusatzfeld
                      .setFeldDatum(new JVDateFormatTTMMJJJJ().parse(inhalt));
                }
                catch (ParseException e)
                {
                  throw new ApplicationException(
                      "Zeile " + anz + ": ungültiges Datumsformat " + inhalt);
                }
                break;
              case Datentyp.GANZZAHL:
                try
                {
                  zusatzfeld.setFeldGanzzahl(Integer.parseInt(inhalt));
                }
                catch (NumberFormatException e)
                {
                  throw new ApplicationException(
                      "Zeile " + anz + ": ungültiges Datenformat " + inhalt);
                }
                break;
              case Datentyp.JANEIN:
                if (inhalt.equalsIgnoreCase("true")
                    || inhalt.equalsIgnoreCase("ja")
                    || inhalt.equalsIgnoreCase("x"))
                {
                  zusatzfeld.setFeldJaNein(true);
                }
                else if (inhalt.equalsIgnoreCase("false")
                    || inhalt.equalsIgnoreCase("nein"))
                {
                  zusatzfeld.setFeldJaNein(false);
                }
                else
                {
                  throw new ApplicationException(
                      "Zeile " + anz + ": ungültiges Datenformat " + inhalt);
                }
                break;
              case Datentyp.WAEHRUNG:
                inhalt = inhalt.replace(",", ".");
                try
                {
                  zusatzfeld.setFeldWaehrung(new BigDecimal(inhalt));
                }
                catch (NumberFormatException e)
                {
                  throw new ApplicationException(
                      "Zeile " + anz + ": ungültiges Datenformat " + inhalt);
                }
                break;
              case Datentyp.ZEICHENFOLGE:
                zusatzfeld.setFeld(inhalt);
                break;
            }
            zusatzfeld.store();
          }
        }

        for (Eigenschaft e : eigenschaftList)
        {
          String inhalt = results
              .getString("eigenschaft_" + e.getBezeichnung());
          if (inhalt.length() != 0 && !inhalt.equalsIgnoreCase("false")
              && !inhalt.equalsIgnoreCase("nein"))
          {
            Eigenschaften eigenschaften = (Eigenschaften) Einstellungen
                .getDBService().createObject(Eigenschaften.class, null);
            eigenschaften.setMitglied(m.getID());
            eigenschaften.setEigenschaft(e.getID());
            eigenschaften.store();
          }
        }
        // Sekundaere-Beitragsgruppe nur bei Mitgliedern möglich
        if (m.getMitgliedstyp().getJVereinid() == Mitgliedstyp.MITGLIED)
        {
          for (Beitragsgruppe bg : sekundaerList)
          {
            String inhalt = results
                .getString("sekundaer_" + bg.getBezeichnung());
            if (inhalt.length() != 0 && !inhalt.equalsIgnoreCase("false")
                && !inhalt.equalsIgnoreCase("nein"))
            {
              SekundaereBeitragsgruppe sekundaer = (SekundaereBeitragsgruppe) Einstellungen
                  .getDBService()
                  .createObject(SekundaereBeitragsgruppe.class, null);
              sekundaer.setMitglied(Integer.parseInt(m.getID()));
              sekundaer.setBeitragsgruppe(Integer.parseInt(bg.getID()));
              sekundaer.store();
            }
          }
        }

        monitor.setPercentComplete(10 + (anz * 10));
        monitor.setStatusText(String.format("Mitglied %s importiert.",
            Adressaufbereitung.getNameVorname(m)));
      }
      results.close();
      stmt.close();
      conn.close();
      DBTransaction.commit();
      monitor.setStatusText("Import komplett abgeschlossen.");
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      monitor.log("Import abgebrochen: " + e.getMessage());
      Logger.error("Fehler", e);
    }
  }

  @Override
  public String getName()
  {
    return "CSV Mitglieder Import";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Mitglied.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return MitgliederImport.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.csv" };
      }
    };
    return new IOFormat[] { f };
  }
}
