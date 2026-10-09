package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

import de.jost_net.JVerein.keys.MandatSequence;
import de.willuhn.util.ApplicationException;

final class ZahlerTest
{
  private static Zahler gueltigerZahler() throws ApplicationException
  {
    Zahler z = new Zahler();
    z.setIban("DE89370400440532013000");
    z.setBic("COBADEFFXXX");
    z.setName("Max Mustermann");
    z.setVerwendungszweck("Mitgliedsbeitrag");
    z.setBetrag(BigDecimal.valueOf(10.00));
    z.setMandatid("MANDAT-1");
    z.setMandatdatum(new Date(0));
    z.setMandatsequence(MandatSequence.RCUR);
    z.setFaelligkeit(new Date());
    return z;
  }

  @Test
  void gueltigerZahlerLiefertAlleFelderUnverandertZurueck()
      throws ApplicationException
  {
    Zahler z = gueltigerZahler();
    assertEquals("DE89370400440532013000", z.getIban());
    assertEquals("COBADEFFXXX", z.getBic());
    assertEquals("MAX MUSTERMANN", z.getName());
    assertEquals("MITGLIEDSBEITRAG", z.getVerwendungszweck());
    assertEquals(BigDecimal.valueOf(10.00), z.getBetrag());
    assertEquals("MANDAT-1", z.getMandatid());
    assertEquals(MandatSequence.RCUR, z.getMandatsequence());
  }

  @Test
  void setMandatidWirftBeiZuLangerId()
  {
    Zahler z = new Zahler();
    String zuLang = "X".repeat(36);
    assertThrows(ApplicationException.class, () -> z.setMandatid(zuLang));
  }

  @Test
  void setMandatdatumWirftBeiDatumInDerZukunft()
  {
    Zahler z = new Zahler();
    Date zukunft = new Date(System.currentTimeMillis() + 86_400_000L);
    assertThrows(ApplicationException.class, () -> z.setMandatdatum(zukunft));
  }

  @Test
  void setBicWirftBeiUngueltigerBic()
  {
    Zahler z = new Zahler();
    assertThrows(ApplicationException.class, () -> z.setBic("UNGUELTIG"));
  }

  @Test
  void setIbanWirftBeiUngueltigerIban()
  {
    Zahler z = new Zahler();
    assertThrows(ApplicationException.class,
        () -> z.setIban("DE00370400440532013000"));
  }

  @Test
  void setBetragWirftBeiNull()
  {
    Zahler z = new Zahler();
    assertThrows(ApplicationException.class,
        () -> z.setBetrag(BigDecimal.ZERO));
  }

  @Test
  void setBetragAkzeptiertNegativenWert() throws ApplicationException
  {
    Zahler z = new Zahler();
    z.setBetrag(BigDecimal.valueOf(-5.00));
    assertEquals(BigDecimal.valueOf(-5.00), z.getBetrag());
  }

  @Test
  void addFasstBetraegeZusammen() throws ApplicationException
  {
    Zahler z1 = new Zahler();
    z1.setBetrag(BigDecimal.valueOf(10.00));
    z1.setVerwendungszweck("Zweck A");
    Zahler z2 = new Zahler();
    z2.setBetrag(BigDecimal.valueOf(5.00));
    z2.setVerwendungszweck("Zweck B");

    z1.add(z2);

    assertEquals(BigDecimal.valueOf(15.00), z1.getBetrag());
  }

  @Test
  void addWirftBeiNegativerSumme()
  {
    assertThrows(ApplicationException.class, () -> {
      Zahler z1 = new Zahler();
      z1.setBetrag(BigDecimal.valueOf(-5.00));
      z1.setVerwendungszweck("Zweck A");
      Zahler z2 = new Zahler();
      z2.setBetrag(BigDecimal.valueOf(-5.00));
      z2.setVerwendungszweck("Zweck B");

      z1.add(z2);
    });
  }

  @Test
  void addKuerztVerwendungszweckAufMaximallaenge() throws ApplicationException
  {
    Zahler z1 = new Zahler();
    z1.setBetrag(BigDecimal.valueOf(1.00));
    z1.setVerwendungszweck("A".repeat(130));

    for (int i = 0; i < 5; i++)
    {
      Zahler z2 = new Zahler();
      z2.setBetrag(BigDecimal.valueOf(1.00));
      z2.setVerwendungszweck("B".repeat(130));
      z1.add(z2);
    }

    assertEquals(140, z1.getVerwendungszweck().length());
    assertTrue(z1.getVerwendungszweck().endsWith("..."));
  }
}
