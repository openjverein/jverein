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
import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.menu.SteuerMenue;
import de.jost_net.JVerein.gui.view.SteuerDetailView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SteuerControl extends AbstractControl implements Savable
{

  private TablePart steuerList;

  private TextInput name;

  private DecimalInput satz;

  private AbstractInput buchungsart;

  private CheckboxInput aktiv;

  public SteuerControl(AbstractView view)
  {
    super(view);
  }

  public Steuer getSteuer()
  {
    return (Steuer) getCurrentObject();
  }

  public TablePart getSteuerList() throws ApplicationException
  {
    try
    {
      if (steuerList != null)
      {
        return steuerList;
      }
      DBIterator<Steuer> steuern = Einstellungen.getDBService()
          .createList(Steuer.class);

      steuerList = new TablePart(steuern,
          new EditAction(SteuerDetailView.class));
      steuerList.addColumn("Name", "name");
      steuerList.addColumn("Steuersatz", "satz",
          o -> {
            return (Double) o + "%";
          }, false,
          Column.ALIGN_RIGHT);
      steuerList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());
      steuerList.addColumn("Aktiv", "aktiv", new JaNeinFormatter());
      steuerList.setContextMenu(new SteuerMenue());
      steuerList.setRememberColWidths(true);
      steuerList.removeFeature(FeatureSummary.class);
      steuerList.setMulti(true);
      return steuerList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getSteuer().getName(), 50);
    name.setMandatory(true);
    return name;
  }

  public DecimalInput getSatz() throws RemoteException
  {
    if (satz != null)
    {
      return satz;
    }
    satz = new DecimalInput(getSteuer().getSatz(), new DecimalFormat("##.##"));
    satz.setComment("%");
    satz.setMandatory(true);
    return satz;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput()
        .getBuchungsartInput(buchungsart, getSteuer().getBuchungsart(), null,
            (Integer) Einstellungen
                .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsart.setMandatory(true);
    buchungsart.setComment("");
    Listener listener = new Listener()
    {
      @Override
      public void handleEvent(Event e)
      {
        String comment = "";
        if (buchungsart.getValue() != null)
        {
          try
          {
            switch (((Buchungsart) buchungsart.getValue()).getArt())
            {
              case ArtBuchungsart.AUSGABE:
                comment = "Ausgabe -> Vorsteuer";
                break;
              case ArtBuchungsart.EINNAHME:
                comment = "Einnahme -> Umsatzsteuer";
                break;
              case ArtBuchungsart.UMBUCHUNG:
                comment = "Umbuchung ist Ungültig";
                break;
            }
          }
          catch (RemoteException re)
          {
            Logger.error("Fehler", re);
          }
        }
        buchungsart.setComment(comment);
      }
    };
    buchungsart.addListener(listener);
    listener.handleEvent(null);
    return buchungsart;
  }

  public CheckboxInput getAktiv() throws RemoteException
  {
    if (aktiv != null)
    {
      return aktiv;
    }
    aktiv = new CheckboxInput(getSteuer().getAktiv());
    return aktiv;
  }

  @Override
  public DBObject prepareStore() throws RemoteException, ApplicationException
  {
    Steuer s = getSteuer();
    s.setName((String) getName().getValue());
    s.setSatz((Double) getSatz().getValue());
    if (getBuchungsart().getValue() != null)
    {
      s.setBuchungsartId(
          Long.parseLong(((Buchungsart) getBuchungsart().getValue()).getID()));
    }
    s.setAktiv((Boolean) getAktiv().getValue());
    return s;
  }

  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore();
      Steuer s = getSteuer();
      s.store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Steuer";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }
}
