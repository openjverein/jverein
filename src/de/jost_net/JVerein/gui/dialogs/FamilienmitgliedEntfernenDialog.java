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

import de.jost_net.JVerein.Messaging.FamilienbeitragMessage;
import de.jost_net.JVerein.gui.control.FamilienbeitragNode;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, ueber den man die Beitragsgruppe beim Auflösen eines
 * Familienverbandes auswählen kann.
 */
public class FamilienmitgliedEntfernenDialog extends AbstractDialog<String>
{

  private MitgliedControl control;

  public FamilienmitgliedEntfernenDialog(FamilienbeitragNode fbn)
  {
    super(AbstractDialog.POSITION_CENTER);
    setTitle("Person aus Familienverband entfernen");
    setSize(450, SWT.DEFAULT);
    control = new MitgliedControl(null);
    control.setMitglied(fbn.getMitglied());
    try
    {
      String kto = (String) control.getIban().getValue();
      if (kto.length() == 0)
      {
        FamilienbeitragNode zahler = (FamilienbeitragNode) fbn.getParent();
        control.getIban().setValue(zahler.getMitglied().getIban());
        control.getBic().setValue(zahler.getMitglied().getBic());
        // control.getKontoinhaber().setValue(
        // zahler.getMitglied().getKontoinhaber());
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup lgBeitragsgruppe = new LabelGroup(parent, "Beitragsgruppe");
    lgBeitragsgruppe.addLabelPair("Beitragsgruppe",
        control.getBeitragsgruppe(false));
    LabelGroup lgBank = new LabelGroup(parent, "Bankverbindung");
    lgBank.addLabelPair("IBAN", control.getIban());
    lgBank.addLabelPair("BIC", control.getBic());
    LabelGroup lgZahlungsweg = new LabelGroup(parent, "Zahlungsweg");
    lgZahlungsweg.addLabelPair("Zahlungsweg", control.getZahlungsweg());
    // lgBank.addLabelPair("Kontoinhaber", control.getKontoinhaber());
    ButtonArea b = new ButtonArea();
    b.addButton("Weiter", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        Mitglied m = control.getMitglied();
        try
        {
          if (control.getBeitragsgruppe(false).getValue() == null)
          {
            throw new ApplicationException("Bitte Beitragsgruppe auswählen");
          }
          Beitragsgruppe bg = (Beitragsgruppe) control.getBeitragsgruppe(false)
              .getValue();
          m.setBeitragsgruppe(Integer.valueOf(bg.getID()));
          // m.setKontoinhaber((String) control.getKontoinhaber().getValue());
          m.setVollZahlerID(null);
          m.setLetzteAenderung();
          m.setIban(control.getIban().getValue().toString());
          m.setBic(control.getBic().getValue().toString());
          m.setZahlungsweg(((Zahlungsweg)control.getZahlungsweg().getValue()).getKey());
          m.store();
          Application.getMessagingFactory().sendMessage(
              new FamilienbeitragMessage(m));

          close();
        }
        catch (ApplicationException e)
        {
          SimpleDialog sd = new SimpleDialog(SimpleDialog.POSITION_CENTER);
          sd.setTitle("Fehler");
          sd.setText(e.getMessage());
          sd.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
          try
          {
            sd.open();
          }
          catch (Exception e1)
          {
            Logger.error("Fehler", e1);
          }
          Logger.error("Fehler", e);
          return;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
          return;
        }
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
  protected String getData() throws Exception
  {
    return null;
  }

}
