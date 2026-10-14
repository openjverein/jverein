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
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.BuchungsartAnzeige;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsartDetailControl extends VorZurueckControl implements Savable
{

  private TextInput nummer;

  private Input bezeichnung;

  private SelectInput art;

  private SelectInput buchungsklasse;

  private CheckboxInput spende;

  private CheckboxInput abschreibung;

  private SelectInput steuer;

  private Buchungsart buchungsart;

  private SelectInput status;

  private TextInput suchbegriff;

  private CheckboxInput regexp;

  private boolean isAbgeschlossen = false;

  public BuchungsartDetailControl(AbstractView view)
  {
    super(view);
    if (getBuchungsart() != null)
    {
      try
      {
        isAbgeschlossen = getBuchungsart().isAbgeschlossen();
      }
      catch (RemoteException e)
      {
        String fehler = "Fehler beim Lesen der Datenbank. Siehe system log";
        Logger.error(fehler, e);
      }
    }
  }

  private Buchungsart getBuchungsart()
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = (Buchungsart) getCurrentObject();
    return buchungsart;
  }

  public TextInput getNummer(boolean withFocus) throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }
    nummer = new TextInput(getBuchungsart().getNummer(), 50);
    if (withFocus)
    {
      nummer.focus();
    }
    nummer.setMandatory(true);
    if (isAbgeschlossen)
    {
      nummer.disable();
    }
    return nummer;
  }

  public Input getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getBuchungsart().getBezeichnung(), 80);
    bezeichnung.setMandatory(true);
    if (isAbgeschlossen)
    {
      bezeichnung.disable();
    }
    return bezeichnung;
  }

  public SelectInput getArt() throws RemoteException
  {
    if (art != null)
    {
      return art;
    }
    art = new SelectInput(ArtBuchungsart.getArray(),
        new ArtBuchungsart(getBuchungsart().getArt()));
    if (isAbgeschlossen)
    {
      art.disable();
    }
    return art;
  }

  public SelectInput getStatus() throws RemoteException
  {
    if (status != null)
    {
      return status;
    }
    status = new SelectInput(StatusBuchungsart.getArray(),
        new StatusBuchungsart(getBuchungsart().getStatus()));
    return status;
  }

  public TextInput getSuchbegriff() throws RemoteException
  {
    if (suchbegriff != null)
    {
      return suchbegriff;
    }
    suchbegriff = new TextInput(getBuchungsart().getSuchbegriff(), 150);
    return suchbegriff;
  }

  public CheckboxInput getRegexp() throws RemoteException
  {
    if (regexp != null)
    {
      return regexp;
    }
    regexp = new CheckboxInput(getBuchungsart().getRegexp());
    return regexp;
  }

  public CheckboxInput getSpende() throws RemoteException
  {
    if (spende != null)
    {
      return spende;
    }
    spende = new CheckboxInput(getBuchungsart().getSpende());
    spende.addListener(event -> {
      steuer.setEnabled(!(boolean) spende.getValue() && !isAbgeschlossen);

      if ((Boolean) spende.getValue())
      {
        steuer.setValue(null);
      }
    });
    if (getBuchungsart().getSteuer() != null)
    {
      spende.setValue(false);
      spende.disable();
    }
    return spende;
  }

  public CheckboxInput getAbschreibung() throws RemoteException
  {
    if (abschreibung != null)
    {
      return abschreibung;
    }
    abschreibung = new CheckboxInput(getBuchungsart().getAbschreibung());
    if (isAbgeschlossen)
    {
      abschreibung.disable();
    }
    return abschreibung;
  }

  public SelectInput getSteuer() throws RemoteException
  {
    if (steuer != null)
    {
      return steuer;
    }
    steuer = new SteuerInput(getBuchungsart().getSteuer());

    steuer.setAttribute("name");
    steuer.setPleaseChoose("Keine Steuer");
    steuer.addListener(event -> {
      spende.setEnabled(steuer.getValue() == null && !isAbgeschlossen);
    });

    // Disable steuer for type spende
    if (getBuchungsart().getSpende())
    {
      steuer.setValue(null);
      steuer.disable();
    }
    if (isAbgeschlossen)
    {
      steuer.disable();
    }
    return steuer;
  }

  private String getBuchungartAttribute()
  {
    try
    {
      switch ((Integer) Einstellungen
          .getEinstellung(Property.BUCHUNGSARTANZEIGE))
      {
        case BuchungsartAnzeige.NUMMER_BEZEICHNUNG:
          return "nrbezeichnung";
        case BuchungsartAnzeige.BEZEICHNUNG_NUMMER:
          return "bezeichnungnr";
        default:
          return "bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }

    return "bezeichnung";
  }

  private String getBuchungartSortOrder()
  {
    try
    {
      switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
      {
        case BuchungsartSort.NACH_NUMMER:
          return "ORDER BY nummer";
        default:
          return "ORDER BY bezeichnung";
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Keine Buchungssortierung hinterlegt.";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }

    return "ORDER BY bezeichnung";
  }

  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    list.setOrder(getBuchungartSortOrder());
    buchungsklasse = new SelectInput(
        list != null ? PseudoIterator.asList(list) : null,
        getBuchungsart().getBuchungsklasse());
    buchungsklasse.setValue(getBuchungsart().getBuchungsklasse());
    buchungsklasse.setAttribute(getBuchungartAttribute());
    buchungsklasse.setPleaseChoose("Bitte auswählen");
    if (isAbgeschlossen)
    {
      buchungsklasse.disable();
    }
    return buchungsklasse;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Buchungsart b = getBuchungsart();
    if (getNummer(false).getValue() != null)
    {
      b.setNummer((String) getNummer(false).getValue());
    }

    b.setBezeichnung((String) getBezeichnung().getValue());
    ArtBuchungsart ba = (ArtBuchungsart) getArt().getValue();
    b.setArt(ba.getKey());
    if (buchungsklasse != null)
    {
      GenericObject o = (GenericObject) getBuchungsklasse().getValue();
      if (o != null)
      {
        b.setBuchungsklasseId(Long.valueOf(o.getID()));
      }
      else
      {
        b.setBuchungsklasseId(null);
      }
    }
    else
    {
      b.setBuchungsklasseId(null);
    }
    b.setSpende((Boolean) spende.getValue());
    b.setAbschreibung((Boolean) abschreibung.getValue());
    StatusBuchungsart st = (StatusBuchungsart) getStatus().getValue();
    b.setStatus(st.getKey());
    b.setSuchbegriff((String) getSuchbegriff().getValue());
    b.setRegexp((Boolean) getRegexp().getValue());
    if (steuer != null)
    {
      b.setSteuer((Steuer) steuer.getValue());
    }
    return b;
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
      String fehler = "Fehler bei speichern der Buchungsart";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
