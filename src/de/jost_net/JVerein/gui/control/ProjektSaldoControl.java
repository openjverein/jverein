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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ProjektSaldoControl extends BuchungsklasseSaldoControl
{
  private SelectInput projekt;

  public ProjektSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    gruppenBezeichnung = "Projekt";
    mitOhneBuchungsart = false;
    mitBuchungsklasseSpalte = true;
    spalteGruppe = PROJEKT;
  }

  public SelectInput getProjekt() throws RemoteException
  {
    if (projekt != null)
    {
      return projekt;
    }
    Projekt p = getDefaultProjekt();
    projekt = new SelectInput(getProjektliste(), p);
    projekt.setAttribute("bezeichnung");
    projekt.setPleaseChoose("Keine Einschränkung");
    projekt.addListener(new ProjektListener());
    if (p != null)
    {
      mitGesamtSaldo = false;
    }
    else
    {
      mitGesamtSaldo = true;
    }
    return projekt;
  }

  public class ProjektListener implements Listener
  {

    ProjektListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      try
      {
        if (saldoList == null)
        {
          return;
        }
        saveProjektSettings();
        ArrayList<PseudoDBObject> zeile = getList();
        getSaldoList().removeAll();
        for (PseudoDBObject sz : zeile)
        {
          getSaldoList().addItem(sz);
        }
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
      catch (ApplicationException e)
      {
        Logger.error("Fehler bei neu laden der Liste.");
      }
    }
  }

  private List<Projekt> getProjektliste() throws RemoteException
  {
    List<Projekt> projektliste = new ArrayList<>();
    DBIterator<Projekt> list = Einstellungen.getDBService()
        .createList(Projekt.class);
    // Nur Projekte anzeigen die in dem Zeitraum aktiv sind
    list.addFilter("(startdatum is null or startdatum <= ?)",
        getDatumbis().getDate());
    list.addFilter("(endedatum is null or endedatum >= ?)",
        getDatumvon().getDate());
    list.setOrder("ORDER BY bezeichnung");
    while (list.hasNext())
    {
      Projekt p = list.next();
      projektliste.add(p);
    }
    return projektliste;
  }

  private Projekt getDefaultProjekt() throws RemoteException
  {
    List<String> ids = new ArrayList<>();
    DBIterator<Projekt> list = Einstellungen.getDBService()
        .createList(Projekt.class);
    // Nur Projekte anzeigen die in dem Zeitraum aktiv sind
    list.addFilter("(startdatum is null or startdatum <= ?)",
        getDatumbis().getDate());
    list.addFilter("(endedatum is null or endedatum >= ?)",
        getDatumvon().getDate());
    list.setOrder("ORDER BY bezeichnung");
    while (list.hasNext())
    {
      Projekt p = list.next();
      ids.add(p.getID());
    }
    Projekt p = null;
    String pid = settings.getString("projekt", "");
    if (pid.length() > 0 && ids.contains(pid))
    {
      try
      {
        p = (Projekt) Einstellungen.getDBService().createObject(Projekt.class,
            pid);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann kein Default
      }
    }
    return p;
  }

  private void saveProjektSettings() throws RemoteException
  {
    Projekt p = (Projekt) getProjekt().getValue();
    if (p != null)
    {
      settings.setAttribute("projekt", p.getID());
      mitGesamtSaldo = false;
    }
    else
    {
      settings.setAttribute("projekt", "");
      mitGesamtSaldo = true;
    }
  }

  @Override
  public void reloadList() throws ApplicationException
  {
    try
    {
      projekt.setList(getProjektliste());
      projekt.setValue(getDefaultProjekt());
      saveProjektSettings();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler bei neu laden der Liste.");
    }
    super.reloadList();
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    // Den Iterator aus BuchungsklasseSaldo erweitern um nach Projekten statt
    // nach Buchungsklassen zu gruppieren
    ExtendedDBIterator<PseudoDBObject> it = super.getIterator();

    it.addColumn("projekt.bezeichnung as " + PROJEKT);
    it.addColumn("projekt.id as " + PROJEKT_ID);
    it.join("projekt", "buchung.projekt = projekt.id");
    Projekt p = (Projekt) getProjekt().getValue();
    if (p != null)
    {
      it.addFilter("projekt.id = ?", p.getID());
    }
    it.addGroupBy("projekt.id");
    it.addGroupBy("projekt.bezeichnung");

    return it;
  }

  @Override
  protected ExtendedDBIterator<PseudoDBObject> getSteuerIterator()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = super.getSteuerIterator();
    it.join("projekt", "buchung.projekt = projekt.id");
    Projekt p = (Projekt) getProjekt().getValue();
    if (p != null)
    {
      it.addFilter("projekt.id = ?", p.getID());
    }
    it.addGroupBy("buchung.projekt");
    it.addColumn("projekt.id as " + PROJEKT_ID);
    it.addColumn("projekt.bezeichnung as " + PROJEKT);

    return it;
  }

  @Override
  protected void sortList(List<PseudoDBObject> list) throws RemoteException
  {
    super.sortList(list);
    list.sort((o1, o2) -> {
      try
      {
        if (o1.getAttribute(PROJEKT) == null)
        {
          return 1;
        }
        if (o2.getAttribute(PROJEKT) == null)
        {
          return -1;
        }

        return ((String) o1.getAttribute(PROJEKT))
            .compareTo((String) o2.getAttribute(PROJEKT));
      }
      catch (RemoteException e)
      {
        return 0;
      }
    });
  }

  @Override
  protected String getAuswertungTitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_TITEL, this);
  }

  @Override
  protected String getAuswertungSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_SUBTITEL, this);
  }

  @Override
  protected String getDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.PROJEKTSALDO_DATEINAME, this);
  }
}
