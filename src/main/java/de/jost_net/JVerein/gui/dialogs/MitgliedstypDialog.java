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
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.SelectNoScrollInput;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;

/**
 * Ein Dialog, ueber den man die Personenart eines neuen Mitglieds auswählen
 * kann.
 */
public class MitgliedstypDialog extends AbstractDialog<Long>
{

  private Long selected = null;

  private SelectNoScrollInput mitgliedstyp = null;

  public MitgliedstypDialog(int position)
  {
    super(position);

    setTitle("Mitgliedstyp auswählen");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup options = new LabelGroup(parent, "Mitgliedstyp");
    options.addInput(this.getMitgliedstypInput());
    ButtonArea b = new ButtonArea();
    b.addButton("Weiter", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        Mitgliedstyp typ = (Mitgliedstyp) mitgliedstyp.getValue();
        try
        {
          selected = Long.valueOf(typ.getID());
        }
        catch (RemoteException | NumberFormatException e)
        {
          return;
        }
        close();
      }
    }, null, true, "go-next.png");
    b.addButton("Abbrechen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  @Override
  protected Long getData() throws Exception
  {
    return this.selected;
  }

  public SelectNoScrollInput getMitgliedstypInput() throws RemoteException
  {
    if (mitgliedstyp != null)
    {
      return mitgliedstyp;
    }
    Mitgliedstyp typ = null;

    DBIterator<Mitgliedstyp> mtIt = Einstellungen.getDBService()
        .createList(Mitgliedstyp.class);
    mtIt.addFilter(Mitgliedstyp.JVEREINID + " != " + Mitgliedstyp.MITGLIED
        + " OR " + Mitgliedstyp.JVEREINID + " IS NULL");
    mtIt.setOrder("order by " + Mitgliedstyp.BEZEICHNUNG);
    mitgliedstyp = new SelectNoScrollInput(
        mtIt != null ? PseudoIterator.asList(mtIt) : null, typ);
    mitgliedstyp.setName("Mitgliedstyp");
    return mitgliedstyp;
  }
}
