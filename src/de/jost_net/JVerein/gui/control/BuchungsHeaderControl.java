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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;

public class BuchungsHeaderControl extends AbstractControl
{
  private TextInput kontoNameInput;

  private TextInput aktJahrAnfangSaldoInput;

  private TextInput aktJahrEinnahmenInput;

  private TextInput aktJahrAusgabenInput;

  private TextInput aktJahrSaldoInput;

  private TextInput lastJahrAnfangSaldoInput;

  private TextInput lastJahrEinnahmenInput;

  private TextInput lastJahrAusgabenInput;

  private TextInput lastJahrSaldoInput;

  public BuchungsHeaderControl(AbstractView view, BuchungsControl control)
  {
    super(view);
    control.addKontoChangeListener(event ->
      {
        Object data = event.data;
        if (data instanceof Konto)
          felderAktuallisieren((Konto) data);
    });
  }

  private void felderAktuallisieren(Konto konto)
  {
    try
    {
      ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
          "konto");

      it.addColumn("konto.bezeichnung as konto");
      it.addColumn("anfangsbestand.betrag AS anfangssaldo");
      it.addColumn("anfangsbestand.datum");
      it.addColumn(
          "SUM(case when buchung.betrag > 0 then buchung.betrag ELSE 0 END) AS einnahmen");
      it.addColumn(
          "SUM(case when buchung.betrag < 0 then buchung.betrag ELSE 0 END) AS ausgaben");
      it.addColumn(
          "anfangsbestand.betrag + SUM(coalesce(buchung.betrag,0)) AS saldo");
      it.addColumn("MAX(buchung.datum) AS letzte_buchung");

      it.join("anfangsbestand", "anfangsbestand.konto = konto.id");
      
      // Hier müssen wir zwischen H2 und MySQL unterscheiden, da es nicht die
      // gleichen Funktionen gibt
      String filter = "konto.id = buchung.konto AND buchung.datum >= anfangsbestand.datum";
      if (JVereinDBService.SETTINGS.getString("database.driver", "h2")
          .toLowerCase().contains("h2"))
      {
        filter += " AND buchung.datum < DATEADD(YEAR, 1,anfangsbestand.datum)";
      }
      else
      {
        filter +=
            " AND buchung.datum < DATE_ADD(anfangsbestand.datum, INTERVAL 1 YEAR)";
      }
      it.leftJoin("buchung", filter);

      it.addFilter("konto.id = ?", konto.getID());
      it.addGroupBy("anfangsbestand.id");
      it.setOrder("Order By anfangsbestand.datum DESC");
      it.setLimit(2);

      if (it.hasNext())
      {
        PseudoDBObject o = it.next();

        getKontoNameInput().setValue(o.getAttribute("konto"));

        getAktJahrAnfangsSaldoInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("anfangssaldo")));
        getAktJahrAnfangsSaldoInput()
            .setComment("am: " + new DateFormatter(new JVDateFormatTTMMJJJJ())
                .format(o.getAttribute("datum")));

        getAktJahrEinnahmenInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("einnahmen")));
        getAktJahrAusgabenInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("ausgaben")));
        getAktJahrSaldoInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("saldo")));
        getAktJahrSaldoInput()
            .setComment("letzte Buchung: "
                + new DateFormatter(new JVDateFormatTTMMJJJJ())
                .format(o.getAttribute("letzte_buchung")));
      }
      else
      {
        getKontoNameInput().setValue("");

        getAktJahrAnfangsSaldoInput().setValue("/");
        getAktJahrAnfangsSaldoInput().setComment("");

        getAktJahrEinnahmenInput().setValue("/");
        getAktJahrAusgabenInput().setValue("/");
        getAktJahrSaldoInput().setValue("/");
        getAktJahrSaldoInput().setComment("");
      }

      if (it.hasNext())
      {
        PseudoDBObject o = it.next();
        getVorJahrAnfangsSaldoInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("anfangssaldo")));
        getVorJahrAnfangsSaldoInput()
            .setComment("am: " + new DateFormatter(new JVDateFormatTTMMJJJJ())
                .format(o.getAttribute("datum")));

        getVorJahrEinnahmenInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("einnahmen")));
        getVorJahrAusgabenInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("ausgaben")));
        getVorJahrSaldoInput()
            .setValue(new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                .format(o.getAttribute("saldo")));
        getVorJahrSaldoInput()
            .setComment("letzte Buchung: "
                + new DateFormatter(new JVDateFormatTTMMJJJJ())
                .format(o.getAttribute("letzte_buchung")));
      }
      else
      {
        getVorJahrAnfangsSaldoInput().setValue("/");
        getVorJahrAnfangsSaldoInput().setComment("");

        getVorJahrEinnahmenInput().setValue("/");
        getVorJahrAusgabenInput().setValue("/");
        getVorJahrSaldoInput().setValue("/");
        getVorJahrSaldoInput().setComment("");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim laden der Kontodaten", e);
    }
  }


  public Input getKontoNameInput()
  {
    if (null == kontoNameInput)
    {
      kontoNameInput = new TextInput("");
      kontoNameInput.disable();
    }
    return kontoNameInput;
  }

  public Input getAktJahrAnfangsSaldoInput()
  {
    if (null == aktJahrAnfangSaldoInput)
    {
      aktJahrAnfangSaldoInput = createTextInput();
    }
    return aktJahrAnfangSaldoInput;
  }

  public Input getAktJahrEinnahmenInput()
  {
    if (null == aktJahrEinnahmenInput)
    {
      aktJahrEinnahmenInput = createTextInput();
    }
    return aktJahrEinnahmenInput;
  }

  public Input getAktJahrAusgabenInput()
  {
    if (null == aktJahrAusgabenInput)
    {
      aktJahrAusgabenInput = createTextInput();
    }
    return aktJahrAusgabenInput;
  }

  public Input getAktJahrSaldoInput()
  {
    if (null == aktJahrSaldoInput)
    {
      aktJahrSaldoInput = createTextInput();
    }
    return aktJahrSaldoInput;
  }

  public Input getVorJahrAnfangsSaldoInput()
  {
    if (null == lastJahrAnfangSaldoInput)
    {
      lastJahrAnfangSaldoInput = createTextInput();
    }
    return lastJahrAnfangSaldoInput;
  }

  public Input getVorJahrEinnahmenInput()
  {
    if (null == lastJahrEinnahmenInput)
    {
      lastJahrEinnahmenInput = createTextInput();
    }
    return lastJahrEinnahmenInput;
  }

  public Input getVorJahrAusgabenInput()
  {
    if (null == lastJahrAusgabenInput)
    {
      lastJahrAusgabenInput = createTextInput();
    }
    return lastJahrAusgabenInput;
  }

  public Input getVorJahrSaldoInput()
  {
    if (null == lastJahrSaldoInput)
    {
      lastJahrSaldoInput = createTextInput();
    }
    return lastJahrSaldoInput;
  }

  private TextInput createTextInput()
  {
    TextInput input = new TextInput("");
    input.setComment("");
    input.disable();
    return input;
  }
}
