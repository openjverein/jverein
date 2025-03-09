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
package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.SollbuchungAuswahlDialog;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.logging.Logger;

public class SollbuchungAuswahlInput
{

  private DialogInput sollbuchungAuswahl = null;

  private Buchung[] buchungen = null;

  private Sollbuchung sollbuchung = null;

  private Mitglied mitglied = null;

  public SollbuchungAuswahlInput(Buchung buchung) throws RemoteException
  {
    buchungen = new Buchung[1];
    buchungen[0] = buchung;
    this.sollbuchung = buchungen[0].getSollbuchung();
  }

  /**
   * Liefert ein Auswahlfeld fuer die Sollbuchung.
   * 
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public DialogInput getSollbuchungAuswahl() throws RemoteException
  {
    if (sollbuchungAuswahl != null
        && !sollbuchungAuswahl.getControl().isDisposed())
    {
      return sollbuchungAuswahl;
    }
    SollbuchungAuswahlDialog d = new SollbuchungAuswahlDialog(
        buchungen[0]);
    d.addCloseListener(new SollbuchungListener());

    sollbuchungAuswahl = new DialogInput(sollbuchung != null
        ? Adressaufbereitung.getNameVorname(sollbuchung.getMitglied()) + ", "
            + new JVDateFormatTTMMJJJJ().format(sollbuchung.getDatum()) + ", "
            + Einstellungen.DECIMALFORMAT.format(sollbuchung.getBetrag())
        : "", d);
    sollbuchungAuswahl.disableClientControl();
    sollbuchungAuswahl.setValue(buchungen[0].getSollbuchung());
    return sollbuchungAuswahl;
  }

  /**
   * Listener, der die Auswahl der Sollbuchung ueberwacht und die
   * Waehrungsbezeichnung hinter dem Betrag abhaengig vom ausgewaehlten Konto
   * anpasst.
   */
  private class SollbuchungListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {

      if (event == null)
      {
        return;
      }

      if (event.data == null)
      {
        try
        {
          if (event.detail != SWT.CANCEL)
            getSollbuchungAuswahl().setText("");
          return;
        }
        catch (RemoteException er)
        {
          String error = "Fehler bei Auswahl der Sollbuchung";
          Logger.error(error, er);
          GUI.getStatusBar().setErrorText(error);
        }
      }
      try
      {
        String b = "";
        if (event.data instanceof Sollbuchung)
        {
          sollbuchung = (Sollbuchung) event.data;
          b = Adressaufbereitung.getNameVorname(sollbuchung.getMitglied())
              + ", " + new JVDateFormatTTMMJJJJ().format(sollbuchung.getDatum())
              + ", "
              + Einstellungen.DECIMALFORMAT.format(sollbuchung.getBetrag());
          String name = buchungen[0].getName();
          String zweck1 = buchungen[0].getZweck();
          if ((name == null || name.length() == 0)
              && (zweck1 == null || zweck1.length() == 0))
          {
            buchungen[0].setName(Adressaufbereitung.getNameVorname(sollbuchung
                .getMitglied()));
            buchungen[0].setZweck(sollbuchung.getZweck1());
            buchungen[0].setBetrag(sollbuchung.getBetrag());
            buchungen[0].setDatum(new Date());
            buchungen[0].setBuchungsartId(sollbuchung
                .getSollbuchungPositionList().get(0).getBuchungsartId());
            buchungen[0].setBuchungsklasseId(sollbuchung
                .getSollbuchungPositionList().get(0).getBuchungsklasseId());
          }
        }
        else if (event.data instanceof Mitglied)
        {
          mitglied = (Mitglied) event.data;
          b = Adressaufbereitung.getNameVorname(mitglied)
              + ", Sollbuchung erzeugen";
        }
        getSollbuchungAuswahl().setText(b);
      }
      catch (RemoteException er)
      {
        String error = "Fehler bei Zuordnung der Sollbuchung";
        Logger.error(error, er);
        GUI.getStatusBar().setErrorText(error);
      }
    }
  }
}
