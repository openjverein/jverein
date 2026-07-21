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
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;

public class BuchungHistoryMatcher {

  public static class SplitPart {
    private final Long buchungsartId;
    private final Long buchungsklasseId;
    private final Long projektId;
    private final double percentage;

    public SplitPart(Long buchungsartId, Long buchungsklasseId, Long projektId, double percentage) {
      this.buchungsartId = buchungsartId;
      this.buchungsklasseId = buchungsklasseId;
      this.projektId = projektId;
      this.percentage = percentage;
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

    public double getPercentage() {
      return percentage;
    }
  }

  public static class Proposal implements Comparable<Proposal> {
    private final Long buchungsartId;
    private final Long buchungsklasseId;
    private final Long projektId;
    private final double score;
    private final String reason;
    private final boolean isSplit;
    private final List<SplitPart> splitParts;
    private final List<String> examples = new ArrayList<>();

    public Proposal(Long buchungsartId, Long buchungsklasseId, Long projektId,
        double score, String reason) {
      this.buchungsartId = buchungsartId;
      this.buchungsklasseId = buchungsklasseId;
      this.projektId = projektId;
      this.score = score;
      this.reason = reason;
      this.isSplit = false;
      this.splitParts = null;
    }

    public Proposal(List<SplitPart> splitParts, double score, String reason) {
      this.buchungsartId = null;
      this.buchungsklasseId = null;
      this.projektId = null;
      this.score = score;
      this.reason = reason;
      this.isSplit = true;
      this.splitParts = splitParts;
    }

