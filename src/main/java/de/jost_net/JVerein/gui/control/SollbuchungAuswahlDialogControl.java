/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
import java.util.ArrayList;
import java.util.StringTokenizer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.SollbuchungQuery;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

public class SollbuchungAuswahlDialogControl extends FilterControl
{

  private BetragSummaryTablePart sollbuchungenList;

  private JVereinTablePart mitgliederList;

  private TextInput suchname2 = null;

  private CheckboxInput spezialsuche = null;

  private boolean umwandeln;

  public SollbuchungAuswahlDialogControl(AbstractView view)
  {
    super(view, SollbuchungControl.class);
  }

  public CheckboxInput getSpezialSuche()
  {
    if (spezialsuche != null && !spezialsuche.getControl().isDisposed())
    {
      return spezialsuche;
    }
    spezialsuche = new CheckboxInput(false);
    spezialsuche.setName("Erlaube Teilstring Vergleich");
    spezialsuche.addListener(e -> refreshMitgliederList());

    return spezialsuche;
  }

  public TextInput getSuchName2(boolean newcontrol)
  {
    if (!newcontrol && suchname2 != null)
    {
      return suchname2;
    }
    suchname2 = new TextInput("", 30);
    suchname2.setName("Name");
    return suchname2;
  }

  public JVereinTablePart getTablePart(Action action, boolean umwandeln,
      boolean multi) throws RemoteException
  {
    if (sollbuchungenList != null)
    {
      return sollbuchungenList;
    }
    this.umwandeln = umwandeln;
    @SuppressWarnings("rawtypes")
    GenericIterator sollbuchungen = new SollbuchungQuery(getFilter(), umwandeln,
        null).get();

    sollbuchungenList = new BetragSummaryTablePart(sollbuchungen, null);
    sollbuchungenList.addColumn("Nr", "id-int");
    sollbuchungenList.addColumn(getAbgeschlossenColumn(), false);
    sollbuchungenList.addColumn("Datum", Sollbuchung.DATUM,
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    sollbuchungenList.addColumn("Abrechnungslauf", Sollbuchung.ABRECHNUNGSLAUF);
    sollbuchungenList.addColumn("Mitglied", Sollbuchung.MITGLIED);
    sollbuchungenList.addColumn("Zahler", Sollbuchung.ZAHLER);
    sollbuchungenList.addColumn("Zweck", Sollbuchung.ZWECK1);
    sollbuchungenList.addColumn("Betrag", Sollbuchung.BETRAG,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    sollbuchungenList.addColumn("Zahlungsweg", Sollbuchung.ZAHLUNGSWEG,
        new Formatter()
        {
          @Override
          public String format(Object o)
          {
            return new Zahlungsweg((Integer) o).getText();
          }
        });
    sollbuchungenList.addColumn("Zahlungseingang", Sollbuchung.ISTSUMME,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      sollbuchungenList.addColumn("Rechnung", "rechnung.nummer");
    }
    sollbuchungenList.setMulti(multi);
    if (action == null)
    {
      sollbuchungenList.setContextMenu(new SollbuchungMenu(sollbuchungenList));
      sollbuchungenList.setAction(
          new EditAction(SollbuchungDetailView.class, sollbuchungenList));
      VorZurueckControl.setObjektListe(null, null);
    }
    else
    {
      sollbuchungenList.setAction(action);
    }

    return sollbuchungenList;
  }

  public JVereinTablePart getMitgliederList(Action action, ContextMenu menu)
      throws RemoteException
  {
    GenericIterator<Mitglied> mitglieder = getMitgliedIterator();
    if (mitgliederList == null)
    {
      mitgliederList = new JVereinTablePart(mitglieder, action);
      mitgliederList.addColumn("Name", "name");
      mitgliederList.addColumn("Vorname", "vorname");
      mitgliederList.setContextMenu(menu);
      mitgliederList.setMulti(true);
    }
    else
    {
      mitgliederList.removeAll();
      while (mitglieder.hasNext())
      {
        mitgliederList.addItem(mitglieder.next());
      }
      mitgliederList.sort();
    }
    return mitgliederList;
  }

  public void refreshMitgliederList()
  {
    try
    {
      GenericIterator<Mitglied> mitglieder = getMitgliedIterator();
      mitgliederList.removeAll();
      while (mitglieder.hasNext())
      {
        mitgliederList.addItem(mitglieder.next());
      }
      mitgliederList.sort();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  private GenericIterator<Mitglied> getMitgliedIterator() throws RemoteException
  {
    DBIterator<Mitglied> mitglieder = Einstellungen.getDBService()
        .createList(Mitglied.class);
    // MitgliedUtils.setMitgliedOderSpender(mitglieder);
    if (suchname2 != null && suchname2.getValue() != null)
    {
      StringBuffer where = new StringBuffer();
      ArrayList<String> object = new ArrayList<>();
      StringTokenizer tok = new StringTokenizer((String) suchname2.getValue(),
          " ,-");
      where.append("(");
      boolean first = true;
      while (tok.hasMoreElements())
      {
        if (!first)
        {
          where.append("or ");
        }
        first = false;
        where.append(
            "upper(name) like upper(?) or upper(vorname) like upper(?) ");
        String o = tok.nextToken();
        if ((Boolean) getSpezialSuche().getValue())
        {
          o = "%" + o + "%";
        }
        object.add(o);
        object.add(o);
      }
      where.append(")");
      if (where.length() > 2)
      {
        mitglieder.addFilter(where.toString(), object.toArray());
      }
    }
    mitglieder.setOrder("order by name, vorname");
    return mitglieder;
  }

  public void refreshSollbuchungenList()
  {
    TabRefresh();
  }

  @Override
  protected void TabRefresh()
  {
    if (sollbuchungenList != null)
    {
      try
      {
        @SuppressWarnings("rawtypes")
        GenericIterator sollbuchungen = new SollbuchungQuery(getFilter(),
            umwandeln, null).get();
        sollbuchungenList.removeAll();
        if (sollbuchungen != null)
        {
          while (sollbuchungen.hasNext())
          {
            sollbuchungenList.addItem(sollbuchungen.next());
          }
        }
        sollbuchungenList.sort();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Refresh der Tabelle", e);
      }
    }
  }

  @Override
  protected String getTableTitle()
  {
    return null;
  }

  @Override
  protected String getTableSubtitle()
  {
    return null;
  }

  @Override
  protected String getTableDateiname()
  {
    return null;
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    // Dieser Aufruf wird nur für den Spalten-PanelButton gebraucht, das wird im
    // Dialog nicht benutzt
    return null;
  }

}
