package de.jost_net.JVerein.gui.action;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.HistoryZuordnungDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.util.BuchungHistoryMatcher.Proposal;
import de.jost_net.JVerein.util.BuchungHistoryMatcher.SplitPart;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungHistoryZuordnungAction implements Action {

  @Override
  public void handleAction(Object context) throws ApplicationException {
    if (context == null || !(context instanceof Buchung)) {
      throw new ApplicationException("Bitte wählen Sie genau eine Buchung aus.");
    }

    try {
      Buchung buchung = (Buchung) context;
      if (buchung.isNewObject()) {
        return;
      }

      HistoryZuordnungDialog dialog = new HistoryZuordnungDialog(buchung, HistoryZuordnungDialog.POSITION_MOUSE);
      dialog.open();

      if (dialog.getAbort()) {
        return; // User cancelled
      }

      Proposal proposal = dialog.getSelectedProposal();

      if (proposal != null && proposal.isSplit()) {
        // Split proposal
        List<SplitPart> parts = proposal.getSplitParts();
        if (parts == null || parts.isEmpty()) {
          return;
        }

        // 1. Prepare master (HAUPT) booking
        Long firstBaId = parts.get(0).getBuchungsartId();
        buchung.setBuchungsartId(firstBaId);
        buchung.setSplitId(Long.valueOf(buchung.getID()));
        buchung.setSplitTyp(SplitbuchungTyp.HAUPT);
        buchung.setSplitbuchung(true);
        buchung.store();

        // 2. Create counter (GEGEN) booking
        Buchung gegen = (Buchung) Einstellungen.getDBService().createObject(Buchung.class, null);
        copyFields(buchung, gegen);
        gegen.setSplitId(Long.valueOf(buchung.getID()));
        gegen.setSplitTyp(SplitbuchungTyp.GEGEN);
        gegen.setBetrag(buchung.getBetrag() * -1);
        gegen.setSplitbuchung(true);
        gegen.setBuchungsartId(firstBaId);
        gegen.store();

        // 3. Create split child bookings
        double totalAmount = buchung.getBetrag();
        double sumSplit = 0.0;
        int size = parts.size();

        for (int i = 0; i < size; i++) {
          SplitPart part = parts.get(i);
          double partAmount;
          
          if (i == size - 1) {
            // Absorb any rounding differences in the last part
            partAmount = BigDecimal.valueOf(totalAmount)
                .subtract(BigDecimal.valueOf(sumSplit))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
          } else {
            partAmount = BigDecimal.valueOf(totalAmount * part.getPercentage())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
            sumSplit += partAmount;
          }

          Buchung splitPart = (Buchung) Einstellungen.getDBService().createObject(Buchung.class, null);
          copyFields(buchung, splitPart);
          splitPart.setSplitId(Long.valueOf(buchung.getID()));
          splitPart.setSplitTyp(SplitbuchungTyp.SPLIT);
          splitPart.setBetrag(partAmount);
          splitPart.setSplitbuchung(true);
          splitPart.setBuchungsartId(part.getBuchungsartId());
          splitPart.setBuchungsklasseId(part.getBuchungsklasseId());
          splitPart.setProjektID(part.getProjektId());
          splitPart.store();
        }

        GUI.getStatusBar().setSuccessText("Buchung erfolgreich in Splitbuchung aufgeteilt.");
      } else {
        // Classic non-split proposal (using values possibly adjusted by the user)
        Buchungsart ba = dialog.getBuchungsart();
        Buchungsklasse bk = dialog.getBuchungsklasse();
        Projekt proj = dialog.getProjekt();

        if (ba != null) {
          buchung.setBuchungsartId(Long.valueOf(ba.getID()));
        } else {
          buchung.setBuchungsartId(null);
        }

        if (bk != null) {
          buchung.setBuchungsklasseId(Long.valueOf(bk.getID()));
        } else {
          buchung.setBuchungsklasseId(null);
        }

        if (proj != null) {
          buchung.setProjektID(Long.valueOf(proj.getID()));
        } else {
          buchung.setProjektID(null);
        }

        buchung.store();
        GUI.getStatusBar().setSuccessText("Buchung erfolgreich klassifiziert.");
      }
    } catch (Exception e) {
      Logger.error("Fehler bei der History-basierten Zuordnung", e);
      GUI.getStatusBar().setErrorText("Fehler bei der Zuordnung: " + e.getLocalizedMessage());
    }
  }

  private void copyFields(Buchung src, Buchung dest) throws RemoteException {
    dest.setAbrechnungslauf(src.getAbrechnungslauf());
    dest.setArt(src.getArt());
    dest.setAuszugsnummer(src.getAuszugsnummer());
    dest.setBlattnummer(src.getBlattnummer());
    dest.setDatum(src.getDatum());
    dest.setKommentar(src.getKommentar());
    dest.setKonto(src.getKonto());
    dest.setName(src.getName());
    dest.setZweck(src.getZweck());
    dest.setIban(src.getIban());
  }
}
