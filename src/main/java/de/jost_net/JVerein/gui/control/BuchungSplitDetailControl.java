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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.SplitbuchungAufloesenAction;
import de.jost_net.JVerein.gui.action.SplitbuchungNeuAction;
import de.jost_net.JVerein.gui.dialogs.SammelueberweisungAuswahlDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.SplitBuchungMenu;
import de.jost_net.JVerein.gui.parts.NewButton;
import de.jost_net.JVerein.gui.parts.SaveButton;
import de.jost_net.JVerein.gui.parts.SplitbuchungListTablePart;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.SplitBuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungSplitDetailControl extends VorZurueckControl
    implements Savable
{
  private SplitbuchungListTablePart splitbuchungsList;

  private SplitBuchung splitBuchung;

  private Boolean editable;

  private boolean checkChanged = true;

  public BuchungSplitDetailControl(AbstractView view)
  {
    super(view);
  }

  public SplitBuchung getSplitBuchung()
  {
    if (splitBuchung != null)
    {
      return splitBuchung;
    }
    splitBuchung = (SplitBuchung) getCurrentObject();
    return splitBuchung;
  }

  public Part getSplitBuchungsList() throws RemoteException
  {
    if (splitbuchungsList != null)
    {
      return splitbuchungsList;
    }
    splitbuchungsList = new SplitbuchungListTablePart(
        getSplitBuchung().getBuchungen(), c -> {
          checkNoChanges();
          new BuchungAction(true).handleAction(c);
        });
    splitbuchungsList.addColumn("Nr", "id-int");
    splitbuchungsList.addColumn("Konto", "konto");
    splitbuchungsList.addColumn("Typ", "splittyp",
        o -> SplitbuchungTyp.get((Integer) o));
    splitbuchungsList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    splitbuchungsList.addColumn("Auszug", "auszugsnummer");
    splitbuchungsList.addColumn("Blatt", "blattnummer");
    splitbuchungsList.addColumn("Name", "name");
    splitbuchungsList.addColumn("Verwendungszweck", "zweck");
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      splitbuchungsList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    splitbuchungsList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    splitbuchungsList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      splitbuchungsList.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        splitbuchungsList.addColumn("Steuer", "steuer", o -> {
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
    splitbuchungsList.addColumn("Mitglied - Sollbuchung", Buchung.SOLLBUCHUNG,
        new SollbuchungFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
    {
      splitbuchungsList.addColumn("Projekt", "projekt");
    }
    splitbuchungsList.setContextMenu(new SplitBuchungMenu(this));
    splitbuchungsList.setFormatter(item -> {
      if (item == null)
      {
        return;
      }
      try
      {
        Buchung b = (Buchung) item.getData();
        if (b.isToDelete())
        {
          item.setForeground(new Color(null, new RGB(255, 0, 0)));
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to format line", e);
      }
    });
    splitbuchungsList.setMulti(true);
    return splitbuchungsList;
  }

  public void refreshSplitbuchungen() throws RemoteException
  {
    if (splitbuchungsList == null)
    {
      return;
    }
    splitbuchungsList.removeAll();

    for (Buchung b : getSplitBuchung().getBuchungen())
    {
      splitbuchungsList.addItem(b);
    }
    splitbuchungsList.sort();
  }

  public Button getSammelueberweisungButton() throws ApplicationException
  {
    Button sammel = new Button("Sammelüberweisung", context -> {
      try
      {
        Buchung master = getSplitBuchung().getMaster();
        SammelueberweisungAuswahlDialog suad = new SammelueberweisungAuswahlDialog(
            master);

        SepaSammelUeberweisung su = suad.open();
        if (su != null)
        {
          for (SepaSammelUeberweisungBuchung ssub : su.getBuchungen())
          {
            Buchung b = (Buchung) Einstellungen.getDBService()
                .createObject(Buchung.class, null);
            b.setAuszugsnummer(master.getAuszugsnummer());
            b.setBetrag(BigDecimal.valueOf(ssub.getBetrag()).negate()
                .setScale(2, RoundingMode.HALF_UP));
            b.setBlattnummer(master.getBlattnummer());
            b.setBuchungsartId(master.getBuchungsartId());
            b.setBuchungsklasseId(master.getBuchungsklasseId());
            b.setDatum(su.getAusfuehrungsdatum());
            b.setKonto(master.getKonto());
            b.setName(ssub.getGegenkontoName());
            b.setSplitId(master.getSplitId());
            b.setSplitTyp(SplitbuchungTyp.SPLIT);
            b.setZweck(ssub.getZweck());
            getSplitBuchung().addBuchung(b);
          }
          refreshSplitbuchungen();
        }
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim übernehmen der Sammelüberweisung", e);
      }
    }, null, false, "list.png");
    sammel.setEnabled(isEditable());
    return sammel;
  }

  private boolean isEditable() throws ApplicationException
  {
    if (editable != null)
    {
      return editable;
    }
    try
    {
      for (Buchung buchung : getSplitBuchung().getBuchungen())
      {
        Jahresabschluss ja = buchung.getJahresabschluss();
        if (ja != null)
        {
          GUI.getStatusBar().setErrorText(String.format(
              "Buchung wurde bereits am %s von %s abgeschlossen.",
              new JVDateFormatTTMMJJJJ().format(ja.getDatum()), ja.getName()));
          editable = false;
          return false;
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Status der aktuellen Buchung kann nicht geprüft werden.", e);
    }
    editable = true;
    return true;
  }

  @Override
  public boolean hasChanged() throws RemoteException
  {
    if (!checkChanged)
    {
      return false;
    }
    try
    {
      getSplitBuchung().prepareStore();
    }
    catch (Exception e)
    {
      return true;
    }
    return getSplitBuchung().hasChanged();
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    // Da wir kein JVereinDBObject haben, null zurückgeben (wird nur in
    // AbstractDetailView verwendet, und da ist ein null-Check)
    return null;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      getSplitBuchung().prepareStore();
      getSplitBuchung().store();
      refreshSplitbuchungen();
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public Button getNewButton() throws ApplicationException
  {
    Button neu = new NewButton(c -> {
      checkNoChanges();
      new SplitbuchungNeuAction().handleAction(c);
    }, getSplitBuchung());
    neu.setEnabled(isEditable());
    return neu;
  }

  public Button getAufloesenButton() throws ApplicationException
  {
    Button aufloesen = new Button("Auflösen",
        new SplitbuchungAufloesenAction(this), getCurrentObject(), false,
        "unlocked.png");
    aufloesen.setEnabled(isEditable());
    return aufloesen;
  }

  public Button getSaveButton() throws ApplicationException
  {
    Button speichern = new SaveButton(this);
    speichern.setEnabled(isEditable());
    return speichern;
  }

  // stellt dir überprüfung auf Änderungen ab
  public void checkNoChanges()
  {
    checkChanged = false;
  }
}
