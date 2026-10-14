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
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.BuchungSollbuchungZuordnungAutomatischAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.BuchungMenu;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.util.AfaUtil;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.Kontenfilter;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsControl extends FilterControl
{

  private BuchungListTablePart buchungsList;

  private BuchungQuery query;

  private Vector<Listener> changeKontoListener = new Vector<>();

  private boolean geldkonto = true;

  public BuchungsControl(AbstractView view, Kontenfilter kontenfilter)
  {
    super(view);
    settingsprefix = BuchungDetailControl.GELDKONTO_PREFIX;
    if (kontenfilter == Kontenfilter.ANLAGEKONTO)
    {
      geldkonto = false;
      settingsprefix = BuchungDetailControl.ANLAGENKONTO_PREFIX;
    }
  }

  @Override
  public BuchungListTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (buchungsList != null)
    {
      return buchungsList;
    }

    saveFilterSettings();

    query = new BuchungQuery(getFilter(), geldkonto);

    List<Buchung> buchungen = query.get();

    buchungsList = new BuchungListTablePart(buchungen,
        new BuchungAction(false, null));
    buchungsList.addColumn("Nr", "id-int");
    buchungsList.addColumn(getAbgeschlossenColumn(), false);
    buchungsList.addColumn("Geprüft", "geprueft", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return (Boolean) o ? "\u2705" : "";
      }
    });
    if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG))
    {
      buchungsList.addColumn("D", "document");
    }
    buchungsList.addColumn("S", "splittyp", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        Integer typ = (Integer) o;
        return SplitbuchungTyp.get(typ).substring(0, 1);
      }
    });

    buchungsList.addColumn("Konto", "konto");
    buchungsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    if (geldkonto)
    {
      buchungsList.addColumn("Auszugsnummer", "auszugsnummer");
      buchungsList.addColumn("Blatt", "blattnummer");
    }

    buchungsList.addColumn("Name", "name");
    if (geldkonto)
      buchungsList.addColumn("IBAN oder Kontonummer", "iban",
          new IBANFormatter());
    buchungsList.addColumn("Verwendungszweck", "zweck", new Formatter()
    {
      @Override
      public String format(Object value)
      {
        if (value == null)
        {
          return null;
        }
        String s = value.toString();
        s = s.replaceAll("\r\n", " ");
        s = s.replaceAll("\r", " ");
        s = s.replaceAll("\n", " ");
        return s;
      }
    });
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      buchungsList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }

    buchungsList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    buchungsList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT) && geldkonto)
    {
      buchungsList.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        buchungsList.addColumn("Steuer", "steuer", o -> {
          if (o == null)
          {
            return "";
          }
          try
          {
            return ((Steuer) o).getName();
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
          return "";
        }, false, Column.ALIGN_RIGHT);
      }
    }
    if (geldkonto)
      buchungsList.addColumn(new Column(Buchung.SOLLBUCHUNG,
          "Mitglied - Sollbuchung", new SollbuchungFormatter(), false,
          Column.ALIGN_AUTO, Column.SORT_BY_DISPLAY));
    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      buchungsList.addColumn("Projekt", "projekt");
    }
    buchungsList.addColumn("Abrechnungslauf", "abrechnungslauf");
    if ((Boolean) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
    {
      buchungsList.addColumn("Spendenbescheinigung", "spendenbescheinigung");
    }
    buchungsList.addColumn(
        new Column("kommentar", "Kommentar", v -> v.toString().split("\n")[0]),
        false);
    buchungsList.setMulti(true);
    buchungsList.setContextMenu(new BuchungMenu(this, buchungsList));
    buchungsList.setRememberState(true);
    buchungsList.updateSaldo((Konto) getFilterValue(Filter.KONTO));
    buchungsList.setAction(new BuchungAction(false, buchungsList));
    VorZurueckControl.setObjektListe(null, null);
    informKontoChangeListener();

    return buchungsList;
  }

  private void informKontoChangeListener() throws RemoteException
  {
    Konto k = (Konto) getFilterValue(Filter.KONTO);
    Event event = new Event();
    event.data = k;
    for (Listener listener : changeKontoListener)
    {
      listener.handleEvent(event);
    }
  }

  public void addKontoChangeListener(Listener listener)
  {
    this.changeKontoListener.add(listener);
  }

  public void refreshBuchungsList() throws ApplicationException
  {
    try
    {
      saveFilterSettings();

      query = new BuchungQuery(getFilter(), geldkonto);

      List<Buchung> buchungen = query.get();

      buchungsList.updateSaldo((Konto) getFilterValue(Filter.KONTO));
      buchungsList.removeAll();

      for (Buchung bu : buchungen)
      {
        buchungsList.addItem(bu);
      }
      // Summenzeile neu laden
      buchungsList.featureEvent(
          de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null);
      buchungsList.sort();
      informKontoChangeListener();
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public boolean getGeldkonto()
  {
    return geldkonto;
  }

  public BuchungQuery getQuery()
  {
    return query;
  }

  public Button getStarteBuchungSollbuchungZuordnungAutomatischButton(
      DateInput vonDatum, DateInput bisDatum)
  {
    Button b = new Button("Zuordnung",
        new BuchungSollbuchungZuordnungAutomatischAction(vonDatum, bisDatum),
        null, false, "user-friends.png");
    return b;
  }

  public Button getAfaButton()
  {
    Button b = new Button("Erzeuge Abschreibungen", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          new AfaUtil(new Geschaeftsjahr(new Date()), null);
          refreshBuchungsList();
        }
        catch (RemoteException e)
        {
          GUI.getStatusBar()
              .setErrorText("Fehler bei der Erstellung der Abschreibungen");
        }
        catch (ParseException ex)
        {
          GUI.getStatusBar().setErrorText(ex.getLocalizedMessage());
        }
      }
    }, null, false, "document-new.png");
    return b;
  }

  @Override
  protected String getTableTitle()
  {
    if (geldkonto)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_TITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGEN_TITEL, this);
    }
  }

  @Override
  protected String getTableSubtitle()
  {
    if (geldkonto)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_SUBTITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGEN_SUBTITEL, this);
    }
  }

  @Override
  protected String getTableDateiname()
  {
    if (geldkonto)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGEN_DATEINAME, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGEN_DATEINAME, this);
    }
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    Date von = (Date) getFilterValue(Filter.DATUM_VON);
    Date bis = (Date) getFilterValue(Filter.DATUM_BIS);
    if (von == null)
    {
      GUI.getStatusBar().setErrorText("Bitte Von Datum eingeben!");
      return;
    }
    if (bis == null)
    {
      GUI.getStatusBar().setErrorText("Bitte Bis Datum eingeben!");
      return;
    }
    if (von.after(bis))
    {
      GUI.getStatusBar().setErrorText("Von Datum ist nach Bis Datum!");
      return;
    }
    refreshBuchungsList();
  }

}
