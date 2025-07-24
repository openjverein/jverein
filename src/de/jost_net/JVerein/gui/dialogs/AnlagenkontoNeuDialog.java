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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.KontoControl;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.BuchungsControl.Kontenfilter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.IntegerNullInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zur Zuordnung einer Buchungsart.
 */
public class AnlagenkontoNeuDialog extends AbstractDialog<Konto>
{

  private TextInput nummer;

  private TextInput bezeichnung;

  private AbstractInput anlagenart;

  private SelectInput buchungsklasse;

  private AbstractInput afaart;

  private IntegerNullInput nutzungsdauer;

  private LabelInput message = null;

  private Konto konto = null;

  private Buchung buchung = null;

  final KontoControl control = new KontoControl(null);

  /**
   * @param position
   */
  public AnlagenkontoNeuDialog(int position, Buchung buchung)
  {
    super(position);
    setTitle("Neues Anlagenkonto");
    setSize(650, SWT.DEFAULT);
    this.buchung = buchung;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Nummer", getNummer());
    group.addLabelPair("Bezeichnung", getBezeichnung());
    group.addLabelPair("Anlagen Buchungsklasse", getBuchungsklasse());
    group.addLabelPair("Anlagen Buchungsart", getAnlagenart());
    group.addLabelPair("AfA Buchungsart", getAfaart());
    group.addLabelPair("Nutzungsdauer", getNutzungsdauer());
    group.addLabelPair("", getMessage());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          if (konto == null)
            konto = (Konto) Einstellungen.getDBService()
                .createObject(Konto.class, null);
        }
        catch (RemoteException e)
        {
          konto = null;
          return;
        }
        handleStore();
        close();
      }
    }, null, true, "ok.png");

    buttons.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        konto = null;
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
  public Konto getData() throws Exception
  {
    return konto;
  }

  private void handleStore() throws ApplicationException
  {
    try
    {
      konto.setNummer((String) getNummer().getValue());
      konto.setBezeichnung((String) getBezeichnung().getValue());
      konto.setEroeffnung(buchung.getDatum());
      konto.setKontoArt(Kontoart.ANLAGE);
      konto.setHibiscusId(-1);
      konto.setAnlagenartId(getSelectedAnlagenartId());
      konto.setBuchungsklasseId(getSelectedBuchungsklasseId());
      konto.setAfaartId(getSelectedAfaartId());
      konto.setAfaRestwert(
          (Double) Einstellungen.getEinstellung(Property.AFARESTWERT));
      konto.setAfaMode(AfaMode.AUTO);
      konto.setNutzungsdauer((Integer) getNutzungsdauer().getValue());
      konto.setKommentar(buchung.getKommentar());
      konto.store();
      BuchungsControl bcontrol = new BuchungsControl(null,
          Kontenfilter.ANLAGEKONTO);
      bcontrol.getSettings().setAttribute("anlagenkonto.kontoid",
          konto.getID());
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e.getMessage());
    }
    catch (ApplicationException e)
    {
      throw new ApplicationException(e.getMessage());
    }
  }

  public TextInput getNummer() throws RemoteException
  {
    if (nummer != null)
    {
      return nummer;
    }
    nummer = new TextInput("", 35);
    return nummer;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput("", 255);
    return bezeichnung;
  }

  public Input getAnlagenart() throws RemoteException
  {
    if (anlagenart != null)
    {
      return anlagenart;
    }
    anlagenart = new BuchungsartInput().getBuchungsartInput(anlagenart,
        buchung.getBuchungsart(), buchungsarttyp.ANLAGENART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    anlagenart.addListener(new AnlagenartListener());
    return anlagenart;
  }

  private Long getSelectedAnlagenartId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getAnlagenart().getValue();
      if (null == buchungsArt)
        return null;
      Long id = Long.valueOf(buchungsArt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Anlagensart kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    list.setOrder(control.getBuchungartSortOrder());
    Buchungsklasse bk = buchung.getBuchungsklasse();
    if (bk == null && buchung.getBuchungsart() != null)
      bk = buchung.getBuchungsart().getBuchungsklasse();
    if (bk != null)
    {
      buchungsklasse = new SelectInput(
          list != null ? PseudoIterator.asList(list) : null, bk);
    }
    else
    {
      buchungsklasse = new SelectInput(
          list != null ? PseudoIterator.asList(list) : null, null);
    }
    buchungsklasse.setAttribute(control.getBuchungartAttribute());
    buchungsklasse.setPleaseChoose("Bitte auswählen");
    return buchungsklasse;
  }

  private Long getSelectedBuchungsklasseId() throws ApplicationException
  {
    try
    {
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse()
          .getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public Input getAfaart() throws RemoteException
  {
    if (afaart != null)
    {
      return afaart;
    }
    afaart = new BuchungsartInput().getBuchungsartInput(afaart, null,
        buchungsarttyp.AFAART, (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    return afaart;
  }

  private Long getSelectedAfaartId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getAfaart().getValue();
      if (null == buchungsArt)
        return null;
      Long id = Long.valueOf(buchungsArt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsart kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  private LabelInput getMessage() throws RemoteException
  {
    if (message != null)
    {
      return message;
    }
    DBService service = Einstellungen.getDBService();
    String sql = "SELECT DISTINCT konto.id from konto "
        + "WHERE (kontoart = ?) ";
    boolean exist = (boolean) service.execute(sql,
        new Object[] { Kontoart.ANLAGE.getKey() }, new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            if (rs.next())
            {
              return true;
            }
            return false;
          }
        });
    if (!exist)
      message = new LabelInput(
          " *Beim ersten Anlagenkonto bitte JVerein neu starten um die Änderungen anzuwenden");
    else
      message = new LabelInput("");
    message.setColor(Color.ERROR);
    return message;
  }

  public IntegerNullInput getNutzungsdauer() throws RemoteException
  {
    if (nutzungsdauer != null)
    {
      return nutzungsdauer;
    }
    nutzungsdauer = new IntegerNullInput();
    return nutzungsdauer;
  }

  public class AnlagenartListener implements Listener
  {

    AnlagenartListener()
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
        Buchungsart ba = (Buchungsart) getAnlagenart().getValue();
        if (ba != null)
        {
          if (getBuchungsklasse().getValue() == null)
            getBuchungsklasse().setValue(ba.getBuchungsklasse());
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
      }
    }
  }

}
