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

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

/**
 * Ein Dialog, ueber den man Empfänger für eine Mail auswählen kann.
 */
public class DruckMailMitgliedDialog extends AbstractDialog<Object>
{
  private List<Mitglied> mitglieder = null;

  private String text = "";

  public DruckMailMitgliedDialog(List<Mitglied> mitglieder, String text,
      int position)
  {
    super(position);
    setTitle("Mitglieder Liste");
    setSize(700, 450);
    this.mitglieder = mitglieder;
    this.text = text;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    LabelInput textFeld = new LabelInput(text);
    if (text != null && !text.isEmpty())
    {
      textFeld.setColor(Color.ERROR);
    }
    group.addLabelPair("", textFeld);

    TablePart empfaenger = new TablePart(mitglieder, null);
    empfaenger.addColumn("EMail", "email");
    empfaenger.addColumn("Name", "name");
    empfaenger.addColumn("Vorname", "vorname");
    empfaenger.addColumn("Mitgliedstyp", Mitglied.MITGLIEDSTYP);
    empfaenger.setRememberOrder(true);
    empfaenger.setRememberColWidths(true);
    empfaenger.setRememberOrder(true);
    empfaenger.paint(parent);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Schließen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

}