    public List<String> getExamples() {
      return examples;
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

    public boolean isSplit() {
      return isSplit;
    }

    public List<SplitPart> getSplitParts() {
      return splitParts;
    }

    public String getProposedBuchungsartLabel() {
      if (!isSplit) {
        try {
          if (buchungsartId != null) {
            Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
                .createObject(Buchungsart.class, String.valueOf(buchungsartId));
            if (ba != null) {
              return ba.getNummer() + " - " + ba.getBezeichnung();
            }
          }
        } catch (Exception e) {
          // ignore
        }
        return "";
      } else {
        StringBuilder sb = new StringBuilder("Aufteilung: ");
        boolean first = true;
        for (SplitPart part : splitParts) {
          if (!first) {
            sb.append(" / ");
          }
          first = false;
          sb.append(Math.round(part.percentage * 100)).append("% ");
          try {
            if (part.buchungsartId != null) {
              Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
                  .createObject(Buchungsart.class, String.valueOf(part.buchungsartId));
              if (ba != null) {
                sb.append(ba.getBezeichnung());
              }
            }
          } catch (Exception e) {
            sb.append("ID ").append(part.buchungsartId);
          }
        }
        return sb.toString();
      }
    }

    public String getProposedBuchungsklasseLabel() {
      if (isSplit) {
        return "";
      }
      try {
        if (buchungsklasseId != null) {
          Buchungsklasse bk = (Buchungsklasse) Einstellungen.getDBService()
              .createObject(Buchungsklasse.class, String.valueOf(buchungsklasseId));
          if (bk != null) {
            return bk.getBezeichnung();
          }
        }
      } catch (Exception e) {
        // ignore
      }
      return "";
    }

    public String getProposedProjektLabel() {
      if (isSplit) {
        return "";
      }
      try {
        if (projektId != null) {
          de.jost_net.JVerein.rmi.Projekt p = (de.jost_net.JVerein.rmi.Projekt) Einstellungen.getDBService()
              .createObject(de.jost_net.JVerein.rmi.Projekt.class, String.valueOf(projektId));
          if (p != null) {
            return p.getBezeichnung();
          }
        }
      } catch (Exception e) {
        // ignore
      }
      return "";
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

      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.add(java.util.Calendar.YEAR, -2);
      java.util.Date twoYearsAgo = cal.getTime();

      boolean hasFilter = false;
      List<String> conditions = new ArrayList<>();
      List<Object> params = new ArrayList<>();
      params.add(twoYearsAgo);

      if (iban != null && !iban.trim().isEmpty()) {
        conditions.add("iban = ?");
        params.add(iban.replace(" ", ""));
      }

      if (name != null && !name.trim().isEmpty()) {
        conditions.add("LOWER(name) LIKE ?");
        params.add("%" + name.trim().toLowerCase() + "%");
      }

      if (zweck != null && !zweck.trim().isEmpty()) {
        String zweckToken = "";
        String[] tokens = zweck.trim().split("\\s+");
        if (tokens.length > 0) {
          zweckToken = tokens[0].toLowerCase();
          if (zweckToken.length() < 4 && tokens.length > 1) {
            zweckToken = (tokens[0] + " " + tokens[1]).toLowerCase();
          }
        }
        if (!zweckToken.isEmpty()) {
          conditions.add("LOWER(zweck) LIKE ?");
          params.add("%" + zweckToken + "%");
        }
      }

      if (!conditions.isEmpty()) {
        it.addFilter("datum >= ? AND (" + String.join(" OR ", conditions) + ")", params.toArray());
        hasFilter = true;
      }

      if (!hasFilter) {
        return proposals;
      }

      it.setOrder("ORDER BY datum DESC");
      Map<String, Proposal> bestProposals = new HashMap<>();

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

        // 4. Audit IBAN Match (Weight: 30)
        if (iban != null && past.getIban() != null
            && iban.replace(" ", "").equalsIgnoreCase(past.getIban().replace(" ", ""))) {
          score += 30;
          matchReasons.add("IBAN Match");
        }

        if (score > 0) {
          boolean isPastSplit = past.getSplitId() != null && past.getSplitTyp() != null && past.getSplitTyp() == SplitbuchungTyp.HAUPT;
          if (isPastSplit) {
            // Retrieve child bookings for this split
            DBIterator<Buchung> children = db.createList(Buchung.class);
            children.addFilter("splitid = ? and splittyp = ?", past.getSplitId(), SplitbuchungTyp.SPLIT);
            List<SplitPart> splitParts = new ArrayList<>();
            double totalSplitAmount = 0.0;
            while (children.hasNext()) {
              Buchung child = children.next();
              if (child.getBetrag() != null) {
                totalSplitAmount += Math.abs(child.getBetrag());
                splitParts.add(new SplitPart(
                    child.getBuchungsartId(),
                    child.getBuchungsklasseId(),
                    child.getProjektID(),
                    Math.abs(child.getBetrag())
                ));
              }
            }
            if (totalSplitAmount != 0.0) {
              List<SplitPart> normalizedParts = new ArrayList<>();
              StringBuilder keyBuilder = new StringBuilder("split");
              for (SplitPart sp : splitParts) {
                double pct = sp.percentage / totalSplitAmount;
                normalizedParts.add(new SplitPart(sp.buchungsartId, sp.buchungsklasseId, sp.projektId, pct));
                keyBuilder.append("_").append(sp.buchungsartId).append("_").append(Math.round(pct * 100));
              }
              String key = keyBuilder.toString();
              Proposal currentBest = bestProposals.get(key);
              if (currentBest == null || currentBest.score < score) {
                String reasonStr = String.join(", ", matchReasons) + " [Split Muster]";
                currentBest = new Proposal(normalizedParts, score, reasonStr);
                bestProposals.put(key, currentBest);
              }
              java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy");
              String dateStr = past.getDatum() != null ? df.format(past.getDatum()) : "";
              String exampleStr = String.format("%s: %s - '%s' (%.2f EUR)", dateStr, past.getName() != null ? past.getName() : "", past.getZweck() != null ? past.getZweck() : "", past.getBetrag() != null ? past.getBetrag() : 0.0);
              if (currentBest.getExamples().size() < 3) {
                currentBest.getExamples().add(exampleStr);
              }
            }
          } else if (past.getSplitTyp() == null || past.getSplitTyp() != SplitbuchungTyp.SPLIT) {
            // Normal classic booking proposal
            Long baId = past.getBuchungsartId();
            Long bkId = past.getBuchungsklasseId();
            Long projId = past.getProjektID();
            String key = baId + "_" + bkId + "_" + projId;

            Proposal currentBest = bestProposals.get(key);
            if (currentBest == null || currentBest.score < score) {
              String reasonStr = String.join(", ", matchReasons);
              currentBest = new Proposal(baId, bkId, projId, score, reasonStr);
              bestProposals.put(key, currentBest);
            }
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd.MM.yyyy");
            String dateStr = past.getDatum() != null ? df.format(past.getDatum()) : "";
            String exampleStr = String.format("%s: %s - '%s' (%.2f EUR)", dateStr, past.getName() != null ? past.getName() : "", past.getZweck() != null ? past.getZweck() : "", past.getBetrag() != null ? past.getBetrag() : 0.0);
            if (currentBest.getExamples().size() < 3) {
              currentBest.getExamples().add(exampleStr);
            }
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

  static double calculateJaccardSimilarity(String s1, String s2,
      boolean stripNumbers) {
    if (s1 == null || s2 == null) {
      return 0;
    }

    if (stripNumbers) {
      s1 = s1.replaceAll("\\d+", "");
      s2 = s2.replaceAll("\\d+", "");
    }

    String[] w1 = s1.toLowerCase().split("\\W+");
    String[] w2 = s2.toLowerCase().split("\\W+");

    Set<String> set1 = new HashSet<>(Arrays.asList(w1));
    Set<String> set2 = new HashSet<>(Arrays.asList(w2));

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
