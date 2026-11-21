package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import de.willuhn.util.ApplicationException;

final class AbrechnungSEPATest
{
  @Test
  void addFasstVerwendungszweckeZusammen() throws ApplicationException
  {
    Zahler z1 = new Zahler();
    z1.setBetrag(BigDecimal.valueOf(1.00));
    z1.setVerwendungszweck("Zweck 1");
    Zahler z2 = new Zahler();
    z2.setBetrag(BigDecimal.valueOf(1.00));
    z2.setVerwendungszweck("Zweck 2");
    z1.add(z2);

    Zahler z3 = new Zahler();
    z3.setBetrag(BigDecimal.valueOf(1.00));
    z3.setVerwendungszweck("Zweck 3");
    z1.add(z3);
    assertEquals("ZWECK 1 1.0, ZWECK 2 1.0, ZWECK 3 1.0",
        z1.getVerwendungszweck());
  }
}
