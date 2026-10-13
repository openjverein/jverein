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
import java.util.Date;
import java.util.List;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.BuchungPartAnzeigenMenu;
import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.SpbAdressaufbereitung;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungDetailControl extends VorZurueckControl
    implements Savable
{

  private SelectInput spendenart;

  private AbstractInput mitglied;

  private TextInput zeile1;

  private TextInput zeile2;

  private TextInput zeile3;

  private TextInput zeile4;

  private TextInput zeile5;

  private TextInput zeile6;

  private TextInput zeile7;

  private DateInput spendedatum;

  private DateInput bescheinigungsdatum;

  private DecimalInput betrag;

  private FormularInput formular;

  private CheckboxInput ersatzaufwendungen;

  private TextAreaInput bezeichnungsachzuwendung;

  private SelectInput herkunftspende;

  private CheckboxInput unterlagenwertermittlung;

  private Spendenbescheinigung spendenbescheinigung;

  private DateInput versanddatum;

  final static String ExportPDF = ".pdf";

  final static String ExportCSV = ".csv";

  public SpendenbescheinigungDetailControl(AbstractView view)
  {
    super(view);
  }

  private Spendenbescheinigung getSpendenbescheinigung()
  {
    if (spendenbescheinigung != null)
    {
      return spendenbescheinigung;
    }
    spendenbescheinigung = (Spendenbescheinigung) getCurrentObject();
    return spendenbescheinigung;
  }

  public SelectInput getSpendenart() throws RemoteException
  {
    if (spendenart != null)
    {
      return spendenart;
    }
    spendenart = new SelectInput(Spendenart.getArray(),
        new Spendenart(getSpendenbescheinigung().getSpendenart()));
    spendenart.setEnabled(false);
    return spendenart;
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    Mitglied m = getSpendenbescheinigung().getMitglied();
    mitglied = new MitgliedInput().getMitgliedInput(mitglied, m,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    mitglied.addListener(new MitgliedListener());
    if (mitglied instanceof SelectInput)
    {
      ((SelectInput) mitglied).setPleaseChoose("Optional auswählen");
      if (m == null)
      {
        ((SelectInput) mitglied).setPreselected(null);
      }
    }
    if (m != null || !getSpendenbescheinigung().isNewObject())
    {
      mitglied.disable();
    }
    return mitglied;
  }

  public TextInput getZeile1(boolean withFocus) throws RemoteException
  {
    if (zeile1 != null)
    {
      return zeile1;
    }
    zeile1 = new TextInput(getSpendenbescheinigung().getZeile1(), 80);
    if (withFocus)
    {
      zeile1.focus();
    }
    zeile1.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile1;
  }

  public TextInput getZeile2() throws RemoteException
  {
    if (zeile2 != null)
    {
      return zeile2;
    }
    zeile2 = new TextInput(getSpendenbescheinigung().getZeile2(), 80);
    zeile2.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile2;
  }

  public TextInput getZeile3() throws RemoteException
  {
    if (zeile3 != null)
    {
      return zeile3;
    }
    zeile3 = new TextInput(getSpendenbescheinigung().getZeile3(), 80);
    zeile3.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile3;
  }

  public TextInput getZeile4() throws RemoteException
  {
    if (zeile4 != null)
    {
      return zeile4;
    }
    zeile4 = new TextInput(getSpendenbescheinigung().getZeile4(), 80);
    zeile4.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile4;
  }

  public TextInput getZeile5() throws RemoteException
  {
    if (zeile5 != null)
    {
      return zeile5;
    }
    zeile5 = new TextInput(getSpendenbescheinigung().getZeile5(), 80);
    zeile5.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile5;
  }

  public TextInput getZeile6() throws RemoteException
  {
    if (zeile6 != null)
    {
      return zeile6;
    }
    zeile6 = new TextInput(getSpendenbescheinigung().getZeile6(), 80);
    zeile6.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile6;
  }

  public TextInput getZeile7() throws RemoteException
  {
    if (zeile7 != null)
    {
      return zeile7;
    }
    zeile7 = new TextInput(getSpendenbescheinigung().getZeile7(), 80);
    zeile7.setEnabled(getSpendenbescheinigung().isNewObject());
    return zeile7;
  }

  public DateInput getSpendedatum() throws RemoteException
  {
    if (spendedatum != null)
    {
      return spendedatum;
    }
    spendedatum = new DateInput(getSpendenbescheinigung().getSpendedatum());
    spendedatum.setEnabled(false);
    return spendedatum;
  }

  public DateInput getBescheinigungsdatum() throws RemoteException
  {
    if (bescheinigungsdatum != null)
    {
      return bescheinigungsdatum;
    }
    bescheinigungsdatum = new DateInput(
        getSpendenbescheinigung().getBescheinigungsdatum());
    bescheinigungsdatum.setEnabled(getSpendenbescheinigung().isNewObject());
    return bescheinigungsdatum;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getSpendenbescheinigung().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setEnabled(false);
    return betrag;
  }

  public SelectInput getFormular() throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    String def = null;
    if (getSpendenbescheinigung().getFormular() != null)
    {
      def = getSpendenbescheinigung().getFormular().getID();
    }
    if (getSpendenbescheinigung().getSpendenart() == Spendenart.SACHSPENDE)
    {
      formular = new FormularInput(FormularArt.SACHSPENDENBESCHEINIGUNG, def);
      // Wegen kompatibilität zu früher
      if (def != null)
      {
        Formular f = getSpendenbescheinigung().getFormular();
        @SuppressWarnings("unchecked")
        List<Formular> list = formular.getList();

        // Contains geht bei RemoteObject nicht, muss über BeanUtil gemacht
        // werden
        boolean found = false;
        for (Formular fo : list)
        {
          if (BeanUtil.equals(fo, f))
          {
            found = true;
            break;
          }
        }
        if (!found)
        {
          list.add(f);
          formular.setList(list);
        }
      }
    }
    else if (getSpendenbescheinigung().getBuchungen().size() > 1)
    {
      formular = new FormularInput(FormularArt.SAMMELSPENDENBESCHEINIGUNG, def);
    }
    else
    {
      formular = new FormularInput(FormularArt.SPENDENBESCHEINIGUNG, def);
    }
    formular.setPleaseChoose("Standard");
    return formular;
  }

  public CheckboxInput getErsatzAufwendungen() throws RemoteException
  {
    if (ersatzaufwendungen != null)
    {
      return ersatzaufwendungen;
    }
    List<Buchung> buchungen = getSpendenbescheinigung().getBuchungen();
    boolean check = false;
    if (buchungen != null && buchungen.size() == 1
        && getSpendenbescheinigung().getSpendenart() == Spendenart.GELDSPENDE)
    {
      // Verzicht aus Buchung lesen
      check = buchungen.get(0).getVerzicht();
    }
    ersatzaufwendungen = new CheckboxInput(check);
    if (buchungen != null && buchungen.size() > 1
        && getSpendenbescheinigung().getSpendenart() == Spendenart.GELDSPENDE)
    {
      // Sammelspendenbescheinigung
      ersatzaufwendungen.setName("*siehe Buchungsliste");
    }
    ersatzaufwendungen.disable();
    return ersatzaufwendungen;
  }

  public TextAreaInput getBezeichnungSachzuwendung() throws RemoteException
  {
    if (bezeichnungsachzuwendung != null)
    {
      return bezeichnungsachzuwendung;
    }
    bezeichnungsachzuwendung = new TextAreaInput(
        getSpendenbescheinigung().getBezeichnungSachzuwendung(), 1000);
    bezeichnungsachzuwendung.setHeight(50);
    bezeichnungsachzuwendung.disable();
    return bezeichnungsachzuwendung;
  }

  public SelectInput getHerkunftSpende() throws RemoteException
  {
    if (herkunftspende != null)
    {
      return herkunftspende;
    }
    herkunftspende = new SelectInput(HerkunftSpende.getArray(),
        new HerkunftSpende(getSpendenbescheinigung().getHerkunftSpende()));
    herkunftspende.disable();
    return herkunftspende;
  }

  public CheckboxInput getUnterlagenWertermittlung() throws RemoteException
  {
    if (unterlagenwertermittlung != null)
    {
      return unterlagenwertermittlung;
    }
    unterlagenwertermittlung = new CheckboxInput(
        getSpendenbescheinigung().getUnterlagenWertermittlung());
    unterlagenwertermittlung.disable();
    return unterlagenwertermittlung;
  }

  public DateInput getVersanddatum() throws RemoteException
  {
    if (versanddatum != null)
    {
      return versanddatum;
    }
    versanddatum = new DateInput(getSpendenbescheinigung().getVersanddatum());
    versanddatum.setName("Versand Datum");
    versanddatum.disable();
    return versanddatum;
  }

  @Override
  public JVereinTablePart getTablePart() throws RemoteException
  {
    return new BuchungListPart(getSpendenbescheinigung().getBuchungen(),
        new BuchungAction(false), new BuchungPartAnzeigenMenu());
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Spendenbescheinigung spb = getSpendenbescheinigung();
    Spendenart spa = (Spendenart) getSpendenart().getValue();
    spb.setMitglied((Mitglied) getMitglied().getValue());
    spb.setSpendenart(spa.getKey());
    spb.setZeile1((String) getZeile1(false).getValue());
    spb.setZeile2((String) getZeile2().getValue());
    spb.setZeile3((String) getZeile3().getValue());
    spb.setZeile4((String) getZeile4().getValue());
    spb.setZeile5((String) getZeile5().getValue());
    spb.setZeile6((String) getZeile6().getValue());
    spb.setZeile7((String) getZeile7().getValue());
    spb.setSpendedatum((Date) getSpendedatum().getValue());
    spb.setBescheinigungsdatum((Date) getBescheinigungsdatum().getValue());
    spb.setBetrag((Double) getBetrag().getValue());
    spb.setBezeichnungSachzuwendung(
        (String) getBezeichnungSachzuwendung().getValue());
    spb.setFormular((Formular) getFormular().getValue());
    HerkunftSpende hsp = (HerkunftSpende) getHerkunftSpende().getValue();
    spb.setHerkunftSpende(hsp.getKey());
    spb.setUnterlagenWertermittlung(
        (Boolean) getUnterlagenWertermittlung().getValue());
    spb.setVersanddatum((Date) getVersanddatum().getValue());
    return spb;
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
      String fehler = "Fehler bei Speichern der Spendenbescheinigung";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public ButtonRtoL getDruckUndMailButton()
  {
    ButtonRtoL b = new ButtonRtoL("Druck und Mail", c -> {
      Spendenbescheinigung spb = getSpendenbescheinigung();
      try
      {
        if (spb.isNewObject())
        {
          GUI.getStatusBar()
              .setErrorText("Spendenbescheinigung bitte erst speichern!");
          return;
        }
      }
      catch (RemoteException e)
      {
        Logger.error(e.getMessage());
        throw new ApplicationException(
            "Fehler bei der Aufbereitung der Spendenbescheinigung");
      }
      GUI.startView(SpendenbescheinigungMailView.class,
          new Spendenbescheinigung[] { getSpendenbescheinigung() });
    }, getSpendenbescheinigung(), false, "document-print.png");
    return b;
  }

  private class MitgliedListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Mitglied selected = (Mitglied) getMitglied().getValue();
        if (selected != null)
        {
          SpbAdressaufbereitung.adressaufbereitung(selected,
              spendenbescheinigung);
          zeile1.setValue(spendenbescheinigung.getZeile1());
          zeile2.setValue(spendenbescheinigung.getZeile2());
          zeile3.setValue(spendenbescheinigung.getZeile3());
          zeile4.setValue(spendenbescheinigung.getZeile4());
          zeile5.setValue(spendenbescheinigung.getZeile5());
          zeile6.setValue(spendenbescheinigung.getZeile6());
          zeile7.setValue(spendenbescheinigung.getZeile7());
        }
        else
        {
          spendenbescheinigung.setMitglied((Integer) null);
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Setzen des Mitglieds:", e);
      }
    }
  }

}
