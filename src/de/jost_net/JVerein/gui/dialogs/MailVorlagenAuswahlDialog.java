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

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.control.MailVorlageControl;
import de.jost_net.JVerein.rmi.MailVorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, ueber den die Vorlage für eine Mail ausgewählt werden kann.
 */
public class MailVorlagenAuswahlDialog extends AbstractDialog<MailVorlage>
{

  private MailVorlageControl control;

  private MailVorlage retval;

  private boolean abort = true;

  private boolean mailsenden = true;

  public MailVorlagenAuswahlDialog(MailVorlageControl control, int position,
      boolean mailsenden)
  {
    super(position);
    this.control = control;
    this.mailsenden = mailsenden;
    setTitle("Mail-Vorlage");
    setSize(550, 450);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    Action action = new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          retval = (MailVorlage) control.getMailVorlageTable(null)
              .getSelection();
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(e.getMessage());
        }
        abort = false;
        close();
      }
    };
    control.getMailVorlageTable(action).paint(parent);

    ButtonArea b = new ButtonArea();

    b.addButton("Verwenden", action, null, true, "ok.png");

    if (mailsenden)
    {
      b.addButton("Ohne Mail-Vorlage", new Action()
      {

        @Override
        public void handleAction(Object context)
        {
          abort = false;
          close();
        }
      }, null, true, "go-next.png");
    }

    b.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected MailVorlage getData() throws Exception
  {
    return retval;
  }

  public boolean getAbort()
  {
    return abort;
  }

}
