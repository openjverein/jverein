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
package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.logging.Logger;

/**
 * Dialog zur Zuordnung einer Buchungsart.
 */
public class BuchungsartZuordnungDialog extends AbstractDialog<Buchungsart>
{

  private SelectInput buchungsarten = null;

  private CheckboxInput ueberschreiben = null;

  private LabelInput status = null;

  private Buchungsart buchungsart = null;

  private boolean ueberschr;
  
  private int unterdrueckunglaenge = 0;
  
  private boolean abort = true;

  /**
   * @param position
   */
  public BuchungsartZuordnungDialog(int position)
  {
    super(position);
    setTitle("Zuordnung Buchungsart");
    setSize(400, 175);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Buchungsart", getBuchungsartAuswahl());
    group.addLabelPair("Buchungsarten überschreiben", getUeberschreiben());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        if (buchungsarten.getValue() == null)
        {
          status.setValue("Bitte auswählen");
          status.setColor(Color.ERROR);
          return;
        }
        if (buchungsarten.getValue() instanceof Buchungsart)
        {
          buchungsart = (Buchungsart) buchungsarten.getValue();
        }
        ueberschr = (Boolean) getUeberschreiben().getValue();
        abort = false;
        close();
      }
    }, null, true, "ok.png");
    buttons.addButton("Entfernen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        buchungsart = null;
        abort = false;
        close();
      }
    }, null, false, "user-trash-full.png");
    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, false, "process-stop.png");

    buttons.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Buchungsart getData() throws Exception
  {
    return buchungsart;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungsart;
  }

  public boolean getOverride()
  {
    return ueberschr;
  }
  
  public boolean getAbort()
  {
    return abort;
  }

  private SelectInput getBuchungsartAuswahl() throws RemoteException
  {
    if (buchungsarten != null)
    {
      return buchungsarten;
    }
    unterdrueckunglaenge = Einstellungen.getEinstellung().getUnterdrueckungLaenge();
    if (unterdrueckunglaenge > 0) 
    {
      final DBService service = Einstellungen.getDBService();
      Calendar cal = Calendar.getInstance();
      Date db = cal.getTime();
      cal.add(Calendar.MONTH, - unterdrueckunglaenge);
      Date dv = cal.getTime();
      String sql = "SELECT buchungsart.* from buchungsart, buchung ";
      sql += "WHERE buchung.buchungsart = buchungsart.id ";
      sql += "AND buchung.datum >= ? AND buchung.datum <= ? ";
      sql += "ORDER BY nummer";
      Logger.debug(sql);
      ResultSetExtractor rs = new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs) throws RemoteException, SQLException
        {
          ArrayList<Buchungsart> list = new ArrayList<Buchungsart>();
          while (rs.next())
          {
            list.add(
              (Buchungsart) service.createObject(Buchungsart.class, rs.getString(1)));
          }
          return list;
        }
      };
      @SuppressWarnings("unchecked")
      ArrayList<Buchungsart> ergebnis = (ArrayList<Buchungsart>) service.execute(sql,
          new Object[] { dv, db }, rs);
      int size = ergebnis.size();
      Buchungsart bua;
      for (int i = 0; i < size; i++)
      {
        bua = ergebnis.get(i);
        for (int j = i + 1; j < size; j++)
        {
          if (bua.getNummer() == ergebnis.get(j).getNummer())
          {
            ergebnis.remove(j);
            j--;
            size--;
          }
        }
      }
      buchungsarten = new SelectInput(ergebnis.toArray(), null);
    }
    else
    {
      DBIterator<Buchungsart> it = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      it.setOrder("ORDER BY nummer");
      buchungsarten = new SelectInput(PseudoIterator.asList(it), null);
    }

    buchungsarten.setValue(null);
    buchungsarten.setAttribute("nrbezeichnung");
    buchungsarten.setPleaseChoose("Bitte Buchungsart auswählen");
    buchungsarten.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        status.setValue("");
      }
    });
    return buchungsarten;
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  private CheckboxInput getUeberschreiben()
  {
    if (ueberschreiben != null)
    {
      return ueberschreiben;
    }
    ueberschreiben = new CheckboxInput(false);
    return ueberschreiben;
  }
}
