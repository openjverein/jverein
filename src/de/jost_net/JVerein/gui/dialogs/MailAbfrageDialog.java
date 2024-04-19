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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, um auszuwählen ob Spendenbescheinigungen 
 * vor dem Versenden noch gedruckt werden müssen.
 */
public class MailAbfrageDialog extends AbstractDialog<MailAbfrageDialog.Auswahl>
{

  private Auswahl retval = null;
  
  private String text;
  
  public enum Auswahl {
    OHNE_DRUCKEN,
    DRUCKEN_STANDARD,
    DRUCKEN_INDIVIDUELL
  }

  public MailAbfrageDialog(String text, int position)
  {
    super(position);
    this.text = text;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    TextAreaInput  textfeld = new TextAreaInput(text, 100);
    textfeld.disable();
    textfeld.paint(parent);
    
    ButtonArea b = new ButtonArea();
    
    b.addButton("Ohne Drucken", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        retval = Auswahl.OHNE_DRUCKEN;
        close();
      }
    }, null, true, "go-next.png");
    
    b.addButton("Mit Drucken (Standard)", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        retval = Auswahl.DRUCKEN_STANDARD;
        close();
      }
    }, null, false, "go-next.png");
    
    b.addButton("Mit Drucken (Individuell)", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        retval = Auswahl.DRUCKEN_INDIVIDUELL;
        close();
      }
    }, null, false, "go-next.png");
    
    b.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        retval = null;
        close();
      }
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected Auswahl getData() throws Exception
  {
    return retval;
  }
}
