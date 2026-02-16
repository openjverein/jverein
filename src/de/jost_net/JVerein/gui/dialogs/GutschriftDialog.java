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
import de.jost_net.JVerein.gui.control.GutschriftControl;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Settings;

public class GutschriftDialog extends AbstractDialog<Boolean>
{
  private boolean einstellungRechnungAnzeigen = false;

  private boolean einstellungBuchungsklasseInBuchung = false;

  private boolean einstellungSteuerInBuchung = false;

  private boolean einstellungSpeicherungAnzeigen = false;

  private boolean isMitglied = false;

  private Settings settings = null;

  private GutschriftControl control;

  public GutschriftDialog(IGutschriftProvider[] providerArray)
      throws RemoteException
  {
    super(SWT.CENTER);
    setTitle("Gutschrift erstellen");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.control = new GutschriftControl(providerArray);
    this.isMitglied = providerArray[0] instanceof Mitglied;
    setSize(950, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    einstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    einstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    einstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    einstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(control.getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);

    left.addHeadline("Überweisung");
    left.addLabelPair("Ausgabe", control.getAusgabeInput());
    left.addLabelPair("Ausführungsdatum", control.getDatumInput());
    left.addLabelPair("Verwendungszweck", control.getZweckInput());

    // Fixen Betrag erstatten
    right.addHeadline("Fixer Betrag");
    if (!isMitglied)
    {
      right.addLabelPair("Fixen Betrag erstatten",
          control.getFixerBetragAbrechnenInput());
    }
    else
    {
      // Nicht anzeigen aber Wert auf true setzen
      control.getFixerBetragAbrechnenInput();
    }
    right.addLabelPair("Erstattungsbetrag", control.getFixerBetragInput());
    right.addLabelPair("Buchungsart", control.getBuchungsartInput());
    if (einstellungBuchungsklasseInBuchung)
    {
      right.addLabelPair("Buchungsklasse", control.getBuchungsklasseInput());
    }
    if (einstellungSteuerInBuchung)
    {
      right.addLabelPair("Steuer", control.getSteuerInput());
    }

    // Nur anzeigen wenn Rechnungen aktiviert sind
    if (einstellungRechnungAnzeigen)
    {
      ColumnLayout cl2 = new ColumnLayout(group.getComposite(), 1);
      SimpleContainer below = new SimpleContainer(cl2.getComposite(), false, 2);
      below.addHeadline("Rechnung");
      SimpleContainer bleft = new SimpleContainer(below.getComposite(), false,
          2);
      SimpleContainer bright = new SimpleContainer(below.getComposite(), false,
          2);

      bleft.addLabelPair("Rechnung zur Gutschrift erzeugen",
          control.getRechnungErzeugenInput());
      if (einstellungSpeicherungAnzeigen)
      {
        bleft.addLabelPair("Rechnung als Buchungsdokument speichern",
            control.getRechnungsDokumentSpeichernInput());
      }
      bleft.addLabelPair("Erstattungsformular", control.getFormularInput());
      bleft.addLabelPair("Rechnungsdatum", control.getRechnungsDatumInput());
      bright.addLabelPair("Rechnungstext", control.getRechnungsTextInput());
      bright.addLabelPair("Kommentar", control.getRechnungKommentarInput());
    }

    LabelGroup below2 = new LabelGroup(parent, "Fehler/Warnungen/Hinweise",
        true);
    below2.getComposite().setLayout(new GridLayout(1, false));
    // below2.addHeadline("Fehler/Warnungen/Hinweise");
    below2.addPart(control.getBugsList());
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.heightHint = 150;
    below2.getComposite().setLayoutData(gridData);

    // Buttons
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getHelpButton());
    buttons.addButton(control.getVZweckVariablenButton());
    if (einstellungRechnungAnzeigen)
    {
      buttons.addButton(control.getRZweckVariablenButton());
    }
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
