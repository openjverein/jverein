/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Dialog zur Zuordnung der Kontoauszugsinformationen (Auszug/Blatt)
 */
public class BetragEingabeDialog extends AbstractDialog<Boolean>
{

  private BigDecimalInput betragInput = null;

  private LabelInput status = null;

  private BigDecimal betrag = null;

  Boolean abort = true;

  /**
   * @param position
   */
  public BetragEingabeDialog(int position, BigDecimal betrag)
  {
    super(position);
    setTitle("Betrag Eingabe");
    setSize(SWT.DEFAULT, SWT.DEFAULT);
    this.betrag = betrag;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Betrag", getBetragInput());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", e -> {
      betrag = (BigDecimal) getBetragInput().getValue();
      if (betrag == null || betrag.signum() <= 0)
      {
        status.setValue("Bitte positiven Betrag sein.");
        status.setColor(Color.ERROR);
        return;
      }
      abort = false;
      close();
    }, null, true, "ok.png");

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Boolean getData() throws Exception
  {
    return abort;
  }

  public BigDecimal getBetrag()
  {
    return betrag;
  }

  private BigDecimalInput getBetragInput()
  {
    if (betragInput != null)
    {
      return betragInput;
    }
    betragInput = new BigDecimalInput(betrag, Einstellungen.BIGDECIMALFORMAT);
    return betragInput;
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

}
