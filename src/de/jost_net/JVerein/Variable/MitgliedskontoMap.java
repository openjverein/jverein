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
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;

public class MitgliedskontoMap
{

  public MitgliedskontoMap()
  {
    //
  }

  public Map<String, Object> getMap(ArrayList<Mitgliedskonto> mk,
      Map<String, Object> inma) throws RemoteException
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
    CurrencyFormatter formatter = new CurrencyFormatter("%",format);
    double summe = 0;
    double saldo = 0;
    double suist = 0;
    for (Mitgliedskonto mkto : mk)
    {
      buda.add(mkto.getDatum());
      zg.add(mkto.getZweck1());
      zg1.add(mkto.getZweck1());
      nettobetrag.add(Double.valueOf(mkto.getNettobetrag()));
      steuersatz.add("("+formatter.format(Double.valueOf(mkto.getSteuersatz()))+")");
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
    map.put(MitgliedskontoVar.BUCHUNGSDATUM.getName(), buda.toArray());
    map.put(MitgliedskontoVar.ZAHLUNGSGRUND.getName(), zg.toArray());
    map.put(MitgliedskontoVar.ZAHLUNGSGRUND1.getName(), zg1.toArray());
    map.put(MitgliedskontoVar.NETTOBETRAG.getName(), nettobetrag.toArray());
    map.put(MitgliedskontoVar.STEUERSATZ.getName(), steuersatz.toArray());
    map.put(MitgliedskontoVar.STEUERBETRAG.getName(), steuerbetrag.toArray());
    map.put(MitgliedskontoVar.BETRAG.getName(), betrag.toArray());
    map.put(MitgliedskontoVar.IST.getName(), ist.toArray());
    map.put(MitgliedskontoVar.DIFFERENZ.getName(), differenz.toArray());
    map.put(MitgliedskontoVar.STAND.getName(), new Double(-1 * saldo));
    map.put(MitgliedskontoVar.SUMME_OFFEN.getName(), new Double(saldo));
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

    map.put(MitgliedskontoVar.BUCHUNGSDATUM.getName(), mk.getDatum());
    map.put(MitgliedskontoVar.ZAHLUNGSGRUND.getName(), mk.getZweck1());
    map.put(MitgliedskontoVar.ZAHLUNGSGRUND1.getName(), mk.getZweck1());
    map.put(MitgliedskontoVar.NETTOBETRAG.getName(), mk.getNettobetrag());
    map.put(MitgliedskontoVar.STEUERSATZ.getName(), mk.getSteuersatz());
    map.put(MitgliedskontoVar.STEUERBETRAG.getName(), mk.getSteuerbetrag());
    map.put(MitgliedskontoVar.BETRAG.getName(), mk.getBetrag());
    map.put(MitgliedskontoVar.IST.getName(), mk.getIstSumme());
    map.put(MitgliedskontoVar.DIFFERENZ.getName(),
        mk.getBetrag() - mk.getIstSumme());
    return map;
  }
}
