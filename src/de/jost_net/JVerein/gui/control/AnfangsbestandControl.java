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
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.AnfangsbestandMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.AnfangsbestandDetailView;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnfangsbestandControl extends FilterControl implements Savable
{

  private JVereinTablePart anfangsbestandList;

  private TextInput konto;

  private DateInput datum;

  private DecimalInput betrag;

  private Anfangsbestand anfangsbestand;

  private boolean editable = false;

  public AnfangsbestandControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Anfangsbestand getAnfangsbestand()
  {
    if (anfangsbestand != null)
    {
      return anfangsbestand;
    }
    anfangsbestand = (Anfangsbestand) getCurrentObject();
    return anfangsbestand;
  }

  public boolean isAnfangsbestandEditable() throws RemoteException
  {
    if (getAnfangsbestand().getJahresabschluss() != null)
    {
      GUI.getStatusBar()
          .setErrorText("Anfangsbestand ist bereits abgeschlossen.");
      return editable = false;
    }
    return editable = true;
  }

  public TextInput getKonto() throws RemoteException
  {
    if (konto != null)
    {
      return konto;
    }
    konto = new TextInput(getAnfangsbestand().getKonto().getNummer(), 35);
    konto.setEnabled(false);
    return konto;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    datum = new DateInput(getAnfangsbestand().getDatum());
    datum.focus();
    if (!getAnfangsbestand().isNewObject())
    {
      datum.setEnabled(false);
    }
    datum.setMandatory(true);
    return datum;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getAnfangsbestand().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setMandatory(true);
    betrag.setEnabled(editable);
    return betrag;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Anfangsbestand a = getAnfangsbestand();
    // Konto ist schon gesetzt und ist nicht editierbar
    a.setDatum((Date) getDatum().getValue());
    a.setBetrag((Double) getBetrag().getValue());
    return a;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Anfangsbestandes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getAnfangsbestandList() throws RemoteException
  {
    if (anfangsbestandList != null)
    {
      return anfangsbestandList;
    }
    anfangsbestandList = new JVereinTablePart(getAnfangsstaende(), null);
    anfangsbestandList.addColumn("Nummer", "nummer");
    anfangsbestandList.addColumn("Bezeichnung", "bezeichnung");
    anfangsbestandList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    anfangsbestandList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    anfangsbestandList.setRememberColWidths(true);
    anfangsbestandList
        .setContextMenu(new AnfangsbestandMenu(anfangsbestandList));
    anfangsbestandList.setRememberOrder(true);
    anfangsbestandList.removeFeature(FeatureSummary.class);
    anfangsbestandList.setAction(
        new EditAction(AnfangsbestandDetailView.class, anfangsbestandList));
    VorZurueckControl.setObjektListe(null, null);
    return anfangsbestandList;
  }

  @Override
  public void TabRefresh()
  {
    if (anfangsbestandList == null)
    {
      return;
    }
    anfangsbestandList.removeAll();
    try
    {
      DBIterator<Anfangsbestand> anfangsbestaende = getAnfangsstaende();
      while (anfangsbestaende.hasNext())
      {
        anfangsbestandList.addItem(anfangsbestaende.next());
      }
      anfangsbestandList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  private DBIterator<Anfangsbestand> getAnfangsstaende() throws RemoteException
  {
    DBIterator<Anfangsbestand> anfangsbestaende = Einstellungen.getDBService()
        .createList(Anfangsbestand.class);
    anfangsbestaende.join("konto");
    anfangsbestaende.addFilter("konto.id = anfangsbestand.konto");
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        anfangsbestaende.addFilter("(lower(bezeichnung) like ?)",
            new Object[] { "%" + tmpSuchname.toLowerCase() + "%" });
      }
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        anfangsbestaende.addFilter("(lower(nummer) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%" });
      }
    }
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      anfangsbestaende.addFilter("datum >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      anfangsbestaende.addFilter("datum <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    anfangsbestaende.setOrder("ORDER BY konto, datum desc");
    return anfangsbestaende;
  }
}
