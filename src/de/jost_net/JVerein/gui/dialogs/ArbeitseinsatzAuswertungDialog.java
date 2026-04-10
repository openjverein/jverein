/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.gui.control.ArbeitseinsatzAbrechnungControl;
import de.jost_net.JVerein.gui.parts.JVereinTablePart.ExportArt;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class ArbeitseinsatzAuswertungDialog extends AbstractDialog<Boolean>
{
  /**
   * @param position
   */
  public ArbeitseinsatzAuswertungDialog(int position)
  {
    super(position);
    super.setSize(600, SWT.DEFAULT);
    setTitle("Auswertung Arbeitseinsätze");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    final ArbeitseinsatzAbrechnungControl control = new ArbeitseinsatzAbrechnungControl(
        ArbeitseinsatzAbrechnungControl.AUSWERTUNG);

    LabelGroup group = new LabelGroup(parent, "Filter");
    group.addLabelPair("Jahr", control.getSuchJahr());
    group.addLabelPair("Auswertung", control.getAuswertungSchluessel());

    GridData gridData = new GridData(GridData.FILL_BOTH);
    LabelGroup liste = new LabelGroup(parent, "", true);
    liste.getComposite().setLayout(new GridLayout(1, false));
    control.getArbeitseinsatzUeberpruefungList().paint(liste.getComposite());
    gridData.heightHint = 300;
    liste.getComposite().setLayoutData(gridData);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(
        control.getHelpButton(DokumentationUtil.ARBEITSEINSATZPRUEFEN));
    buttons.addButton(control.exportButton(ExportArt.PDF));
    buttons.addButton(control.exportButton(ExportArt.CSV));
    buttons.addButton(control.getAbbrechenButton(this));
    buttons.paint(parent);
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return true;
  }
}
