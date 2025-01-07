package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import org.eclipse.swt.widgets.Composite;

import java.rmi.RemoteException;

public class WirtschaftsplanUebersichtPart implements Part
{
  private WirtschaftsplanungControl control;


  public WirtschaftsplanUebersichtPart (WirtschaftsplanungControl control) {
    this.control = control;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup uebersicht = new LabelGroup(parent, "Übersicht");

    ColumnLayout columns = new ColumnLayout(uebersicht.getComposite(), 2);

    SimpleContainer einnahmen = new SimpleContainer(columns.getComposite());

    DateInput von = new DateInput(control.getWirtschaftsplanungZeile().getVon(), new JVDateFormatTTMMJJJJ());
    von.disable();
    einnahmen.addLabelPair("Von", von);
    DecimalInput sollEinnahme = new DecimalInput(control.getWirtschaftsplanungZeile().getPlanEinnahme(), Einstellungen.DECIMALFORMAT);
    sollEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Soll", sollEinnahme);
    DecimalInput istEinnahme = new DecimalInput(control.getWirtschaftsplanungZeile().getIstEinnahme(), Einstellungen.DECIMALFORMAT);
    istEinnahme.disable();
    einnahmen.addLabelPair("Einnahmen Ist", istEinnahme);

    SimpleContainer ausgaben = new SimpleContainer(columns.getComposite());

    DateInput bis = new DateInput(control.getWirtschaftsplanungZeile().getBis(), new JVDateFormatTTMMJJJJ());
    bis.disable();
    ausgaben.addLabelPair("Bis", bis);
    DecimalInput sollAusgaben = new DecimalInput(control.getWirtschaftsplanungZeile().getPlanAusgabe(), Einstellungen.DECIMALFORMAT);
    sollAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Soll", sollAusgaben);
    DecimalInput istAusgaben = new DecimalInput(control.getWirtschaftsplanungZeile().getIstAusgabe(), Einstellungen.DECIMALFORMAT);
    istAusgaben.disable();
    ausgaben.addLabelPair("Ausgaben Ist", istAusgaben);
  }
}
