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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.gui.menu.BuchungsartMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.BuchungsartDetailView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.BuchungsartAnzeige;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsartControl extends FilterControl implements Savable
{
  private JVereinTablePart buchungsartList;

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

  public BuchungsartControl(AbstractView view)
  {
    super(view);
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
      steuer.setEnabled(!(boolean) spende.getValue());

      if ((Boolean) spende.getValue())
      {
        steuer.setValue(null);
      }
    });
    return spende;
  }

  public CheckboxInput getAbschreibung() throws RemoteException
  {
    if (abschreibung != null)
    {
      return abschreibung;
    }
    abschreibung = new CheckboxInput(getBuchungsart().getAbschreibung());
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

    // Disable steuer for type spende
    if (getBuchungsart().getSpende())
    {
      steuer.setValue(null);
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

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (buchungsartList != null)
    {
      return buchungsartList;
    }
    buchungsartList = new JVereinTablePart(getBuchungsarten(), null);
    buchungsartList.addColumn("Nummer", "nummer");
    buchungsartList.addColumn("Bezeichnung", "bezeichnung");
    buchungsartList.addColumn("Art", "art", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        if (o instanceof Integer)
        {
          return ArtBuchungsart.get((Integer) o);
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
    buchungsartList.addColumn("Buchungsklasse", "buchungsklasse",
        new BuchungsklasseFormatter());
    buchungsartList.addColumn("Spende", "spende", new JaNeinFormatter());
    buchungsartList.addColumn("Abschreibung", "abschreibung",
        new JaNeinFormatter(), false, Column.ALIGN_RIGHT);
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      buchungsartList.addColumn("Steuer", "steuer", o -> {
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
    buchungsartList.addColumn("Status", "status", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
        {
          return "";
        }
        if (o instanceof Integer)
        {
          return StatusBuchungsart.get((Integer) o);
        }
        return "ungültig";
      }
    }, false, Column.ALIGN_LEFT);
    buchungsartList.addColumn("Suchtext", "suchbegriff");
    buchungsartList.setContextMenu(new BuchungsartMenu(buchungsartList));
    buchungsartList.setMulti(true);
    buchungsartList.setRememberState(true);
    buchungsartList.setAction(
        new EditAction(BuchungsartDetailView.class, buchungsartList));
    VorZurueckControl.setObjektListe(null, null);
    return buchungsartList;
  }

  private DBIterator<Buchungsart> getBuchungsarten()
      throws RemoteException, ApplicationException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Buchungsart> buchungsarten = service
        .createList(Buchungsart.class);

    for (Entry<Filter, Object> entry : getFilter().entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case STATUS:
          if ((Boolean) value)
          {
            buchungsarten.addFilter("status != ?", StatusBuchungsart.INACTIVE);
          }
          break;
        case NUMMER:
          buchungsarten.addFilter("nummer like ?",
              "%" + value.toString().toUpperCase() + "%");
          break;
        case BEZEICHNUNG:
          buchungsarten.addFilter("UPPER(bezeichnung) like ?",
              "%" + value.toString().toUpperCase() + "%");
          break;
        case BUCHUNGSARTART:
          buchungsarten.addFilter("art = ?", ((ArtBuchungsart) value).getKey());
          break;
        case BUCHUNGSKLASSE:
          buchungsarten.addFilter("buchungsklasse = ?",
              ((Buchungsklasse) value).getID());
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    buchungsarten.setOrder("ORDER BY nummer");
    return buchungsarten;
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    try
    {
      if (buchungsartList != null)
      {
        buchungsartList.removeAll();
        DBIterator<Buchungsart> buchungsarten = getBuchungsarten();
        while (buchungsarten.hasNext())
        {
          buchungsartList.addItem(buchungsarten.next());
        }
        buchungsartList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim Refresh der Tabelle", e);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.BUCHUNGSARTEN_DATEINAME, this);
  }
}
