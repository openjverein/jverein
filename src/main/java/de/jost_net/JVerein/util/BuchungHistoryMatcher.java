package de.jost_net.JVerein.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;

public class BuchungHistoryMatcher {

  public static class Proposal implements Comparable<Proposal> {
    private final Long buchungsartId;
    private final Long buchungsklasseId;
    private final Long projektId;
    private final double score;
    private final String reason;

    public Proposal(Long buchungsartId, Long buchungsklasseId, Long projektId,
        double score, String reason) {
      this.buchungsartId = buchungsartId;
      this.buchungsklasseId = buchungsklasseId;
      this.projektId = projektId;
      this.score = score;
      this.reason = reason;
    }

    public Long getBuchungsartId() {
      return buchungsartId;
    }

    public Long getBuchungsklasseId() {
      return buchungsklasseId;
    }

    public Long getProjektId() {
      return projektId;
    }

    public double getScore() {
      return score;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public int compareTo(Proposal o) {
      return Double.compare(o.score, this.score); // Descending order
    }
  }

  public static List<Proposal> getProposals(String name, String iban,
      String zweck, double betrag) {
    List<Proposal> proposals = new ArrayList<>();
    try {
      DBService db = Einstellungen.getDBService();
      DBIterator<Buchung> it = db.createList(Buchung.class);

      boolean hasFilter = false;
      // Fetch candidates with a matching name or IBAN to prevent full-table scan
      if (iban != null && !iban.trim().isEmpty()) {
        it.addFilter("iban = ? OR LOWER(name) LIKE ?", iban.replace(" ", ""),
            "%" + name.trim().toLowerCase() + "%");
        hasFilter = true;
      } else if (name != null && !name.trim().isEmpty()) {
        it.addFilter("LOWER(name) LIKE ?", "%" + name.trim().toLowerCase() + "%");
        hasFilter = true;
      }

      if (!hasFilter) {
        // If both are empty, don't return anything or scan the whole table
        return proposals;
      }

      it.setOrder("ORDER BY datum DESC");
      Map<String, Proposal> bestProposals = new HashMap<>(); // Key: BA_BK_Proj combination

      while (it.hasNext()) {
        Buchung past = it.next();
        if (past.getBuchungsartId() == null) {
          continue;
        }

        double score = 0;
        List<String> matchReasons = new ArrayList<>();

        // 1. Audit Name Match (Weight: 80)
        if (name != null && past.getName() != null) {
          if (name.trim().equalsIgnoreCase(past.getName().trim())) {
            score += 80;
            matchReasons.add("Name (Exact)");
          } else {
            double nameSim = calculateJaccardSimilarity(name, past.getName(), false);
            if (nameSim > 0.5) {
              score += 60 * nameSim;
              matchReasons.add("Name (Fuzzy: " + Math.round(nameSim * 100) + "%)");
            }
          }
        }

        // 2. Audit Value/Amount Pattern (Weight: 60)
        if (past.getBetrag() != null) {
          double pastBetrag = past.getBetrag();
          double absBetrag = Math.abs(betrag);
          double absPast = Math.abs(pastBetrag);
          if (Math.abs(betrag - pastBetrag) < 0.01) {
            score += 60;
            matchReasons.add("Amount (Exact)");
          } else if (absBetrag > 0 && absPast > 0) {
            double ratio = Math.abs(absBetrag - absPast) / Math.max(absBetrag, absPast);
            if (ratio < 0.05) { // within 5%
              score += 30 * (1 - ratio);
              matchReasons.add("Amount (Close: " + Math.round((1 - ratio) * 100) + "%)");
            }
          }
        }

        // 3. Audit Zweck Pattern (Weight: 40) - Stripping Numbers/Invoice IDs
        if (zweck != null && past.getZweck() != null) {
          double zweckSim = calculateJaccardSimilarity(zweck, past.getZweck(), true);
          if (zweckSim > 0.2) {
            score += 40 * zweckSim;
            matchReasons.add("Zweck Pattern (Fuzzy: " + Math.round(zweckSim * 100) + "%)");
          }
        }

        // 4. Audit IBAN Match (Weight: 30) - Low weight due to multiple business IBANs
        if (iban != null && past.getIban() != null
            && iban.replace(" ", "").equalsIgnoreCase(past.getIban().replace(" ", ""))) {
          score += 30;
          matchReasons.add("IBAN Match");
        }

        if (score > 0) {
          Long baId = past.getBuchungsartId();
          Long bkId = past.getBuchungsklasseId();
          Long projId = past.getProjektID();
          String key = baId + "_" + bkId + "_" + projId;

          Proposal currentBest = bestProposals.get(key);
          if (currentBest == null || currentBest.score < score) {
            String reasonStr = String.join(", ", matchReasons);
            bestProposals.put(key,
                new Proposal(baId, bkId, projId, score, reasonStr));
          }
        }
      }

      proposals.addAll(bestProposals.values());
      Collections.sort(proposals);

    } catch (Exception e) {
      Logger.error("Error matching past history", e);
    }
    return proposals;
  }

  private static double calculateJaccardSimilarity(String s1, String s2,
      boolean stripNumbers) {
    if (s1 == null || s2 == null) {
      return 0;
    }

    // Strip numbers if required (useful for bypassing changing invoice numbers)
    if (stripNumbers) {
      s1 = s1.replaceAll("\\d+", "");
      s2 = s2.replaceAll("\\d+", "");
    }

    String[] w1 = s1.toLowerCase().split("\\W+");
    String[] w2 = s2.toLowerCase().split("\\W+");

    Set<String> set1 = new HashSet<>(Arrays.asList(w1));
    Set<String> set2 = new HashSet<>(Arrays.asList(w2));

    // Remove short words
    set1.removeIf(w -> w.length() < 3);
    set2.removeIf(w -> w.length() < 3);

    if (set1.isEmpty() || set2.isEmpty()) {
      return 0;
    }

    Set<String> intersection = new HashSet<>(set1);
    intersection.retainAll(set2);

    Set<String> union = new HashSet<>(set1);
    union.addAll(set2);

    return (double) intersection.size() / union.size();
  }
}
