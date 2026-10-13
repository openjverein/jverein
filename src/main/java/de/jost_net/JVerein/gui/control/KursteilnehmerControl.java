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
import java.util.Map.Entry;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.menu.KursteilnehmerMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.KursteilnehmerDetailView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KursteilnehmerControl extends FilterControl
{

  private BetragSummaryTablePart kursteilnehmerList;

  public KursteilnehmerControl(AbstractView view)
  {
    super(view);
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (kursteilnehmerList != null)
    {
      return kursteilnehmerList;
    }
    DBIterator<Kursteilnehmer> kursteilnehmer = getIterator();
    kursteilnehmerList = new BetragSummaryTablePart(kursteilnehmer, null);
    kursteilnehmerList.addColumn("Nr", "id-int");
    kursteilnehmerList.addColumn("Name", "name");
    kursteilnehmerList.addColumn("Vorname", "vorname");
    kursteilnehmerList.addColumn("Straße", "strasse");
    kursteilnehmerList.addColumn("PLZ", "plz");
    kursteilnehmerList.addColumn("Ort", "ort");
    kursteilnehmerList.addColumn("Verwendungszweck", "vzweck1");
    kursteilnehmerList.addColumn("BIC", "bic");
    kursteilnehmerList.addColumn("IBAN", "iban", new IBANFormatter());
    kursteilnehmerList.addColumn("Kontoinhaber", "kontoinhaber");
    kursteilnehmerList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    kursteilnehmerList.addColumn("Mandats-ID", "mandatid");
    kursteilnehmerList.addColumn("Eingabedatum", "eingabedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kursteilnehmerList.addColumn("Abbuchungsdatum", "abbudatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    kursteilnehmerList
        .setContextMenu(new KursteilnehmerMenu(kursteilnehmerList));
    kursteilnehmerList.setMulti(true);
    kursteilnehmerList.setAction(
        new EditAction(KursteilnehmerDetailView.class, kursteilnehmerList));
    VorZurueckControl.setObjektListe(null, null);
    return kursteilnehmerList;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {

    try
    {
      if (kursteilnehmerList == null)
      {
        return;
      }
      kursteilnehmerList.removeAll();
      DBIterator<Kursteilnehmer> kursteilnehmer = getIterator();
      while (kursteilnehmer.hasNext())
      {
        Kursteilnehmer kt = kursteilnehmer.next();
        kursteilnehmerList.addItem(kt);
      }
      kursteilnehmerList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Kursteilnehmer> getIterator()
      throws RemoteException, ApplicationException
  {
    DBIterator<Kursteilnehmer> kursteilnehmer = Einstellungen.getDBService()
        .createList(Kursteilnehmer.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case NAME:
          kursteilnehmer.addFilter("name like ?", "%" + value.toString() + "%");
          break;
        case VERWENDUNGSZWECK:
          kursteilnehmer.addFilter("(lower(vzweck1) like ?)",
              "%" + value.toString().toLowerCase() + "%");
          break;
        case EINGABEDATUM_VON:
          kursteilnehmer.addFilter("eingabedatum >= ?", value);
          break;
        case EINGABEDATUM_BIS:
          kursteilnehmer.addFilter("eingabedatum <= ?", value);
          break;
        case ABBUCHUNGSDATUM_VON:
          kursteilnehmer.addFilter("abbudatum >= ?", value);
          break;
        case ABBUCHUNGSDATUM_BIS:
          kursteilnehmer.addFilter("abbudatum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    return kursteilnehmer;
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.KURSTEILNEHMER_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.KURSTEILNEHMER_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.KURSTEILNEHMER_DATEINAME, this);
  }
}
