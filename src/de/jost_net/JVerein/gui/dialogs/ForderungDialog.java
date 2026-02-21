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
import de.jost_net.JVerein.gui.control.ForderungControl;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class ForderungDialog extends AbstractDialog<Boolean>
{
  private Mitglied[] mitglieder;

  /**
   * @param position
   */
  public ForderungDialog(int position, Mitglied[] m)
  {
    super(position);
    super.setSize(950, SWT.DEFAULT);
    setTitle("Forderung erstellen");
    this.mitglieder = m;
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    boolean einstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    boolean einstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    boolean einstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    boolean einstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    final ForderungControl control = new ForderungControl(mitglieder);

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(control.getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);

    left.addHeadline("Forderung");
    left.addLabelPair("Fälligkeit ", control.getSepaControl().getFaelligkeit());
    left.addLabelPair("Zahlungsgrund", control.getPart().getBuchungstext());
    left.addLabelPair("Betrag", control.getPart().getBetrag());
    left.addLabelPair("Buchungsart", control.getPart().getBuchungsart());
    if (einstellungBuchungsklasseInBuchung)
    {
      left.addLabelPair("Buchungsklasse",
          control.getPart().getBuchungsklasse());
    }
    if (einstellungSteuerInBuchung)
    {
      left.addLabelPair("Steuer", control.getPart().getSteuer());
    }
    left.addLabelPair("Zahlungsweg", control.getPart().getZahlungsweg());
    left.addLabelPair("Mitglied zahlt selbst",
        control.getPart().getMitgliedzahltSelbst());
    left.addHeadline("Vorlagen");
    left.addLabelPair("Als Vorlage speichern",
        control.getVorlageSpeichernInput());

    right.addHeadline("Sollbuchungen");
    right.addLabelPair("Sollbuchungen zusammenfassen",
        control.getSepaControl().getSollbuchungenZusammenfassen());

    right.addHeadline("Lastschriften");
    right.addLabelPair("Kompakte Abbuchung",
        control.getSepaControl().getKompakteAbbuchung());
    right.addLabelPair("SEPA-Check temporär deaktivieren",
        control.getSepaControl().getSEPACheck());
    right.addLabelPair("Lastschrift-PDF erstellen",
        control.getSepaControl().getSEPAPrint());
    right.addLabelPair("Abbuchungsausgabe",
        control.getSepaControl().getAbbuchungsausgabe());

    if (einstellungRechnungAnzeigen)
    {
      right.addHeadline("Rechnungen");
      right.addLabelPair("Rechnung erstellen",
          control.getSepaControl().getRechnung());
      if (einstellungSpeicherungAnzeigen)
      {
        right.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getSepaControl().getRechnungsdokumentSpeichern());
      }
      right.addLabelPair("Rechnungsformular",
          control.getSepaControl().getRechnungFormular());
      right.addLabelPair("Rechnungstext", control.getRechnungsTextInput());
      right.addLabelPair("Rechnungsdatum",
          control.getSepaControl().getRechnungsdatum());
    }

    LabelGroup below = new LabelGroup(parent, "Fehler/Warnungen/Hinweise",
        true);
    below.getComposite().setLayout(new GridLayout(1, false));
    below.addPart(control.getBugsList());
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 150;
    below.getComposite().setLayoutData(gridData);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getHelpButton());
    buttons.addButton(control.getZahlungsgrundVariablenButton());
    if (einstellungRechnungAnzeigen)
    {
      buttons.addButton(control.getRechnungstextVariablenButton());
    }
    buttons.addButton(control.getVorlagenButton());
    buttons.addButton(control.getPruefenButton());
    buttons.addButton(control.getErstellenButton(this));
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
