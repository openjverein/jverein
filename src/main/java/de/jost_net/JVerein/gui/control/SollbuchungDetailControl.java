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
import java.util.Date;
import java.util.List;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Messaging.SollbuchungMessage;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.SollbuchungPositionNeuAction;
import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.BuchungPartBearbeitenMenu;
import de.jost_net.JVerein.gui.menu.SollbuchungPositionMenu;
import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.NewButton;
import de.jost_net.JVerein.gui.parts.SollbuchungPositionListPart;
import de.jost_net.JVerein.gui.view.SollbuchungPositionDetailView;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SollbuchungDetailControl extends VorZurueckControl implements Savable
{
  // SollbuchungDetailView
  private DateInput datum = null;

  private TextAreaInput zweck1;

  private SelectInput zahlungsweg;

  private DecimalInput betrag;

  private AbstractInput mitglied;

  private AbstractInput zahler;

  private ButtonRtoL neuButton;

  private Sollbuchung sollbuchung;

  private JVereinTablePart buchungList;

  private BuchungListPart istbuchungList;

  private boolean editable = false;

  private SollbuchungMessageConsumer sollbuchungConsumer = null;

  public SollbuchungDetailControl(AbstractView view)
  {
    super(view);
  }

  // Aufruf durch SollbuchungNeuDialog
  public SollbuchungDetailControl(AbstractView view, Sollbuchung sollb)
  {
    super(view);
    sollbuchung = sollb;
  }

  private Sollbuchung getSollbuchung() throws RemoteException
  {
    if (sollbuchung != null)
    {
      return sollbuchung;
    }
    sollbuchung = Einstellungen.getDBService().createObject(Sollbuchung.class,
        ((Sollbuchung) getCurrentObject()).getID());
    GUI.getCurrentView().setCurrentObject(sollbuchung);
    return sollbuchung;
  }

  public void setSollbuchung(Sollbuchung sollb)
  {
    sollbuchung = sollb;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }

    Date d = new Date();
    if (getSollbuchung() != null)
    {
      d = getSollbuchung().getDatum();
    }

    this.datum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Datum wählen");
    this.datum.setMandatory(true);
    datum.setEnabled(editable);
    return datum;
  }

  public TextAreaInput getZweck1() throws RemoteException
  {
    if (zweck1 != null)
    {
      return zweck1;
    }
    String z = "";
    if (getSollbuchung() != null)
    {
      z = getSollbuchung().getZweck1();
    }
    zweck1 = new TextAreaInput(z, 500);
    zweck1.setHeight(30);
    zweck1.setMandatory(true);
    zweck1.setEnabled(editable);
    return zweck1;
  }

  public SelectInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }
    Integer z = null;
    if (getSollbuchung() != null)
    {
      z = getSollbuchung().getZahlungsweg();
    }
    ArrayList<Zahlungsweg> weg = Zahlungsweg.getArray();

    zahlungsweg = new SelectInput(weg,
        z == null
            ? new Zahlungsweg(
                (Integer) Einstellungen.getEinstellung(Property.ZAHLUNGSWEG))
            : new Zahlungsweg(getSollbuchung().getZahlungsweg()));
    zahlungsweg.setName("Zahlungsweg");
    zahlungsweg.setEnabled(editable);
    return zahlungsweg;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    Double b = Double.valueOf(0.0d);
    if (getSollbuchung() != null)
    {
      b = getSollbuchung().getBetrag();
    }
    betrag = new DecimalInput(b, Einstellungen.DECIMALFORMAT);
    betrag.setEnabled(false);
    return betrag;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Sollbuchung sollb = getSollbuchung();
    sollb.setZahlerId(getSelectedZahlerId());
    // Betrag hier nicht setzen sonst kann es zu Änderungsmeldungen kommen
    // wenn Positionen gelöscht werden
    sollb.setDatum((Date) getDatum().getValue());
    Zahlungsweg zw = (Zahlungsweg) getZahlungsweg().getValue();
    sollb.setZahlungsweg(zw.getKey());
    sollb.setZweck1((String) getZweck1().getValue());
    sollb.setMitglied((Mitglied) getMitglied().getValue());
    return sollb;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim speichern der Sollbuchung";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    if (getSollbuchung().getMitglied() != null)
    {
      Mitglied[] mitgliedArray = { getSollbuchung().getMitglied() };
      mitglied = new SelectInput(mitgliedArray, getSollbuchung().getMitglied());
      mitglied.setEnabled(false);
    }
    else
    {
      mitglied = new MitgliedInput().getMitgliedInput(mitglied, null,
          (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
      if (mitglied instanceof SelectInput)
      {
        ((SelectInput) mitglied).setPleaseChoose("Bitte auswählen");
        ((SelectInput) mitglied).setPreselected(null);
      }
      mitglied.setEnabled(editable);
    }
    mitglied.setMandatory(true);
    return mitglied;
  }

  public Input getZahler() throws RemoteException
  {
    if (zahler != null)
    {
      return zahler;
    }
    zahler = new MitgliedInput().getMitgliedInput(zahler,
        getSollbuchung().getZahler(),
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    if (zahler instanceof SelectInput)
    {
      ((SelectInput) zahler).setPleaseChoose("Bitte auswählen");
      if (getSollbuchung().getZahler() == null)
      {
        ((SelectInput) zahler).setPreselected(null);
      }
    }
    zahler.setMandatory(true);
    zahler.setEnabled(editable);
    return zahler;
  }

  private Long getSelectedZahlerId() throws ApplicationException
  {
    try
    {
      if (zahler == null)
      {
        return null;
      }
      Mitglied derZahler = (Mitglied) getZahler().getValue();
      if (null == derZahler)
      {
        return null;
      }
      return Long.valueOf(derZahler.getID());
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählter Zahler kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public ButtonRtoL getNeuButton() throws RemoteException
  {
    if (neuButton != null)
    {
      return neuButton;
    }
    neuButton = new NewButton(new SollbuchungPositionNeuAction(),
        getSollbuchung());
    return neuButton;
  }

  public JVereinTablePart getSollbuchungPositionList() throws RemoteException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    List<SollbuchungPosition> list = getSollbuchung()
        .getSollbuchungPositionList();

    if (!editable)
    {
      buchungList = new SollbuchungPositionListPart(list, null);
    }
    else
    {
      buchungList = new SollbuchungPositionListPart(list,
          new EditAction(SollbuchungPositionDetailView.class));
    }

    if (editable)
    {
      buchungList.setContextMenu(new SollbuchungPositionMenu());
    }
    buchungList.setMulti(true);
    sollbuchungConsumer = new SollbuchungMessageConsumer();
    Application.getMessagingFactory()
        .registerMessageConsumer(sollbuchungConsumer);
    return buchungList;
  }

  public JVereinTablePart getBuchungList() throws RemoteException
  {
    if (istbuchungList != null)
    {
      return istbuchungList;
    }
    istbuchungList = new BuchungListPart(getSollbuchung().getBuchungList(),
        new BuchungAction(false), new BuchungPartBearbeitenMenu());
    return istbuchungList;
  }

  public boolean isSollbuchungEditable() throws RemoteException
  {
    if (getSollbuchung().getRechnung() != null)
    {
      GUI.getStatusBar().setErrorText(
          "Sollbuchung kann nicht bearbeitet werden. Es wurde bereits eine Rechnung über diese Sollbuchung erstellt.");
      return editable = false;
    }
    if (getSollbuchung().getAbrechnungslauf() != null
        && getSollbuchung().getAbrechnungslauf().getAbgeschlossen())
    {
      GUI.getStatusBar().setErrorText(
          "Sollbuchung kann nicht bearbeitet werden. Der zugehörige Abrechnungslauf ist abgeschlossen.");
      return editable = false;
    }
    return editable = true;
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class SollbuchungMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[] { SollbuchungMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            Sollbuchung sollb = (Sollbuchung) ((SollbuchungMessage) message)
                .getObject();
            getBetrag().setValue(sollb.getBetrag());
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("Fehler beim Update der Sollbuchung Betrag Anzeige.",
                e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(SollbuchungMessageConsumer.this);
          }
        }
      });
    }
  }

  public PanelButton getDetailSpaltenPanelButton()
  {
    return new PanelButton("document-properties.png", context -> {
      try
      {
        new TabelleSpaltenAuswahlDialog(getSollbuchungPositionList(),
            getBuchungList()).open();
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Spalten-Auswahl-Dialog");
      }
    }, "Spalten auswählen");
  }

  public void deregisterSollbuchungConsumer()
  {
    Application.getMessagingFactory()
        .unRegisterMessageConsumer(sollbuchungConsumer);
  }

}
