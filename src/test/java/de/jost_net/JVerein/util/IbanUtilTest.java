package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.willuhn.util.ApplicationException;

final class IbanUtilTest
{
  @Test
  void checkIbanNormalisiertGueltigeIban() throws ApplicationException
  {
    assertEquals("DE89370400440532013000",
        IbanUtil.checkIban("DE89 3704 0044 0532 0130 00"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "DE00370400440532013000", // falsche Pruefziffer
      "XX89370400440532013000", // unbekanntes Land
      ""                        // leer
  })
  void checkIbanWirftBeiUngueltigerIban(String iban)
  {
    assertThrows(ApplicationException.class, () -> IbanUtil.checkIban(iban));
  }

  @Test
  void checkBicAkzeptiertGueltigeBic() throws ApplicationException
  {
    IbanUtil.checkBic("COBADEFFXXX");
  }

  @Test
  void checkBicWirftBeiUngueltigerBic()
  {
    assertThrows(ApplicationException.class,
        () -> IbanUtil.checkBic("UNGUELTIG"));
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", value = {
      "DE89370400440532013000, COBADEFFXXX",
      "DE00000000000000000000, null"
  })
  void getBicFuerIbanLiefertBicOderNull(String iban, String erwarteteBic)
  {
    assertEquals(erwarteteBic, IbanUtil.getBicFuerIban(iban));
  }

  @ParameterizedTest
  @CsvSource(nullValues = "null", value = {
      "10000000, Bundesbank",
      "MARKDEF1100, Bundesbank",
      "'COBADEFF', 'Commerzbank, CC SP'", // 8-stellige BIC ohne Filialkennzeichen,
                                          // nicht mit einer 8-stelligen BLZ verwechseln
      "99999999, null",
      "XXXXXXXXXXX, null"
  })
  void getBanknameLiefertNamenOderNull(String bicOderBlz, String erwarteterName)
  {
    assertEquals(erwarteterName, IbanUtil.getBankname(bicOderBlz));
  }

  @Test
  void getBanknameLiefertNullBeiNullOderLeer()
  {
    assertNull(IbanUtil.getBankname(null));
    assertNull(IbanUtil.getBankname(""));
  }
}
