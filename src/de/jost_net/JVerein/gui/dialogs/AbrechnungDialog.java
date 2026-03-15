/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für den Abrechnungslauf
 */
public class AbrechnungDialog extends AbstractDialog<Boolean>
{

  /**
   * @param position
   */
  public AbrechnungDialog(int position)
  {
    super(position);
    super.setSize(950, SWT.DEFAULT);
    setTitle("Abrechnung");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    final AbrechnungSEPAControl control = new AbrechnungSEPAControl();

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(control.getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite());
    SimpleContainer rigth = new SimpleContainer(cl.getComposite());

    left.addHeadline("Parameter");
    left.addInput(control.getAbbuchungsmodus());
    left.addInput(control.getFaelligkeit());
    if ((Integer) Einstellungen.getEinstellung(
        Property.BEITRAGSMODEL) == Beitragsmodel.FLEXIBEL.getKey())
    {
      left.addInput(control.getAbrechnungsmonat());
    }
    left.addInput(control.getStichtag());
    left.addInput(control.getVondatum());
    left.addInput(control.getVonEingabedatum());
    left.addInput(control.getBisdatum());
    left.addInput(control.getZahlungsgrund());
    if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
    {
      left.addLabelPair("Zusatzbeträge", control.getZusatzbetrag());
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
    {
      left.addLabelPair("Kursteilnehmer", control.getKursteilnehmer());
    }
    left.addLabelPair("Sollbuchungen zusammenfassen",
        control.getSollbuchungenZusammenfassen());

    rigth.addHeadline("Lastschriften");
    rigth.addLabelPair("Kompakte Abbuchung", control.getKompakteAbbuchung());
    rigth.addLabelPair("SEPA-Check temporär deaktivieren",
        control.getSEPACheck());
    rigth.addLabelPair("Lastschrift-PDF erstellen", control.getSEPAPrint());
    rigth.addInput(control.getAbbuchungsausgabe());

    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      rigth.addHeadline("Rechnungen");
      rigth.addLabelPair("Rechnung erstellen²", control.getRechnung());
      if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG)
          && JVereinPlugin.isArchiveServiceActive())
      {
        rigth.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getRechnungsdokumentSpeichern());
      }
      rigth.addInput(control.getRechnungsformular());
      rigth.addInput(control.getRechnungstext("Wenn leer Zahlungsgrund"));
      rigth.addInput(control.getRechnungsdatum());
    }

    group.addSeparator();
    group.addText(
        "¹) Für die Berechnung, ob ein Mitglied bereits eingetreten oder ausgetreten ist. "
            + "Und für Berechnung ob Zusatzbeträge fällig sind.\n"
            + "²) Es wird für jede (zusammengefasste) Sollbuchung eine separate Rechnung erstellt.",
        true);

    LabelGroup below = new LabelGroup(parent, "Fehler/Warnungen/Hinweise",
        true);
    below.getComposite().setLayout(new GridLayout(1, false));
    below.addPart(control.getBugsList());
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 150;
    below.getComposite().setLayoutData(gridData);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getHelpButton(DokumentationUtil.ABRECHNUNG));
    buttons.addButton(control.getZahlungsgrundVariablenButton());
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      buttons.addButton(control.getRechnungstextVariablenButton());
    }
    buttons.addButton(control.getPruefenButton());
    buttons.addButton(control.getStartButton(this));
    buttons.addButton(control.getAbbrechenButton(this));
    buttons.paint(parent);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return true;
  }

  @Override
  protected boolean isModeless()
  {
    return true;
  }
}
