package de.jost_net.JVerein.keys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

final class MandatSequenceTest
{
  @ParameterizedTest
  @EnumSource(MandatSequence.class)
  void getTxtLiefertDenSepaCode(MandatSequence sequence)
  {
    assertEquals(sequence.name(), sequence.getTxt());
  }
}
