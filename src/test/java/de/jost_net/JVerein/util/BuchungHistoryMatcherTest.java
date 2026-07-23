package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.jost_net.JVerein.util.BuchungHistoryMatcher.Proposal;
import de.jost_net.JVerein.util.BuchungHistoryMatcher.SplitPart;

class BuchungHistoryMatcherTest {

  @Test
  void testJaccardSimilarity_Identical() {
    double sim = BuchungHistoryMatcher.calculateJaccardSimilarity("Mitgliedsbeitrag", "Mitgliedsbeitrag", false);
    assertEquals(1.0, sim, 0.001);
  }

  @Test
  void testJaccardSimilarity_CaseInsensitive() {
    double sim = BuchungHistoryMatcher.calculateJaccardSimilarity("MITGLIEDSBEITRAG", "mitgliedsbeitrag", false);
    assertEquals(1.0, sim, 0.001);
  }

  @Test
  void testJaccardSimilarity_Different() {
    double sim = BuchungHistoryMatcher.calculateJaccardSimilarity("Spende", "Mitgliedsbeitrag", false);
    assertEquals(0.0, sim, 0.001);
  }

  @Test
  void testJaccardSimilarity_StripNumbers() {
    // "Beitrag 2024" vs "Beitrag 2025" - stripping numbers leaves "Beitrag"
    double sim = BuchungHistoryMatcher.calculateJaccardSimilarity("Beitrag 2024", "Beitrag 2025", true);
    assertEquals(1.0, sim, 0.001);
  }

  @Test
  void testJaccardSimilarity_NoStripNumbers() {
    // "Beitrag 2024" has tokens: "beitrag", "2024"
    // "Beitrag 2025" has tokens: "beitrag", "2025"
    // Intersection: "beitrag" (1 token)
    // Union: "beitrag", "2024", "2025" (3 tokens)
    // Similarity: 1/3 = 0.333
    double sim = BuchungHistoryMatcher.calculateJaccardSimilarity("Beitrag 2024", "Beitrag 2025", false);
    assertEquals(0.333, sim, 0.01);
  }

  @Test
  void testProposalSorting() {
    List<Proposal> proposals = new ArrayList<>();
    proposals.add(new Proposal(1L, 1L, 1L, 50.0, "Reason 1"));
    proposals.add(new Proposal(2L, 2L, 2L, 95.0, "Reason 2"));
    proposals.add(new Proposal(3L, 3L, 3L, 80.0, "Reason 3"));

    Collections.sort(proposals);

    // Should be sorted in descending order of score
    assertEquals(95.0, proposals.get(0).getScore());
    assertEquals(80.0, proposals.get(1).getScore());
    assertEquals(50.0, proposals.get(2).getScore());
  }

  @Test
  void testProposalPropertiesClassic() {
    Proposal p = new Proposal(10L, 20L, 30L, 88.5, "Exakt");
    assertEquals(10L, p.getBuchungsartId());
    assertEquals(20L, p.getBuchungsklasseId());
    assertEquals(30L, p.getProjektId());
    assertEquals(88.5, p.getScore());
    assertEquals("Exakt", p.getReason());
    assertTrue(!p.isSplit());
  }

  @Test
  void testProposalPropertiesSplit() {
    List<SplitPart> parts = new ArrayList<>();
    parts.add(new SplitPart(1L, 1L, 1L, 0.6));
    parts.add(new SplitPart(2L, 2L, 2L, 0.4));

    Proposal p = new Proposal(parts, 99.0, "Splittung");
    assertTrue(p.isSplit());
    assertEquals(99.0, p.getScore());
    assertEquals("Splittung", p.getReason());
    assertEquals(2, p.getSplitParts().size());
    assertEquals(0.6, p.getSplitParts().get(0).getPercentage());
    assertEquals(0.4, p.getSplitParts().get(1).getPercentage());
  }
}
