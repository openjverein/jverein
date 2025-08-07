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
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.ZusatzbetragVorlageAuswahlAction;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung von Zusatzbeträgen
 */
public class MitgliedZusatzbetragZuordnungDialog extends AbstractDialog<String>
{
  private ZusatzbetragPart part;

  private Mitglied[] m;

  private String message = "";

  /**
   * @param position
   */
  public MitgliedZusatzbetragZuordnungDialog(int position, Mitglied[] m)
  {
    super(position);
    setTitle("Zuordnung Zusatzbetrag");
    this.m = m;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Zusatzbetrag zusatzb = (Zusatzbetrag) Einstellungen.getDBService()
        .createObject(Zusatzbetrag.class, null);
    part = new ZusatzbetragPart(zusatzb, false);
    part.paint(parent);

    final ZusatzbetragControl control = new ZusatzbetragControl(null);
    LabelGroup group = new LabelGroup(parent, "Vorlagen");
    group.addLabelPair("Als Vorlage speichern", control.getVorlage());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ZUSATZBETRAEGE, false, "question-circle.png");
    buttons.addButton("Vorlagen", new ZusatzbetragVorlageAuswahlAction(part),
        null, false, "view-refresh.png");

    buttons.addButton("Speichern", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        int count = 0;
        try
        {
          for (Mitglied mit : m)
          {
            Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
                .createObject(Zusatzbetrag.class, null);
            zb.setAusfuehrung((Date) part.getAusfuehrung().getValue());
            zb.setBetrag((Double) part.getBetrag().getValue());
            zb.setBuchungstext((String) part.getBuchungstext().getValue());
            zb.setEndedatum((Date) part.getEndedatum().getValue());
            zb.setFaelligkeit((Date) part.getFaelligkeit().getValue());
            IntervallZusatzzahlung iz = (IntervallZusatzzahlung) part
                .getIntervall().getValue();
            zb.setIntervall(iz.getKey());
            zb.setMitglied(Integer.parseInt(mit.getID()));
            zb.setStartdatum((Date) part.getStartdatum(true).getValue());
            zb.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
            zb.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
            zb.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
            zb.store();
            count++;
          }
          if (control.getVorlage().getValue()
              .equals(ZusatzbetragControl.MITDATUM)
              || control.getVorlage().getValue()
                  .equals(ZusatzbetragControl.OHNEDATUM))
          {
            ZusatzbetragVorlage zv = (ZusatzbetragVorlage) Einstellungen
                .getDBService().createObject(ZusatzbetragVorlage.class, null);
            IntervallZusatzzahlung iz = (IntervallZusatzzahlung) part
                .getIntervall().getValue();
            zv.setIntervall(iz.getKey());
            zv.setBuchungstext((String) part.getBuchungstext().getValue());
            zv.setBetrag((Double) part.getBetrag().getValue());
            if (control.getVorlage().getValue()
                .equals(ZusatzbetragControl.MITDATUM))
            {
              zv.setEndedatum((Date) part.getEndedatum().getValue());
              zv.setFaelligkeit((Date) part.getFaelligkeit().getValue());
              zv.setStartdatum((Date) part.getStartdatum(true).getValue());
            }
            zv.setBuchungsart((Buchungsart) part.getBuchungsart().getValue());
            zv.setBuchungsklasseId(part.getSelectedBuchungsKlasseId());
            zv.setZahlungsweg((Zahlungsweg) part.getZahlungsweg().getValue());
            zv.store();
          }
          message = String.format("%d Zusatzbeiträge gespeichert.", count);
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        catch (ApplicationException e)
        {
          SimpleDialog sd = new SimpleDialog(AbstractDialog.POSITION_CENTER);
          sd.setText(e.getMessage());
          sd.setTitle("Fehler");
          try
          {
            sd.open();
          }
          catch (Exception e1)
          {
            Logger.error("Fehler", e1);
          }
          return;
        }

        close();
      }
    }, null, true, "document-save.png");

    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        throw new OperationCanceledException();
      }
    }, null, false, "process-stop.png");
    buttons.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public String getData() throws Exception
  {
    return message;
  }
}
