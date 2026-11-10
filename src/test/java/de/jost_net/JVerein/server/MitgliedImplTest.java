package de.jost_net.JVerein.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * Prüft die Familienverband-Regeln von MitgliedImpl.insertCheck() an einem
 * Mock, bei dem nur diese Methode echt läuft und die Getter gestubbt sind.
 */
class MitgliedImplTest
{
  private MockedStatic<Einstellungen> einstellungen;

  /** JVereinDBService legt beim Laden ein Jameica-Settings an. */
  private MockedConstruction<Settings> settings;

  private JVereinDBService db;

  @BeforeEach
  void setUp()
  {
    settings = Mockito.mockConstruction(Settings.class);
    db = mock(JVereinDBService.class);
    einstellungen = Mockito.mockStatic(Einstellungen.class);
    einstellungen.when(Einstellungen::getDBService).thenReturn(db);
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    einstellung(Property.GEBURTSDATUMPFLICHT, false);
    einstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT, false);
    einstellung(Property.EINTRITTSDATUMPFLICHT, false);
    einstellung(Property.BEITRAGSMODEL,
        Beitragsmodel.GLEICHERTERMINFUERALLE.getKey());
  }

  @AfterEach
  void tearDown()
  {
    einstellungen.close();
    settings.close();
  }

  private void einstellung(Property property, Object wert)
  {
    einstellungen.when(() -> Einstellungen.getEinstellung(property))
        .thenReturn(wert);
  }

  private static Date datum(int jahr, int monat, int tag)
  {
    return Date.valueOf(LocalDate.of(jahr, monat, tag));
  }

  private static Mitgliedstyp mitgliedstyp(String id) throws Exception
  {
    Mitgliedstyp typ = mock(Mitgliedstyp.class);
    doReturn(id).when(typ).getID();
    return typ;
  }

  private static Beitragsgruppe gruppe(ArtBeitragsart art) throws Exception
  {
    Beitragsgruppe bg = mock(Beitragsgruppe.class);
    doReturn(art).when(bg).getBeitragsArt();
    return bg;
  }

  /** Ein Mitglied, bei dem nur insertCheck() echt ausgeführt wird. */
  private static MitgliedImpl mitglied(ArtBeitragsart art, Date austritt)
      throws Exception
  {
    MitgliedImpl m = mock(MitgliedImpl.class);
    doCallRealMethod().when(m).insertCheck();
    doReturn("10").when(m).getID();
    doReturn("N").when(m).getPersonenart();
    doReturn("Mustermann").when(m).getName();
    doReturn("Anna").when(m).getVorname();
    doReturn(mitgliedstyp(Mitgliedstyp.MITGLIED)).when(m).getMitgliedstyp();
    doReturn(gruppe(art)).when(m).getBeitragsgruppe();
    doReturn(new Date(System.currentTimeMillis())).when(m).getGeburtsdatum();
    doReturn("w").when(m).getGeschlecht();
    doReturn(datum(2020, 1, 1)).when(m).getEintritt();
    doReturn(austritt).when(m).getAustritt();
    return m;
  }

  /** Vollzahler mit Art der Beitragsgruppe und Mitgliedstyp */
  private static Mitglied vollzahler(String typ, ArtBeitragsart art)
      throws Exception
  {
    Mitglied z = mock(Mitglied.class);
    doReturn(mitgliedstyp(typ)).when(z).getMitgliedstyp();
    doReturn(gruppe(art)).when(z).getBeitragsgruppe();
    doReturn(datum(2019, 1, 1)).when(z).getEintritt();
    return z;
  }

  @SuppressWarnings("unchecked")
  private static DBIterator<Mitglied> iterator(Mitglied... mitglieder)
      throws Exception
  {
    DBIterator<Mitglied> it = mock(DBIterator.class);
    if (mitglieder.length == 0)
    {
      when(it.hasNext()).thenReturn(false);
    }
    else
    {
      when(it.hasNext()).thenReturn(true, false);
      when(it.next()).thenReturn(mitglieder[0]);
    }
    return it;
  }

  /**
   * Die Abfragen von insertCheck(): zuerst die Suche nach dem Vollzahler des
   * Mitglieds, dann die Suche nach eigenen Angehörigen.
   */
  private void datenbank(Mitglied z, boolean mitAustrittGefunden)
      throws Exception
  {
    DBIterator<Mitglied> suche1 = iterator(z);
    DBIterator<Mitglied> suche2 = mitAustrittGefunden ? iterator(z) : iterator();
    DBIterator<Mitglied> keineAngehoerigen = iterator();
    doReturn(suche1).doReturn(suche2).doReturn(keineAngehoerigen).when(db)
        .createList(Mitglied.class);
  }

  @Test
  void angehoerigerMitVollzahlerWirdAkzeptiert() throws Exception
  {
    MitgliedImpl m = mitglied(ArtBeitragsart.FAMILIE_ANGEHOERIGER, null);
    doReturn(5L).when(m).getVollZahlerID();
    datenbank(vollzahler(Mitgliedstyp.MITGLIED, ArtBeitragsart.NORMAL), true);

    assertDoesNotThrow(() -> m.insertCheck());
  }

  @Test
  void ausgetretenerAngehoerigerMitAktivemVollzahlerWirdAkzeptiert()
      throws Exception
  {
    // Hat das Mitglied ein Austrittsdatum, sucht insertCheck() nur einen
    // Vollzahler, der schon früher ausgetreten ist. Ist der Vollzahler noch
    // aktiv, wird keiner gefunden, das ist der Normalfall.
    MitgliedImpl m = mitglied(ArtBeitragsart.FAMILIE_ANGEHOERIGER,
        datum(2024, 12, 31));
    doReturn(5L).when(m).getVollZahlerID();
    datenbank(vollzahler(Mitgliedstyp.MITGLIED, ArtBeitragsart.NORMAL), false);

    assertDoesNotThrow(() -> m.insertCheck());
  }

  @Test
  void vollzahlerDarfKeinNichtMitgliedSein() throws Exception
  {
    MitgliedImpl m = mitglied(ArtBeitragsart.FAMILIE_ANGEHOERIGER, null);
    doReturn(5L).when(m).getVollZahlerID();
    datenbank(vollzahler(Mitgliedstyp.SPENDER, ArtBeitragsart.NORMAL), true);

    ApplicationException e = assertThrows(ApplicationException.class,
        () -> m.insertCheck());
    assertTrue(e.getMessage().contains("kein Nicht-Mitglied"), e.getMessage());
  }

  @Test
  void beitragsgruppeDesVollzahlersMussNormalSein() throws Exception
  {
    MitgliedImpl m = mitglied(ArtBeitragsart.FAMILIE_ANGEHOERIGER, null);
    doReturn(5L).when(m).getVollZahlerID();
    datenbank(vollzahler(Mitgliedstyp.MITGLIED,
        ArtBeitragsart.FAMILIE_ANGEHOERIGER), true);

    ApplicationException e = assertThrows(ApplicationException.class,
        () -> m.insertCheck());
    assertTrue(e.getMessage().contains("Normal sein"), e.getMessage());
  }

  @Test
  void mitgliedKannNichtSeinEigenerAbweichenderZahlerSein() throws Exception
  {
    MitgliedImpl m = mitglied(ArtBeitragsart.NORMAL, null);
    Mitglied selbst = mock(Mitglied.class);
    doReturn("10").when(selbst).getID();
    doReturn(selbst).when(m).getAbweichenderZahler();

    ApplicationException e = assertThrows(ApplicationException.class,
        () -> m.insertCheck());
    assertTrue(e.getMessage().contains("nicht identisch"), e.getMessage());
  }

  @Test
  void anderesMitgliedAlsAbweichenderZahlerWirdAkzeptiert() throws Exception
  {
    MitgliedImpl m = mitglied(ArtBeitragsart.NORMAL, null);
    Mitglied anderer = mock(Mitglied.class);
    doReturn("11").when(anderer).getID();
    doReturn(anderer).when(m).getAbweichenderZahler();

    assertDoesNotThrow(() -> m.insertCheck());
  }
}
