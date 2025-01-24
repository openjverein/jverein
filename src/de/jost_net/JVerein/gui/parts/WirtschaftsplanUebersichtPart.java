package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;
import java.util.List;

public class WirtschaftsplanUebersichtPart implements Part
{
  private final WirtschaftsplanungControl control;

  private DecimalInput sollEinnahme;

  private DecimalInput sollAusgaben;

  public WirtschaftsplanUebersichtPart (WirtschaftsplanungControl control) {
    this.control = control;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup uebersicht = new LabelGroup(parent, "Übersicht");

    ColumnLayout columns = new ColumnLayout(uebersicht.getComposite(), 2);

    SimpleContainer einnahmen = new SimpleContainer(columns.getComposite());

    DateInput von = new DateInput(control.getWirtschaftsplanungZeile().getWirtschaftsplan().getDatumVon(), new JVDateFormatTTMMJJJJ());
    von.disable();
    einnahmen.addLabelPair("Von", von);
    sollEinnahme = new DecimalInput(control.getWirtschaftsplanungZeile().getPlanEinnahme(), Einstellungen.DECIMALFORMAT);
    sollEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Soll", sollEinnahme);
    DecimalInput istEinnahme = new DecimalInput(control.getWirtschaftsplanungZeile().getIstEinnahme(), Einstellungen.DECIMALFORMAT);
    istEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Ist", istEinnahme);

    SimpleContainer ausgaben = new SimpleContainer(columns.getComposite());

    DateInput bis = new DateInput(control.getWirtschaftsplanungZeile().getWirtschaftsplan().getDatumBis(), new JVDateFormatTTMMJJJJ());
    bis.disable();
    ausgaben.addLabelPair("Bis", bis);
    sollAusgaben = new DecimalInput(control.getWirtschaftsplanungZeile().getPlanAusgabe(), Einstellungen.DECIMALFORMAT);
    sollAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Soll", sollAusgaben);
    DecimalInput istAusgaben = new DecimalInput(control.getWirtschaftsplanungZeile().getIstAusgabe(), Einstellungen.DECIMALFORMAT);
    istAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Ist", istAusgaben);
  }

  @SuppressWarnings("unchecked")
  public void updateSoll() throws ApplicationException
  {
    if (sollEinnahme == null || sollAusgaben == null) {
      return;
    }

    List<WirtschaftsplanungNode> einnahmen;
    List<WirtschaftsplanungNode> ausgaben;

    try
    {
      einnahmen = (List<WirtschaftsplanungNode>) control.getEinnahmen().getItems();
      ausgaben = (List<WirtschaftsplanungNode>) control.getAusgaben().getItems();
    }
    catch (RemoteException e) {
      throw new ApplicationException("Fehler beim aktualisieren der Übersicht!");
    }

    double sollEinnahmen = einnahmen.stream()
        .mapToDouble(WirtschaftsplanungNode::getSoll)
        .sum();

    double sollAusgaben = ausgaben.stream()
        .mapToDouble(WirtschaftsplanungNode::getSoll)
        .sum();

    this.sollEinnahme.setValue(sollEinnahmen);
    this.sollAusgaben.setValue(sollAusgaben);
  }
}
