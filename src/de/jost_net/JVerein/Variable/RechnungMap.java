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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.FormularfeldControl;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Einstellung;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;

public class RechnungMap
{

  public RechnungMap()
  {
    //
  }

  public Map<String, Object> getMap(Rechnung re, Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }

    ArrayList<Date> buda = new ArrayList<>();
    ArrayList<String> zg = new ArrayList<>();
    ArrayList<String> zg1 = new ArrayList<>();
    ArrayList<Double> nettobetrag = new ArrayList<>();
    ArrayList<String> steuersatz = new ArrayList<>();
    ArrayList<Double> steuerbetrag = new ArrayList<>();
    ArrayList<Double> betrag = new ArrayList<>();
    ArrayList<Double> ist = new ArrayList<>();
    ArrayList<Double> differenz = new ArrayList<>();

    DecimalFormat format = new DecimalFormat("0");
    CurrencyFormatter formatter = new CurrencyFormatter("%", format);
    double summe = 0;
    double saldo = 0;
    double suist = 0;
    for (Mitgliedskonto mkto : re.getMitgliedskontoList())
    {
      buda.add(mkto.getDatum());
      zg.add(mkto.getZweck1());
      zg1.add(mkto.getZweck1());
      nettobetrag.add(Double.valueOf(mkto.getNettobetrag()));
      steuersatz.add(
          "(" + formatter.format(Double.valueOf(mkto.getSteuersatz())) + ")");
      steuerbetrag.add(Double.valueOf(mkto.getSteuerbetrag()));
      betrag.add(Double.valueOf(mkto.getBetrag()));
      ist.add(mkto.getIstSumme());
      suist += mkto.getIstSumme();
      differenz.add(mkto.getBetrag() - mkto.getIstSumme());
      summe += mkto.getBetrag();
      saldo += mkto.getBetrag() - mkto.getIstSumme();
    }
    if (buda.size() > 1)
    {
      if (Einstellungen.getEinstellung().getOptiert())
      {
        zg1.add("Rechnungsbetrag inkl. USt.");
        zg.add("Rechnungsbetrag inkl. USt.");
      }
      else
      {
        zg1.add("Summe");
        zg.add("Summe");
      }
      betrag.add(summe);
      differenz.add(saldo);
      ist.add(suist);
    }
    map.put(FormularfeldControl.BUCHUNGSDATUM, buda.toArray());
    map.put(FormularfeldControl.ZAHLUNGSGRUND, zg.toArray());
    map.put(FormularfeldControl.ZAHLUNGSGRUND1, zg1.toArray());
    map.put(FormularfeldControl.BETRAG, betrag.toArray());
    map.put(RechnungVar.BUCHUNGSDATUM.getName(), buda.toArray());
    map.put(RechnungVar.ZAHLUNGSGRUND.getName(), zg.toArray());
    map.put(RechnungVar.ZAHLUNGSGRUND1.getName(), zg1.toArray());
    map.put(RechnungVar.NETTOBETRAG.getName(), nettobetrag.toArray());
    map.put(RechnungVar.STEUERSATZ.getName(), steuersatz.toArray());
    map.put(RechnungVar.STEUERBETRAG.getName(), steuerbetrag.toArray());
    map.put(RechnungVar.BETRAG.getName(), betrag.toArray());
    map.put(RechnungVar.IST.getName(), ist.toArray());
    map.put(RechnungVar.DIFFERENZ.getName(), differenz.toArray());
    map.put(RechnungVar.STAND.getName(), Double.valueOf(-1 * saldo));
    map.put(RechnungVar.SUMME_OFFEN.getName(), Double.valueOf(saldo));
    map.put(RechnungVar.QRCODE_INTRO.getName(),
        Einstellungen.getEinstellung().getQRCodeIntro());

    map.put(RechnungVar.DATUM.getName(), re.getDatum());
    map.put(RechnungVar.NUMMER.getName(), StringTool.lpad(re.getID(),
        Einstellungen.getEinstellung().getZaehlerLaenge(), "0"));

    map.put(RechnungVar.PERSONENART.getName(), re.getPersonenart());
    map.put(RechnungVar.GESCHLECHT.getName(), re.getGeschlecht());
    map.put(RechnungVar.ANREDE.getName(), re.getAnrede());
    map.put(RechnungVar.ANREDE_DU.getName(),
        Adressaufbereitung.getAnredeDu(re));
    map.put(RechnungVar.ANREDE_DU.getName(),
        Adressaufbereitung.getAnredeFoermlich(re));
    map.put(RechnungVar.TITEL.getName(), re.getTitel());
    map.put(RechnungVar.NAME.getName(), re.getName());
    map.put(RechnungVar.VORNAME.getName(), re.getVorname());
    map.put(RechnungVar.STRASSE.getName(), re.getStrasse());
    map.put(RechnungVar.ADRESSIERUNGSZUSATZ.getName(),
        re.getAdressierungszusatz());
    map.put(RechnungVar.PLZ.getName(), re.getPlz());
    map.put(RechnungVar.ORT.getName(), re.getOrt());
    map.put(RechnungVar.STAAT.getName(), re.getStaat());
    map.put(RechnungVar.MANDATID.getName(), re.getMandatID());
    map.put(RechnungVar.MANDATDATUM.getName(), re.getMandatDatum());
    map.put(RechnungVar.BIC.getName(), re.getBIC());
    map.put(RechnungVar.IBAN.getName(), re.getIBAN());
    map.put(RechnungVar.IBANMASKIERT.getName(),
        VarTools.maskieren(re.getIBAN()));
    map.put(RechnungVar.EMPFAENGER.getName(),
        Adressaufbereitung.getAdressfeld(re));

    return map;
  }

  public Map<String, Object> getMap(Mitgliedskonto mk, Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }

    map.put(RechnungVar.BUCHUNGSDATUM.getName(), mk.getDatum());
    map.put(RechnungVar.ZAHLUNGSGRUND.getName(), mk.getZweck1());
    map.put(RechnungVar.ZAHLUNGSGRUND1.getName(), mk.getZweck1());
    map.put(RechnungVar.NETTOBETRAG.getName(), mk.getNettobetrag());
    map.put(RechnungVar.STEUERSATZ.getName(), mk.getSteuersatz());
    map.put(RechnungVar.STEUERBETRAG.getName(), mk.getSteuerbetrag());
    map.put(RechnungVar.BETRAG.getName(), mk.getBetrag());
    map.put(RechnungVar.IST.getName(), mk.getIstSumme());
    map.put(RechnungVar.DIFFERENZ.getName(), mk.getBetrag() - mk.getIstSumme());
    return map;
  }
}
