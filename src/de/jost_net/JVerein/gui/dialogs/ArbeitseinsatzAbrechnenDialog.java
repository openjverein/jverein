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
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.ArbeitseinsatzAbrechnungControl;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class ArbeitseinsatzAbrechnenDialog extends AbstractDialog<Boolean>
{
  /**
   * @param position
   */
  public ArbeitseinsatzAbrechnenDialog(int position)
  {
    super(position);
    super.setSize(950, SWT.DEFAULT);
    setTitle("Auswertung Arbeitseinsätze");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    final ArbeitseinsatzAbrechnungControl control = new ArbeitseinsatzAbrechnungControl();

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(control.getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);

    left.addHeadline("Filter");
    left.addLabelPair("Jahr", control.getSuchJahr());
    left.addLabelPair("Auswertung", control.getAuswertungSchluessel());

    right.addHeadline("Abrechnungsparameter");
    right.addLabelPair("Buchungstext", control.getBuchungstext());
    right.addLabelPair("Fälligkeit", control.getFaelligkeit());
    right.addLabelPair("Zahlungsweg", control.getZahlungsweg());
    right.addLabelPair("Mitglied zahlt selbst",
        control.getMitgliedzahltSelbst());

    GridData gridData = new GridData(GridData.FILL_BOTH);
    LabelGroup liste = new LabelGroup(parent, "", true);
    liste.getComposite().setLayout(new GridLayout(1, false));
    control.getArbeitseinsatzUeberpruefungList().paint(liste.getComposite());
    gridData.heightHint = 150;
    liste.getComposite().setLayoutData(gridData);

    LabelGroup below = new LabelGroup(parent, "Fehler/Warnungen/Hinweise",
        true);
    below.getComposite().setLayout(new GridLayout(1, false));
    below.addPart(control.getBugsList());
    gridData.heightHint = 150;
    below.getComposite().setLayoutData(gridData);

    ButtonArea buttons2 = new ButtonArea();
    buttons2.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ARBEITSEINSATZPRUEFEN, false, "question-circle.png");
    buttons2.addButton(control.getPDFAusgabeButton());
    buttons2.addButton(control.getCSVAusgabeButton());
    buttons2.addButton(control.getVariablenButton());
    buttons2.addButton(control.getPruefenButton());
    buttons2.addButton(control.getStartButton(this));
    buttons2.addButton(control.getAbbrechenButton(this));
    buttons2.paint(parent);
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
