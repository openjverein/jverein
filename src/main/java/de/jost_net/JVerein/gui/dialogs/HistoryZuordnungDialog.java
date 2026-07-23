package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.util.BuchungHistoryMatcher;
import de.jost_net.JVerein.util.BuchungHistoryMatcher.Proposal;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;

public class HistoryZuordnungDialog extends AbstractDialog<Proposal> {

  private final Buchung buchung;
  private Table table;
  private AbstractInput buchungsartInput;
  private SelectInput buchungsklasseInput;
  private SelectInput projektInput;

  private Proposal selectedProposal = null;
  private Buchungsart finalBuchungsart = null;
  private Buchungsklasse finalBuchungsklasse = null;
  private Projekt finalProjekt = null;
  private boolean abort = true;

  public HistoryZuordnungDialog(Buchung buchung, int position) {
    super(position);
    this.buchung = buchung;
    setTitle("Historie-basierte Buchungszuordnung");
    setSize(650, 480);
  }

  @Override
  protected void paint(Composite parent) throws Exception {
    LabelGroup group = new LabelGroup(parent, "Buchungsinformationen");
    group.addLabelPair("Name / IBAN", new LabelInput(buchung.getName() + " (" + (buchung.getIban() != null ? buchung.getIban() : "") + ")"));
    group.addLabelPair("Verwendungszweck", new LabelInput(buchung.getZweck()));
    group.addLabelPair("Betrag", new LabelInput(String.format("%.2f EUR", buchung.getBetrag())));

    LabelGroup editGroup = new LabelGroup(parent, "Individuelle Zuordnung anpassen");
    
    // Buchungsart input
    buchungsartInput = new BuchungsartInput().getBuchungsartInput(
        null, buchungsarttyp.BUCHUNGSART, (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    if (buchungsartInput instanceof SelectInput) {
      ((SelectInput) buchungsartInput).setPleaseChoose("Bitte auswählen");
    }
    editGroup.addLabelPair("Buchungsart", buchungsartInput);

    // Buchungsklasse input
    buchungsklasseInput = new BuchungsklasseInput().getBuchungsklasseInput(null, null);
    if (buchungsklasseInput instanceof SelectInput) {
      ((SelectInput) buchungsklasseInput).setPleaseChoose("Bitte auswählen");
    }
    editGroup.addLabelPair("Buchungsklasse", buchungsklasseInput);

    // Projekt input
    List<Projekt> projektList = new ArrayList<>();
    DBIterator<Projekt> projIt = Einstellungen.getDBService().createList(Projekt.class);
    projIt.setOrder("ORDER BY bezeichnung");
    while (projIt.hasNext()) {
      projektList.add(projIt.next());
    }
    projektInput = new SelectInput(projektList, null);
    projektInput.setAttribute("bezeichnung");
    projektInput.setPleaseChoose("Bitte auswählen");
    editGroup.addLabelPair("Projekt", projektInput);

    List<Proposal> proposals = BuchungHistoryMatcher.getProposals(
        buchung.getName(),
        buchung.getIban(),
        buchung.getZweck(),
        buchung.getBetrag() != null ? buchung.getBetrag() : 0.0
    );

    if (proposals.size() == 1) {
      Proposal p = proposals.get(0);
      selectedProposal = p;
      if (!p.isSplit()) {
        try {
          if (p.getBuchungsartId() != null) {
            Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
                .createObject(Buchungsart.class, String.valueOf(p.getBuchungsartId()));
            buchungsartInput.setValue(ba);
          }
          if (p.getBuchungsklasseId() != null) {
            Buchungsklasse bk = (Buchungsklasse) Einstellungen.getDBService()
                .createObject(Buchungsklasse.class, String.valueOf(p.getBuchungsklasseId()));
            buchungsklasseInput.setValue(bk);
          }
          if (p.getProjektId() != null) {
            Projekt proj = (Projekt) Einstellungen.getDBService()
                .createObject(Projekt.class, String.valueOf(p.getProjektId()));
            projektInput.setValue(proj);
          }
        } catch (Exception e) {
          Logger.error("Fehler beim Laden des Vorschlags", e);
        }
      } else {
        buchungsartInput.setEnabled(false);
        buchungsklasseInput.setEnabled(false);
        projektInput.setEnabled(false);
      }

      LabelGroup examplesGroup = new LabelGroup(parent, "Bisherige Buchungen (Beispiele aus dem Verlauf)");
      org.eclipse.swt.widgets.Text examplesText = new org.eclipse.swt.widgets.Text(examplesGroup.getComposite(), SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
      examplesText.setEditable(false);
      GridData gdEx = new GridData(GridData.FILL_HORIZONTAL);
      gdEx.heightHint = 120;
      examplesText.setLayoutData(gdEx);
      if (p.getExamples() != null && !p.getExamples().isEmpty()) {
        examplesText.setText(String.join("\n", p.getExamples()));
      } else {
        examplesText.setText("Keine historischen Buchungen als Beispiel vorhanden.");
      }
    } else {
      LabelGroup listGroup = new LabelGroup(parent, "Vorschläge aus der Historie (Doppelklick zum Auswählen)");
      
      table = new Table(listGroup.getComposite(), SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      GridData gd = new GridData(GridData.FILL_BOTH);
      gd.heightHint = 120;
      table.setLayoutData(gd);

      TableColumn colDetails = new TableColumn(table, SWT.LEFT);
      colDetails.setText("Buchungsart");
      colDetails.setWidth(200);

      TableColumn colKlasse = new TableColumn(table, SWT.LEFT);
      colKlasse.setText("Buchungsklasse");
      colKlasse.setWidth(120);

      TableColumn colProj = new TableColumn(table, SWT.LEFT);
      colProj.setText("Projekt");
      colProj.setWidth(120);

      TableColumn colScore = new TableColumn(table, SWT.RIGHT);
      colScore.setText("Score");
      colScore.setWidth(50);

      TableColumn colReason = new TableColumn(table, SWT.LEFT);
      colReason.setText("Grund");
      colReason.setWidth(180);

      for (Proposal p : proposals) {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setData(p);
        item.setText(0, p.getProposedBuchungsartLabel());
        item.setText(1, p.getProposedBuchungsklasseLabel());
        item.setText(2, p.getProposedProjektLabel());
        item.setText(3, Math.round(p.getScore()) + "%");
        item.setText(4, p.isSplit() ? "[Split] " + p.getReason() : p.getReason());
      }

      LabelGroup examplesGroup = new LabelGroup(parent, "Bisherige Buchungen (Beispiele aus dem Verlauf)");
      org.eclipse.swt.widgets.Text examplesText = new org.eclipse.swt.widgets.Text(examplesGroup.getComposite(), SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
      examplesText.setEditable(false);
      GridData gdEx = new GridData(GridData.FILL_HORIZONTAL);
      gdEx.heightHint = 60;
      examplesText.setLayoutData(gdEx);

      table.addListener(SWT.Selection, event -> {
        TableItem[] selection = table.getSelection();
        if (selection.length > 0) {
          Proposal p = (Proposal) selection[0].getData();
          if (p != null) {
            if (p.getExamples() != null && !p.getExamples().isEmpty()) {
              examplesText.setText(String.join("\n", p.getExamples()));
            } else {
              examplesText.setText("Keine historischen Buchungen als Beispiel vorhanden.");
            }
            if (!p.isSplit()) {
              buchungsartInput.setEnabled(true);
              buchungsklasseInput.setEnabled(true);
              projektInput.setEnabled(true);

              try {
                if (p.getBuchungsartId() != null) {
                  Buchungsart ba = (Buchungsart) Einstellungen.getDBService()
                      .createObject(Buchungsart.class, String.valueOf(p.getBuchungsartId()));
                  buchungsartInput.setValue(ba);
                } else {
                  buchungsartInput.setValue(null);
                }

                if (p.getBuchungsklasseId() != null) {
                  Buchungsklasse bk = (Buchungsklasse) Einstellungen.getDBService()
                      .createObject(Buchungsklasse.class, String.valueOf(p.getBuchungsklasseId()));
                  buchungsklasseInput.setValue(bk);
                } else {
                  buchungsklasseInput.setValue(null);
                }

                if (p.getProjektId() != null) {
                  Projekt proj = (Projekt) Einstellungen.getDBService()
                      .createObject(Projekt.class, String.valueOf(p.getProjektId()));
                  projektInput.setValue(proj);
                } else {
                  projektInput.setValue(null);
                }
              } catch (Exception e) {
                Logger.error("Fehler beim Laden des Vorschlags", e);
              }
            } else {
              buchungsartInput.setValue(null);
              buchungsklasseInput.setValue(null);
              projektInput.setValue(null);
              buchungsartInput.setEnabled(false);
              buchungsklasseInput.setEnabled(false);
              projektInput.setEnabled(false);
            }
          }
        }
      });

      table.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseDoubleClick(MouseEvent e) {
          TableItem[] selection = table.getSelection();
          if (selection.length > 0) {
            selectedProposal = (Proposal) selection[0].getData();
            if (!selectedProposal.isSplit()) {
              try {
                finalBuchungsart = (Buchungsart) buchungsartInput.getValue();
                finalBuchungsklasse = (Buchungsklasse) buchungsklasseInput.getValue();
                finalProjekt = (Projekt) projektInput.getValue();
              } catch (Exception ex) {
                Logger.error("Fehler beim Auslesen der Dialogwerte", ex);
              }
            }
            abort = false;
            close();
          }
        }
      });

      if (table.getItemCount() > 0) {
        table.setSelection(0);
        table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());
      }
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action() {
      @Override
      public void handleAction(Object context) {
        if (table != null) {
          TableItem[] selection = table.getSelection();
          if (selection.length > 0) {
            selectedProposal = (Proposal) selection[0].getData();
          }
        }
        if (selectedProposal == null || !selectedProposal.isSplit()) {
          try {
            finalBuchungsart = (Buchungsart) buchungsartInput.getValue();
            finalBuchungsklasse = (Buchungsklasse) buchungsklasseInput.getValue();
            finalProjekt = (Projekt) projektInput.getValue();
          } catch (Exception ex) {
            Logger.error("Fehler beim Auslesen der Dialogwerte", ex);
          }
        }
        abort = false;
        close();
      }
    }, null, true, "ok.png");

    buttons.addButton("Abbrechen", new Action() {
      @Override
      public void handleAction(Object context) {
        abort = true;
        close();
      }
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  public Proposal getSelectedProposal() {
    return selectedProposal;
  }

  public Buchungsart getBuchungsart() {
    return finalBuchungsart;
  }

  public Buchungsklasse getBuchungsklasse() {
    return finalBuchungsklasse;
  }

  public Projekt getProjekt() {
    return finalProjekt;
  }

  public boolean getAbort() {
    return abort;
  }

  @Override
  public Proposal getData() throws Exception {
    return selectedProposal;
  }
}
