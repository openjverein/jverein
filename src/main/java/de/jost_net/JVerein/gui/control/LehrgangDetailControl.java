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
import java.rmi.RemoteException;
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.LehrgangsmitgliederAction;
import de.jost_net.JVerein.gui.dialogs.NichtMitgliedNeuDialog;
import de.jost_net.JVerein.gui.dialogs.PersonenartDialog;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.gui.menu.LehrgangsmitgliederMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangDetailControl extends VorZurueckControl implements Savable
{

  private SelectInput lehrgangsart;

  private DateInput von = null;

  private DateInput bis = null;

  private TextInput veranstalter = null;

  private Lehrgang lehrgang = null;

  private TextInput bezeichnung;

  private BigDecimalInput betrag;

  private JVereinTablePart mitgliederList;

  public LehrgangDetailControl(AbstractView view)
  {
    super(view);
  }

  public Lehrgang getLehrgang()
  {
    if (lehrgang != null)
    {
      return lehrgang;
    }
    lehrgang = (Lehrgang) getCurrentObject();
    return lehrgang;
  }

  public SelectInput getLehrgangsart() throws RemoteException
  {
    if (lehrgangsart != null)
    {
      return lehrgangsart;
    }
    DBIterator<Lehrgangsart> it = Einstellungen.getDBService()
        .createList(Lehrgangsart.class);
    it.setOrder("order by bezeichnung");
    lehrgangsart = new SelectInput(
        it != null ? PseudoIterator.asList(it) : null,
        getLehrgang().getLehrgangsart());
    lehrgangsart.setPleaseChoose("Bitte auswählen");
    lehrgangsart.setMandatory(true);
    return lehrgangsart;
  }

  public DateInput getVon() throws RemoteException
  {
    if (von != null)
    {
      return von;
    }

    Date d = getLehrgang().getVon();

    this.von = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.von.setTitle("Datum");
    this.von.setText("Bitte (Beginn-)Datum wählen");
    von.setMandatory(true);
    return von;
  }

  public DateInput getBis() throws RemoteException
  {
    if (bis != null)
    {
      return bis;
    }

    Date d = getLehrgang().getBis();

    this.bis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.bis.setTitle("Datum");
    this.bis.setText("Bitte Ende-Datum wählen");
    return bis;
  }

  public Input getVeranstalter() throws RemoteException
  {
    if (veranstalter != null)
    {
      return veranstalter;
    }
    veranstalter = new TextInput(getLehrgang().getVeranstalter(), 50);
    return veranstalter;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getLehrgang().getBezeichnung(), 30);
    bezeichnung.setMandatory(true);
    return bezeichnung;
  }

  public BigDecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new BigDecimalInput(getLehrgang().getBetrag(),
        Einstellungen.BIGDECIMALFORMAT);
    return betrag;
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    if (mitgliederList != null)
    {
      return mitgliederList;
    }

    mitgliederList = new JVereinTablePart(PseudoIterator.asList(getIterator()),
        new LehrgangsmitgliederAction(LehrgangsmitgliederAction.ART.BEARBEITEN,
            null));
    mitgliederList.setTableName("Mitglieder");
    mitgliederList.addColumn("Nr", "id");
    mitgliederList.addColumn("Anrede", "anrede");
    mitgliederList.addColumn("Titel", "titel");
    mitgliederList.addColumn("Name", "name");
    mitgliederList.addColumn("Vorname", "vorname");
    mitgliederList.addColumn("Straße", "strasse");
    mitgliederList.addColumn("Adressieringszusatz", "adressieringszusatz");
    mitgliederList.addColumn("Ort", "ort");
    mitgliederList.addColumn("Ergebnis", "ergebnis");

    mitgliederList.setMulti(true);
    mitgliederList.setContextMenu(new LehrgangsmitgliederMenu(this));

    return mitgliederList;
  }

  public void refreshMitgliederList()
  {
    if (mitgliederList == null)
    {
      return;
    }
    try
    {
      ExtendedDBIterator<PseudoDBObject> it = getIterator();
      mitgliederList.removeAll();
      while (it.hasNext())
      {
        mitgliederList.addItem(it.next());
      }
      mitgliederList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  public ExtendedDBIterator<PseudoDBObject> getIterator() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "mitglied");
    it.join("lehrgangsteilnehmer",
        "lehrgangsteilnehmer.mitglied = mitglied.id");
    it.addFilter("lehrgangsteilnehmer.lehrgang = ?", getLehrgang().getID());
    return it;
  }

  public Button getLehrgangsteilnehmerNeuButton()
  {
    return new Button("Neu",
        new LehrgangsmitgliederAction(LehrgangsmitgliederAction.ART.NEU, this),
        null, false, "document-new.png");
  }

  public Button getNichtMitgliedErzeugenButton()
  {
    Button b = new Button("Neues Nicht-Mitglied anlegen", event -> {

      try
      {
        Mitglied nm = Einstellungen.getDBService().createObject(Mitglied.class,
            null);
        if ((Boolean) Einstellungen
            .getEinstellung(Property.JURISTISCHEPERSONEN))
        {
          PersonenartDialog pad = new PersonenartDialog(
              PersonenartDialog.POSITION_CENTER);
          String pa = pad.open();
          if (pa == null)
          {
            return;
          }
          nm.setPersonenart(pa);
        }
        else
        {
          nm.setPersonenart("n");
        }
        NichtMitgliedNeuDialog dialog = new NichtMitgliedNeuDialog(
            NichtMitgliedNeuDialog.POSITION_CENTER, nm);
        if (!dialog.open())
        {
          if (dialog.getStatus() != null)
          {
            throw new Exception(dialog.getStatus());
          }
          else
          {
            Lehrgangsteilnehmer tln = Einstellungen.getDBService()
                .createObject(Lehrgangsteilnehmer.class, null);
            tln.setLehrgang(Long.valueOf(getLehrgang().getID()));
            tln.setLehrgangsart(
                Long.valueOf(getLehrgang().getLehrgangsart().getID()));
            tln.setMitglied(Integer.valueOf(nm.getID()));
            tln.setVeranstalter(getLehrgang().getVeranstalter());
            tln.setVon(getLehrgang().getVon());
            tln.setBis(getLehrgang().getBis());
            tln.store();
            refreshMitgliederList();
          }
        }
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception ex)
      {
        throw new ApplicationException(
            "Fehler beim Erzeugen eines Nicht-Mitgliedes: " + ex.getMessage());
      }
    }, null, false, "document-new.png");
    // button
    return b;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Lehrgang l = getLehrgang();

    Lehrgangsart la = (Lehrgangsart) getLehrgangsart().getValue();
    if (la != null)
    {
      l.setLehrgangsart(Long.valueOf(la.getID()));
    }
    else
    {
      l.setLehrgangsart(null);
    }
    l.setBezeichnung((String) getBezeichnung().getValue());
    l.setVon((Date) getVon().getValue());
    l.setBis((Date) getBis().getValue());
    l.setVeranstalter((String) getVeranstalter().getValue());
    l.setBetrag((BigDecimal) getBetrag().getValue());
    return l;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
      Lehrgangsart la = (Lehrgangsart) getLehrgangsart().getValue();
      if (la != null)
      {
        DBIterator<Lehrgangsteilnehmer> teilnehmer = Einstellungen
            .getDBService().createList(Lehrgangsteilnehmer.class);
        teilnehmer.addFilter("lehrgang = ?", getLehrgang().getID());
        while (teilnehmer.hasNext())
        {
          Lehrgangsteilnehmer t = teilnehmer.next();
          t.setVon((Date) getVon().getValue());
          t.setBis((Date) getBis().getValue());
          t.setVeranstalter((String) getVeranstalter().getValue());
          t.setLehrgangsart(Long.valueOf(la.getID()));
          t.store();
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Lehrgangs";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANG_TEILNEHMER_TITEL,
        getLehrgang());
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANG_TEILNEHMER_SUBTITEL,
        getLehrgang());
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.LEHRGANG_TEILNEHMER_DATEINAME,
        getLehrgang());
  }
}
