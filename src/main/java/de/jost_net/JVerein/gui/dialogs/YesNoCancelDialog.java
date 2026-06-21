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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * Dialog mit Ja, Abbrechen und optionalem Nein Button.
 */
public class YesNoCancelDialog extends AbstractDialog<Integer>
{
  private Integer selection = CANCEL;

  private String text = null;

  private boolean mitNo = false;

  public static final Integer YES = 1;

  public static final Integer NO = 2;

  public static final Integer CANCEL = 3;

  /**
   * @param position
   */
  public YesNoCancelDialog(int position, boolean mitNo)
  {
    super(position);
    this.mitNo = mitNo;
  }

  /**
   * Speichert den anzuzeigenden Text.
   * 
   * @param text
   *          anzuzeigender Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(this.text, true);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Ja", context -> {
      selection = YES;
      close();
    }, null, true, "ok.png");
    if (mitNo)
    {
      buttons.addButton("Nein", context -> {
        selection = NO;
        close();
      }, null, false, "nok.png");
    }
    buttons.addButton("Abbrechen", context -> {
      selection = CANCEL;
      close();
    }, null, false, "process-stop.png");

    container.addButtonArea(buttons);
    getShell().setMinimumSize(400, SWT.DEFAULT);
    getShell().setSize(getShell().computeSize(400, SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Integer getData() throws Exception
  {
    return selection;
  }
}
